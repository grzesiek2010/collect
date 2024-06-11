package org.odk.collect.android.preferences.screens

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.gson.Gson
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.odk.collect.android.application.initialization.AnalyticsInitializer
import org.odk.collect.android.application.initialization.MapsInitializer
import org.odk.collect.android.injection.config.AppDependencyModule
import org.odk.collect.android.projects.ProjectsDataService
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.fragmentstest.FragmentScenarioLauncherRule
import org.odk.collect.maps.layers.ReferenceLayerRepository
import org.odk.collect.projects.Project
import org.odk.collect.projects.ProjectsRepository
import org.odk.collect.settings.InMemSettingsProvider
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.shared.strings.UUIDGenerator

@RunWith(AndroidJUnit4::class)
class MapsPreferencesFragmentTest {

    @get:Rule
    val launcherRule = FragmentScenarioLauncherRule()

    val project = Project.Saved("123", "Project X", "X", "#cccccc")
    private val projectsDataService = mock<ProjectsDataService>().apply {
        whenever(getCurrentProject()).thenReturn(project)
    }
    private val projectsRepository = mock<ProjectsRepository>().apply {
        whenever(get(any())).thenReturn(project)
    }

    private val referenceLayerRepository = mock<ReferenceLayerRepository>()
    private val settingsProvider = InMemSettingsProvider()

    @Before
    fun setup() {
        CollectHelpers.overrideAppDependencyModule(object : AppDependencyModule() {
            override fun providesCurrentProjectProvider(
                settingsProvider: SettingsProvider,
                projectsRepository: ProjectsRepository,
                analyticsInitializer: AnalyticsInitializer,
                context: Context,
                mapsInitializer: MapsInitializer
            ): ProjectsDataService {
                return projectsDataService
            }

            override fun providesProjectsRepository(uuidGenerator: UUIDGenerator, gson: Gson, settingsProvider: SettingsProvider): ProjectsRepository {
                return projectsRepository
            }

            override fun providesSettingsProvider(context: Context?): SettingsProvider {
                return settingsProvider
            }
        })
    }

    @Test
    fun `Project Name preference should be visible`() {
        settingsProvider.getUnprotectedSettings().save(MapsPreferencesFragment.REFERENCE_LAYER_KEY, "blah")

        val scenario = launcherRule.launch(MapsPreferencesFragment::class.java)


    }
}
