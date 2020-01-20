package org.odk.collect.android.storagemigrator;

import android.content.ContentValues;
import android.database.Cursor;

import androidx.lifecycle.LiveData;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.dao.FormsDao;
import org.odk.collect.android.dao.InstancesDao;
import org.odk.collect.android.dao.ItemsetDao;
import org.odk.collect.android.forms.Form;
import org.odk.collect.android.instances.Instance;
import org.odk.collect.android.itemsets.Itemset;
import org.odk.collect.android.tasks.ServerPollingJob;
import org.odk.collect.android.upload.AutoSendWorker;
import org.odk.collect.android.utilities.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import timber.log.Timber;

import static android.provider.BaseColumns._ID;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.FORM_FILE_PATH;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.FORM_MEDIA_PATH;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.JRCACHE_FILE_PATH;
import static org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH;

public class StorageMigrator {

    StorageMigrationResult performStorageMigration() {
        if (isFormUploaderRunning()) {
            return StorageMigrationResult.FORM_DOWNLOADER_IS_RUNNING;
        }
        if (isFormDownloaderRunning()) {
            return StorageMigrationResult.FORM_UPLOADER_IS_RUNNING;
        }
        if (!isEnoughSpaceOnInternalStorage()) {
            return StorageMigrationResult.NOT_ENOUGH_SPACE;
        }
        if (moveAppDataToInternalStorage() != StorageMigrationResult.MOVING_FILES_SUCCEEDED) {
            return StorageMigrationResult.MOVING_FILES_FAILED;
        }
        if (migrateDatabasePaths() != StorageMigrationResult.MIGRATING_DATABASE_PATHS_SUCCEEDED) {
            return StorageMigrationResult.MIGRATING_DATABASE_PATHS_FAILED;
        }
        deleteODKDirFromSdCard();

        return StorageMigrationResult.SUCCESS;
    }

    private boolean isFormUploaderRunning() {
        boolean result = false;
        LiveData<List<WorkInfo>> statuses = WorkManager.getInstance().getWorkInfosForUniqueWorkLiveData(AutoSendWorker.class.getName());

        if (statuses.getValue() != null) {
            for (WorkInfo status : statuses.getValue()) {
                if (status.getState().equals(WorkInfo.State.RUNNING)) {
                    result = true;
                }
            }
        }
        return result;
    }

    private boolean isFormDownloaderRunning() {
        return ServerPollingJob.isIsDownloadingForms();
    }

    private boolean isEnoughSpaceOnInternalStorage() {
        try {
            return FileUtils.getAvailableInternalMemorySize() > getODKFolderSize();
        } catch (Exception | Error e) {
            Timber.w(e);
            return false;
        }
    }

    private long getODKFolderSize() {
        return FileUtils.getFolderSize(new File(Collect.ODK_ROOT));
    }

    private StorageMigrationResult moveAppDataToInternalStorage() {
        try {
            org.apache.commons.io.FileUtils.copyDirectory(new File(Collect.ODK_ROOT), Collect.getInstance().getExternalFilesDir(null));
        } catch (FileNotFoundException e) {
            return StorageMigrationResult.MOVING_FILES_SUCCEEDED;
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

    private void deleteODKDirFromSdCard() {
        FileUtils.deleteDirectory(new File(Collect.ODK_ROOT));
    }

    private void migrateInstancesDatabase() {
        InstancesDao instancesDao = new InstancesDao();
        Cursor cursor = instancesDao.getInstancesCursor();
        if (cursor != null && cursor.moveToFirst()) {
            List<Instance> instances = instancesDao.getInstancesFromCursor(cursor);
            for (Instance instance : instances) {
                String where = _ID + "=?";
                String[] whereArgs = {String.valueOf(instance.getDatabaseId())};

                ContentValues values = instancesDao.getValuesFromInstanceObject(instance);
                values.put(INSTANCE_FILE_PATH, getRelativePathFromInstancePath(instance.getInstanceFilePath()));

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
                String where = _ID + "=?";
                String[] whereArgs = {String.valueOf(form.getId())};

                ContentValues values = formsDao.getValuesFromFormObject(form);
                values.put(FORM_MEDIA_PATH, getRelativePathFromFormPath(form.getFormMediaPath()));
                values.put(FORM_FILE_PATH, getRelativePathFromFormPath(form.getFormFilePath()));
                values.put(JRCACHE_FILE_PATH, getRelativePathFromCachePath(form.getJrCacheFilePath()));

                formsDao.updateForm(values, where, whereArgs);
            }
        }
        if (cursor != null) {
            cursor.close();
        }
    }

    private void migrateItemsetsDatabase() {
        ItemsetDao itemsetDao = new ItemsetDao();
        Cursor cursor = itemsetDao.getItemsets();
        if (cursor != null && cursor.moveToFirst()) {
            List<Itemset> itemsets = itemsetDao.getItemsetsFromCursor(cursor);
            for (Itemset itemset : itemsets) {
                String where = _ID + "=?";
                String[] whereArgs = {String.valueOf(itemset.getId())};

                ContentValues values = itemsetDao.getValuesFromFormObject(itemset);
                values.put(INSTANCE_FILE_PATH, getRelativePathFromFormPath(itemset.getPath()));

                itemsetDao.update(values, where, whereArgs);
            }
        }
        if (cursor != null) {
            cursor.close();
        }
    }

    private String getRelativePathFromInstancePath(String absoluteInstancePath) {
        return absoluteInstancePath.substring(Collect.INSTANCES_PATH.length() + 1);
    }

    private String getRelativePathFromFormPath(String absoluteFormPath) {
        return absoluteFormPath.substring(Collect.FORMS_PATH.length() + 1);
    }

    private String getRelativePathFromCachePath(String absoluteCachePath) {
        return absoluteCachePath.substring(Collect.CACHE_PATH.length() + 1);
    }
}
