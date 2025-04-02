package org.odk.collect.android.instancemanagement

import org.javarosa.form.api.FormEntryController
import org.javarosa.form.api.FormEntryModel
import org.odk.collect.android.formentry.FormEntryUseCases
import org.odk.collect.android.javarosawrapper.FormController
import java.io.File

class InstanceCloner {
    fun clone(formController: FormController): String? {
        val sourceInstanceFile = formController.getInstanceFile() ?: return null
        val targetInstanceFile = copyInstanceDir(sourceInstanceFile) ?: return null

        val newFormController = FormEntryUseCases.loadEditedForm(
            formController.getMediaFolder()?.absolutePath,
            FormEntryController(FormEntryModel(formController.getFormDef())),
            targetInstanceFile
        )
        FormEntryUseCases.saveInstanceToDisk(newFormController)

        return targetInstanceFile.absolutePath
    }

    private fun copyInstanceDir(sourceInstanceFile: File): File? {
        val sourceInstanceDir = sourceInstanceFile.parentFile ?: return null
        val targetInstanceDir = File(sourceInstanceDir.parent, "${sourceInstanceDir.name}_1")

        if (!sourceInstanceDir.copyRecursively(targetInstanceDir, true)) return null

        val targetInstanceFile = File(targetInstanceDir, sourceInstanceFile.name)
        val updatedTargetInstanceFile = File(targetInstanceDir, "${sourceInstanceFile.nameWithoutExtension}_1.${sourceInstanceFile.extension}")

        return if (targetInstanceFile.renameTo(updatedTargetInstanceFile)) {
            updatedTargetInstanceFile
        } else {
            null
        }
    }
}
