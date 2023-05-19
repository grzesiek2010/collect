package org.odk.collect.android.mainmenu

import android.app.Application
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.odk.collect.android.R
import org.odk.collect.android.activities.DeleteSavedFormActivity
import org.odk.collect.android.activities.FormDownloadListActivity
import org.odk.collect.android.activities.InstanceChooserList
import org.odk.collect.android.activities.InstanceUploaderListActivity
import org.odk.collect.android.activities.viewmodels.CurrentProjectViewModel
import org.odk.collect.android.application.initialization.AnalyticsInitializer
import org.odk.collect.android.formlists.blankformlist.BlankFormListActivity
import org.odk.collect.android.formmanagement.InstancesAppState
import org.odk.collect.android.injection.config.AppDependencyModule
import org.odk.collect.android.instancemanagement.autosend.AutoSendSettingsProvider
import org.odk.collect.android.projects.CurrentProjectProvider
import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.android.utilities.ApplicationConstants
import org.odk.collect.android.utilities.FormsRepositoryProvider
import org.odk.collect.android.utilities.InstancesRepositoryProvider
import org.odk.collect.android.version.VersionInformation
import org.odk.collect.androidshared.livedata.MutableNonNullLiveData
import org.odk.collect.androidshared.network.NetworkStateProvider
import org.odk.collect.androidtest.ActivityScenarioLauncherRule
import org.odk.collect.async.Scheduler
import org.odk.collect.projects.Project
import org.odk.collect.projects.ProjectsRepository
import org.odk.collect.settings.SettingsProvider

@RunWith(AndroidJUnit4::class)
class MainMenuActivityTest {

    private val project = Project.Saved("123", "Project", "P", "#f5f5f5")

    private val mainMenuViewModel = mock<MainMenuViewModel> {
        on { sendableInstancesCount } doReturn MutableLiveData(0)
        on { sentInstancesCount } doReturn MutableLiveData(0)
        on { editableInstancesCount } doReturn MutableLiveData(0)
    }

    private val currentProjectViewModel = mock<CurrentProjectViewModel> {
        on { hasCurrentProject() } doReturn true
        on { currentProject } doReturn MutableNonNullLiveData(project)
    }

    @get:Rule
    val launcherRule = ActivityScenarioLauncherRule()

    @Before
    fun setup() {
        CollectHelpers.overrideAppDependencyModule(object : AppDependencyModule() {
            override fun providesMainMenuViewModelFactory(
                versionInformation: VersionInformation,
                application: Application,
                settingsProvider: SettingsProvider,
                instancesAppState: InstancesAppState,
                scheduler: Scheduler,
                formsRepositoryProvider: FormsRepositoryProvider,
                instancesRepositoryProvider: InstancesRepositoryProvider,
                autoSendSettingsProvider: AutoSendSettingsProvider,
                networkStateProvider: NetworkStateProvider
            ): MainMenuViewModel.Factory {
                return object : MainMenuViewModel.Factory(
                    versionInformation,
                    application,
                    settingsProvider,
                    instancesAppState,
                    scheduler,
                    formsRepositoryProvider.get(),
                    instancesRepositoryProvider.get(),
                    autoSendSettingsProvider,
                    networkStateProvider
                ) {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return mainMenuViewModel as T
                    }
                }
            }

            override fun providesCurrentProjectViewModel(
                currentProjectProvider: CurrentProjectProvider,
                analyticsInitializer: AnalyticsInitializer,
                storagePathProvider: StoragePathProvider,
                projectsRepository: ProjectsRepository
            ): CurrentProjectViewModel.Factory {
                return object : CurrentProjectViewModel.Factory(
                    currentProjectProvider,
                    analyticsInitializer
                ) {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return currentProjectViewModel as T
                    }
                }
            }
        })
    }

    @Test
    fun `Activity title is current project name`() {
        val scenario = launcherRule.launch(MainMenuActivity::class.java)
        scenario.onActivity {
            assertThat(it.title, `is`("Project"))
        }
    }

    @Test
    fun `Project icon for current project should be displayed`() {
        val scenario = launcherRule.launch(MainMenuActivity::class.java)
        scenario.onActivity { activity: MainMenuActivity ->
            val projectIcon = activity.findViewById<TextView>(R.id.project_icon_text)

            assertThat(projectIcon.visibility, `is`(View.VISIBLE))
            assertThat(projectIcon.text, `is`(project.icon))

            val background = projectIcon.background as GradientDrawable
            assertThat(background.color!!.defaultColor, equalTo(Color.parseColor(project.color)))
        }
    }

    @Test
    fun `Fill Blank Form button should have proper text`() {
        val scenario = launcherRule.launch(MainMenuActivity::class.java)
        scenario.onActivity { activity: MainMenuActivity ->
            val button = activity.findViewById<StartNewFormButton>(R.id.enter_data)
            assertThat(button.text, `is`(activity.getString(R.string.enter_data)))
        }
    }

    @Test
    fun `Fill Blank Form button should start list of blank forms`() {
        val scenario = launcherRule.launch(MainMenuActivity::class.java)
        scenario.onActivity { activity: MainMenuActivity ->
            Intents.init()

            val button = activity.findViewById<StartNewFormButton>(R.id.enter_data)
            button.performClick()
            assertThat(
                Intents.getIntents()[0],
                hasComponent(BlankFormListActivity::class.java.name)
            )

            Intents.release()
        }
    }

    @Test
    fun `Edit Saved Form button should have proper text`() {
        val scenario = launcherRule.launch(MainMenuActivity::class.java)
        scenario.onActivity { activity: MainMenuActivity ->
            val button = activity.findViewById<MainMenuButton>(R.id.review_data)
            assertThat(button.text, `is`(activity.getString(R.string.review_data)))
        }
    }

    @Test
    fun `Edit Saved Form button should start list of saved forms`() {
        val scenario = launcherRule.launch(MainMenuActivity::class.java)
        scenario.onActivity { activity: MainMenuActivity ->
            Intents.init()

            val button = activity.findViewById<MainMenuButton>(R.id.review_data)
            button.performClick()
            assertThat(Intents.getIntents()[0], hasComponent(InstanceChooserList::class.java.name))
            assertThat(
                Intents.getIntents()[0].extras!!.get(ApplicationConstants.BundleKeys.FORM_MODE),
                `is`(ApplicationConstants.FormModes.EDIT_SAVED)
            )

            Intents.release()
        }
    }

    @Test
    fun `Send Finalized Form button should have proper text`() {
        val scenario = launcherRule.launch(MainMenuActivity::class.java)
        scenario.onActivity { activity: MainMenuActivity ->
            val button = activity.findViewById<MainMenuButton>(R.id.send_data)
            assertThat(button.text, `is`(activity.getString(R.string.send_data)))
        }
    }

    @Test
    fun `Send Finalized Form button should start list of finalized forms`() {
        val scenario = launcherRule.launch(MainMenuActivity::class.java)
        scenario.onActivity { activity: MainMenuActivity ->
            Intents.init()

            val button = activity.findViewById<MainMenuButton>(R.id.send_data)
            button.performClick()
            assertThat(
                Intents.getIntents()[0],
                hasComponent(InstanceUploaderListActivity::class.java.name)
            )

            Intents.release()
        }
    }

    @Test
    fun `View Sent Form button should have proper text`() {
        val scenario = launcherRule.launch(MainMenuActivity::class.java)
        scenario.onActivity { activity: MainMenuActivity ->
            val button = activity.findViewById<MainMenuButton>(R.id.view_sent_forms)
            assertThat(button.text, `is`(activity.getString(R.string.view_sent_forms)))
        }
    }

    @Test
    fun `View Sent Form button should start list of sent forms`() {
        val scenario = launcherRule.launch(MainMenuActivity::class.java)
        scenario.onActivity { activity: MainMenuActivity ->
            Intents.init()

            val button = activity.findViewById<MainMenuButton>(R.id.view_sent_forms)
            button.performClick()
            assertThat(Intents.getIntents()[0], hasComponent(InstanceChooserList::class.java.name))
            assertThat(
                Intents.getIntents()[0].extras!!.get(ApplicationConstants.BundleKeys.FORM_MODE),
                `is`(ApplicationConstants.FormModes.VIEW_SENT)
            )

            Intents.release()
        }
    }

    @Test
    fun `Get Blank Form button should have proper text`() {
        val scenario = launcherRule.launch(MainMenuActivity::class.java)
        scenario.onActivity { activity: MainMenuActivity ->
            val button = activity.findViewById<MainMenuButton>(R.id.get_forms)
            assertThat(button.text, `is`(activity.getString(R.string.get_forms)))
        }
    }

    @Test
    fun `Get Blank Form button should start list of forms to download`() {
        val scenario = launcherRule.launch(MainMenuActivity::class.java)
        scenario.onActivity { activity: MainMenuActivity ->
            Intents.init()

            val button = activity.findViewById<MainMenuButton>(R.id.get_forms)
            button.performClick()
            assertThat(
                Intents.getIntents()[0],
                hasComponent(FormDownloadListActivity::class.java.name)
            )

            Intents.release()
        }
    }

    @Test
    fun `Delete Saved Form button should have proper text`() {
        val scenario = launcherRule.launch(MainMenuActivity::class.java)
        scenario.onActivity { activity: MainMenuActivity ->
            val button = activity.findViewById<MainMenuButton>(R.id.manage_forms)
            assertThat(button.text, `is`(activity.getString(R.string.manage_files)))
        }
    }

    @Test
    fun `Delete Saved Form button should start list of forms to delete`() {
        val scenario = launcherRule.launch(MainMenuActivity::class.java)
        scenario.onActivity { activity: MainMenuActivity ->
            Intents.init()

            val button = activity.findViewById<MainMenuButton>(R.id.manage_forms)
            button.performClick()
            assertThat(
                Intents.getIntents()[0],
                hasComponent(DeleteSavedFormActivity::class.java.name)
            )

            Intents.release()
        }
    }

    @Test
    fun `When editSavedFormButton is enabled in settings, should be visible`() {
        whenever(mainMenuViewModel.shouldEditSavedFormButtonBeVisible()).thenReturn(true)

        val scenario = launcherRule.launch(MainMenuActivity::class.java)
        scenario.onActivity { activity: MainMenuActivity ->
            val editSavedFormButton = activity.findViewById<MainMenuButton>(R.id.review_data)
            assertThat(editSavedFormButton.visibility, equalTo(View.VISIBLE))
        }
    }

    @Test
    fun `When editSavedFormButton is disabled in settings, should be gone`() {
        whenever(mainMenuViewModel.shouldEditSavedFormButtonBeVisible()).thenReturn(false)

        val scenario = launcherRule.launch(MainMenuActivity::class.java)
        scenario.onActivity { activity: MainMenuActivity ->
            val editSavedFormButton = activity.findViewById<MainMenuButton>(R.id.review_data)
            assertThat(editSavedFormButton.visibility, equalTo(View.GONE))
        }
    }

    @Test
    fun `When sendFinalizedFormButton is enabled in settings, should be visible`() {
        whenever(mainMenuViewModel.shouldSendFinalizedFormButtonBeVisible()).thenReturn(true)

        val scenario = launcherRule.launch(MainMenuActivity::class.java)
        scenario.onActivity { activity: MainMenuActivity ->
            val editSavedFormButton = activity.findViewById<MainMenuButton>(R.id.send_data)
            assertThat(editSavedFormButton.visibility, equalTo(View.VISIBLE))
        }
    }

    @Test
    fun `When sendFinalizedFormButton is disabled in settings, should be gone`() {
        whenever(mainMenuViewModel.shouldSendFinalizedFormButtonBeVisible()).thenReturn(false)

        val scenario = launcherRule.launch(MainMenuActivity::class.java)
        scenario.onActivity { activity: MainMenuActivity ->
            val editSavedFormButton = activity.findViewById<MainMenuButton>(R.id.send_data)
            assertThat(editSavedFormButton.visibility, equalTo(View.GONE))
        }
    }

    @Test
    fun `When viewSentFormButton is enabled in settings, should be visible`() {
        whenever(mainMenuViewModel.shouldViewSentFormButtonBeVisible()).thenReturn(true)

        val scenario = launcherRule.launch(MainMenuActivity::class.java)
        scenario.onActivity { activity: MainMenuActivity ->
            val editSavedFormButton = activity.findViewById<MainMenuButton>(R.id.view_sent_forms)
            assertThat(editSavedFormButton.visibility, equalTo(View.VISIBLE))
        }
    }

    @Test
    fun `When viewSentFormButton is disabled in settings, should be gone`() {
        whenever(mainMenuViewModel.shouldViewSentFormButtonBeVisible()).thenReturn(false)

        val scenario = launcherRule.launch(MainMenuActivity::class.java)
        scenario.onActivity { activity: MainMenuActivity ->
            val editSavedFormButton = activity.findViewById<MainMenuButton>(R.id.view_sent_forms)
            assertThat(editSavedFormButton.visibility, equalTo(View.GONE))
        }
    }

    @Test
    fun `When getBlankFormButton is enabled in settings, should be visible`() {
        whenever(mainMenuViewModel.shouldGetBlankFormButtonBeVisible()).thenReturn(true)

        val scenario = launcherRule.launch(MainMenuActivity::class.java)
        scenario.onActivity { activity: MainMenuActivity ->
            val editSavedFormButton = activity.findViewById<MainMenuButton>(R.id.get_forms)
            assertThat(editSavedFormButton.visibility, equalTo(View.VISIBLE))
        }
    }

    @Test
    fun `When getBlankFormButton is disabled in settings, should be gone`() {
        whenever(mainMenuViewModel.shouldGetBlankFormButtonBeVisible()).thenReturn(false)

        val scenario = launcherRule.launch(MainMenuActivity::class.java)
        scenario.onActivity { activity: MainMenuActivity ->
            val editSavedFormButton = activity.findViewById<MainMenuButton>(R.id.get_forms)
            assertThat(editSavedFormButton.visibility, equalTo(View.GONE))
        }
    }

    @Test
    fun `When deleteSavedFormButton is enabled in settings, should be visible`() {
        whenever(mainMenuViewModel.shouldDeleteSavedFormButtonBeVisible()).thenReturn(true)

        val scenario = launcherRule.launch(MainMenuActivity::class.java)
        scenario.onActivity { activity: MainMenuActivity ->
            val editSavedFormButton = activity.findViewById<MainMenuButton>(R.id.manage_forms)
            assertThat(editSavedFormButton.visibility, equalTo(View.VISIBLE))
        }
    }

    @Test
    fun `When deleteSavedFormButton is disabled in settings, should be gone`() {
        whenever(mainMenuViewModel.shouldDeleteSavedFormButtonBeVisible()).thenReturn(false)

        val scenario = launcherRule.launch(MainMenuActivity::class.java)
        scenario.onActivity { activity: MainMenuActivity ->
            val editSavedFormButton = activity.findViewById<MainMenuButton>(R.id.manage_forms)
            assertThat(editSavedFormButton.visibility, equalTo(View.GONE))
        }
    }
}
