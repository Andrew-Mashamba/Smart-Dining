package com.seacliff.pos.data.repository

import com.seacliff.pos.data.local.dao.PaymentDao
import com.seacliff.pos.data.local.entity.PaymentEntity
import com.seacliff.pos.data.remote.api.ApiService
import com.seacliff.pos.data.remote.dto.BillResponse
import com.seacliff.pos.data.remote.dto.CreatePaymentRequest
import com.seacliff.pos.data.remote.dto.PaymentDto
import com.seacliff.pos.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentRepository @Inject constructor(
    private val apiService: ApiService,
    private val paymentDao: PaymentDao
) {

    /**
     * Convert PaymentDto from API to PaymentEntity for local storage
     */
    private fun PaymentDto.toEntity(): PaymentEntity {
        return PaymentEntity(
            id = this.id,
            orderId = this.orderId,
            amount = this.amount,
            method = this.paymentMethod,
            status = this.status,
            transactionId = this.transactionId,
            createdAt = Date()
        )
    }

    /**
     * Create a cash payment
     */
    fun createCashPayment(
        orderId: Long,
        amount: Double,
        tendered: Double
    ): Flow<Resource<PaymentEntity>> = flow {
        try {
            emit(Resource.Loading())

            val request = CreatePaymentRequest(
                orderId = orderId,
                amount = amount,
                paymentMethod = "cash",
                tendered = tendered
            )

            val response = apiService.createPayment(request)

            if (response.isSuccessful && response.body() != null) {
                val paymentResponse = response.body()!!
                val payment = paymentResponse.payment.toEntity()

                // Save to local database
                paymentDao.insertPayment(payment)

                emit(Resource.Success(payment))
            } else {
                emit(Resource.Error("Failed to process payment"))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Payment error: ${e.localizedMessage}"))
        }
    }

    /**
     * Create a card payment
     */
    fun createCardPayment(
        orderId: Long,
        amount: Double,
        cardLastFour: String,
        cardType: String
    ): Flow<Resource<PaymentEntity>> = flow {
        try {
            emit(Resource.Loading())

            val request = CreatePaymentRequest(
                orderId = orderId,
                amount = amount,
                paymentMethod = "card",
                cardLastFour = cardLastFour,
                cardType = cardType
            )

            val response = apiService.createPayment(request)

            if (response.isSuccessful && response.body() != null) {
                val paymentResponse = response.body()!!
                val payment = paymentResponse.payment.toEntity()

                // Save to local database
                paymentDao.insertPayment(payment)

                emit(Resource.Success(payment))
            } else {
                emit(Resource.Error("Failed to process payment"))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Payment error: ${e.localizedMessage}"))
        }
    }

    /**
     * Create a mobile money payment
     */
    fun createMobilePayment(
        orderId: Long,
        amount: Double,
        phoneNumber: String,
        provider: String
    ): Flow<Resource<PaymentEntity>> = flow {
        try {
            emit(Resource.Loading())

            val request = CreatePaymentRequest(
                orderId = orderId,
                amount = amount,
                paymentMethod = "mobile_money",
                phoneNumber = phoneNumber,
                provider = provider
            )

            val response = apiService.createPayment(request)

            if (response.isSuccessful && response.body() != null) {
                val paymentResponse = response.body()!!
                val payment = paymentResponse.payment.toEntity()

                // Save to local database
                paymentDao.insertPayment(payment)

                emit(Resource.Success(payment))
            } else {
                emit(Resource.Error("Failed to process payment"))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Payment error: ${e.localizedMessage}"))
        }
    }

    /**
     * Legacy method for backward compatibility
     */
    fun createPayment(
        orderId: Long,
        amount: Double,
        method: String,
        tendered: Double? = null,
        phoneNumber: String? = null,
        provider: String? = null,
        cardLastFour: String? = null,
        cardType: String? = null
    ): Flow<Resource<PaymentEntity>> = flow {
        try {
            emit(Resource.Loading())

            val request = CreatePaymentRequest(
                orderId = orderId,
                amount = amount,
                paymentMethod = method,
                tendered = tendered,
                phoneNumber = phoneNumber,
                provider = provider,
                cardLastFour = cardLastFour,
                cardType = cardType
            )

            val response = apiService.createPayment(request)

            if (response.isSuccessful && response.body() != null) {
                val paymentResponse = response.body()!!
                val payment = paymentResponse.payment.toEntity()

                // Save to local database
                paymentDao.insertPayment(payment)

                emit(Resource.Success(payment))
            } else {
                emit(Resource.Error("Failed to process payment"))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Payment error: ${e.localizedMessage}"))
        }
    }

    fun getOrderBill(orderId: Long): Flow<Resource<BillResponse>> = flow {
        try {
            emit(Resource.Loading())

            val response = apiService.getOrderBill(orderId)

            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error("Failed to load bill"))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Error: ${e.localizedMessage}"))
        }
    }

    fun getPaymentsByOrder(orderId: Long): Flow<List<PaymentEntity>> {
        return paymentDao.getPaymentsByOrder(orderId)
    }

    fun getTodayPayments(): Flow<List<PaymentEntity>> {
        return paymentDao.getTodayPayments()
    }

    suspend fun confirmPayment(paymentId: Long): Resource<Unit> {
        return try {
            val response = apiService.confirmPayment(paymentId)

            if (response.isSuccessful) {
                paymentDao.updatePaymentStatus(paymentId, "completed")
                Resource.Success(Unit)
            } else {
                Resource.Error("Failed to confirm payment")
            }
        } catch (e: Exception) {
            Resource.Error("Error: ${e.localizedMessage}")
        }
    }

    suspend fun insertPayment(payment: PaymentEntity): Long {
        return paymentDao.insertPayment(payment)
    }
}
