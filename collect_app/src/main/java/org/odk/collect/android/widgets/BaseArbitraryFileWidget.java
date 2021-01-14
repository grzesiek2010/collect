package org.odk.collect.android.widgets;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.utilities.FileUtil;
import org.odk.collect.android.utilities.MediaUtils;
import org.odk.collect.android.utilities.QuestionMediaManager;
import org.odk.collect.android.widgets.interfaces.FileWidget;
import org.odk.collect.android.widgets.interfaces.WidgetDataReceiver;
import org.odk.collect.android.widgets.utilities.WaitingForDataRegistry;

import java.io.File;

import timber.log.Timber;

public abstract class BaseArbitraryFileWidget extends QuestionWidget implements FileWidget, WidgetDataReceiver  {
    @NonNull
    private final FileUtil fileUtil;

    @NonNull
    protected final MediaUtils mediaUtils;

    private final QuestionMediaManager questionMediaManager;
    protected final WaitingForDataRegistry waitingForDataRegistry;

    protected String binaryName;

    public BaseArbitraryFileWidget(Context context, QuestionDetails questionDetails, @NonNull FileUtil fileUtil, @NonNull MediaUtils mediaUtils,
                                   QuestionMediaManager questionMediaManager, WaitingForDataRegistry waitingForDataRegistry) {
        super(context, questionDetails);
        this.fileUtil = fileUtil;
        this.mediaUtils = mediaUtils;
        this.questionMediaManager = questionMediaManager;
        this.waitingForDataRegistry = waitingForDataRegistry;
    }

    @Override
    public IAnswerData getAnswer() {
        return binaryName != null ? new StringData(binaryName) : null;
    }

    @Override
    public void deleteFile() {
        questionMediaManager.deleteAnswerFile(getFormEntryPrompt().getIndex().toString(),
                getInstanceFolder() + File.separator + binaryName);
        binaryName = null;
    }

    @Override
    public void setData(Object object) {
        if (binaryName != null) {
            deleteFile();
        }

        File newFile;
        // get the file path and create a copy in the instance folder
        if (object instanceof Uri) {
            String sourcePath = mediaUtils.getPath(getContext(), (Uri) object);
            String destinationPath = mediaUtils.getDestinationPathFromSourcePath(sourcePath, getInstanceFolder());
            File source = fileUtil.getFileAtPath(sourcePath);
            newFile = fileUtil.getFileAtPath(destinationPath);
            fileUtil.copyFile(source, newFile);
        } else if (object instanceof File) {
            // Getting a file indicates we've done the copy in the before step
            newFile = (File) object;
        } else {
            Timber.w("FileWidget's setBinaryData must receive a File or Uri object.");
            return;
        }

        if (newFile.exists()) {
            questionMediaManager.replaceAnswerFile(getFormEntryPrompt().getIndex().toString(), newFile.getAbsolutePath());
            binaryName = newFile.getName();
            showAnswerText();
            Timber.i("Setting current answer to %s", newFile.getName());

            widgetValueChanged();
        } else {
            Timber.e("Inserting Arbitrary file FAILED");
        }
    }

    protected abstract void showAnswerText();
}