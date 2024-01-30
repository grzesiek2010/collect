package org.odk.collect.android.formentry.savepoint

import org.odk.collect.android.formentry.saving.FormSaveViewModel
import org.odk.collect.android.instancemanagement.autosend.shouldFormBeSentAutomatically
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
        private val formSaveViewModel: FormSaveViewModel,
        private val form: Form,
        private val instance: Instance,
        private val formsRepository: FormsRepository,
        private val instancesRepository: InstancesRepository,
        val scheduler: Scheduler
) : SchedulerAsyncTaskMimic<Unit, Unit, String?>(scheduler) {
    private var priority: Int = ++lastPriorityUsed

    override fun onPreExecute() = Unit

    override fun doInBackground(vararg params: Unit): String? {
        if (priority < lastPriorityUsed) {
            return null
        }

        return try {
            val payload = formController.getFilledInFormXml()
            val savePointFile = if (formSaveViewModel.lastSavedTime == null) {
                if (form.savePointFilePath.isNullOrBlank()) {
                    formsRepository.save(Form.Builder(form).savePointFilePath("formBlah.xml").build())
                }
                File(StoragePathProvider().getOdkDirPath(StorageSubdirectory.CACHE), "formBlah.xml")
            } else {
                if (instance.savePointFilePath.isNullOrBlank()) {
                    instancesRepository.save(Instance.Builder(instance).savePointFilePath("instanceBlah.xml").build())
                }
                File(StoragePathProvider().getOdkDirPath(StorageSubdirectory.CACHE), "instanceBlah.xml")
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
