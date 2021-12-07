package org.odk.collect.android.notifications;

import org.odk.collect.android.formmanagement.ServerFormDetails;
import org.odk.collect.forms.FormSourceException;

import java.util.List;
import java.util.Map;

public interface Notifier {

    void onUpdatesAvailable(List<ServerFormDetails> updates);

    void onUpdatesDownloaded(Map<ServerFormDetails, String> result, String projectId);

    void onSync(FormSourceException exception, String projectId);

    void onSubmission(boolean failure, String message);
}
