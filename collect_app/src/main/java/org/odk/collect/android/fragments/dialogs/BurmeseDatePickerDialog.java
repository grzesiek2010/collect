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

package org.odk.collect.android.fragments.dialogs;

import org.javarosa.core.model.FormIndex;
import org.odk.collect.android.R;
import org.odk.collect.android.logic.DatePickerDetails;
import org.odk.collect.android.utilities.DateTimeUtils;

import org.joda.time.LocalDateTime;

import java.util.Arrays;

import mmcalendar.MyanmarDate;
import mmcalendar.MyanmarDateConverter;
import mmcalendar.MyanmarDateKernel;
import mmcalendar.WesternDate;
import mmcalendar.WesternDateConverter;

public class BurmeseDatePickerDialog extends CustomDatePickerDialog {
    private static final int MIN_SUPPORTED_YEAR = 1261; //1900 in Gregorian calendar
    private static final int MAX_SUPPORTED_YEAR = 1461; //2100 in Gregorian calendar

    private String[] monthsArray ;

    public static BurmeseDatePickerDialog newInstance(FormIndex formIndex, LocalDateTime date, DatePickerDetails datePickerDetails) {
        BurmeseDatePickerDialog dialog = new BurmeseDatePickerDialog();
        dialog.setArguments(getArgs(formIndex, date, datePickerDetails));

        return dialog;
    }

    @Override
    public void onResume() {
        super.onResume();
        monthsArray = getResources().getStringArray(R.array.burmese_months);
        setUpValues();
    }

    @Override
    protected void updateDays() {
        MyanmarDate myanmarDate = getCurrentBurmeseDate();
        setUpDayPicker(myanmarDate.getMonthDay(), myanmarDate.getMonthLength());
    }

    @Override
    protected LocalDateTime getOriginalDate() {
        WesternDate westernDate = WesternDateConverter.convert(getCurrentBurmeseDate());
        return new LocalDateTime().withYear(westernDate.getYear()).withMonthOfYear(westernDate.getMonth()).withDayOfMonth(westernDate.getDay());
    }

    private void setUpDatePicker() {
        LocalDateTime localDateTime = DateTimeUtils
                .skipDaylightSavingGapIfExists(getDate())
                .toDateTime()
                .toLocalDateTime();

        MyanmarDate myanmarDate = MyanmarDateConverter.convert(localDateTime.getYear(),
                localDateTime.getMonthOfYear(), localDateTime.getDayOfMonth(), localDateTime.getHourOfDay(),
                localDateTime.getMinuteOfHour(), localDateTime.getSecondOfMinute());

        setUpDayPicker(myanmarDate.getMonthDay(), myanmarDate.getMonthLength());
        setUpMonthPicker(myanmarDate.getMonth(), monthsArray);
        setUpYearPicker(myanmarDate.getYearInt(), MIN_SUPPORTED_YEAR, MAX_SUPPORTED_YEAR);
    }

    private void setUpValues() {
        setUpDatePicker();
        updateGregorianDateLabel();
    }

    private MyanmarDate getCurrentBurmeseDate() {
        double myanmarMonth = Arrays.asList(monthsArray).indexOf(getMonth()) % 12;
        return MyanmarDateKernel.j2m(MyanmarDateKernel.m2j(getYear(), myanmarMonth, Math.floor(myanmarMonth / 13), 0, getDay()));
    }
}
