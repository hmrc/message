/*
 * Copyright 2024 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.message.controllers

import com.github.tomakehurst.wiremock.client.WireMock.*
import org.apache.pekko.stream.Materializer
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.inject
import play.api.inject.Injector
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{ JsValue, Json }
import play.api.test.Helpers.*
import play.api.test.{ FakeHeaders, FakeRequest, Helpers }
import uk.gov.hmrc.common.message.util.SecureMessageUtil

import scala.concurrent.{ Await, ExecutionContext }
import scala.concurrent.duration.*
import uk.gov.hmrc.message.util.*
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import scala.concurrent.Future
import org.mockito.ArgumentMatchers.any
import uk.gov.hmrc.play.audit.http.connector.AuditResult.Success

class MessageControllerV4Spec
    extends PlaySpec with MockitoSugar with WithWireMock with ScalaFutures with GuiceOneAppPerSuite {

  override def dependenciesPort: Int =
    app.configuration
      .getOptional[Int]("microservice.services.secure-message.port")
      .getOrElse(throw new Exception("Port missing for message"))

  implicit val mat: Materializer = app.injector.instanceOf[Materializer]
  implicit val ec: ExecutionContext = app.injector.instanceOf[ExecutionContext]

  private val injector: Injector = new GuiceApplicationBuilder()
    .configure("securemessage.connection.active" -> "true")
    .injector()

  lazy val messageController: MessagesController = injector.instanceOf[MessagesController]

  "Create V4 message" must {
    "successfully create v4 message for v3 payload " in {
      val message = Resources.readJson("messages/controller/v3/GMC.json")
      val v3Message = Json.toJson(message)
      val v4Message = SecureMessageUtil.createSecureMessage(v3Message)

      givenThat(
        post(urlEqualTo("/secure-messaging/v4/message"))
          .withRequestBody(equalTo(v4Message.toString))
          .willReturn(aResponse().withBody("""{"id":"6716c46562042f3c83323de4"}""").withStatus(CREATED))
      )
      val request = FakeRequest(Helpers.POST, routes.MessagesController.createMessage().url, FakeHeaders(), v3Message)

      val response = Await.result(messageController.createMessage()(request), 3.seconds)
      response.body.consumeData.map(s => s.utf8String mustBe """{"id":"6716c46562042f3c83323de4"}""")
      response.header.status mustBe CREATED
    }

    "return BAD_REQUEST when secureMessageConnector throws 4xx upstream error" in {
      val message = Resources.readJson("messages/controller/v3/GMC.json")
      val v3Message = Json.toJson(message)
      val v4Message = SecureMessageUtil.createSecureMessage(v3Message)

      givenThat(
        post(urlEqualTo("/secure-messaging/v4/message"))
          .withRequestBody(equalTo(v4Message.toString))
          .willReturn(badRequest.withBody("Submission has not passed validation. Invalid payload."))
      )
      val request = FakeRequest(Helpers.POST, routes.MessagesController.createMessage().url, FakeHeaders(), v3Message)

      val response = Await.result(messageController.createMessage()(request), 3.seconds)
      response.body.consumeData.map(s => s.utf8String mustBe """{"id":"6716c46562042f3c83323de4"}""")
      response.header.status mustBe BAD_REQUEST
    }

    "return INTERNAL_SERVER_ERROR when secureMessageConnector throws 5xx upstream error" in {
      val message = Resources.readJson("messages/controller/v3/GMC.json")
      val v3Message = Json.toJson(message)
      val v4Message = SecureMessageUtil.createSecureMessage(v3Message)

      givenThat(
        post(urlEqualTo("/secure-messaging/v4/message"))
          .withRequestBody(equalTo(v4Message.toString))
          .willReturn(serverError.withBody("Unknown eis error"))
      )
      val request = FakeRequest(Helpers.POST, routes.MessagesController.createMessage().url, FakeHeaders(), v3Message)

      val response = Await.result(messageController.createMessage()(request), 3.seconds)
      response.body.consumeData.map(s => s.utf8String mustBe """{"id":"6716c46562042f3c83323de4"}""")
      response.header.status mustBe INTERNAL_SERVER_ERROR
    }

    "return INTERNAL_SERVER_ERROR when secureMessageConnector throws upstream error with undefined error message" in {
      val message = Resources.readJson("messages/controller/v3/GMC.json")
      val v3Message = Json.toJson(message)
      val v4Message = SecureMessageUtil.createSecureMessage(v3Message)

      givenThat(
        post(urlEqualTo("/secure-messaging/v4/message"))
          .withRequestBody(equalTo(v4Message.toString))
          .willReturn(noContent.withBody("Unknown error"))
      )
      val request = FakeRequest(Helpers.POST, routes.MessagesController.createMessage().url, FakeHeaders(), v3Message)

      val response = Await.result(messageController.createMessage()(request), 3.seconds)
      response.body.consumeData.map(s => s.utf8String mustBe """{"id":"6716c46562042f3c83323de4"}""")
      response.header.status mustBe INTERNAL_SERVER_ERROR
    }

    "return BAD_REQUEST for invalid message" in {
      val message = Resources.readJson("messages/controller/v3/Invalid_Email_Message.json")
      val v3Message = Json.toJson(message)

      val request = FakeRequest(Helpers.POST, routes.MessagesController.createMessage().url, FakeHeaders(), v3Message)

      val response = Await.result(messageController.createMessage()(request), 3.seconds)

      response.body.consumeData.map(s => s.utf8String mustBe """{"id":"6716c46562042f3c83323de4"}""")
      response.header.status mustBe BAD_REQUEST
    }

    "return INTERNAL_SERVER_ERROR for invalid request " in {
      val mockAuditConnector: AuditConnector = mock[AuditConnector]

      val message = Resources.readJson("messages/controller/v3/Invalid_Message.json")
      val v3Message = Json.toJson(message)

      when(mockAuditConnector.sendEvent(any)(any, any)).thenReturn(Future.successful(Success))

      val request = FakeRequest(Helpers.POST, routes.MessagesController.createMessage().url, FakeHeaders(), v3Message)

      val response = Await.result(messageController.createMessage()(request), 3.seconds)

      response.body.consumeData.map(s => s.utf8String mustBe """{"id":"6716c46562042f3c83323de4"}""")
      response.header.status mustBe INTERNAL_SERVER_ERROR
    }

    "return BAD_REQUEST for request with invalid date" in {
      val mockAuditConnector: AuditConnector = mock[AuditConnector]

      val message = Resources.readJson("messages/controller/v3/Invalid_Date_Message.json")
      val v3Message = Json.toJson(message)

      when(mockAuditConnector.sendEvent(any)(any, any)).thenReturn(Future.successful(Success))

      val request = FakeRequest(Helpers.POST, routes.MessagesController.createMessage().url, FakeHeaders(), v3Message)

      val response = Await.result(messageController.createMessage()(request), 3.seconds)

      response.body.consumeData.map(s => s.utf8String mustBe """{"id":"6716c46562042f3c83323de4"}""")
      response.header.status mustBe BAD_REQUEST
    }
  }
}
