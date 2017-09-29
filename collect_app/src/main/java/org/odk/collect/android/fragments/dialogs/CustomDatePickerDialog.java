package org.odk.collect.android.fragments.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.NumberPicker;

import org.joda.time.DateTime;
import org.odk.collect.android.R;

public abstract class CustomDatePickerDialog extends DialogFragment {
    public static final String DATE_PICKER_DIALOG = "datePickerDialog";

    protected static final String WIDGET_ID = "widgetId";
    protected static final String IS_VALUE_SELECTED = "isValueSelected";
    protected static final String DAY = "day";
    protected static final String MONTH = "month";
    protected static final String YEAR = "year";
    protected static final String HIDE_DAY = "hideDay";
    protected static final String HIDE_MONTH = "hideMonth";

    protected NumberPicker dayPicker;
    protected NumberPicker monthPicker;
    protected NumberPicker yearPicker;

    private int widgetId;
    protected int day;
    protected int month;
    protected int year;

    protected boolean isValueSelected;
    protected boolean hideDay;
    protected boolean hideMonth;

    public interface CustomDatePickerDialogListener {
        void onDateChanged(int widgetId, int day, int month, int year);
    }

    private CustomDatePickerDialogListener listener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (CustomDatePickerDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnHeadlineSelectedListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle savedInstanceStateToRead = savedInstanceState;
        if (savedInstanceStateToRead == null) {
            savedInstanceStateToRead = getArguments();
        }

        isValueSelected = savedInstanceStateToRead.getBoolean(IS_VALUE_SELECTED);
        widgetId = savedInstanceStateToRead.getInt(WIDGET_ID);
        day = savedInstanceStateToRead.getInt(DAY);
        month = savedInstanceStateToRead.getInt(MONTH);
        year = savedInstanceStateToRead.getInt(YEAR);
        hideDay = savedInstanceStateToRead.getBoolean(HIDE_DAY);
        hideMonth = savedInstanceStateToRead.getBoolean(HIDE_MONTH);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.select_date)
                .setView(R.layout.custom_date_picker_dialog)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        DateTime dateTime = getDateAsGregorian();
                        listener.onDateChanged(widgetId, dateTime.getDayOfMonth(), dateTime.getMonthOfYear(), dateTime.getYear());
                        dismiss();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dismiss();
                    }
                })
                .create();
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpPickers();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        DateTime dateTime = getDateAsGregorian();
        outState.putInt(WIDGET_ID, widgetId);
        outState.putBoolean(IS_VALUE_SELECTED, true);
        outState.putInt(DAY, dateTime.getDayOfMonth());
        outState.putInt(MONTH, dateTime.getMonthOfYear());
        outState.putInt(YEAR, dateTime.getYear());
        outState.putBoolean(HIDE_DAY, hideDay);
        outState.putBoolean(HIDE_MONTH, hideMonth);

        super.onSaveInstanceState(outState);
    }

    private void setUpPickers() {
        dayPicker = (NumberPicker) getDialog().findViewById(R.id.day_picker);
        dayPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                updateGregorianDateLabel();
            }
        });
        monthPicker = (NumberPicker) getDialog().findViewById(R.id.month_picker);
        monthPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                updateDays();
                updateGregorianDateLabel();
            }
        });
        yearPicker = (NumberPicker) getDialog().findViewById(R.id.year_picker);
        yearPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                updateDays();
                updateGregorianDateLabel();
            }
        });

        if (hideMonth) {
            dayPicker.setVisibility(View.GONE);
            monthPicker.setVisibility(View.GONE);
            dayPicker.setValue(0);
            monthPicker.setValue(0);
        }
    }

    protected static Bundle getArgs(int widgetId, boolean isValueSelected, int day, int month, int year, boolean hideDay, boolean hideMonth) {
        Bundle args = new Bundle();
        args.putInt(WIDGET_ID, widgetId);
        args.putBoolean(IS_VALUE_SELECTED, isValueSelected);
        args.putInt(DAY, day);
        args.putInt(MONTH, month);
        args.putInt(YEAR, year);
        args.putBoolean(HIDE_DAY, hideDay);
        args.putBoolean(HIDE_MONTH, hideMonth);

        return args;
    }

    protected abstract void updateDays();
    protected abstract void updateGregorianDateLabel();
    protected abstract DateTime getDateAsGregorian();
}
