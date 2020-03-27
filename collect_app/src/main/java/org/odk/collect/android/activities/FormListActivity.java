package org.odk.collect.android.activities;

import org.odk.collect.android.utilities.ListSortingUtils;

abstract class FormListActivity extends AppListActivity {

    protected String getSortingOrder() {
        return ListSortingUtils.getFormSortingOrder(getSelectedSortingOrder());
    }
}
