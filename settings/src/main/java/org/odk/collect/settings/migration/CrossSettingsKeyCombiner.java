package org.odk.collect.settings.migration;

import static org.odk.collect.settings.migration.MigrationUtils.asPairs;
import static org.odk.collect.settings.migration.MigrationUtils.replace;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CrossSettingsKeyCombiner implements CrossSettingsMigration {

    List<SettingsKeyPair> oldSettings;
    Object[] tempOldValueArray;
    List<Object[]> oldValueArrays = new ArrayList<>();
    List<KeyValuePair[]> newKeyValuePairArrays = new ArrayList<>();

    CrossSettingsKeyCombiner(List<SettingsKeyPair> oldSettings) {
        this.oldSettings = oldSettings;
    }

    public CrossSettingsKeyCombiner withValues(Object... oldValues) {
        tempOldValueArray = oldValues;
        return this;
    }

    public CrossSettingsKeyCombiner toPairs(Object... keysAndValues) {
        oldValueArrays.add(tempOldValueArray);
        newKeyValuePairArrays.add(asPairs(keysAndValues));
        return this;
    }

    public void apply() {
        Map<String, ?> prefMap = prefs.getAll();
        Object[] oldValues = new Object[oldKeys.length];
        for (int i = 0; i < oldKeys.length; i++) {
            oldValues[i] = prefMap.get(oldKeys[i]);
        }
        for (int i = 0; i < oldValueArrays.size(); i++) {
            if (Arrays.equals(oldValues, oldValueArrays.get(i))) {
                replace(prefs, oldKeys, newKeyValuePairArrays.get(i));
            }
        }
    }
}