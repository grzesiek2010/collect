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
package org.odk.collect.android.utilities;

public class ApplicationConstants {

    public enum BundleKeys {
        FORM_MODE("formMode");

        private String mBundleKeys;

        BundleKeys(String formMode) {
            mBundleKeys = formMode;
        }

        public String bundleKey() {
            return mBundleKeys;
        }
    }

    public enum FormModes {
        EDIT_SAVED("editSaved"),
        VIEW_SENT("viewSent");

        private String mFormMode;

        FormModes(String formMode) {
            mFormMode = formMode;
        }

        public String formMode() {
            return mFormMode;
        }
    }
}
