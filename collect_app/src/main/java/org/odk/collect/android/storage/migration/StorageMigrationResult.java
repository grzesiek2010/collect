package org.odk.collect.android.storage.migration;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;

import org.odk.collect.android.R;

public enum StorageMigrationResult {
    SUCCESS(1),
    FORM_UPLOADER_IS_RUNNING(2),
    FORM_DOWNLOADER_IS_RUNNING(3),
    NOT_ENOUGH_SPACE(4),
    MOVING_FILES_FAILED(5);

    private int resultCode;

    StorageMigrationResult(int resultCode) {
        this.resultCode = resultCode;
    }

    public int getResultCode() {
        return resultCode;
    }

    public static StorageMigrationResult getResult(Integer resultCode) {
        switch (resultCode) {
            case 1:
                return SUCCESS;
            case 2:
                return FORM_UPLOADER_IS_RUNNING;
            case 3:
                return FORM_DOWNLOADER_IS_RUNNING;
            case 4:
                return NOT_ENOUGH_SPACE;
            case 5:
                return MOVING_FILES_FAILED;
            default:
                return null;
        }
    }

    public static Spanned getBannerText(StorageMigrationResult result, Context context) {
        String bannerText;
        if (result == null) {
            bannerText = context.getString(R.string.scoped_storage_banner_text);
        } else {
            bannerText = result == SUCCESS
                    ? context.getString(R.string.storage_migration_completed)
                    : context.getString(R.string.scoped_storage_banner_text)
                    + "<br><br>"
                    + "<font color='red'>"
                    + context.getString(R.string.last_attempt_failed)
                    + " "
                    + getResultMessage(result, context)
                    + "</font>";
        }
        return Html.fromHtml(bannerText);
    }

    private static String getResultMessage(StorageMigrationResult result, Context context) {
        String message = null;
        switch (result) {
            case SUCCESS:
                message = context.getString(R.string.storage_migration_completed);
                break;
            case NOT_ENOUGH_SPACE:
                message = context.getString(R.string.storage_migration_not_enough_space);
                break;
            case FORM_UPLOADER_IS_RUNNING:
                message = context.getString(R.string.storage_migration_form_uploader_is_running);
                break;
            case FORM_DOWNLOADER_IS_RUNNING:
                message = context.getString(R.string.storage_migration_form_downloader_is_running);
                break;
            case MOVING_FILES_FAILED:
                message = context.getString(R.string.storage_migration_failed);
                break;
        }
        return message;
    }
}