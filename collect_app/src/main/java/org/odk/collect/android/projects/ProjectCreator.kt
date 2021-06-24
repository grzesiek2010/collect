package org.odk.collect.android.projects

import org.json.JSONException
import org.json.JSONObject
import org.odk.collect.android.configure.SettingsImporter
import org.odk.collect.android.configure.qr.AppConfigurationKeys
import org.odk.collect.android.preferences.keys.GeneralKeys
import org.odk.collect.projects.ProjectsRepository

class ProjectCreator(
    private val projectImporter: ProjectImporter,
    private val projectsRepository: ProjectsRepository,
    private val currentProjectProvider: CurrentProjectProvider,
    private val settingsImporter: SettingsImporter,
    private val projectGenerator: ProjectGenerator
) {

    fun createNewProject(settingsJson: String): Boolean {
        var projectName = ""
        var projectIcon = ""
        var projectColor = ""
        if (JSONObject(settingsJson).has(AppConfigurationKeys.PROJECT)) {
            val projectDetails = JSONObject(settingsJson).getJSONObject(AppConfigurationKeys.PROJECT)
            if (projectDetails.has(AppConfigurationKeys.PROJECT_NAME)) {
                projectName = projectDetails.getString(AppConfigurationKeys.PROJECT_NAME)
            }
            if (projectDetails.has(AppConfigurationKeys.PROJECT_ICON)) {
                projectIcon = projectDetails.getString(AppConfigurationKeys.PROJECT_ICON)
            }
            if (projectDetails.has(AppConfigurationKeys.PROJECT_COLOR)) {
                projectColor = projectDetails.getString(AppConfigurationKeys.PROJECT_COLOR)
            }
        }

        val newProject = projectGenerator.generateProjectFromDetails(getServerUrl(settingsJson), projectName, projectIcon, projectColor)
        val savedProject = projectImporter.importNewProject(newProject)

        val settingsImportedSuccessfully = settingsImporter.fromJSON(settingsJson, savedProject)

        return if (settingsImportedSuccessfully) {
            if (projectsRepository.getAll().size == 1) {
                currentProjectProvider.setCurrentProject(savedProject.uuid)
            }
            true
        } else {
            projectsRepository.delete(savedProject.uuid)
            false
        }
    }

    private fun getServerUrl(settingsJson: String): String {
        val url = try {
            JSONObject(settingsJson)
                .getJSONObject(AppConfigurationKeys.GENERAL)
                .getString(GeneralKeys.KEY_SERVER_URL)
        } catch (e: JSONException) {
            ""
        }
        return if (url.isNotBlank()) url else "https://demo.getodk.org"
    }
}
