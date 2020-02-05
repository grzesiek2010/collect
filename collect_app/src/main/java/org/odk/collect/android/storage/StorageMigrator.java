package org.odk.collect.android.storage;

import android.content.ContentValues;
import android.database.Cursor;

import androidx.lifecycle.LiveData;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import org.apache.commons.io.FileUtils;
import org.odk.collect.android.dao.FormsDao;
import org.odk.collect.android.dao.InstancesDao;
import org.odk.collect.android.dao.ItemsetDao;
import org.odk.collect.android.database.ItemsetDbAdapter;
import org.odk.collect.android.forms.Form;
import org.odk.collect.android.instances.Instance;
import org.odk.collect.android.itemsets.Itemset;
import org.odk.collect.android.tasks.ServerPollingJob;
import org.odk.collect.android.upload.AutoSendWorker;

import java.io.File;
import java.util.List;

import timber.log.Timber;

import static android.provider.BaseColumns._ID;
import static org.odk.collect.android.database.ItemsetDbAdapter.KEY_PATH;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.FORM_FILE_PATH;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.FORM_MEDIA_PATH;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.JRCACHE_FILE_PATH;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH;

public class StorageMigrator {

    private StoragePathProvider storagePathProvider;
    private StorageStateProvider storageStateProvider;

    public StorageMigrator() {
        this(new StoragePathProvider(), new StorageStateProvider());
    }

    public StorageMigrator(StoragePathProvider storagePathProvider, StorageStateProvider storageStateProvider) {
        this.storagePathProvider = storagePathProvider;
        this.storageStateProvider = storageStateProvider;
    }

    StorageMigrationResult performStorageMigration() {
        storageStateProvider.clearOdkDirOnScopedStorage();

        if (isFormUploaderRunning()) {
            return StorageMigrationResult.FORM_UPLOADER_IS_RUNNING;
        }
        if (isFormDownloaderRunning()) {
            return StorageMigrationResult.FORM_DOWNLOADER_IS_RUNNING;
        }
        if (!storageStateProvider.isEnoughSpaceToPerformMigartion()) {
            return StorageMigrationResult.NOT_ENOUGH_SPACE;
        }
        if (moveAppDataToInternalStorage() != StorageMigrationResult.MOVING_FILES_SUCCEEDED) {
            return StorageMigrationResult.MOVING_FILES_FAILED;
        }
        storageStateProvider.enableUsingScopedStorage();
        if (migrateDatabasePaths() != StorageMigrationResult.MIGRATING_DATABASE_PATHS_SUCCEEDED) {
            storageStateProvider.disableUsingScopedStorage();
            return StorageMigrationResult.MIGRATING_DATABASE_PATHS_FAILED;
        }

        storageStateProvider.deleteOdkDirFromUnscopedStorage();

        return StorageMigrationResult.SUCCESS;
    }

    private boolean isFormUploaderRunning() {
        LiveData<List<WorkInfo>> statuses = WorkManager.getInstance().getWorkInfosForUniqueWorkLiveData(AutoSendWorker.class.getName());

        if (statuses.getValue() != null) {
            for (WorkInfo status : statuses.getValue()) {
                if (status.getState().equals(WorkInfo.State.RUNNING)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isFormDownloaderRunning() {
        return ServerPollingJob.isIsDownloadingForms();
    }

    private StorageMigrationResult moveAppDataToInternalStorage() {
        try {
            File odkDirOnUnscopedStorage = new File(storagePathProvider.getUnscopedExternalFilesDirPath() + File.separator + StorageSubdirectory.ODK.getDirectoryName());
            File odkDirOnScopedStorage = new File(storagePathProvider.getScopedExternalFilesDirPath() + File.separator + StorageSubdirectory.ODK.getDirectoryName());
            FileUtils.copyDirectory(odkDirOnUnscopedStorage, odkDirOnScopedStorage);
        } catch (Exception | Error e) {
            Timber.w(e);
            return StorageMigrationResult.MOVING_FILES_FAILED;
        }
        return StorageMigrationResult.MOVING_FILES_SUCCEEDED;
    }

    private StorageMigrationResult migrateDatabasePaths() {
        try {
            migrateFormsDatabase();
            migrateInstancesDatabase();
            migrateItemsetsDatabase();
        } catch (Exception | Error e) {
            Timber.w(e);
            return StorageMigrationResult.MIGRATING_DATABASE_PATHS_FAILED;
        }
        return StorageMigrationResult.MIGRATING_DATABASE_PATHS_SUCCEEDED;
    }

    private void migrateInstancesDatabase() {
        InstancesDao instancesDao = new InstancesDao();
        Cursor cursor = instancesDao.getInstancesCursor();
        if (cursor != null && cursor.moveToFirst()) {
            List<Instance> instances = instancesDao.getInstancesFromCursor(cursor);
            for (Instance instance : instances) {
                ContentValues values = instancesDao.getValuesFromInstanceObject(instance);
                values.put(INSTANCE_FILE_PATH, getRelativeInstanceDbPath(instance.getInstanceFilePath()));

                String where = _ID + "=?";
                String[] whereArgs = {String.valueOf(instance.getDatabaseId())};
                instancesDao.updateInstance(values, where, whereArgs);
            }
        }
        if (cursor != null) {
            cursor.close();
        }
    }

    private void migrateFormsDatabase() {
        FormsDao formsDao = new FormsDao();
        Cursor cursor = formsDao.getFormsCursor();
        if (cursor != null && cursor.moveToFirst()) {
            List<Form> forms = formsDao.getFormsFromCursor(cursor);
            for (Form form : forms) {
                ContentValues values = formsDao.getValuesFromFormObject(form);
                values.put(FORM_MEDIA_PATH, getRelativeFormDbPath(form.getFormMediaPath()));
                values.put(FORM_FILE_PATH, getRelativeFormDbPath(form.getFormFilePath()));
                values.put(JRCACHE_FILE_PATH, getRelativeCacheDbPath(form.getJrCacheFilePath()));

                String where = _ID + "=?";
                String[] whereArgs = {String.valueOf(form.getId())};
                formsDao.updateForm(values, where, whereArgs);
            }
        }
        if (cursor != null) {
            cursor.close();
        }
    }

    private void migrateItemsetsDatabase() {
        ItemsetDao itemsetDao = new ItemsetDao();
        ItemsetDbAdapter adapter = new ItemsetDbAdapter();
        adapter.open();
        Cursor cursor = adapter.getItemsets();
        if (cursor != null && cursor.moveToFirst()) {
            List<Itemset> itemsets = itemsetDao.getItemsetsFromCursor(cursor);
            for (Itemset itemset : itemsets) {
                ContentValues values = itemsetDao.getValuesFromFormObject(itemset);
                values.put(KEY_PATH, getRelativeFormDbPath(itemset.getPath()));

                String where = _ID + "=?";
                String[] whereArgs = {String.valueOf(itemset.getId())};
                itemsetDao.update(values, where, whereArgs);
            }
        }
        if (cursor != null) {
            cursor.close();
            adapter.close();
        }
    }

    private String getRelativeFormDbPath(String path) {
        return storagePathProvider.getRelativeFilePath(storagePathProvider.getUnscopedStorageDirPath(StorageSubdirectory.FORMS), path);
    }

    private String getRelativeInstanceDbPath(String path) {
        return storagePathProvider.getRelativeFilePath(storagePathProvider.getUnscopedStorageDirPath(StorageSubdirectory.INSTANCES), path);
    }

    private String getRelativeCacheDbPath(String path) {
        return storagePathProvider.getRelativeFilePath(storagePathProvider.getUnscopedStorageDirPath(StorageSubdirectory.CACHE), path);
    }
}