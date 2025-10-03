package org.odk.collect.android.formlists.blankformlist

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.odk.collect.strings.R.string
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun FormsSyncStatusBanner(lastSuccessfulSyncDate: Long?) {
    val message = lastSuccessfulSyncDate?.let {
        SimpleDateFormat(
            LocalContext.current.getString(string.failed_to_sync_forms_message),
            Locale.getDefault()
        ).format(it)
    }

    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
        Card {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(string.failed_to_sync_forms_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                message?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { /* TODO: handle click */ },
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Favorite"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(string.failed_to_sync_forms_refresh))
                }
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    MaterialTheme {
        FormsSyncStatusBanner(1759405483003)
    }
}
