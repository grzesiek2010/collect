package org.odk.collect.android.widgets

import android.net.Uri
import android.view.View
import androidx.compose.ui.test.ExperimentalTestApi
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
import net.bytebuddy.utility.RandomString
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
import java.io.File

class SignatureWidgetTest : BinaryWidgetTest<SignatureWidget, StringData>() {
    @get:Rule
    val composeRule = createAndroidComposeRule<WidgetTestActivity>()
    private val questionMediaManager = FakeQuestionMediaManager()
    private val mediaWidgetAnswerViewModel = MediaWidgetAnswerViewModel(mock(), questionMediaManager, mock(), mock(), mock())
    private val dependencies = QuestionWidget.Dependencies(
        null,
        mediaWidgetAnswerViewModel
    )

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

    override fun createWidget(): SignatureWidget {
        whenever(formEntryPrompt.controlType).thenReturn(Constants.CONTROL_IMAGE_CHOOSE)

        return SignatureWidget(
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
        return StringData(RandomString.make())
    }

    override fun createBinaryData(answerData: StringData): Any {
        return TempFiles.createTempFileWithName(answerData.displayText)
    }

    @Test
    fun `setting a new answer removes the old file and keeps the new one`() {
        super.settingANewAnswerShouldRemoveTheOldAnswer()

        val promptIndex = formEntryPrompt.index.toString()
        assertThat(questionMediaManager.originalFiles[promptIndex], equalTo(questionMediaManager.getAnswerFile(formEntryPrompt.answerText)!!.absolutePath))
        assertThat(questionMediaManager.recentFiles[promptIndex], notNullValue())
    }

    @Test
    fun `clearing the answer removes the existing answer and its file`() {
        super.callingClearShouldRemoveTheExistingAnswer()

        val promptIndex = formEntryPrompt.index.toString()
        assertThat(questionMediaManager.originalFiles[promptIndex], equalTo(questionMediaManager.getAnswerFile(formEntryPrompt.answerText)!!.absolutePath))
        assertThat(questionMediaManager.recentFiles[promptIndex], equalTo(null))
    }

    @Test
    override fun usingReadOnlyOptionShouldMakeAllClickableElementsDisabled() {
        formEntryPrompt = MockFormEntryPromptBuilder(formEntryPrompt)
            .withReadOnly(true)
            .build()
        createWidget()

        composeRule.onNodeWithClickLabel(activity.getString(string.sign_button)).assertDoesNotExist()
    }

    @Test
    fun `the sign button is hidden when read-only override is used`() {
        readOnlyOverride = true
        formEntryPrompt = MockFormEntryPromptBuilder(formEntryPrompt)
            .withReadOnly(false)
            .build()
        createWidget()

        composeRule.onNodeWithClickLabel(activity.getString(string.sign_button)).assertDoesNotExist()
    }

    @Test
    fun `the image is hidden when there is no answer`() {
        createWidget()
        composeRule.onNodeWithClickLabel(activity.getString(string.open_file)).assertDoesNotExist()
    }

    @Test
    fun `the image is displayed when there is an answer`() {
        val file = questionMediaManager.addAnswerFile(File.createTempFile("current", ".png"))

        formEntryPrompt = MockFormEntryPromptBuilder(formEntryPrompt)
            .withAnswerDisplayText(file.name)
            .build()

        createWidget()
        composeRule.onNodeWithClickLabel(activity.getString(string.open_file)).assertExists()
        composeRule.onNodeWithText(activity.getString(string.selected_invalid_image)).assertDoesNotExist()
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

        val file = questionMediaManager.addAnswerFile(File.createTempFile("current", ".png"))
        formEntryPrompt = MockFormEntryPromptBuilder(formEntryPrompt)
            .withAnswerDisplayText(file.name)
            .build()

        createWidget()
        composeRule.waitUntilAtLeastOneExists(hasText(activity.getString(string.selected_invalid_image)))
    }

    @Test
    fun `neither the image nor the error message is displayed when the answer is a reference`() {
        formEntryPrompt = MockFormEntryPromptBuilder(formEntryPrompt)
            .withAnswerDisplayText(DrawWidgetTest.REFERENCE_URI)
            .build()

        createWidget()
        composeRule.onNodeWithClickLabel(activity.getString(string.open_file)).assertDoesNotExist()
        composeRule.onNodeWithText(activity.getString(string.selected_invalid_image)).assertDoesNotExist()
    }

    @Test
    fun `long-pressing the button and the answer shows the context menu`() {
        SingletonImageLoader.setUnsafe { context ->
            ImageLoader.Builder(context)
                .components {
                    add { chain -> SuccessResult(createBitmap(10, 10).asImage(), chain.request) }
                }
                .build()
        }
        val file = questionMediaManager.addAnswerFile(File.createTempFile("current", ".png"))

        formEntryPrompt = MockFormEntryPromptBuilder(formEntryPrompt)
            .withAnswerDisplayText(file.name)
            .build()

        val widget = createWidget()

        composeRule.onNodeWithClickLabel(activity.getString(string.sign_button)).performTouchInput { longClick() }
        composeRule.onNodeWithClickLabel(activity.getString(string.open_file)).performTouchInput { longClick() }

        assertThat(viewsWithShownContextMenu, equalTo(listOf<View>(widget, widget)))
    }

    @Test
    fun `clicking the sign button passes the reference to DrawActivity when the answer is a reference`() {
        val file = File.createTempFile("default", ".bmp")
        overrideReferenceManager(setupFakeReferenceManager(listOf(Pair(DrawWidgetTest.REFERENCE_URI, file.absolutePath))))

        formEntryPrompt = MockFormEntryPromptBuilder(formEntryPrompt)
            .withAnswerDisplayText(DrawWidgetTest.REFERENCE_URI)
            .build()

        createWidget()
        composeRule.onNodeWithClickLabel(activity.getString(string.sign_button)).performClick()

        val intent = shadowOf(activity).nextStartedActivity
        assertComponentEquals(activity, DrawActivity::class.java, intent)
        assertExtraEquals(DrawActivity.OPTION, DrawActivity.OPTION_SIGNATURE, intent)
        assertExtraEquals(DrawActivity.REF_IMAGE, Uri.fromFile(file), intent)
    }

    @Test
    fun `clicking the sign button does not pass the reference to DrawActivity when it does not exist`() {
        overrideReferenceManager(setupFakeReferenceManager(listOf(Pair(DrawWidgetTest.REFERENCE_URI, "/something"))))

        formEntryPrompt = MockFormEntryPromptBuilder(formEntryPrompt)
            .withAnswerDisplayText(DrawWidgetTest.REFERENCE_URI)
            .build()

        createWidget()
        composeRule.onNodeWithClickLabel(activity.getString(string.sign_button)).performClick()

        val intent = shadowOf(activity).nextStartedActivity
        assertComponentEquals(activity, DrawActivity::class.java, intent)
        assertExtraEquals(DrawActivity.OPTION, DrawActivity.OPTION_SIGNATURE, intent)
        assertThat(intent.hasExtra(DrawActivity.REF_IMAGE), equalTo(false))
    }

    @Test
    fun `clicking the sign button does not pass a reference to DrawActivity when there is no answer`() {
        createWidget()
        composeRule.onNodeWithClickLabel(activity.getString(string.sign_button)).performClick()

        val intent = shadowOf(activity).nextStartedActivity
        assertComponentEquals(activity, DrawActivity::class.java, intent)
        assertExtraEquals(DrawActivity.OPTION, DrawActivity.OPTION_SIGNATURE, intent)
        assertThat(intent.hasExtra(DrawActivity.REF_IMAGE), equalTo(false))
    }
}
