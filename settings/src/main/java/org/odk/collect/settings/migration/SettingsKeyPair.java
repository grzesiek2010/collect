package org.odk.collect.settings.migration;

import org.odk.collect.shared.settings.Settings;

public class SettingsKeyPair {
    final Settings settings;
    final String key;

    public SettingsKeyPair(Settings settings, String key) {
        this.settings = settings;
        this.key = key;
    }
}
