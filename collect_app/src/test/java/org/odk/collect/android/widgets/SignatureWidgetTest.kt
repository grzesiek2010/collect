package org.odk.collect.android.widgets

import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.view.View
import androidx.core.util.Pair
import net.bytebuddy.utility.RandomString
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.notNullValue
import org.hamcrest.Matchers.nullValue
import org.javarosa.core.model.data.StringData
import org.javarosa.core.reference.ReferenceManager
import org.junit.Test
import org.mockito.kotlin.whenever
import org.odk.collect.android.R
import org.odk.collect.android.formentry.questions.QuestionDetails
import org.odk.collect.android.injection.config.AppDependencyModule
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.android.support.CollectHelpers.setupFakeReferenceManager
import org.odk.collect.android.support.MockFormEntryPromptBuilder
import org.odk.collect.android.widgets.base.BinaryWidgetTest
import org.odk.collect.android.widgets.support.FakeQuestionMediaManager
import org.odk.collect.android.widgets.support.FakeWaitingForDataRegistry
import org.odk.collect.android.widgets.support.SynchronousImageLoader
import org.odk.collect.draw.DrawActivity
import org.odk.collect.imageloader.ImageLoader
import org.odk.collect.shared.TempFiles
import org.robolectric.Shadows.shadowOf
import java.io.File

class SignatureWidgetTest : BinaryWidgetTest<SignatureWidget, StringData>() {
    private var currentFile: File? = null
    private val questionMediaManager = object : FakeQuestionMediaManager() {
        override fun getAnswerFile(fileName: String?): File? {
            return if (currentFile == null) {
                super.getAnswerFile(fileName)
            } else {
                if (fileName == DrawWidgetTest.USER_SPECIFIED_IMAGE_ANSWER) currentFile else null
            }
        }
    }

    override fun createWidget(): SignatureWidget {
        return SignatureWidget(
            activity,
            QuestionDetails(formEntryPrompt, readOnlyOverride),
            questionMediaManager,
            FakeWaitingForDataRegistry(),
            TempFiles.getPathInTempDir(),
            dependencies
        )
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
    override fun usingReadOnlyOptionShouldMakeAllClickableElementsDisabled() {
        whenever(formEntryPrompt.isReadOnly).thenReturn(true)

        assertThat(spyWidget.binding.signButton.visibility, `is`(View.GONE))
    }

    @Test
    fun `the sign button is hidden when read-only override is used`() {
        readOnlyOverride = true
        whenever(formEntryPrompt.isReadOnly).thenReturn(false)

        assertThat(spyWidget.binding.signButton.visibility, `is`(View.GONE))
    }

    @Test
    fun `the image and error message are hidden when there is no answer`() {
        val widget = createWidget()

        assertThat(widget.binding.image.visibility, `is`(View.GONE))
        assertThat(widget.binding.image.drawable, nullValue())

        assertThat(widget.binding.errorMessage.visibility, `is`(View.GONE))
    }

    @Test
    fun `the image is hidden and the error message is displayed when the answer can't be loaded`() {
        CollectHelpers.overrideAppDependencyModule(object : AppDependencyModule() {
            override fun providesImageLoader(): ImageLoader {
                return SynchronousImageLoader(true)
            }
        })

        val imagePath = File.createTempFile("current", ".bmp").absolutePath
        currentFile = File(imagePath)

        formEntryPrompt = MockFormEntryPromptBuilder()
            .withAnswerDisplayText(DrawWidgetTest.USER_SPECIFIED_IMAGE_ANSWER)
            .build()

        val widget = createWidget()

        assertThat(widget.binding.image.visibility, `is`(View.GONE))
        assertThat(widget.binding.image.drawable, nullValue())

        assertThat(widget.binding.errorMessage.visibility, `is`(View.VISIBLE))
    }

    @Test
    fun `the image and error message are hidden when the answer is a reference`() {
        val imagePath = File.createTempFile("default", ".bmp").absolutePath

        val referenceManager = setupFakeReferenceManager(
            listOf(Pair(DrawWidgetTest.REFERENCE_URI, imagePath))
        )
        CollectHelpers.overrideAppDependencyModule(object : AppDependencyModule() {
            override fun providesReferenceManager(): ReferenceManager {
                return referenceManager
            }

            override fun providesImageLoader(): ImageLoader {
                return SynchronousImageLoader()
            }
        })

        formEntryPrompt = MockFormEntryPromptBuilder()
            .withAnswerDisplayText(DrawWidgetTest.REFERENCE_URI)
            .build()

        val widget = createWidget()
        val imageView = widget.binding.image
        assertThat(imageView.visibility, `is`(View.GONE))
        assertThat(imageView.drawable, nullValue())
        assertThat(widget.binding.errorMessage.visibility, `is`(View.GONE))
    }

    @Test
    fun `the answer is displayed in the image view when there is one`() {
        CollectHelpers.overrideAppDependencyModule(object : AppDependencyModule() {
            override fun providesImageLoader(): ImageLoader {
                return SynchronousImageLoader()
            }
        })

        val imagePath = File.createTempFile("current", ".bmp").absolutePath
        currentFile = File(imagePath)

        formEntryPrompt = MockFormEntryPromptBuilder()
            .withAnswerDisplayText(DrawWidgetTest.USER_SPECIFIED_IMAGE_ANSWER)
            .build()

        val widget = createWidget()
        val imageView = widget.binding.image
        assertThat(imageView.visibility, `is`(View.VISIBLE))
        val drawable = imageView.drawable
        assertThat(drawable, notNullValue())

        val loadedPath = shadowOf((drawable as BitmapDrawable).bitmap).createdFromPath
        assertThat(loadedPath, equalTo(imagePath))
    }

    @Test
    fun `clicking the sign button passes the reference to DrawActivity when the answer is a reference`() {
        val file = File.createTempFile("default", ".bmp")
        val imagePath = file.absolutePath

        val referenceManager = setupFakeReferenceManager(
            listOf(Pair(DrawWidgetTest.REFERENCE_URI, imagePath))
        )
        CollectHelpers.overrideAppDependencyModule(object : AppDependencyModule() {
            override fun providesReferenceManager(): ReferenceManager {
                return referenceManager
            }

            override fun providesImageLoader(): ImageLoader {
                return SynchronousImageLoader()
            }
        })

        formEntryPrompt = MockFormEntryPromptBuilder()
            .withAnswerDisplayText(DrawWidgetTest.REFERENCE_URI)
            .build()

        val intent = getIntentLaunchedByClick(R.id.sign_button)
        assertComponentEquals(activity, DrawActivity::class.java, intent)
        assertExtraEquals(DrawActivity.OPTION, DrawActivity.OPTION_SIGNATURE, intent)
        assertExtraEquals(DrawActivity.REF_IMAGE, Uri.fromFile(file), intent)
    }

    @Test
    fun `clicking the sign button does not pass the reference to DrawActivity when it does not exist`() {
        val referenceManager = setupFakeReferenceManager(
            listOf(Pair(DrawWidgetTest.REFERENCE_URI, "/something"))
        )
        CollectHelpers.overrideAppDependencyModule(object : AppDependencyModule() {
            override fun providesReferenceManager(): ReferenceManager {
                return referenceManager
            }
        })

        formEntryPrompt = MockFormEntryPromptBuilder()
            .withAnswerDisplayText(DrawWidgetTest.REFERENCE_URI)
            .build()

        val intent = getIntentLaunchedByClick(R.id.sign_button)
        assertComponentEquals(activity, DrawActivity::class.java, intent)
        assertExtraEquals(DrawActivity.OPTION, DrawActivity.OPTION_SIGNATURE, intent)
        assertThat(intent.hasExtra(DrawActivity.REF_IMAGE), `is`(false))
    }

    @Test
    fun `clicking the sign button does not pass a reference to DrawActivity when there is no answer`() {
        val intent = getIntentLaunchedByClick(R.id.sign_button)
        assertComponentEquals(activity, DrawActivity::class.java, intent)
        assertExtraEquals(DrawActivity.OPTION, DrawActivity.OPTION_SIGNATURE, intent)
        assertThat(intent.hasExtra(DrawActivity.REF_IMAGE), `is`(false))
    }
}
