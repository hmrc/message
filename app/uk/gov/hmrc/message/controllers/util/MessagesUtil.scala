/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.message.controllers.util

import java.time.Instant
import play.api.http.Status._
import play.api.libs.json._
import play.api.mvc.{ Request, Result }
import play.api.Logger
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.common.message.failuremodule.FailureResponseService.errorResponseResult
import uk.gov.hmrc.common.message.model.{ Message, TaxEntity }
import uk.gov.hmrc.play.audit.http.connector.{ AuditConnector, AuditResult }
import uk.gov.hmrc.play.audit.model.{ DataEvent, EventTypes }

import javax.inject.{ Inject, Named }
import scala.concurrent.{ ExecutionContext, Future }
import scala.language.postfixOps

class MessagesUtil @Inject() (
  @Named("app-name") appName: String,
  audit: AuditConnector,
  @Named("audit-event-max-size") auditEventMaxSize: Int
)(implicit ec: ExecutionContext) {
  val logger: Logger = Logger(this.getClass)

  def buildBadRequest(errorMessage: String)(implicit request: Request[JsValue], hc: HeaderCarrier): Result = {
    logger.warn(s"Bad request: reason: $errorMessage")
    auditCreateMessageForFailure(errorMessage)
    errorResponseResult(errorMessage)
  }

  def auditUpdatedMessageFor(m: Message, transactionName: String)(implicit hc: HeaderCarrier): Future[AuditResult] =
    audit.sendEvent(
      DataEvent(
        auditSource = appName,
        auditType = EventTypes.Succeeded,
        tags = Map("transactionName" -> transactionName),
        detail = Map("messageId" -> m.id.toString) ++
          Map("source" -> m.externalRef.map(_.source).getOrElse("")) ++
          Map("templateId" -> m.alertDetails.templateId) ++
          m.body.map(_.form.map("formId" -> _).toMap).getOrElse(Map.empty) ++
          m.body.map(_.`type`.map("messageType" -> _).toMap).getOrElse(Map.empty) ++
          TaxEntity.forAudit(m.recipient)
      )
    )

  def auditCreateMessageFor(auditType: String, m: Message, transactionName: String)(implicit
    hc: HeaderCarrier,
    request: Request[JsValue]
  ): Future[Unit] = {

    val params = Map(
      "batchId"     -> m.body.flatMap(_.batchId),
      "replyTo"     -> m.body.flatMap(_.replyTo),
      "threadId"    -> m.body.flatMap(_.threadId),
      "enquiryType" -> m.body.flatMap(_.enquiryType),
      "adviser"     -> m.body.flatMap(_.adviser).map(_.pidId),
      "topic"       -> m.body.flatMap(_.topic)
    )

    audit
      .sendEvent(
        DataEvent(
          auditSource = appName,
          auditType = auditType,
          tags = Map("transactionName" -> transactionName),
          detail = Map(
            "messageId"                 -> m.id.toString,
            "formId"                    -> m.body.flatMap(_.form).getOrElse(""),
            "messageType"               -> m.body.flatMap(_.`type`).getOrElse(m.alertDetails.templateId),
            m.recipient.identifier.name -> m.recipient.identifier.value,
            "originalRequest" -> {
              val requestStr = Json.stringify(request.body)
              if (requestStr.length > auditEventMaxSize) {
                val truncatedRequest = handleBiggerContent(request.body)
                if (truncatedRequest.length > auditEventMaxSize)
                  "request is too big even without content and sourceData"
                else truncatedRequest
              } else requestStr
            }
          ) ++ params.collect { case (k, Some(v)) => k -> v } ++ getOptionalTagValue(NotificationType, m.tags)
        )
      )
      .map {
        case AuditResult.Disabled => logger.warn(s"Audit disabled for create message with id: ${m.id.toString}")
        case AuditResult.Success  => logger.trace("Successful Audit for create message")
        case AuditResult.Failure(msg, _) =>
          logger.error(s"Unable to send an audit event for messageId: ${m.id.toString} : $msg")
      }
  }

  def auditCreateMessageForFailure(transactionName: String)(implicit
    hc: HeaderCarrier,
    request: Request[JsValue]
  ): Future[Unit] =
    audit
      .sendEvent(
        DataEvent(
          auditSource = appName,
          auditType = EventTypes.Failed,
          tags = Map("transactionName" -> transactionName),
          detail = Map(
            "originalRequest" -> {
              val requestStr = Json.stringify(request.body)
              if (requestStr.length > auditEventMaxSize) {
                val truncatedRequest = handleBiggerContent(request.body)
                if (truncatedRequest.length > auditEventMaxSize)
                  "request is too big even without content and sourceData"
                else truncatedRequest
              } else requestStr
            }
          )
        )
      )
      .map {
        case AuditResult.Disabled => logger.warn(s"Audit disabled for request id: ${request.id}")
        case AuditResult.Success  => logger.trace("Successful Audit for failed request")
        case AuditResult.Failure(msg, _) =>
          logger.error(s"Unable to send an audit event for messageId: ${request.id} : $msg")
      }

  def handleBiggerContent(body: JsValue): String = {
    val sourceDataAlternativeText = "sourceData is removed to reduce size"
    val contentAlternativeText = "content is removed to reduce size"
    val bodyObj = body.as[JsObject]
    Json.stringify((bodyObj.keys.contains("sourceData"), bodyObj.keys.contains("content")) match {
      case (true, true) =>
        bodyObj ++ Json.obj("sourceData" -> sourceDataAlternativeText, "content" -> contentAlternativeText)
      case (false, true) => bodyObj ++ Json.obj("content" -> contentAlternativeText)
      case (true, false) => bodyObj ++ Json.obj("sourceData" -> sourceDataAlternativeText)
      case _             => bodyObj
    })
  }

  private def getOptionalTagValue(key: String, tags: Option[Map[String, String]]): Map[String, String] =
    (for {
      m <- tags
      v <- m.get(key)
    } yield (key, v)) toMap

  private val NotificationType = "notificationType"

}
