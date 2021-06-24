package org.odk.collect.android.configure

import org.json.JSONException
import org.json.JSONObject
import org.odk.collect.android.application.initialization.SettingsMigrator
import org.odk.collect.android.configure.qr.AppConfigurationKeys
import org.odk.collect.android.preferences.source.SettingsProvider
import org.odk.collect.projects.Project
import org.odk.collect.shared.Settings

class SettingsImporter(
    private val settingsProvider: SettingsProvider,
    private val settngsMigrator: SettingsMigrator,
    private val settingsValidator: SettingsValidator,
    private val generalDefaults: Map<String, Any>,
    private val adminDefaults: Map<String, Any>,
    private val settingsChangedHandler: SettingsChangeHandler
) {

    fun fromJSON(json: String, project: Project.Saved): Boolean {
        if (!settingsValidator.isValid(json)) {
            return false
        }

        val generalSettings = settingsProvider.getGeneralSettings(project.uuid)
        val adminSettings = settingsProvider.getAdminSettings(project.uuid)

        generalSettings.clear()
        adminSettings.clear()

        try {
            val jsonObject = JSONObject(json)

            val general = jsonObject.getJSONObject(AppConfigurationKeys.GENERAL)
            importToPrefs(general, generalSettings)

            val admin = jsonObject.getJSONObject(AppConfigurationKeys.ADMIN)
            importToPrefs(admin, adminSettings)
        } catch (ignored: JSONException) {
            // Ignored
        }

        settngsMigrator.migrate(generalSettings, adminSettings)

        clearUnknownKeys(generalSettings, generalDefaults)
        clearUnknownKeys(adminSettings, adminDefaults)

        loadDefaults(generalSettings, generalDefaults)
        loadDefaults(adminSettings, adminDefaults)

        for ((key, value) in generalSettings.getAll()) {
            settingsChangedHandler.onSettingChanged(project.uuid, value, key)
        }
        for ((key, value) in adminSettings.getAll()) {
            settingsChangedHandler.onSettingChanged(project.uuid, value, key)
        }
        return true
    }

    private fun importToPrefs(jsonObject: JSONObject, preferences: Settings) {
        jsonObject.keys().forEach {
            preferences.save(it, jsonObject[it])
        }
    }

    private fun loadDefaults(preferences: Settings, defaults: Map<String, Any>) {
        defaults.forEach { (key, value) ->
            if (!preferences.contains(key)) {
                preferences.save(key, value)
            }
        }
    }

    private fun clearUnknownKeys(preferences: Settings, defaults: Map<String, Any>) {
        preferences.getAll().forEach { (key, _) ->
            if (!defaults.containsKey(key)) {
                preferences.remove(key)
            }
        }
    }
}
