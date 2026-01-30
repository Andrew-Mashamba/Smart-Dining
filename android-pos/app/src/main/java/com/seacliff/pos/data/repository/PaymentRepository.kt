package com.seacliff.pos.data.repository

import com.seacliff.pos.data.local.dao.PaymentDao
import com.seacliff.pos.data.local.entity.PaymentEntity
import com.seacliff.pos.data.remote.api.ApiService
import com.seacliff.pos.data.remote.dto.CreatePaymentRequest
import com.seacliff.pos.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentRepository @Inject constructor(
    private val apiService: ApiService,
    private val paymentDao: PaymentDao
) {

    fun createPayment(
        orderId: Long,
        amount: Double,
        method: String
    ): Flow<Resource<PaymentEntity>> = flow {
        try {
            emit(Resource.Loading())

            val request = CreatePaymentRequest(
                orderId = orderId,
                amount = amount,
                method = method
            )

            val response = apiService.createPayment(request)

            if (response.isSuccessful && response.body()?.data != null) {
                val payment = response.body()!!.data!!

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

    fun getOrderBill(orderId: Long): Flow<Resource<Map<String, Any>>> = flow {
        try {
            emit(Resource.Loading())

            val response = apiService.getOrderBill(orderId)

            if (response.isSuccessful && response.body()?.data != null) {
                emit(Resource.Success(response.body()!!.data!!))
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
}
