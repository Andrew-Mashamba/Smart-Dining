package com.seacliff.pos.data.repository

import com.seacliff.pos.data.local.prefs.PreferencesManager
import com.seacliff.pos.data.remote.api.ApiService
import com.seacliff.pos.data.remote.dto.LoginRequest
import com.seacliff.pos.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val preferencesManager: PreferencesManager
) {

    fun login(email: String, password: String): Flow<Resource<Unit>> = flow {
        try {
            emit(Resource.Loading())

            val response = apiService.login(LoginRequest(email, password))

            if (response.isSuccessful && response.body() != null) {
                val loginResponse = response.body()!!

                // Save authentication data
                preferencesManager.saveToken(loginResponse.token)
                preferencesManager.saveStaffId(loginResponse.staff.id)
                preferencesManager.saveStaffName(loginResponse.staff.name)
                preferencesManager.saveStaffRole(loginResponse.staff.role)

                emit(Resource.Success(Unit))
            } else {
                emit(Resource.Error("Login failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Login error: ${e.localizedMessage}"))
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
