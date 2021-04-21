package org.odk.collect.android.activities

import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy
import org.odk.collect.android.R
import org.odk.collect.android.activities.viewmodels.SplashScreenViewModel
import org.odk.collect.android.fragments.dialogs.FirstLaunchDialog
import org.odk.collect.android.injection.config.AppDependencyModule
import org.odk.collect.android.preferences.source.Settings
import org.odk.collect.android.preferences.source.SettingsProvider
import org.odk.collect.android.projects.ProjectImporter
import org.odk.collect.android.projects.ProjectsRepository
import org.odk.collect.android.support.RobolectricHelpers

@RunWith(AndroidJUnit4::class)
class SplashScreenActivityTest {
    private val testDispatcher = TestCoroutineDispatcher()

    private lateinit var splashScreenViewModel: SplashScreenViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        splashScreenViewModel = spy(SplashScreenViewModel(mock(Settings::class.java), mock(Settings::class.java), mock(ProjectsRepository::class.java), mock(ProjectImporter::class.java)))

        RobolectricHelpers.overrideAppDependencyModule(object : AppDependencyModule() {
            override fun providesSplashScreenViewModel(settingsProvider: SettingsProvider, projectsRepository: ProjectsRepository, projectImporter: ProjectImporter): SplashScreenViewModel.Factory {
                return object : SplashScreenViewModel.Factory(settingsProvider.getGeneralSettings(), settingsProvider.getMetaSettings(), projectsRepository, projectImporter) {
                    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                        return splashScreenViewModel as T
                    }
                }
            }
        })
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `The Fist Launch Screen should be displayed if the app is newly installed`() {
        doReturn(true).`when`(splashScreenViewModel).isFirstLaunch()

        val scenario = ActivityScenario.launch(SplashScreenActivity::class.java)
        scenario.onActivity { activity: SplashScreenActivity ->
            assertThat(RobolectricHelpers.getFragmentByClass(activity.supportFragmentManager, FirstLaunchDialog::class.java), `is`(notNullValue()))
        }
    }

    @Test
    fun `The Fist Launch Screen should not be displayed if the app is not newly installed`() {
        doReturn(false).`when`(splashScreenViewModel).isFirstLaunch()
        doReturn(true).`when`(splashScreenViewModel).shouldDisplaySplashScreen
        doReturn(false).`when`(splashScreenViewModel).doesLogoFileExist

        val scenario = ActivityScenario.launch(SplashScreenActivity::class.java)
        scenario.onActivity { activity: SplashScreenActivity ->
            assertThat(RobolectricHelpers.getFragmentByClass(activity.supportFragmentManager, FirstLaunchDialog::class.java), `is`(nullValue()))
        }
    }

    @Test
    fun `The Splash screen should be displayed with the default logo if it's enabled and no other logo is set`() {
        doReturn(false).`when`(splashScreenViewModel).isFirstLaunch()
        doReturn(true).`when`(splashScreenViewModel).shouldDisplaySplashScreen
        doReturn(false).`when`(splashScreenViewModel).doesLogoFileExist

        val scenario = ActivityScenario.launch(SplashScreenActivity::class.java)
        scenario.onActivity { activity: SplashScreenActivity ->
            assertThat(activity.findViewById<View>(R.id.splash_default).visibility, `is`(View.VISIBLE))
            assertThat(activity.findViewById<View>(R.id.splash).visibility, `is`(View.GONE))
        }
    }

    @Test
    fun `The Splash screen should be displayed with custom logo if it's enabled and custom logo is set`() {
        doReturn(false).`when`(splashScreenViewModel).isFirstLaunch()
        doReturn(true).`when`(splashScreenViewModel).shouldDisplaySplashScreen
        doReturn(true).`when`(splashScreenViewModel).doesLogoFileExist
        doReturn(null).`when`(splashScreenViewModel).scaledSplashScreenLogoBitmap
        doReturn(null).`when`(splashScreenViewModel).splashScreenLogoFile

        val scenario = ActivityScenario.launch(SplashScreenActivity::class.java)
        scenario.onActivity { activity: SplashScreenActivity ->
            assertThat(activity.findViewById<View>(R.id.splash_default).visibility, `is`(View.GONE))
            assertThat(activity.findViewById<View>(R.id.splash).visibility, `is`(View.VISIBLE))
        }
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `The main menu should be displayed automatically after 2s if the Splash screen is enabled and the app is not newly installed`() = testDispatcher.runBlockingTest {
        doReturn(false).`when`(splashScreenViewModel).isFirstLaunch()
        doReturn(true).`when`(splashScreenViewModel).shouldDisplaySplashScreen
        doReturn(false).`when`(splashScreenViewModel).doesLogoFileExist

        Intents.init()

        val scenario = ActivityScenario.launch(SplashScreenActivity::class.java)
        advanceTimeBy(1000)
        assertThat(scenario.state, `is`(Lifecycle.State.RESUMED))
        assertThat(Intents.getIntents().isEmpty(), `is`(true))
        advanceTimeBy(1000)
        Intents.intended(hasComponent(MainMenuActivity::class.java.name))

        Intents.release()
    }

    @Test
    fun `The main menu should be displayed immediately if the splash screen is disabled and the app is not newly installed`() {
        doReturn(false).`when`(splashScreenViewModel).isFirstLaunch()
        doReturn(false).`when`(splashScreenViewModel).shouldDisplaySplashScreen

        Intents.init()
        val scenario = ActivityScenario.launch(SplashScreenActivity::class.java)
        assertThat(scenario.state, `is`(Lifecycle.State.DESTROYED))
        Intents.intended(hasComponent(MainMenuActivity::class.java.name))
        Intents.release()
    }
}
