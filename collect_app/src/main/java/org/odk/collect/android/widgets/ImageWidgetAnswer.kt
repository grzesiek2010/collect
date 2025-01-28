package org.odk.collect.android.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import org.javarosa.core.model.data.IAnswerData
import org.javarosa.core.reference.InvalidReferenceException
import org.javarosa.core.reference.ReferenceManager
import org.odk.collect.android.databinding.ImageWidgetAnswerBinding
import org.odk.collect.android.utilities.QuestionMediaManager
import org.odk.collect.imageloader.GlideImageLoader.ImageLoaderCallback
import org.odk.collect.imageloader.ImageLoader
import timber.log.Timber
import java.io.File

class ImageWidgetAnswer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {
    private val binding = ImageWidgetAnswerBinding.inflate(LayoutInflater.from(context), this, true)

    private lateinit var imageLoader: ImageLoader
    private lateinit var questionMediaManager: QuestionMediaManager
    private lateinit var referenceManager: ReferenceManager
    private var binaryName: String? = null

    fun setup(
        answer: IAnswerData?,
        imageLoader: ImageLoader,
        questionMediaManager: QuestionMediaManager,
        referenceManager: ReferenceManager
    ) {
        this.imageLoader = imageLoader
        this.questionMediaManager = questionMediaManager
        this.referenceManager = referenceManager

        setAnswer(answer?.displayText)
    }

    fun setAnswer(answer: String?) {
        binaryName = answer

        if (binaryName != null) {
            val imageFile = getFile()
            if (imageFile != null && imageFile.exists()) {
                visibility = VISIBLE
                imageLoader.loadImage(
                    binding.answerView,
                    imageFile,
                    ImageView.ScaleType.FIT_CENTER,
                    object : ImageLoaderCallback {
                        override fun onLoadFailed() {
                            visibility = GONE
                        }

                        override fun onLoadSucceeded() {
                        }
                    })
            }
        }
    }

    fun getAnswer(): String? = binaryName

    fun getImageView() = binding.answerView

    private fun getFile(): File? {
        if (binaryName == null) {
            return null
        }

        val file = questionMediaManager.getAnswerFile(binaryName)
        if ((file == null || !file.exists())) {
            val filePath = defaultFilePath
            return if (filePath != null) {
                File(filePath)
            } else {
                null
            }
        }

        return file
    }

    private val defaultFilePath: String?
        get() {
            try {
                return referenceManager.deriveReference(binaryName).localURI
            } catch (e: InvalidReferenceException) {
                Timber.w(e)
            }

            return null
        }
}
