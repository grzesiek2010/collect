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
import org.odk.collect.android.upload.FormUploadException
import org.odk.collect.android.utilities.ApplicationConstants.RequestCodes
import org.odk.collect.android.utilities.FormsUploadResultInterpreter
import org.odk.collect.forms.instances.Instance
import org.odk.collect.strings.localization.getLocalizedString

object FormsSubmissionNotificationBuilder {

    fun build(application: Application, result: Map<Instance, FormUploadException?>, projectName: String): Notification {
        val allFormsUploadedSuccessfully = FormsUploadResultInterpreter.allFormsUploadedSuccessfully(result)

        val notifyIntent = if (allFormsUploadedSuccessfully) {
            Intent(application, MainMenuActivity::class.java)
        } else {
            Intent(application, InstanceUploaderListActivity::class.java)
        }.apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val pendingNotify = PendingIntent.getActivity(
            application,
            RequestCodes.FORMS_UPLOADED_NOTIFICATION,
            notifyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val title =
            if (allFormsUploadedSuccessfully) application.getLocalizedString(R.string.forms_upload_succeeded)
            else application.getLocalizedString(R.string.forms_upload_failed)

        val content =
            if (allFormsUploadedSuccessfully) application.getLocalizedString(R.string.all_uploads_succeeded)
            else application.getLocalizedString(
                R.string.some_uploads_failed,
                FormsUploadResultInterpreter.getNumberOfFailures(result),
                result.size
            )

        return NotificationCompat.Builder(
            application,
            NotificationManagerNotifier.COLLECT_NOTIFICATION_CHANNEL
        ).apply {
            setContentIntent(pendingNotify)
            setContentTitle(title)
            setContentText(content)
            setSubText(projectName)
            setSmallIcon(R.drawable.ic_notification_small)
            setAutoCancel(true)
        }.build()
    }
}
