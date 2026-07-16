/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
