package org.odk.collect.android.widgets

import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.provider.MediaStore
import android.view.View
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.core.graphics.createBitmap
import androidx.core.util.Pair
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.asImage
import coil3.imageDecoderEnabled
import coil3.request.ErrorResult
import coil3.request.SuccessResult
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.javarosa.core.model.Constants
import org.javarosa.core.model.data.StringData
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.odk.collect.android.formentry.questions.QuestionDetails
import org.odk.collect.android.support.CollectHelpers.overrideReferenceManager
import org.odk.collect.android.support.CollectHelpers.setupFakeReferenceManager
import org.odk.collect.android.support.MockFormEntryPromptBuilder
import org.odk.collect.android.support.WidgetTestActivity
import org.odk.collect.android.widgets.base.BinaryWidgetTest
import org.odk.collect.android.widgets.support.FakeQuestionMediaManager
import org.odk.collect.android.widgets.support.FakeWaitingForDataRegistry
import org.odk.collect.androidtest.onNodeWithClickLabel
import org.odk.collect.draw.DrawActivity
import org.odk.collect.shared.TempFiles
import org.odk.collect.strings.R.string
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowBitmapFactory
import org.robolectric.shadows.ShadowToast
import java.io.File

class AnnotateWidgetTest : BinaryWidgetTest<AnnotateWidget, StringData>() {
    @get:Rule
    val composeRule = createAndroidComposeRule<WidgetTestActivity>()
    private val questionMediaManager = FakeQuestionMediaManager()
    private val mediaWidgetAnswerViewModel = MediaWidgetAnswerViewModel(mock(), questionMediaManager, mock(), mock(), mock())
    private val dependencies = QuestionWidget.Dependencies(
        null,
        mediaWidgetAnswerViewModel
    )

    private val file = TempFiles.createTempFile("sample", ".jpg")

    @Before
    fun setup() {
        SingletonImageLoader.setUnsafe { context ->
            ImageLoader.Builder(context)
                .imageDecoderEnabled(false)
                .build()
        }
    }

    @After
    fun teardown() {
        SingletonImageLoader.reset()
    }

    override fun createWidget(): AnnotateWidget {
        whenever(formEntryPrompt.controlType).thenReturn(Constants.CONTROL_IMAGE_CHOOSE)

        return AnnotateWidget(
            composeRule.activity,
            QuestionDetails(formEntryPrompt, readOnlyOverride),
            questionMediaManager,
            FakeWaitingForDataRegistry(),
            TempFiles.getPathInTempDir(),
            dependencies
        ).also {
            widgetInComposeActivity(composeRule, it)
            activity = composeRule.activity
        }
    }

    override fun getNextAnswer(): StringData {
        return StringData(file.name)
    }

    override fun createBinaryData(answerData: StringData): Any {
        return file
    }

    @Test
    fun `setting a new answer removes the old file and keeps the new one`() {
        super.settingANewAnswerShouldRemoveTheOldAnswer()

        val promptIndex = formEntryPrompt.index.toString()
        assertThat(questionMediaManager.originalFiles[promptIndex], equalTo(formEntryPrompt.answerText))
        assertThat(questionMediaManager.recentFiles[promptIndex], notNullValue())
    }

    @Test
    fun `clearing the answer removes the existing answer and its file`() {
        super.callingClearShouldRemoveTheExistingAnswer()

        val promptIndex = formEntryPrompt.index.toString()
        assertThat(questionMediaManager.originalFiles[promptIndex], equalTo(formEntryPrompt.answerText))
        assertThat(questionMediaManager.recentFiles[promptIndex], equalTo(null))
    }

    @Test
    fun `the buttons launch the correct intents when there is no custom package`() {
        val answerFile = questionMediaManager.addAnswerFile(File.createTempFile("current", ".png"))
        formEntryPrompt = MockFormEntryPromptBuilder(formEntryPrompt)
            .withAnswerDisplayText(answerFile.name)
            .build()

        stubAllRuntimePermissionsGranted(true)

        composeRule.onNodeWithClickLabel(activity.getString(string.capture_image)).performClick()
        var intent = shadowOf(activity).nextStartedActivity
        assertActionEquals(MediaStore.ACTION_IMAGE_CAPTURE, intent)
        assertThat(intent.getPackage(), equalTo(null))

        composeRule.onNodeWithClickLabel(activity.getString(string.choose_image)).performClick()
        intent = shadowOf(activity).nextStartedActivity
        assertActionEquals(Intent.ACTION_GET_CONTENT, intent)

        composeRule.onNodeWithClickLabel(activity.getString(string.markup_image)).performClick()
        intent = shadowOf(activity).nextStartedActivity
        assertComponentEquals(activity, DrawActivity::class.java, intent)
        assertExtraEquals(DrawActivity.OPTION, DrawActivity.OPTION_ANNOTATE, intent)
    }

    @Test
    fun `the buttons launch the correct intents when a custom package is set`() {
        formEntryPrompt = MockFormEntryPromptBuilder(formEntryPrompt)
            .withAdditionalAttribute("intent", "com.customcameraapp")
            .build()

        stubAllRuntimePermissionsGranted(true)

        composeRule.onNodeWithClickLabel(activity.getString(string.capture_image)).performClick()
        var intent = shadowOf(activity).nextStartedActivity
        assertActionEquals(MediaStore.ACTION_IMAGE_CAPTURE, intent)
        assertThat(intent.getPackage(), equalTo("com.customcameraapp"))

        composeRule.onNodeWithClickLabel(activity.getString(string.choose_image)).performClick()
        intent = shadowOf(activity).nextStartedActivity
        assertActionEquals(Intent.ACTION_GET_CONTENT, intent)
        assertTypeEquals("image/*", intent)
    }

    @Test
    fun `the capture button does not launch an intent when permissions are denied`() {
        stubAllRuntimePermissionsGranted(false)

        composeRule.onNodeWithClickLabel(activity.getString(string.capture_image)).performClick()
        assertThat(shadowOf(activity).nextStartedActivity, equalTo(null))
    }

    @Test
    override fun usingReadOnlyOptionShouldMakeAllClickableElementsDisabled() {
        formEntryPrompt = MockFormEntryPromptBuilder(formEntryPrompt)
            .withReadOnly(true)
            .build()
        createWidget()

        composeRule.onNodeWithClickLabel(activity.getString(string.capture_image)).assertDoesNotExist()
        composeRule.onNodeWithClickLabel(activity.getString(string.choose_image)).assertDoesNotExist()
        composeRule.onNodeWithClickLabel(activity.getString(string.markup_image)).assertDoesNotExist()
    }

    @Test
    fun `all buttons are hidden when read-only override is used`() {
        readOnlyOverride = true
        formEntryPrompt = MockFormEntryPromptBuilder(formEntryPrompt)
            .withReadOnly(false)
            .build()
        createWidget()

        composeRule.onNodeWithClickLabel(activity.getString(string.capture_image)).assertDoesNotExist()
        composeRule.onNodeWithClickLabel(activity.getString(string.choose_image)).assertDoesNotExist()
        composeRule.onNodeWithClickLabel(activity.getString(string.markup_image)).assertDoesNotExist()
    }

    @Test
    fun `the image is hidden when there is no answer`() {
        createWidget()
        composeRule.onNodeWithClickLabel(activity.getString(string.open_file)).assertDoesNotExist()
    }

    @Test
    fun `the image is displayed when there is an answer`() {
        val answerFile = questionMediaManager.addAnswerFile(File.createTempFile("current", ".png"))
        formEntryPrompt = MockFormEntryPromptBuilder(formEntryPrompt)
            .withAnswerDisplayText(answerFile.name)
            .build()

        createWidget()
        composeRule.onNodeWithClickLabel(activity.getString(string.open_file)).assertExists()
        composeRule.onNodeWithText(activity.getString(string.selected_invalid_image)).assertDoesNotExist()
    }

    @Test
    fun `a gif file is not attached and a message is displayed when it is selected`() {
        val widget = createWidget()

        val gif = TempFiles.createTempFile("sample", ".gif")
        questionMediaManager.addAnswerFile(gif)
        widget.setData(gif)

        assertThat(widget.answer, equalTo(null))
        composeRule.onNodeWithClickLabel(activity.getString(string.open_file)).assertDoesNotExist()
        assertThat(ShadowToast.getTextOfLatestToast(), equalTo("Gif files are not supported"))
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `the error message is displayed when the answer image can't be loaded`() {
        SingletonImageLoader.setUnsafe { context ->
            ImageLoader.Builder(context)
                .components {
                    add { chain -> ErrorResult(null, chain.request, Throwable()) }
                }
                .build()
        }

        val answerFile = questionMediaManager.addAnswerFile(File.createTempFile("current", ".png"))
        formEntryPrompt = MockFormEntryPromptBuilder(formEntryPrompt)
            .withAnswerDisplayText(answerFile.name)
            .build()

        createWidget()
        composeRule.waitUntilAtLeastOneExists(hasText(activity.getString(string.selected_invalid_image)))
    }

    @Test
    fun `neither the image nor the error message is displayed when the answer is a reference`() {
        val imagePath = File.createTempFile("default", ".bmp").absolutePath
        overrideReferenceManager(setupFakeReferenceManager(listOf(Pair(DrawWidgetTest.REFERENCE_URI, imagePath))))

        formEntryPrompt = MockFormEntryPromptBuilder(formEntryPrompt)
            .withAnswerDisplayText(DrawWidgetTest.REFERENCE_URI)
            .build()

        createWidget()
        composeRule.onNodeWithClickLabel(activity.getString(string.open_file)).assertDoesNotExist()
        composeRule.onNodeWithText(activity.getString(string.selected_invalid_image)).assertDoesNotExist()
    }

    @Test
    fun `the capture and choose buttons are disabled but the annotate button is enabled when the answer is a reference`() {
        val imagePath = File.createTempFile("default", ".bmp").absolutePath
        overrideReferenceManager(setupFakeReferenceManager(listOf(Pair(DrawWidgetTest.REFERENCE_URI, imagePath))))

        formEntryPrompt = MockFormEntryPromptBuilder(formEntryPrompt)
            .withAnswerDisplayText(DrawWidgetTest.REFERENCE_URI)
            .build()

        createWidget()
        composeRule.onNodeWithClickLabel(activity.getString(string.capture_image)).assertIsNotEnabled()
        composeRule.onNodeWithClickLabel(activity.getString(string.choose_image)).assertIsNotEnabled()
        composeRule.onNodeWithClickLabel(activity.getString(string.markup_image)).assertIsEnabled()
    }

    @Test
    fun `all buttons are enabled when there is an answer`() {
        val answerFile = questionMediaManager.addAnswerFile(File.createTempFile("current", ".png"))
        formEntryPrompt = MockFormEntryPromptBuilder(formEntryPrompt)
            .withAnswerDisplayText(answerFile.name)
            .build()

        createWidget()
        composeRule.onNodeWithClickLabel(activity.getString(string.capture_image)).assertIsEnabled()
        composeRule.onNodeWithClickLabel(activity.getString(string.choose_image)).assertIsEnabled()
        composeRule.onNodeWithClickLabel(activity.getString(string.markup_image)).assertIsEnabled()
    }

    @Test
    fun `the capture and choose buttons are enabled but the annotate button is disabled when there is no answer`() {
        createWidget()

        composeRule.onNodeWithClickLabel(activity.getString(string.capture_image)).assertIsEnabled()
        composeRule.onNodeWithClickLabel(activity.getString(string.choose_image)).assertIsEnabled()
        composeRule.onNodeWithClickLabel(activity.getString(string.markup_image)).assertIsNotEnabled()
    }

    @Test
    fun `the annotate button is enabled after an answer is set`() {
        val widget = createWidget()
        composeRule.onNodeWithClickLabel(activity.getString(string.markup_image)).assertIsNotEnabled()

        val answerFile = questionMediaManager.addAnswerFile(File.createTempFile("current", ".png"))
        widget.setData(answerFile)

        composeRule.onNodeWithClickLabel(activity.getString(string.markup_image)).assertIsEnabled()
    }

    @Test
    fun `the annotate button is disabled when the image does not exist`() {
        overrideReferenceManager(setupFakeReferenceManager(listOf(Pair(DrawWidgetTest.REFERENCE_URI, "bad_path"))))

        formEntryPrompt = MockFormEntryPromptBuilder(formEntryPrompt)
            .withAnswerDisplayText(DrawWidgetTest.REFERENCE_URI)
            .build()
        createWidget()
        composeRule.onNodeWithClickLabel(activity.getString(string.markup_image)).assertIsNotEnabled()

        formEntryPrompt = MockFormEntryPromptBuilder(formEntryPrompt)
            .withAnswerDisplayText(DrawWidgetTest.USER_SPECIFIED_IMAGE_ANSWER)
            .build()
        createWidget()
        composeRule.onNodeWithClickLabel(activity.getString(string.markup_image)).assertIsNotEnabled()
    }

    @Test
    fun `long-pressing the buttons and the answer shows the context menu`() {
        SingletonImageLoader.setUnsafe { context ->
            ImageLoader.Builder(context)
                .components {
                    add { chain -> SuccessResult(createBitmap(10, 10).asImage(), chain.request) }
                }
                .build()
        }
        val answerFile = questionMediaManager.addAnswerFile(File.createTempFile("current", ".png"))
        formEntryPrompt = MockFormEntryPromptBuilder(formEntryPrompt)
            .withAnswerDisplayText(answerFile.name)
            .build()

        val widget = createWidget()

        composeRule.onNodeWithClickLabel(activity.getString(string.capture_image)).performTouchInput { longClick() }
        composeRule.onNodeWithClickLabel(activity.getString(string.choose_image)).performTouchInput { longClick() }
        composeRule.onNodeWithClickLabel(activity.getString(string.markup_image)).performTouchInput { longClick() }
        composeRule.onNodeWithClickLabel(activity.getString(string.open_file)).performTouchInput { longClick() }

        assertThat(viewsWithShownContextMenu, equalTo(listOf<View>(widget, widget, widget, widget)))
    }

    @Test
    fun `clicking the annotate button passes the reference to DrawActivity when the answer is a reference`() {
        val referenceFile = File.createTempFile("default", ".bmp")
        overrideReferenceManager(setupFakeReferenceManager(listOf(Pair(DrawWidgetTest.REFERENCE_URI, referenceFile.absolutePath))))

        formEntryPrompt = MockFormEntryPromptBuilder(formEntryPrompt)
            .withAnswerDisplayText(DrawWidgetTest.REFERENCE_URI)
            .build()

        createWidget()
        composeRule.onNodeWithClickLabel(activity.getString(string.markup_image)).performClick()

        val intent = shadowOf(activity).nextStartedActivity
        assertComponentEquals(activity, DrawActivity::class.java, intent)
        assertExtraEquals(DrawActivity.OPTION, DrawActivity.OPTION_ANNOTATE, intent)
        assertExtraEquals(DrawActivity.REF_IMAGE, Uri.fromFile(referenceFile), intent)
    }

    @Test
    fun `DrawActivity is launched in portrait when the reference image is portrait`() {
        assertDrawActivityOrientationForReferenceImage(100, 200, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
    }

    @Test
    fun `DrawActivity is launched in landscape when the reference image is landscape`() {
        assertDrawActivityOrientationForReferenceImage(200, 100, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
    }

    private fun assertDrawActivityOrientationForReferenceImage(width: Int, height: Int, expectedOrientation: Int) {
        val referenceFile = File.createTempFile("default", ".bmp")
        ShadowBitmapFactory.provideWidthAndHeightHints(referenceFile.absolutePath, width, height)
        overrideReferenceManager(setupFakeReferenceManager(listOf(Pair(DrawWidgetTest.REFERENCE_URI, referenceFile.absolutePath))))

        formEntryPrompt = MockFormEntryPromptBuilder(formEntryPrompt)
            .withAnswerDisplayText(DrawWidgetTest.REFERENCE_URI)
            .build()

        createWidget()
        composeRule.onNodeWithClickLabel(activity.getString(string.markup_image)).performClick()

        val intent = shadowOf(activity).nextStartedActivity
        assertExtraEquals(DrawActivity.SCREEN_ORIENTATION, expectedOrientation, intent)
    }
}
