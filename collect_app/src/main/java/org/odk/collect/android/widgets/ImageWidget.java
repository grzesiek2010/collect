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

import static org.odk.collect.android.formentry.questions.WidgetViewUtils.createSimpleButton;
import static org.odk.collect.android.utilities.ApplicationConstants.RequestCodes;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;

import org.odk.collect.android.BuildConfig;
import org.odk.collect.android.R;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.formentry.questions.WidgetViewUtils;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.storage.StorageSubdirectory;
import org.odk.collect.android.utilities.Appearances;
import org.odk.collect.android.utilities.ContentUriProvider;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.QuestionMediaManager;
import org.odk.collect.android.widgets.interfaces.ButtonClickListener;
import org.odk.collect.android.widgets.utilities.WaitingForDataRegistry;
import org.odk.collect.androidshared.system.CameraUtils;
import org.odk.collect.selfiecamera.CaptureSelfieActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

/**
 * Widget that allows user to take pictures, sounds or video and add them to the form.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */

@SuppressLint("ViewConstructor")
public class ImageWidget extends BaseImageWidget implements ButtonClickListener {

    Button captureButton;
    Button chooseButton;

    private boolean selfie;

    public ImageWidget(Context context, final QuestionDetails prompt, QuestionMediaManager questionMediaManager, WaitingForDataRegistry waitingForDataRegistry, String tmpImageFilePath) {
        super(context, prompt, questionMediaManager, waitingForDataRegistry, tmpImageFilePath);
        render();

        imageClickHandler = new ViewImageClickHandler();
        imageCaptureHandler = new ImageCaptureHandler();
        setUpLayout();
        updateAnswer();
        addAnswerView(answerLayout, WidgetViewUtils.getStandardMargin(context));
    }

    @Override
    protected void setUpLayout() {
        super.setUpLayout();

        String appearance = getFormEntryPrompt().getAppearanceHint();
        selfie = Appearances.isFrontCameraAppearance(getFormEntryPrompt());

        captureButton = createSimpleButton(getContext(), R.id.capture_image, questionDetails.isReadOnly(), getContext().getString(org.odk.collect.strings.R.string.capture_image), getAnswerFontSize(), this);

        chooseButton = createSimpleButton(getContext(), R.id.choose_image, questionDetails.isReadOnly(), getContext().getString(org.odk.collect.strings.R.string.choose_image), getAnswerFontSize(), this);

        answerLayout.addView(captureButton);
        answerLayout.addView(chooseButton);
        answerLayout.addView(errorTextView);
        answerLayout.addView(imageView);

        hideButtonsIfNeeded(appearance);
    }

    @Override
    public Intent addExtrasToIntent(Intent intent) {
        return intent;
    }

    @Override
    protected boolean doesSupportDefaultValues() {
        return false;
    }

    @Override
    public void clearAnswer() {
        super.clearAnswer();
        // reset buttons
        captureButton.setText(getContext().getString(org.odk.collect.strings.R.string.capture_image));
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
        if (buttonId == R.id.capture_image) {
            getPermissionsProvider().requestCameraPermission((Activity) getContext(), this::captureImage);
        } else if (buttonId == R.id.choose_image) {
            imageCaptureHandler.chooseImage(org.odk.collect.strings.R.string.choose_image);
        }
    }

    private void hideButtonsIfNeeded(String appearance) {
        if (selfie || ((appearance != null
                && appearance.toLowerCase(Locale.ENGLISH).contains(Appearances.NEW)))) {
            chooseButton.setVisibility(View.GONE);
        }
    }

    private void captureImage() {
        if (selfie && new CameraUtils().isFrontCameraAvailable(getContext())) {
            Intent intent = new Intent(getContext(), CaptureSelfieActivity.class);
            intent.putExtra(CaptureSelfieActivity.EXTRA_TMP_PATH, new StoragePathProvider().getOdkDirPath(StorageSubdirectory.CACHE));
            imageCaptureHandler.captureImage(intent, RequestCodes.MEDIA_FILE_PATH, org.odk.collect.strings.R.string.capture_image);
        } else {
            List<Intent> allIntents = new ArrayList();
            PackageManager packageManager = getContext().getPackageManager();

            // Add default camera app
            Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            List<ResolveInfo> cameraApps = packageManager.queryIntentActivities(captureIntent, 0);
            ResolveInfo res = cameraApps.get(0);

            Intent defaultCameraAppIntent = new Intent(captureIntent);
            defaultCameraAppIntent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            defaultCameraAppIntent.setPackage(res.activityInfo.packageName);

            // Add other camera apps
            for (String packageName : SPECIFIC_CAMERA_APPS) {
                Intent specificCameraAppIntent = new Intent(captureIntent);
                specificCameraAppIntent.setPackage(packageName);
                try {
                    Uri uri = new ContentUriProvider().getUriForFile(getContext(),
                            BuildConfig.APPLICATION_ID + ".provider",
                            new File(tmpImageFilePath));
                    // if this gets modified, the onActivityResult in
                    // FormEntyActivity will also need to be updated.
                    specificCameraAppIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, uri);
                    FileUtils.grantFilePermissions(specificCameraAppIntent, uri, getContext());
                } catch (IllegalArgumentException e) {
                    Timber.e(e);
                }
                allIntents.add(specificCameraAppIntent);
            }

            Intent chooserIntent = Intent.createChooser(defaultCameraAppIntent, "Select camera");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, allIntents.toArray(new Parcelable[allIntents.size()]));
            launchActivityForResult(chooserIntent, RequestCodes.IMAGE_CAPTURE, org.odk.collect.strings.R.string.capture_image);
        }
    }

    private static final String[] SPECIFIC_CAMERA_APPS =  new String[]{
            "net.sourceforge.opencamera",
            "com.jeyluta.timestampcamerafree"
    };
}
