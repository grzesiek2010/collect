/*
 * Copyright (C) 2009 University of Washington
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
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.CaptureSelfieActivity;
import org.odk.collect.android.activities.CaptureSelfieActivityNewApi;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.MediaUtils;

import java.io.File;

import timber.log.Timber;

import static org.odk.collect.android.utilities.ApplicationConstants.RequestCodes;

/**
 * Widget that allows user to take pictures, sounds or video and add them to the form.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class ImageWidget extends BaseImageWidget {

    private Button captureButton;
    private Button chooseButton;

    private boolean selfie;

    public ImageWidget(Context context, final FormEntryPrompt prompt, final boolean selfie) {
        super(context, prompt);
        this.selfie = selfie;
    }

    @Override
    public void onImageClick() {
        Collect.getInstance().getActivityLogger().logInstanceAction(this, "viewButton",
                "click", getFormEntryPrompt().getIndex());
        Intent i = new Intent("android.intent.action.VIEW");
        Uri uri = MediaUtils.getImageUriFromMediaProvider(
                getInstanceFolder() + File.separator + binaryName);
        if (uri != null) {
            Timber.i("setting view path to: %s", uri.toString());
            i.setDataAndType(uri, "image/*");
            try {
                getContext().startActivity(i);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(getContext(),
                        getContext().getString(R.string.activity_not_found,
                                "view image"),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void setUpLayout() {
        super.setUpLayout();
        captureButton = getSimpleButton(getContext().getString(R.string.capture_image), R.id.capture_image);
        captureButton.setEnabled(!getFormEntryPrompt().isReadOnly());

        chooseButton = getSimpleButton(getContext().getString(R.string.choose_image), R.id.choose_image);
        chooseButton.setEnabled(!getFormEntryPrompt().isReadOnly());

        answerLayout.addView(captureButton);
        answerLayout.addView(chooseButton);
        answerLayout.addView(errorTextView);

        // and hide the capture and choose button if read-only
        if (getFormEntryPrompt().isReadOnly()) {
            captureButton.setVisibility(View.GONE);
            chooseButton.setVisibility(View.GONE);
        } else if (selfie) {
            chooseButton.setVisibility(View.GONE);
        }
        errorTextView.setVisibility(View.GONE);


        if (selfie) {
            boolean isFrontCameraAvailable;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                isFrontCameraAvailable = CaptureSelfieActivityNewApi.isFrontCameraAvailable();
            } else {
                isFrontCameraAvailable = CaptureSelfieActivity.isFrontCameraAvailable();
            }

            if (!isFrontCameraAvailable) {
                captureButton.setEnabled(false);
                errorTextView.setText(R.string.error_front_camera_unavailable);
                errorTextView.setVisibility(View.VISIBLE);
            }

        }
    }

    @Override
    protected void setUpBinary() {
        // Only add the imageView if the user has taken a picture
        if (binaryName != null) {
            DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
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
    }

    @Override
    public void clearAnswer() {
        super.clearAnswer();
        // reset buttons
        captureButton.setText(getContext().getString(R.string.capture_image));
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        captureButton.setOnLongClickListener(l);
        chooseButton.setOnLongClickListener(l);
        super.setOnLongClickListener(l);
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        captureButton.cancelLongPress();
        chooseButton.cancelLongPress();
    }

    @Override
    public void onButtonClick(int buttonId) {
        switch (buttonId) {
            case R.id.capture_image:
                captureImage();
                break;
            case R.id.choose_image:
                chooseImage();
                break;
        }
    }

    private void captureImage() {
        Collect.getInstance().getActivityLogger().logInstanceAction(this, "captureButton",
                "click", getFormEntryPrompt().getIndex());
        errorTextView.setVisibility(View.GONE);
        Intent i;
        if (selfie) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                i = new Intent(getContext(), CaptureSelfieActivityNewApi.class);
            } else {
                i = new Intent(getContext(), CaptureSelfieActivity.class);
            }
        } else {
            i = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            // We give the camera an absolute filename/path where to put the
            // picture because of bug:
            // http://code.google.com/p/android/issues/detail?id=1480
            // The bug appears to be fixed in Android 2.0+, but as of feb 2,
            // 2010, G1 phones only run 1.6. Without specifying the path the
            // images returned by the camera in 1.6 (and earlier) are ~1/4
            // the size. boo.

            // if this gets modified, the onActivityResult in
            // FormEntyActivity will also need to be updated.
            i.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,
                    Uri.fromFile(new File(Collect.TMPFILE_PATH)));
        }
        try {
            waitForData();
            ((Activity) getContext()).startActivityForResult(i,
                    RequestCodes.IMAGE_CAPTURE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getContext(),
                    getContext().getString(R.string.activity_not_found, "image capture"),
                    Toast.LENGTH_SHORT).show();
            cancelWaitingForData();
        }
    }

    private void chooseImage() {
        Collect.getInstance().getActivityLogger().logInstanceAction(this, "chooseButton",
                "click", getFormEntryPrompt().getIndex());
        errorTextView.setVisibility(View.GONE);
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("image/*");

        try {
            waitForData();
            ((Activity) getContext()).startActivityForResult(i,
                    RequestCodes.IMAGE_CHOOSER);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getContext(),
                    getContext().getString(R.string.activity_not_found, "choose image"),
                    Toast.LENGTH_SHORT).show();
            cancelWaitingForData();
        }
    }
}
