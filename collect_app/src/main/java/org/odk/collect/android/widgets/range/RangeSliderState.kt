package org.odk.collect.android.widgets.range

import org.javarosa.core.model.Constants.DATATYPE_INTEGER
import org.javarosa.core.model.RangeQuestion
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.utilities.Appearances
import kotlin.math.absoluteValue

data class RangeSliderState(
    val sliderValue: Float?,
    val rangeStart: Float,
    val rangeEnd: Float,
    val numOfSteps: Int,
    val isDiscrete: Boolean,
    val isHorizontal: Boolean,
    val isValid: Boolean,
    val isEnabled: Boolean,
    val numOfTicks: Int
) {
    val realValue
        get() = sliderValue?.let {
            rangeStart + it * (rangeEnd - rangeStart)
        }

    val valueLabel
        get() = realValue?.let {
            if (isDiscrete) it.toInt().toString() else it.toString()
        }.orEmpty()

    val startLabel
        get() = if (isDiscrete) rangeStart.toInt().toString() else rangeStart.toString()

    val endLabel
        get() = if (isDiscrete) rangeEnd.toInt().toString() else rangeEnd.toString()

    companion object {
        fun fromPrompt(prompt: FormEntryPrompt): RangeSliderState {
            val rangeQuestion = prompt.question as RangeQuestion
            val start = rangeQuestion.rangeStart.toFloat()
            val end = rangeQuestion.rangeEnd.toFloat()
            val step = rangeQuestion.rangeStep.toFloat().absoluteValue
            val sliderValue = toSliderValue(prompt.answerValue?.value?.toString()?.toFloatOrNull(), start, end)
            val numSteps = ((end - start).absoluteValue / step).toInt().coerceAtLeast(1) - 1
            val isDiscrete = prompt.dataType == DATATYPE_INTEGER
            val isValid = step != 0f && start != end && ((end - start) % step == 0f)
            val sanitizedAppearance = Appearances.getSanitizedAppearanceHint(prompt)
            val isHorizontal = !sanitizedAppearance.contains(Appearances.VERTICAL)
            val showTicks = isValid && !sanitizedAppearance.contains(Appearances.NO_TICKS)

            return RangeSliderState(
                sliderValue = sliderValue,
                rangeStart = start,
                rangeEnd = end,
                numOfSteps = numSteps,
                isDiscrete = isDiscrete,
                isHorizontal = isHorizontal,
                isValid = isValid,
                isEnabled = !prompt.isReadOnly && isValid,
                numOfTicks = if (showTicks) numSteps + 2 else 0
            )
        }

        private fun toSliderValue(value: Float?, start: Float, end: Float): Float? {
            val sliderValue = value?.let {
                (it - start) / (end - start)
            }
            return if (sliderValue == null || sliderValue !in 0f..1f) {
                null
            } else {
                sliderValue
            }
        }
    }
}
