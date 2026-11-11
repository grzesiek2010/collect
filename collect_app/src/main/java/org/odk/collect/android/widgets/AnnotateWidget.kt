package org.odk.collect.android.widgets

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
import android.view.View
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.databinding.AnnotateWidgetBinding
import org.odk.collect.android.formentry.questions.QuestionDetails
import org.odk.collect.android.utilities.Appearances
import org.odk.collect.android.utilities.ApplicationConstants.RequestCodes
import org.odk.collect.android.utilities.FileUtils
import org.odk.collect.android.utilities.QuestionMediaManager
import org.odk.collect.android.utilities.getExistingAnswerFile
import org.odk.collect.android.widgets.utilities.ImageCaptureIntentCreator
import org.odk.collect.android.widgets.utilities.WaitingForDataRegistry
import org.odk.collect.androidshared.bitmap.ImageFileUtils
import org.odk.collect.androidshared.ui.ToastUtils
import org.odk.collect.draw.DrawActivity
import org.odk.collect.permissions.PermissionListener
import org.odk.collect.strings.R
import java.io.File
import java.util.Locale

@SuppressLint("ViewConstructor")
class AnnotateWidget(
    context: Context,
    prompt: QuestionDetails,
    questionMediaManager: QuestionMediaManager,
    waitingForDataRegistry: WaitingForDataRegistry,
    tmpImageFilePath: String,
    dependencies: Dependencies
) : BaseImageWidget(context, prompt, questionMediaManager, waitingForDataRegistry, tmpImageFilePath, dependencies) {
    lateinit var binding: AnnotateWidgetBinding

    init {
        imageClickHandler = DrawImageClickHandler(DrawActivity.OPTION_ANNOTATE, RequestCodes.ANNOTATE_IMAGE, R.string.annotate_image)
        imageCaptureHandler = ImageCaptureHandler()

        render()
    }

    override fun onCreateWidgetView(context: Context, prompt: FormEntryPrompt, answerFontSize: Int): View {
        binding = AnnotateWidgetBinding.inflate((context as Activity).layoutInflater)
        errorTextView = binding.errorMessage
        imageView = binding.image
        updateAnswer()

        if (formEntryPrompt.appearanceHint?.lowercase(Locale.ENGLISH)?.contains(Appearances.NEW) == true) {
            binding.chooseButton.visibility = View.GONE
        }

        updateButtonStates()

        binding.captureButton.setOnClickListener {
            permissionsProvider.requestCameraPermission(getContext() as Activity, object : PermissionListener {
                override fun granted() {
                    val intent = ImageCaptureIntentCreator.imageCaptureIntent(formEntryPrompt, getContext(), tmpImageFilePath)
                    imageCaptureHandler.captureImage(intent, RequestCodes.IMAGE_CAPTURE, R.string.annotate_image)
                }
            })
        }
        binding.chooseButton.setOnClickListener { imageCaptureHandler.chooseImage(R.string.annotate_image) }
        binding.annotateButton.setOnClickListener { imageClickHandler.clickImage("annotateButton") }
        binding.image.setOnClickListener { imageClickHandler.clickImage("viewImage") }

        if (questionDetails.isReadOnly) {
            binding.captureButton.visibility = View.GONE
            binding.chooseButton.visibility = View.GONE
            binding.annotateButton.visibility = View.GONE
        }

        return binding.root
    }

    override fun addExtrasToIntent(intent: Intent): Intent {
        val screenOrientation = if (isBaseImagePortrait()) {
            SCREEN_ORIENTATION_PORTRAIT
        } else {
            SCREEN_ORIENTATION_LANDSCAPE
        }

        intent.putExtra(DrawActivity.SCREEN_ORIENTATION, screenOrientation)
        return intent
    }

    private fun isBaseImagePortrait(): Boolean {
        val baseImage = answerOrDefaultImageFile
        if (baseImage == null || !baseImage.exists()) {
            return false
        }

        val dimensions = ImageFileUtils.getImageDisplayDimensions(baseImage)
        return dimensions != null && dimensions.height > dimensions.width
    }

    override fun clearAnswer() {
        super.clearAnswer()
        updateButtonStates()
        binding.captureButton.text = context.getString(R.string.capture_image)
    }

    override fun setOnLongClickListener(l: OnLongClickListener?) {
        binding.captureButton.setOnLongClickListener(l)
        binding.chooseButton.setOnLongClickListener(l)
        binding.annotateButton.setOnLongClickListener(l)
        super.setOnLongClickListener(l)
    }

    override fun cancelLongPress() {
        super.cancelLongPress()
        binding.captureButton.cancelLongPress()
        binding.chooseButton.cancelLongPress()
        binding.annotateButton.cancelLongPress()
    }

    override fun setData(newImageObj: Any) {
        if (newImageObj is File) {
            val mimeType = FileUtils.getMimeType(newImageObj)
            if (mimeType == "image/gif") {
                ToastUtils.showLongToast(R.string.gif_not_supported)
            } else {
                super.setData(newImageObj)
                updateButtonStates()
            }
        }
    }

    private fun updateButtonStates() {
        val isDefaultImage = isDefaultImage()
        val isAnswerImage = questionMediaManager.getExistingAnswerFile(binaryName) != null

        binding.annotateButton.isEnabled = isDefaultImage || isAnswerImage
        binding.captureButton.isEnabled = !isDefaultImage
        binding.chooseButton.isEnabled = !isDefaultImage
    }

    private fun isDefaultImage(): Boolean {
        if (binaryName?.startsWith("jr://images/") != true) {
            return false
        }

        val file = answerOrDefaultImageFile
        return file != null && file.exists()
    }
}
