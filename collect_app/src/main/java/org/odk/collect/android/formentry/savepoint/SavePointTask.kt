package org.odk.collect.android.formentry.savepoint

import android.util.Log
import org.odk.collect.android.javarosawrapper.FormController
import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.android.storage.StorageSubdirectory
import org.odk.collect.android.tasks.SaveFormToDisk
import org.odk.collect.async.Scheduler
import org.odk.collect.async.SchedulerAsyncTaskMimic
import org.odk.collect.forms.Form
import org.odk.collect.forms.FormsRepository
import org.odk.collect.forms.instances.Instance
import org.odk.collect.forms.instances.InstancesRepository
import timber.log.Timber
import java.io.File

interface SavePointListener {
    fun onSavePointError(errorMessage: String?)
}

class SavePointTask(
    private var listener: SavePointListener?,
    private val formController: FormController,
    private val formDbId: Long,
    private val instanceDbId: Long?,
    private val formsRepository: FormsRepository,
    private val instancesRepository: InstancesRepository,
    val scheduler: Scheduler
) : SchedulerAsyncTaskMimic<Unit, Unit, String?>(scheduler) {
    private var priority: Int = ++lastPriorityUsed

    override fun onPreExecute() = Unit

    override fun doInBackground(vararg params: Unit): String? {
        Log.i("QWERTY", "SavePointTask")

        if (priority < lastPriorityUsed) {
            Log.i("QWERTY", "priority < lastPriorityUsed")
            return null
        }

        return try {
            Log.i("QWERTY", "try")

            val payload = formController.getFilledInFormXml()
            Log.i("QWERTY", "${payload.toString()}")
            val cacheDir = StoragePathProvider().getOdkDirPath(StorageSubdirectory.CACHE)
            val form = formsRepository.get(formDbId)!!
            val instance = if (instanceDbId != null) instancesRepository.get(instanceDbId) else null
            val savePointFile = if (instance == null) {
                val savepointFileName = form.formFilePath.substringAfterLast("/")
                if (form.savePointFilePath.isNullOrBlank()) {
                    formsRepository.save(Form.Builder(form).savePointFilePath(savepointFileName).build())
                }
                File(cacheDir, savepointFileName)
            } else {
                val savepointFileName = instance.instanceFilePath.substringAfterLast("/")
                if (instance.savePointFilePath.isNullOrBlank()) {
                    instancesRepository.save(Instance.Builder(instance).savePointFilePath(savepointFileName).build())
                }
                File(cacheDir, savepointFileName)
            }

            if (priority == lastPriorityUsed) {
                SaveFormToDisk.writeFile(payload, savePointFile.absolutePath)
            }

            null
        } catch (e: Exception) {
            Timber.e(e.message)
            e.message
        }
    }

    override fun onPostExecute(result: String?) {
        if (result != null) {
            listener?.onSavePointError(result)
            listener = null
        }
    }

    override fun onProgressUpdate(vararg values: Unit) = Unit

    override fun onCancelled() = Unit

    companion object {
        private var lastPriorityUsed: Int = 0
    }
}
