package org.odk.collect.forms.savepoints

import java.io.Serializable

data class Savepoint(
    val formDbId: Long,
    val instanceDbId: Long?,
    val savepointFilePath: String,
    val instanceFilePath: String
) : Serializable
