package org.odk.collect.android.widgets

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import org.odk.collect.strings.R.string

@Composable
fun BarcodeWidgetContent(
    answer: String?,
    readOnly: Boolean,
    isAnswerHidden: Boolean,
    fontSize: Int,
    onGetBarcodeClick: () -> Unit,
    onLongClick: () -> Unit,
    widgetAnswer: @Composable () -> Unit
) {
    Column {
        if (!readOnly) {
            WidgetIconButton(
                Icons.Default.QrCodeScanner,
                if (answer == null) {
                    stringResource(string.get_barcode)
                } else {
                    stringResource(string.replace_barcode)
                },
                fontSize,
                onGetBarcodeClick,
                onLongClick
            )
        }

        if (!isAnswerHidden) {
            widgetAnswer()
        }
    }
}

@Preview
@Composable
private fun BarcodeWidgetContentNoAnswerPreview() {
    MaterialTheme {
        BarcodeWidgetContent(
            null,
            false,
            false,
            10,
            {},
            {},
            {}
        )
    }
}

@Preview
@Composable
private fun BarcodeWidgetContentWIthAnswerPreview() {
    MaterialTheme {
        BarcodeWidgetContent(
            "123",
            false,
            false,
            10,
            {},
            {},
            {}
        )
    }
}

@Preview
@Composable
private fun BarcodeWidgetContentReadOnlyPreview() {
    MaterialTheme {
        BarcodeWidgetContent(
            null,
            true,
            false,
            10,
            {},
            {},
            {}
        )
    }
}
