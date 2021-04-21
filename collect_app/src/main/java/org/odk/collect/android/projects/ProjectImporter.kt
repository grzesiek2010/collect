package org.odk.collect.android.projects

import org.odk.collect.android.preferences.keys.MetaKeys
import org.odk.collect.android.preferences.source.Settings

class ProjectImporter(
    private val projectsRepository: ProjectsRepository,
    private val metaSettings: Settings
) {
    fun importDemoProject() {
        val project = Project("Demo project", "D", "#3e9fcc", DEMO_PROJECT_ID)
        projectsRepository.add(project)
        metaSettings.save(MetaKeys.CURRENT_PROJECT_ID, DEMO_PROJECT_ID)
    }

    // Now it does the same like importDemoProject() but it should be changed later
    fun importExistingProject() {
        val project = Project("Demo project", "D", "#3e9fcc", DEMO_PROJECT_ID)
        projectsRepository.add(project)
        metaSettings.save(MetaKeys.CURRENT_PROJECT_ID, DEMO_PROJECT_ID)
    }

    companion object {
        const val DEMO_PROJECT_ID = "DEMO"
    }
}
