package org.odk.collect.android.storagemigrator;

import androidx.lifecycle.LiveData;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.tasks.ServerPollingJob;
import org.odk.collect.android.upload.AutoSendWorker;
import org.odk.collect.android.utilities.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import timber.log.Timber;

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
        return StorageMigrationResult.MIGRATING_DATABASE_PATHS_SUCCEEDED;
    }

    private void deleteODKDirFromSdCard() {
        FileUtils.deleteDirectory(new File(Collect.ODK_ROOT));
    }
}
