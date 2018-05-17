/*
 * Copyright 2018 Nafundi
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

import android.database.Cursor;
import android.database.MatrixCursor;
import android.provider.BaseColumns;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.dto.Form;
import org.odk.collect.android.provider.FormsProviderAPI;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class FormUtilsTest {

    /**
     * A case where all forms have null {@value org.odk.collect.android.provider.FormsProviderAPI.FormsColumns#JR_VERSION},
     * that means for each {@value org.odk.collect.android.provider.FormsProviderAPI.FormsColumns#JR_FORM_ID}
     * we should get only the newest one (by {@value org.odk.collect.android.provider.FormsProviderAPI.FormsColumns#DATE}).
     */
    @Test
    public void removeOldFormsTest1() {
        MatrixCursor cursor = getCursor();
        cursor.addRow(new Object[] {1, "Form1", null, "form1", null, null, null, null, null, null, 1526367600000L, null, null, null, null, null, null});
        cursor.addRow(new Object[] {2, "Form1", null, "form1", null, null, null, null, null, null, 1526371200000L, null, null, null, null, null, null});
        cursor.addRow(new Object[] {3, "Form1", null, "form1", null, null, null, null, null, null, 1526368200000L, null, null, null, null, null, null});
        cursor.addRow(new Object[] {4, "Form2", null, "form2", null, null, null, null, null, null, 1526375400000L, null, null, null, null, null, null});

        Cursor filteredCursor = FormUtils.removeOldForms(cursor);

        assertNotNull(filteredCursor);
        assertEquals(2, filteredCursor.getCount());

        List<Form> forms = getFormsFromCursor(filteredCursor);

        assertEquals(2, forms.size());
        assertEquals(2, forms.get(0).getId());
        assertEquals(4, forms.get(1).getId());
    }

    /**
     * A case where all forms have set {@value org.odk.collect.android.provider.FormsProviderAPI.FormsColumns#JR_VERSION},
     * that means for each {@value org.odk.collect.android.provider.FormsProviderAPI.FormsColumns#JR_FORM_ID}
     * we should get only the one with the highest {@value org.odk.collect.android.provider.FormsProviderAPI.FormsColumns#JR_VERSION}.
     */
    @Test
    public void removeOldFormsTest2() {
        MatrixCursor cursor = getCursor();
        cursor.addRow(new Object[] {1, "Form1", null, "form1", 3, null, null, null, null, null, 1526367600000L, null, null, null, null, null, null});
        cursor.addRow(new Object[] {2, "Form1", null, "form1", 2, null, null, null, null, null, 1526371200000L, null, null, null, null, null, null});
        cursor.addRow(new Object[] {3, "Form1", null, "form1", 1, null, null, null, null, null, 1526368200000L, null, null, null, null, null, null});
        cursor.addRow(new Object[] {4, "Form2", null, "form2", 1, null, null, null, null, null, 1526375400000L, null, null, null, null, null, null});

        Cursor filteredCursor = FormUtils.removeOldForms(cursor);

        assertNotNull(filteredCursor);
        assertEquals(2, filteredCursor.getCount());

        List<Form> forms = getFormsFromCursor(filteredCursor);

        assertEquals(2, forms.size());
        assertEquals(1, forms.get(0).getId());
        assertEquals(4, forms.get(1).getId());
    }

    /**
     * A case where forms have mixed values in {@value org.odk.collect.android.provider.FormsProviderAPI.FormsColumns#JR_VERSION},
     * that means for each {@value org.odk.collect.android.provider.FormsProviderAPI.FormsColumns#JR_FORM_ID}
     * we should get only the one with the highest {@value org.odk.collect.android.provider.FormsProviderAPI.FormsColumns#JR_VERSION}.
     * Null value in {@value org.odk.collect.android.provider.FormsProviderAPI.FormsColumns#JR_VERSION}
     * is treated as "-1" value.
     */
    @Test
    public void removeOldFormsTest3() {
        MatrixCursor cursor = getCursor();
        cursor.addRow(new Object[] {1, "Form1", null, "form1", null, null, null, null, null, null, 1526367600000L, null, null, null, null, null, null});
        cursor.addRow(new Object[] {2, "Form1", null, "form1", null, null, null, null, null, null, 1526371200000L, null, null, null, null, null, null});
        cursor.addRow(new Object[] {3, "Form1", null, "form1", 1, null, null, null, null, null, 1526368200000L, null, null, null, null, null, null});
        cursor.addRow(new Object[] {4, "Form2", null, "form2", 1, null, null, null, null, null, 1526375400000L, null, null, null, null, null, null});

        Cursor filteredCursor = FormUtils.removeOldForms(cursor);

        assertNotNull(filteredCursor);
        assertEquals(2, filteredCursor.getCount());

        List<Form> forms = getFormsFromCursor(filteredCursor);

        assertEquals(2, forms.size());
        assertEquals(3, forms.get(0).getId());
        assertEquals(4, forms.get(1).getId());
    }

    private List<Form> getFormsFromCursor(Cursor cursor) {
        List<Form> forms = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int idColumnIndex = cursor.getColumnIndex(BaseColumns._ID);
                int displayNameColumnIndex = cursor.getColumnIndex(FormsProviderAPI.FormsColumns.DISPLAY_NAME);
                int jrFormIdColumnIndex = cursor.getColumnIndex(FormsProviderAPI.FormsColumns.JR_FORM_ID);
                int jrVersionColumnIndex = cursor.getColumnIndex(FormsProviderAPI.FormsColumns.JR_VERSION);
                int dateColumnIndex = cursor.getColumnIndex(FormsProviderAPI.FormsColumns.DATE);

                Form form = new Form.Builder()
                        .id(cursor.getInt(idColumnIndex))
                        .displayName(cursor.getString(displayNameColumnIndex))
                        .jrFormId(cursor.getString(jrFormIdColumnIndex))
                        .jrVersion(cursor.getString(jrVersionColumnIndex))
                        .date(cursor.getLong(dateColumnIndex))
                        .build();

                forms.add(form);
            } while (cursor.moveToNext());
        }
        return forms;
    }

    private MatrixCursor getCursor() {
        return new MatrixCursor(
                new String[] {
                        BaseColumns._ID,
                        FormsProviderAPI.FormsColumns.DISPLAY_NAME,
                        FormsProviderAPI.FormsColumns.DESCRIPTION,
                        FormsProviderAPI.FormsColumns.JR_FORM_ID,
                        FormsProviderAPI.FormsColumns.JR_VERSION,
                        FormsProviderAPI.FormsColumns.FORM_FILE_PATH,
                        FormsProviderAPI.FormsColumns.SUBMISSION_URI,
                        FormsProviderAPI.FormsColumns.BASE64_RSA_PUBLIC_KEY,
                        FormsProviderAPI.FormsColumns.DISPLAY_SUBTEXT,
                        FormsProviderAPI.FormsColumns.MD5_HASH,
                        FormsProviderAPI.FormsColumns.DATE,
                        FormsProviderAPI.FormsColumns.JRCACHE_FILE_PATH,
                        FormsProviderAPI.FormsColumns.FORM_MEDIA_PATH,
                        FormsProviderAPI.FormsColumns.LANGUAGE,
                        FormsProviderAPI.FormsColumns.AUTO_SEND,
                        FormsProviderAPI.FormsColumns.AUTO_DELETE,
                        FormsProviderAPI.FormsColumns.LAST_DETECTED_FORM_VERSION_HASH}
        );
    }
}
