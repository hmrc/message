/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

import play.routes.compiler.InjectedRoutesGenerator
import sbt.Keys.*
import sbt.*
import uk.gov.hmrc.DefaultBuildSettings
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion

val appName: String = "message"

ThisBuild / majorVersion := 8
ThisBuild / scalaVersion := "3.6.3"

val forceSourceHeader = true

lazy val appDependencies: Seq[ModuleID] = Dependencies.appDependencies

lazy val microservice = Project(appName, file("."))
  .enablePlugins(
    play.sbt.PlayScala,
    SbtDistributablesPlugin
  )
  .disablePlugins(JUnitXmlReportPlugin) // Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(
    libraryDependencies ++= appDependencies,
    Test / parallelExecution := false,
    Test / fork := false,
    retrieveManaged := true,
    routesImport ++= Seq(
      "uk.gov.hmrc.message._",
      "uk.gov.hmrc.domain._"
    ),
    routesGenerator := InjectedRoutesGenerator
  )
  .settings(
    scalacOptions := scalacOptions.value
      .diff(Seq("-Wunused:all", "-Wconf:msg=unused import:s,msg=Flag.*repeatedly:s"))
      .distinct
  )
  .settings(inConfig(TemplateTest)(Defaults.testSettings) *)
  .settings(ScoverageSettings())

lazy val TemplateTest = config("tt") extend Test

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test")
  .settings(DefaultBuildSettings.itSettings())

Test / test := (Test / test)
  .dependsOn(scalafmtCheckAll)
  .value

it / test := (it / Test / test)
  .dependsOn(scalafmtCheckAll, it / scalafmtCheckAll)
  .value
