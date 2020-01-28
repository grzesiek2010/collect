package org.odk.collect.android.storage;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

public class StoragePathProviderTest {

    private final StoragePathProvider storagePathProvider = spy(StoragePathProvider.class);
    private final StorageStateProvider storageStateProvider = spy(StorageStateProvider.class);

    @Before
    public void setup() {
        doReturn("/storage/emulated/0/Android/data/org.odk.collect.android/files").when(storagePathProvider).getPrimaryExternalStorageFilePath();
        doReturn("/storage/emulated/0").when(storagePathProvider).getSecondaryExternalStorageFilePath();
    }

    private void mockUsingScopedStorage() {
        doReturn(true).when(storageStateProvider).isScopedStorageUsed();
    }

    private void mockUsingSdCard() {
        doReturn(false).when(storageStateProvider).isScopedStorageUsed();
    }

    @Test
    public void when_scopedStorageNotUsed_should_sdCardPathsBeUsed() {
        mockUsingSdCard();

        assertEquals("/storage/emulated/0/odk", storagePathProvider.getMainODKDirPath());
        assertEquals("/storage/emulated/0/odk/forms", storagePathProvider.getFormsDirPath());
        assertEquals("/storage/emulated/0/odk/instances", storagePathProvider.getInstancesDirPath());
        assertEquals("/storage/emulated/0/odk/metadata", storagePathProvider.getMetadataDirPath());
        assertEquals("/storage/emulated/0/odk/.cache", storagePathProvider.getCacheDirPath());
        assertEquals("/storage/emulated/0/odk/layers", storagePathProvider.getOfflineLayersDirPath());
        assertEquals("/storage/emulated/0/odk/settings", storagePathProvider.getSettingsDirPath());
        assertEquals("/storage/emulated/0/odk/.cache/tmp.jpg", storagePathProvider.getTmpFilePath());
        assertEquals("/storage/emulated/0/odk/.cache/tmpDraw.jpg", storagePathProvider.getTmpDrawFilePath());
    }

    @Test
    public void when_scopedStorageUsed_should_externalStoragePathsBeUsed() {
        mockUsingScopedStorage();

        assertEquals("/storage/emulated/0/Android/data/org.odk.collect.android/files/odk", storagePathProvider.getMainODKDirPath());
        assertEquals("/storage/emulated/0/Android/data/org.odk.collect.android/files/odk/forms", storagePathProvider.getFormsDirPath());
        assertEquals("/storage/emulated/0/Android/data/org.odk.collect.android/files/odk/instances", storagePathProvider.getInstancesDirPath());
        assertEquals("/storage/emulated/0/Android/data/org.odk.collect.android/files/odk/metadata", storagePathProvider.getMetadataDirPath());
        assertEquals("/storage/emulated/0/Android/data/org.odk.collect.android/files/odk/.cache", storagePathProvider.getCacheDirPath());
        assertEquals("/storage/emulated/0/Android/data/org.odk.collect.android/files/odk/layers", storagePathProvider.getOfflineLayersDirPath());
        assertEquals("/storage/emulated/0/Android/data/org.odk.collect.android/files/odk/settings", storagePathProvider.getSettingsDirPath());
        assertEquals("/storage/emulated/0/Android/data/org.odk.collect.android/files/odk/.cache/tmp.jpg", storagePathProvider.getTmpFilePath());
        assertEquals("/storage/emulated/0/Android/data/org.odk.collect.android/files/odk/.cache/tmpDraw.jpg", storagePathProvider.getTmpDrawFilePath());
    }

    @Test
    public void when_scopedStorageNotUsed_should_getCacheFilePathMethodReturnAbsolutePath() {
        mockUsingSdCard();

        assertEquals("/storage/emulated/0/odk/.cache/4cd980d50f884362afba842cbff3a798.formdef", storagePathProvider.getCacheFilePathToStoreInDatabaseBasingOnRelativePath("4cd980d50f884362afba842cbff3a798.formdef"));
    }

    @Test
    public void when_scopedStorageUsed_should_getCacheFilePathMethodReturnRelativePath() {
        mockUsingScopedStorage();

        assertEquals("4cd980d50f884362afba842cbff3a798.formdef", storagePathProvider.getCacheFilePathToStoreInDatabaseBasingOnRelativePath("4cd980d50f884362afba842cbff3a798.formdef"));
    }

    @Test
    public void getAbsoluteCacheFilePath_should_returnAbsolutePathToCacheFile() {
        mockUsingScopedStorage();

        assertEquals("/storage/emulated/0/Android/data/org.odk.collect.android/files/odk/.cache/4cd980d50f884362afba842cbff3a798.formdef", storagePathProvider.getAbsoluteCacheFilePath("4cd980d50f884362afba842cbff3a798.formdef"));
        assertEquals("/storage/emulated/0/Android/data/org.odk.collect.android/files/odk/.cache/4cd980d50f884362afba842cbff3a798.formdef", storagePathProvider.getAbsoluteCacheFilePath("/storage/emulated/0/Android/data/org.odk.collect.android/files/odk/.cache/4cd980d50f884362afba842cbff3a798.formdef"));
    }

    @Test
    public void when_scopedStorageNotUsed_should_getFormFilePathMethodReturnAbsolutePath() {
        mockUsingSdCard();

        assertEquals("/storage/emulated/0/odk/forms/All widgets.xml", storagePathProvider.getFormFilePathToStoreInDatabaseBasingOnRelativePath("All widgets.xml"));
    }

    @Test
    public void when_scopedStorageUsed_should_getFormFilePathMethodReturnRelativePath() {
        mockUsingScopedStorage();

        assertEquals("All widgets.xml", storagePathProvider.getFormFilePathToStoreInDatabaseBasingOnRelativePath("All widgets.xml"));
    }

    @Test
    public void getAbsoluteFormFilePath_should_returnAbsolutePathToFormFile() {
        mockUsingScopedStorage();

        assertEquals("/storage/emulated/0/Android/data/org.odk.collect.android/files/odk/forms/All widgets.xml", storagePathProvider.getAbsoluteFormFilePath("All widgets.xml"));
        assertEquals("/storage/emulated/0/Android/data/org.odk.collect.android/files/odk/forms/All widgets.xml", storagePathProvider.getAbsoluteFormFilePath("/storage/emulated/0/Android/data/org.odk.collect.android/files/odk/forms/All widgets.xml"));
    }

    @Test
    public void getRelativeFormFilePath_should_returnRelativePathToInstanceFile() {
        mockUsingScopedStorage();

        assertEquals("All widgets.xml", storagePathProvider.getRelativeFormFilePath("All widgets.xml"));
        assertEquals("All widgets.xml", storagePathProvider.getRelativeFormFilePath("/storage/emulated/0/Android/data/org.odk.collect.android/files/odk/forms/All widgets.xml"));
    }

    @Test
    public void when_scopedStorageNotUsed_should_getInstanceFilePathMethodReturnAbsolutePath() {
        mockUsingSdCard();

        assertEquals("/storage/emulated/0/odk/instances/All widgets_2020-01-20_13-54-11/All widgets_2020-01-20_13-54-11.xml", storagePathProvider.getInstanceFilePathToStoreInDatabaseBasingOnRelativePath("All widgets_2020-01-20_13-54-11/All widgets_2020-01-20_13-54-11.xml"));
    }

    @Test
    public void when_scopedStorageUsed_should_getInstanceFilePathMethodReturnRelativePath() {
        mockUsingScopedStorage();

        assertEquals("All widgets_2020-01-20_13-54-11/All widgets_2020-01-20_13-54-11.xml", storagePathProvider.getInstanceFilePathToStoreInDatabaseBasingOnRelativePath("All widgets_2020-01-20_13-54-11/All widgets_2020-01-20_13-54-11.xml"));
    }

    @Test
    public void getAbsoluteInstanceFilePath_should_returnAbsolutePathToInstanceFile() {
        mockUsingScopedStorage();

        assertEquals("/storage/emulated/0/Android/data/org.odk.collect.android/files/odk/instances/All widgets_2020-01-20_13-54-11/All widgets_2020-01-20_13-54-11.xml", storagePathProvider.getAbsoluteInstanceFilePath("All widgets_2020-01-20_13-54-11/All widgets_2020-01-20_13-54-11.xml"));
        assertEquals("/storage/emulated/0/Android/data/org.odk.collect.android/files/odk/instances/All widgets_2020-01-20_13-54-11/All widgets_2020-01-20_13-54-11.xml", storagePathProvider.getAbsoluteInstanceFilePath("/storage/emulated/0/Android/data/org.odk.collect.android/files/odk/instances/All widgets_2020-01-20_13-54-11/All widgets_2020-01-20_13-54-11.xml"));
    }

    @Test
    public void getRelativeInstanceFilePath_should_returnRelativePathToInstanceFile() {
        mockUsingScopedStorage();

        assertEquals("All widgets_2020-01-20_13-54-11/All widgets_2020-01-20_13-54-11.xml", storagePathProvider.getRelativeInstanceFilePath("All widgets_2020-01-20_13-54-11/All widgets_2020-01-20_13-54-11.xml"));
        assertEquals("All widgets_2020-01-20_13-54-11/All widgets_2020-01-20_13-54-11.xml", storagePathProvider.getRelativeInstanceFilePath("/storage/emulated/0/Android/data/org.odk.collect.android/files/odk/instances/All widgets_2020-01-20_13-54-11/All widgets_2020-01-20_13-54-11.xml"));
    }
}
