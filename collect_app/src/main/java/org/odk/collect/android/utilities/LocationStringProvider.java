package org.odk.collect.android.utilities;

import android.location.Location;

import androidx.annotation.Nullable;

public class LocationStringProvider {

    @Nullable
    private final Location location;

    public LocationStringProvider(@Nullable Location location) {
        this.location = location;
    }

    public String getLocationAsString() {
        return String.format("%s %s %s %s", getLatitude(), getLongitude(), getAltitude(), getAccuracy());
    }

    public String getLatitude() {
        return location == null ? "" : String.valueOf(location.getLatitude());
    }

    public String getLongitude() {
        return location == null ? "" : String.valueOf(location.getLongitude());
    }

    public String getAltitude() {
        return location == null ? "" : String.valueOf(location.getAltitude());
    }

    public String getAccuracy() {
        return location == null ? "" :
                location.isFromMockProvider() ? "0" : String.valueOf(location.getAccuracy());
    }

}
