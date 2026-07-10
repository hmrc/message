/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.message.controllers.model

import play.api.libs.json.{ JsString, JsSuccess, Json, Reads }
import uk.gov.hmrc.common.message.DateValidationException
import uk.gov.hmrc.message.util.SpecBase

import java.time.LocalDate

class PackageSpec extends SpecBase {

  "javaDateReads()" should {
    "read the date string correctly" in {
      implicit val reads: Reads[LocalDate] = javaDateReads()

      Json.fromJson(JsString("""2026-02-15""")) mustBe JsSuccess(LocalDate.of(2026, 2, 15))
    }

    "throw exception for invalid date" in {
      implicit val reads: Reads[LocalDate] = javaDateReads()

      intercept[DateValidationException] {
        Json.fromJson(JsString("""2026-20-15"""))
      }
    }
  }

  "javaDateReads with parameter fieldName" should {
    "read the date string correctly" in {
      implicit val reads: Reads[LocalDate] = javaDateReads("2026-02-15")

      Json.fromJson(JsString("""2026-02-15""")) mustBe JsSuccess(LocalDate.of(2026, 2, 15))
    }

    "throw exception for invalid date" in {
      implicit val reads: Reads[LocalDate] = javaDateReads("2026-02-15")

      intercept[DateValidationException] {
        Json.fromJson(JsString("""2026-20-15"""))
      }
    }
  }
}
