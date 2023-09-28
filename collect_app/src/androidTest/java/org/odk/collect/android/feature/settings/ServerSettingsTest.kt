package org.odk.collect.android.feature.settings

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.odk.collect.android.support.TestDependencies
import org.odk.collect.android.support.pages.MainMenuPage
import org.odk.collect.android.support.pages.ProjectSettingsPage
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.TestRuleChain.chain
import org.odk.collect.androidtest.RecordedIntentsRule
import org.odk.collect.strings.R

@RunWith(AndroidJUnit4::class)
class ServerSettingsTest {
    private val testDependencies = TestDependencies()
    val rule = CollectTestRule()

    @get:Rule
    var copyFormChain: RuleChain = chain(testDependencies)
        .around(RecordedIntentsRule())
        .around(rule)

    @Test
    fun whenUsingODKServer_canAddCredentialsForServer() {
        testDependencies.server.setCredentials("Joe", "netsky")
        testDependencies.server.addForm("One Question", "one-question", "1", "one-question.xml")

        MainMenuPage().assertOnPage()
            .openProjectSettingsDialog()
            .clickSettings()
            .clickServerSettings()
            .clickOnURL()
            .inputText(testDependencies.server.url)
            .clickOKOnDialog()
            .assertText(testDependencies.server.url)
            .clickServerUsername()
            .inputText("Joe")
            .clickOKOnDialog()
            .assertText("Joe")
            .clickServerPassword()
            .inputText("netsky")
            .clickOKOnDialog()
            .assertText("********")
            .pressBack(ProjectSettingsPage())
            .pressBack(MainMenuPage())
            .clickGetBlankForm()
            .clickGetSelected()
            .assertMessage("All downloads succeeded!")
            .clickOKOnDialog(MainMenuPage())
    }

    @Test
    fun selectingServerTypeIsDisabled() {
        MainMenuPage().assertOnPage()
            .openProjectSettingsDialog()
            .clickSettings()
            .clickServerSettings()
            .clickOnServerType()
            .assertTextDoesNotExist(R.string.cancel)
    }

    @Test
    fun currentServerUrlShouldBeSelectedToMakeChangingItEasier_whenServerURLConfigurationDialogIsLaunched() {
        MainMenuPage().assertOnPage()
                .openProjectSettingsDialog()
                .clickSettings()
                .clickServerSettings()
                .assertText("https://demo.getodk.org")
                .clickOnURL()
                .addText(testDependencies.server.url)
                .clickOKOnDialog()
                .assertText(testDependencies.server.url)
    }
}
