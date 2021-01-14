package org.odk.collect.android.utilities;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import org.javarosa.form.api.FormEntryPrompt;
import org.javarosa.xpath.parser.XPathSyntaxException;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.exception.ExternalParamsException;
import org.odk.collect.android.external.ExternalAppsUtils;

import java.util.Map;

import timber.log.Timber;

import static android.content.Intent.ACTION_SENDTO;

public class ExternalAppIntentProvider {
    public interface OnIntentProvided {
        void onException(String message);
        void onSuccess(Intent intent);
    }

    private static final String URI_KEY = "uri_data";

    private final ActivityAvailability activityAvailability;

    public ExternalAppIntentProvider(ActivityAvailability activityAvailability) {
        this.activityAvailability = activityAvailability;
    }

    public void provideIntentToRunExternalApp(OnIntentProvided onIntentProvided, Context context, FormEntryPrompt formEntryPrompt) {
        String exSpec = formEntryPrompt.getAppearanceHint().replaceFirst("^ex[:]", "");
        final String intentName = ExternalAppsUtils.extractIntentName(exSpec);
        final Map<String, String> exParams = ExternalAppsUtils.extractParameters(exSpec);
        final String errorString;
        String v = formEntryPrompt.getSpecialFormQuestionText("noAppErrorString");
        errorString = (v != null) ? v : context.getString(R.string.no_app);

        Intent i = new Intent(intentName);

        // Use special "uri_data" key to set intent data. This must be done before checking if an
        // activity is available to handle implicit intents.
        if (exParams.containsKey(URI_KEY)) {
            try {
                String uriValue = (String) ExternalAppsUtils.getValueRepresentedBy(exParams.get(URI_KEY),
                        formEntryPrompt.getIndex().getReference());
                i.setData(Uri.parse(uriValue));
                exParams.remove(URI_KEY);
            } catch (XPathSyntaxException e) {
                Timber.d(e);
                onIntentProvided.onException(e.getMessage());
            }
        }

        if (!activityAvailability.isActivityAvailable(i)) {
            Intent launchIntent = Collect.getInstance().getPackageManager().getLaunchIntentForPackage(intentName);

            if (launchIntent != null) {
                // Make sure FLAG_ACTIVITY_NEW_TASK is not set because it doesn't work with startActivityForResult
                launchIntent.setFlags(0);
                i = launchIntent;
            }
        }

        if (activityAvailability.isActivityAvailable(i)) {
            try {
                ExternalAppsUtils.populateParameters(i, exParams, formEntryPrompt.getIndex().getReference());
                // ACTION_SENDTO used for sending text messages or emails doesn't require any results
                if (ACTION_SENDTO.equals(i.getAction())) {
                    context.startActivity(i);
                } else {
                    onIntentProvided.onSuccess(i);
                }
            } catch (ExternalParamsException | ActivityNotFoundException e) {
                Timber.d(e);
                onIntentProvided.onException(e.getMessage());
            }
        } else {
            onIntentProvided.onException(errorString);
        }
    }
}
