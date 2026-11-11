package org.odk.collect.android.widgets

import android.view.View
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.core.graphics.createBitmap
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.asImage
import coil3.request.SuccessResult
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
import org.javarosa.core.model.Constants
import org.javarosa.core.model.data.StringData
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.odk.collect.android.formentry.questions.QuestionDetails
import org.odk.collect.android.injection.config.AppDependencyModule
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.android.support.WidgetTestActivity
import org.odk.collect.android.utilities.ApplicationConstants
import org.odk.collect.android.utilities.MediaUtils
import org.odk.collect.android.widgets.base.FileWidgetTest
import org.odk.collect.android.widgets.support.FakeQuestionMediaManager
import org.odk.collect.android.widgets.support.FakeWaitingForDataRegistry
import org.odk.collect.android.widgets.utilities.FileRequester
import org.odk.collect.androidshared.system.IntentLauncher
import org.odk.collect.androidtest.onNodeWithClickLabel
import org.odk.collect.strings.R.string
import org.robolectric.shadows.ShadowToast
import java.io.File

class ExImageWidgetTest : FileWidgetTest<ExImageWidget>() {
    @get:Rule
    val composeRule = createAndroidComposeRule<WidgetTestActivity>()
    private val fileRequester = mock<FileRequester>()
    private val questionMediaManager = FakeQuestionMediaManager()
    private val mediaUtils = mock<MediaUtils>()
    private val mediaWidgetAnswerViewModel = MediaWidgetAnswerViewModel(mock(), questionMediaManager, mediaUtils, mock(), mock())
    private val dependencies = QuestionWidget.Dependencies(
        null,
        mediaWidgetAnswerViewModel
    )

    @Before
    fun setup() {
        CollectHelpers.overrideAppDependencyModule(object : AppDependencyModule() {
            override fun providesMediaUtils(intentLauncher: IntentLauncher): MediaUtils {
                return mediaUtils
            }
        })
        whenever(mediaUtils.isImageFile(any())).thenReturn(true)
        whenever(formEntryPrompt.controlType).thenReturn(Constants.CONTROL_IMAGE_CHOOSE)
        activity = composeRule.activity

        SingletonImageLoader.setUnsafe { context ->
            ImageLoader.Builder(context)
                .components {
                    add { chain -> SuccessResult(createBitmap(10, 10).asImage(), chain.request) }
                }
                .build()
        }
    }

    @After
    fun teardown() {
        SingletonImageLoader.reset()
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
            questionMediaManager,
            FakeWaitingForDataRegistry(),
            fileRequester,
            dependencies
        ).also {
            widgetInComposeActivity(composeRule, it)
        }
    }

    @Test
    fun whenWidgetCreated_shouldLaunchButtonBeVisible() {
        createWidget()
        composeRule.onNodeWithClickLabel(activity.getString(string.launch_app)).assertIsDisplayed()
    }

    @Test
    fun whenWidgetCreated_shouldLaunchButtonHaveProperName() {
        createWidget()
        composeRule.onNodeWithClickLabel(activity.getString(string.launch_app)).assertTextEquals("Launch")
    }

    @Test
    fun whenThereIsNoAnswer_shouldImageViewBeHidden() {
        createWidget()
        composeRule.onNodeWithClickLabel(activity.getString(string.open_file)).assertDoesNotExist()
    }

    @Test
    fun whenThereIsAnswer_shouldImageViewBeDisplayed() {
        val file = questionMediaManager.addAnswerFile(File.createTempFile("image", ".png"))
        whenever(formEntryPrompt.answerText).thenReturn(file.name)
        createWidget()

        composeRule.onNodeWithClickLabel(activity.getString(string.open_file)).assertIsDisplayed()
    }

    @Test
    fun whenAnswerCleared_shouldImageViewBeHidden() {
        val file = questionMediaManager.addAnswerFile(File.createTempFile("image", ".png"))
        whenever(formEntryPrompt.answerText).thenReturn(file.name)

        createWidget().clearAnswer()
        composeRule.onNodeWithClickLabel(activity.getString(string.open_file)).assertDoesNotExist()
    }

    @Test
    fun whenLongPressingButtonAndAnswer_shouldContextMenuBeShown() {
        val file = questionMediaManager.addAnswerFile(File.createTempFile("image", ".png"))
        whenever(formEntryPrompt.answerText).thenReturn(file.name)
        val widget = createWidget()

        composeRule.onNodeWithClickLabel(activity.getString(string.launch_app)).performTouchInput { longClick() }
        composeRule.onNodeWithClickLabel(activity.getString(string.open_file)).performTouchInput { longClick() }

        assertThat(viewsWithShownContextMenu, equalTo(listOf<View>(widget, widget)))
    }

    @Test
    fun whenLaunchButtonClicked_exWidgetIntentLauncherShouldBeStarted() {
        createWidget()
        composeRule.onNodeWithClickLabel(activity.getString(string.launch_app)).performClick()
        verify(fileRequester).launch(activity, ApplicationConstants.RequestCodes.EX_IMAGE_CHOOSER, formEntryPrompt)
    }

    @Test
    fun whenImageViewClicked_shouldFileViewerByCalled() {
        val file = questionMediaManager.addAnswerFile(File.createTempFile("image", ".png"))
        whenever(formEntryPrompt.answerText).thenReturn(file.name)
        createWidget()

        composeRule.onNodeWithClickLabel(activity.getString(string.open_file)).performClick()
        verify(mediaUtils).openFile(any(), eq(file), eq("image/*"))
    }

    @Test
    fun whenUnsupportedFileTypeAttached_shouldNotThatFileBeAdded() {
        val answer = File.createTempFile("doc", ".pdf")
        whenever(mediaUtils.isImageFile(answer)).thenReturn(false)
        widget.setData(answer)
        assertThat(widget.answer, nullValue())
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
        val file = questionMediaManager.addAnswerFile(File.createTempFile("image", ".png"))
        whenever(formEntryPrompt.isReadOnly).thenReturn(true)
        whenever(formEntryPrompt.answerText).thenReturn(file.name)
        createWidget()

        composeRule.onNodeWithClickLabel(activity.getString(string.launch_app)).assertDoesNotExist()
        composeRule.onNodeWithClickLabel(activity.getString(string.open_file)).assertExists()
    }

    @Test
    fun whenReadOnlyOverrideOptionIsUsed_shouldAllClickableElementsBeDisabled() {
        readOnlyOverride = true
        val file = questionMediaManager.addAnswerFile(File.createTempFile("image", ".png"))
        whenever(formEntryPrompt.answerText).thenReturn(file.name)
        createWidget()

        composeRule.onNodeWithClickLabel(activity.getString(string.launch_app)).assertDoesNotExist()
        composeRule.onNodeWithClickLabel(activity.getString(string.open_file)).assertExists()
    }
}
