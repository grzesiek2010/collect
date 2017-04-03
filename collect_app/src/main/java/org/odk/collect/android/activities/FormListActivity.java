package org.odk.collect.android.activities;

import org.odk.collect.android.provider.FormsProviderAPI;

import static org.odk.collect.android.utilities.ApplicationConstants.SortingOrder.BY_DATE_ASC;
import static org.odk.collect.android.utilities.ApplicationConstants.SortingOrder.BY_DATE_DESC;
import static org.odk.collect.android.utilities.ApplicationConstants.SortingOrder.BY_NAME_ASC;
import static org.odk.collect.android.utilities.ApplicationConstants.SortingOrder.BY_NAME_DESC;

abstract class FormListActivity extends AppListActivity {
    @Override
    protected void sortByNameAsc() {
        setupAdapter();
    }

    @Override
    protected void sortByNameDesc() {
        setupAdapter();
    }

    @Override
    protected void sortByDateAsc() {
        setupAdapter();
    }

    @Override
    protected void sortByDateDesc() {
        setupAdapter();
    }

    @Override
    protected void sortByStatusAsc() {
    }

    @Override
    protected void sortByStatusDesc() {
    }

    @Override
    protected void setupAdapter() {
    }

    public String getSortingOrder() {
        String sortingOrder = FormsProviderAPI.FormsColumns.DISPLAY_NAME + " ASC";
        switch (mSelectedSortingOrder) {
            case BY_NAME_ASC:
                sortingOrder = FormsProviderAPI.FormsColumns.DISPLAY_NAME + " ASC";
                break;
            case BY_NAME_DESC:
                sortingOrder = FormsProviderAPI.FormsColumns.DISPLAY_NAME + " DESC";
                break;
            case BY_DATE_ASC:
                sortingOrder = FormsProviderAPI.FormsColumns.DATE + " ASC";
                break;
            case BY_DATE_DESC:
                sortingOrder = FormsProviderAPI.FormsColumns.DATE + " DESC";
                break;
        }
        return sortingOrder;
    }
}