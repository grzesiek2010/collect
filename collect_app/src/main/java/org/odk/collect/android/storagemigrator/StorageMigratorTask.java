package org.odk.collect.android.storagemigrator;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.utilities.ToastUtils;

import java.lang.ref.WeakReference;

import javax.inject.Inject;

public class StorageMigratorTask extends AsyncTask<Void, Void, StorageMigrationResult> {

    private ProgressDialog progressDialog;
    private WeakReference<Context> contextRef;

    @Inject
    StorageMigrator storageMigrator;

    public StorageMigratorTask(Context context) {
        Collect.getInstance().getComponent().inject(this);
        this.contextRef = new WeakReference<>(context);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        showProgressDialog();
    }

    @Override
    protected StorageMigrationResult doInBackground(Void... voids) {
        return storageMigrator.performStorageMigration();
    }

    @Override
    protected void onPostExecute(StorageMigrationResult result) {
        super.onPostExecute(result);
        dismissProgressDialog();
        handleStorageMigrationResult(result);
    }

    private void showProgressDialog() {
        progressDialog = new ProgressDialog(contextRef.get());
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage(contextRef.get().getString(R.string.files_migration));
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void dismissProgressDialog() {
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void handleStorageMigrationResult(StorageMigrationResult result) {
        String message = null;
        switch (result) {
            case SUCCESS:
                message = contextRef.get().getString(R.string.files_migration_completed);
                break;
            case NOT_ENOUGH_SPACE:
                message = contextRef.get().getString(R.string.files_migration_not_enough_space);
                break;
            case FORM_UPLOADER_IS_RUNNING:
                message = contextRef.get().getString(R.string.files_migration_form_uploader_is_running);
                break;
            case FORM_DOWNLOADER_IS_RUNNING:
                message = contextRef.get().getString(R.string.files_migration_form_downloader_is_running);
                break;
            case MOVING_FILES_FAILED:
            case MIGRATING_DATABASE_PATHS_FAILED:
                message = contextRef.get().getString(R.string.files_migration_failed);
                break;
        }

        ToastUtils.showLongToast(message);
    }
}
