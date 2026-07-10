/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.message.connectors

import play.api.libs.json.JsValue
import play.api.libs.ws.writeableOf_JsValue
import play.api.Logging
import play.mvc.Http.Status
import uk.gov.hmrc.common.message.util.SecureMessageUtil
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{ HeaderCarrier, HttpResponse, UpstreamErrorResponse }
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.net.URI
import javax.inject.{ Inject, Singleton }
import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class SecureMessageConnector @Inject() (httpClient: HttpClientV2, servicesConfig: ServicesConfig)(implicit
  ec: ExecutionContext
) extends Logging {

  private val secureMessageBackendUrl: String =
    s"${servicesConfig.baseUrl("secure-message")}/secure-messaging/v4/message"

  private val requestBodyV4 = (body: JsValue) => SecureMessageUtil.createSecureMessage(body)

  def postMessage(body: JsValue)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    logger.warn(s"CreateMessage: post message to $secureMessageBackendUrl")
    httpClient
      .post(URI(secureMessageBackendUrl).toURL)
      .withBody(requestBodyV4(body))
      .execute[HttpResponse]
      .flatMap { response =>
        response.status match {
          case Status.OK | Status.CREATED => Future.successful(response)
          case failedStatus               => throw UpstreamErrorResponse(response.body, response.status)
        }
      }
  }
}
