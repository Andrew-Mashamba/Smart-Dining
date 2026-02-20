package com.seacliff.pos.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seacliff.pos.data.local.entity.TipEntity
import com.seacliff.pos.data.repository.AuthRepository
import com.seacliff.pos.data.repository.TipRepository
import com.seacliff.pos.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TipSummary(
    val total: Double,
    val count: Int,
    val average: Double,
    val highest: Double
)

@HiltViewModel
class TipViewModel @Inject constructor(
    private val tipRepository: TipRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _tipsForPeriod = MutableLiveData<Resource<List<TipEntity>>>()
    val tipsForPeriod: LiveData<Resource<List<TipEntity>>> = _tipsForPeriod

    private val _tipSummary = MutableLiveData<TipSummary>()
    val tipSummary: LiveData<TipSummary> = _tipSummary

    private val _totalTips = MutableLiveData<Double>()
    val totalTips: LiveData<Double> = _totalTips

    fun loadTipsForPeriod(startDate: Long, endDate: Long) {
        _tipsForPeriod.value = Resource.Loading()
        viewModelScope.launch {
            val waiterId = authRepository.getStaffId()
            val result = tipRepository.getTipsByDateRange(waiterId, startDate, endDate)
            _tipsForPeriod.value = result
        }
    }

    fun loadTipSummary(startDate: Long, endDate: Long) {
        viewModelScope.launch {
            val waiterId = authRepository.getStaffId()
            val tipsResult = tipRepository.getTipsByDateRange(waiterId, startDate, endDate)

            if (tipsResult is Resource.Success) {
                val tips = tipsResult.data ?: emptyList()

                if (tips.isNotEmpty()) {
                    val total = tips.sumOf { it.amount }
                    val count = tips.size
                    val average = total / count
                    val highest = tips.maxOfOrNull { it.amount } ?: 0.0

                    _tipSummary.value = TipSummary(total, count, average, highest)
                } else {
                    _tipSummary.value = TipSummary(0.0, 0, 0.0, 0.0)
                }
            }
        }
    }

    fun loadTotalTips() {
        viewModelScope.launch {
            val waiterId = authRepository.getStaffId()
            val result = tipRepository.getTotalTipsByWaiter(waiterId)

            if (result is Resource.Success) {
                _totalTips.value = result.data ?: 0.0
            }
        }
    }
}
