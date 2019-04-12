package org.odk.collect.android.externalintents;

import android.support.test.rule.ActivityTestRule;
import android.support.v7.app.AppCompatActivity;

import static org.odk.collect.android.externalintents.ExportedActivitiesUtils.clearDirectories;

class ExportedActivityTestRule<A extends AppCompatActivity> extends ActivityTestRule<A> {

    ExportedActivityTestRule(Class<A> activityClass) {
        super(activityClass);
    }

    @Override
    protected void beforeActivityLaunched() {
        super.beforeActivityLaunched();

        clearDirectories();
    }

}
