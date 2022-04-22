package org.odk.collect.android.support.rules

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import org.odk.collect.android.activities.FirstLaunchActivity
import org.odk.collect.android.external.AndroidShortcutsActivity
import org.odk.collect.android.support.pages.FirstLaunchPage
import org.odk.collect.android.support.pages.MainMenuPage
import org.odk.collect.android.support.pages.Page
import org.odk.collect.android.support.pages.ShortcutsPage
import org.odk.collect.androidtest.ActivityScenarioLauncherRule
import java.util.function.Consumer

class CollectTestRule @JvmOverloads constructor(
    private val useDemoProject: Boolean = true
) : ActivityScenarioLauncherRule() {

    override fun before() {
        super.before()

        val firstLaunchPage = launch(
            Intent(
                ApplicationProvider.getApplicationContext(),
                FirstLaunchActivity::class.java
            ),
            FirstLaunchPage()
        ).assertOnPage()

        if (useDemoProject) {
            firstLaunchPage.clickTryCollect()
        }
    }

    fun startAtMainMenu() = MainMenuPage()

    fun startAtFirstLaunch() = FirstLaunchPage()

    fun withProject(serverUrl: String): MainMenuPage =
        startAtFirstLaunch()
            .clickManuallyEnterProjectDetails()
            .inputUrl(serverUrl)
            .addProject()

    fun launchShortcuts(): ShortcutsPage {
        val scenario = launch(AndroidShortcutsActivity::class.java)
        return ShortcutsPage(scenario).assertOnPage()
    }

    fun <T : Page<T>> launch(intent: Intent, destination: T): T {
        launch<Activity>(intent)
        return destination.assertOnPage()
    }

    fun <T : Page<T>> launchForResult(
        intent: Intent,
        destination: T,
        actions: Consumer<T>
    ): Instrumentation.ActivityResult {
        val scenario = launch<Activity>(intent)
        destination.assertOnPage()
        actions.accept(destination)
        return scenario.result
    }
}
