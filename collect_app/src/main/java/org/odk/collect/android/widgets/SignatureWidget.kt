package org.odk.collect.android.widgets

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.ImageView
import org.javarosa.core.model.data.IAnswerData
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.databinding.SignatureWidgetBinding
import org.odk.collect.android.formentry.questions.QuestionDetails
import org.odk.collect.android.utilities.ApplicationConstants.RequestCodes
import org.odk.collect.android.utilities.QuestionMediaManager
import org.odk.collect.android.utilities.getExistingAnswerFile
import org.odk.collect.android.widgets.image.FileAnswerDelegate
import org.odk.collect.android.widgets.interfaces.WidgetDataReceiver
import org.odk.collect.android.widgets.utilities.DrawActivityLauncher
import org.odk.collect.android.widgets.utilities.WaitingForDataRegistry
import org.odk.collect.draw.DrawActivity
import org.odk.collect.imageloader.GlideImageLoader
import org.odk.collect.strings.R

@SuppressLint("ViewConstructor")
class SignatureWidget(
    context: Context,
    questionDetails: QuestionDetails,
    private val questionMediaManager: QuestionMediaManager,
    waitingForDataRegistry: WaitingForDataRegistry,
    tmpImageFilePath: String,
    dependencies: Dependencies
) : QuestionWidget(context, dependencies, questionDetails), WidgetDataReceiver {
    lateinit var binding: SignatureWidgetBinding

    private val fileAnswerDelegate = FileAnswerDelegate(this, questionMediaManager, questionDetails.prompt)
    private val drawActivityLauncher = DrawActivityLauncher(questionMediaManager, referenceManager, waitingForDataRegistry, tmpImageFilePath)

    init {
        render()
        updateAnswer()
    }

    override fun onCreateWidgetView(context: Context, prompt: FormEntryPrompt, answerFontSize: Int): View {
        binding = SignatureWidgetBinding.inflate((context as Activity).layoutInflater)
        binding.signButton.setOnClickListener { launchDrawActivity() }
        binding.image.setOnClickListener { launchDrawActivity() }

        if (questionDetails.isReadOnly) {
            binding.signButton.visibility = View.GONE
        }

        return binding.root
    }

    override fun getAnswer(): IAnswerData? {
        return fileAnswerDelegate.getAnswer()
    }

    override fun clearAnswer() {
        fileAnswerDelegate.deleteFile()
        binding.image.setImageDrawable(null)
        updateAnswer()
    }

    override fun setData(answer: Any) {
        fileAnswerDelegate.setData(answer)
        updateAnswer()
    }

    override fun setOnLongClickListener(l: OnLongClickListener?) {
        binding.signButton.setOnLongClickListener(l)
        binding.image.setOnLongClickListener(l)
    }

    override fun cancelLongPress() {
        super.cancelLongPress()
        binding.signButton.cancelLongPress()
        binding.image.cancelLongPress()
    }

    private fun launchDrawActivity() {
        drawActivityLauncher.launch(
            context as Activity,
            formEntryPrompt.index,
            DrawActivity.OPTION_SIGNATURE,
            fileAnswerDelegate.binaryName,
            RequestCodes.SIGNATURE_CAPTURE,
            R.string.signature_capture
        )
    }

    private fun updateAnswer() {
        binding.image.visibility = View.GONE
        binding.errorMessage.visibility = View.GONE

        val file = questionMediaManager.getExistingAnswerFile(fileAnswerDelegate.binaryName)
        if (file != null) {
            binding.image.visibility = View.VISIBLE
            imageLoader.loadImage(binding.image, file, ImageView.ScaleType.FIT_CENTER, object : GlideImageLoader.ImageLoaderCallback {
                override fun onLoadFailed() {
                    binding.image.visibility = View.GONE
                    binding.errorMessage.visibility = View.VISIBLE
                }

                override fun onLoadSucceeded() {}
            })
        }
    }
}
