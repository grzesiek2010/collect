package org.odk.collect.android.widgets.items;

import android.content.Context;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.helper.Selection;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.fragments.dialogs.SelectMinimalDialog;
import org.odk.collect.android.fragments.dialogs.SelectOneMinimalDialog;
import org.odk.collect.android.listeners.AdvanceToNextListener;
import org.odk.collect.android.utilities.StringUtils;
import org.odk.collect.android.utilities.WidgetAppearanceUtils;

import java.util.List;

public class SelectOneMinimalWidget extends SelectMinimalWidget {
    private Selection selectedItem;
    private final boolean autoAdvance;
    private AdvanceToNextListener autoAdvanceListener;

    public SelectOneMinimalWidget(Context context, QuestionDetails prompt, boolean autoAdvance) {
        super(context, prompt);
        selectedItem = getQuestionDetails().getPrompt().getAnswerValue() == null
                ? null
                : ((Selection) getQuestionDetails().getPrompt().getAnswerValue().getValue());
        this.autoAdvance = autoAdvance;
        if (context instanceof AdvanceToNextListener) {
            autoAdvanceListener = (AdvanceToNextListener) context;
        }
        updateAnswer();
    }

    @Override
    protected void showDialog() {
        SelectOneMinimalDialog dialog = new SelectOneMinimalDialog(getSavedSelectedValue(), WidgetAppearanceUtils.isFlexAppearance(getFormEntryPrompt()), WidgetAppearanceUtils.isAutocomplete(getFormEntryPrompt()), getChoicesRecyclerViewProps());
        dialog.show(((FormEntryActivity) getContext()).getSupportFragmentManager(), SelectMinimalDialog.class.getName());
    }

    @Override
    public IAnswerData getAnswer() {
        return selectedItem == null
                ? null
                : new SelectOneData(selectedItem);
    }

    @Override
    public void clearAnswer() {
        selectedItem = null;
        super.clearAnswer();
    }

    @Override
    public void setBinaryData(Object answer) {
        List<Selection> answers = (List<Selection>) answer;
        selectedItem = answers.isEmpty() ? null : answers.get(0);
        updateAnswer();
        widgetValueChanged();

        if (autoAdvance && autoAdvanceListener != null) {
            autoAdvanceListener.advance();
        }
    }

    @Override
    public void setChoiceSelected(int choiceIndex, boolean isSelected) {
        selectedItem = isSelected
                ? items.get(choiceIndex).selection()
                : null;
    }

    private void updateAnswer() {
        if (selectedItem == null) {
            binding.answer.setText(R.string.select_answer);
        } else {
            binding.answer.setText(StringUtils.textToHtml(getFormEntryPrompt().getSelectItemText(selectedItem)));
        }
    }

    private String getSavedSelectedValue() {
        return selectedItem == null
                ? null
                : selectedItem.getValue();
    }
}
