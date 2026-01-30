package com.seacliff.pos.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    fun login(email: String, password: String) {
        viewModelScope.launch {
            authRepository.login(email, password).collect { resource ->
                _loginState.value = resource
            }
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
}
