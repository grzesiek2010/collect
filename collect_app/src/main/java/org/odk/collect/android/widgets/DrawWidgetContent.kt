package org.odk.collect.android.widgets

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
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
fun DrawWidgetContent(
    mediaWidgetAnswerViewModel: MediaWidgetAnswerViewModel,
    formEntryPrompt: FormEntryPrompt,
    answer: String?,
    readOnly: Boolean,
    buttonDetails: DrawButtonDetails,
    buttonFontSize: Int,
    onDrawClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Column {
        if (!readOnly) {
            WidgetIconButton(
                ImageVector.vectorResource(buttonDetails.icon),
                stringResource(buttonDetails.text),
                buttonFontSize,
                onDrawClick,
                onLongClick
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

enum class DrawButtonDetails(
    @DrawableRes val icon: Int,
    @StringRes val text: Int
) {
    DRAW(R.drawable.ic_baseline_draw_white_24, string.draw_image),
    SIGNATURE(R.drawable.ic_baseline_signature_white_24, string.sign_button)
}
