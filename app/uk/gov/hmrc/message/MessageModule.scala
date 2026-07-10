/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.message

import com.google.inject.name.Named
import com.google.inject.{ AbstractModule, Provides }

import java.time.Instant
import play.api.{ Configuration, Logging }
import uk.gov.hmrc.common.message.model.{ SystemTimeSource, TimeSource }

import javax.inject.Singleton

class MessageModule extends AbstractModule with Logging {

  @Singleton
  @Provides
  def systemTimeSourceProvider(): TimeSource = new TimeSource() {
    def now(): Instant = SystemTimeSource.now()
  }

  @Provides
  @Named("app-name")
  @Singleton
  def appNameProvider(configuration: Configuration): String =
    configuration
      .getOptional[String]("appName")
      .getOrElse(throw new RuntimeException("App name not found in config"))

  @Provides
  @Named("audit-event-max-size")
  @Singleton
  def auditEventMaxSize(configuration: Configuration): Int =
    configuration
      .getOptional[Int]("auditEventMaxSize")
      .getOrElse(
        throw new RuntimeException("auditEventMaxSize not found in config")
      )

}
