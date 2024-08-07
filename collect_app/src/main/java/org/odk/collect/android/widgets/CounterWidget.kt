package org.odk.collect.android.widgets

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import org.javarosa.core.model.data.IAnswerData
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.databinding.CounterWidgetBinding
import org.odk.collect.android.formentry.questions.QuestionDetails
import org.odk.collect.android.widgets.utilities.StringWidgetUtils

class CounterWidget(
    context: Context,
    questionDetails: QuestionDetails
) : QuestionWidget(context, questionDetails) {
    lateinit var binding: CounterWidgetBinding

    private var value: Int? = null
        set(newValue) {
            field = newValue?.takeIf { it in 0..999999999 }
            binding.value.text = field?.toString().orEmpty()
            updateButtonStates(field)
        }

    init {
        render()
    }

    private fun updateButtonStates(value: Int?) {
        binding.minusButton.isEnabled = value != null && value > 0
        binding.plusButton.isEnabled = value == null || value < 999999999
    }

    override fun onCreateAnswerView(
        context: Context,
        prompt: FormEntryPrompt,
        answerFontSize: Int
    ): View {
        binding = CounterWidgetBinding.inflate(LayoutInflater.from(context))
        value = formEntryPrompt.answerValue?.value as Int?

        if (formEntryPrompt.isReadOnly) {
            binding.minusButton.isEnabled = false
            binding.plusButton.isEnabled = false
        }

        binding.minusButton.setOnClickListener {
            value = value?.minus(1)
            widgetValueChanged()
        }

        binding.plusButton.setOnClickListener {
            value = value?.plus(1) ?: 1
            widgetValueChanged()
        }
        return binding.root
    }

    override fun setOnLongClickListener(listener: OnLongClickListener?) = Unit

    override fun getAnswer(): IAnswerData? {
        return StringWidgetUtils.getIntegerData(value?.toString().orEmpty(), formEntryPrompt)
    }

    override fun clearAnswer() {
        value = null
        widgetValueChanged()
    }
}