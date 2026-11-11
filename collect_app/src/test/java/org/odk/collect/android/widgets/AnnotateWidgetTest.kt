package org.odk.collect.android.widgets

import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.provider.MediaStore
import android.view.View
import androidx.core.util.Pair
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.notNullValue
import org.hamcrest.Matchers.nullValue
import org.javarosa.core.model.data.StringData
import org.javarosa.core.reference.ReferenceManager
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.kotlin.whenever
import org.odk.collect.android.R
import org.odk.collect.android.formentry.questions.QuestionDetails
import org.odk.collect.android.injection.config.AppDependencyModule
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.android.support.CollectHelpers.overrideReferenceManager
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
import org.robolectric.shadows.ShadowBitmapFactory
import org.robolectric.shadows.ShadowToast
import java.io.File

class AnnotateWidgetTest : BinaryWidgetTest<AnnotateWidget, StringData>() {
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

    private val file = TempFiles.createTempFile("sample", ".jpg")

    override fun createWidget(): AnnotateWidget {
        return AnnotateWidget(
            activity,
            QuestionDetails(formEntryPrompt, readOnlyOverride),
            questionMediaManager,
            FakeWaitingForDataRegistry(),
            TempFiles.getPathInTempDir(),
            dependencies
        )
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
        stubAllRuntimePermissionsGranted(true)

        var intent = getIntentLaunchedByClick(R.id.capture_button)
        assertActionEquals(MediaStore.ACTION_IMAGE_CAPTURE, intent)
        assertThat(intent.getPackage(), equalTo(null))

        intent = getIntentLaunchedByClick(R.id.choose_button)
        assertActionEquals(Intent.ACTION_GET_CONTENT, intent)

        intent = getIntentLaunchedByClick(R.id.annotate_button)
        assertComponentEquals(activity, DrawActivity::class.java, intent)
        assertExtraEquals(DrawActivity.OPTION, DrawActivity.OPTION_ANNOTATE, intent)
    }

    @Test
    fun `the buttons launch the correct intents when a custom package is set`() {
        formEntryPrompt = MockFormEntryPromptBuilder()
            .withAdditionalAttribute("intent", "com.customcameraapp")
            .build()

        stubAllRuntimePermissionsGranted(true)

        var intent = getIntentLaunchedByClick(R.id.capture_button)
        assertActionEquals(MediaStore.ACTION_IMAGE_CAPTURE, intent)
        assertThat(intent.getPackage(), equalTo("com.customcameraapp"))

        intent = getIntentLaunchedByClick(R.id.choose_button)
        assertActionEquals(Intent.ACTION_GET_CONTENT, intent)
        assertTypeEquals("image/*", intent)
    }

    @Test
    fun `the capture button does not launch an intent when permissions are denied`() {
        stubAllRuntimePermissionsGranted(false)

        assertNull(getIntentLaunchedByClick(R.id.capture_button))
    }

    @Test
    override fun usingReadOnlyOptionShouldMakeAllClickableElementsDisabled() {
        whenever(formEntryPrompt.isReadOnly).thenReturn(true)

        assertThat(spyWidget.binding.captureButton.visibility, `is`(View.GONE))
        assertThat(spyWidget.binding.chooseButton.visibility, `is`(View.GONE))
        assertThat(spyWidget.binding.annotateButton.visibility, `is`(View.GONE))
    }

    @Test
    fun `all buttons are hidden when read-only override is used`() {
        readOnlyOverride = true
        whenever(formEntryPrompt.isReadOnly).thenReturn(false)

        assertThat(spyWidget.binding.captureButton.visibility, `is`(View.GONE))
        assertThat(spyWidget.binding.chooseButton.visibility, `is`(View.GONE))
        assertThat(spyWidget.binding.annotateButton.visibility, `is`(View.GONE))
    }

    @Test
    fun `the image and error message are hidden when there is no answer`() {
        val widget = createWidget()

        assertThat(widget.binding.image.visibility, `is`(View.GONE))
        assertThat(widget.binding.image.drawable, nullValue())

        assertThat(widget.binding.errorMessage.visibility, `is`(View.GONE))
    }

    @Test
    fun `a gif file is not attached and a message is displayed when it is selected`() {
        val widget = createWidget()

        val file = TempFiles.createTempFile("sample", ".gif")
        questionMediaManager.addAnswerFile(file)
        widget.setData(file)

        assertThat(widget.binding.image.visibility, `is`(View.GONE))
        assertThat(widget.binding.image.drawable, nullValue())

        assertThat(ShadowToast.getTextOfLatestToast(), `is`("Gif files are not supported"))
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
    fun `the image and error message are hidden but the annotate button is enabled when the answer is a reference`() {
        val imagePath = File.createTempFile("default", ".bmp").absolutePath

        val referenceManager = setupFakeReferenceManager(
            listOf(Pair(DrawWidgetTest.REFERENCE_URI, imagePath))
        )
        CollectHelpers.overrideAppDependencyModule(object : AppDependencyModule() {
            override fun providesReferenceManager(): ReferenceManager {
                return referenceManager
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
        assertThat(widget.binding.annotateButton.isEnabled, `is`(true))
    }

    @Test
    fun `the capture and choose buttons are disabled but the annotate button is enabled when the answer is a reference`() {
        val imagePath = File.createTempFile("default", ".bmp").absolutePath

        val referenceManager = setupFakeReferenceManager(
            listOf(Pair(DrawWidgetTest.REFERENCE_URI, imagePath))
        )
        CollectHelpers.overrideAppDependencyModule(object : AppDependencyModule() {
            override fun providesReferenceManager(): ReferenceManager {
                return referenceManager
            }
        })

        formEntryPrompt = MockFormEntryPromptBuilder()
            .withAnswerDisplayText(DrawWidgetTest.REFERENCE_URI)
            .build()

        val widget = createWidget()
        assertThat(widget.binding.captureButton.isEnabled, `is`(false))
        assertThat(widget.binding.chooseButton.isEnabled, `is`(false))
        assertThat(widget.binding.annotateButton.isEnabled, `is`(true))
    }

    @Test
    fun `all buttons are enabled when there is an answer`() {
        val imagePath = File.createTempFile("current", ".bmp").absolutePath
        currentFile = File(imagePath)

        formEntryPrompt = MockFormEntryPromptBuilder()
            .withAnswerDisplayText(DrawWidgetTest.USER_SPECIFIED_IMAGE_ANSWER)
            .build()

        val widget = createWidget()
        assertThat(widget.binding.captureButton.isEnabled, `is`(true))
        assertThat(widget.binding.chooseButton.isEnabled, `is`(true))
        assertThat(widget.binding.annotateButton.isEnabled, `is`(true))
    }

    @Test
    fun `the capture and choose buttons are enabled but the annotate button is disabled when there is no answer`() {
        val widget = createWidget()

        assertThat(widget.binding.captureButton.isEnabled, `is`(true))
        assertThat(widget.binding.chooseButton.isEnabled, `is`(true))
        assertThat(widget.binding.annotateButton.isEnabled, `is`(false))
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
    fun `the annotate button is disabled when the image does not exist`() {
        overrideReferenceManager(
            setupFakeReferenceManager(listOf(Pair(DrawWidgetTest.REFERENCE_URI, "bad_path")))
        )

        formEntryPrompt = MockFormEntryPromptBuilder()
            .withAnswerDisplayText(DrawWidgetTest.REFERENCE_URI)
            .build()

        assertThat(widget.binding.annotateButton.isEnabled, `is`(false))

        formEntryPrompt = MockFormEntryPromptBuilder()
            .withAnswerDisplayText(DrawWidgetTest.USER_SPECIFIED_IMAGE_ANSWER)
            .build()

        assertThat(widget.binding.annotateButton.isEnabled, `is`(false))
    }

    @Test
    fun `clicking the annotate button passes the reference to DrawActivity when the answer is a reference`() {
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

        val intent = getIntentLaunchedByClick(R.id.annotate_button)
        assertComponentEquals(activity, DrawActivity::class.java, intent)
        assertExtraEquals(DrawActivity.OPTION, DrawActivity.OPTION_ANNOTATE, intent)
        assertExtraEquals(DrawActivity.REF_IMAGE, Uri.fromFile(file), intent)
    }

    @Test
    fun `clicking the annotate button does not pass the reference to DrawActivity when it does not exist`() {
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

        val intent = getIntentLaunchedByClick(R.id.annotate_button)
        assertComponentEquals(activity, DrawActivity::class.java, intent)
        assertExtraEquals(DrawActivity.OPTION, DrawActivity.OPTION_ANNOTATE, intent)
        assertThat(intent.hasExtra(DrawActivity.REF_IMAGE), `is`(false))
    }

    @Test
    fun `clicking the annotate button does not pass a reference to DrawActivity when there is no answer`() {
        val intent = getIntentLaunchedByClick(R.id.annotate_button)
        assertComponentEquals(activity, DrawActivity::class.java, intent)
        assertExtraEquals(DrawActivity.OPTION, DrawActivity.OPTION_ANNOTATE, intent)
        assertThat(intent.hasExtra(DrawActivity.REF_IMAGE), `is`(false))
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
        val file = File.createTempFile("default", ".bmp")
        val imagePath = file.absolutePath
        ShadowBitmapFactory.provideWidthAndHeightHints(imagePath, width, height)

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

        val intent = getIntentLaunchedByClick(R.id.annotate_button)
        assertExtraEquals(DrawActivity.SCREEN_ORIENTATION, expectedOrientation, intent)
    }
}
