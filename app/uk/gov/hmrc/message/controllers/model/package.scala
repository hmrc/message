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

package uk.gov.hmrc.message.controllers

import uk.gov.hmrc.common.message.DateValidationException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import play.api.libs.json.Reads
import scala.util.{ Failure, Success, Try }

package object model {
  private val defaultJavaDateFormat = "yyyy-MM-dd"

  def javaDateReads(): Reads[LocalDate] =
    Reads[LocalDate](js =>
      js.validate[String]
        .map[LocalDate](dtString =>
          Try {
            LocalDate.parse(dtString, DateTimeFormatter.ofPattern(defaultJavaDateFormat))
          } match {
            case Success(reads) => reads
            case Failure(_)     => throw DateValidationException("Invalid date format provided")
          }
        )
    )

  def javaDateReads(fieldName: String): Reads[LocalDate] =
    Reads[LocalDate](js =>
      js.validate[String]
        .map[LocalDate](dtString =>
          Try {
            LocalDate.parse(dtString, DateTimeFormatter.ofPattern(defaultJavaDateFormat))
          } match {
            case Success(reads) => reads
            case Failure(_)     => throw DateValidationException(s"$fieldName: invalid date format provided")
          }
        )
    )
}
