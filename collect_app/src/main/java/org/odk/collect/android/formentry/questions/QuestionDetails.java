package org.odk.collect.android.formentry.questions;

import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.widgets.utilities.WidgetAnswerRepository;
import org.odk.collect.android.widgets.QuestionWidget;

/**
 * Data class representing a "question" for use with {@link QuestionWidget}
 * and its subclasses
 */
public class QuestionDetails {

    private final FormEntryPrompt prompt;
    private final WidgetAnswerRepository widgetAnswerRepository;
    private final String formAnalyticsID;
    private final boolean isReadOnly;

    public QuestionDetails(FormEntryPrompt prompt, String formAnalyticsID) {
        this(prompt, formAnalyticsID, false);
    }

    public QuestionDetails(FormEntryPrompt prompt, String formAnalyticsID, boolean readOnlyOverride) {
        this.prompt = prompt;
        this.widgetAnswerRepository = new WidgetAnswerRepository(prompt);
        this.formAnalyticsID = formAnalyticsID;
        this.isReadOnly = readOnlyOverride || prompt.isReadOnly();
    }

    public FormEntryPrompt getPrompt() {
        return prompt;
    }

    public WidgetAnswerRepository getWidgetAnswerRepository() {
        return  widgetAnswerRepository;
    }

    public String getFormAnalyticsID() {
        return formAnalyticsID;
    }

    public boolean isReadOnly() {
        return isReadOnly;
    }
}
