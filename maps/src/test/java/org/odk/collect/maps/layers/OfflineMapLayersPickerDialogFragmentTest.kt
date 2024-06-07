package org.odk.collect.maps.layers

import android.net.Uri
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityOptionsCompat
import androidx.core.net.toUri
import androidx.fragment.app.testing.FragmentScenario
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
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
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.CoreMatchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.odk.collect.androidshared.ui.FragmentFactoryBuilder
import org.odk.collect.androidtest.DrawableMatcher.withImageDrawable
import org.odk.collect.fragmentstest.FragmentScenarioLauncherRule
import org.odk.collect.maps.R
import org.odk.collect.settings.InMemSettingsProvider
import org.odk.collect.settings.keys.ProjectKeys
import org.odk.collect.shared.TempFiles
import org.odk.collect.strings.R.string
import org.odk.collect.testshared.EspressoHelpers
import org.odk.collect.testshared.FakeScheduler
import org.odk.collect.testshared.RecyclerViewMatcher
import org.odk.collect.testshared.RecyclerViewMatcher.Companion.withRecyclerView
import org.odk.collect.testshared.RobolectricHelpers
import org.odk.collect.webpage.ExternalWebPageHelper
import java.io.File

@RunWith(AndroidJUnit4::class)
class OfflineMapLayersPickerDialogFragmentTest {
    private val file1 = TempFiles.createTempFile("layer1", MbtilesFile.FILE_EXTENSION)
    private val file2 = TempFiles.createTempFile("layer2", MbtilesFile.FILE_EXTENSION)
    private val file3 = TempFiles.createTempFile("layer3", MbtilesFile.FILE_EXTENSION)

    private val referenceLayerRepository = InMemReferenceLayerRepository()
    private val scheduler = FakeScheduler()
    private val settingsProvider = InMemSettingsProvider()
    private val externalWebPageHelper = mock<ExternalWebPageHelper>()
    private val testRegistry = TestRegistry()

    @get:Rule
    val fragmentScenarioLauncherRule = FragmentScenarioLauncherRule(
        FragmentFactoryBuilder()
            .forClass(OfflineMapLayersPickerDialogFragment::class) {
                OfflineMapLayersPickerDialogFragment(testRegistry, referenceLayerRepository, scheduler, settingsProvider, externalWebPageHelper)
            }.build()
    )

    @Test
    fun `clicking the 'cancel' button dismisses the layers picker`() {
        val scenario = launchFragment()

        scenario.onFragment {
            assertThat(it.isVisible, equalTo(true))
            EspressoHelpers.clickOnText(string.cancel)
            assertThat(it.isVisible, equalTo(false))
        }
    }

    @Test
    fun `clicking the 'cancel' button does not save the layer`() {
        launchFragment()

        scheduler.flush()

        EspressoHelpers.clickOnText(string.cancel)
        assertThat(settingsProvider.getUnprotectedSettings().contains(ProjectKeys.KEY_REFERENCE_LAYER), equalTo(false))
    }

    @Test
    fun `the 'cancel' button should be enabled during loading layers`() {
        launchFragment()

        onView(withText(string.cancel)).check(matches(isEnabled()))
    }

    @Test
    fun `clicking the 'save' button dismisses the layers picker`() {
        val scenario = launchFragment()

        scheduler.flush()

        scenario.onFragment {
            assertThat(it.isVisible, equalTo(true))
            EspressoHelpers.clickOnText(string.save)
            assertThat(it.isVisible, equalTo(false))
        }
    }

    @Test
    fun `the 'save' button should be disabled during loading layers`() {
        launchFragment()

        onView(withText(string.save)).check(matches(not(isEnabled())))
        scheduler.flush()
        onView(withText(string.save)).check(matches(isEnabled()))
    }

    @Test
    fun `clicking the 'save' button saves null when 'None' option is checked`() {
        launchFragment()

        scheduler.flush()

        EspressoHelpers.clickOnText(string.save)
        assertThat(settingsProvider.getUnprotectedSettings().getString(ProjectKeys.KEY_REFERENCE_LAYER), equalTo(null))
    }

    @Test
    fun `clicking the 'save' button saves the layer id if any is checked`() {
        referenceLayerRepository.addLayer(file1, false)

        launchFragment()

        scheduler.flush()

        EspressoHelpers.clickOnText(file1.name)
        EspressoHelpers.clickOnText(string.save)
        assertThat(settingsProvider.getUnprotectedSettings().getString(ProjectKeys.KEY_REFERENCE_LAYER), equalTo(file1.absolutePath))
    }

    @Test
    fun `when no layer id is saved in settings the 'None' option should be checked`() {
        referenceLayerRepository.addLayer(file1, false)

        launchFragment()

        scheduler.flush()

        onView(withRecyclerView(R.id.layers).atPositionOnView(0, R.id.radio_button)).check(matches(isChecked()))
        onView(withRecyclerView(R.id.layers).atPositionOnView(1, R.id.radio_button)).check(matches(not(isChecked())))
    }

    @Test
    fun `when layer id is saved in settings the layer it belongs to should be checked`() {
        referenceLayerRepository.addLayer(file1, false)
        referenceLayerRepository.addLayer(file2, false)

        settingsProvider.getUnprotectedSettings().save(ProjectKeys.KEY_REFERENCE_LAYER, file2.absolutePath)

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
    fun `the 'learn more' button should be enabled during loading layers`() {
        launchFragment()

        onView(withText(string.get_help_with_reference_layers)).check(matches(isEnabled()))
    }

    @Test
    fun `clicking the 'learn more' button opens the forum thread`() {
        launchFragment()

        scheduler.flush()

        EspressoHelpers.clickOnText(string.get_help_with_reference_layers)

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
        referenceLayerRepository.addLayer(file1, false)
        referenceLayerRepository.addLayer(file2, false)

        launchFragment()

        scheduler.flush()

        onView(withId(R.id.layers)).check(matches(RecyclerViewMatcher.withListSize(3)))
        onView(withRecyclerView(R.id.layers).atPositionOnView(0, R.id.title)).check(matches(withText(string.none)))
        onView(withRecyclerView(R.id.layers).atPositionOnView(1, R.id.title)).check(matches(withText(file1.name)))
        onView(withRecyclerView(R.id.layers).atPositionOnView(2, R.id.title)).check(matches(withText(file2.name)))
    }

    @Test
    fun `checking layers sets selection correctly`() {
        referenceLayerRepository.addLayer(file1, false)

        launchFragment()

        scheduler.flush()

        EspressoHelpers.clickOnText(file1.name)
        onView(withRecyclerView(R.id.layers).atPositionOnView(0, R.id.radio_button)).check(matches(not(isChecked())))
        onView(withRecyclerView(R.id.layers).atPositionOnView(1, R.id.radio_button)).check(matches(isChecked()))

        EspressoHelpers.clickOnText(string.none)
        onView(withRecyclerView(R.id.layers).atPositionOnView(0, R.id.radio_button)).check(matches(isChecked()))
        onView(withRecyclerView(R.id.layers).atPositionOnView(1, R.id.radio_button)).check(matches(not(isChecked())))
    }

    @Test
    fun `recreating maintains selection`() {
        referenceLayerRepository.addLayer(file1, false)

        val scenario = launchFragment()

        scheduler.flush()

        EspressoHelpers.clickOnText(file1.name)
        scenario.recreate()
        onView(withRecyclerView(R.id.layers).atPositionOnView(0, R.id.radio_button)).check(matches(not(isChecked())))
        onView(withRecyclerView(R.id.layers).atPositionOnView(1, R.id.radio_button)).check(matches(isChecked()))
    }

    @Test
    fun `clicking the 'add layer' and selecting layers displays the confirmation dialog`() {
        val scenario = launchFragment()

        testRegistry.addUris(Uri.parse("blah"))
        EspressoHelpers.clickOnText(string.add_layer)

        scenario.onFragment {
            assertThat(
                it.childFragmentManager.findFragmentByTag(OfflineMapLayersImporterDialogFragment::class.java.name),
                instanceOf(OfflineMapLayersImporterDialogFragment::class.java)
            )
        }
    }

    @Test
    fun `clicking the 'add layer' and selecting nothing does not display the confirmation dialog`() {
        val scenario = launchFragment()

        EspressoHelpers.clickOnText(string.add_layer)

        scenario.onFragment {
            assertThat(
                it.childFragmentManager.findFragmentByTag(OfflineMapLayersImporterDialogFragment::class.java.name),
                equalTo(null)
            )
        }
    }

    @Test
    fun `progress indicator is displayed during loading layers after receiving new ones`() {
        referenceLayerRepository.addLayer(file1, false)
        referenceLayerRepository.addLayer(file2, false)

        launchFragment()

        scheduler.flush()

        testRegistry.addUris(file1.toUri(), file2.toUri())

        EspressoHelpers.clickOnText(string.add_layer)
        scheduler.flush()
        onView(withId(R.id.add_layer_button)).inRoot(isDialog()).perform(click())

        onView(withId(R.id.progress_indicator)).check(matches(isDisplayed()))
        onView(withId(R.id.layers)).check(matches(not(isDisplayed())))

        scheduler.flush()

        onView(withId(R.id.progress_indicator)).check(matches(not(isDisplayed())))
        onView(withId(R.id.layers)).check(matches(isDisplayed()))
    }

    @Test
    fun `when new layers added the list should be updated`() {
        launchFragment()

        scheduler.flush()

        testRegistry.addUris(file1.toUri(), file2.toUri())

        EspressoHelpers.clickOnText(string.add_layer)
        scheduler.flush()
        onView(withId(R.id.add_layer_button)).inRoot(isDialog()).perform(click())

        scheduler.flush()
        RobolectricHelpers.runLooper()

        onView(withId(R.id.layers)).check(matches(RecyclerViewMatcher.withListSize(3)))
        onView(withRecyclerView(R.id.layers).atPositionOnView(0, R.id.title)).check(matches(withText(string.none)))
        onView(withRecyclerView(R.id.layers).atPositionOnView(1, R.id.title)).check(matches(withText(file1.name)))
        onView(withRecyclerView(R.id.layers).atPositionOnView(2, R.id.title)).check(matches(withText(file2.name)))
    }

    @Test
    fun `layers are collapsed by default`() {
        referenceLayerRepository.addLayer(file1, false)
        referenceLayerRepository.addLayer(file2, false)

        launchFragment()

        scheduler.flush()

        assertLayerCollapsed(1)
        assertLayerCollapsed(2)
    }

    @Test
    fun `recreating maintains expanded layers`() {
        referenceLayerRepository.addLayer(file1, false)
        referenceLayerRepository.addLayer(file2, false)
        referenceLayerRepository.addLayer(file3, false)

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
        referenceLayerRepository.addLayer(file1, false)
        referenceLayerRepository.addLayer(file2, false)

        launchFragment()

        scheduler.flush()

        onView(withRecyclerView(R.id.layers).atPositionOnView(1, R.id.arrow)).perform(click())
        onView(withId(R.id.layers)).perform(scrollToPosition<RecyclerView.ViewHolder>(2))
        onView(withRecyclerView(R.id.layers).atPositionOnView(2, R.id.arrow)).perform(click())

        onView(withRecyclerView(R.id.layers).atPositionOnView(1, R.id.path)).check(matches(withText(file1.absolutePath)))
        onView(withRecyclerView(R.id.layers).atPositionOnView(2, R.id.path)).check(matches(withText(file2.absolutePath)))
    }

    @Test
    fun `clicking delete shows the confirmation dialog`() {
        referenceLayerRepository.addLayer(file1, false)

        launchFragment()

        scheduler.flush()

        onView(withRecyclerView(R.id.layers).atPositionOnView(1, R.id.arrow)).perform(click())
        onView(withRecyclerView(R.id.layers).atPositionOnView(1, R.id.delete_layer)).perform(scrollTo(), click())

        onView(withText(string.cancel)).inRoot(isDialog()).check(matches(isDisplayed()))
        onView(withText(string.delete_layer)).inRoot(isDialog()).check(matches(isDisplayed()))
    }

    @Test
    fun `clicking delete and canceling does not remove the layer`() {
        referenceLayerRepository.addLayer(file1, false)

        launchFragment()

        scheduler.flush()

        onView(withRecyclerView(R.id.layers).atPositionOnView(1, R.id.arrow)).perform(click())
        onView(withRecyclerView(R.id.layers).atPositionOnView(1, R.id.delete_layer)).perform(scrollTo(), click())

        onView(withText(string.cancel)).inRoot(isDialog()).perform(click())

        onView(withId(R.id.layers)).check(matches(RecyclerViewMatcher.withListSize(2)))
        onView(withId(R.id.layers)).perform(scrollToPosition<RecyclerView.ViewHolder>(0))
        onView(withRecyclerView(R.id.layers).atPositionOnView(0, R.id.title)).check(matches(withText(string.none)))
        onView(withRecyclerView(R.id.layers).atPositionOnView(1, R.id.title)).check(matches(withText(file1.name)))
        assertThat(file1.exists(), equalTo(true))
    }

    @Test
    fun `clicking delete and confirming removes the layer`() {
        referenceLayerRepository.addLayer(file1, false)
        referenceLayerRepository.addLayer(file2, false)

        launchFragment()

        scheduler.flush()

        onView(withRecyclerView(R.id.layers).atPositionOnView(1, R.id.arrow)).perform(click())
        onView(withRecyclerView(R.id.layers).atPositionOnView(1, R.id.delete_layer)).perform(scrollTo(), click())

        onView(withText(string.delete_layer)).inRoot(isDialog()).perform(click())
        scheduler.flush()

        onView(withId(R.id.layers)).check(matches(RecyclerViewMatcher.withListSize(2)))
        onView(withRecyclerView(R.id.layers).atPositionOnView(0, R.id.title)).check(matches(withText(string.none)))
        onView(withRecyclerView(R.id.layers).atPositionOnView(1, R.id.title)).check(matches(withText(file2.name)))
        assertThat(file1.exists(), equalTo(false))
        assertThat(file2.exists(), equalTo(true))
    }

    private fun assertLayerCollapsed(position: Int) {
        onView(withRecyclerView(R.id.layers).atPositionOnView(position, R.id.arrow)).check(matches(withImageDrawable(org.odk.collect.icons.R.drawable.ic_baseline_expand_24)))
        onView(withRecyclerView(R.id.layers).atPositionOnView(position, R.id.path)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
        onView(withRecyclerView(R.id.layers).atPositionOnView(position, R.id.delete_layer)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
    }

    private fun assertLayerExpanded(position: Int) {
        onView(withRecyclerView(R.id.layers).atPositionOnView(position, R.id.arrow)).check(matches(withImageDrawable(org.odk.collect.icons.R.drawable.ic_baseline_collapse_24)))
        onView(withRecyclerView(R.id.layers).atPositionOnView(position, R.id.path)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
        onView(withRecyclerView(R.id.layers).atPositionOnView(position, R.id.delete_layer)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
    }

    private fun launchFragment(): FragmentScenario<OfflineMapLayersPickerDialogFragment> {
        return fragmentScenarioLauncherRule.launchInContainer(OfflineMapLayersPickerDialogFragment::class.java)
    }

    private class InMemReferenceLayerRepository : ReferenceLayerRepository {
        private val layers = mutableListOf<ReferenceLayer>()

        override fun getAll(): List<ReferenceLayer> {
            return layers
        }

        override fun get(id: String): ReferenceLayer? {
            return layers.find { it.id == id }
        }

        override fun addLayer(file: File, shared: Boolean) {
            layers.add(ReferenceLayer(file.absolutePath, file, file.name))
        }
    }

    private class TestRegistry : ActivityResultRegistry() {
        val uris = mutableListOf<Uri>()

        override fun <I, O> onLaunch(
            requestCode: Int,
            contract: ActivityResultContract<I, O>,
            input: I,
            options: ActivityOptionsCompat?
        ) {
            assertThat(contract, instanceOf(ActivityResultContracts.GetMultipleContents()::class.java))
            assertThat(input, equalTo("*/*"))
            dispatchResult(requestCode, uris)
        }

        fun addUris(vararg uris: Uri) {
            this.uris.addAll(uris)
        }
    }
}
