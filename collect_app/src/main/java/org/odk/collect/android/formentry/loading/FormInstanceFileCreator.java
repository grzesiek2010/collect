package org.odk.collect.android.formentry.loading;

import org.odk.collect.android.utilities.FileUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.function.Supplier;

import timber.log.Timber;

public class FormInstanceFileCreator {

    private final String instancesDir;
    private final Supplier<Long> clock;

    public FormInstanceFileCreator(String instancesDir, Supplier<Long> clock) {
        this.instancesDir = instancesDir;
        this.clock = clock;
    }

    public File createInstanceFileBasedOnFormName(String formDefinitionPath) {
        String formFileName = formDefinitionPath.substring(formDefinitionPath.lastIndexOf('/') + 1,
                formDefinitionPath.lastIndexOf('.'));
        return createInstanceFile(formFileName);
    }

    public File createInstanceFileBasedOnInstanceName(String instanceFileName) {
        return createInstanceFile(instanceFileName.substring(0, instanceFileName.lastIndexOf('_')));
    }

    private File createInstanceFile(String baseName) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.ENGLISH)
                .format(new Date(clock.get()));
        String instanceDir = instancesDir + File.separator + baseName + "_" + timestamp;

        if (FileUtils.createFolder(instanceDir)) {
            return new File(instanceDir + File.separator + baseName + "_" + timestamp + ".xml");
        } else {
            Timber.e(new Error("Error creating form instance file"));
            return null;
        }
    }
}
