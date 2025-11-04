package org.odk.collect.android.widgets
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import org.odk.collect.androidshared.R.dimen

object BarcodeWidgetAnswer {
    @Composable
    fun Container(
        modifier: Modifier,
        answer: String,
        fontSize: Int
    ) {
        Content(modifier, answer, fontSize)
    }

    @Composable
    fun Content(
        modifier: Modifier,
        answer: String,
        fontSize: Int
    ) {
        Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.QrCodeScanner,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(dimensionResource(id = dimen.margin_small)))
            Text(
                text = answer,
                style = TextStyle(fontSize = fontSize.sp)
            )
        }
    }
}
