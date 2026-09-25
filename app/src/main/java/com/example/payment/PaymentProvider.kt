package com.example.payment

import com.example.data.model.PaymentGateway
import com.example.data.model.PaymentStatus
import com.example.data.model.WithdrawalStatus

data class PaymentInitRequest(
  val orderId: String,
  val amountKobo: Long,
  val currency: String = "NGN",
  val customerEmail: String,
  val customerName: String,
  val metadata: Map<String, String> = emptyMap()
)

data class PaymentInitResult(
  val success: Boolean,
  val reference: String,
  val accessCode: String,
  val checkoutUrl: String,
  val provider: PaymentGateway,
  val errorMessage: String? = null
)

data class PaymentVerifyResult(
  val isSuccessful: Boolean,
  val reference: String,
  val status: PaymentStatus,
  val amountKobo: Long,
  val currency: String,
  val gatewayResponse: String,
  val paidAt: Long?,
  val providerTransactionId: String?,
  val errorMessage: String? = null
)

data class WebhookEventPayload(
  val eventId: String,
  val eventType: String,
  val provider: PaymentGateway,
  val reference: String,
  val amountKobo: Long,
  val currency: String,
  val status: PaymentStatus,
  val signature: String,
  val rawPayloadJson: String
)

data class WebhookProcessResult(
  val isValidSignature: Boolean,
  val isDuplicate: Boolean,
  val reference: String,
  val status: PaymentStatus,
  val amountKobo: Long,
  val currency: String,
  val message: String
)

data class RefundRequest(
  val paymentReference: String,
  val amountKobo: Long,
  val reason: String
)

data class RefundResult(
  val isSuccessful: Boolean,
  val refundId: String,
  val status: String,
  val amountKobo: Long,
  val processedAt: Long,
  val message: String
)

data class TransferRequest(
  val reference: String,
  val recipientCode: String,
  val amountKobo: Long,
  val narration: String
)

data class TransferResult(
  val isSuccessful: Boolean,
  val transferId: String,
  val reference: String,
  val amountKobo: Long,
  val status: WithdrawalStatus,
  val message: String
)

data class TransferVerifyResult(
  val isSuccessful: Boolean,
  val transferId: String,
  val status: WithdrawalStatus,
  val settledAt: Long?
)

interface PaymentProvider {
  val gateway: PaymentGateway

  suspend fun initializePayment(request: PaymentInitRequest): PaymentInitResult

  suspend fun verifyPayment(reference: String): PaymentVerifyResult

  suspend fun handleWebhook(signature: String, payload: WebhookEventPayload): WebhookProcessResult

  suspend fun refundPayment(request: RefundRequest): RefundResult

  suspend fun createTransfer(request: TransferRequest): TransferResult

  suspend fun verifyTransfer(transferReference: String): TransferVerifyResult
}
