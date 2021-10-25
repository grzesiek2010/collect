package org.odk.collect.android.widgets;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.NonNull;

import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.databinding.ExArbitraryFileWidgetAnswerBinding;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.utilities.ApplicationConstants;
import org.odk.collect.android.utilities.ExternalAppIntentProvider;
import org.odk.collect.android.utilities.MediaUtils;
import org.odk.collect.android.utilities.QuestionMediaManager;
import org.odk.collect.android.widgets.utilities.WaitingForDataRegistry;
import org.odk.collect.androidshared.utils.ToastUtils;

@SuppressLint("ViewConstructor")
public class ExArbitraryFileWidget extends BaseArbitraryFileWidget {
    ExArbitraryFileWidgetAnswerBinding binding;

    private final ExternalAppIntentProvider externalAppIntentProvider;

    public ExArbitraryFileWidget(Context context, QuestionDetails questionDetails, @NonNull MediaUtils mediaUtils,
                                 QuestionMediaManager questionMediaManager, WaitingForDataRegistry waitingForDataRegistry,
                                 ExternalAppIntentProvider externalAppIntentProvider) {
        super(context, questionDetails, mediaUtils, questionMediaManager, waitingForDataRegistry);
        this.externalAppIntentProvider = externalAppIntentProvider;
    }

    @Override
    protected View onCreateAnswerView(Context context, FormEntryPrompt prompt, int answerFontSize) {
        binding = ExArbitraryFileWidgetAnswerBinding.inflate(((Activity) context).getLayoutInflater());
        setupAnswerFile(prompt.getAnswerText());

        binding.exArbitraryFileButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);
        binding.exArbitraryFileAnswerText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);

        if (questionDetails.isReadOnly()) {
            binding.exArbitraryFileButton.setVisibility(GONE);
        } else {
            binding.exArbitraryFileButton.setOnClickListener(v -> onButtonClick());
            binding.exArbitraryFileAnswerText.setOnClickListener(v -> mediaUtils.openFile(getContext(), answerFile, null));
        }

        if (answerFile != null) {
            binding.exArbitraryFileAnswerText.setText(answerFile.getName());
            binding.exArbitraryFileAnswerText.setVisibility(VISIBLE);
        }

        return binding.getRoot();
    }

    @Override
    public void clearAnswer() {
        binding.exArbitraryFileAnswerText.setVisibility(GONE);
        deleteFile();
        widgetValueChanged();
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener listener) {
        binding.exArbitraryFileButton.setOnLongClickListener(listener);
        binding.exArbitraryFileAnswerText.setOnLongClickListener(listener);
    }

    @Override
    protected void showAnswerText() {
        binding.exArbitraryFileAnswerText.setText(answerFile.getName());
        binding.exArbitraryFileAnswerText.setVisibility(VISIBLE);
    }

    @Override
    protected void hideAnswerText() {
        binding.exArbitraryFileAnswerText.setVisibility(GONE);
    }

    private void onButtonClick() {
        waitingForDataRegistry.waitForData(getFormEntryPrompt().getIndex());

        try {
            Intent intent = externalAppIntentProvider.getIntentToRunExternalApp(getFormEntryPrompt());
            intentLauncher.launchForResult((Activity) getContext(), intent, ApplicationConstants.RequestCodes.EX_ARBITRARY_FILE_CHOOSER, () -> {
                try {
                    Intent intentWithoutDefaultCategory = externalAppIntentProvider.getIntentToRunExternalAppWithoutDefaultCategory(getFormEntryPrompt(), Collect.getInstance().getPackageManager());
                    intentLauncher.launchForResult((Activity) getContext(), intentWithoutDefaultCategory, ApplicationConstants.RequestCodes.EX_ARBITRARY_FILE_CHOOSER, () -> {
                        final String errorString;
                        String v = getFormEntryPrompt().getSpecialFormQuestionText("noAppErrorString");
                        errorString = (v != null) ? v : getContext().getString(R.string.no_app);
                        ToastUtils.showLongToast(getContext(), errorString);
                        return null;
                    });
                } catch (Exception | Error e) {
                    ToastUtils.showLongToast(getContext(), e.getMessage());
                }
                return null;
            });
        } catch (Exception | Error e) {
            ToastUtils.showLongToast(getContext(), e.getMessage());
        }
    }
}
