package org.odk.collect.android.utilities;

import org.javarosa.core.model.utils.DateUtils;
import org.joda.time.LocalDateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.BuildConfig;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static junit.framework.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21, manifest = "src/main/AndroidManifest.xml",
        packageName = "org.odk.collect")
public class DateTest {

    private static final String EAT_TIME_ZONE = "Africa/Nairobi";

    private TimeZone mCurrentTimeZone;

    @Before
    public void setUp() {
        mCurrentTimeZone = TimeZone.getDefault();
    }

    @After
    public void tearDown() {
        TimeZone.setDefault(mCurrentTimeZone);
    }

    @Test
    public void testCurrentGetDateMethod() {
        TimeZone.setDefault(TimeZone.getTimeZone(EAT_TIME_ZONE));

        Date test = getDateUsingCurrentMethod();

        Calendar c = Calendar.getInstance();
        c.setTime(test);

        // We get 01-01-1960 as expected
        assertEquals(c.get(Calendar.YEAR), 1960);
        assertEquals(c.get(Calendar.MONTH), 0); // range [0-11]
        assertEquals(c.get(Calendar.DAY_OF_MONTH), 1);
    }

    @Test
    public void testNewGetDateMethod() {
        TimeZone.setDefault(TimeZone.getTimeZone(EAT_TIME_ZONE));

        Date test = getDateUsingNewMethod();

        Calendar c = Calendar.getInstance();
        c.setTime(test);

        // We get 01-01-1960 as expected
        assertEquals(c.get(Calendar.YEAR), 1960);
        assertEquals(c.get(Calendar.MONTH), 0); // range [0-11]
        assertEquals(c.get(Calendar.DAY_OF_MONTH), 1);
    }

    public static Date getDateUsingCurrentMethod2() {
        Calendar cd = Calendar.getInstance();
        cd.set(Calendar.YEAR, 1961);
        cd.set(Calendar.MONTH, 0);
        cd.set(Calendar.DAY_OF_MONTH, 1);
        cd.set(Calendar.HOUR_OF_DAY, 0);
        cd.set(Calendar.MINUTE, 0);
        cd.set(Calendar.SECOND, 0);
        cd.set(Calendar.MILLISECOND, 0);

        return cd.getTime();
    }

    public Date getDateUsingCurrentMethod() {
        return DateUtils.getDate(getDateFields(), null);
    }

    public Date getDateUsingNewMethod () {
        return getDateNewVersion(getDateFields(), null);
    }

    // This is my new version of {@link #DateUtils.getDate(DateFields, String)} method
    private Date getDateNewVersion(DateUtils.DateFields f, String timezone) {
        LocalDateTime ldt = new LocalDateTime()
                .withYear(f.year)
                .withMonthOfYear(f.month)
                .withDayOfMonth(f.day)
                .withHourOfDay(f.hour)
                .withMinuteOfHour(f.minute)
                .withSecondOfMinute(f.second)
                .withMillisOfSecond(f.secTicks);

        return timezone == null ? ldt.toDate() : ldt.toDate(TimeZone.getTimeZone(timezone));
    }

    private DateUtils.DateFields getDateFields() {
        // 01-01-1960 - there is a daylight saving gap at midnight in Nairobi timezone
        DateUtils.DateFields dateFields = new DateUtils.DateFields();
        dateFields.year = 1960;
        dateFields.month = 1;
        dateFields.day = 1;
        dateFields.hour = 0;
        dateFields.minute = 12;
        dateFields.second = 0;

        return dateFields;
    }
}