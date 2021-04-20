package org.odk.collect.android.activities.viewmodels

import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.odk.collect.android.preferences.keys.GeneralKeys
import org.odk.collect.android.preferences.keys.MetaKeys
import org.odk.collect.android.preferences.source.SharedPreferencesSettings

class SplashScreenViewModelTest {
    private lateinit var generalSettings: SharedPreferencesSettings
    private lateinit var metaSettings: SharedPreferencesSettings
    private lateinit var splashScreenViewModel: SplashScreenViewModel

    @Before
    fun setup() {
        generalSettings = Mockito.mock(SharedPreferencesSettings::class.java)
        metaSettings = Mockito.mock(SharedPreferencesSettings::class.java)
        splashScreenViewModel = SplashScreenViewModel(generalSettings, metaSettings)
    }

    @Test
    fun `shouldDisplaySplashScreen() should return false if displaying splash screen is disabled`() {
        `when`(generalSettings.getBoolean(GeneralKeys.KEY_SHOW_SPLASH)).thenReturn(false)
        assertThat(splashScreenViewModel.shouldDisplaySplashScreen, `is`(false))
    }

    @Test
    fun `shouldDisplaySplashScreen() should return true if displaying splash screen is enabled`() {
        `when`(generalSettings.getBoolean(GeneralKeys.KEY_SHOW_SPLASH)).thenReturn(true)
        assertThat(splashScreenViewModel.shouldDisplaySplashScreen, `is`(true))
    }

    @Test
    fun `splashScreenLogoFile should return empty string if no custom logo is specified`() {
        assertThat(splashScreenViewModel.splashScreenLogoFile.name, `is`(""))
    }

    @Test
    fun `splashScreenLogoFile should return file name if custom logo is specified`() {
        `when`(generalSettings.getString(GeneralKeys.KEY_SPLASH_PATH)).thenReturn("blah")
        assertThat(splashScreenViewModel.splashScreenLogoFile.name, `is`("blah"))
    }

    @Test
    fun `shouldFirstLaunchDialogBeDisplayed() should return true if the app is newly installed`() {
        assertThat(splashScreenViewModel.isFirstLaunch(), `is`(true))
        verify(metaSettings).save(MetaKeys.KEY_FIRST_LAUNCH, false)
    }

    @Test
    fun `shouldFirstLaunchDialogBeDisplayed() should return false if the app is not newly installed`() {
        `when`(metaSettings.contains(MetaKeys.KEY_FIRST_LAUNCH)).thenReturn(true)
        assertThat(splashScreenViewModel.isFirstLaunch(), `is`(false))
    }
}
