package org.odk.collect.android.widgets.utilities

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import org.javarosa.core.model.FormIndex
import org.javarosa.core.reference.InvalidReferenceException
import org.javarosa.core.reference.ReferenceManager
import org.odk.collect.android.utilities.QuestionMediaManager
import org.odk.collect.android.utilities.getExistingAnswerFile
import org.odk.collect.androidshared.ui.multiclicksafe.MultiClickGuard
import org.odk.collect.draw.DrawActivity
import org.odk.collect.strings.R.string
import timber.log.Timber
import java.io.File

class DrawActivityLauncher(
    private val questionMediaManager: QuestionMediaManager,
    private val referenceManager: ReferenceManager,
    private val waitingForDataRegistry: WaitingForDataRegistry,
    private val tmpImageFilePath: String
) {
    fun launch(
        activity: Activity,
        formIndex: FormIndex,
        drawOption: String,
        binaryName: String?,
        requestCode: Int,
        errorStringResource: Int
    ) {
        if (!MultiClickGuard.allowClick(javaClass.name)) {
            return
        }

        val intent = Intent(activity, DrawActivity::class.java).apply {
            putExtra(DrawActivity.OPTION, drawOption)
            val file = getAnswerOrDefaultImageFile(binaryName)
            if (file != null && file.exists()) {
                putExtra(DrawActivity.REF_IMAGE, Uri.fromFile(file))
            }
            putExtra(DrawActivity.EXTRA_OUTPUT, Uri.fromFile(File(tmpImageFilePath)))
        }

        try {
            waitingForDataRegistry.waitForData(formIndex)
            activity.startActivityForResult(intent, requestCode)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(
                activity,
                activity.getString(string.activity_not_found, activity.getString(errorStringResource)),
                Toast.LENGTH_SHORT
            ).show()
            waitingForDataRegistry.cancelWaitingForData()
        }
    }

    private fun getAnswerOrDefaultImageFile(binaryName: String?): File? {
        if (binaryName == null) {
            return null
        }

        return questionMediaManager.getExistingAnswerFile(binaryName) ?: getDefaultFile(binaryName)
    }

    private fun getDefaultFile(binaryName: String): File? {
        return try {
            File(referenceManager.deriveReference(binaryName).localURI)
        } catch (e: InvalidReferenceException) {
            Timber.w(e)
            null
        }
    }
}
