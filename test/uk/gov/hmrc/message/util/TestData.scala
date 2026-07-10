/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.message.util

import uk.gov.hmrc.common.message.model.Regime.{ paye, sa }
import uk.gov.hmrc.common.message.model.{ AlertDetails, ExternalRef, Message, Regime, RenderUrl, TaxEntity, TaxpayerName }
import uk.gov.hmrc.domain.{ Nino, SaUtr }

import java.time.LocalDate

object TestData {
  val TEST_SUBJECT = "test_subject"
  val TEST_TEMPLATE_ID = "test_id"
  val TEST_SERVICE = "test_service"
  val TEST_URL = "http://localhost:8880/test"
  val TEST_HASH = "#asfh1234tygh567"
  val TEST_ID = "test_id"
  val TEST_SOURCE = "test_source"

  val TEST_DAY = 22
  val TEST_MONTH = 2
  val TEST_YEAR = 2026

  val TEST_LOCAL_DATE: LocalDate = LocalDate.of(TEST_YEAR, TEST_MONTH, TEST_DAY)

  val TEST_RECIPIENT: TaxEntity = TaxEntity(regime = paye, identifier = Nino("NM439088A"))
  val TEST_RECIPIENT_SA_UTR: TaxEntity = TaxEntity(regime = sa, identifier = SaUtr("12345678"))

  val TEST_ALERT_DETAILS: AlertDetails =
    AlertDetails(templateId = TEST_TEMPLATE_ID, recipientName = None, data = Map())

  val TEST_EXTERNAL_REF: ExternalRef = ExternalRef(TEST_ID, TEST_SOURCE)

  val TEST_RENDER_URL: RenderUrl = RenderUrl(TEST_SERVICE, TEST_URL)

  lazy val TEST_MESSAGE: Message = Message(
    recipient = TEST_RECIPIENT_SA_UTR,
    subject = TEST_SUBJECT,
    body = None,
    validFrom = TEST_LOCAL_DATE,
    alertFrom = Some(TEST_LOCAL_DATE),
    alertDetails = TEST_ALERT_DETAILS,
    lastUpdated = None,
    hash = TEST_HASH,
    statutory = true,
    renderUrl = TEST_RENDER_URL,
    sourceData = None,
    externalRef = Some(TEST_EXTERNAL_REF),
    tags = Some(Map("notificationType" -> "Direct Debit"))
  )
}
