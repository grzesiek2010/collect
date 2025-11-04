package org.odk.collect.android.widgets

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import org.javarosa.core.model.Constants
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.widgets.video.VideoWidgetAnswer

@Composable
fun WidgetAnswer(
    modifier: Modifier = Modifier,
    prompt: FormEntryPrompt,
    answer: String?,
    fontSize: Int = 0,
    viewModelProvider: ViewModelProvider = EmptyViewModelProvider()
) {
    if (answer != null) {
        when (prompt.controlType) {
            Constants.CONTROL_INPUT -> {
                when (prompt.dataType) {
                    Constants.DATATYPE_BARCODE -> BarcodeWidgetAnswer.Container(modifier, answer, fontSize)
                }
            }
            Constants.CONTROL_VIDEO_CAPTURE -> VideoWidgetAnswer.Container(modifier, answer, viewModelProvider)
            else -> throw IllegalArgumentException("Unsupported control type: ${prompt.controlType}")
        }
    }
}

class EmptyViewModelProvider : ViewModelProvider(
    ViewModelStore(),
    NewInstanceFactory()
)
