package org.odk.collect.maps.layers

import java.io.File

data class CheckableReferenceLayer(
    val id: String?,
    val file: File?,
    val name: String,
    val isChecked: Boolean,
    val isExpanded: Boolean
)
