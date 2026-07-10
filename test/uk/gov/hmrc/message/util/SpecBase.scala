/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.message.util

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.Application

trait SpecBase extends AnyWordSpec with Matchers with MockitoSugar {
  lazy val applicationBuilder: GuiceApplicationBuilder = new GuiceApplicationBuilder()

  lazy val app: Application = applicationBuilder
    .configure(
      "metrics.enabled" -> "false"
    )
    .build()
}
