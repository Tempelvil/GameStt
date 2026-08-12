package com.example.gamest.data.local.preferences

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.steamDataStore by preferencesDataStore(
    name = "steam_preferences"
)

data class SteamConnectionData(
    val isConnected: Boolean = false,
    val profileUrl: String = "",
    val steamId: String = "",
    val lastSyncAt: Long? = null
)

class SteamConnectionPreferences(
    private val context: Context
) {

    private object Keys {
        val IS_CONNECTED =
            booleanPreferencesKey("steam_is_connected")

        val PROFILE_URL =
            stringPreferencesKey("steam_profile_url")

        val STEAM_ID =
            stringPreferencesKey("steam_id")

        val LAST_SYNC_AT =
            longPreferencesKey("steam_last_sync_at")
    }

    val connectionData: Flow<SteamConnectionData> =
        context.steamDataStore.data.map { preferences ->
            SteamConnectionData(
                isConnected =
                    preferences[Keys.IS_CONNECTED] ?: false,

                profileUrl =
                    preferences[Keys.PROFILE_URL] ?: "",

                steamId =
                    preferences[Keys.STEAM_ID] ?: "",

                lastSyncAt =
                    preferences[Keys.LAST_SYNC_AT]
            )
        }

    suspend fun saveConnection(
        profileUrl: String,
        steamId: String
    ) {
        context.steamDataStore.edit { preferences ->
            preferences[Keys.IS_CONNECTED] = true
            preferences[Keys.PROFILE_URL] = profileUrl
            preferences[Keys.STEAM_ID] = steamId
            preferences[Keys.LAST_SYNC_AT] =
                System.currentTimeMillis()
        }
    }

    suspend fun updateLastSync() {
        context.steamDataStore.edit { preferences ->
            preferences[Keys.LAST_SYNC_AT] =
                System.currentTimeMillis()
        }
    }

    suspend fun clearConnection() {
        context.steamDataStore.edit { preferences ->
            preferences.remove(Keys.IS_CONNECTED)
            preferences.remove(Keys.PROFILE_URL)
            preferences.remove(Keys.STEAM_ID)
            preferences.remove(Keys.LAST_SYNC_AT)
        }
    }
}