package org.odk.collect.maps.layers

import android.os.Bundle
import androidx.core.net.toUri
import androidx.fragment.app.testing.FragmentScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.not
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.androidshared.ui.FragmentFactoryBuilder
import org.odk.collect.fragmentstest.FragmentScenarioLauncherRule
import org.odk.collect.shared.TempFiles
import org.odk.collect.strings.R
import org.odk.collect.testshared.FakeScheduler
import org.odk.collect.testshared.RobolectricHelpers
import java.io.File

@RunWith(AndroidJUnit4::class)
class OfflineMapLayersImportDialogTest {
    private val scheduler = FakeScheduler()
    private val sharedLayersDirPath = TempFiles.createTempDir().absolutePath
    private val projectLayersDirPath = TempFiles.createTempDir().absolutePath

    @get:Rule
    val fragmentScenarioLauncherRule = FragmentScenarioLauncherRule(
        FragmentFactoryBuilder()
            .forClass(OfflineMapLayersImportDialog::class) {
                OfflineMapLayersImportDialog(scheduler, sharedLayersDirPath, projectLayersDirPath)
            }.build()
    )

    @Test
    fun `clicking the 'cancel' button dismisses the dialog`() {
        launchFragment(arrayListOf()).onFragment {
            assertThat(it.isVisible, equalTo(true))
            onView(withText(R.string.cancel)).perform(click())
            assertThat(it.isVisible, equalTo(false))
        }
    }

    @Test
    fun `clicking the 'cancel' button does not set fragment result`() {
        launchFragment(arrayListOf()).onFragment {
            var resultReceived = false
            it.parentFragmentManager.setFragmentResultListener(
                OfflineMapLayersImportDialog.RESULT_KEY,
                it
            ) { _, _ ->
                resultReceived = true
            }

            onView(withContentDescription(R.string.cancel)).perform(click())
            assertThat(resultReceived, equalTo(false))
        }
    }

    @Test
    fun `clicking the 'add layer' button dismisses the dialog`() {
        launchFragment(arrayListOf()).onFragment {
            scheduler.flush()
            assertThat(it.isVisible, equalTo(true))
            onView(withContentDescription(R.string.add_layer)).perform(click())
            scheduler.flush()
            RobolectricHelpers.runLooper()
            assertThat(it.isVisible, equalTo(false))
        }
    }

    @Test
    fun `clicking the 'add layer' button sets fragment result`() {
        launchFragment(arrayListOf()).onFragment {
            scheduler.flush()
            var resultReceived = false
            it.parentFragmentManager.setFragmentResultListener(
                OfflineMapLayersImportDialog.RESULT_KEY,
                it
            ) { _, _ ->
                resultReceived = true
            }

            onView(withContentDescription(R.string.add_layer)).perform(click())
            scheduler.flush()
            RobolectricHelpers.runLooper()
            assertThat(resultReceived, equalTo(true))
        }
    }

    @Test
    fun `'All projects' location should be selected by default`() {
        launchFragment(arrayListOf())

        onView(withId(org.odk.collect.maps.R.id.all_projects_option)).check(matches(isChecked()))
        onView(withId(org.odk.collect.maps.R.id.current_project_option)).check(matches(not(isChecked())))
    }

    @Test
    fun `recreating maintains the selected layers location`() {
        val file1 = TempFiles.createTempFile("layer1", ".mbtiles")
        val file2 = TempFiles.createTempFile("layer2", ".mbtiles")

        val scenario = launchFragment(arrayListOf(file1.toUri().toString(), file2.toUri().toString()))
        onView(withId(org.odk.collect.maps.R.id.current_project_option)).perform(click())

        scenario.recreate()

        onView(withId(org.odk.collect.maps.R.id.all_projects_option)).check(matches(not(isChecked())))
        onView(withId(org.odk.collect.maps.R.id.current_project_option)).check(matches(isChecked()))
    }

    @Test
    fun `the list of selected layers should be displayed`() {
        val file1 = TempFiles.createTempFile("layer1", ".mbtiles")
        val file2 = TempFiles.createTempFile("layer2", ".mbtiles")

        launchFragment(arrayListOf(file1.toUri().toString(), file2.toUri().toString()))
        scheduler.flush()
        onView(withText("layer1.mbtiles")).check(matches(isDisplayed()))
        onView(withText("layer2.mbtiles")).check(matches(isDisplayed()))
    }

    @Test
    fun `recreating maintains the list of selected layers`() {
        val file1 = TempFiles.createTempFile("layer1", ".mbtiles")
        val file2 = TempFiles.createTempFile("layer2", ".mbtiles")

        val scenario = launchFragment(arrayListOf(file1.toUri().toString(), file2.toUri().toString()))
        scheduler.flush()

        scenario.recreate()

        onView(withText("layer1.mbtiles")).check(matches(isDisplayed()))
        onView(withText("layer2.mbtiles")).check(matches(isDisplayed()))
    }

    @Test
    fun `only mbtiles files are taken into account`() {
        val file1 = TempFiles.createTempFile("layer1", ".mbtiles")
        val file2 = TempFiles.createTempFile("layer2", ".txt")

        val scenario = launchFragment(arrayListOf(file1.toUri().toString(), file2.toUri().toString()))
        scheduler.flush()

        scenario.recreate()

        onView(withText("layer1.mbtiles")).check(matches(isDisplayed()))
        onView(withText("layer2.txt")).check(doesNotExist())
    }

    @Test
    fun `clicking the 'add layer' button moves the files to the shared layers dir if it is selected`() {
        val file1 = TempFiles.createTempFile("layer1", ".mbtiles").also {
            it.writeText("blah1")
        }
        val file2 = TempFiles.createTempFile("layer2", ".mbtiles").also {
            it.writeText("blah2")
        }

        launchFragment(arrayListOf(file1.toUri().toString(), file2.toUri().toString()))
        scheduler.flush()

        onView(withContentDescription(R.string.add_layer)).perform(click())
        scheduler.flush()

        assertThat(File(sharedLayersDirPath).listFiles().size, equalTo(2))
        assertThat(File(projectLayersDirPath).listFiles().size, equalTo(0))

        val copiedFile1 = File(sharedLayersDirPath, "layer1.mbtiles")
        assertThat(copiedFile1.exists(), equalTo(true))
        assertThat(copiedFile1.readText(), equalTo("blah1"))

        val copiedFile2 = File(sharedLayersDirPath, "layer2.mbtiles")
        assertThat(copiedFile2.exists(), equalTo(true))
        assertThat(copiedFile2.readText(), equalTo("blah2"))
    }

    @Test
    fun `clicking the 'add layer' button moves the files to the project layers dir if it is selected`() {
        val file1 = TempFiles.createTempFile("layer1", ".mbtiles").also {
            it.writeText("blah1")
        }
        val file2 = TempFiles.createTempFile("layer2", ".mbtiles").also {
            it.writeText("blah2")
        }

        launchFragment(arrayListOf(file1.toUri().toString(), file2.toUri().toString()))
        scheduler.flush()

        onView(withId(org.odk.collect.maps.R.id.current_project_option)).perform(scrollTo(), click())

        onView(withContentDescription(R.string.add_layer)).perform(click())
        scheduler.flush()

        assertThat(File(sharedLayersDirPath).listFiles().size, equalTo(0))
        assertThat(File(projectLayersDirPath).listFiles().size, equalTo(2))

        val copiedFile1 = File(projectLayersDirPath, "layer1.mbtiles")
        assertThat(copiedFile1.exists(), equalTo(true))
        assertThat(copiedFile1.readText(), equalTo("blah1"))

        val copiedFile2 = File(projectLayersDirPath, "layer2.mbtiles")
        assertThat(copiedFile2.exists(), equalTo(true))
        assertThat(copiedFile2.readText(), equalTo("blah2"))
    }

    private fun launchFragment(uris: ArrayList<String>): FragmentScenario<OfflineMapLayersImportDialog> {
        return fragmentScenarioLauncherRule.launchInContainer(
            OfflineMapLayersImportDialog::class.java,
            Bundle().apply { putStringArrayList(OfflineMapLayersImportDialog.URIS, uris) }
        )
    }
}
