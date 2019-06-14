/*
 * Copyright 2019 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.odk.collect.android;

import org.odk.collect.android.activities.CollectAbstractActivity;
import org.odk.collect.android.fragments.dialogs.GoogleSheetsUploaderProgressDialog;
import org.odk.collect.android.listeners.InstanceUploaderListener;
import org.odk.collect.android.tasks.InstanceUploaderTask;

import static org.odk.collect.android.fragments.dialogs.GoogleSheetsUploaderProgressDialog.GOOGLE_SHEETS_UPLOADER_PROGRESS_DIALOG_TAG;

public abstract class UploaderActivity extends CollectAbstractActivity implements InstanceUploaderListener, GoogleSheetsUploaderProgressDialog.OnSendingFormsCanceledListener {

    protected InstanceUploaderTask uploaderTask;

    protected void dismissProgressDialog() {
        GoogleSheetsUploaderProgressDialog progressDialog = getProgressDialog();
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    protected GoogleSheetsUploaderProgressDialog getProgressDialog() {
        return (GoogleSheetsUploaderProgressDialog) getSupportFragmentManager().findFragmentByTag(GOOGLE_SHEETS_UPLOADER_PROGRESS_DIALOG_TAG);
    }

    @Override
    public void onSendingFormsCanceled() {
        uploaderTask.cancel(true);
        uploaderTask.setUploaderListener(null);
        finish();
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return uploaderTask;
    }
}
