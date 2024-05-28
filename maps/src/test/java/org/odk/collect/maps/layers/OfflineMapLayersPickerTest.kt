package org.odk.collect.maps.layers

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.odk.collect.androidshared.ui.FragmentFactoryBuilder
import org.odk.collect.androidtest.DrawableMatcher.withImageDrawable
import org.odk.collect.fragmentstest.FragmentScenarioLauncherRule
import org.odk.collect.maps.R
import org.odk.collect.settings.InMemSettingsProvider
import org.odk.collect.settings.keys.ProjectKeys
import org.odk.collect.shared.TempFiles
import org.odk.collect.strings.R.string
import org.odk.collect.strings.localization.getLocalizedString
import org.odk.collect.testshared.FakeScheduler
import org.odk.collect.testshared.RecyclerViewMatcher
import org.odk.collect.testshared.RecyclerViewMatcher.Companion.withRecyclerView
import org.odk.collect.webpage.ExternalWebPageHelper

@RunWith(AndroidJUnit4::class)
class OfflineMapLayersPickerTest {
    private val referenceLayerRepository = mock<ReferenceLayerRepository>().also {
        whenever(it.getAllSupported()).thenReturn(emptyList())
    }
    private val scheduler = FakeScheduler()
    private val settingsProvider = InMemSettingsProvider()
    private val externalWebPageHelper = mock<ExternalWebPageHelper>()

    @get:Rule
    val fragmentScenarioLauncherRule = FragmentScenarioLauncherRule(
        FragmentFactoryBuilder()
            .forClass(OfflineMapLayersPicker::class) {
                OfflineMapLayersPicker(referenceLayerRepository, scheduler, settingsProvider, externalWebPageHelper)
            }.build()
    )

    @Test
    fun `clicking the 'cancel' button dismisses the layers picker`() {
        val scenario = launchFragment()

        scheduler.flush()

        scenario.onFragment {
            assertThat(it.isVisible, equalTo(true))
            onView(withText(string.cancel)).perform(click())
            assertThat(it.isVisible, equalTo(false))
        }
    }

    @Test
    fun `clicking the 'cancel' button does not save the layer`() {
        whenever(referenceLayerRepository.getAllSupported()).thenReturn(listOf(
            ReferenceLayer("1", TempFiles.createTempFile(), "layer1")
        ))

        launchFragment()

        scheduler.flush()

        onView(withText("layer1")).perform(scrollTo(), click())
        onView(withText(string.cancel)).perform(click())
        assertThat(settingsProvider.getUnprotectedSettings().contains(ProjectKeys.KEY_REFERENCE_LAYER), equalTo(false))
    }

    @Test
    fun `clicking the 'save' button dismisses the layers picker`() {
        val scenario = launchFragment()

        scheduler.flush()

        scenario.onFragment {
            assertThat(it.isVisible, equalTo(true))
            onView(withText(string.save)).perform(click())
            assertThat(it.isVisible, equalTo(false))
        }
    }

    @Test
    fun `clicking the 'save' button saves null when 'None' option is checked`() {
        whenever(referenceLayerRepository.getAllSupported()).thenReturn(listOf(
            ReferenceLayer("1", TempFiles.createTempFile(), "layer1")
        ))

        launchFragment()

        scheduler.flush()

        onView(withText(string.save)).perform(click())
        assertThat(settingsProvider.getUnprotectedSettings().getString(ProjectKeys.KEY_REFERENCE_LAYER), equalTo(null))
    }

    @Test
    fun `clicking the 'save' button saves the layer id if any is checked`() {
        whenever(referenceLayerRepository.getAllSupported()).thenReturn(listOf(
            ReferenceLayer("1", TempFiles.createTempFile(), "layer1")
        ))

        launchFragment()

        scheduler.flush()

        onView(withText("layer1")).perform(scrollTo(), click())
        onView(withText(string.save)).perform(click())
        assertThat(settingsProvider.getUnprotectedSettings().getString(ProjectKeys.KEY_REFERENCE_LAYER), equalTo("1"))
    }

    @Test
    fun `when no layer id is saved in settings the 'None' option should be checked`() {
        whenever(referenceLayerRepository.getAllSupported()).thenReturn(listOf(
            ReferenceLayer("1", TempFiles.createTempFile(), "layer1")
        ))

        launchFragment()

        scheduler.flush()

        onView(withRecyclerView(R.id.layers).atPositionOnView(0, R.id.radio_button)).check(matches(isChecked()))
        onView(withRecyclerView(R.id.layers).atPositionOnView(1, R.id.radio_button)).check(matches(not(isChecked())))
    }

    @Test
    fun `when layer id is saved in settings the layer it belongs to should be checked`() {
        whenever(referenceLayerRepository.getAllSupported()).thenReturn(listOf(
            ReferenceLayer("1", TempFiles.createTempFile(), "layer1"),
            ReferenceLayer("2", TempFiles.createTempFile(), "layer2")
        ))
        settingsProvider.getUnprotectedSettings().save(ProjectKeys.KEY_REFERENCE_LAYER, "2")

        launchFragment()

        scheduler.flush()

        onView(withRecyclerView(R.id.layers).atPositionOnView(0, R.id.radio_button)).check(matches(not(isChecked())))
        onView(withRecyclerView(R.id.layers).atPositionOnView(1, R.id.radio_button)).check(matches(not(isChecked())))
        onView(withRecyclerView(R.id.layers).atPositionOnView(2, R.id.radio_button)).check(matches(isChecked()))
    }

    @Test
    fun `progress indicator is displayed during loading layers`() {
        launchFragment()

        onView(withId(R.id.progress_indicator)).check(matches(isDisplayed()))
        onView(withId(R.id.layers)).check(matches(not(isDisplayed())))

        scheduler.flush()

        onView(withId(R.id.progress_indicator)).check(matches(not(isDisplayed())))
        onView(withId(R.id.layers)).check(matches(isDisplayed()))
    }

    @Test
    fun `the 'cancel' button should be enabled during loading layers`() {
        launchFragment()

        onView(withText(string.cancel)).check(matches(isEnabled()))
    }

    @Test
    fun `the 'save' button should be disabled during loading layers`() {
        launchFragment()

        onView(withText(string.save)).check(matches(not(isEnabled())))
        scheduler.flush()
        onView(withText(string.save)).check(matches(isEnabled()))
    }

    @Test
    fun `the 'add layer' button should be enabled during loading layers`() {
        launchFragment()

        onView(withText(string.add_layer)).check(matches(isEnabled()))
    }

    @Test
    fun `the 'learn more' button should be enabled during loading layers`() {
        launchFragment()

        onView(withText(string.get_help_with_reference_layers)).check(matches(isEnabled()))
    }

    @Test
    fun `clicking the 'learn more' button opens the forum thread`() {
        launchFragment()

        scheduler.flush()

        onView(withText(string.get_help_with_reference_layers)).perform(click())

        verify(externalWebPageHelper).openWebPageInCustomTab(any(), eq(Uri.parse("https://docs.getodk.org/collect-offline-maps/#transferring-offline-tilesets-to-devices")))
    }

    @Test
    fun `if there are no layers the 'none' option is displayed`() {
        launchFragment()

        scheduler.flush()

        onView(withId(R.id.layers)).check(matches(RecyclerViewMatcher.withListSize(1)))
        onView(withRecyclerView(R.id.layers).atPositionOnView(0, R.id.title)).check(matches(withText(string.none)))
    }

    @Test
    fun `if there are multiple layers all of them are displayed along with the 'None'`() {
        whenever(referenceLayerRepository.getAllSupported()).thenReturn(listOf(
            ReferenceLayer("1", TempFiles.createTempFile(), "layer1"),
            ReferenceLayer("2", TempFiles.createTempFile(), "layer2")
        ))

        launchFragment()

        scheduler.flush()

        onView(withId(R.id.layers)).check(matches(RecyclerViewMatcher.withListSize(3)))
        onView(withRecyclerView(R.id.layers).atPositionOnView(0, R.id.title)).check(matches(withText(string.none)))
        onView(withRecyclerView(R.id.layers).atPositionOnView(1, R.id.title)).check(matches(withText("layer1")))
        onView(withRecyclerView(R.id.layers).atPositionOnView(2, R.id.title)).check(matches(withText("layer2")))
    }

    @Test
    fun `checking layers sets selection correctly`() {
        whenever(referenceLayerRepository.getAllSupported()).thenReturn(listOf(
            ReferenceLayer("1", TempFiles.createTempFile(), "layer1")
        ))

        launchFragment()

        scheduler.flush()

        onView(withText("layer1")).perform(scrollTo(), click())
        onView(withRecyclerView(R.id.layers).atPositionOnView(0, R.id.radio_button)).check(matches(not(isChecked())))
        onView(withRecyclerView(R.id.layers).atPositionOnView(1, R.id.radio_button)).check(matches(isChecked()))

        onView(withText(string.none)).perform(scrollTo(), click())
        onView(withRecyclerView(R.id.layers).atPositionOnView(0, R.id.radio_button)).check(matches(isChecked()))
        onView(withRecyclerView(R.id.layers).atPositionOnView(1, R.id.radio_button)).check(matches(not(isChecked())))
    }

    @Test
    fun `recreating maintains selection`() {
        whenever(referenceLayerRepository.getAllSupported()).thenReturn(listOf(
            ReferenceLayer("1", TempFiles.createTempFile(), "layer1")
        ))

        val scenario = launchFragment()

        scheduler.flush()

        onView(withText("layer1")).perform(scrollTo(), click())
        scenario.recreate()
        onView(withRecyclerView(R.id.layers).atPositionOnView(0, R.id.radio_button)).check(matches(not(isChecked())))
        onView(withRecyclerView(R.id.layers).atPositionOnView(1, R.id.radio_button)).check(matches(isChecked()))
    }

    @Test
    fun `the 'None' option is not expandable`() {
        launchFragment()

        scheduler.flush()

        onView(withRecyclerView(R.id.layers).atPositionOnView(0, R.id.arrow)).check(matches(not(isDisplayed())))
    }

    @Test
    fun `layers are collapsed by default`() {
        whenever(referenceLayerRepository.getAllSupported()).thenReturn(listOf(
            ReferenceLayer("1", TempFiles.createTempFile(), "layer1"),
            ReferenceLayer("2", TempFiles.createTempFile(), "layer2")
        ))

        launchFragment()

        scheduler.flush()

        assertLayerCollapsed(1)
        assertLayerCollapsed(2)
    }

    @Test
    fun `recreating maintains expanded layers`() {
        whenever(referenceLayerRepository.getAllSupported()).thenReturn(listOf(
            ReferenceLayer("1", TempFiles.createTempFile(), "layer1"),
            ReferenceLayer("2", TempFiles.createTempFile(), "layer2"),
            ReferenceLayer("3", TempFiles.createTempFile(), "layer3")
        ))

        val scenario = launchFragment()

        scheduler.flush()

        onView(withRecyclerView(R.id.layers).atPositionOnView(1, R.id.arrow)).perform(click())
        onView(withId(R.id.layers)).perform(scrollToPosition<RecyclerView.ViewHolder>(3))
        onView(withRecyclerView(R.id.layers).atPositionOnView(3, R.id.arrow)).perform(click())

        scenario.recreate()

        assertLayerExpanded(1)
        assertLayerCollapsed(2)
        assertLayerExpanded(3)
    }

    @Test
    fun `correct path is displayed after expanding layers`() {
        val file1 = TempFiles.createTempFile()
        val file2 = TempFiles.createTempFile()
        whenever(referenceLayerRepository.getAllSupported()).thenReturn(listOf(
            ReferenceLayer("1", file1, "layer1"),
            ReferenceLayer("2", file2, "layer2")
        ))

        launchFragment()

        scheduler.flush()

        onView(withRecyclerView(R.id.layers).atPositionOnView(1, R.id.arrow)).perform(click())
        onView(withId(R.id.layers)).perform(scrollToPosition<RecyclerView.ViewHolder>(2))
        onView(withRecyclerView(R.id.layers).atPositionOnView(2, R.id.arrow)).perform(click())

        onView(withRecyclerView(R.id.layers).atPositionOnView(1, R.id.path)).check(matches(withText(file1.absolutePath)))
        onView(withRecyclerView(R.id.layers).atPositionOnView(2, R.id.path)).check(matches(withText(file2.absolutePath)))
    }

    @Test
    fun `clicking delete shows a confirmation dialog`() {
        whenever(referenceLayerRepository.getAllSupported()).thenReturn(listOf(
            ReferenceLayer("1", TempFiles.createTempFile(), "layer1")
        ))

        launchFragment()

        scheduler.flush()

        onView(withRecyclerView(R.id.layers).atPositionOnView(1, R.id.arrow)).perform(click())
        onView(withRecyclerView(R.id.layers).atPositionOnView(1, R.id.delete_layer)).perform(scrollTo(), click())

        onView(withText(ApplicationProvider.getApplicationContext<Application>().getLocalizedString(string.delete_layer_confirmation_message, "layer1"))).inRoot(isDialog()).check(matches(isDisplayed()))
        onView(withText(string.cancel)).inRoot(isDialog()).check(matches(isDisplayed()))
        onView(withText(string.delete_layer)).inRoot(isDialog()).check(matches(isDisplayed()))
    }

    @Test
    fun `clicking delete and canceling does not remove the layer`() {
        val layerFile1 = TempFiles.createTempFile()
        whenever(referenceLayerRepository.getAllSupported()).thenReturn(listOf(
            ReferenceLayer("1", layerFile1, "layer1")
        ))

        launchFragment()

        scheduler.flush()

        onView(withRecyclerView(R.id.layers).atPositionOnView(1, R.id.arrow)).perform(click())
        onView(withRecyclerView(R.id.layers).atPositionOnView(1, R.id.delete_layer)).perform(scrollTo(), click())

        onView(withText(string.cancel)).inRoot(isDialog()).perform(click())

        onView(withId(R.id.layers)).check(matches(RecyclerViewMatcher.withListSize(2)))
        onView(withId(R.id.layers)).perform(scrollToPosition<RecyclerView.ViewHolder>(0))
        onView(withRecyclerView(R.id.layers).atPositionOnView(0, R.id.title)).check(matches(withText(string.none)))
        onView(withRecyclerView(R.id.layers).atPositionOnView(1, R.id.title)).check(matches(withText("layer1")))
        assertThat(layerFile1.exists(), equalTo(true))
    }

    @Test
    fun `clicking delete and confirming removes the layer`() {
        val layerFile1 = TempFiles.createTempFile()
        val layerFile2 = TempFiles.createTempFile()
        whenever(referenceLayerRepository.getAllSupported()).thenReturn(listOf(
            ReferenceLayer("1", layerFile1, "layer1"),
            ReferenceLayer("2", layerFile2, "layer2")
        ))

        launchFragment()

        scheduler.flush()

        onView(withRecyclerView(R.id.layers).atPositionOnView(1, R.id.arrow)).perform(click())
        onView(withRecyclerView(R.id.layers).atPositionOnView(1, R.id.delete_layer)).perform(scrollTo(), click())

        onView(withText(string.delete_layer)).inRoot(isDialog()).perform(click())
        scheduler.flush()

        onView(withId(R.id.layers)).check(matches(RecyclerViewMatcher.withListSize(2)))
        onView(withRecyclerView(R.id.layers).atPositionOnView(0, R.id.title)).check(matches(withText(string.none)))
        onView(withRecyclerView(R.id.layers).atPositionOnView(1, R.id.title)).check(matches(withText("layer2")))
        assertThat(layerFile1.exists(), equalTo(false))
        assertThat(layerFile2.exists(), equalTo(true))
    }

    @Test
    fun `clicking the 'add layer' button opens file picker that allows selecting multiple files`() {
        Intents.init()
        launchFragment()

        onView(withText(string.add_layer)).perform(click())

        Intents.getIntents()[0].apply {
            assertThat(this, IntentMatchers.hasAction(Intent.ACTION_GET_CONTENT))
            assertThat(categories.containsAll(listOf(Intent.CATEGORY_OPENABLE)), equalTo(true))
            assertThat(this, IntentMatchers.hasType("*/*"))
            assertThat(this, IntentMatchers.hasExtra(Intent.EXTRA_ALLOW_MULTIPLE, true))
        }

        Intents.release()
    }

    @Test
    fun `progress indicator is displayed during loading layers after receiving new ones`() {
        whenever(referenceLayerRepository.getAllSupported()).thenReturn(listOf(
            ReferenceLayer("1", TempFiles.createTempFile(), "layer1")
        ))
        val scenario = launchFragment()

        scheduler.flush()

        scenario.onFragment {
            it.childFragmentManager.setFragmentResult(OfflineMapLayersImportDialog.RESULT_KEY, bundleOf())
        }

        onView(withId(R.id.progress_indicator)).check(matches(isDisplayed()))
        onView(withId(R.id.layers)).check(matches(not(isDisplayed())))

        scheduler.flush()
        onView(withId(R.id.progress_indicator)).check(matches(not(isDisplayed())))
        onView(withId(R.id.layers)).check(matches(isDisplayed()))
    }

    @Test
    fun `when new layers added the list should be updated`() {
        whenever(referenceLayerRepository.getAllSupported()).thenReturn(listOf(
            ReferenceLayer("1", TempFiles.createTempFile(), "layer1")
        ))

        val scenario = launchFragment()

        scheduler.flush()

        whenever(referenceLayerRepository.getAllSupported()).thenReturn(listOf(
            ReferenceLayer("1", TempFiles.createTempFile(), "layer1"),
            ReferenceLayer("2", TempFiles.createTempFile(), "layer2")
        ))

        scenario.onFragment {
            it.childFragmentManager.setFragmentResult(OfflineMapLayersImportDialog.RESULT_KEY, bundleOf())
        }

        scheduler.flush()

        onView(withId(R.id.layers)).check(matches(RecyclerViewMatcher.withListSize(3)))
        onView(withRecyclerView(R.id.layers).atPositionOnView(0, R.id.title)).check(matches(withText(string.none)))
        onView(withRecyclerView(R.id.layers).atPositionOnView(1, R.id.title)).check(matches(withText("layer1")))
        onView(withRecyclerView(R.id.layers).atPositionOnView(2, R.id.title)).check(matches(withText("layer2")))
    }

    private fun assertLayerCollapsed(position: Int) {
        onView(withRecyclerView(R.id.layers).atPositionOnView(position, R.id.arrow)).check(matches(withImageDrawable(org.odk.collect.icons.R.drawable.ic_baseline_expand_more_24)))
        onView(withRecyclerView(R.id.layers).atPositionOnView(position, R.id.path)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
        onView(withRecyclerView(R.id.layers).atPositionOnView(position, R.id.delete_layer)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
    }

    private fun assertLayerExpanded(position: Int) {
        onView(withRecyclerView(R.id.layers).atPositionOnView(position, R.id.arrow)).check(matches(withImageDrawable(org.odk.collect.icons.R.drawable.ic_baseline_expand_less_24)))
        onView(withRecyclerView(R.id.layers).atPositionOnView(position, R.id.path)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
        onView(withRecyclerView(R.id.layers).atPositionOnView(position, R.id.delete_layer)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
    }

    private fun launchFragment(): FragmentScenario<OfflineMapLayersPicker> {
        return fragmentScenarioLauncherRule.launchInContainer(OfflineMapLayersPicker::class.java)
    }
}
