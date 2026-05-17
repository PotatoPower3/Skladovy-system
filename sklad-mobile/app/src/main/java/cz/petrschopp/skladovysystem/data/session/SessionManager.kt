package cz.petrschopp.skladovysystem.data.session

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import cz.petrschopp.skladovysystem.data.model.LoggedUserDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.sessionDataStore by preferencesDataStore(name = "session")

data class SavedCredentials(
    val username: String,
    val password: String
)

data class LoggedUser(
    val id: Int,
    val username: String,
    val firstName: String,
    val lastName: String,
    val roleName: String
)

class SessionManager(
    private val context: Context
) {
    private object Keys {
        val SAVED_USERNAME = stringPreferencesKey("saved_username")
        val SAVED_PASSWORD = stringPreferencesKey("saved_password")

        val USER_ID = stringPreferencesKey("user_id")
        val USERNAME = stringPreferencesKey("username")
        val FIRST_NAME = stringPreferencesKey("first_name")
        val LAST_NAME = stringPreferencesKey("last_name")
        val ROLE_NAME = stringPreferencesKey("role_name")
    }

    val savedCredentialsFlow: Flow<SavedCredentials?> = context.sessionDataStore.data.map { preferences ->
        val username = preferences[Keys.SAVED_USERNAME].orEmpty()
        val password = preferences[Keys.SAVED_PASSWORD].orEmpty()

        if (username.isBlank() || password.isBlank()) {
            null
        } else {
            SavedCredentials(
                username = username,
                password = password
            )
        }
    }

    val loggedUserFlow: Flow<LoggedUser?> = context.sessionDataStore.data.map { preferences ->
        val userId = preferences[Keys.USER_ID]?.toIntOrNull() ?: return@map null

        LoggedUser(
            id = userId,
            username = preferences[Keys.USERNAME].orEmpty(),
            firstName = preferences[Keys.FIRST_NAME].orEmpty(),
            lastName = preferences[Keys.LAST_NAME].orEmpty(),
            roleName = preferences[Keys.ROLE_NAME].orEmpty()
        )
    }

    suspend fun saveLogin(
        username: String,
        password: String,
        user: LoggedUserDto
    ) {
        context.sessionDataStore.edit { preferences ->
            preferences[Keys.SAVED_USERNAME] = username
            preferences[Keys.SAVED_PASSWORD] = password

            preferences[Keys.USER_ID] = user.id.toString()
            preferences[Keys.USERNAME] = user.username
            preferences[Keys.FIRST_NAME] = user.firstName
            preferences[Keys.LAST_NAME] = user.lastName
            preferences[Keys.ROLE_NAME] = user.roleName
        }
    }

    suspend fun clearSession() {
        context.sessionDataStore.edit { preferences ->
            preferences.clear()
        }
    }
}