package org.odk.collect.android.externalintents;

import junit.framework.Assert;

import org.odk.collect.android.utilities.StorageManager;

import java.io.File;

import timber.log.Timber;

import static org.odk.collect.android.application.Collect.CACHE_PATH;
import static org.odk.collect.android.application.Collect.ODK_ROOT;
import static org.odk.collect.android.application.Collect.OFFLINE_LAYERS;

class ExportedActivitiesUtils {

    private static final String[] DIRS = {
            ODK_ROOT,
            StorageManager.getFormsDirPath(),
            StorageManager.getInstancesDirPath(),
            CACHE_PATH,
            StorageManager.getMetadataDirPath(),
            OFFLINE_LAYERS
    };

    private ExportedActivitiesUtils() {

    }

    static void clearDirectories() {
        for (String dirName : DIRS) {
            File dir = new File(dirName);
            if (dir.exists()) {
                if (dir.delete()) {
                    Timber.i("Directory was not deleted");
                }
            }
        }

    }

    static void testDirectories() {
        for (String dirName : DIRS) {
            File dir = new File(dirName);
            Assert.assertTrue("File " + dirName + "does not exist", dir.exists());
            Assert.assertTrue("File" + dirName + "does not exist", dir.isDirectory());
        }
    }

}
