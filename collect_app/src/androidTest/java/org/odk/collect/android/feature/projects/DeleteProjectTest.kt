package org.odk.collect.android.feature.projects

import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.odk.collect.android.R
import org.odk.collect.android.support.CollectTestRule
import org.odk.collect.android.support.TestRuleChain
import org.odk.collect.android.support.pages.MainMenuPage

class DeleteProjectTest {

    val rule = CollectTestRule()

    @get:Rule
    var chain: RuleChain = TestRuleChain
        .chain()
        .around(rule)

    @Test
    fun deleteProjectTest() {
        // Add project Turtle nesting
        rule.startAtMainMenu()
            .openProjectSettings()
            .clickAddProject()
            .switchToManualMode()
            .inputUrl("https://my-server.com")
            .inputUsername("John")
            .addProject()

            // Delete Demo project
            .openProjectSettings()
            .clickAdminSettings()
            .deleteProject()

            // Assert switching to Turtle nesting
            .checkIsToastWithMessageDisplayed(R.string.switched_project, "my-server.com")
            .assertProjectIcon("M")

            // Delete Turtle nesting project
            .openProjectSettings()
            .clickAdminSettings()
            .deleteLastProject()
    }

    @Test
    fun deleteProjectWithInstancesTest() {
        // Fill a form
        rule.startAtMainMenu()
            .copyForm("one-question.xml")
            .startBlankForm("One Question")
            .swipeToEndScreen()
            .clickSaveAndExit()

            // Try to delete the project
            .openProjectSettings()
            .clickAdminSettings()
            .deleteProjectWithInstances()

            // Delete saved instance
            .pressBack(MainMenuPage())
            .clickDeleteSavedForm()
            .clickForm("One Question")
            .clickDeleteSelected(1)
            .clickDeleteForms()
            .pressBack(MainMenuPage())

            // Try to delete the project
            .openProjectSettings()
            .clickAdminSettings()
            .deleteLastProject()
    }
}
