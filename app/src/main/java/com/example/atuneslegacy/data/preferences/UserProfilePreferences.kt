package com.example.atuneslegacy.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.atuneslegacy.ui.theme.AppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.userPreferencesDataStore by preferencesDataStore(name = "user_profile_prefs")

class UserProfilePreferences(private val context: Context) {

    companion object {
        val USER_NAME_KEY = stringPreferencesKey("user_name")
        val USER_BIO_KEY = stringPreferencesKey("user_bio")
        val PROFILE_PIC_KEY = stringPreferencesKey("profile_pic_uri")
        val APP_THEME_KEY = stringPreferencesKey("app_theme")
        val FIRST_LAUNCH_KEY = booleanPreferencesKey("is_first_launch_completed")
    }

    val isFirstLaunchCompletedFlow: Flow<Boolean> = context.userPreferencesDataStore.data
        .map { prefs -> prefs[FIRST_LAUNCH_KEY] ?: false }

    val appThemeFlow: Flow<AppTheme> = context.userPreferencesDataStore.data
        .map { prefs ->
            val themeName = prefs[APP_THEME_KEY] ?: AppTheme.LEGACY_LIGHT.name
            try {
                AppTheme.valueOf(themeName)
            } catch (e: Exception) {
                AppTheme.LEGACY_LIGHT
            }
        }

    val userNameFlow: Flow<String> = context.userPreferencesDataStore.data
        .map { prefs -> prefs[USER_NAME_KEY] ?: "Vinyl Collector" }

    val userBioFlow: Flow<String> = context.userPreferencesDataStore.data
        .map { prefs -> prefs[USER_BIO_KEY] ?: "Passionate audiophile and local media curator." }

    val profilePicFlow: Flow<String?> = context.userPreferencesDataStore.data
        .map { prefs -> prefs[PROFILE_PIC_KEY] }

    suspend fun completeFirstLaunch() {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[FIRST_LAUNCH_KEY] = true
        }
    }

    suspend fun saveTheme(theme: AppTheme) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[APP_THEME_KEY] = theme.name
        }
    }

    suspend fun saveProfile(name: String, bio: String) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[USER_NAME_KEY] = name
            prefs[USER_BIO_KEY] = bio
        }
    }

    suspend fun saveProfilePicture(uri: String?) {
        context.userPreferencesDataStore.edit { prefs ->
            if (uri != null) {
                prefs[PROFILE_PIC_KEY] = uri
            } else {
                prefs.remove(PROFILE_PIC_KEY)
            }
        }
    }
}
