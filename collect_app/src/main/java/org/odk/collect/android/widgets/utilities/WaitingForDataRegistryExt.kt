package org.odk.collect.android.widgets.utilities

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.widget.Toast
import androidx.annotation.StringRes
import org.javarosa.core.model.FormIndex
import org.odk.collect.strings.R.string

fun WaitingForDataRegistry.launchForResult(
    activity: Activity,
    formIndex: FormIndex,
    intent: Intent,
    requestCode: Int,
    @StringRes errorStringResource: Int
) {
    try {
        waitForData(formIndex)
        activity.startActivityForResult(intent, requestCode)
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(
            activity,
            activity.getString(string.activity_not_found, activity.getString(errorStringResource)),
            Toast.LENGTH_SHORT
        ).show()
        cancelWaitingForData()
    }
}
