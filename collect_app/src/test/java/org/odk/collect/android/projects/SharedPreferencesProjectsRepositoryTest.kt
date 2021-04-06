package org.odk.collect.android.projects

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.gson.Gson
import org.junit.runner.RunWith
import org.odk.collect.android.preferences.keys.AdminKeys
import org.odk.collect.android.preferences.source.SettingsProvider
import org.odk.collect.android.utilities.UUIDGenerator

@RunWith(AndroidJUnit4::class)
class SharedPreferencesProjectsRepositoryTest : ProjectsRepositoryTest() {
    override fun buildSubject(): ProjectsRepository {
        SettingsProvider(ApplicationProvider.getApplicationContext()).getAdminSettings().remove(AdminKeys.KEY_PROJECTS)
        return SharedPreferencesProjectsRepository(UUIDGenerator(), Gson(), SettingsProvider(ApplicationProvider.getApplicationContext()).getAdminSettings())
    }
}
