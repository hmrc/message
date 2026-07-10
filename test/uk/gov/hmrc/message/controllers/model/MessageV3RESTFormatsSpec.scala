/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.message.controllers.model

import org.scalatest.matchers.should.Matchers.shouldBe
import play.api.libs.json.{ JsResultException, Json }
import uk.gov.hmrc.common.message.model.Message
import uk.gov.hmrc.message.util.SpecBase

class MessageV3RESTFormatsSpec extends SpecBase {

  "messageApiV3Reads" should {

    import MessageV3RESTFormats.messageApiV3Reads

    "read the json correctly" in new Setup {
      Json.parse(messageJsonString).as[Message] shouldBe a[Message]
    }

    "throw exception for invalid json" in new Setup {
      intercept[JsResultException] {
        Json.parse(messageInvalidJsonString).as[Message]
      }
    }
  }

  trait Setup {
    val messageJsonString: String =
      """{
        |   "externalRef":{
        |       "id":"test_id",
        |       "source":"test_source"
        |   },
        |   "recipient":{
        |       "taxIdentifier":{
        |           "name":"sautr",
        |           "value":"12345678"
        |       }
        |   },
        |   "validFrom": "2026-02-22",
        |   "alertFrom": "2026-02-22",
        |   "messageType":"fhddsAlertMessage",
        |   "subject":"test_subject",
        |   "content":"SGVsbG8gV29ybGQ=",
        |   "alertQueue":"PRIORITY",
        |   "alertDetails": {
        |    "templateId": "test_id",
        |    "data": {}
        |  },
        |  "tags": {
        |    "notificationType": "Direct Debit"
        |  }
       }""".stripMargin

    val messageInvalidJsonString: String =
      """{
        |   "recipient":{
        |       "taxIdentifier":{
        |           "name":"sautr",
        |           "value":"12345678"
        |       }
        |   },
        |   "validFrom": "2026-02-22",
        |   "alertFrom": "2026-02-22",
        |   "messageType":"fhddsAlertMessage",
        |   "subject":"test_subject",
        |   "content":"SGVsbG8gV29ybGQ=",
        |   "alertQueue":"PRIORITY",
        |   "alertDetails": {
        |    "templateId": "test_id",
        |    "data": {}
        |  },
        |  "tags": {
        |    "notificationType": "Direct Debit"
        |  }
          }""".stripMargin

    val messageInvalidJsonString1: String =
      """{
        |   "recipient":{
        |       "taxIdentifier":{
        |           "name":"sautr",
        |           "value":"12345678"
        |       }
        |   },
        |   "validFrom": "2026-02-22",
        |   "alertFrom": "2026-02-22",
        |   "messageType":"fhddsAlertMessage",
        |   "subject":"test_subject",
        |   "content":"SGVsbG8gV29ybGQ=",
        |   "alertQueue":"PRIORITY",
        |   "alertDetails": {
        |    "templateId": "test_id"
        |  },
        |  "tags": {
        |    "notificationType": "Direct Debit"
        |  }
              }""".stripMargin
  }
}
