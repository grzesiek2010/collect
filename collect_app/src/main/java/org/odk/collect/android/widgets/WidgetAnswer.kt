package org.odk.collect.android.widgets

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.javarosa.core.model.Constants
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.formhierarchy.QuestionAnswerProcessor
import org.odk.collect.android.javarosawrapper.FormController
import org.odk.collect.android.widgets.arbitraryfile.ArbitraryFileWidgetAnswerViewModel
import org.odk.collect.android.widgets.utilities.QuestionFontSizeUtils
import org.odk.collect.android.widgets.video.VideoWidgetAnswer
import org.odk.collect.async.Scheduler
import org.odk.collect.icons.R
import org.odk.collect.shared.settings.Settings

@Composable
fun WidgetAnswer(
    prompt: FormEntryPrompt,
    viewModelProvider: ViewModelProvider,
    summaryMode: Boolean = false,
    onLongClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val widgetAnswerViewModel = viewModelProvider[WidgetAnswerViewModel::class]
    val answerFlow = remember(prompt) { widgetAnswerViewModel.getAnswerText(prompt, context) }
    val answer by answerFlow.collectAsStateWithLifecycle()
    val answerFontSize = widgetAnswerViewModel.getAnswerFontSize()

    answer?.let {
        if (it.isBlank()) {
            return
        }

        when (prompt.controlType) {
            Constants.CONTROL_INPUT -> {
                when (prompt.dataType) {
                    Constants.DATATYPE_BARCODE -> TextWidgetAnswer(
                        ImageVector.vectorResource(R.drawable.ic_baseline_barcode_scanner_white_24),
                        it,
                        answerFontSize,
                        summaryMode,
                        onLongClick
                    )
                    else -> TextWidgetAnswer(null, it, answerFontSize, summaryMode, onLongClick)
                }
            }
            Constants.CONTROL_VIDEO_CAPTURE -> VideoWidgetAnswer(it, viewModelProvider, onLongClick)
            Constants.CONTROL_FILE_CAPTURE -> {
                val context = LocalContext.current
                val viewModel = viewModelProvider[ArbitraryFileWidgetAnswerViewModel::class]

                TextWidgetAnswer(
                    Icons.Default.AttachFile,
                    it,
                    answerFontSize,
                    summaryMode,
                    onLongClick,
                    stringResource(org.odk.collect.strings.R.string.open_file)
                ) { viewModel.openFile(context, answer) }
            }
            else -> TextWidgetAnswer(null, it, answerFontSize, summaryMode, onLongClick)
        }
    }
}

class WidgetAnswerViewModel(
    private val scheduler: Scheduler,
    private val formController: FormController,
    private val settings: Settings
) : ViewModel() {
    fun getAnswerText(prompt: FormEntryPrompt, context: Context): StateFlow<String?> {
        val answer = MutableStateFlow<String?>(null)

        scheduler.immediate {
            answer.value = QuestionAnswerProcessor.getQuestionAnswer(prompt, context, formController)
        }

        return answer
    }

    fun getAnswerFontSize(): Int {
        return QuestionFontSizeUtils.getFontSize(settings, QuestionFontSizeUtils.FontSize.HEADLINE_6)
    }
}
