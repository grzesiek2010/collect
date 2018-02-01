/*
 * Copyright (C) 2012 University of Washington
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.widgets;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore.Images;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.DrawActivity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.ViewIds;

import java.io.File;

import timber.log.Timber;

import static org.odk.collect.android.utilities.ApplicationConstants.RequestCodes;

/**
 * Signature widget.
 *
 * @author BehrAtherton@gmail.com
 */
public class SignatureWidget extends BaseImageWidget {

    private Button signButton;

    public SignatureWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);

        errorTextView = new TextView(context);
        errorTextView.setId(ViewIds.generateViewId());
        errorTextView.setText(R.string.selected_invalid_image);

        signButton = getSimpleButton(getContext().getString(R.string.sign_button));
        signButton.setEnabled(!prompt.isReadOnly());

        // finish complex layout
        LinearLayout answerLayout = new LinearLayout(getContext());
        answerLayout.setOrientation(LinearLayout.VERTICAL);
        answerLayout.addView(signButton);
        answerLayout.addView(errorTextView);

        // and hide the sign button if read-only
        if (prompt.isReadOnly()) {
            signButton.setVisibility(View.GONE);
        }
        errorTextView.setVisibility(View.GONE);

        // retrieve answer from data model and update ui
        binaryName = prompt.getAnswerText();

        // Only add the imageView if the user has signed
        if (binaryName != null) {
            DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            int screenWidth = metrics.widthPixels;
            int screenHeight = metrics.heightPixels;

            File f = new File(getInstanceFolder() + File.separator + binaryName);

            Bitmap bmp = null;
            if (f.exists()) {
                bmp = FileUtils.getBitmapScaledToDisplay(f, screenHeight, screenWidth);
                if (bmp == null) {
                    errorTextView.setVisibility(View.VISIBLE);
                }
            }

            imageView = getAnswerImageView(bmp);
            answerLayout.addView(imageView);
        }
        addAnswerView(answerLayout);
    }

    @Override
    public void onImageClick() {
        Collect.getInstance().getActivityLogger().logInstanceAction(this, "viewImage",
                "click", getFormEntryPrompt().getIndex());
        launchSignatureActivity();
    }

    private void launchSignatureActivity() {
        errorTextView.setVisibility(View.GONE);
        Intent i = new Intent(getContext(), DrawActivity.class);
        i.putExtra(DrawActivity.OPTION, DrawActivity.OPTION_SIGNATURE);
        // copy...
        if (binaryName != null) {
            File f = new File(getInstanceFolder() + File.separator + binaryName);
            i.putExtra(DrawActivity.REF_IMAGE, Uri.fromFile(f));
        }
        i.putExtra(DrawActivity.EXTRA_OUTPUT,
                Uri.fromFile(new File(Collect.TMPFILE_PATH)));

        try {
            waitForData();
            ((Activity) getContext()).startActivityForResult(i,
                    RequestCodes.SIGNATURE_CAPTURE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getContext(),
                    getContext().getString(R.string.activity_not_found, "signature capture"),
                    Toast.LENGTH_SHORT).show();
            cancelWaitingForData();
        }
    }

    @Override
    public void clearAnswer() {
        super.clearAnswer();
        // reset buttons
        signButton.setText(getContext().getString(R.string.sign_button));
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        signButton.setOnLongClickListener(l);
        super.setOnLongClickListener(l);
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        signButton.cancelLongPress();
    }

    @Override
    public void onButtonClick(int buttonId) {
        Collect.getInstance()
                .getActivityLogger()
                .logInstanceAction(this, "signButton", "click",
                        getFormEntryPrompt().getIndex());
        launchSignatureActivity();
    }
}
