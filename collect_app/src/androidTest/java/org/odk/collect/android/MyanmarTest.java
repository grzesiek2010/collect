package org.odk.collect.android;

import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Test;
import org.junit.runner.RunWith;

import mmcalendar.CalendarType;
import mmcalendar.Language;
import mmcalendar.LanguageCatalog;
import mmcalendar.MyanmarDate;
import mmcalendar.MyanmarDateConverter;
import mmcalendar.MyanmarDateKernel;
import mmcalendar.WesternDate;
import mmcalendar.WesternDateConverter;
import mmcalendar.WesternDateKernel;

@RunWith(AndroidJUnit4.class)
public class MyanmarTest {

    @Test
    public void test() {
        MyanmarDate myanmarDate = MyanmarDateConverter.convert(2018, 10, 24, 1, 1, 1);


        for (int i = 1; i <= 13 ; i++) {
            getMyanmarDate2(i);
        }
    }

    private MyanmarDate getMyanmarDate2(int month) {
        MyanmarDate myanmarDate = MyanmarDateKernel.j2m(MyanmarDateKernel.m2j(1380, month, 1));
        WesternDate westernDate = WesternDateConverter.convert(myanmarDate);
        LanguageCatalog languageCatalog = LanguageCatalog.getInstance();
        languageCatalog.setLanguage(Language.MYANMAR);
        Log.i("QWERTY", "Myanmar month: " + myanmarDate.getMonth() + "-" + myanmarDate.getMonthName(languageCatalog) + " | western month: " + westernDate.getMonth());

        return myanmarDate;
    }
}
