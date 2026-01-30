package com.seacliff.pos.data.local.prefs

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.seacliff.pos.data.remote.api.TokenProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext context: Context
) : TokenProvider {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    override fun getToken(): String? {
        return sharedPreferences.getString(KEY_AUTH_TOKEN, null)
    }

    override fun saveToken(token: String) {
        sharedPreferences.edit {
            putString(KEY_AUTH_TOKEN, token)
        }
    }

    override fun clearToken() {
        sharedPreferences.edit {
            remove(KEY_AUTH_TOKEN)
        }
    }

    fun getStaffId(): Long {
        return sharedPreferences.getLong(KEY_STAFF_ID, 0)
    }

    fun saveStaffId(staffId: Long) {
        sharedPreferences.edit {
            putLong(KEY_STAFF_ID, staffId)
        }
    }

    fun getStaffName(): String? {
        return sharedPreferences.getString(KEY_STAFF_NAME, null)
    }

    fun saveStaffName(name: String) {
        sharedPreferences.edit {
            putString(KEY_STAFF_NAME, name)
        }
    }

    fun getStaffRole(): String? {
        return sharedPreferences.getString(KEY_STAFF_ROLE, null)
    }

    fun saveStaffRole(role: String) {
        sharedPreferences.edit {
            putString(KEY_STAFF_ROLE, role)
        }
    }

    fun isLoggedIn(): Boolean {
        return getToken() != null
    }

    fun clearAll() {
        sharedPreferences.edit {
            clear()
        }
    }

    companion object {
        private const val PREFS_NAME = "seacliff_pos_prefs"
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_STAFF_ID = "staff_id"
        private const val KEY_STAFF_NAME = "staff_name"
        private const val KEY_STAFF_ROLE = "staff_role"
    }
}
