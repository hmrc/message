/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.message.controllers

import org.apache.pekko.util.ByteString
import org.bson.types.ObjectId

import play.api.Logging
import play.api.http.HttpEntity
import play.api.i18n.*
import play.api.libs.json.*
import play.api.mvc.*
import uk.gov.hmrc.common.message.DateValidationException
import uk.gov.hmrc.message.connectors.SecureMessageConnector
import uk.gov.hmrc.message.controllers.model.*
import uk.gov.hmrc.common.message.validationmodule.MessageValidator.isValidMessage
import uk.gov.hmrc.message.controllers.util.MessagesUtil
import uk.gov.hmrc.common.message.failuremodule.FailureResponseService.errorResponseResult
import uk.gov.hmrc.common.message.model.*
import uk.gov.hmrc.http.UpstreamErrorResponse.{ Upstream4xxResponse, Upstream5xxResponse }
import uk.gov.hmrc.play.audit.model.EventTypes
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{ Inject, Singleton }
import scala.concurrent.{ ExecutionContext, Future }
import scala.reflect.ClassTag
import scala.util.{ Failure, Success, Try }

@Singleton
class MessagesController @Inject() (
  seucreMessageConnector: SecureMessageConnector,
  messagesUtil: MessagesUtil,
  cc: MessagesControllerComponents
)(implicit ec: ExecutionContext)
    extends BackendController(cc) with Logging with I18nSupport {

  override protected def withJsonBody[T](
    f: T => Future[Result]
  )(implicit request: Request[JsValue], c: ClassTag[T], reads: Reads[T]): Future[Result] =
    Try(request.body.validate[T]) match {
      case Success(JsSuccess(payload, _)) => f(payload)

      case Success(JsError(errs)) =>
        Future.successful(errorResponseResult(errs.headOption.fold("Unknown") { case (_, validationErrors) =>
          val errorMessage = validationErrors.headOption.fold("Unknown") { errors =>
            errors.messages
              .find(_.startsWith("The backend has rejected the message due to an unknown tax identifier."))
              .getOrElse(errors.messages.toString)
          }

          messagesUtil.auditCreateMessageForFailure(errorMessage)
          errorMessage
        }))

      case Failure(e) if e.isInstanceOf[DateValidationException] =>
        Future.successful(messagesUtil.buildBadRequest(e.getMessage))

      case Failure(e) =>
        Future.successful(messagesUtil.buildBadRequest(s"could not parse body due to ${e.getMessage}"))
    }

  def createMessage(): Action[JsValue] = Action.async(parse.json) {
    implicit val messageV3RESTFormats: Reads[Message] =
      uk.gov.hmrc.message.controllers.model.MessageV3RESTFormats.messageApiV3Reads

    implicit request =>
      withJsonBody[Message] { message =>
        isValidMessage(message) match {
          case Success(_) =>
            seucreMessageConnector.postMessage(request.body).map { response =>
              val status = response.status
              val id = (response.json \ "id").as[String]
              val headers = response.headers.view.mapValues(_.headOption.getOrElse("")).toMap
              val body = response.body
              messagesUtil.auditCreateMessageFor(
                EventTypes.Succeeded,
                message.copy(id = ObjectId(id)),
                "Message successfully created by 'secure-message' service"
              )
              Result(
                header = ResponseHeader(status, headers),
                body = HttpEntity.Strict(ByteString(body), response.header("contentType"))
              )
            } recover {
              case Upstream4xxResponse(error) =>
                errorResponseResult(error.message, error.statusCode, showErrorID = true)

              case Upstream5xxResponse(error) =>
                errorResponseResult(error.message, error.statusCode, showErrorID = true)

              case error =>
                errorResponseResult(error.getMessage, INTERNAL_SERVER_ERROR, showErrorID = true)
            }

          case Failure(e) =>
            Future.successful(messagesUtil.buildBadRequest(e.getMessage))
        }
      }
  }
}
