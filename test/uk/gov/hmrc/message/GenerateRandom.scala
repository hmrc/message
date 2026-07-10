/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.message

import uk.gov.hmrc.domain.{ NinoGenerator, SaUtrGenerator }

import java.util.UUID
import scala.util.Random

object GenerateRandom {
  val rand = new Random()
  val ninoGenerator = new NinoGenerator(rand)
  val utrGenerator = new SaUtrGenerator(rand)

  def email() = s"${UUID.randomUUID()}@TEST.com"

  def utr() = utrGenerator.nextSaUtr

  def nino() = ninoGenerator.nextNino

}
