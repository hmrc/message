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
