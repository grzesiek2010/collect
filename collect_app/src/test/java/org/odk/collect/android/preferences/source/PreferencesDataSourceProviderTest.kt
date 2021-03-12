package org.odk.collect.android.preferences.source

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.android.preferences.keys.MetaKeys.CURRENT_PROJECT

@RunWith(AndroidJUnit4::class)
class PreferencesDataSourceProviderTest {
    private val preferencesDataSourceProvider = PreferencesDataSourceProvider(ApplicationProvider.getApplicationContext())

    @Test
    fun `getMetaPreferences() should always return the same object`() {
        assertThat(preferencesDataSourceProvider.getMetaPreferences(), `is`(preferencesDataSourceProvider.getMetaPreferences()))
    }

    @Test
    fun `getGeneralPreferences() should always return the same object`() {
        assertThat(preferencesDataSourceProvider.getGeneralPreferences(), `is`(preferencesDataSourceProvider.getGeneralPreferences()))
        assertThat(preferencesDataSourceProvider.getGeneralPreferences("1234"), `is`(preferencesDataSourceProvider.getGeneralPreferences("1234")))

    }

    @Test
    fun `getAdminPreferences() should always return the same object`() {
        assertThat(preferencesDataSourceProvider.getAdminPreferences(), `is`(preferencesDataSourceProvider.getAdminPreferences()))
        assertThat(preferencesDataSourceProvider.getAdminPreferences("1234"), `is`(preferencesDataSourceProvider.getAdminPreferences("1234")))
    }

    @Test
    fun `x`() {
        preferencesDataSourceProvider.getMetaPreferences().save(CURRENT_PROJECT, "1234")
    }
}