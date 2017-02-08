package org.odk.collect.android.UI;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.MainMenuActivity;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class DownloadFormTestCase {

    private static final String FORM_NAME = "Biggest N of Set";

    @Rule
    public ActivityTestRule<MainMenuActivity> mActivityRule = new ActivityTestRule<>(
            MainMenuActivity.class);

    @Test
    public void testDownloadForm() {
        onView(withId(R.id.get_forms)).perform(click());
        onView(withId(R.id.toggle_button)).perform(click());
        onView(withText(FORM_NAME)).perform(click());
        onView(withId(R.id.add_button)).perform(click());
        onView(withText(R.string.ok))
                .check(matches(isDisplayed()))
                .perform(click());
        onView(withId(R.id.enter_data)).perform(click());
        onView(withText(FORM_NAME)).perform(click());
        onView(withText(R.string.entering_repeat))
                .check(matches(isDisplayed()))
                .perform(click());
        onView(withText(FORM_NAME)).check(matches(isDisplayed()));
    }
}
