package org.odk.collect.android.utilities;

import android.location.Location;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LocationStringProviderTest {
    private final Location location = mock(Location.class);
    private LocationStringProvider locationStringProvider;
    private LocationStringProvider nullLocationStringProvider;

    @Before
    public void setup() {
        when(location.getLatitude()).thenReturn(52.35339957011008);
        when(location.getLongitude()).thenReturn(19.19730884540468);
        when(location.getAltitude()).thenReturn(546.2);
        when(location.getAccuracy()).thenReturn(7.5f);
        locationStringProvider = new LocationStringProvider(location);

        nullLocationStringProvider = new LocationStringProvider(null);
    }

    @Test
    public void whenLocationIsNull_shouldGetLatitudeReturnEmptyString() {
        assertThat(nullLocationStringProvider.getLatitude(), is(""));
    }

    @Test
    public void whenLocationIsNull_shouldGetLongitudeReturnEmptyString() {
        assertThat(nullLocationStringProvider.getLongitude(), is(""));
    }

    @Test
    public void whenLocationIsNull_shouldGetAltitudeReturnEmptyString() {
        assertThat(nullLocationStringProvider.getAltitude(), is(""));
    }

    @Test
    public void whenLocationIsNull_shouldGetAccuracyReturnEmptyString() {
        assertThat(nullLocationStringProvider.getAccuracy(), is(""));
    }

    @Test
    public void whenLocationIsSet_shouldGetLatitudeReturnProperString() {
        assertThat(locationStringProvider.getLatitude(), is("52.35339957011008"));
    }

    @Test
    public void whenLocationIsSet_shouldGetLongitudeReturnProperString() {
        assertThat(locationStringProvider.getLongitude(), is("19.19730884540468"));
    }

    @Test
    public void whenLocationIsSet_shouldGetAltitudeReturnProperString() {
        assertThat(locationStringProvider.getAltitude(), is("546.2"));
    }

    @Test
    public void whenLocationIsSet_shouldGetAccuracyReturnProperString() {
        assertThat(locationStringProvider.getAccuracy(), is("7.5"));
    }

    @Test
    public void whenLocationIsSetButLocationIsFaked_shouldGetAccuracyReturnZero() {
        when(location.isFromMockProvider()).thenReturn(true);
        assertThat(locationStringProvider.getAccuracy(), is("0"));
    }
}