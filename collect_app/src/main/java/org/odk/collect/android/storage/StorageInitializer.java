package org.odk.collect.android.storage;

import android.content.Context;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.projects.Project;

import java.io.File;

import timber.log.Timber;

public class StorageInitializer {

    private final StoragePathProvider storagePathProvider;
    private final Context context;

    public StorageInitializer() {
        this(new StoragePathProvider(), Collect.getInstance());
    }

    public StorageInitializer(StoragePathProvider storagePathProvider, Context context) {
        this.storagePathProvider = storagePathProvider;
        this.context = context;
    }

    public void createOdkDirsOnStorage() throws RuntimeException {
        createDirs(storagePathProvider.getOdkRootDirPaths());
    }

    public void createProjectDirsOnStorage(Project.Saved project) {
        createDirs(storagePathProvider.getProjectDirPaths(project));
    }

    private void createDirs(String[] dirPaths) {
        for (String dirPath : dirPaths) {
            File dir = new File(dirPath);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    String message = context.getString(R.string.cannot_create_directory, dirPath);
                    Timber.w(message);
                    throw new RuntimeException(message);
                }
            } else {
                if (!dir.isDirectory()) {
                    String message = context.getString(R.string.not_a_directory, dirPath);
                    Timber.w(message);
                    throw new RuntimeException(message);
                }
            }
        }
    }

}
