package org.odk.collect.android.instancemanagement.send

sealed interface UploadResult {
    data class Success(val message: String?) : UploadResult
    data class Error(val exception: FormUploadException) : UploadResult
}
