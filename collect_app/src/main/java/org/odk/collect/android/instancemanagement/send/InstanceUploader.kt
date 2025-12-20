package org.odk.collect.android.instancemanagement.send

import org.odk.collect.forms.instances.Instance
import kotlin.jvm.Throws

interface InstanceUploader {
    @Throws(FormUploadException::class)
    fun uploadOneSubmission(instance: Instance, urlString: String): String?

    fun getUrlToSubmitTo(instance: Instance, deviceId: String?, overrideURL: String?): String
}
