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
package org.odk.collect.android.integration;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.preferences.PreferencesActivity;
import org.odk.collect.android.provider.FormsProviderAPI;
import org.odk.collect.android.provider.InstanceProviderAPI;
import org.odk.collect.android.utilities.ResetUtility;
import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class ResetAppStateTestCase {

    @Before
    public void setUp() throws IOException {
        setupTestSettings();
        saveTestFormFiles();
        setupTestFormsDatabase();
        saveTestInstanceFiles();
        setupTestInstancesDatabase();
        createTestItemsetsDatabaseFile();
        saveTestLayerFiles();
        saveTestCacheFiles();
        saveTestOSMDroidFiles();
    }

    @Test
    public void resetAppState() {
        List<Integer> resetActions = Arrays.asList(
                ResetUtility.ResetAction.RESET_PREFERENCES, ResetUtility.ResetAction.RESET_INSTANCES,
                ResetUtility.ResetAction.RESET_FORMS, ResetUtility.ResetAction.RESET_LAYERS,
                ResetUtility.ResetAction.RESET_CACHE, ResetUtility.ResetAction.RESET_OSM_DROID
        );

        List<Integer> failedResetActions = new ResetUtility().reset(Collect.getInstance(), resetActions);
        assertEquals(0, failedResetActions.size());

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(Collect.getInstance());
        assertEquals(null, settings.getString(PreferencesActivity.KEY_USERNAME, null));
        assertEquals(null, settings.getString(PreferencesActivity.KEY_PASSWORD, null));

        assertEquals(0, getFormsCount());
        assertEquals(0, getInstancesCount());
    }

    private void setupTestSettings() {
        String username = "usernameTest";
        String password = "passwordTest";
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(Collect.getInstance());
        settings
                .edit()
                .putString(PreferencesActivity.KEY_USERNAME, username)
                .putString(PreferencesActivity.KEY_PASSWORD, password)
                .apply();

        assertEquals(username, settings.getString(PreferencesActivity.KEY_USERNAME, null));
        assertEquals(password, settings.getString(PreferencesActivity.KEY_PASSWORD, null));
    }

    private void setupTestFormsDatabase() {
        ContentValues values = new ContentValues();
        values.put(FormsProviderAPI.FormsColumns.JRCACHE_FILE_PATH, "/storage/emulated/0/odk/.cache/3a76a386464925b6f3e53422673dfe3c.formdef");
        values.put(FormsProviderAPI.FormsColumns.MD5_HASH, "ccce6015dd1b8f935f5f3058e81eeb43");
        values.put(FormsProviderAPI.FormsColumns.JR_FORM_ID, "jrFormId");
        values.put(FormsProviderAPI.FormsColumns.FORM_MEDIA_PATH, "/storage/emulated/0/odk/forms/testFile1-media");
        values.put(FormsProviderAPI.FormsColumns.DATE, "1487077903756");
        values.put(FormsProviderAPI.FormsColumns.DISPLAY_NAME, "displayName");
        values.put(FormsProviderAPI.FormsColumns.FORM_FILE_PATH, "/storage/emulated/0/odk/forms/testFile1");
        values.put(FormsProviderAPI.FormsColumns.DISPLAY_SUBTEXT, "Added on Tue, Feb 14, 2017 at 14:21");
        //Collect.getInstance().getContentResolver()
                //.insert(FormsProviderAPI.FormsColumns.CONTENT_URI, values);

       // assertEquals(1, getFormsCount());
    }

    private void setupTestInstancesDatabase() {
        ContentValues values = new ContentValues();
        values.put(InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH, "/storage/emulated/0/odk/instances/testDir1/testFile1");
        values.put(InstanceProviderAPI.InstanceColumns.SUBMISSION_URI, "submissionUri");
        values.put(InstanceProviderAPI.InstanceColumns.DISPLAY_NAME, "displayName");
        values.put(InstanceProviderAPI.InstanceColumns.DISPLAY_NAME, "formName");
        values.put(InstanceProviderAPI.InstanceColumns.JR_FORM_ID, "jrformid");
        values.put(InstanceProviderAPI.InstanceColumns.JR_VERSION, "jrversion");
        Collect.getInstance().getContentResolver()
                .insert(InstanceProviderAPI.InstanceColumns.CONTENT_URI, values);

        assertEquals(1, getInstancesCount());
    }

    private void createTestItemsetsDatabaseFile() throws IOException {
        assertTrue(new File(Collect.METADATA_PATH + "/itemsets.db").createNewFile());
    }

    private void saveTestFormFiles() throws IOException {
        assertTrue(new File(Collect.FORMS_PATH + "/testFile1").createNewFile());
        assertTrue(new File(Collect.FORMS_PATH + "/testFile2").createNewFile());
        assertTrue(new File(Collect.FORMS_PATH + "/testFile3").createNewFile());

        assertTrue(new File(Collect.FORMS_PATH + "/testDir1/testFile1-media").mkdirs());
        assertTrue(new File(Collect.FORMS_PATH + "/testDir2/testFile2-media").mkdirs());
        assertTrue(new File(Collect.FORMS_PATH + "/testDir3/testFile3-media/testFile").mkdirs());
    }

    private void saveTestInstanceFiles() {
        assertTrue(new File(Collect.INSTANCES_PATH + "/testDir1/testFile1").mkdirs());
        assertTrue(new File(Collect.INSTANCES_PATH + "/testDir2/testFile2").mkdirs());
        assertTrue(new File(Collect.INSTANCES_PATH + "/testDir3").mkdirs());
    }

    private void saveTestLayerFiles() throws IOException {
        assertTrue(new File(Collect.OFFLINE_LAYERS + "/testFile1").createNewFile());
        assertTrue(new File(Collect.OFFLINE_LAYERS + "/testFile2").createNewFile());
        assertTrue(new File(Collect.OFFLINE_LAYERS + "/testFile3").createNewFile());
        assertTrue(new File(Collect.OFFLINE_LAYERS + "/testFile4").createNewFile());
    }

    private void saveTestCacheFiles() throws IOException {
        assertTrue(new File(Collect.CACHE_PATH + "/testFile1").createNewFile());
        assertTrue(new File(Collect.CACHE_PATH + "/testFile2").createNewFile());
        assertTrue(new File(Collect.CACHE_PATH + "/testFile3").createNewFile());
    }

    private void saveTestOSMDroidFiles() throws IOException {
        assertTrue(new File(OpenStreetMapTileProviderConstants.TILE_PATH_BASE.getPath() + "/testFile1").mkdirs());
        assertTrue(new File(OpenStreetMapTileProviderConstants.TILE_PATH_BASE.getPath() + "/testFile2").mkdirs());
        assertTrue(new File(OpenStreetMapTileProviderConstants.TILE_PATH_BASE.getPath() + "/testFile3").mkdirs());
    }

    private int getFormsCount() {
        int forms = 0;
        Cursor cursor = Collect.getInstance().getContentResolver().query(
                FormsProviderAPI.FormsColumns.CONTENT_URI, null, null, null,
                FormsProviderAPI.FormsColumns.DISPLAY_NAME + " ASC");
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    while (!cursor.isAfterLast()) {
                        forms++;
                        cursor.moveToNext();
                    }
                }
            } finally {
                cursor.close();
            }
        }

        return forms;
    }

    private int getInstancesCount() {
        int instances = 0;
        Cursor cursor = Collect.getInstance().getContentResolver().query(
                InstanceProviderAPI.InstanceColumns.CONTENT_URI, null, null, null,
                InstanceProviderAPI.InstanceColumns.DISPLAY_NAME + " ASC");
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    while (!cursor.isAfterLast()) {
                        instances++;
                        cursor.moveToNext();
                    }
                }
            } finally {
                cursor.close();
            }
        }
        return instances;
    }
}