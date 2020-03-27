package org.odk.collect.android.utilities;

import org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns;

import static org.odk.collect.android.utilities.ApplicationConstants.SortingOrder.BY_DATE_ASC;
import static org.odk.collect.android.utilities.ApplicationConstants.SortingOrder.BY_DATE_DESC;
import static org.odk.collect.android.utilities.ApplicationConstants.SortingOrder.BY_NAME_DESC;
import static org.odk.collect.android.utilities.ApplicationConstants.SortingOrder.BY_STATUS_ASC;
import static org.odk.collect.android.utilities.ApplicationConstants.SortingOrder.BY_STATUS_DESC;

public class ListSortingUtils {
    private static String INSTANCES_BY_NAME_ASC = InstanceColumns.DISPLAY_NAME + " COLLATE NOCASE ASC, " + InstanceColumns.STATUS + " DESC";
    private static String INSTANCES_BY_NAME_DESC = InstanceColumns.DISPLAY_NAME + " COLLATE NOCASE DESC, " + InstanceColumns.STATUS + " DESC";
    private static String INSTANCES_BY_DATE_ASC = InstanceColumns.LAST_STATUS_CHANGE_DATE + " ASC";
    private static String INSTANCES_BY_DATE_DESC = InstanceColumns.LAST_STATUS_CHANGE_DATE + " DESC";
    private static String INSTANCES_BY_STATUS_ASC = InstanceColumns.STATUS + " ASC, " + InstanceColumns.DISPLAY_NAME + " COLLATE NOCASE ASC";
    private static String INSTANCES_BY_STATUS_DESC = InstanceColumns.STATUS + " DESC, " + InstanceColumns.DISPLAY_NAME + " COLLATE NOCASE ASC";

    private ListSortingUtils() {
    }

    public static String getInstanceSortingOrder(int selectedSortingOrder) {
        switch (selectedSortingOrder) {
            case BY_NAME_DESC:
                return INSTANCES_BY_NAME_DESC;
            case BY_DATE_ASC:
                return INSTANCES_BY_DATE_ASC;
            case BY_DATE_DESC:
                return INSTANCES_BY_DATE_DESC;
            case BY_STATUS_ASC:
                return INSTANCES_BY_STATUS_ASC;
            case BY_STATUS_DESC:
                return INSTANCES_BY_STATUS_DESC;
            default:
                return INSTANCES_BY_NAME_ASC;
        }
    }
}
