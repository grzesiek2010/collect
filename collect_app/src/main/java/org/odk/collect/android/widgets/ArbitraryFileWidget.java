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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;

import android.util.TypedValue;
import android.view.View;

import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.databinding.ArbitraryFileWidgetAnswerBinding;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.utilities.ApplicationConstants;
import org.odk.collect.android.utilities.FileUtil;
import org.odk.collect.android.utilities.MediaUtils;
import org.odk.collect.android.utilities.QuestionMediaManager;
import org.odk.collect.android.widgets.utilities.WaitingForDataRegistry;

import java.io.File;

@SuppressLint("ViewConstructor")
public class ArbitraryFileWidget extends BaseArbitraryWidget {
    ArbitraryFileWidgetAnswerBinding binding;

    ArbitraryFileWidget(Context context, QuestionDetails questionDetails, @NonNull FileUtil fileUtil, @NonNull MediaUtils mediaUtils,
                        QuestionMediaManager questionMediaManager, WaitingForDataRegistry waitingForDataRegistry) {
        super(context, questionDetails, fileUtil, mediaUtils, questionMediaManager, waitingForDataRegistry);
    }

    @Override
    protected View onCreateAnswerView(Context context, FormEntryPrompt prompt, int answerFontSize) {
        binding = ArbitraryFileWidgetAnswerBinding.inflate(((Activity) context).getLayoutInflater());
        binaryName = prompt.getAnswerText();

        if (prompt.isReadOnly()) {
            binding.arbitraryFileButton.setVisibility(GONE);
        } else {
            binding.arbitraryFileButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);
            binding.arbitraryFileButton.setOnClickListener(v -> onButtonClick());
            binding.arbitraryFileAnswerText.setOnClickListener(v -> mediaUtils.openFile(getContext(), new File(getInstanceFolder() + File.separator + binaryName)));
        }

        if (binaryName != null && !binaryName.isEmpty()) {
            binding.arbitraryFileAnswerText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);
            binding.arbitraryFileAnswerText.setText(binaryName);
            binding.arbitraryFileAnswerText.setVisibility(VISIBLE);
        }

        return binding.getRoot();
    }

    @Override
    public void clearAnswer() {
        binding.arbitraryFileAnswerText.setVisibility(GONE);
        deleteFile();
        widgetValueChanged();
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        binding.arbitraryFileButton.setOnLongClickListener(l);
        binding.arbitraryFileAnswerText.setOnLongClickListener(l);
    }

    @Override
    protected void showAnswerText() {
        binding.arbitraryFileAnswerText.setText(binaryName);
        binding.arbitraryFileAnswerText.setVisibility(VISIBLE);
    }

    private void onButtonClick() {
        waitingForDataRegistry.waitForData(getFormEntryPrompt().getIndex());
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*"); // all file types
        ((Activity) getContext()).startActivityForResult(intent, ApplicationConstants.RequestCodes.ARBITRARY_FILE_CHOOSER);
    }
}
