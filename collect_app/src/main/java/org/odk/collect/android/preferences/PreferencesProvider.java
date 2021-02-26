package org.odk.collect.android.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PreferencesProvider {

    private static final String META_PREFS_NAME = "meta";
    private static final String ADMIN_PREFS_NAME = "admin_prefs";

    private final Context context;
    private final MetaPreferences metaPreferences;

    public PreferencesProvider(Context context) {
        this.context = context;
        this.metaPreferences = new MetaPreferences(context.getSharedPreferences(META_PREFS_NAME, Context.MODE_PRIVATE));
    }

    public MetaPreferences getMetaSharedPreferences() {
        return metaPreferences;
    }

    public SharedPreferences getGeneralSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public SharedPreferences getAdminSharedPreferences() {
        return context.getSharedPreferences(ADMIN_PREFS_NAME, Context.MODE_PRIVATE);
    }
}

