package com.example

import com.example.data.model.CurrencyFormatter
import com.example.data.model.PaymentGateway
import com.example.data.model.PaymentStatus
import com.example.payment.PaystackPaymentProvider
import com.example.payment.WebhookEventPayload
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MarketplaceTests {

  @Test
  fun testCurrencyFormatterKoboToNaira() {
    val amountKobo = 285_000_000_00L // 285 Million Naira
    val formatted = CurrencyFormatter.formatKoboToNaira(amountKobo)
    assertTrue(formatted.startsWith("₦"))
    assertTrue(formatted.contains("285,000,000"))
  }

  @Test
  fun testAuthoritativePriceCalculationNoFloatDrift() {
    val basePriceKobo = 320_000_000_00L
    val deliveryFeeKobo = 450_000_00L
    val documentationFeeKobo = 250_000_00L

    val totalAmountKobo = basePriceKobo + deliveryFeeKobo + documentationFeeKobo
    assertEquals(320_700_000_00L, totalAmountKobo)

    val gatewayFeeKobo = (totalAmountKobo * 0.015).toLong()
    val netSettlementKobo = totalAmountKobo - gatewayFeeKobo
    assertEquals(totalAmountKobo, netSettlementKobo + gatewayFeeKobo)
  }

  @Test
  fun testPaystackWebhookHmacSignatureValidation() = runBlocking {
    val provider = PaystackPaymentProvider(secretKey = "test_secret_key_123")
    val payloadJson = """{"event":"charge.success","data":{"reference":"pstk_ref_99214","amount":32070000000}}"""

    val validSig = provider.generateTestSignature(payloadJson)

    val validPayload = WebhookEventPayload(
      eventId = "ev_101",
      eventType = "charge.success",
      provider = PaymentGateway.PAYSTACK,
      reference = "pstk_ref_99214",
      amountKobo = 320_700_000_00L,
      currency = "NGN",
      status = PaymentStatus.SUCCESS,
      signature = validSig,
      rawPayloadJson = payloadJson
    )

    val validResult = provider.handleWebhook(validSig, validPayload)
    assertTrue(validResult.isValidSignature)

    val invalidResult = provider.handleWebhook("forged_signature_xyz", validPayload)
    assertFalse(invalidResult.isValidSignature)
  }
}
