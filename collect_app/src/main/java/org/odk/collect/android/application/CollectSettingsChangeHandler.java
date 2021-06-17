package org.odk.collect.android.application;

import org.odk.collect.analytics.Analytics;
import org.odk.collect.android.analytics.AnalyticsEvents;
import org.odk.collect.android.backgroundwork.FormUpdateScheduler;
import org.odk.collect.android.configure.SettingsChangeHandler;
import org.odk.collect.android.logic.PropertyManager;
import org.odk.collect.android.preferences.source.SettingsProvider;
import org.odk.collect.shared.Settings;
import org.odk.collect.shared.strings.Md5;

import java.io.ByteArrayInputStream;

import static org.odk.collect.android.preferences.keys.GeneralKeys.KEY_EXTERNAL_APP_RECORDING;
import static org.odk.collect.android.preferences.keys.GeneralKeys.KEY_FORM_UPDATE_MODE;
import static org.odk.collect.android.preferences.keys.GeneralKeys.KEY_PERIODIC_FORM_UPDATES_CHECK;
import static org.odk.collect.android.preferences.keys.GeneralKeys.KEY_PROTOCOL;
import static org.odk.collect.android.preferences.keys.GeneralKeys.KEY_SERVER_URL;

public class CollectSettingsChangeHandler implements SettingsChangeHandler {

    private final PropertyManager propertyManager;
    private final FormUpdateScheduler formUpdateScheduler;
    private final Analytics analytics;
    private final SettingsProvider settingsProvider;

    public CollectSettingsChangeHandler(PropertyManager propertyManager, FormUpdateScheduler formUpdateScheduler, Analytics analytics, SettingsProvider settingsProvider) {
        this.propertyManager = propertyManager;
        this.formUpdateScheduler = formUpdateScheduler;
        this.analytics = analytics;
        this.settingsProvider = settingsProvider;
    }

    @Override
    public void onSettingChanged(String projectId, Object newValue, String changedKey) {
        propertyManager.reload();

        if (changedKey.equals(KEY_FORM_UPDATE_MODE) || changedKey.equals(KEY_PERIODIC_FORM_UPDATES_CHECK) || changedKey.equals(KEY_PROTOCOL)) {
            formUpdateScheduler.scheduleUpdates(projectId);
        }

        if (changedKey.equals(KEY_EXTERNAL_APP_RECORDING) && !((Boolean) newValue)) {
            Settings generalSettings = settingsProvider.getGeneralSettings(projectId);
            String currentServerUrl = generalSettings.getString(KEY_SERVER_URL);
            String serverHash = Md5.getMd5Hash(new ByteArrayInputStream(currentServerUrl.getBytes()));

            analytics.logServerEvent(AnalyticsEvents.INTERNAL_RECORDING_OPT_IN, serverHash);
        }
    }
}
