package com.seacliff.pos.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seacliff.pos.data.local.entity.PaymentEntity
import com.seacliff.pos.data.local.entity.TipEntity
import com.seacliff.pos.data.remote.dto.BillResponse
import com.seacliff.pos.data.remote.dto.TipSuggestionsResponse
import com.seacliff.pos.data.repository.AuthRepository
import com.seacliff.pos.data.repository.PaymentRepository
import com.seacliff.pos.data.repository.TipRepository
import com.seacliff.pos.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository,
    private val tipRepository: TipRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _bill = MutableLiveData<Resource<BillResponse>>()
    val bill: LiveData<Resource<BillResponse>> = _bill

    private val _paymentState = MutableLiveData<Resource<PaymentEntity>>()
    val paymentState: LiveData<Resource<PaymentEntity>> = _paymentState

    private val _paymentResult = MutableLiveData<Resource<PaymentEntity>>()
    val paymentResult: LiveData<Resource<PaymentEntity>> = _paymentResult

    private val _tipResult = MutableLiveData<Resource<Long>>()
    val tipResult: LiveData<Resource<Long>> = _tipResult

    private val _tipSuggestions = MutableLiveData<Resource<TipSuggestionsResponse>>()
    val tipSuggestions: LiveData<Resource<TipSuggestionsResponse>> = _tipSuggestions

    private val _selectedPaymentMethod = MutableLiveData<String>("cash")
    val selectedPaymentMethod: LiveData<String> = _selectedPaymentMethod

    fun loadBill(orderId: Long) {
        viewModelScope.launch {
            paymentRepository.getOrderBill(orderId).collect { resource ->
                _bill.value = resource
            }
        }
    }

    fun loadTipSuggestions(orderId: Long) {
        viewModelScope.launch {
            val result = tipRepository.getTipSuggestions(orderId)
            _tipSuggestions.value = result
        }
    }

    fun selectPaymentMethod(method: String) {
        _selectedPaymentMethod.value = method
    }

    /**
     * Process a cash payment
     */
    fun processCashPayment(orderId: Long, amount: Double, tendered: Double) {
        viewModelScope.launch {
            paymentRepository.createCashPayment(orderId, amount, tendered).collect { resource ->
                _paymentState.value = resource
            }
        }
    }

    /**
     * Process a card payment
     */
    fun processCardPayment(orderId: Long, amount: Double, cardLastFour: String, cardType: String) {
        viewModelScope.launch {
            paymentRepository.createCardPayment(orderId, amount, cardLastFour, cardType).collect { resource ->
                _paymentState.value = resource
            }
        }
    }

    /**
     * Process a mobile money payment
     */
    fun processMobilePayment(orderId: Long, amount: Double, phoneNumber: String, provider: String) {
        viewModelScope.launch {
            paymentRepository.createMobilePayment(orderId, amount, phoneNumber, provider).collect { resource ->
                _paymentState.value = resource
            }
        }
    }

    /**
     * Generic payment processing (backward compatible)
     */
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
                _paymentState.value?.data?.let { payment ->
                    _paymentState.value = Resource.Success(payment.copy(status = "completed"))
                }
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

    fun processPayment(payment: PaymentEntity) {
        _paymentResult.value = Resource.Loading()
        viewModelScope.launch {
            try {
                val paymentId = paymentRepository.insertPayment(payment)
                _paymentResult.value = Resource.Success(payment.copy(id = paymentId))
            } catch (e: Exception) {
                _paymentResult.value = Resource.Error(e.message ?: "Failed to process payment")
            }
        }
    }

    /**
     * Save tip via API
     */
    fun createTip(orderId: Long, amount: Double, tipMethod: String, paymentId: Long? = null) {
        viewModelScope.launch {
            val result = tipRepository.createTip(orderId, amount, tipMethod, paymentId)
            when (result) {
                is Resource.Success -> _tipResult.value = Resource.Success(result.data?.id ?: 0)
                is Resource.Error -> _tipResult.value = Resource.Error(result.message ?: "Failed to save tip")
                is Resource.Loading -> _tipResult.value = Resource.Loading()
            }
        }
    }

    /**
     * Save tip locally (backward compatible)
     */
    fun saveTip(tip: TipEntity) {
        viewModelScope.launch {
            val result = tipRepository.insertTip(tip)
            _tipResult.value = result
        }
    }

    fun getCurrentWaiterId(): Long {
        return authRepository.getStaffId()
    }
}
