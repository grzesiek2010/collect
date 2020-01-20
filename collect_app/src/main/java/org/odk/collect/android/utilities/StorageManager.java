package org.odk.collect.android.utilities;

import android.os.Environment;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.osmdroid.config.Configuration;

import java.io.File;

import timber.log.Timber;

import static org.odk.collect.android.preferences.GeneralKeys.KEY_SCOPED_STORAGE_USED;

public class StorageManager {

    private StorageManager() {

    }

    public static String getStoragePath() {
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

    public static String[] getODKDirs() {
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

    public static String getTmpFilePath() {
        return getCacheDirPath() + File.separator + "tmp.jpg";
    }

    public static String getTmpDrawFilePath() {
        return getCacheDirPath() + File.separator + "tmpDraw.jpg";
    }

    public static String getQRCodeFilePath() {
        return getSettingsDirPath() + File.separator + "collect-settings.png";
    }

    public static boolean clearFormsDir() {
        return deleteFolderContents(getFormsDirPath());
    }

    public static boolean clearInstancesDir() {
        return deleteFolderContents(getInstancesDirPath());
    }

    public static boolean clearOfflineLayersDir() {
        return deleteFolderContents(getOfflineLayersDirPath());
    }

    public static boolean clearCacheDir() {
        return deleteFolderContents(getCacheDirPath());
    }

    public static boolean clearSettingsDir() {
        return deleteFolderContents(getSettingsDirPath());
    }

    public static boolean clearOSMDroidDir() {
        return deleteFolderContents(Configuration.getInstance().getOsmdroidTileCache().getPath());
    }

    private static boolean deleteFolderContents(String path) {
        boolean result = true;
        File file = new File(path);
        if (file.exists()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    result = deleteRecursive(f);
                }
            }
        }
        return result;
    }

    private static boolean deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
            }
        }
        return fileOrDirectory.delete();
    }

    public static void createMediaDir(String mediaDirName) {
        FileUtils.createFolder(getFormsDirPath() + File.separator + mediaDirName);
    }
}
