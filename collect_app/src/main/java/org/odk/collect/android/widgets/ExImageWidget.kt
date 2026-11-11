package org.odk.collect.android.widgets

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.ImageView
import org.javarosa.core.model.data.IAnswerData
import org.javarosa.core.model.data.StringData
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.databinding.ExImageWidgetAnswerBinding
import org.odk.collect.android.formentry.questions.QuestionDetails
import org.odk.collect.android.utilities.ApplicationConstants
import org.odk.collect.android.utilities.FileUtils
import org.odk.collect.android.utilities.QuestionMediaManager
import org.odk.collect.android.widgets.interfaces.FileWidget
import org.odk.collect.android.widgets.interfaces.WidgetDataReceiver
import org.odk.collect.android.widgets.utilities.FileRequester
import org.odk.collect.android.widgets.utilities.WaitingForDataRegistry
import org.odk.collect.androidshared.system.ContextExt.getActivity
import org.odk.collect.androidshared.ui.ToastUtils
import org.odk.collect.strings.R
import timber.log.Timber
import java.io.File

@SuppressLint("ViewConstructor")
class ExImageWidget(
    context: Context,
    questionDetails: QuestionDetails,
    private val questionMediaManager: QuestionMediaManager,
    private val waitingForDataRegistry: WaitingForDataRegistry,
    private val fileRequester: FileRequester,
    dependencies: Dependencies
) : QuestionWidget(context, dependencies, questionDetails), FileWidget, WidgetDataReceiver {
    lateinit var binding: ExImageWidgetAnswerBinding

    @JvmField
    var answerFile: File? = null

    init {
        render()
    }

    override fun onCreateWidgetView(context: Context, prompt: FormEntryPrompt, answerFontSize: Int): View {
        setupAnswerFile(prompt.answerText)

        binding = ExImageWidgetAnswerBinding.inflate((context as Activity).layoutInflater)

        binding.launchExternalAppButton.visibility = if (questionDetails.isReadOnly) GONE else VISIBLE
        binding.launchExternalAppButton.setOnClickListener { launchExternalApp() }
        binding.imageView.setOnClickListener { mediaUtils.openFile(getContext().getActivity(), answerFile!!, "image/*") }
        if (answerFile != null) {
            displayImage()
        } else {
            binding.imageView.visibility = GONE
        }

        return binding.root
    }

    override fun deleteFile() {
        questionMediaManager.deleteAnswerFile(formEntryPrompt.index.toString(), answerFile!!.absolutePath)
        answerFile = null
    }

    override fun clearAnswer() {
        deleteFile()
        binding.imageView.visibility = GONE
        widgetValueChanged()
    }

    override fun getAnswer(): IAnswerData? {
        return answerFile?.let { StringData(it.name) }
    }

    override fun setData(answer: Any) {
        if (answerFile != null) {
            clearAnswer()
        }

        if (answer is File && mediaUtils.isImageFile(answer)) {
            answerFile = answer
            if (answer.exists()) {
                questionMediaManager.replaceAnswerFile(formEntryPrompt.index.toString(), answer.absolutePath)
                displayImage()
                widgetValueChanged()
            } else {
                Timber.e(Error("Inserting Image file FAILED"))
            }
        } else if (answer is File) {
            ToastUtils.showLongToast(R.string.invalid_file_type)
            mediaUtils.deleteMediaFile(answer.absolutePath)
            Timber.e(Error("ExImageWidget's setBinaryData must receive an image file but received: " + FileUtils.getMimeType(answer)))
        } else {
            Timber.e(Error("ExImageWidget's setBinaryData must receive an image file but received: " + answer.javaClass))
        }
    }

    override fun setOnLongClickListener(listener: OnLongClickListener?) {
        binding.launchExternalAppButton.setOnLongClickListener(listener)
        binding.imageView.setOnLongClickListener(listener)
    }

    override fun cancelLongPress() {
        super.cancelLongPress()
        binding.launchExternalAppButton.cancelLongPress()
        binding.imageView.cancelLongPress()
    }

    private fun launchExternalApp() {
        waitingForDataRegistry.waitForData(formEntryPrompt.index)
        fileRequester.launch(context as Activity, ApplicationConstants.RequestCodes.EX_IMAGE_CHOOSER, formEntryPrompt)
    }

    private fun setupAnswerFile(fileName: String?) {
        if (!fileName.isNullOrEmpty()) {
            answerFile = questionMediaManager.getAnswerFile(fileName)
        }
    }

    private fun displayImage() {
        imageLoader.loadImage(binding.imageView, answerFile, ImageView.ScaleType.FIT_CENTER, null)
        binding.imageView.visibility = VISIBLE
    }
}
