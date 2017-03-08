/*
 * Copyright 2017 Nafundi
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
package org.odk.collect.android.utilities;

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

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21, manifest = "src/main/AndroidManifest.xml",
        packageName = "org.odk.collect")
// https://github.com/opendatakit/collect/issues/356
public class DaylightSavingSmallTest {

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
        assertEquals(test.getYear(), 59);
        assertEquals(test.getMonth(), 11);
    }

    @Test
    public void testNewGetDateMethod() {
        TimeZone.setDefault(TimeZone.getTimeZone(EAT_TIME_ZONE));

        Date test = getDateUsingNewMethod();
        assertEquals(test.getYear(), 60);
        assertEquals(test.getMonth(), 0);
    }

    public static Date getDateUsingCurrentMethod () {
        Calendar cd = Calendar.getInstance();
        cd.set(Calendar.YEAR, 1960);
        cd.set(Calendar.MONTH, 0);
        cd.set(Calendar.DAY_OF_MONTH, 0);
        cd.set(Calendar.HOUR_OF_DAY, 0);
        cd.set(Calendar.MINUTE, 0);
        cd.set(Calendar.SECOND, 0);
        cd.set(Calendar.MILLISECOND, 0);

        return cd.getTime();
    }

    public static Date getDateUsingNewMethod () {
        LocalDateTime ldt = new LocalDateTime()
                .withYear(1960)
                .withMonthOfYear(1)
                .withDayOfMonth(1)
                .withHourOfDay(0)
                .withMinuteOfHour(0)
                .withSecondOfMinute(0)
                .withMillisOfSecond(0);

        return ldt.toDate();
    }
}
