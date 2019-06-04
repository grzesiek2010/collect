package org.odk.collect.android.fragments.dialogs;

import org.javarosa.core.model.FormIndex;
import org.joda.time.LocalDateTime;
import org.joda.time.chrono.PersianChronologyKhayyamBorkowski;
import org.odk.collect.android.R;
import org.odk.collect.android.logic.DatePickerDetails;
import org.odk.collect.android.utilities.DateTimeUtils;

import java.util.Arrays;

public class PersianDatePickerDialog extends CustomDatePickerDialog {
    private static final int MIN_SUPPORTED_YEAR = 1278; //1900 in Gregorian calendar
    private static final int MAX_SUPPORTED_YEAR = 1478; //2100 in Gregorian calendar

    private String[] monthsArray;

    public static PersianDatePickerDialog newInstance(FormIndex formIndex, LocalDateTime date, DatePickerDetails datePickerDetails) {
        PersianDatePickerDialog dialog = new PersianDatePickerDialog();
        dialog.setArguments(getArgs(formIndex, date, datePickerDetails));

        return dialog;
    }

    @Override
    public void onResume() {
        super.onResume();
        monthsArray = getResources().getStringArray(R.array.persian_months);
        setUpValues();
    }

    @Override
    protected void updateDays() {
        LocalDateTime localDateTime = getCurrentPersianDate();
        setUpDayPicker(localDateTime.getDayOfMonth(), localDateTime.dayOfMonth().getMaximumValue());
    }

    @Override
    protected LocalDateTime getOriginalDate() {
        return getCurrentPersianDate();
    }

    private void setUpDatePicker() {
        LocalDateTime persianDate = DateTimeUtils
                .skipDaylightSavingGapIfExists(getDate())
                .toDateTime()
                .withChronology(PersianChronologyKhayyamBorkowski.getInstance())
                .toLocalDateTime();
        setUpDayPicker(persianDate.getDayOfMonth(), persianDate.dayOfMonth().getMaximumValue());
        setUpMonthPicker(persianDate.getMonthOfYear(), monthsArray);
        setUpYearPicker(persianDate.getYear(), MIN_SUPPORTED_YEAR, MAX_SUPPORTED_YEAR);
    }

    private void setUpValues() {
        setUpDatePicker();
        updateGregorianDateLabel();
    }

    private LocalDateTime getCurrentPersianDate() {
        int persianDay = getDay();
        int persianMonth = Arrays.asList(monthsArray).indexOf(getMonth());
        int persianYear = getYear();

        LocalDateTime persianDate = new LocalDateTime(persianYear, persianMonth + 1, 1, 0, 0, 0, 0, PersianChronologyKhayyamBorkowski.getInstance());
        if (persianDay > persianDate.dayOfMonth().getMaximumValue()) {
            persianDay = persianDate.dayOfMonth().getMaximumValue();
        }

        return new LocalDateTime(persianYear, persianMonth + 1, persianDay, 0, 0, 0, 0, PersianChronologyKhayyamBorkowski.getInstance());
    }
}
