package org.odk.collect.android.support.pages

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.odk.collect.strings.R

class SavePointRecoveryDialogPage : Page<SavePointRecoveryDialogPage>() {
    override fun assertOnPage(): SavePointRecoveryDialogPage {
        onView(withText(R.string.recover)).inRoot(isDialog()).check(matches(isDisplayed()))
        onView(withText(R.string.do_not_recover)).inRoot(isDialog()).check(matches(isDisplayed()))
        return this
    }

    fun clickRecover(formName: String): FormHierarchyPage {
        clickOKOnDialog()
        return FormHierarchyPage(formName).assertOnPage()
    }

    fun clickDoNotRecover(formName: String): FormEntryPage {
        clickCancelOnDialog()
        return FormEntryPage(formName).assertOnPage()
    }
}
