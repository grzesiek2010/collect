package org.odk.collect.android.widgets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.androidshared.ui.compose.marginStandard
import org.odk.collect.icons.R
import org.odk.collect.strings.R.string

@Composable
fun AnnotateWidgetContent(
    mediaWidgetAnswerViewModel: MediaWidgetAnswerViewModel,
    formEntryPrompt: FormEntryPrompt,
    answer: String?,
    readOnly: Boolean,
    newImagesOnly: Boolean,
    isDefaultImage: Boolean,
    canAnnotate: Boolean,
    buttonFontSize: Int,
    onCaptureClick: () -> Unit,
    onChooseClick: () -> Unit,
    onAnnotateClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Column {
        if (!readOnly) {
            WidgetIconButton(
                Icons.Default.PhotoCamera,
                stringResource(string.capture_image),
                buttonFontSize,
                onCaptureClick,
                onLongClick,
                enabled = !isDefaultImage
            )
        }

        if (!readOnly && !newImagesOnly) {
            WidgetIconButton(
                Icons.Default.PhotoLibrary,
                stringResource(string.choose_image),
                buttonFontSize,
                onChooseClick,
                onLongClick,
                Modifier.padding(top = marginStandard()),
                enabled = !isDefaultImage
            )
        }

        if (!readOnly) {
            WidgetIconButton(
                ImageVector.vectorResource(R.drawable.ic_baseline_markup_white_24),
                stringResource(string.markup_image),
                buttonFontSize,
                onAnnotateClick,
                onLongClick,
                Modifier.padding(top = marginStandard()),
                enabled = canAnnotate
            )
        }

        WidgetAnswer(
            Modifier.padding(top = marginStandard()),
            formEntryPrompt,
            answer,
            mediaWidgetAnswerViewModel = mediaWidgetAnswerViewModel,
            onLongClick = onLongClick
        )
    }
}
