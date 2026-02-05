package es.maestre.juntosjc

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val PREFERENCES_NAME = "user_prefs"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = PREFERENCES_NAME)

class DataStoreManager(private val context: Context) {
    companion object {
        private val KEY_EMAIL = stringPreferencesKey("saved_email")
    }

    suspend fun saveEmail(email: String) {
        context.dataStore.edit { prefs: MutablePreferences ->
            prefs[KEY_EMAIL] = email
        }
    }

    suspend fun clearEmail() {
        context.dataStore.edit { prefs: MutablePreferences ->
            prefs.remove(KEY_EMAIL)
        }
    }

    fun getEmail(): Flow<String?> {
        return context.dataStore.data.map { prefs: Preferences ->
            prefs[KEY_EMAIL]
        }
    }
}
