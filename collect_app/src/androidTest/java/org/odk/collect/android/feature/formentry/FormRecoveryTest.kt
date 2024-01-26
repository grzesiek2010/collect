package org.odk.collect.android.feature.formentry

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.odk.collect.android.support.pages.MainMenuPage
import org.odk.collect.android.support.pages.ProjectSettingsPage
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.TestRuleChain

@RunWith(AndroidJUnit4::class)
class FormRecoveryTest {
    private var rule = CollectTestRule()

    @get:Rule
    var testRuleChain: RuleChain = TestRuleChain.chain()
        .around(rule)

    @Test
    fun startingAnUpdatedVersionOfAFormWhenThereIsASavepointForThePreviousVersionOfTheForm_shouldPromptUserAndSuccessfullyRecoverIfTheyAccept() {
        rule.startAtMainMenu()
            .copyForm("one-question.xml")
            .startBlankForm("One Question")
            .answerQuestion("what is your age", "30")
            .killAndReopenApp(MainMenuPage())
            .copyForm("one-question-updated.xml")
            .startBlankFormWithSavepointRecovery("One Question Updated")
            .clickRecover("One Question")
            .assertHierarchyItem(0, "what is your age", "30")
    }

    @Test
    fun startingAnUpdatedVersionOfAFormWhenThereIsASavepointForThePreviousVersionOfTheForm_shouldPromptUserAndStartTheNewFormIfTheyReject() {
        rule.startAtMainMenu()
            .copyForm("one-question.xml")
            .startBlankForm("One Question")
            .answerQuestion("what is your age", "30")
            .killAndReopenApp(MainMenuPage())
            .copyForm("one-question-updated.xml")
            .startBlankFormWithSavepointRecovery("One Question Updated")
            .clickDoNotRecover("One Question Updated")
    }

    @Test
    fun startingAnUpdatedVersionOfAFormWhenThereIsASavepointForThePreviousVersionOfTheForm_shouldNotAskAgainIfUserRejects() {
        rule.startAtMainMenu()
            .copyForm("one-question.xml")
            .startBlankForm("One Question")
            .answerQuestion("what is your age", "30")
            .killAndReopenApp(MainMenuPage())
            .copyForm("one-question-updated.xml")
            .startBlankFormWithSavepointRecovery("One Question Updated")
            .clickDoNotRecover("One Question Updated")
            .pressBackAndDiscardForm()
            .openProjectSettingsDialog()
            .clickSettings()
            .clickFormManagement()
            .clickHideOldFormVersions()
            .pressBack(ProjectSettingsPage())
            .pressBack(MainMenuPage())
            .startBlankForm("One Question Updated")
            .pressBackAndDiscardForm()
            .startBlankForm("One Question")
    }

    @Test
    fun startingAnUpdatedVersionOfAFormWhenThereIsASavepointForBothTheNewAndThePreviousVersionOfTheForm_shouldNotPromptUserToRecoverButStartTheNewerFormWithItsSavepoint() {
        rule.startAtMainMenu()
            .copyForm("one-question.xml")
            .clickFillBlankForm()
            .pressBack(MainMenuPage())
            .copyForm("one-question-updated.xml")
            .openProjectSettingsDialog()
            .clickSettings()
            .clickFormManagement()
            .clickHideOldFormVersions()
            .pressBack(ProjectSettingsPage())
            .pressBack(MainMenuPage())
            .startBlankForm("One Question Updated")
            .answerQuestion("what is your age", "30")
            .killAndReopenApp(MainMenuPage())
            .startBlankForm("One Question")
            .answerQuestion("what is your age", "20")
            .killAndReopenApp(MainMenuPage())
            .startBlankFormWithSavepoint("One Question Updated")
            .assertHierarchyItem(0, "what is your age", "30")
    }

    @Test
    fun startingAnOldVersionOfAFormWhenThereIsASavepointForBothTheNewAndThePreviousVersionOfTheForm_shouldNotPromptUserToRecoverButStartTheOldFormWithItsSavepoint() {
        rule.startAtMainMenu()
            .copyForm("one-question.xml")
            .clickFillBlankForm()
            .pressBack(MainMenuPage())
            .copyForm("one-question-updated.xml")
            .openProjectSettingsDialog()
            .clickSettings()
            .clickFormManagement()
            .clickHideOldFormVersions()
            .pressBack(ProjectSettingsPage())
            .pressBack(MainMenuPage())
            .startBlankForm("One Question Updated")
            .answerQuestion("what is your age", "30")
            .killAndReopenApp(MainMenuPage())
            .startBlankForm("One Question")
            .answerQuestion("what is your age", "20")
            .killAndReopenApp(MainMenuPage())
            .startBlankFormWithSavepoint("One Question")
            .assertHierarchyItem(0, "what is your age", "20")
    }

    @Test
    fun startingAnUpdatedVersionOfAFormWhenThereIsASavepointForMoreThanOneOfTheOlderVersionsOfTheForm_shouldPromptUserAndSuccessfullyRecoverTheNewestFromTheOldFormVersionsIfTheyAccept() {
        rule.startAtMainMenu()
            .copyForm("one-question.xml")
            .clickFillBlankForm()
            .pressBack(MainMenuPage())
            .copyForm("one-question-updated.xml")
            .clickFillBlankForm()
            .pressBack(MainMenuPage())
            .copyForm("one-question-updated-again.xml")
            .openProjectSettingsDialog()
            .clickSettings()
            .clickFormManagement()
            .clickHideOldFormVersions()
            .pressBack(ProjectSettingsPage())
            .pressBack(MainMenuPage())
            .startBlankForm("One Question Updated")
            .answerQuestion("what is your age", "40")
            .killAndReopenApp(MainMenuPage())
            .startBlankForm("One Question")
            .answerQuestion("what is your age", "50")
            .killAndReopenApp(MainMenuPage())
            .startBlankFormWithSavepointRecovery("One Question Updated Again")
            .clickRecover("One Question Updated")
            .assertHierarchyItem(0, "what is your age", "40")
    }
}
