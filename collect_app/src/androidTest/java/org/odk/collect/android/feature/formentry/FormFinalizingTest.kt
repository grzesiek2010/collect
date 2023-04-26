package org.odk.collect.android.feature.formentry

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.odk.collect.android.support.pages.MainMenuPage
import org.odk.collect.android.support.pages.SaveOrIgnoreDialog
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.TestRuleChain.chain

@RunWith(AndroidJUnit4::class)
class FormFinalizingTest {
    private val rule = CollectTestRule()

    @get:Rule
    val copyFormChain: RuleChain = chain().around(rule)

    @Test
    fun fillingForm_andPressingSaveAsDraft_doesNotFinalizesForm() {
        rule.startAtMainMenu()
            .copyForm(FORM)
            .assertNumberOfFinalizedForms(0)
            .startBlankForm("One Question")
            .swipeToEndScreen()
            .clickSaveAsDraft()
            .assertNumberOfEditableForms(1)
            .assertNumberOfFinalizedForms(0)
    }

    @Test
    fun fillingForm_andPressingFinalize_finalizesForm() {
        rule.startAtMainMenu()
            .copyForm(FORM)
            .assertNumberOfFinalizedForms(0)
            .startBlankForm("One Question")
            .swipeToEndScreen()
            .clickFinalize()
            .assertNumberOfEditableForms(1)
            .assertNumberOfFinalizedForms(1)
    }

    @Test
    fun fillingForm_andPressingBack_andPressingSave_doesNotFinalizesForm() {
        rule.startAtMainMenu()
            .copyForm(FORM)
            .assertNumberOfFinalizedForms(0)
            .startBlankForm("One Question")
            .closeSoftKeyboard()
            .pressBack(SaveOrIgnoreDialog("One Question", MainMenuPage()))
            .clickSaveChanges()
            .assertNumberOfEditableForms(1)
            .assertNumberOfFinalizedForms(0)
    }

    companion object {
        private const val FORM = "one-question.xml"
    }
}
