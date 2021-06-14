package org.odk.collect.android.utilities

import android.app.Application
import android.content.Context
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.android.preferences.keys.MetaKeys
import org.odk.collect.shared.TempFiles
import org.odk.collect.testshared.InMemSettings
import java.io.File

@RunWith(AndroidJUnit4::class)
class LaunchStateTest {

    private val externalFilesDir = TempFiles.createTempDir()
    private val context = ApplicationProvider.getApplicationContext<Application>()

    @Test
    fun `isUpgradedFirstLaunch() returns false for empty meta settings`() {
        val appStateProvider = LaunchState(1, InMemSettings(), externalFilesDir, context)
        assertThat(appStateProvider.isUpgradedFirstLaunch(), equalTo(false))
    }

    @Test
    fun `isUpgradedFirstLaunch() returns false if last launched is equal to current version`() {
        val inMemSettings = InMemSettings()
        inMemSettings.save(MetaKeys.LAST_LAUNCHED, 1)

        val appStateProvider = LaunchState(1, inMemSettings, externalFilesDir, context)
        assertThat(appStateProvider.isUpgradedFirstLaunch(), equalTo(false))
    }

    @Test
    fun `isUpgradedFirstLaunch() returns true if last launched is less than current version`() {
        val inMemSettings = InMemSettings()
        inMemSettings.save(MetaKeys.LAST_LAUNCHED, 1)

        val appStateProvider = LaunchState(2, inMemSettings, externalFilesDir, context)
        assertThat(appStateProvider.isUpgradedFirstLaunch(), equalTo(true))
    }

    @Test
    fun `isUpgradedFirstLaunch() returns false if last launched is less than current version after appLaunched()`() {
        val inMemSettings = InMemSettings()
        inMemSettings.save(MetaKeys.LAST_LAUNCHED, 1)

        val appStateProvider = LaunchState(2, inMemSettings, externalFilesDir, context)
        appStateProvider.appLaunched()
        assertThat(appStateProvider.isUpgradedFirstLaunch(), equalTo(false))
    }

    @Test
    fun `isUpgradedFirstLaunch() returns true for empty meta settings if legacy metadata dir is not empty`() {
        val metadataDir = File(externalFilesDir, "metadata").also { it.mkdir() }
        File(metadataDir, "something").createNewFile()

        val appStateProvider = LaunchState(1, InMemSettings(), externalFilesDir, context)

        assertThat(appStateProvider.isUpgradedFirstLaunch(), equalTo(true))
    }

    @Test
    fun `isUpgradedFirstLaunch() returns true for empty meta settings if legacy general prefs is not empty`() {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
            .putString("anything", "anything")
            .apply()

        val appStateProvider = LaunchState(1, InMemSettings(), externalFilesDir, context)

        assertThat(appStateProvider.isUpgradedFirstLaunch(), equalTo(true))
    }

    @Test
    fun `isUpgradedFirstLaunch() returns true for empty meta settings if legacy admin prefs is not empty`() {
        context.getSharedPreferences("admin_prefs", Context.MODE_PRIVATE).edit()
            .putString("anything", "anything")
            .apply()

        val appStateProvider = LaunchState(1, InMemSettings(), externalFilesDir, context)

        assertThat(appStateProvider.isUpgradedFirstLaunch(), equalTo(true))
    }
}
