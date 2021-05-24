package org.odk.collect.android.feature.projects

import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.odk.collect.android.R
import org.odk.collect.android.support.CollectTestRule
import org.odk.collect.android.support.TestDependencies
import org.odk.collect.android.support.TestRuleChain
import org.odk.collect.android.support.pages.GeneralSettingsPage
import org.odk.collect.android.support.pages.MainMenuPage

class SwitchProjectTest {

    val rule = CollectTestRule()
    val testDependencies = TestDependencies()

    @get:Rule
    var chain: RuleChain = TestRuleChain.chain(testDependencies)
        .around(rule)

    @Test
    fun canSwitchActiveProjectToAnotherInList() {
        // Add project Turtle nesting
        rule.startAtMainMenu()
            .assertProjectIcon("D", "#3e9fcc")
            .openProjectSettings()
            .clickAddProject()
            .inputProjectName("Turtle nesting")
            .inputProjectIcon("T")
            .inputProjectColor("#0000FF")
            .addProject()

            // Switch to Turtle nesting
            .openProjectSettings()
            .assertCurrentProject("Demo project")
            .assertInactiveProject("Turtle nesting")
            .selectProject("Turtle nesting")
            .checkIsToastWithMessageDisplayed(R.string.switched_project, "Turtle nesting")
            .assertProjectIcon("T", "#0000FF")
    }

    @Test
    fun switchingProject_switchesServerFormsAndInstances() {
        testDependencies.server.addForm("One Question", "one-question", "1", "one-question.xml")

        rule.startAtMainMenu()
            // Copy and fill form
            .copyForm("two-question.xml")
            .startBlankForm("Two Question")
            .swipeToNextQuestion("What is your age?")
            .swipeToEndScreen()
            .clickSaveAndExit()
            .assertNumberOfFinalizedForms(1)

            // Create and switch to new project
            .assertProjectIcon("D", "#3e9fcc")
            .openProjectSettings()
            .clickAddProject()
            .inputProjectName("Turtle nesting")
            .inputProjectIcon("T")
            .inputProjectColor("#0000FF")
            .addProject()
            .openProjectSettings()
            .selectProject("Turtle nesting")

            // Set server and download form
            .setServer(testDependencies.server.url)
            .clickGetBlankForm()
            .clickGetSelected()
            .clickOK(MainMenuPage())

            // Fill form
            .startBlankForm("One Question")
            .swipeToEndScreen()
            .clickSaveAndExit()
            .assertNumberOfFinalizedForms(1)

            // Switch back to first project
            .openProjectSettings()
            .selectProject("Demo project")

            // Check server
            .openProjectSettings()
            .clickGeneralSettings()
            .clickServerSettings()
            .clickOnURL()
            .assertText("https://demo.getodk.org")
            .clickOKOnDialog()
            .pressBack(GeneralSettingsPage())
            .pressBack(MainMenuPage())

            // Check forms
            .clickFillBlankForm()
            .assertFormExists("Two Question")
            .pressBack(MainMenuPage())

            // Check instances
            .clickSendFinalizedForm(1)
            .assertText("Two Question")
    }
}
