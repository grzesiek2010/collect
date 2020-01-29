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
                getRootOdkDirPath(),
                getFormsDirPath(),
                getInstancesDirPath(),
                getCacheDirPath(),
                getMetadataDirPath(),
                getOfflineLayersDirPath()
            };
    }

    private String getStoragePath() {
        return storageStateProvider.isScopedStorageUsed()
                ? getScopedExternalFilesDirPath()
                : getUnscopedExternalFilesDirPath();
    }

    String getScopedExternalFilesDirPath() {
        File primaryStorageFile = Collect.getInstance().getExternalFilesDir(null);
        if (primaryStorageFile != null) {
            return primaryStorageFile.getAbsolutePath();
        }
        return "";
    }

    String getUnscopedExternalFilesDirPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    public String getRootOdkDirPath() {
        return getStoragePath() + File.separator + "odk";
    }

    public String getFormsDirPath() {
        return getRootOdkDirPath() + File.separator + "forms";
    }

    public String getInstancesDirPath() {
        return getRootOdkDirPath() + File.separator + "instances";
    }

    public String getMetadataDirPath() {
        return getRootOdkDirPath() + File.separator + "metadata";
    }

    public String getCacheDirPath() {
        return getRootOdkDirPath() + File.separator + ".cache";
    }

    public String getOfflineLayersDirPath() {
        return getRootOdkDirPath() + File.separator + "layers";
    }

    public String getSettingsDirPath() {
        return getRootOdkDirPath() + File.separator + "settings";
    }

    public String getTmpFilePath() {
        return getCacheDirPath() + File.separator + "tmp.jpg";
    }

    public String getTmpDrawFilePath() {
        return getCacheDirPath() + File.separator + "tmpDraw.jpg";
    }

    // TODO the method should be removed once using Scoped storage became required
    public String getCacheDbPathFromRelativePath(String relativePath) {
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
    public String getFormDbPathFromRelativePath(String relativePath) {
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

    public String getInstanceDbPath(String path) {
        String absolutePath;
        String relativePath;
        if (path.startsWith(getInstancesDirPath())) {
            absolutePath = path;
            relativePath = getRelativeInstanceFilePath(path);
        } else {
            relativePath = path;
            absolutePath = getAbsoluteInstanceFilePath(path);
        }

        return storageStateProvider.isScopedStorageUsed()
                ? relativePath
                : absolutePath;
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
