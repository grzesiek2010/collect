/*
 * Copyright 2019 Nafundi
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

import org.javarosa.form.api.FormEntryPrompt;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FormEntryPromptUtilsTest {

    @Test
    public void getNumberOfColumnsTest() {
        FormEntryPrompt formEntryPrompt = mock(FormEntryPrompt.class);

        when(formEntryPrompt.getAppearanceHint()).thenReturn("");
        assertEquals(1, FormEntryPromptUtils.getNumberOfColumns(formEntryPrompt, null));

        when(formEntryPrompt.getAppearanceHint()).thenReturn("columns-2");
        assertEquals(2, FormEntryPromptUtils.getNumberOfColumns(formEntryPrompt, null));

        when(formEntryPrompt.getAppearanceHint()).thenReturn("compact-2");
        assertEquals(2, FormEntryPromptUtils.getNumberOfColumns(formEntryPrompt, null));

        when(formEntryPrompt.getAppearanceHint()).thenReturn("columns-10");
        assertEquals(10, FormEntryPromptUtils.getNumberOfColumns(formEntryPrompt, null));

        when(formEntryPrompt.getAppearanceHint()).thenReturn("compact-10");
        assertEquals(10, FormEntryPromptUtils.getNumberOfColumns(formEntryPrompt, null));

        when(formEntryPrompt.getAppearanceHint()).thenReturn("columns-10 quick");
        assertEquals(10, FormEntryPromptUtils.getNumberOfColumns(formEntryPrompt, null));

        when(formEntryPrompt.getAppearanceHint()).thenReturn("columns-5 autocomplete");
        assertEquals(5, FormEntryPromptUtils.getNumberOfColumns(formEntryPrompt, null));

        when(formEntryPrompt.getAppearanceHint()).thenReturn("columns-10quick");
        assertEquals(1, FormEntryPromptUtils.getNumberOfColumns(formEntryPrompt, null));

        when(formEntryPrompt.getAppearanceHint()).thenReturn("columns-5autocomplete");
        assertEquals(1, FormEntryPromptUtils.getNumberOfColumns(formEntryPrompt, null));

        when(formEntryPrompt.getAppearanceHint()).thenReturn("columns-5 ");
        assertEquals(5, FormEntryPromptUtils.getNumberOfColumns(formEntryPrompt, null));

        when(formEntryPrompt.getAppearanceHint()).thenReturn("columns-5  ");
        assertEquals(5, FormEntryPromptUtils.getNumberOfColumns(formEntryPrompt, null));

        when(formEntryPrompt.getAppearanceHint()).thenReturn("  columns-5");
        assertEquals(5, FormEntryPromptUtils.getNumberOfColumns(formEntryPrompt, null));

        when(formEntryPrompt.getAppearanceHint()).thenReturn("quick columns-5");
        assertEquals(5, FormEntryPromptUtils.getNumberOfColumns(formEntryPrompt, null));

        when(formEntryPrompt.getAppearanceHint()).thenReturn("autocomplete compact-5");
        assertEquals(5, FormEntryPromptUtils.getNumberOfColumns(formEntryPrompt, null));

        when(formEntryPrompt.getAppearanceHint()).thenReturn("autocomplete compact-");
        assertEquals(1, FormEntryPromptUtils.getNumberOfColumns(formEntryPrompt, null));
    }
}
