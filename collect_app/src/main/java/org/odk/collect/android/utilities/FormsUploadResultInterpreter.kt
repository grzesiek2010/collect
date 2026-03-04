package org.odk.collect.android.utilities

import android.content.Context
import org.odk.collect.android.instancemanagement.send.UploadResult
import org.odk.collect.android.instancemanagement.userVisibleInstanceName
import org.odk.collect.errors.ErrorItem
import org.odk.collect.forms.instances.Instance
import org.odk.collect.strings.localization.getLocalizedString

object FormsUploadResultInterpreter {
    fun getFailures(result: Map<Instance, UploadResult>, context: Context) = result.filter {
        it.value is UploadResult.Error
    }.map {
        ErrorItem(
            it.key.userVisibleInstanceName(context.resources),
            context.getLocalizedString(org.odk.collect.strings.R.string.form_details, it.key.formId ?: "", it.key.formVersion ?: ""),
            (it.value as UploadResult.Error).exception.message
        )
    }

    fun getNumberOfFailures(result: Map<Instance, UploadResult>) = result.count {
        it.value is UploadResult.Error
    }

    fun allFormsUploadedSuccessfully(result: Map<Instance, UploadResult>) = result.values.all {
        it is UploadResult.Success
    }
}
