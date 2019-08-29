package org.odk.collect.android.regression;

import android.Manifest;

import androidx.test.rule.GrantPermissionRule;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.espressoutils.FormEntry;
import org.odk.collect.android.espressoutils.MainMenu;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.ResetStateRule;
import org.odk.collect.android.support.ScreenshotOnFailureTestRule;

import java.util.concurrent.TimeoutException;

import static androidx.test.espresso.Espresso.pressBack;

// Issue number NODK-211
@RunWith(AndroidJUnit4.class)
public class SignatureWidgetTest extends BaseRegressionTest {

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_PHONE_STATE)
            )
            .around(new ResetStateRule())
            .around(new CopyFormRule("All_widgets.xml"));

    @Rule
    public TestRule screenshotFailRule = new ScreenshotOnFailureTestRule();

    @Test
    public void saveIgnoreDialog_ShouldUseBothOptions() throws TimeoutException, InterruptedException {

        //TestCase1
        MainMenu.startBlankForm("All widgets");
        FormEntry.clickGoToIconInForm();
        FormEntry.clickOnText("Image widgets");
        FormEntry.clickOnText("Signature widget");
        FormEntry.clickSignatureButton();
        waitForId(R.id.drawView);
        pressBack();
        FormEntry.checkIsTextDisplayed("Exit Gather Signature");
        FormEntry.checkIsStringDisplayed(R.string.keep_changes);
        FormEntry.clickOnString(R.string.do_not_save);
        waitForId(R.id.questionholder);
        FormEntry.clickSignatureButton();
        waitForId(R.id.drawView);
        pressBack();
        FormEntry.clickOnString(R.string.keep_changes);
        waitForId(R.id.questionholder);
        FormEntry.clickGoToIconInForm();
        FormEntry.clickJumpEndButton();
        FormEntry.clickSaveAndExit();
    }

    @Test
    public void multiClickOnPlus_ShouldDisplayIcons() throws TimeoutException, InterruptedException {

        //TestCase2
        MainMenu.startBlankForm("All widgets");
        FormEntry.clickGoToIconInForm();
        FormEntry.clickOnText("Image widgets");
        FormEntry.clickOnText("Signature widget");
        FormEntry.clickSignatureButton();
        waitForId(R.id.drawView);
        FormEntry.clickOnId(R.id.fab_actions);
        FormEntry.checkIsIdDisplayed(R.id.fab_save_and_close);
        FormEntry.clickOnId(R.id.fab_set_color);
        FormEntry.clickOnString(R.string.ok);
        FormEntry.clickOnId(R.id.fab_actions);
        FormEntry.checkIsIdDisplayed(R.id.fab_set_color);
        pressBack();
        FormEntry.clickOnString(R.string.keep_changes);
        waitForId(R.id.questionholder);
        FormEntry.clickGoToIconInForm();
        FormEntry.clickJumpEndButton();
        FormEntry.clickSaveAndExit();
    }
}