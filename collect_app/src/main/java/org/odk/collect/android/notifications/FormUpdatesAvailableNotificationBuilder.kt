package org.odk.collect.android.notifications

import android.app.Application
import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import org.odk.collect.android.R
import org.odk.collect.android.activities.FormDownloadListActivity
import org.odk.collect.android.utilities.ApplicationConstants.RequestCodes
import org.odk.collect.android.utilities.IconUtils
import org.odk.collect.strings.localization.getLocalizedString

class FormUpdatesAvailableNotificationBuilder(private val application: Application) {

    fun build(): Notification {
        val intent = Intent(application, FormDownloadListActivity::class.java).apply {
            putExtra(FormDownloadListActivity.DISPLAY_ONLY_UPDATED_FORMS, true)
        }

        val contentIntent = PendingIntent.getActivity(
            application,
            RequestCodes.FORM_UPDATES_AVAILABLE_NOTIFICATION,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(
            application,
            NotificationManagerNotifier.COLLECT_NOTIFICATION_CHANNEL
        ).apply {
            setContentIntent(contentIntent)
            setContentTitle(application.getLocalizedString(R.string.form_updates_available))
            setContentText(null)
            setSmallIcon(IconUtils.getNotificationAppIcon())
            setAutoCancel(true)
        }.build()
    }
}
