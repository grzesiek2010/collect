package org.odk.collect.android.widgets.utilities;

import android.app.Activity;
import android.content.Intent;
import android.provider.MediaStore;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.javarosa.form.api.FormEntryPrompt;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.fakes.FakePermissionsProvider;
import org.odk.collect.android.formentry.FormEntryViewModel;
import org.odk.collect.android.utilities.ApplicationConstants;
import org.odk.collect.android.widgets.support.FakeWaitingForDataRegistry;
import org.odk.collect.androidshared.utils.IntentLauncher;
import org.odk.collect.androidshared.utils.IntentLauncherImpl;
import org.odk.collect.testshared.ErrorIntentLauncher;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowToast;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithAnswer;
import static org.robolectric.Shadows.shadowOf;

@RunWith(AndroidJUnit4.class)
public class ExternalAppRecordingRequesterTest {

    private IntentLauncher intentLauncher;
    private final FakePermissionsProvider permissionsProvider = new FakePermissionsProvider();
    private final FakeWaitingForDataRegistry waitingForDataRegistry = new FakeWaitingForDataRegistry();

    private Activity activity;
    private ExternalAppRecordingRequester requester;

    @Before
    public void setup() {
        activity = Robolectric.buildActivity(Activity.class).get();
    }

    private void setupRequester() {
        requester = new ExternalAppRecordingRequester(activity, intentLauncher, waitingForDataRegistry, permissionsProvider, mock(FormEntryViewModel.class));
    }

    @Test
    public void requestRecording_whenIntentIsNotAvailable_doesNotStartAnyIntentAndCancelsWaitingForData() {
        intentLauncher = new ErrorIntentLauncher();
        setupRequester();

        permissionsProvider.setPermissionGranted(true);

        requester.requestRecording(promptWithAnswer(null));

        Intent startedActivity = shadowOf(activity).getNextStartedActivity();
        String toastMessage = ShadowToast.getTextOfLatestToast();
        assertThat(startedActivity, nullValue());
        assertThat(waitingForDataRegistry.waiting.isEmpty(), is(true));
        assertThat(toastMessage, equalTo(activity.getString(R.string.activity_not_found, activity.getString(R.string.capture_audio))));
    }

    @Test
    public void requestRecording_whenPermissionIsNotGranted_doesNotStartAnyIntentAndCancelsWaitingForData() {
        intentLauncher = IntentLauncherImpl.INSTANCE;
        setupRequester();

        permissionsProvider.setPermissionGranted(false);

        requester.requestRecording(promptWithAnswer(null));

        Intent startedActivity = shadowOf(activity).getNextStartedActivity();
        assertThat(startedActivity, nullValue());
        assertThat(waitingForDataRegistry.waiting.isEmpty(), equalTo(true));
    }

    @Test
    public void requestRecording_whenPermissionIsGranted_startsRecordSoundIntentAndSetsWidgetWaitingForData() {
        intentLauncher = IntentLauncherImpl.INSTANCE;
        setupRequester();

        permissionsProvider.setPermissionGranted(true);

        FormEntryPrompt prompt = promptWithAnswer(null);
        requester.requestRecording(prompt);

        Intent startedActivity = shadowOf(activity).getNextStartedActivity();
        assertThat(startedActivity.getAction(), equalTo(MediaStore.Audio.Media.RECORD_SOUND_ACTION));
        assertThat(startedActivity.getStringExtra(MediaStore.EXTRA_OUTPUT), equalTo(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                .toString()));

        ShadowActivity.IntentForResult intentForResult = shadowOf(activity).getNextStartedActivityForResult();
        assertThat(intentForResult.requestCode, equalTo(ApplicationConstants.RequestCodes.AUDIO_CAPTURE));

        assertThat(waitingForDataRegistry.waiting.contains(prompt.getIndex()), equalTo(true));
    }
}
