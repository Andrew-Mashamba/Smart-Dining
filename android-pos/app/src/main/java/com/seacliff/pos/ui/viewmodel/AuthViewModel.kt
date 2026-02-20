package com.seacliff.pos.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seacliff.pos.data.remote.dto.StaffSummaryDto
import com.seacliff.pos.data.repository.AuthRepository
import com.seacliff.pos.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableLiveData<Resource<Unit>>()
    val loginState: LiveData<Resource<Unit>> = _loginState

    private val _logoutState = MutableLiveData<Resource<Unit>>()
    val logoutState: LiveData<Resource<Unit>> = _logoutState

    private val _staffList = MutableLiveData<Resource<List<StaffSummaryDto>>>()
    val staffList: LiveData<Resource<List<StaffSummaryDto>>> = _staffList

    private val _setPinState = MutableLiveData<Resource<Unit>>()
    val setPinState: LiveData<Resource<Unit>> = _setPinState

    // Currently selected staff for PIN login
    private val _selectedStaff = MutableLiveData<StaffSummaryDto?>()
    val selectedStaff: LiveData<StaffSummaryDto?> = _selectedStaff

    /**
     * Login with email and password
     */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            authRepository.login(email, password).collect { resource ->
                _loginState.value = resource
            }
        }
    }

    /**
     * Login with 4-digit PIN
     */
    fun loginWithPin(staffId: Long, pin: String) {
        viewModelScope.launch {
            authRepository.loginWithPin(staffId, pin).collect { resource ->
                _loginState.value = resource
            }
        }
    }

    /**
     * Load staff list for PIN login selection
     */
    fun loadStaffForPinLogin() {
        _staffList.value = Resource.Loading()
        viewModelScope.launch {
            val result = authRepository.getStaffForPinLogin()
            _staffList.value = result
        }
    }

    /**
     * Select a staff member for PIN login
     */
    fun selectStaff(staff: StaffSummaryDto?) {
        _selectedStaff.value = staff
    }

    /**
     * Set or update PIN for current user
     */
    fun setPin(pin: String, currentPassword: String) {
        _setPinState.value = Resource.Loading()
        viewModelScope.launch {
            val result = authRepository.setPin(pin, currentPassword)
            _setPinState.value = result
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout().collect { resource ->
                _logoutState.value = resource
            }
        }
    }

    fun isLoggedIn(): Boolean = authRepository.isLoggedIn()

    fun getStaffName(): String? = authRepository.getStaffName()

    fun getStaffRole(): String? = authRepository.getStaffRole()

    fun getStaffId(): Long = authRepository.getStaffId()

    /**
     * Reset login state (useful when navigating back to login screen)
     */
    fun resetLoginState() {
        _loginState.value = null
        _selectedStaff.value = null
    }
}
