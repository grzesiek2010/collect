package org.odk.collect.android.storage;

import android.os.Environment;

import org.odk.collect.android.application.Collect;

import java.io.File;

import javax.inject.Inject;

public class StoragePathProvider {

    @Inject
    StorageStateProvider storageStateProvider;

    public StoragePathProvider() {
        Collect.getComponent().inject(this);
    }

    public String[] getODKDirPaths() {
        return new String[]{
                getMainODKDirPath(),
                getFormsDirPath(),
                getInstancesDirPath(),
                getCacheDirPath(),
                getMetadataDirPath(),
                getOfflineLayersDirPath()
            };
    }

    private String getStoragePath() {
        return storageStateProvider.isScopedStorageUsed()
                ? getPrimaryExternalStorageFilePath()
                : getSecondaryExternalStorageFilePath();
    }

    String getPrimaryExternalStorageFilePath() {
        File primaryStorageFile = Collect.getInstance().getExternalFilesDir(null);
        if (primaryStorageFile != null) {
            return primaryStorageFile.getAbsolutePath();
        }
        return "";
    }

    String getSecondaryExternalStorageFilePath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    public String getMainODKDirPath() {
        return getStoragePath() + File.separator + "odk";
    }

    public String getFormsDirPath() {
        return getMainODKDirPath() + File.separator + "forms";
    }

    public String getInstancesDirPath() {
        return getMainODKDirPath() + File.separator + "instances";
    }

    public String getMetadataDirPath() {
        return getMainODKDirPath() + File.separator + "metadata";
    }

    public String getCacheDirPath() {
        return getMainODKDirPath() + File.separator + ".cache";
    }

    public String getOfflineLayersDirPath() {
        return getMainODKDirPath() + File.separator + "layers";
    }

    public String getSettingsDirPath() {
        return getMainODKDirPath() + File.separator + "settings";
    }

    public String getTmpFilePath() {
        return getCacheDirPath() + File.separator + "tmp.jpg";
    }

    public String getTmpDrawFilePath() {
        return getCacheDirPath() + File.separator + "tmpDraw.jpg";
    }

    // TODO the method should be removed once using Scoped storage became required
    public String getCacheFilePathToStoreInDatabaseBasingOnRelativePath(String relativePath) {
        return storageStateProvider.isScopedStorageUsed()
                ? relativePath
                : getCacheDirPath() + File.separator + relativePath;
    }

    public String getAbsoluteCacheFilePath(String filePath) {
        if (filePath == null) {
            return null;
        }
        return filePath.startsWith(getCacheDirPath())
                ? filePath
                : getCacheDirPath() + File.separator + filePath;
    }

    // TODO the method should be removed once using Scoped storage became required
    public String getFormFilePathToStoreInDatabaseBasingOnRelativePath(String relativePath) {
        return storageStateProvider.isScopedStorageUsed()
                ? relativePath
                : getFormsDirPath() + File.separator + relativePath;
    }

    // TODO the method should be removed once using Scoped storage became required
    public String getRelativeFormFilePath(String filePath) {
        return filePath.startsWith(getFormsDirPath())
                ? filePath.substring(getFormsDirPath().length() + 1)
                : filePath;
    }

    public String getAbsoluteFormFilePath(String filePath) {
        if (filePath == null) {
            return null;
        }
        return filePath.startsWith(getFormsDirPath())
                ? filePath
                : getFormsDirPath() + File.separator + filePath;
    }

    // TODO the method should be removed once using Scoped storage became required
    public String getInstanceFilePathToStoreInDatabaseBasingOnRelativePath(String relativePath) {
        return storageStateProvider.isScopedStorageUsed()
                ? relativePath
                : getInstancesDirPath() + File.separator + relativePath;
    }

    public String getAbsoluteInstanceFilePath(String filePath) {
        if (filePath == null) {
            return null;
        }
        return filePath.startsWith(getInstancesDirPath())
                ? filePath
                : getInstancesDirPath() + File.separator + filePath;
    }

    public String getRelativeInstanceFilePath(String filePath) {
        return filePath.startsWith(getInstancesDirPath())
                ? filePath.substring(getInstancesDirPath().length() + 1)
                : filePath;
    }
}
