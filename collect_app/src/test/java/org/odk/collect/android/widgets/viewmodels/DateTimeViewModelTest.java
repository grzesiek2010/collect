package org.odk.collect.android.widgets.viewmodels;

import android.widget.DatePicker;
import android.widget.TimePicker;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;

import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.support.LiveDataTester;
import org.odk.collect.android.utilities.DateTimeUtils;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class DateTimeViewModelTest {
    private final LiveDataTester liveDataTester = new LiveDataTester();

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private DateTimeViewModel viewModel;
    LiveData<LocalDateTime> selectedDate;
    LiveData<DateTime> selectedTime;

    @Before
    public void setUp() {
        viewModel = new DateTimeViewModel();
        selectedDate = liveDataTester.activate(viewModel.getSelectedDate());
        selectedTime = liveDataTester.activate(viewModel.getSelectedTime());
    }

    @After
    public void teardown() {
        liveDataTester.teardown();
    }

    @Test
    public void setSelectedDate_updatesDateReturnedByViewModel() {
        viewModel.setSelectedDate(2012, 4, 12);
        assertThat(selectedDate.getValue(), is(DateTimeUtils.getSelectedDate(
                new LocalDateTime().withDate(2012, 5, 12), LocalDateTime.now())));
    }

    @Test
    public void setSelectedDateTime_updatesTimeReturnedByViewModel() {
        viewModel.setSelectedTime(12, 10);
        assertThat(selectedTime.getValue(), is(DateTimeUtils.getSelectedTime(
                new LocalDateTime().withTime(12, 10, 0, 0), LocalDateTime.now()).toDateTime()));
    }

    @Test
    public void updatingDateInDateSetListener_updatesDateReturnedByViewModel() {
        viewModel.getOnDateSetListener().onDateSet(mock(DatePicker.class), 2012, 4, 12);

        assertThat(selectedDate.getValue(), is(DateTimeUtils.getSelectedDate(
                new LocalDateTime().withDate(2012, 5, 12), LocalDateTime.now())));
    }

    @Test
    public void updatingTimeInTimeSetListener_updatesTimeReturnedByViewModel() {
        TimePicker timePicker = mock(TimePicker.class);
        when(timePicker.getCurrentHour()).thenReturn(12);
        when(timePicker.getCurrentMinute()).thenReturn(10);
        viewModel.getOnTimeSetListener().onTimeSet(timePicker, 0, 0);

        assertThat(selectedTime.getValue(), is(DateTimeUtils.getSelectedTime(
                new LocalDateTime().withTime(12, 10, 0, 0), LocalDateTime.now()).toDateTime()));
    }
}
