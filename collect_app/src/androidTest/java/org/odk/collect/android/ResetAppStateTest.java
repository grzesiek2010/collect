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

import android.Manifest;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.preferences.AdminKeys;
import org.odk.collect.android.preferences.GeneralKeys;
import org.odk.collect.android.provider.FormsProviderAPI.FormsColumns;
import org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns;
import org.odk.collect.android.utilities.ResetUtility;
import org.odk.collect.android.utilities.StorageManager;
import org.odk.collect.android.utilities.WebCredentialsUtils;
import org.osmdroid.config.Configuration;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import androidx.test.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.runner.AndroidJUnit4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class ResetAppStateTest {

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    );

    @Before
    public void setUp() throws IOException {
        resetAppState(Arrays.asList(
                ResetUtility.ResetAction.RESET_PREFERENCES, ResetUtility.ResetAction.RESET_INSTANCES,
                ResetUtility.ResetAction.RESET_FORMS, ResetUtility.ResetAction.RESET_LAYERS,
                ResetUtility.ResetAction.RESET_CACHE, ResetUtility.ResetAction.RESET_OSM_DROID
        ));
    }

    @After
    public void tearDown() throws IOException {
        resetAppState(Arrays.asList(
                ResetUtility.ResetAction.RESET_PREFERENCES, ResetUtility.ResetAction.RESET_INSTANCES,
                ResetUtility.ResetAction.RESET_FORMS, ResetUtility.ResetAction.RESET_LAYERS,
                ResetUtility.ResetAction.RESET_CACHE, ResetUtility.ResetAction.RESET_OSM_DROID
        ));
    }

    @Test
    public void resetSettingsTest() throws IOException {
        WebCredentialsUtils webCredentialsUtils = new WebCredentialsUtils();
        webCredentialsUtils.saveCredentials("https://opendatakit.appspot.com/", "admin", "admin");

        setupTestSettings();
        resetAppState(Collections.singletonList(ResetUtility.ResetAction.RESET_PREFERENCES));

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(Collect.getInstance());
        assertEquals(settings.getString(GeneralKeys.KEY_USERNAME, ""), "");
        assertEquals(settings.getString(GeneralKeys.KEY_PASSWORD, ""), "");
        assertTrue(settings.getBoolean(AdminKeys.KEY_VIEW_SENT, true));

        assertEquals(0, getFormsCount());
        assertEquals(0, getInstancesCount());
        assertEquals("", webCredentialsUtils.getCredentials(URI.create("https://opendatakit.appspot.com/")).getUsername());
        assertEquals("", webCredentialsUtils.getCredentials(URI.create("https://opendatakit.appspot.com/")).getPassword());
    }

    @Test
    public void resetFormsTest() throws IOException {
        saveTestFormFiles();
        setupTestFormsDatabase();
        createTestItemsetsDatabaseFile();
        resetAppState(Collections.singletonList(ResetUtility.ResetAction.RESET_FORMS));
        assertFolderEmpty(StorageManager.getFormsDirPath());
        assertFalse(new File(StorageManager.getMetadataDirPath() + "/itemsets.db").exists());
    }

    @Test
    public void resetInstancesTest() throws IOException {
        saveTestInstanceFiles();
        setupTestInstancesDatabase();
        resetAppState(Collections.singletonList(ResetUtility.ResetAction.RESET_INSTANCES));
        assertFolderEmpty(StorageManager.getInstancesDirPath());
    }

    @Test
    public void resetLayersTest() throws IOException {
        saveTestLayerFiles();
        resetAppState(Collections.singletonList(ResetUtility.ResetAction.RESET_LAYERS));
        assertFolderEmpty(StorageManager.getOfflineLayersDirPath());
    }

    @Test
    public void resetCacheTest() throws IOException {
        saveTestCacheFiles();
        resetAppState(Collections.singletonList(ResetUtility.ResetAction.RESET_CACHE));
        assertFolderEmpty(Collect.CACHE_PATH);
    }

    @Test
    public void resetOSMDroidTest() throws IOException {
        saveTestOSMDroidFiles();
        resetAppState(Collections.singletonList(ResetUtility.ResetAction.RESET_OSM_DROID));
        assertFolderEmpty(Configuration.getInstance().getOsmdroidTileCache().getPath());
    }

    private void resetAppState(List<Integer> resetActions) {
        List<Integer> failedResetActions = new ResetUtility().reset(InstrumentationRegistry.getTargetContext(), resetActions);
        assertEquals(0, failedResetActions.size());
    }

    private void setupTestSettings() throws IOException {
        String username = "usernameTest";
        String password = "passwordTest";
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(Collect.getInstance());
        settings
                .edit()
                .putString(GeneralKeys.KEY_USERNAME, username)
                .putString(GeneralKeys.KEY_PASSWORD, password)
                .apply();

        assertEquals(username, settings.getString(GeneralKeys.KEY_USERNAME, null));
        assertEquals(password, settings.getString(GeneralKeys.KEY_PASSWORD, null));

        settings
                .edit()
                .putBoolean(AdminKeys.KEY_VIEW_SENT, false)
                .apply();

        assertFalse(settings.getBoolean(AdminKeys.KEY_VIEW_SENT, false));

        assertTrue(new File(StorageManager.getSettingsDirPath()).exists() || new File(StorageManager.getSettingsDirPath()).mkdir());
        assertTrue(new File(StorageManager.getSettingsDirPath() + "/collect.settings").createNewFile());
        assertTrue(new File(Collect.ODK_ROOT + "/collect.settings").createNewFile());
    }

    private void setupTestFormsDatabase() {
        ContentValues values = new ContentValues();
        values.put(FormsColumns.JRCACHE_FILE_PATH, Collect.ODK_ROOT + "/.cache/3a76a386464925b6f3e53422673dfe3c.formdef");
        values.put(FormsColumns.JR_FORM_ID, "jrFormId");
        values.put(FormsColumns.FORM_MEDIA_PATH, StorageManager.getFormsDirPath() + "/testFile1-media");
        values.put(FormsColumns.DATE, "1487077903756");
        values.put(FormsColumns.DISPLAY_NAME, "displayName");
        values.put(FormsColumns.FORM_FILE_PATH, StorageManager.getFormsDirPath() + "/testFile1.xml");
        Collect.getInstance().getContentResolver()
                .insert(FormsColumns.CONTENT_URI, values);

        assertEquals(1, getFormsCount());
    }

    private void setupTestInstancesDatabase() {
        ContentValues values = new ContentValues();
        values.put(InstanceColumns.INSTANCE_FILE_PATH, StorageManager.getInstancesDirPath() + "/testDir1/testFile1");
        values.put(InstanceColumns.SUBMISSION_URI, "submissionUri");
        values.put(InstanceColumns.DISPLAY_NAME, "displayName");
        values.put(InstanceColumns.DISPLAY_NAME, "formName");
        values.put(InstanceColumns.JR_FORM_ID, "jrformid");
        values.put(InstanceColumns.JR_VERSION, "jrversion");
        Collect.getInstance().getContentResolver()
                .insert(InstanceColumns.CONTENT_URI, values);

        assertEquals(1, getInstancesCount());
    }

    private void createTestItemsetsDatabaseFile() throws IOException {
        assertTrue(new File(StorageManager.getMetadataDirPath() + "/itemsets.db").createNewFile());
    }

    private void saveTestFormFiles() throws IOException {
        assertTrue(new File(StorageManager.getFormsDirPath() + "/testFile1.xml").createNewFile());
        assertTrue(new File(StorageManager.getFormsDirPath() + "/testFile2.xml").createNewFile());
        assertTrue(new File(StorageManager.getFormsDirPath() + "/testFile3.xml").createNewFile());

        assertTrue(new File(StorageManager.getFormsDirPath() + "/testDir1/testFile1-media").mkdirs());
        assertTrue(new File(StorageManager.getFormsDirPath() + "/testDir2/testFile2-media").mkdirs());
        assertTrue(new File(StorageManager.getFormsDirPath() + "/testDir3/testFile3-media/testFile.csv").mkdirs());
    }

    private void saveTestInstanceFiles() {
        assertTrue(new File(StorageManager.getInstancesDirPath() + "/testDir1/testFile1.xml").mkdirs());
        assertTrue(new File(StorageManager.getInstancesDirPath() + "/testDir2/testFile2.xml").mkdirs());
        assertTrue(new File(StorageManager.getInstancesDirPath() + "/testDir3").mkdirs());
    }

    private void saveTestLayerFiles() throws IOException {
        assertTrue(new File(StorageManager.getOfflineLayersDirPath() + "/testFile1").createNewFile());
        assertTrue(new File(StorageManager.getOfflineLayersDirPath() + "/testFile2").createNewFile());
        assertTrue(new File(StorageManager.getOfflineLayersDirPath() + "/testFile3").createNewFile());
        assertTrue(new File(StorageManager.getOfflineLayersDirPath() + "/testFile4").createNewFile());
    }

    private void saveTestCacheFiles() throws IOException {
        assertTrue(new File(Collect.CACHE_PATH + "/testFile1").createNewFile());
        assertTrue(new File(Collect.CACHE_PATH + "/testFile2").createNewFile());
        assertTrue(new File(Collect.CACHE_PATH + "/testFile3").createNewFile());
    }

    private void saveTestOSMDroidFiles() throws IOException {
        assertTrue(new File(Configuration.getInstance().getOsmdroidTileCache().getPath() + "/testFile1").mkdirs());
        assertTrue(new File(Configuration.getInstance().getOsmdroidTileCache().getPath() + "/testFile2").mkdirs());
        assertTrue(new File(Configuration.getInstance().getOsmdroidTileCache().getPath() + "/testFile3").mkdirs());
    }

    private int getFormsCount() {
        int forms = 0;
        Cursor cursor = Collect.getInstance().getContentResolver().query(
                FormsColumns.CONTENT_URI, null, null, null,
                FormsColumns.DISPLAY_NAME + " ASC");
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
                InstanceColumns.CONTENT_URI, null, null, null,
                InstanceColumns.DISPLAY_NAME + " ASC");
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

    private void assertFolderEmpty(String folder) {
        assertTrue(new File(folder).isDirectory());
        assertEquals(new File(folder).list().length, 0);
    }
}
