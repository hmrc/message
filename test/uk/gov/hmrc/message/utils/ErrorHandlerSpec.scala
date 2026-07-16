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

package uk.gov.hmrc.message.utils

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.http.Status.NOT_FOUND
import play.api.mvc.{ RequestHeader, Result }
import play.api.test.Helpers.*
import play.api.test.{ FakeHeaders, FakeRequest }
import play.api.{ Application, inject }
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.message.util.SpecBase
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.http.connector.AuditResult.Success
import uk.gov.hmrc.play.audit.model.DataEvent
import uk.gov.hmrc.play.bootstrap.config.HttpAuditEvent

import scala.concurrent.{ ExecutionContext, Future }

class ErrorHandlerSpec extends SpecBase {

  "onClientError" should {
    "return result with BAD_REQUEST when input status is BAD_REQUEST" in new Setup {
      val result: Result = await(errorHandler.onClientError(fakeRequest, BAD_REQUEST, "error occurred"))

      result.header.status must be(BAD_REQUEST)
    }

    "return result with relevant status code when input status is other than BAD_REQUEST" in new Setup {
      val result: Result = await(errorHandler.onClientError(fakeRequest, NOT_FOUND, "error occurred"))

      result.header.status must be(NOT_FOUND)
    }
  }

  "onServerError" should {
    val exception = new RuntimeException("network error")

    "return result with INTERNAL_SERVER_ERROR" in new Setup {
      val result: Result = await(errorHandler.onServerError(fakeRequest, exception))

      result.header.status must be(INTERNAL_SERVER_ERROR)
    }
  }

  trait Setup {
    implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
    implicit val fakeRequest: RequestHeader = FakeRequest("GET", "/")

    val errorHandler: ErrorHandler = app.injector.instanceOf[ErrorHandler]
  }
}
