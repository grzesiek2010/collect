package org.odk.collect.android.notifications.builders

import android.app.Application
import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import org.odk.collect.android.R
import org.odk.collect.android.activities.InstanceUploaderListActivity
import org.odk.collect.android.activities.MainMenuActivity
import org.odk.collect.android.notifications.NotificationManagerNotifier
import org.odk.collect.android.utilities.ApplicationConstants.RequestCodes
import org.odk.collect.android.utilities.IconUtils
import org.odk.collect.strings.localization.getLocalizedString

class FormsSubmissionNotificationBuilder(private val application: Application) {

    fun build(submissionFailed: Boolean, message: String, projectName: String): Notification {
        val notifyIntent =
            if (submissionFailed) Intent(application, InstanceUploaderListActivity::class.java)
            else Intent(application, MainMenuActivity::class.java)

        val pendingNotify = PendingIntent.getActivity(
            application,
            RequestCodes.FORMS_UPLOADED_NOTIFICATION,
            notifyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val title =
            if (submissionFailed) application.getLocalizedString(R.string.forms_upload_failed)
            else application.getLocalizedString(R.string.forms_upload_succeeded)

        val content =
            if (submissionFailed) message
            else application.getLocalizedString(R.string.all_uploads_succeeded)

        return NotificationCompat.Builder(
            application,
            NotificationManagerNotifier.COLLECT_NOTIFICATION_CHANNEL
        ).apply {
            setContentIntent(pendingNotify)
            setContentTitle(title)
            setContentText(content)
            setSubText(projectName)
            setSmallIcon(IconUtils.getNotificationAppIcon())
            setAutoCancel(true)
        }.build()
    }
}
