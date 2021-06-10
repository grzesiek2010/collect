package org.odk.collect.android.projects

import org.odk.collect.android.configure.SettingsImporter
import org.odk.collect.projects.ProjectsRepository

class ProjectCreator(
    private val projectImporter: ProjectImporter,
    private val projectsRepository: ProjectsRepository,
    private val currentProjectProvider: CurrentProjectProvider,
    private val settingsImporter: SettingsImporter,
    private val projectDetailsCreator: ProjectDetailsCreator
) {

    fun createNewProject(settingsJson: String): Boolean {
        val newProject = projectDetailsCreator.getProject(settingsJson)
        val savedProject = projectImporter.importNewProject(newProject)

        val settingsImportedSuccessfully = settingsImporter.fromJSON(settingsJson, savedProject.uuid)

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
}
