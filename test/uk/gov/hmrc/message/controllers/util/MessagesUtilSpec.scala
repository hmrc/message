/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.message.controllers.util

import org.scalatest.concurrent.ScalaFutures
import play.api.libs.json.{ JsObject, JsString, JsValue, Json }
import play.api.mvc.Result
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.{ Nino, SaUtr }
import uk.gov.hmrc.message.GenerateRandom
import uk.gov.hmrc.message.util.SpecBase
import uk.gov.hmrc.play.audit.http.connector.{ AuditConnector, AuditResult }
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.Application
import play.api.http.Status.{ BAD_REQUEST, INTERNAL_SERVER_ERROR }
import play.api.test.Helpers.*
import uk.gov.hmrc.common.message.model.Message
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.message.util.TestData.*
import uk.gov.hmrc.play.audit.http.connector.AuditResult.{ Disabled, Failure, Success }

import java.time.LocalDate
import scala.concurrent.{ ExecutionContext, Future }

class MessagesUtilSpec extends SpecBase with ScalaFutures {

  "handleBiggerContent function" must {
    "replace sourceData and content values with alternative text if both exists" in new TestCase {
      val message: JsObject =
        Json
          .parse(s"""
                    | {
                    |   "sourceData": "Test subject",
                    |   "foo": "not important",
                    |   "content": "test content"
                    | }
    """.stripMargin)
          .as[JsObject]

      Json.parse(messagesUtil.handleBiggerContent(message)) mustBe
        Json.parse(s"""
                      | {
                      |   "sourceData": "sourceData is removed to reduce size",
                      |   "foo": "not important",
                      |   "content": "content is removed to reduce size"
                      | }
    """.stripMargin)
    }

    "replace content with alternative text if only content is present" in new TestCase {
      val message: JsObject =
        Json
          .parse(s"""
                    | {
                    |   "foo": "not important",
                    |   "content": "test content"
                    | }
    """.stripMargin)
          .as[JsObject]

      Json.parse(messagesUtil.handleBiggerContent(message)) mustBe
        Json.parse(s"""
                      | {
                      |   "foo": "not important",
                      |   "content": "content is removed to reduce size"
                      | }
    """.stripMargin)
    }

    "replace sourceData with alternative text only if sourceData is present" in new TestCase {
      val message: JsObject =
        Json
          .parse(s"""
                    | {
                    |   "foo": "not important",
                    |   "sourceData": "Test subject"
                    | }
    """.stripMargin)
          .as[JsObject]

      Json.parse(messagesUtil.handleBiggerContent(message)) mustBe
        Json.parse(s"""
                      | {
                      |   "foo": "not important",
                      |   "sourceData": "sourceData is removed to reduce size"
                      | }
    """.stripMargin)
    }

    "return message data as it is if both sourceData and content are missing" in new TestCase {
      val message: JsObject =
        Json
          .parse(s"""
                    | {
                    |   "foo": "not important"
                    | }
    """.stripMargin)
          .as[JsObject]

      Json.parse(messagesUtil.handleBiggerContent(message)) mustBe
        Json.parse(s"""
                      | {
                      |   "foo": "not important"
                      | }
    """.stripMargin)
    }
  }

  "buildBadRequest" must {
    "return correct output status code" when {

      "error message is of invalid payload" in new TestCase {
        when(mockAuditConnector.sendEvent(any)(any, any)).thenReturn(Future.successful(Success))

        val result: Result = messagesUtil.buildBadRequest("Submission has not passed validation. Invalid payload.")

        result.header.status must be(BAD_REQUEST)
      }

      "error message is not in the predefined error list" in new TestCase {
        when(mockAuditConnector.sendEvent(any)(any, any)).thenReturn(Future.successful(Success))

        val result: Result = messagesUtil.buildBadRequest("Error occurred")

        result.header.status must be(INTERNAL_SERVER_ERROR)
      }
    }
  }

  "auditUpdatedMessageFor" must {
    "successfully send the event" in new TestCase {
      when(mockAuditConnector.sendEvent(any)(any, any)).thenReturn(Future.successful(Success))

      val result: AuditResult = await(messagesUtil.auditUpdatedMessageFor(messageOb, "test_transaction"))

      result must be(Disabled)
    }
  }

  "auditCreateMessageForFailure" must {
    "log the error" when {
      "sendEvent returns failure" in new TestCase {
        lazy val application: Application = applicationBuilder
          .configure(
            "metrics.enabled"   -> "false",
            "auditEventMaxSize" -> 6
          )
          .build()

        val jsonString: String =
          """{
            |  "externalRef": {
            |    "id": "abcd1234",
            |    "source": "gmc"
            |  },
            |  "recipient": {
            |    "taxIdentifier": {
            |      "name": "sautr",
            |      "value": "5000061334"
            |    },
            |    "name": {
            |      "title": "Dr",
            |      "forename": "Bruce",
            |      "secondForename": "Hulk",
            |      "surname": "Banner",
            |      "honours": "Green",
            |      "line1": "Line1"
            |    }
            |  },
            |  "messageType": "mailout-batch",
            |  "subject": "Reminder to file a Self Assessment return",
            |  "content": "PGgxPlRlc3QgY29udGVudDwvaDE+IDxzY3JpcHQ+d2luZG93LmFsZXJ0KCJoZWxsbyIpPC9zY3JpcHQ+IDxwPmJvZHk8L3A+",
            |  "validFrom": "2020-05-04",
            |  "details": {
            |    "formId": "SA316",
            |    "statutory": true,
            |    "paperSent": false,
            |    "sourceData": "abc",
            |    "batchId": "1234"
            |  }
            |}""".stripMargin

        val request: FakeRequest[JsValue] = FakeRequest("GET", "/").withBody[JsValue](Json.parse(jsonString))
        val headerCarrier: HeaderCarrier = HeaderCarrier()

        val msgUtil: MessagesUtil = application.injector.instanceOf[MessagesUtil]

        when(mockAuditConnector.sendEvent(any)(any, any)).thenReturn(Future.successful(Failure("error occurred")))

        await(msgUtil.auditCreateMessageForFailure("test_transaction")(headerCarrier, request)) mustBe ()
      }
    }
  }

  trait TestCase {
    val utr: SaUtr = GenerateRandom.utr()
    val nino: Nino = GenerateRandom.nino()
    val messageOb: Message = TEST_MESSAGE.copy(recipient = TEST_RECIPIENT, externalRef = None, tags = None)

    val mockAuditConnector: AuditConnector = mock[AuditConnector]

    implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

    implicit val fakeRequest: FakeRequest[JsValue] = FakeRequest("GET", "/").withBody[JsValue](JsString("""{test}"""))

    implicit val hc: HeaderCarrier = HeaderCarrier()

    lazy val messagesUtil: MessagesUtil = app.injector.instanceOf[MessagesUtil]
  }
}
