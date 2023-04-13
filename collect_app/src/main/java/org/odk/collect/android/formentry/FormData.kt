package org.odk.collect.android.formentry

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
sealed class FormData : Parcelable {
    abstract val formPath: String

    data class BlankFormData(
        override val formPath: String
    ) : FormData()

    data class SavedFormData(
        override val formPath: String,
        val instancePath: String
    ) : FormData()
}
