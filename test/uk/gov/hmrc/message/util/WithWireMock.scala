/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.message.util

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration._
import org.scalatest.{ BeforeAndAfterAll, BeforeAndAfterEach, Suite }

trait WithWireMock extends BeforeAndAfterAll with BeforeAndAfterEach { suite: Suite =>

  def dependenciesPort = 5808
  lazy val wireMockServer = new WireMockServer(wireMockConfig().port(dependenciesPort))

  override def beforeAll() = {
    super.beforeAll()
    wireMockServer.start()
    WireMock.configureFor(dependenciesPort)
  }

  override def beforeEach() = {
    super.beforeEach()
    wireMockServer.resetMappings()
    wireMockServer.resetRequests()
  }

  override def afterAll() = {
    super.afterAll()
    wireMockServer.stop()
  }

}
