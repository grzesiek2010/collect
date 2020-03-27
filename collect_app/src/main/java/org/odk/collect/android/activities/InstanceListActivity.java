package org.odk.collect.android.activities;

import org.odk.collect.android.utilities.ListSortingUtils;

abstract class InstanceListActivity extends AppListActivity {
    protected String getSortingOrder() {
        return ListSortingUtils.getInstanceSortingOrder(getSelectedSortingOrder());
    }
}