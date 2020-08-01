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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.TypedValue;
import android.view.View;

import org.javarosa.core.model.data.DateTimeData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryPrompt;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.databinding.DateTimeWidgetAnswerBinding;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.widgets.interfaces.BinaryDataReceiver;
import org.odk.collect.android.logic.DatePickerDetails;
import org.odk.collect.android.utilities.DateTimeUtils;
import org.odk.collect.android.widgets.utilities.DateTimeWidgetUtils;

/**
 * Displays a DatePicker widget. DateWidget handles leap years and does not allow dates that do not
 * exist.
 */
@SuppressLint("ViewConstructor")
public class DateTimeWidget extends QuestionWidget implements BinaryDataReceiver {
    DateTimeWidgetAnswerBinding binding;

    private LocalDateTime selectedDate;
    private DatePickerDetails datePickerDetails;
    private DateTime selectedTime;

    public DateTimeWidget(Context context, QuestionDetails prompt) {
        super(context, prompt);
    }

    @Override
    protected View onCreateAnswerView(Context context, FormEntryPrompt prompt, int answerFontSize) {
        binding = DateTimeWidgetAnswerBinding.inflate(((Activity) context).getLayoutInflater());
        datePickerDetails = DateTimeUtils.getDatePickerDetails(prompt.getQuestion().getAppearanceAttr());

        if (prompt.isReadOnly()) {
            binding.dateWidget.widgetButton.setVisibility(GONE);
            binding.timeWidget.widgetButton.setVisibility(GONE);
        } else {
            binding.dateWidget.widgetButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);
            binding.timeWidget.widgetButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);

            binding.dateWidget.widgetButton.setText(getContext().getString(R.string.select_date));
            binding.timeWidget.widgetButton.setText(getContext().getString(R.string.select_time));

            binding.dateWidget.widgetButton.setOnClickListener(v -> {
                DateTimeWidgetUtils.setWidgetWaitingForData(prompt.getIndex());
                DateTimeWidgetUtils.showDatePickerDialog((FormEntryActivity) context, prompt.getIndex(), datePickerDetails, selectedDate);
            });
            binding.timeWidget.widgetButton.setOnClickListener(v -> {
                DateTimeWidgetUtils.setWidgetWaitingForData(prompt.getIndex());
                DateTimeWidgetUtils.showTimePickerDialog((FormEntryActivity) getContext(), selectedTime);
            });
        }
        binding.dateWidget.widgetAnswerText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);
        binding.timeWidget.widgetAnswerText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);

        if (getFormEntryPrompt().getAnswerValue() == null) {
            resetAnswerFields();
        } else {
            selectedDate = new LocalDateTime(getFormEntryPrompt().getAnswerValue().getValue());
            binding.dateWidget.widgetAnswerText.setText(DateTimeUtils.getDateTimeLabel(
                    selectedDate.toDate(), datePickerDetails, false, context));

            selectedTime = new DateTime(getFormEntryPrompt().getAnswerValue().getValue());
            binding.timeWidget.widgetAnswerText.setText(DateTimeWidgetUtils.getTimeData(selectedTime).getDisplayText());
        }

        return binding.getRoot();
    }

    @Override
    public IAnswerData getAnswer() {
        if (isNullValue()) {
            return null;
        } else {
            LocalDateTime ldt;
            if (isTimeNull()) {
                ldt = DateTimeUtils.getLocalDateTime(selectedDate.getYear(),
                        selectedDate.getMonthOfYear(), selectedDate.getDayOfMonth(), 0, 0);
                selectedTime = DateTime.now();
            } else if (isDateNull()) {
                ldt = new LocalDateTime()
                        .withHourOfDay(selectedTime.getHourOfDay())
                        .withMinuteOfHour(selectedTime.getMinuteOfHour());

                selectedDate = DateTimeWidgetUtils.getCurrentDate();
            } else {
                ldt = DateTimeUtils.getLocalDateTime(selectedDate.getYear(), selectedDate.getMonthOfYear(),
                        selectedDate.getDayOfMonth(), selectedTime.getHourOfDay(), selectedTime.getMinuteOfHour());
            }
            return new DateTimeData(ldt.toDate());
        }
    }

    @Override
    public void clearAnswer() {
        resetAnswerFields();
        widgetValueChanged();
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        binding.dateWidget.widgetButton.setOnLongClickListener(l);
        binding.dateWidget.widgetAnswerText.setOnLongClickListener(l);

        binding.timeWidget.widgetButton.setOnLongClickListener(l);
        binding.timeWidget.widgetAnswerText.setOnLongClickListener(l);
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        binding.dateWidget.widgetButton.cancelLongPress();
        binding.dateWidget.widgetAnswerText.cancelLongPress();

        binding.timeWidget.widgetButton.cancelLongPress();
        binding.timeWidget.widgetAnswerText.cancelLongPress();
    }

    @Override
    public void setData(Object answer) {
        if (answer instanceof LocalDateTime) {
            selectedDate = (LocalDateTime) answer;
            binding.dateWidget.widgetAnswerText.setText(DateTimeUtils.getDateTimeLabel(
                    selectedDate.toDate(), datePickerDetails, false, getContext()));

        } else if (answer instanceof DateTime) {
            selectedTime = (DateTime) answer;
            binding.timeWidget.widgetAnswerText.setText(DateTimeWidgetUtils.getTimeData(selectedTime).getDisplayText());
        }
    }

    private void resetAnswerFields() {
        binding.dateWidget.widgetAnswerText.setText(R.string.no_date_selected);
        selectedDate = DateTimeWidgetUtils.getCurrentDate();

        binding.timeWidget.widgetAnswerText.setText(R.string.no_time_selected);
        selectedTime = DateTime.now();
    }

    private boolean isNullValue() {
        return getFormEntryPrompt().isRequired()
                ? isDateNull() || isTimeNull()
                : isDateNull() && isTimeNull();
    }

    private boolean isDateNull() {
        return binding.dateWidget.widgetAnswerText.getText().equals(getContext().getString(R.string.no_date_selected));
    }

    private boolean isTimeNull() {
        return binding.timeWidget.widgetAnswerText.getText().equals(getContext().getString(R.string.no_time_selected));
    }
}
