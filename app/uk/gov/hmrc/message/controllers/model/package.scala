/*
 * Copyright 2023 HM Revenue & Customs
 *
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
