/*
 * Copyright 2018 Nafundi
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

package org.odk.collect.android.widgets;

import android.content.Context;
import android.support.annotation.Nullable;
import android.widget.ImageView;
import android.widget.TextView;

import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.utilities.MediaUtils;
import org.odk.collect.android.widgets.interfaces.FileWidget;

import java.io.File;

import timber.log.Timber;

public abstract class BaseImageWidget extends QuestionWidget implements FileWidget {
    @Nullable
    protected ImageView imageView;
    protected String binaryName;
    protected TextView errorTextView;

    public BaseImageWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);
    }

    @Override
    public void deleteFile() {
        // get the file path and delete the file
        String name = binaryName;
        // clean up variables
        binaryName = null;
        // delete from media provider
        int del = MediaUtils.deleteImageFileFromMediaProvider(
                getInstanceFolder() + File.separator + name);
        Timber.i("Deleted %d rows from media content provider", del);
    }

    protected abstract void onImageClick();
}
