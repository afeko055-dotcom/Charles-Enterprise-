package com.example.data.model

import java.text.NumberFormat
import java.util.Locale

enum class UserRole(val label: String, val badgeColorHex: Long) {
  CUSTOMER("Customer", 0xFF38BDF8),
  RIDER("Dispatch Rider", 0xFFF59E0B),
  SUPPORT("Customer Support", 0xFF10B981),
  INVENTORY_MANAGER("Inventory Manager", 0xFFA855F7),
  ORDER_MANAGER("Order Manager", 0xFF6366F1),
  FINANCE_MANAGER("Finance Manager", 0xFF06B6D4),
  DISPATCH_MANAGER("Dispatch Manager", 0xFFEC4899),
  SUPER_ADMIN("Super Admin", 0xFFEF4444)
}

enum class VehicleStatus(val label: String) {
  AVAILABLE("Available"),
  RESERVED("Reserved"),
  PAYMENT_PENDING("Payment Pending"),
  SOLD("Sold"),
  IN_DELIVERY("In Delivery"),
  DELIVERED("Delivered")
}

enum class VehicleCondition(val label: String) {
  BRAND_NEW("Brand New"),
  FOREIGN_USED("Foreign Used (Tokunbo)"),
  CERTIFIED_PRE_OWNED("Certified Pre-Owned")
}

enum class TransmissionType(val label: String) {
  AUTOMATIC("Automatic"),
  MANUAL("Manual"),
  DUAL_CLUTCH("Dual-Clutch / PDK")
}

enum class FuelType(val label: String) {
  PETROL("Petrol (V8/V6/I4)"),
  DIESEL("Turbo Diesel"),
  HYBRID("Plug-in Hybrid"),
  ELECTRIC("100% Electric (EV)")
}

enum class BodyType(val label: String) {
  SEDAN("Executive Sedan"),
  SUV("Luxury SUV"),
  COUPE("Grand Tourer / Coupe"),
  TRUCK("Super Duty Truck"),
  CONVERTIBLE("Cabriolet / Spyder")
}

enum class OrderStatus(val label: String) {
  PENDING("Pending Checkout"),
  PAYMENT_PENDING("Payment Gateway Pending"),
  PAID("Payment Verified"),
  CONFIRMED("Order Confirmed"),
  PROCESSING("PDI & Prep"),
  DISPATCHED("Dispatched on Carrier"),
  DELIVERED("Handed Over (Delivered)"),
  CANCELLED("Cancelled"),
  REFUNDED("Refunded")
}

enum class PaymentStatus(val label: String) {
  INITIATED("Initiated"),
  PENDING("Pending Clearing"),
  PROCESSING("Processing"),
  SUCCESS("Verified Success"),
  FAILED("Payment Failed"),
  CANCELLED("Cancelled by User"),
  REFUNDED("Refund Completed")
}

enum class PaymentGateway(val displayName: String, val defaultFeePercent: Double) {
  PAYSTACK("Paystack Hosted Checkout", 1.5),
  FLUTTERWAVE("Flutterwave Hosted Checkout", 1.4),
  MONNIFY("Monnify Reserve Checkout", 1.3)
}

enum class LedgerType(val label: String) {
  SALE("Vehicle Purchase Settlement"),
  PAYMENT_FEE("Gateway Processing Fee"),
  REFUND("Customer Order Refund"),
  WITHDRAWAL("Bank Account Payout"),
  ADJUSTMENT("Audit Ledger Rebalance")
}

enum class LedgerDirection {
  CREDIT,
  DEBIT
}

enum class WithdrawalStatus(val label: String) {
  PENDING("Verification Pending"),
  PROCESSING("Gateway Transferring"),
  COMPLETED("Settled to Bank"),
  FAILED("Transfer Rejected"),
  CANCELLED("Cancelled by Compliance")
}

enum class DeliveryStatus(val label: String) {
  ASSIGNED("Rider Assigned"),
  ACCEPTED("Accepted by Rider"),
  PICKUP_PENDING("En Route to Showroom"),
  PICKED_UP("Vehicle Loaded onto Flatbed"),
  IN_TRANSIT("In Transit on Highway"),
  NEAR_DESTINATION("Arrived in Customer Vicinity"),
  DELIVERED("Handover Verified (OTP Entered)"),
  FAILED("Delivery Failed"),
  CANCELLED("Delivery Terminated")
}

object CurrencyFormatter {
  fun formatKoboToNaira(kobo: Long): String {
    val naira = kobo / 100.0
    val format = NumberFormat.getCurrencyInstance(Locale("en", "NG"))
    format.maximumFractionDigits = 0
    // If currency symbol is NGN or ?, format cleanly as ?
    val formatted = NumberFormat.getNumberInstance(Locale.US).format(naira.toLong())
    return "₦$formatted"
  }

  fun formatKoboToUSD(kobo: Long, exchangeRateToNgn: Double = 1600.0): String {
    val usd = (kobo / 100.0) / exchangeRateToNgn
    val formatted = NumberFormat.getNumberInstance(Locale.US).format(usd.toLong())
    return "$$formatted"
  }
}
