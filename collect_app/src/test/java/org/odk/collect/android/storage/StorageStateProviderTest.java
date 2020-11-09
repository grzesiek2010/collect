package org.odk.collect.android.storage;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.utilities.Clock;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class StorageStateProviderTest {

    @Test
    public void whenScopedStorageIsUsed_shouldNotPerformAutomaticMigration() {
        Clock clock = mock(Clock.class);
        when(clock.getCurrentTime()).thenReturn(0L);
        StorageStateProvider storageStateProvider = spy(new StorageStateProvider(clock));
        when(storageStateProvider.isScopedStorageUsed()).thenReturn(true);
        assertThat(storageStateProvider.shouldPerformAutomaticMigration(), is(false));
    }

    @Test
    public void whenScopedStorageIsNotUsed_shouldPerformAutomaticMigrationAndRepeatItOnceADay() {
        Clock clock = mock(Clock.class);
        when(clock.getCurrentTime()).thenReturn(90000000L);
        StorageStateProvider storageStateProvider = spy(new StorageStateProvider(clock));
        when(storageStateProvider.isScopedStorageUsed()).thenReturn(false);
        assertThat(storageStateProvider.shouldPerformAutomaticMigration(), is(true));
        when(clock.getCurrentTime()).thenReturn(90000000L + 86400000 - 1);
        assertThat(storageStateProvider.shouldPerformAutomaticMigration(), is(false));
        when(clock.getCurrentTime()).thenReturn(90000000L + 86400000);
        assertThat(storageStateProvider.shouldPerformAutomaticMigration(), is(true));
    }
}
