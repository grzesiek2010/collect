package org.odk.collect.android.utilities;

import org.odk.collect.android.provider.InstanceProviderAPI;

import static org.odk.collect.android.utilities.ApplicationConstants.SortingOrder.BY_DATE_ASC;
import static org.odk.collect.android.utilities.ApplicationConstants.SortingOrder.BY_DATE_DESC;
import static org.odk.collect.android.utilities.ApplicationConstants.SortingOrder.BY_NAME_ASC;
import static org.odk.collect.android.utilities.ApplicationConstants.SortingOrder.BY_NAME_DESC;
import static org.odk.collect.android.utilities.ApplicationConstants.SortingOrder.BY_STATUS_ASC;
import static org.odk.collect.android.utilities.ApplicationConstants.SortingOrder.BY_STATUS_DESC;

public class ListSortingUtils {
    private ListSortingUtils() {
    }

    public static String getInstanceSortingOrder(int selectedSortingOrder) {
        String sortingOrder = InstanceProviderAPI.InstanceColumns.DISPLAY_NAME + " COLLATE NOCASE ASC, " + InstanceProviderAPI.InstanceColumns.STATUS + " DESC";
        switch (selectedSortingOrder) {
            case BY_NAME_ASC:
                sortingOrder = InstanceProviderAPI.InstanceColumns.DISPLAY_NAME + " COLLATE NOCASE ASC, " + InstanceProviderAPI.InstanceColumns.STATUS + " DESC";
                break;
            case BY_NAME_DESC:
                sortingOrder = InstanceProviderAPI.InstanceColumns.DISPLAY_NAME + " COLLATE NOCASE DESC, " + InstanceProviderAPI.InstanceColumns.STATUS + " DESC";
                break;
            case BY_DATE_ASC:
                sortingOrder = InstanceProviderAPI.InstanceColumns.LAST_STATUS_CHANGE_DATE + " ASC";
                break;
            case BY_DATE_DESC:
                sortingOrder = InstanceProviderAPI.InstanceColumns.LAST_STATUS_CHANGE_DATE + " DESC";
                break;
            case BY_STATUS_ASC:
                sortingOrder = InstanceProviderAPI.InstanceColumns.STATUS + " ASC, " + InstanceProviderAPI.InstanceColumns.DISPLAY_NAME + " COLLATE NOCASE ASC";
                break;
            case BY_STATUS_DESC:
                sortingOrder = InstanceProviderAPI.InstanceColumns.STATUS + " DESC, " + InstanceProviderAPI.InstanceColumns.DISPLAY_NAME + " COLLATE NOCASE ASC";
                break;
        }
        return sortingOrder;
    }
}
