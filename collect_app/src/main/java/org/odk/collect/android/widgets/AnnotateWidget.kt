package org.odk.collect.android.widgets

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
import android.view.View
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import org.javarosa.core.model.data.IAnswerData
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.formentry.questions.QuestionDetails
import org.odk.collect.android.utilities.Appearances
import org.odk.collect.android.utilities.ApplicationConstants.RequestCodes
import org.odk.collect.android.utilities.FileUtils
import org.odk.collect.android.utilities.QuestionMediaManager
import org.odk.collect.android.utilities.getExistingAnswerFile
import org.odk.collect.android.widgets.image.FileAnswerDelegate
import org.odk.collect.android.widgets.interfaces.WidgetDataReceiver
import org.odk.collect.android.widgets.utilities.DrawActivityLauncher
import org.odk.collect.android.widgets.utilities.ImageCaptureIntentCreator
import org.odk.collect.android.widgets.utilities.QuestionFontSizeUtils
import org.odk.collect.android.widgets.utilities.WaitingForDataRegistry
import org.odk.collect.android.widgets.utilities.launchForResult
import org.odk.collect.androidshared.bitmap.ImageFileUtils
import org.odk.collect.androidshared.ui.ComposeThemeProvider.Companion.setContextThemedContent
import org.odk.collect.androidshared.ui.ToastUtils
import org.odk.collect.draw.DrawActivity
import org.odk.collect.permissions.PermissionListener
import org.odk.collect.strings.R
import java.io.File
import java.util.Locale

@SuppressLint("ViewConstructor")
class AnnotateWidget(
    context: Context,
    questionDetails: QuestionDetails,
    private val questionMediaManager: QuestionMediaManager,
    private val waitingForDataRegistry: WaitingForDataRegistry,
    private val tmpImageFilePath: String,
    private val dependencies: Dependencies
) : QuestionWidget(context, dependencies, questionDetails), WidgetDataReceiver {
    private val fileAnswerDelegate = FileAnswerDelegate(this, questionMediaManager, questionDetails.prompt)
    private val drawActivityLauncher = DrawActivityLauncher(questionMediaManager, referenceManager, waitingForDataRegistry, tmpImageFilePath)

    init { render() }

    override fun onCreateWidgetView(context: Context, prompt: FormEntryPrompt, answerFontSize: Int): View {
        val readOnly = questionDetails.isReadOnly
        val newImagesOnly = prompt.appearanceHint?.lowercase(Locale.ENGLISH)?.contains(Appearances.NEW) == true
        val buttonFontSize = QuestionFontSizeUtils.getFontSize(settings, QuestionFontSizeUtils.FontSize.BODY_LARGE)

        return ComposeView(context).apply {
            setContextThemedContent(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool) {
                val answer = fileAnswerDelegate.binaryName
                val isDefaultImage = remember(answer) { isDefaultImage(answer) }
                val isAnswerImage = remember(answer) { questionMediaManager.getExistingAnswerFile(answer) != null }

                AnnotateWidgetContent(
                    dependencies.mediaWidgetAnswerViewModel,
                    formEntryPrompt,
                    answer,
                    readOnly,
                    newImagesOnly,
                    isDefaultImage,
                    isDefaultImage || isAnswerImage,
                    buttonFontSize,
                    onCaptureClick = { captureImage() },
                    onChooseClick = { chooseImage() },
                    onAnnotateClick = { launchDrawActivity() },
                    onLongClick = { showContextMenu() }
                )
            }
        }
    }

    override fun getAnswer(): IAnswerData? {
        return fileAnswerDelegate.getAnswer()
    }

    override fun clearAnswer() {
        fileAnswerDelegate.deleteFile()
    }

    override fun setData(answer: Any) {
        if (answer is File) {
            val mimeType = FileUtils.getMimeType(answer)
            if (mimeType == "image/gif") {
                ToastUtils.showLongToast(R.string.gif_not_supported)
            } else {
                fileAnswerDelegate.setData(answer)
            }
        }
    }

    override fun setOnLongClickListener(listener: OnLongClickListener?) {}

    private fun captureImage() {
        permissionsProvider.requestCameraPermission(context as Activity, object : PermissionListener {
            override fun granted() {
                val intent = ImageCaptureIntentCreator.imageCaptureIntent(formEntryPrompt, context, tmpImageFilePath)
                launchActivityForResult(intent, RequestCodes.IMAGE_CAPTURE, R.string.annotate_image)
            }
        })
    }

    private fun chooseImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }
        launchActivityForResult(intent, RequestCodes.IMAGE_CHOOSER, R.string.annotate_image)
    }

    private fun launchDrawActivity() {
        val screenOrientation = if (isBaseImagePortrait()) {
            SCREEN_ORIENTATION_PORTRAIT
        } else {
            SCREEN_ORIENTATION_LANDSCAPE
        }

        drawActivityLauncher.launch(
            context as Activity,
            formEntryPrompt.index,
            DrawActivity.OPTION_ANNOTATE,
            fileAnswerDelegate.binaryName,
            RequestCodes.ANNOTATE_IMAGE,
            R.string.annotate_image,
            screenOrientation
        )
    }

    private fun isBaseImagePortrait(): Boolean {
        val baseImage = drawActivityLauncher.getAnswerOrDefaultImageFile(fileAnswerDelegate.binaryName)
        if (baseImage == null || !baseImage.exists()) {
            return false
        }

        val dimensions = ImageFileUtils.getImageDisplayDimensions(baseImage)
        return dimensions != null && dimensions.height > dimensions.width
    }

    private fun isDefaultImage(binaryName: String?): Boolean {
        if (binaryName?.startsWith("jr://images/") != true) {
            return false
        }

        val file = drawActivityLauncher.getAnswerOrDefaultImageFile(binaryName)
        return file != null && file.exists()
    }

    private fun launchActivityForResult(intent: Intent, requestCode: Int, errorStringResource: Int) {
        waitingForDataRegistry.launchForResult(context as Activity, formEntryPrompt.index, intent, requestCode, errorStringResource)
    }
}
