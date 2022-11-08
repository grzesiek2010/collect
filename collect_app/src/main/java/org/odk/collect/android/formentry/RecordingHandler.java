package org.odk.collect.android.formentry;

import androidx.lifecycle.LifecycleOwner;

import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.instance.TreeReference;
import org.odk.collect.android.audio.AudioFileAppender;
import org.odk.collect.android.exception.JavaRosaException;
import org.odk.collect.android.javarosawrapper.FormController;
import org.odk.collect.android.utilities.QuestionMediaManager;
import org.odk.collect.audiorecorder.recording.RecordingSession;
import org.odk.collect.utilities.Result;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.function.Consumer;

import timber.log.Timber;

public class RecordingHandler {

    private final QuestionMediaManager questionMediaManager;
    private final LifecycleOwner lifecycleOwner;
    private final AudioFileAppender amrAppender;
    private final AudioFileAppender m4aAppender;

    public RecordingHandler(QuestionMediaManager questionMediaManager, LifecycleOwner lifecycleOwner, AudioFileAppender amrAppender, AudioFileAppender m4aAppender) {
        this.questionMediaManager = questionMediaManager;
        this.lifecycleOwner = lifecycleOwner;
        this.amrAppender = amrAppender;
        this.m4aAppender = m4aAppender;
    }

    public void handle(FormController formController, RecordingSession session, Consumer<Boolean> onRecordingHandled) {
        questionMediaManager.createAnswerFile(session.getFile()).observe(lifecycleOwner, result -> {
            if (result != null && result.isSuccess()) {
                try {
                    if (session.getId() instanceof FormIndex) {
                        handleForegroundRecording(formController, session, result);
                    } else if (session.getId() instanceof HashSet) {
                        handleBackgroundRecording(formController, session, result);
                    }

                    onRecordingHandled.accept(true);
                } catch (JavaRosaException | IOException e) {
                    Timber.e(e);
                    onRecordingHandled.accept(false);
                }
            }
        });
    }

    private void handleForegroundRecording(FormController formController, RecordingSession session, Result<File> result) throws JavaRosaException {
        FormIndex formIndex = (FormIndex) session.getId();
        questionMediaManager.replaceAnswerFile(formIndex.toString(), result.getOrNull().getAbsolutePath());
        formController.answerQuestion(formIndex, new StringData(result.getOrNull().getName()));
        session.getFile().delete();
    }

    private void handleBackgroundRecording(FormController formController, RecordingSession session, Result<File> result) throws IOException {
        HashSet<TreeReference> treeReferences = (HashSet<TreeReference>) session.getId();

        TreeReference firstReference = treeReferences.iterator().next();
        StringData answer = (StringData) formController.getAnswer(firstReference);

        if (answer != null) {
            File existingAnswerFile = questionMediaManager.getAnswerFile((String) answer.getValue());
            if (existingAnswerFile != null && existingAnswerFile.exists()) {
                File newAnswerFile = result.getOrNull();

                if (newAnswerFile.getName().endsWith(".m4a")) {
                    m4aAppender.append(existingAnswerFile, newAnswerFile);
                } else {
                    amrAppender.append(existingAnswerFile, newAnswerFile);
                }

                newAnswerFile.delete();
            } else {
                for (TreeReference treeReference : treeReferences) {
                    formController.getFormDef().setValue(new StringData(result.getOrNull().getName()), treeReference, false);
                }
            }
        } else {
            for (TreeReference treeReference : treeReferences) {
                formController.getFormDef().setValue(new StringData(result.getOrNull().getName()), treeReference, false);
            }
        }

        session.getFile().delete();
    }
}
