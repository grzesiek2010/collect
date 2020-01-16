package org.odk.collect.android.storagemigrator;

public enum StorageMigrationResult {
    SUCCESS,
    FORM_UPLOADER_IS_RUNNING,
    FORM_DOWNLOADER_IS_RUNNING,
    NOT_ENOUGH_SPACE,
    MOVING_FILES_SUCCEEDED,
    MOVING_FILES_FAILED,
    MIGRATING_DATABASE_PATHS_SUCCEEDED,
    MIGRATING_DATABASE_PATHS_FAILED
}
