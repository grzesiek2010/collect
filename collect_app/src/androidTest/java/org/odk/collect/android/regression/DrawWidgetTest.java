package org.odk.collect.android.regression;

import android.Manifest;

import androidx.test.rule.GrantPermissionRule;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.espressoutils.FormEntry;
import org.odk.collect.android.espressoutils.MainMenu;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.ResetStateRule;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.odk.collect.android.espressoutils.matchers.DrawableMatcher.withImageDrawable;
import static org.odk.collect.android.espressoutils.matchers.RecyclerViewMatcher.withRecyclerView;

// Issue number NODK-209
@RunWith(AndroidJUnit4.class)
public class DrawWidgetTest extends BaseRegressionTest {

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
            )
            .around(new ResetStateRule())
            .around(new CopyFormRule("All_widgets.xml", "regression/"));

    @Test
    public void saveIgnoreDialog_ShouldUseBothOptions() {

        onView(withId(R.id.enter_data)).perform(click());
        onView(withId(R.id.menu_sort)).perform(click());

        onView(withRecyclerView(R.id.recyclerView)
                .atPositionOnView(0, R.id.title))
                .check(matches(withText(main.getActivity().getString(R.string.sort_by_name_asc))));
        onView(withRecyclerView(R.id.recyclerView)
                .atPositionOnView(0, R.id.icon))
                .check(matches(withImageDrawable(R.drawable.ic_sort_by_alpha)));

        onView(withRecyclerView(R.id.recyclerView)
                .atPositionOnView(1, R.id.title))
                .check(matches(withText(main.getActivity().getString(R.string.sort_by_name_desc))));
        onView(withRecyclerView(R.id.recyclerView)
                .atPositionOnView(1, R.id.icon))
                .check(matches(withImageDrawable(R.drawable.ic_sort_by_alpha)));

        onView(withRecyclerView(R.id.recyclerView)
                .atPositionOnView(2, R.id.title))
                .check(matches(withText(main.getActivity().getString(R.string.sort_by_date_asc))));
        onView(withRecyclerView(R.id.recyclerView)
                .atPositionOnView(2, R.id.icon))
                .check(matches(withImageDrawable(R.drawable.ic_access_time)));

        onView(withRecyclerView(R.id.recyclerView)
                .atPositionOnView(3, R.id.title))
                .check(matches(withText(main.getActivity().getString(R.string.sort_by_date_desc))));
        onView(withRecyclerView(R.id.recyclerView)
                .atPositionOnView(3, R.id.icon))
                .check(matches(withImageDrawable(R.drawable.ic_access_time)));
    }

    @Test
    public void setColor_ShouldSeeColorPicker() {

        //TestCase2
        MainMenu.startBlankForm("All widgets");
        FormEntry.clickGoToIconInForm();
        FormEntry.clickOnText("Image widgets");
        FormEntry.clickOnText("Draw widget");
        FormEntry.clickOnId(R.id.simple_button);
        FormEntry.clickOnId(R.id.fab_actions);
        FormEntry.clickOnId(R.id.fab_set_color);
        FormEntry.clickOnString(R.string.ok);
        pressBack();
        FormEntry.clickOnString(R.string.keep_changes);
        FormEntry.clickGoToIconInForm();
        FormEntry.clickJumpEndButton();
        FormEntry.clickSaveAndExit();
    }

    @Test
    public void multiClickOnPlus_ShouldDisplayIcons() {

        //TestCase3
        MainMenu.startBlankForm("All widgets");
        FormEntry.clickGoToIconInForm();
        FormEntry.clickOnText("Image widgets");
        FormEntry.clickOnText("Draw widget");
        FormEntry.clickOnId(R.id.simple_button);
        FormEntry.clickOnId(R.id.fab_actions);
        FormEntry.checkIsStringDisplayed(R.string.set_color);
        FormEntry.checkIsIdDisplayed(R.id.fab_clear);
        FormEntry.clickOnId(R.id.fab_actions);
        FormEntry.checkIsStringDisplayed(R.string.set_color);
        FormEntry.checkIsIdDisplayed(R.id.fab_save_and_close);
        FormEntry.clickOnId(R.id.fab_actions);
        FormEntry.checkIsStringDisplayed(R.string.set_color);
        FormEntry.checkIsStringDisplayed(R.string.set_color);
        pressBack();
        FormEntry.clickOnString(R.string.keep_changes);
        FormEntry.clickGoToIconInForm();
        FormEntry.clickJumpEndButton();
        FormEntry.clickSaveAndExit();
    }
}