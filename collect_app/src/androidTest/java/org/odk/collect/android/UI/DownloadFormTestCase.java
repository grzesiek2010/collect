package org.odk.collect.android.UI;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.activities.FormDownloadList;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

@RunWith(AndroidJUnit4.class)
public class DownloadFormTestCase {

    private static final String FORM_NAME = "Biggest N of Set";

    @Rule
    public ActivityTestRule<FormDownloadList> mActivityRule = new ActivityTestRule<>(
            FormDownloadList.class);

    @Test
    public void testDownloadForm() {

    }
}
