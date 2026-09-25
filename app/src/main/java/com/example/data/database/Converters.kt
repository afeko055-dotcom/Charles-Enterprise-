package com.example.data.database

import androidx.room.TypeConverter
import com.example.data.model.BodyType
import com.example.data.model.DeliveryStatus
import com.example.data.model.FuelType
import com.example.data.model.LedgerDirection
import com.example.data.model.LedgerType
import com.example.data.model.OrderStatus
import com.example.data.model.PaymentGateway
import com.example.data.model.PaymentStatus
import com.example.data.model.TransmissionType
import com.example.data.model.UserRole
import com.example.data.model.VehicleCondition
import com.example.data.model.VehicleStatus
import com.example.data.model.WithdrawalStatus

class Converters {
  @TypeConverter
  fun fromUserRole(value: UserRole?): String? = value?.name
  @TypeConverter
  fun toUserRole(value: String?): UserRole? = value?.let { enumValueOf<UserRole>(it) }

  @TypeConverter
  fun fromVehicleStatus(value: VehicleStatus?): String? = value?.name
  @TypeConverter
  fun toVehicleStatus(value: String?): VehicleStatus? = value?.let { enumValueOf<VehicleStatus>(it) }

  @TypeConverter
  fun fromVehicleCondition(value: VehicleCondition?): String? = value?.name
  @TypeConverter
  fun toVehicleCondition(value: String?): VehicleCondition? = value?.let { enumValueOf<VehicleCondition>(it) }

  @TypeConverter
  fun fromTransmissionType(value: TransmissionType?): String? = value?.name
  @TypeConverter
  fun toTransmissionType(value: String?): TransmissionType? = value?.let { enumValueOf<TransmissionType>(it) }

  @TypeConverter
  fun fromFuelType(value: FuelType?): String? = value?.name
  @TypeConverter
  fun toFuelType(value: String?): FuelType? = value?.let { enumValueOf<FuelType>(it) }

  @TypeConverter
  fun fromBodyType(value: BodyType?): String? = value?.name
  @TypeConverter
  fun toBodyType(value: String?): BodyType? = value?.let { enumValueOf<BodyType>(it) }

  @TypeConverter
  fun fromOrderStatus(value: OrderStatus?): String? = value?.name
  @TypeConverter
  fun toOrderStatus(value: String?): OrderStatus? = value?.let { enumValueOf<OrderStatus>(it) }

  @TypeConverter
  fun fromPaymentStatus(value: PaymentStatus?): String? = value?.name
  @TypeConverter
  fun toPaymentStatus(value: String?): PaymentStatus? = value?.let { enumValueOf<PaymentStatus>(it) }

  @TypeConverter
  fun fromPaymentGateway(value: PaymentGateway?): String? = value?.name
  @TypeConverter
  fun toPaymentGateway(value: String?): PaymentGateway? = value?.let { enumValueOf<PaymentGateway>(it) }

  @TypeConverter
  fun fromLedgerType(value: LedgerType?): String? = value?.name
  @TypeConverter
  fun toLedgerType(value: String?): LedgerType? = value?.let { enumValueOf<LedgerType>(it) }

  @TypeConverter
  fun fromLedgerDirection(value: LedgerDirection?): String? = value?.name
  @TypeConverter
  fun toLedgerDirection(value: String?): LedgerDirection? = value?.let { enumValueOf<LedgerDirection>(it) }

  @TypeConverter
  fun fromWithdrawalStatus(value: WithdrawalStatus?): String? = value?.name
  @TypeConverter
  fun toWithdrawalStatus(value: String?): WithdrawalStatus? = value?.let { enumValueOf<WithdrawalStatus>(it) }

  @TypeConverter
  fun fromDeliveryStatus(value: DeliveryStatus?): String? = value?.name
  @TypeConverter
  fun toDeliveryStatus(value: String?): DeliveryStatus? = value?.let { enumValueOf<DeliveryStatus>(it) }
}
