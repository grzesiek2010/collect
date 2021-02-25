package org.odk.collect.android.instrumented.storage;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.storage.StorageInitializer;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.support.ResetStateRule;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(AndroidJUnit4.class)
public class StorageInitializerTest {

    @Rule
    public RuleChain copyFormChain = RuleChain.outerRule(new ResetStateRule());

    private StoragePathProvider storagePathProvider;
    private StorageInitializer storageInitializer;

    @Before
    public void setup() {
        storagePathProvider = new StoragePathProvider();
        storageInitializer = new StorageInitializer(storagePathProvider, ApplicationProvider.getApplicationContext());
    }

    @Test
    public void createOdkDirsOnStorage_shouldCreteRequiredDirs() {
        for (String dirName : storagePathProvider.getOdkDirPaths()) {
            File dir = new File(dirName);
            assertThat(dir.exists(), is(false));
        }

        storageInitializer.createOdkDirsOnStorage();

        for (String dirName : storagePathProvider.getOdkDirPaths()) {
            File dir = new File(dirName);
            assertThat(dir.exists(), is(true));
            assertThat(dir.isDirectory(), is(true));
        }
    }
}
