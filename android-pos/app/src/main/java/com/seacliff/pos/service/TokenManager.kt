package com.seacliff.pos.service

import android.os.Build
import com.google.firebase.messaging.FirebaseMessaging
import com.seacliff.pos.data.remote.api.ApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

object TokenManager {

    fun registerToken(apiService: ApiService, authToken: String) {
        FirebaseMessaging.getInstance().token.addOnSuccessListener { fcmToken ->
            Timber.d("FCM Token obtained: $fcmToken")

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = apiService.registerDeviceToken(
                        authHeader = "Bearer $authToken",
                        body = mapOf(
                            "fcm_token" to fcmToken,
                            "device_name" to "${Build.MANUFACTURER} ${Build.MODEL}"
                        )
                    )
                    if (response.isSuccessful) {
                        Timber.d("FCM token registered successfully")
                    } else {
                        Timber.e("Failed to register FCM token: ${response.code()}")
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error registering FCM token")
                }
            }
        }.addOnFailureListener { e ->
            Timber.e(e, "Failed to get FCM token")
        }
    }

    fun removeToken(apiService: ApiService, authToken: String) {
        FirebaseMessaging.getInstance().token.addOnSuccessListener { fcmToken ->
            Timber.d("Removing FCM Token: $fcmToken")

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = apiService.removeDeviceToken(
                        authHeader = "Bearer $authToken",
                        body = mapOf("fcm_token" to fcmToken)
                    )
                    if (response.isSuccessful) {
                        Timber.d("FCM token removed successfully")
                    } else {
                        Timber.e("Failed to remove FCM token: ${response.code()}")
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error removing FCM token")
                }
            }
        }
    }
}
