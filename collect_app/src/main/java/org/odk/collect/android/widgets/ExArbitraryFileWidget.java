package org.odk.collect.android.widgets;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.NonNull;

import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.databinding.ExArbitraryFileWidgetAnswerBinding;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.utilities.ActivityAvailability;
import org.odk.collect.android.utilities.ApplicationConstants;
import org.odk.collect.android.utilities.ExternalAppIntentProvider;
import org.odk.collect.android.utilities.FileUtil;
import org.odk.collect.android.utilities.MediaUtils;
import org.odk.collect.android.utilities.QuestionMediaManager;
import org.odk.collect.android.utilities.ToastUtils;
import org.odk.collect.android.widgets.utilities.WaitingForDataRegistry;

import java.io.File;

import timber.log.Timber;

@SuppressLint("ViewConstructor")
public class ExArbitraryFileWidget extends BaseArbitraryWidget {
    ExArbitraryFileWidgetAnswerBinding binding;
    private static final String URI_KEY = "uri_data";

    public ActivityAvailability activityAvailability = new ActivityAvailability(getContext());

    public ExArbitraryFileWidget(Context context, QuestionDetails questionDetails, @NonNull FileUtil fileUtil, @NonNull MediaUtils mediaUtils,
                                 QuestionMediaManager questionMediaManager, WaitingForDataRegistry waitingForDataRegistry) {
        super(context, questionDetails, fileUtil, mediaUtils, questionMediaManager, waitingForDataRegistry);
    }

    @Override
    protected View onCreateAnswerView(Context context, FormEntryPrompt prompt, int answerFontSize) {
        binding = ExArbitraryFileWidgetAnswerBinding.inflate(((Activity) context).getLayoutInflater());
        binaryName = prompt.getAnswerText();

        if (prompt.isReadOnly()) {
            binding.exArbitraryFileButton.setVisibility(GONE);
        } else {
            binding.exArbitraryFileButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);
            binding.exArbitraryFileButton.setOnClickListener(v -> onButtonClick());
            binding.exArbitraryFileAnswerText.setOnClickListener(v -> mediaUtils.openFile(getContext(), new File(getInstanceFolder() + File.separator + binaryName)));
        }

        if (binaryName != null && !binaryName.isEmpty()) {
            binding.exArbitraryFileAnswerText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);
            binding.exArbitraryFileAnswerText.setText(binaryName);
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
    public void setOnLongClickListener(OnLongClickListener l) {
        binding.exArbitraryFileButton.setOnLongClickListener(l);
        binding.exArbitraryFileAnswerText.setOnLongClickListener(l);
    }

    @Override
    protected void showAnswerText() {
        binding.exArbitraryFileAnswerText.setText(binaryName);
        binding.exArbitraryFileAnswerText.setVisibility(VISIBLE);
    }

    private void onButtonClick() {
        waitingForDataRegistry.waitForData(getFormEntryPrompt().getIndex());
        ExternalAppIntentProvider externalAppIntentProvider = new ExternalAppIntentProvider(activityAvailability);
        externalAppIntentProvider.provideIntentToRunExternalApp(new ExternalAppIntentProvider.OnIntentProvided() {
            @Override
            public void onException(String message) {
            }

            @Override
            public void onSuccess(Intent intent) {
                fireActivity(intent);
            }
        }, getContext(), getFormEntryPrompt());
    }

    private void fireActivity(Intent i) throws ActivityNotFoundException {
        try {
            ((Activity) getContext()).startActivityForResult(i, ApplicationConstants.RequestCodes.EX_STRING_CAPTURE);
        } catch (SecurityException e) {
            Timber.i(e);
            ToastUtils.showLongToast(R.string.not_granted_permission);
        }
    }
}
