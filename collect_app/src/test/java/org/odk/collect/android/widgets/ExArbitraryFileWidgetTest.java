package org.odk.collect.android.widgets;

import android.content.Intent;
import android.view.View;

import androidx.annotation.NonNull;

import org.javarosa.core.model.data.StringData;
import org.javarosa.xpath.parser.XPathSyntaxException;
import org.junit.Test;
import org.mockito.Mock;
import org.odk.collect.android.exception.ExternalParamsException;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.utilities.ActivityAvailability;
import org.odk.collect.android.utilities.ExternalAppIntentProvider;
import org.odk.collect.android.utilities.MediaUtils;
import org.odk.collect.android.widgets.base.FileWidgetTest;
import org.odk.collect.android.widgets.support.FakeQuestionMediaManager;
import org.odk.collect.android.widgets.support.FakeWaitingForDataRegistry;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

public class ExArbitraryFileWidgetTest extends FileWidgetTest<ExArbitraryFileWidget> {
    @Mock
    MediaUtils mediaUtils;

    @Mock
    ExternalAppIntentProvider externalAppIntentProvider;

    @Override
    public StringData getInitialAnswer() {
        return new StringData("document.pdf");
    }

    @NonNull
    @Override
    public StringData getNextAnswer() {
        return new StringData("document.xlsx");
    }

    @NonNull
    @Override
    public ExArbitraryFileWidget createWidget() {
        return new ExArbitraryFileWidget(activity, new QuestionDetails(formEntryPrompt, "formAnalyticsID", readOnlyOverride),
                mediaUtils, new FakeQuestionMediaManager(), new FakeWaitingForDataRegistry(), externalAppIntentProvider, new ActivityAvailability(activity));
    }

    @Test
    public void whenThereIsNoAnswer_shouldAnswerTextBeHidden() {
        assertThat(getWidget().binding.exArbitraryFileAnswerText.getVisibility(), is(View.GONE));
    }

    @Test
    public void whenThereIsAnswer_shouldAnswerTextBeDisplayed() {
        when(formEntryPrompt.getAnswerText()).thenReturn(getInitialAnswer().getDisplayText());

        ExArbitraryFileWidget widget = getWidget();
        assertThat(widget.binding.exArbitraryFileAnswerText.getVisibility(), is(View.VISIBLE));
        assertThat(widget.binding.exArbitraryFileAnswerText.getText(), is(getInitialAnswer().getDisplayText()));
    }

    @Test
    public void whenClickingOnButton_shouldFilePickerByCalled() throws ExternalParamsException, XPathSyntaxException {
        Intent intent = mock(Intent.class);
        when(externalAppIntentProvider.getIntentToRunExternalApp(any(), any(), any())).thenReturn(intent);
        getWidget().binding.exArbitraryFileButton.performClick();
        assertThat(shadowOf(activity).getNextStartedActivity(), is(intent));
    }

    @Test
    public void whenClickingOnAnswer_shouldFileViewerByCalled() {
        when(formEntryPrompt.getAnswerText()).thenReturn(getInitialAnswer().getDisplayText());

        ExArbitraryFileWidget widget = getWidget();
        widget.binding.exArbitraryFileAnswerText.performClick();
        verify(mediaUtils).openFile(activity, widget.answerFile, null);
    }

    @Test
    public void whenClearAnswerCall_shouldAnswerTextBeHidden() {
        when(formEntryPrompt.getAnswerText()).thenReturn(getInitialAnswer().getDisplayText());

        ExArbitraryFileWidget widget = getWidget();
        widget.clearAnswer();
        assertThat(widget.binding.exArbitraryFileAnswerText.getVisibility(), is(View.GONE));
    }

    @Test
    public void usingReadOnlyOptionShouldMakeAllClickableElementsDisabled() {
        when(formEntryPrompt.isReadOnly()).thenReturn(true);
        when(formEntryPrompt.getAnswerText()).thenReturn(getInitialAnswer().getDisplayText());

        ExArbitraryFileWidget widget = getWidget();
        assertThat(widget.binding.exArbitraryFileButton.getVisibility(), is(View.GONE));
        assertThat(widget.binding.exArbitraryFileAnswerText.getVisibility(), is(View.VISIBLE));
        assertThat(widget.binding.exArbitraryFileAnswerText.getText(), is(getInitialAnswer().getDisplayText()));
    }
}
