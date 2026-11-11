package org.odk.collect.android.widgets

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.View
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import org.javarosa.core.model.data.IAnswerData
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.formentry.questions.QuestionDetails
import org.odk.collect.android.utilities.ApplicationConstants.RequestCodes
import org.odk.collect.android.utilities.QuestionMediaManager
import org.odk.collect.android.widgets.image.FileAnswerDelegate
import org.odk.collect.android.widgets.interfaces.WidgetDataReceiver
import org.odk.collect.android.widgets.utilities.DrawActivityLauncher
import org.odk.collect.android.widgets.utilities.QuestionFontSizeUtils
import org.odk.collect.android.widgets.utilities.WaitingForDataRegistry
import org.odk.collect.androidshared.ui.ComposeThemeProvider.Companion.setContextThemedContent
import org.odk.collect.draw.DrawActivity
import org.odk.collect.strings.R

@SuppressLint("ViewConstructor")
class SignatureWidget(
    context: Context,
    questionDetails: QuestionDetails,
    questionMediaManager: QuestionMediaManager,
    waitingForDataRegistry: WaitingForDataRegistry,
    tmpImageFilePath: String,
    private val dependencies: Dependencies
) : QuestionWidget(context, dependencies, questionDetails), WidgetDataReceiver {
    private val fileAnswerDelegate = FileAnswerDelegate(this, questionMediaManager, questionDetails.prompt)
    private val drawActivityLauncher = DrawActivityLauncher(questionMediaManager, referenceManager, waitingForDataRegistry, tmpImageFilePath)

    init { render() }

    override fun onCreateWidgetView(context: Context, prompt: FormEntryPrompt, answerFontSize: Int): View {
        val readOnly = questionDetails.isReadOnly
        val buttonFontSize = QuestionFontSizeUtils.getFontSize(settings, QuestionFontSizeUtils.FontSize.BODY_LARGE)

        return ComposeView(context).apply {
            setContextThemedContent(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool) {
                DrawWidgetContent(
                    dependencies.mediaWidgetAnswerViewModel,
                    formEntryPrompt,
                    fileAnswerDelegate.binaryName,
                    readOnly,
                    DrawButtonDetails.SIGNATURE,
                    buttonFontSize,
                    onDrawClick = { launchDrawActivity() },
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
        fileAnswerDelegate.setData(answer)
    }

    override fun setOnLongClickListener(listener: OnLongClickListener?) {}

    private fun launchDrawActivity() {
        drawActivityLauncher.launch(
            context as Activity,
            formEntryPrompt.index,
            DrawActivity.OPTION_SIGNATURE,
            fileAnswerDelegate.binaryName,
            RequestCodes.SIGNATURE_CAPTURE,
            R.string.signature_capture
        )
    }
}
