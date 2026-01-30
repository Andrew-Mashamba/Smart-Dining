package com.seacliff.pos.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seacliff.pos.data.local.entity.PaymentEntity
import com.seacliff.pos.data.repository.PaymentRepository
import com.seacliff.pos.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository
) : ViewModel() {

    private val _bill = MutableLiveData<Resource<Map<String, Any>>>()
    val bill: LiveData<Resource<Map<String, Any>>> = _bill

    private val _paymentState = MutableLiveData<Resource<PaymentEntity>>()
    val paymentState: LiveData<Resource<PaymentEntity>> = _paymentState

    private val _selectedPaymentMethod = MutableLiveData<String>("cash")
    val selectedPaymentMethod: LiveData<String> = _selectedPaymentMethod

    fun loadBill(orderId: Long) {
        viewModelScope.launch {
            paymentRepository.getOrderBill(orderId).collect { resource ->
                _bill.value = resource
            }
        }
    }

    fun selectPaymentMethod(method: String) {
        _selectedPaymentMethod.value = method
    }

    fun processPayment(orderId: Long, amount: Double, method: String) {
        viewModelScope.launch {
            paymentRepository.createPayment(orderId, amount, method).collect { resource ->
                _paymentState.value = resource
            }
        }
    }

    fun confirmPayment(paymentId: Long) {
        viewModelScope.launch {
            val result = paymentRepository.confirmPayment(paymentId)
            if (result is Resource.Success) {
                _paymentState.value = Resource.Success(
                    _paymentState.value?.data?.copy(status = "completed")!!
                )
            }
        }
    }

    fun getTodayPayments() {
        viewModelScope.launch {
            paymentRepository.getTodayPayments().collect { payments ->
                // Handle payments if needed
            }
        }
    }
}
