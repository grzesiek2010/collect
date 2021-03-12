package org.odk.collect.android.preferences.source

import android.content.Context
import androidx.preference.PreferenceManager
import org.odk.collect.android.preferences.keys.AdminKeys
import org.odk.collect.android.preferences.keys.GeneralKeys
import org.odk.collect.android.preferences.keys.MetaKeys.CURRENT_PROJECT

class PreferencesDataSourceProvider(private val context: Context) {
    fun getMetaPreferences(): PreferencesDataSource {
        return preferences.getOrPut(META_PREFS_NAME) {
            SharedPreferencesDataSource(context.getSharedPreferences(META_PREFS_NAME, Context.MODE_PRIVATE))
        }
    }

    @JvmOverloads
    fun getGeneralPreferences(projectId: String = ""): PreferencesDataSource {
        val preferenceId = getProjectPreferenceId(GENERAL_PREFS_NAME, projectId)

        return preferences.getOrPut(preferenceId) {
            if (projectId == GENERAL_PREFS_NAME) {
                SharedPreferencesDataSource(PreferenceManager.getDefaultSharedPreferences(context), GeneralKeys.DEFAULTS)
            } else {
                SharedPreferencesDataSource(context.getSharedPreferences(preferenceId, Context.MODE_PRIVATE), GeneralKeys.DEFAULTS)
            }
        }
    }

    @JvmOverloads
    fun getAdminPreferences(projectId: String = ""): PreferencesDataSource {
        val preferenceId = getProjectPreferenceId(ADMIN_PREFS_NAME, projectId)

        return preferences.getOrPut(preferenceId) {
            SharedPreferencesDataSource(context.getSharedPreferences(preferenceId, Context.MODE_PRIVATE), AdminKeys.getDefaults())
        }
    }

    private fun getProjectPreferenceId(preferenceName: String, projectId: String): String {
        return if (projectId.isBlank()) {
            preferenceName + getMetaPreferences().getString(CURRENT_PROJECT)
        } else {
            preferenceName + projectId
        }
    }

    companion object {
        private val preferences = mutableMapOf<String, PreferencesDataSource>()

        private const val META_PREFS_NAME = "meta"
        private const val GENERAL_PREFS_NAME = "general_prefs"
        private const val ADMIN_PREFS_NAME = "admin_prefs"
    }
}
