package org.odk.collect.android.preferences;

import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import java.util.Set;

import static java.util.Collections.emptySet;

public class MetaPreferences {
    private final SharedPreferences sharedPreferences;

    public MetaPreferences(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    public void save(String key, @Nullable Object value) {
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

    public Set<String> getStringSet(String key) {
        return sharedPreferences.getStringSet(key, emptySet());
    }

    public boolean getBoolean(String key) {
        return sharedPreferences.getBoolean(key, false);
    }

    public void clear() {
        sharedPreferences.edit().clear().apply();
    }

    public SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }
}
