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

import android.content.Context;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.dao.FormsDao;
import org.odk.collect.android.dao.InstancesDao;
import org.odk.collect.android.database.ItemsetDbAdapter;
import org.odk.collect.android.preferences.AdminSharedPreferences;
import org.odk.collect.android.preferences.GeneralSharedPreferences;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ResetUtility {

    private List<Integer> failedResetActions;

    public List<Integer> reset(Context context, List<Integer> resetActions) {

        failedResetActions = new ArrayList<>();
        failedResetActions.addAll(resetActions);

        for (int action : resetActions) {
            switch (action) {
                case ResetAction.RESET_PREFERENCES:
                    resetPreferences(context);
                    break;
                case ResetAction.RESET_INSTANCES:
                    resetInstances();
                    break;
                case ResetAction.RESET_FORMS:
                    resetForms();
                    break;
                case ResetAction.RESET_LAYERS:
                    if (StorageManager.clearOfflineLayersDir()) {
                        failedResetActions.remove(failedResetActions.indexOf(ResetAction.RESET_LAYERS));
                    }
                    break;
                case ResetAction.RESET_CACHE:
                    if (StorageManager.clearCacheDir()) {
                        failedResetActions.remove(failedResetActions.indexOf(ResetAction.RESET_CACHE));
                    }
                    break;
                case ResetAction.RESET_OSM_DROID:
                    if (StorageManager.clearOSMDroidDir()) {
                        failedResetActions.remove(failedResetActions.indexOf(ResetAction.RESET_OSM_DROID));
                    }
                    break;
            }
        }

        return failedResetActions;
    }

    private void resetPreferences(Context context) {
        WebCredentialsUtils.clearAllCredentials();

        GeneralSharedPreferences.getInstance().loadDefaultPreferences();
        AdminSharedPreferences.getInstance().loadDefaultPreferences();

        boolean deletedSettingsFolderContest = StorageManager.clearSettingsDir();

        boolean deletedSettingsFile = !new File(StorageManager.getStoragePath() + "/collect.settings").exists()
                || (new File(StorageManager.getStoragePath() + "/collect.settings").delete());
        
        new LocaleHelper().updateLocale(context);

        if (deletedSettingsFolderContest && deletedSettingsFile) {
            failedResetActions.remove(failedResetActions.indexOf(ResetAction.RESET_PREFERENCES));
        }

        Collect.getInstance().initializeJavaRosa();
    }

    private void resetInstances() {
        new InstancesDao().deleteInstancesDatabase();

        if (StorageManager.clearInstancesDir()) {
            failedResetActions.remove(failedResetActions.indexOf(ResetAction.RESET_INSTANCES));
        }
    }

    private void resetForms() {
        new FormsDao().deleteFormsDatabase();

        File itemsetDbFile = new File(StorageManager.getMetadataDirPath() + File.separator + ItemsetDbAdapter.DATABASE_NAME);

        if (StorageManager.clearFormsDir() && (!itemsetDbFile.exists() || itemsetDbFile.delete())) {
            failedResetActions.remove(failedResetActions.indexOf(ResetAction.RESET_FORMS));
        }
    }

    public static class ResetAction {
        public static final int RESET_PREFERENCES = 0;
        public static final int RESET_INSTANCES = 1;
        public static final int RESET_FORMS = 2;
        public static final int RESET_LAYERS = 3;
        public static final int RESET_CACHE = 4;
        public static final int RESET_OSM_DROID = 5;
    }
}
