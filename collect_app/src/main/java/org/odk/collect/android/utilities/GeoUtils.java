package org.odk.collect.android.utilities;

import android.os.Bundle;

import org.odk.collect.android.storage.StoragePathProvider;

import java.io.File;

import static org.odk.collect.android.geo.MapFragment.KEY_REFERENCE_LAYER;

public class GeoUtils {

    private GeoUtils() {

    }

    /**
     * Corrects location provider names so "gps" displays as "GPS" in user-facing messaging.
     */
    public static String capitalizeGps(String locationProvider) {
        return "gps".equals(locationProvider) ? "GPS" : locationProvider;
    }

    public static File getReferenceLayerFile(Bundle config, StoragePathProvider storagePathProvider) {
        String path = storagePathProvider.getAbsoluteOfflineMapLayerPath(config.getString(KEY_REFERENCE_LAYER));
        return path != null && new File(path).exists()
                ? new File(path)
                : null;
    }
}
