package org.odk.collect.android.widgets;

import android.content.Context;

import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.fragments.dialogs.PersianDatePickerDialog;

import static org.odk.collect.android.fragments.dialogs.CustomDatePickerDialog.DATE_PICKER_DIALOG;

public class PersianDateWidget extends AbstractDateWidget {

    public PersianDateWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);
    }

    protected void showDatePickerDialog() {
        PersianDatePickerDialog persianDatePickerDialog = PersianDatePickerDialog.newInstance(getFormEntryPrompt().getIndex(), date, datePickerDetails);
        persianDatePickerDialog.show(((FormEntryActivity) getContext()).getSupportFragmentManager(), DATE_PICKER_DIALOG);
    }
}
