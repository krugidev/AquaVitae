package pt.aquavitae.android.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Guarda a sessão autenticada (token JWT + identificação básica do utilizador)
 * em DataStore Preferences — nunca em SharedPreferences, conforme decidido para
 * o projeto. A instância de [DataStore] é fornecida pelo Hilt (ver [pt.aquavitae.android.di.DataStoreModule]).
 */
class TokenDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    private object Keys {
        val TOKEN = stringPreferencesKey("jwt_token")
        val USER_ID = longPreferencesKey("user_id")
        val USERNAME = stringPreferencesKey("username")
    }

    val tokenFlow: Flow<String?> = dataStore.data.map { prefs -> prefs[Keys.TOKEN] }
    val usernameFlow: Flow<String?> = dataStore.data.map { prefs -> prefs[Keys.USERNAME] }
    val userIdFlow: Flow<Long?> = dataStore.data.map { prefs -> prefs[Keys.USER_ID] }

    suspend fun saveSession(token: String, userId: Long, username: String) {
        dataStore.edit { prefs ->
            prefs[Keys.TOKEN] = token
            prefs[Keys.USER_ID] = userId
            prefs[Keys.USERNAME] = username
        }
    }

    suspend fun clearSession() {
        dataStore.edit { prefs -> prefs.clear() }
    }
}
