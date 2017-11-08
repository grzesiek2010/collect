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

package org.odk.collect.android;

import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.preferences.AdminSharedPreferences;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.preferences.PreferenceKeys;
import org.odk.collect.android.utilities.SharedPreferencesUtils;

import java.util.HashMap;

import static junit.framework.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class LoadDefaultSharedPreferencesTest {

    @Test
    public void generalSharedPreferencesTest() {
        GeneralSharedPreferences.getInstance().loadDefaultValues();
        HashMap<String, Object> defaultValues = PreferenceKeys.GENERAL_KEYS;

        for (String key : SharedPreferencesUtils.getAllGeneralKeys()) {
            Log.i("TESTGP", key + " = " + GeneralSharedPreferences.getInstance().get(key) + " : " + defaultValues.get(key));
            assertEquals(GeneralSharedPreferences.getInstance().get(key), defaultValues.get(key));
        }
    }

    @Test
    public void adminSharedPreferencesTest() {
        AdminSharedPreferences.getInstance().loadDefaultValues();

        for (String key : SharedPreferencesUtils.getAllAdminKeys()) {
            Log.i("TESTAP", key + " = " + AdminSharedPreferences.getInstance().get(key) + " : " + AdminSharedPreferences.getInstance().getDefault(key));
            assertEquals(AdminSharedPreferences.getInstance().get(key), AdminSharedPreferences.getInstance().getDefault(key));
        }
    }
}