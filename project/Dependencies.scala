/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

import sbt.*

object Dependencies {

  private val bootstrapVersion = "10.7.0"

  lazy val appDependencies: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "bootstrap-backend-play-30" % bootstrapVersion,
    "uk.gov.hmrc" %% "dc-message-library"        % "1.28.0",
    "uk.gov.hmrc" %% "bootstrap-test-play-30"    % bootstrapVersion % "test",
    "uk.gov.hmrc" %% "domain-test-play-30"       % "13.0.0"         % "test"
  )
}
