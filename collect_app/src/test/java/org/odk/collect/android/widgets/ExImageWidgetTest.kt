package org.odk.collect.android.widgets

import android.view.View
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
import org.javarosa.core.model.data.StringData
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.odk.collect.android.formentry.questions.QuestionDetails
import org.odk.collect.android.injection.config.AppDependencyModule
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.android.utilities.ApplicationConstants
import org.odk.collect.android.utilities.MediaUtils
import org.odk.collect.android.widgets.base.FileWidgetTest
import org.odk.collect.android.widgets.support.FakeQuestionMediaManager
import org.odk.collect.android.widgets.support.FakeWaitingForDataRegistry
import org.odk.collect.android.widgets.utilities.FileRequester
import org.odk.collect.android.widgets.utilities.QuestionFontSizeUtils
import org.odk.collect.androidshared.system.IntentLauncher
import org.odk.collect.settings.keys.ProjectKeys.KEY_FONT_SIZE
import org.robolectric.shadows.ShadowToast
import java.io.File

class ExImageWidgetTest : FileWidgetTest<ExImageWidget>() {
    private val fileRequester = mock<FileRequester>()
    private val mediaUtils = mock<MediaUtils>()

    @Before
    fun setup() {
        CollectHelpers.overrideAppDependencyModule(object : AppDependencyModule() {
            override fun providesMediaUtils(intentLauncher: IntentLauncher): MediaUtils {
                return mediaUtils
            }
        })
        whenever(mediaUtils.isImageFile(any())).thenReturn(true)
    }

    override fun getInitialAnswer(): StringData {
        return StringData("image1.png")
    }

    override fun getNextAnswer(): StringData {
        return StringData("image2.png")
    }

    override fun createWidget(): ExImageWidget {
        return ExImageWidget(
            activity,
            QuestionDetails(formEntryPrompt, readOnlyOverride),
            FakeQuestionMediaManager(),
            FakeWaitingForDataRegistry(),
            fileRequester,
            dependencies
        )
    }

    @Test
    fun whenWidgetCreated_shouldLaunchButtonBeVisible() {
        assertThat(widget.binding.launchExternalAppButton.visibility, equalTo(View.VISIBLE))
    }

    @Test
    fun whenWidgetCreated_shouldLaunchButtonHaveProperName() {
        assertThat(widget.binding.launchExternalAppButton.text.toString(), equalTo("Launch"))
    }

    @Test
    fun whenFontSizeChanged_CustomFontSizeShouldBeUsed() {
        settingsProvider.getUnprotectedSettings().save(KEY_FONT_SIZE, "30")

        assertThat(
            widget.binding.launchExternalAppButton.textSize.toInt(),
            equalTo(QuestionFontSizeUtils.getFontSize(settingsProvider.getUnprotectedSettings(), QuestionFontSizeUtils.FontSize.BODY_LARGE))
        )
    }

    @Test
    fun whenThereIsNoAnswer_shouldImageViewBeHidden() {
        assertThat(widget.binding.imageView.visibility, equalTo(View.GONE))
    }

    @Test
    fun whenThereIsAnswer_shouldImageViewBeDisplayed() {
        whenever(formEntryPrompt.answerText).thenReturn(initialAnswer.displayText)

        assertThat(widget.binding.imageView.visibility, equalTo(View.VISIBLE))
    }

    @Test
    fun whenAnswerCleared_shouldImageViewBeHidden() {
        whenever(formEntryPrompt.answerText).thenReturn(initialAnswer.displayText)

        widget.clearAnswer()
        assertThat(widget.binding.imageView.visibility, equalTo(View.GONE))
    }

    @Test
    fun whenLaunchButtonClicked_exWidgetIntentLauncherShouldBeStarted() {
        widget.binding.launchExternalAppButton.performClick()
        verify(fileRequester).launch(activity, ApplicationConstants.RequestCodes.EX_IMAGE_CHOOSER, formEntryPrompt)
    }

    @Test
    fun whenImageViewClicked_shouldFileViewerByCalled() {
        whenever(formEntryPrompt.answerText).thenReturn(initialAnswer.displayText)

        widget.binding.imageView.performClick()
        verify(mediaUtils).openFile(activity, widget.answerFile!!, "image/*")
    }

    @Test
    fun whenUnsupportedFileTypeAttached_shouldNotThatFileBeAdded() {
        val answer = File.createTempFile("doc", ".pdf")
        whenever(mediaUtils.isImageFile(answer)).thenReturn(false)
        widget.setData(answer)
        assertThat(widget.answer, nullValue())
        assertThat(widget.binding.imageView.visibility, equalTo(View.GONE))
    }

    @Test
    fun whenUnsupportedFileTypeAttached_shouldTheFileBeRemoved() {
        val answer = File.createTempFile("doc", ".pdf")
        whenever(mediaUtils.isImageFile(answer)).thenReturn(false)
        widget.setData(answer)
        verify(mediaUtils).deleteMediaFile(answer.absolutePath)
    }

    @Test
    fun whenUnsupportedFileTypeAttached_shouldToastBeDisplayed() {
        val answer = File.createTempFile("doc", ".pdf")
        whenever(mediaUtils.isImageFile(answer)).thenReturn(false)
        widget.setData(answer)
        assertThat(ShadowToast.getTextOfLatestToast(), equalTo("Application returned an invalid file type"))
    }

    @Test
    override fun usingReadOnlyOptionShouldMakeAllClickableElementsDisabled() {
        whenever(formEntryPrompt.isReadOnly).thenReturn(true)
        whenever(formEntryPrompt.answerText).thenReturn(initialAnswer.displayText)

        assertThat(widget.binding.launchExternalAppButton.visibility, equalTo(View.GONE))
        assertThat(widget.binding.imageView.visibility, equalTo(View.VISIBLE))
    }

    @Test
    fun whenReadOnlyOverrideOptionIsUsed_shouldAllClickableElementsBeDisabled() {
        readOnlyOverride = true
        whenever(formEntryPrompt.answerText).thenReturn(initialAnswer.displayText)

        assertThat(widget.binding.launchExternalAppButton.visibility, equalTo(View.GONE))
        assertThat(widget.binding.imageView.visibility, equalTo(View.VISIBLE))
    }
}
