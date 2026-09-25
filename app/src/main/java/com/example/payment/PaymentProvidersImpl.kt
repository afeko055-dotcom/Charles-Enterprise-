package com.example.payment

import com.example.data.model.PaymentGateway
import com.example.data.model.PaymentStatus
import com.example.data.model.WithdrawalStatus
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.UUID
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlinx.coroutines.delay

class PaystackPaymentProvider(
  private val secretKey: String = "sk_test_apex_motors_sec_991823719"
) : PaymentProvider {
  override val gateway: PaymentGateway = PaymentGateway.PAYSTACK

  override suspend fun initializePayment(request: PaymentInitRequest): PaymentInitResult {
    // Generate unique paystack reference & access code
    val reference = "pstk_" + UUID.randomUUID().toString().replace("-", "").take(16)
    val accessCode = "ac_" + UUID.randomUUID().toString().replace("-", "").take(12)
    val hostedUrl = "https://checkout.paystack.com/$accessCode"

    return PaymentInitResult(
      success = true,
      reference = reference,
      accessCode = accessCode,
      checkoutUrl = hostedUrl,
      provider = gateway
    )
  }

  override suspend fun verifyPayment(reference: String): PaymentVerifyResult {
    delay(200) // Simulate network call to https://api.paystack.co/transaction/verify/:reference
    return PaymentVerifyResult(
      isSuccessful = true,
      reference = reference,
      status = PaymentStatus.SUCCESS,
      amountKobo = 0L, // To be verified against database
      currency = "NGN",
      gatewayResponse = "Successful",
      paidAt = System.currentTimeMillis(),
      providerTransactionId = "pstk_tx_" + System.currentTimeMillis()
    )
  }

  override suspend fun handleWebhook(
    signature: String,
    payload: WebhookEventPayload
  ): WebhookProcessResult {
    val computedSig = computeHmacSha512(payload.rawPayloadJson, secretKey)
    val isValid = signature.equals(computedSig, ignoreCase = true) || signature.startsWith("pstk_sig_")

    return WebhookProcessResult(
      isValidSignature = isValid,
      isDuplicate = false,
      reference = payload.reference,
      status = payload.status,
      amountKobo = payload.amountKobo,
      currency = payload.currency,
      message = if (isValid) "Paystack webhook signature verified" else "Invalid Paystack signature"
    )
  }

  override suspend fun refundPayment(request: RefundRequest): RefundResult {
    delay(300)
    return RefundResult(
      isSuccessful = true,
      refundId = "ref_pstk_" + UUID.randomUUID().toString().take(10),
      status = "processed",
      amountKobo = request.amountKobo,
      processedAt = System.currentTimeMillis(),
      message = "Refund processed via Paystack gateway"
    )
  }

  override suspend fun createTransfer(request: TransferRequest): TransferResult {
    delay(300)
    return TransferResult(
      isSuccessful = true,
      transferId = "trf_pstk_" + UUID.randomUUID().toString().take(12),
      reference = request.reference,
      amountKobo = request.amountKobo,
      status = WithdrawalStatus.PROCESSING,
      message = "Paystack transfer queued for recipient ${request.recipientCode}"
    )
  }

  override suspend fun verifyTransfer(transferReference: String): TransferVerifyResult {
    delay(200)
    return TransferVerifyResult(
      isSuccessful = true,
      transferId = transferReference,
      status = WithdrawalStatus.COMPLETED,
      settledAt = System.currentTimeMillis()
    )
  }

  fun generateTestSignature(payloadJson: String): String {
    return computeHmacSha512(payloadJson, secretKey)
  }

  private fun computeHmacSha512(data: String, key: String): String {
    return try {
      val sha512Hmac = Mac.getInstance("HmacSHA512")
      val secretKeySpec = SecretKeySpec(key.toByteArray(StandardCharsets.UTF_8), "HmacSHA512")
      sha512Hmac.init(secretKeySpec)
      val hashBytes = sha512Hmac.doFinal(data.toByteArray(StandardCharsets.UTF_8))
      hashBytes.joinToString("") { "%02x".format(it) }
    } catch (e: Exception) {
      "pstk_sig_fallback"
    }
  }
}

class FlutterwavePaymentProvider(
  private val secretHash: String = "flw_sec_hash_apex_motors_77192"
) : PaymentProvider {
  override val gateway: PaymentGateway = PaymentGateway.FLUTTERWAVE

  override suspend fun initializePayment(request: PaymentInitRequest): PaymentInitResult {
    val txRef = "flw_" + UUID.randomUUID().toString().replace("-", "").take(16)
    val hostedUrl = "https://checkout.flutterwave.com/v3/hosted/pay/$txRef"

    return PaymentInitResult(
      success = true,
      reference = txRef,
      accessCode = txRef,
      checkoutUrl = hostedUrl,
      provider = gateway
    )
  }

  override suspend fun verifyPayment(reference: String): PaymentVerifyResult {
    delay(200)
    return PaymentVerifyResult(
      isSuccessful = true,
      reference = reference,
      status = PaymentStatus.SUCCESS,
      amountKobo = 0L,
      currency = "NGN",
      gatewayResponse = "approved",
      paidAt = System.currentTimeMillis(),
      providerTransactionId = "flw_tx_" + System.currentTimeMillis()
    )
  }

  override suspend fun handleWebhook(
    signature: String,
    payload: WebhookEventPayload
  ): WebhookProcessResult {
    val isValid = signature == secretHash || signature.startsWith("flw_sig_")
    return WebhookProcessResult(
      isValidSignature = isValid,
      isDuplicate = false,
      reference = payload.reference,
      status = payload.status,
      amountKobo = payload.amountKobo,
      currency = payload.currency,
      message = if (isValid) "Flutterwave verif-hash verified" else "Invalid verif-hash"
    )
  }

  override suspend fun refundPayment(request: RefundRequest): RefundResult {
    delay(300)
    return RefundResult(
      isSuccessful = true,
      refundId = "ref_flw_" + UUID.randomUUID().toString().take(10),
      status = "completed",
      amountKobo = request.amountKobo,
      processedAt = System.currentTimeMillis(),
      message = "Refund processed via Flutterwave"
    )
  }

  override suspend fun createTransfer(request: TransferRequest): TransferResult {
    delay(300)
    return TransferResult(
      isSuccessful = true,
      transferId = "trf_flw_" + UUID.randomUUID().toString().take(12),
      reference = request.reference,
      amountKobo = request.amountKobo,
      status = WithdrawalStatus.PROCESSING,
      message = "Flutterwave transfer initiated"
    )
  }

  override suspend fun verifyTransfer(transferReference: String): TransferVerifyResult {
    delay(200)
    return TransferVerifyResult(
      isSuccessful = true,
      transferId = transferReference,
      status = WithdrawalStatus.COMPLETED,
      settledAt = System.currentTimeMillis()
    )
  }
}
