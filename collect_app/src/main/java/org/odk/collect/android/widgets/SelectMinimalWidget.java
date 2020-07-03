package org.odk.collect.android.widgets;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.databinding.SelectMinimalWidgetAnswerBinding;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.fragments.dialogs.SelectMinimalDialog;

public class SelectMinimalWidget extends AbstractSelectOneWidget {

    private SelectMinimalWidgetAnswerBinding binding;

    public SelectMinimalWidget(Context context, QuestionDetails questionDetails, boolean autoAdvance) {
        super(context, questionDetails, autoAdvance);
    }

    @Override
    protected View onCreateAnswerView(Context context, FormEntryPrompt prompt, int answerFontSize) {
        binding = SelectMinimalWidgetAnswerBinding.inflate(((Activity) context).getLayoutInflater());
        binding.button.setOnClickListener(v -> {
            SelectMinimalDialog selectMinimalDialog = new SelectMinimalDialog(adapter, prompt);
            selectMinimalDialog.show(((FormEntryActivity) getContext()).getSupportFragmentManager(), "SelectMinimalDialog");
        });

        return binding.getRoot();
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {

    }

    @Override
    public IAnswerData getAnswer() {
        return null;
    }

    @Override
    public void clearAnswer() {

    }
}
