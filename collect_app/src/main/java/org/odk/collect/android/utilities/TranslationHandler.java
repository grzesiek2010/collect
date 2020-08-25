package org.odk.collect.android.utilities;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;

import org.odk.collect.android.application.Collect;

import java.util.Locale;

public class TranslationHandler {
    public static String getString(int stringId) {
        return getLocalizedResources(Collect.getInstance()).getString(stringId);
    }

    // The application context will give us the system's locale
    private static Resources getLocalizedResources(Context context) {
        Configuration conf = context.getResources().getConfiguration();
        conf = new Configuration(conf);
        conf.setLocale(new Locale(LocaleHelper.getLocaleCode(context)));
        Context localizedContext = context.createConfigurationContext(conf);
        return localizedContext.getResources();
    }
}
