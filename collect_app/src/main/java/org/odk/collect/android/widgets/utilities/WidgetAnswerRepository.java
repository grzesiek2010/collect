package org.odk.collect.android.widgets.utilities;

import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.form.api.FormEntryPrompt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WidgetAnswerRepository {
    private final FormEntryPrompt formEntryPrompt;

    public WidgetAnswerRepository(FormEntryPrompt formEntryPrompt) {
        this.formEntryPrompt = formEntryPrompt;
    }

    public List<Selection> getSelectMultiAnswer() {
        if (formEntryPrompt.getAnswerValue() == null) {
            return new ArrayList<>();
        } else if (formEntryPrompt.getAnswerValue().getValue() instanceof Selection) {
            // This shouldn't happen https://github.com/getodk/collect/issues/4282
            return Collections.singletonList((Selection) formEntryPrompt.getAnswerValue().getValue());
        } else {
            return  (List<Selection>) formEntryPrompt.getAnswerValue().getValue();
        }
    }
}
