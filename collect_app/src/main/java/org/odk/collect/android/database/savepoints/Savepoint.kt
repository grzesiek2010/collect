package org.odk.collect.android.database.savepoints

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Savepoint(
    val formDbId: Long,
    val instanceDbId: Long?,
    val savepointFilePath: String,
    val instanceFilePath: String
) : Parcelable
