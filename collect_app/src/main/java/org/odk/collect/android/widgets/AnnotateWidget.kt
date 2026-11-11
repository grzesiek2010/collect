package org.odk.collect.android.widgets

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import org.javarosa.core.model.data.IAnswerData
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.databinding.AnnotateWidgetBinding
import org.odk.collect.android.formentry.questions.QuestionDetails
import org.odk.collect.android.utilities.Appearances
import org.odk.collect.android.utilities.ApplicationConstants.RequestCodes
import org.odk.collect.android.utilities.FileUtils
import org.odk.collect.android.utilities.QuestionMediaManager
import org.odk.collect.android.utilities.getExistingAnswerFile
import org.odk.collect.android.widgets.image.FileAnswerDelegate
import org.odk.collect.android.widgets.interfaces.WidgetDataReceiver
import org.odk.collect.android.widgets.utilities.DrawActivityLauncher
import org.odk.collect.android.widgets.utilities.ImageCaptureIntentCreator
import org.odk.collect.android.widgets.utilities.WaitingForDataRegistry
import org.odk.collect.androidshared.bitmap.ImageFileUtils
import org.odk.collect.androidshared.ui.ToastUtils
import org.odk.collect.draw.DrawActivity
import org.odk.collect.imageloader.GlideImageLoader
import org.odk.collect.permissions.PermissionListener
import org.odk.collect.strings.R
import java.io.File
import java.util.Locale

@SuppressLint("ViewConstructor")
class AnnotateWidget(
    context: Context,
    questionDetails: QuestionDetails,
    private val questionMediaManager: QuestionMediaManager,
    private val waitingForDataRegistry: WaitingForDataRegistry,
    private val tmpImageFilePath: String,
    dependencies: Dependencies
) : QuestionWidget(context, dependencies, questionDetails), WidgetDataReceiver {
    lateinit var binding: AnnotateWidgetBinding

    private val fileAnswerDelegate = FileAnswerDelegate(this, questionMediaManager, questionDetails.prompt)
    private val drawActivityLauncher = DrawActivityLauncher(questionMediaManager, referenceManager, waitingForDataRegistry, tmpImageFilePath)

    init {
        render()
    }

    override fun onCreateWidgetView(context: Context, prompt: FormEntryPrompt, answerFontSize: Int): View {
        binding = AnnotateWidgetBinding.inflate((context as Activity).layoutInflater)
        updateAnswer()

        if (formEntryPrompt.appearanceHint?.lowercase(Locale.ENGLISH)?.contains(Appearances.NEW) == true) {
            binding.chooseButton.visibility = View.GONE
        }

        updateButtonStates()

        binding.captureButton.setOnClickListener {
            permissionsProvider.requestCameraPermission(getContext() as Activity, object : PermissionListener {
                override fun granted() {
                    val intent = ImageCaptureIntentCreator.imageCaptureIntent(formEntryPrompt, getContext(), tmpImageFilePath)
                    launchActivityForResult(intent, RequestCodes.IMAGE_CAPTURE, R.string.annotate_image)
                }
            })
        }
        binding.chooseButton.setOnClickListener { chooseImage() }
        binding.annotateButton.setOnClickListener { launchDrawActivity() }
        binding.image.setOnClickListener { launchDrawActivity() }

        if (questionDetails.isReadOnly) {
            binding.captureButton.visibility = View.GONE
            binding.chooseButton.visibility = View.GONE
            binding.annotateButton.visibility = View.GONE
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
        updateButtonStates()
        binding.captureButton.text = context.getString(R.string.capture_image)
    }

    override fun setOnLongClickListener(l: OnLongClickListener?) {
        binding.captureButton.setOnLongClickListener(l)
        binding.chooseButton.setOnLongClickListener(l)
        binding.annotateButton.setOnLongClickListener(l)
        binding.image.setOnLongClickListener(l)
    }

    override fun cancelLongPress() {
        super.cancelLongPress()
        binding.captureButton.cancelLongPress()
        binding.chooseButton.cancelLongPress()
        binding.annotateButton.cancelLongPress()
        binding.image.cancelLongPress()
    }

    override fun setData(newImageObj: Any) {
        if (newImageObj is File) {
            val mimeType = FileUtils.getMimeType(newImageObj)
            if (mimeType == "image/gif") {
                ToastUtils.showLongToast(R.string.gif_not_supported)
            } else {
                fileAnswerDelegate.setData(newImageObj)
                updateAnswer()
                updateButtonStates()
            }
        }
    }

    private fun launchDrawActivity() {
        val screenOrientation = if (isBaseImagePortrait()) {
            SCREEN_ORIENTATION_PORTRAIT
        } else {
            SCREEN_ORIENTATION_LANDSCAPE
        }

        drawActivityLauncher.launch(
            context as Activity,
            formEntryPrompt.index,
            DrawActivity.OPTION_ANNOTATE,
            fileAnswerDelegate.binaryName,
            RequestCodes.ANNOTATE_IMAGE,
            R.string.annotate_image,
            screenOrientation
        )
    }

    private fun isBaseImagePortrait(): Boolean {
        val baseImage = drawActivityLauncher.getAnswerOrDefaultImageFile(fileAnswerDelegate.binaryName)
        if (baseImage == null || !baseImage.exists()) {
            return false
        }

        val dimensions = ImageFileUtils.getImageDisplayDimensions(baseImage)
        return dimensions != null && dimensions.height > dimensions.width
    }

    private fun chooseImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }
        launchActivityForResult(intent, RequestCodes.IMAGE_CHOOSER, R.string.annotate_image)
    }

    private fun launchActivityForResult(intent: Intent, requestCode: Int, errorStringResource: Int) {
        try {
            waitingForDataRegistry.waitForData(formEntryPrompt.index)
            (context as Activity).startActivityForResult(intent, requestCode)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(
                context,
                context.getString(R.string.activity_not_found, context.getString(errorStringResource)),
                Toast.LENGTH_SHORT
            ).show()
            waitingForDataRegistry.cancelWaitingForData()
        }
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

    private fun updateButtonStates() {
        val isDefaultImage = isDefaultImage()
        val isAnswerImage = questionMediaManager.getExistingAnswerFile(fileAnswerDelegate.binaryName) != null

        binding.annotateButton.isEnabled = isDefaultImage || isAnswerImage
        binding.captureButton.isEnabled = !isDefaultImage
        binding.chooseButton.isEnabled = !isDefaultImage
    }

    private fun isDefaultImage(): Boolean {
        val binaryName = fileAnswerDelegate.binaryName
        if (binaryName?.startsWith("jr://images/") != true) {
            return false
        }

        val file = drawActivityLauncher.getAnswerOrDefaultImageFile(binaryName)
        return file != null && file.exists()
    }
}
