package org.odk.collect.android.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class PreferencesSourceTest {
    private static final String KEY_STRING = "keyString";
    private static final String KEY_BOOLEAN = "keyBoolean";
    private static final String KEY_LONG = "keyLong";
    private static final String KEY_INT = "keyInt";
    private static final String KEY_FLOAT = "keyFloat";
    private static final String KEY_SET = "keySet";


    private static final String DEFAULT_STRING_VALUE = "qwerty";
    private static final boolean DEFAULT_BOOLEAN_VALUE = true;
    private static final long DEFAULT_LONG_VALUE = 15L;
    private static final int DEFAULT_INT_VALUE = 23;
    private static final float DEFAULT_FLOAT_VALUE = 44.65f;
    private static final Set<String> DEFAULT_SET_VALUE = new HashSet<String>(){{
        add("a");
        add("b");
        add("c");
    }};

    private static final String CUSTOM_STRING_VALUE = "12345";
    private static final boolean CUSTOM_BOOLEAN_VALUE = false;
    private static final long CUSTOM_LONG_VALUE = 5L;
    private static final int CUSTOM_INT_VALUE = 3;
    private static final float CUSTOM_FLOAT_VALUE = 8.25f;
    private static final Set<String> CUSTOM_SET_VALUE = new HashSet<String>(){{
        add("x");
        add("y");
        add("z");
    }};

    private PreferencesSource preferencesSource;
    private SharedPreferences sharedPreferences;

    @Before
    public void setup() {
        sharedPreferences = spy(ApplicationProvider.getApplicationContext().getSharedPreferences("prefs", Context.MODE_PRIVATE));
        preferencesSource = new PreferencesSource(sharedPreferences, getDefaultPrefs());
    }

    @Test
    public void whenStringPrefNotSaved_shouldDefaultValueBeReturned() {
        assertThat(preferencesSource.getStringPref(KEY_STRING), is(DEFAULT_STRING_VALUE));
    }

    @Test
    public void whenStringPrefSaved_shouldProperValueBeReturned() {
        preferencesSource.save(KEY_STRING, CUSTOM_STRING_VALUE);
        assertThat(preferencesSource.getStringPref(KEY_STRING), is(CUSTOM_STRING_VALUE));
    }

    @Test
    public void whenBooleanPrefNotSaved_shouldDefaultValueBeReturned() {
        assertThat(preferencesSource.getBooleanPref(KEY_BOOLEAN), is(DEFAULT_BOOLEAN_VALUE));
    }

    @Test
    public void whenBooleanPrefSaved_shouldProperValueBeReturned() {
        preferencesSource.save(KEY_BOOLEAN, CUSTOM_BOOLEAN_VALUE);
        assertThat(preferencesSource.getBooleanPref(KEY_BOOLEAN), is(CUSTOM_BOOLEAN_VALUE));
    }

    @Test
    public void whenLongPrefNotSaved_shouldDefaultValueBeReturned() {
        assertThat(preferencesSource.getLongPref(KEY_LONG), is(DEFAULT_LONG_VALUE));
    }

    @Test
    public void whenLongPrefSaved_shouldProperValueBeReturned() {
        preferencesSource.save(KEY_LONG, CUSTOM_LONG_VALUE);
        assertThat(preferencesSource.getLongPref(KEY_LONG), is(CUSTOM_LONG_VALUE));
    }

    @Test
    public void whenIntPrefNotSaved_shouldDefaultValueBeReturned() {
        assertThat(preferencesSource.getIntPref(KEY_INT), is(DEFAULT_INT_VALUE));
    }

    @Test
    public void whenIntPrefSaved_shouldProperValueBeReturned() {
        preferencesSource.save(KEY_INT, CUSTOM_INT_VALUE);
        assertThat(preferencesSource.getIntPref(KEY_INT), is(CUSTOM_INT_VALUE));
    }

    @Test
    public void whenFloatPrefNotSaved_shouldDefaultValueBeReturned() {
        assertThat(preferencesSource.getFloatPref(KEY_FLOAT), is(DEFAULT_FLOAT_VALUE));
    }

    @Test
    public void whenFloatPrefSaved_shouldProperValueBeReturned() {
        preferencesSource.save(KEY_FLOAT, CUSTOM_FLOAT_VALUE);
        assertThat(preferencesSource.getFloatPref(KEY_FLOAT), is(CUSTOM_FLOAT_VALUE));
    }

    @Test
    public void whenSetPrefNotSaved_shouldDefaultValueBeReturned() {
         assertThat(preferencesSource.getSetOfStringsPref(KEY_SET), is(DEFAULT_SET_VALUE));
    }

    @Test
    public void whenSetPrefSaved_shouldProperValueBeReturned() {
        preferencesSource.save(KEY_SET, CUSTOM_SET_VALUE);
        assertThat(preferencesSource.getSetOfStringsPref(KEY_SET), is(CUSTOM_SET_VALUE));
    }

    @Test
    public void whenKeyIsNull_shouldNotSaveAnything() {
        preferencesSource.save(null, 5);
        verify(sharedPreferences, never()).edit();
    }

    @Test
    public void whenPreferencesAreReloaded_shouldDefaultValuesBeLoaded() {
        preferencesSource.save(KEY_STRING, CUSTOM_STRING_VALUE);
        preferencesSource.save(KEY_BOOLEAN, CUSTOM_BOOLEAN_VALUE);
        preferencesSource.save(KEY_LONG, CUSTOM_LONG_VALUE);
        preferencesSource.save(KEY_INT, CUSTOM_INT_VALUE);
        preferencesSource.save(KEY_FLOAT, CUSTOM_FLOAT_VALUE);
        preferencesSource.save(KEY_SET, CUSTOM_SET_VALUE);

        preferencesSource.resetPreferences();

        assertThat(preferencesSource.getStringPref(KEY_STRING), is(DEFAULT_STRING_VALUE));
        assertThat(preferencesSource.getBooleanPref(KEY_BOOLEAN), is(DEFAULT_BOOLEAN_VALUE));
        assertThat(preferencesSource.getLongPref(KEY_LONG), is(DEFAULT_LONG_VALUE));
        assertThat(preferencesSource.getIntPref(KEY_INT), is(DEFAULT_INT_VALUE));
        assertThat(preferencesSource.getFloatPref(KEY_FLOAT), is(DEFAULT_FLOAT_VALUE));
        assertThat(preferencesSource.getSetOfStringsPref(KEY_SET), is(DEFAULT_SET_VALUE));
    }

    @Test
    public void whenDefaultValuesNotPassed_shouldNotCrash() {
        preferencesSource = new PreferencesSource(sharedPreferences, null);

        assertThat(preferencesSource.getStringPref(KEY_STRING), is(""));
        assertThat(preferencesSource.getBooleanPref(KEY_BOOLEAN), is(false));
        assertThat(preferencesSource.getLongPref(KEY_LONG), is(0L));
        assertThat(preferencesSource.getIntPref(KEY_INT), is(0));
        assertThat(preferencesSource.getFloatPref(KEY_FLOAT), is(0f));
        assertThat(preferencesSource.getSetOfStringsPref(KEY_SET), is(Collections.emptySet()));
    }

    private Map<String, Object> getDefaultPrefs() {
        return new HashMap<String, Object>(){{
            put(KEY_STRING, DEFAULT_STRING_VALUE);
            put(KEY_BOOLEAN, DEFAULT_BOOLEAN_VALUE);
            put(KEY_LONG, DEFAULT_LONG_VALUE);
            put(KEY_INT, DEFAULT_INT_VALUE);
            put(KEY_FLOAT, DEFAULT_FLOAT_VALUE);
            put(KEY_SET, DEFAULT_SET_VALUE);
        }};
    }
}
