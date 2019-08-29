package org.odk.collect.android.regression;

import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.odk.collect.android.activities.MainMenuActivity;

import java.util.concurrent.TimeoutException;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

public class BaseRegressionTest {

    @Rule
    public ActivityTestRule<MainMenuActivity> main = new ActivityTestRule<>(MainMenuActivity.class);

    void waitForId(int viewId) throws TimeoutException, InterruptedException {
        waitFor(() -> onView(withId(viewId)).check(ViewAssertions.matches(isDisplayed())));
    }

    private static void waitFor(Runnable runnable) throws TimeoutException, InterruptedException {
        long startTime = System.currentTimeMillis();

        do {
            try {
                runnable.run();
                return;
            } catch (Exception e) {
                Thread.sleep(500);
            }
        } while ((System.currentTimeMillis() - startTime) < 10000);
        throw new TimeoutException();
    }
}