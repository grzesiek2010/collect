package org.odk.collect.android.utilities;

import android.os.Environment;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.preferences.GeneralSharedPreferences;

import java.io.File;

import timber.log.Timber;

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

    public static void createODKDirs() throws RuntimeException {
        if (!isScopedStorageUsed()) {
            String cardStatus = Environment.getExternalStorageState();
            if (!cardStatus.equals(Environment.MEDIA_MOUNTED)) {
                throw new RuntimeException(
                        Collect.getInstance().getString(R.string.sdcard_unmounted, cardStatus));
            }
        }

        for (String dirName : getODKDirs()) {
            File dir = new File(dirName);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    String message = Collect.getInstance().getString(R.string.cannot_create_directory, dirName);
                    Timber.w(message);
                    throw new RuntimeException(message);
                }
            } else {
                if (!dir.isDirectory()) {
                    String message = Collect.getInstance().getString(R.string.not_a_directory, dirName);
                    Timber.w(message);
                    throw new RuntimeException(message);
                }
            }
        }
    }

    private static String[] getODKDirs() {
        return new String[] {
                getODKDirPath(),
                getFormsDirPath(),
                getInstancesDirPath(),
                getCacheDirPath(),
                getMetadataDirPath(),
                getOfflineLayersDirPath(),
                getSettingsDirPath()
        };
    }
}
