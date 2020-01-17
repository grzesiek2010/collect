package org.odk.collect.android.utilities;

import android.os.Environment;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.preferences.GeneralSharedPreferences;

import static org.odk.collect.android.preferences.GeneralKeys.KEY_SCOPED_STORAGE_USED;

public class StorageManager {

    private static String getStoragePath() {
        return isScopedStorageUsed()
                ? Collect.getInstance().getExternalFilesDir(null).getAbsolutePath()
                : Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    public static String getODKDirPath() {
        return getStoragePath() + "/odk";
    }

    public static String getFormsDirPath() {
        return getODKDirPath() + "/forms";
    }

    public static String getInstancesDirPath() {
        return getODKDirPath() + "/instances";
    }

    public static String getMetadataDirPath() {
        return getODKDirPath() + "/metadata";
    }

    public static String getCacheDirPath() {
        return getODKDirPath() + "/.cache";
    }

    public static String getOfflineLayersDirPath() {
        return getODKDirPath() + "/layers";
    }

    public static String getSettingsDirPath() {
        return getODKDirPath() + "/settings";
    }

    private static boolean isScopedStorageUsed() {
        return GeneralSharedPreferences.getInstance().getBoolean(KEY_SCOPED_STORAGE_USED, false);
    }

    public static void markMigrationToScopedStorage() {
        GeneralSharedPreferences.getInstance().save(KEY_SCOPED_STORAGE_USED, true);
    }
}
