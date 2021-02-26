package org.odk.collect.android.preferences;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PreferencesSource {
    private final SharedPreferences sharedPreferences;
    private final Map<String, Object> defaultPreferences;

    public PreferencesSource(@NonNull SharedPreferences sharedPreferences, Map<String, Object> defaultPreferences) {
        this.sharedPreferences = sharedPreferences;
        this.defaultPreferences = defaultPreferences != null ? defaultPreferences : new HashMap<>();
    }

    public void save(String key, Object value) {
        if (key == null) {
            return;
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (value == null || value instanceof String) {
            editor.putString(key, (String) value);
        } else if (value instanceof Boolean) {
            editor.putBoolean(key, (Boolean) value);
        } else if (value instanceof Long) {
            editor.putLong(key, (Long) value);
        } else if (value instanceof Integer) {
            editor.putInt(key, (Integer) value);
        } else if (value instanceof Float) {
            editor.putFloat(key, (Float) value);
        } else if (value instanceof Set) {
            editor.putStringSet(key, (Set<String>) value);
        } else {
            throw new RuntimeException("Unhandled preference value type: " + value);
        }
        editor.apply();
    }

    public void resetPreferences() {
        sharedPreferences.edit().clear().apply();

        for (Map.Entry<String, ?> prefs : defaultPreferences.entrySet()) {
            save(prefs.getKey(), prefs.getValue());
        }
    }

    public String getStringPref(String key) {
        String defaultValue = (String) defaultPreferences.getOrDefault(key, "");
        return sharedPreferences.getString(key, defaultValue);
    }

    public boolean getBooleanPref(String key) {
        boolean defaultValue = (boolean) defaultPreferences.getOrDefault(key, false);
        return sharedPreferences.getBoolean(key, defaultValue);
    }

    public long getLongPref(String key) {
        long defaultValue = (long) defaultPreferences.getOrDefault(key, 0L);
        return sharedPreferences.getLong(key, defaultValue);
    }

    public int getIntPref(String key) {
        int defaultValue = (int) defaultPreferences.getOrDefault(key, 0);
        return sharedPreferences.getInt(key, defaultValue);
    }

    public float getFloatPref(String key) {
        float defaultValue = (float) defaultPreferences.getOrDefault(key, 0f);
        return sharedPreferences.getFloat(key, defaultValue);
    }

    public Set<String> getSetOfStringsPref(String key) {
        Set<String> defaultValue = (Set<String>) defaultPreferences.getOrDefault(key, Collections.emptySet());
        return sharedPreferences.getStringSet(key, defaultValue);
    }
}
