package com.train.ramarai.postest.utils


import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit


val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
class SettingPreferences private constructor(private val dataStore: DataStore<Preferences>){

    companion object {
        @Volatile
        private var INSTANCE: SettingPreferences? = null

        private val IS_LOGGED_IN_KEY = booleanPreferencesKey("is_logged_in")
        private val LOGIN_TIMESTAMP_KEY = longPreferencesKey("login_timestamp")
        private val USERNAME_KEY = stringPreferencesKey("username")
        private val ROLE_KEY = stringPreferencesKey("role")
        private val EMAIL_KEY = stringPreferencesKey("email")
        private val UID_KEY = stringPreferencesKey("userID")
        private const val LOGIN_DURATION_HOURS = 12L

        fun getInstance(dataStore: DataStore<Preferences>): SettingPreferences {
            return INSTANCE ?: synchronized(this){
                val instance = SettingPreferences(dataStore)
                INSTANCE = instance
                instance
            }

        }
    }

    suspend fun setUserDetails(username: String, email: String, role: String, userID: String) {
        dataStore.edit { preferences ->
            preferences[USERNAME_KEY] = username
            preferences[EMAIL_KEY] = email
            preferences[ROLE_KEY] = role
            preferences[UID_KEY] = userID
        }
    }

    fun getUserDetails(): Flow<Map<String, String>> {
        return dataStore.data.map { preferences ->
            val username = preferences[USERNAME_KEY] ?: ""
            val email = preferences[EMAIL_KEY] ?: ""
            val role = preferences[ROLE_KEY] ?: ""
            val userID = preferences[UID_KEY] ?: ""

            mapOf(
                "username" to username,
                "email" to email,
                "role" to role,
                "documentsID" to userID
            )
        }
    }

    suspend fun setLoginSession() {
        dataStore.edit {preferences ->
            preferences[IS_LOGGED_IN_KEY] = true
            preferences[LOGIN_TIMESTAMP_KEY] = System.currentTimeMillis()
        }
    }

    suspend fun clearLoginSession() {
        dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN_KEY] = false
            preferences.remove(LOGIN_TIMESTAMP_KEY)
        }
    }

    fun isUserLoggedIn(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[IS_LOGGED_IN_KEY] ?: false
        }
    }

    fun isSessionExpired(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            val loginTimeStamp = preferences[LOGIN_TIMESTAMP_KEY] ?: 0L
            val currentTime = System.currentTimeMillis()
            val diffInMillis = currentTime - loginTimeStamp
            val diffInHours = TimeUnit.MILLISECONDS.toHours(diffInMillis)
            diffInHours >= LOGIN_DURATION_HOURS
        }
    }
}