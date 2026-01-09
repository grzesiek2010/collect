package org.odk.collect.android.formentry

import org.javarosa.core.model.FormIndex
import org.odk.collect.android.javarosawrapper.ValidationResult

data class CurrentIndex(
    val screenIndex: FormIndex,
    val questionIndex: FormIndex?,
    val validationResult: ValidationResult?
)
