package org.odk.collect.hilttest

import androidx.test.core.app.launchActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import dagger.hilt.android.testing.UninstallModules
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@UninstallModules(HiltTestModule::class)
@Config(application = HiltTestApplication::class)
@HiltAndroidTest
class HiltTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @BindValue
    val hiltTestNameProvider = mock<HiltTestNameProvider> {
        on { getName() } doReturn "Yes Yes Yes"
    }

    @Test
    fun testHiltNameProvider() {
        launchActivity<HiltTestActivity>().use {
            it.onActivity {
                val a = it.hiltTestNameProvider.getName()
                val b = "sdsds"
            }
        }
    }
}
