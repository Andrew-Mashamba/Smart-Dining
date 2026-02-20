package com.seacliff.pos.data.repository

import com.seacliff.pos.data.local.prefs.PreferencesManager
import com.seacliff.pos.data.remote.api.ApiService
import com.seacliff.pos.worker.SyncManager
import com.seacliff.pos.data.remote.dto.LoginRequest
import com.seacliff.pos.data.remote.dto.PinLoginRequest
import com.seacliff.pos.data.remote.dto.SetPinRequest
import com.seacliff.pos.data.remote.dto.StaffSummaryDto
import com.seacliff.pos.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val preferencesManager: PreferencesManager,
    private val syncManager: SyncManager
) {

    /**
     * Login with email and password
     */
    fun login(email: String, password: String): Flow<Resource<Unit>> = flow {
        try {
            emit(Resource.Loading())

            val response = apiService.login(LoginRequest(email, password))

            if (response.isSuccessful && response.body() != null) {
                val loginResponse = response.body()!!

                // Save authentication data
                preferencesManager.saveToken(loginResponse.token)
                preferencesManager.saveStaffId(loginResponse.user.id)
                preferencesManager.saveStaffName(loginResponse.user.name)
                preferencesManager.saveStaffRole(loginResponse.user.role)

                syncManager.triggerImmediateSync()
                emit(Resource.Success(Unit))
            } else {
                emit(Resource.Error("Login failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Login error: ${e.localizedMessage}"))
        }
    }

    /**
     * Login with 4-digit PIN (for waiters)
     */
    fun loginWithPin(staffId: Long, pin: String): Flow<Resource<Unit>> = flow {
        try {
            emit(Resource.Loading())

            if (pin.length != 4 || !pin.all { it.isDigit() }) {
                emit(Resource.Error("PIN must be exactly 4 digits"))
                return@flow
            }

            val response = apiService.loginWithPin(PinLoginRequest(staffId, pin))

            if (response.isSuccessful && response.body() != null) {
                val loginResponse = response.body()!!

                // Save authentication data
                preferencesManager.saveToken(loginResponse.token)
                preferencesManager.saveStaffId(loginResponse.user.id)
                preferencesManager.saveStaffName(loginResponse.user.name)
                preferencesManager.saveStaffRole(loginResponse.user.role)

                syncManager.triggerImmediateSync()
                emit(Resource.Success(Unit))
            } else {
                val errorMessage = when (response.code()) {
                    401 -> "Invalid PIN"
                    403 -> "Account is inactive"
                    404 -> "Staff member not found"
                    422 -> "PIN not set. Please use email/password login."
                    else -> "Login failed: ${response.message()}"
                }
                emit(Resource.Error(errorMessage))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Login error: ${e.localizedMessage}"))
        }
    }

    /**
     * Get list of staff members who have PIN set (for PIN login screen)
     */
    suspend fun getStaffForPinLogin(): Resource<List<StaffSummaryDto>> {
        return try {
            val response = apiService.getStaffForPinLogin()

            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!.staff)
            } else {
                Resource.Error("Failed to load staff list")
            }
        } catch (e: Exception) {
            Resource.Error("Error: ${e.localizedMessage}")
        }
    }

    /**
     * Set or update PIN for current user
     */
    suspend fun setPin(pin: String, currentPassword: String): Resource<Unit> {
        return try {
            if (pin.length != 4 || !pin.all { it.isDigit() }) {
                return Resource.Error("PIN must be exactly 4 digits")
            }

            val response = apiService.setPin(SetPinRequest(pin, currentPassword))

            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                val errorMessage = when (response.code()) {
                    401 -> "Current password is incorrect"
                    else -> "Failed to set PIN: ${response.message()}"
                }
                Resource.Error(errorMessage)
            }
        } catch (e: Exception) {
            Resource.Error("Error: ${e.localizedMessage}")
        }
    }

    fun logout(): Flow<Resource<Unit>> = flow {
        try {
            emit(Resource.Loading())

            val response = apiService.logout()

            if (response.isSuccessful) {
                preferencesManager.clearAll()
                emit(Resource.Success(Unit))
            } else {
                // Clear local data even if API call fails
                preferencesManager.clearAll()
                emit(Resource.Success(Unit))
            }
        } catch (e: Exception) {
            // Clear local data even on error
            preferencesManager.clearAll()
            emit(Resource.Success(Unit))
        }
    }

    fun isLoggedIn(): Boolean = preferencesManager.isLoggedIn()

    fun getStaffRole(): String? = preferencesManager.getStaffRole()

    fun getStaffId(): Long = preferencesManager.getStaffId()

    fun getStaffName(): String? = preferencesManager.getStaffName()
}
