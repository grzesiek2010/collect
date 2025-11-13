package org.odk.collect.android.widgets

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
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
import org.odk.collect.android.widgets.video.VideoWidgetAnswer
import org.odk.collect.async.Scheduler
import org.odk.collect.icons.R

@Composable
fun WidgetAnswer(
    modifier: Modifier = Modifier,
    prompt: FormEntryPrompt,
    fontSize: Int = 0,
    viewModelProvider: ViewModelProvider,
    onLongClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel = viewModelProvider[WidgetAnswerViewModel::class]
    val answerFlow = remember(prompt) { viewModel.getQuestionAnswer(prompt, context) }
    val answer by answerFlow.collectAsStateWithLifecycle()

    answer?.let {
        if (it.isBlank()) {
            return
        }

        when (prompt.controlType) {
            Constants.CONTROL_INPUT -> {
                when (prompt.dataType) {
                    Constants.DATATYPE_BARCODE -> TextWidgetAnswer(
                        modifier,
                        ImageVector.vectorResource(R.drawable.ic_baseline_barcode_scanner_white_24),
                        it,
                        fontSize,
                        onLongClick
                    )
                }
            }
            Constants.CONTROL_VIDEO_CAPTURE -> VideoWidgetAnswer(modifier, it, viewModelProvider, onLongClick)
            Constants.CONTROL_FILE_CAPTURE -> {
                val context = LocalContext.current
                val viewModel = viewModelProvider[ArbitraryFileWidgetAnswerViewModel::class]

                TextWidgetAnswer(
                    modifier,
                    Icons.Default.AttachFile,
                    it,
                    fontSize,
                    onLongClick,
                    stringResource(org.odk.collect.strings.R.string.open_file)
                ) { viewModel.openFile(context, answer) }
            }
            else -> throw IllegalArgumentException("Unsupported control type: ${prompt.controlType}")
        }
    }
}

class WidgetAnswerViewModel(
    private val scheduler: Scheduler,
    private val formController: FormController
) : ViewModel() {
    fun getQuestionAnswer(prompt: FormEntryPrompt, context: Context): StateFlow<String?> {
        val answer = MutableStateFlow<String?>(null)

        scheduler.immediate {
            answer.value = QuestionAnswerProcessor.getQuestionAnswer(prompt, context, formController)
        }

        return answer
    }
}
