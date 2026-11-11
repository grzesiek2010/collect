package org.odk.collect.android.widgets

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.View
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import org.javarosa.core.model.data.IAnswerData
import org.javarosa.core.model.data.StringData
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.formentry.questions.QuestionDetails
import org.odk.collect.android.utilities.ApplicationConstants
import org.odk.collect.android.utilities.FileUtils
import org.odk.collect.android.utilities.QuestionMediaManager
import org.odk.collect.android.widgets.interfaces.FileWidget
import org.odk.collect.android.widgets.interfaces.WidgetDataReceiver
import org.odk.collect.android.widgets.utilities.FileRequester
import org.odk.collect.android.widgets.utilities.QuestionFontSizeUtils
import org.odk.collect.android.widgets.utilities.WaitingForDataRegistry
import org.odk.collect.androidshared.ui.ComposeThemeProvider.Companion.setContextThemedContent
import org.odk.collect.androidshared.ui.ToastUtils
import org.odk.collect.strings.R
import timber.log.Timber
import java.io.File

@SuppressLint("ViewConstructor")
class ExImageWidget(
    context: Context,
    questionDetails: QuestionDetails,
    private val questionMediaManager: QuestionMediaManager,
    private val waitingForDataRegistry: WaitingForDataRegistry,
    private val fileRequester: FileRequester,
    private val dependencies: Dependencies
) : QuestionWidget(context, dependencies, questionDetails), FileWidget, WidgetDataReceiver {
    private var binaryName by mutableStateOf<String?>(formEntryPrompt.answerText?.takeIf { it.isNotEmpty() })

    init {
        render()
    }

    override fun onCreateWidgetView(context: Context, prompt: FormEntryPrompt, answerFontSize: Int): View {
        val readOnly = questionDetails.isReadOnly
        val buttonFontSize = QuestionFontSizeUtils.getFontSize(settings, QuestionFontSizeUtils.FontSize.BODY_LARGE)

        return ComposeView(context).apply {
            setContextThemedContent(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool) {
                ExImageWidgetContent(
                    dependencies.mediaWidgetAnswerViewModel,
                    formEntryPrompt,
                    binaryName,
                    readOnly,
                    buttonFontSize,
                    onLaunchClick = { launchExternalApp() },
                    onLongClick = { showContextMenu() }
                )
            }
        }
    }

    override fun deleteFile() {
        questionMediaManager.deleteAnswerFile(
            formEntryPrompt.index.toString(),
            questionMediaManager.getAnswerFile(binaryName)!!.absolutePath
        )
        binaryName = null
    }

    override fun clearAnswer() {
        deleteFile()
        widgetValueChanged()
    }

    override fun getAnswer(): IAnswerData? {
        return binaryName?.let { StringData(it) }
    }

    override fun setData(answer: Any) {
        if (binaryName != null) {
            clearAnswer()
        }

        if (answer is File && mediaUtils.isImageFile(answer)) {
            if (answer.exists()) {
                questionMediaManager.replaceAnswerFile(formEntryPrompt.index.toString(), answer.absolutePath)
                binaryName = answer.name
                widgetValueChanged()
            } else {
                Timber.e(Error("Inserting Image file FAILED"))
            }
        } else if (answer is File) {
            ToastUtils.showLongToast(R.string.invalid_file_type)
            mediaUtils.deleteMediaFile(answer.absolutePath)
            Timber.e(Error("ExImageWidget's setBinaryData must receive an image file but received: " + FileUtils.getMimeType(answer)))
        } else {
            Timber.e(Error("ExImageWidget's setBinaryData must receive an image file but received: " + answer.javaClass))
        }
    }

    override fun setOnLongClickListener(listener: OnLongClickListener?) {}

    private fun launchExternalApp() {
        waitingForDataRegistry.waitForData(formEntryPrompt.index)
        fileRequester.launch(context as Activity, ApplicationConstants.RequestCodes.EX_IMAGE_CHOOSER, formEntryPrompt)
    }
}
