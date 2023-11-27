package org.odk.collect.qrcode

import android.graphics.Bitmap
import org.odk.collect.androidshared.utils.CompressionUtils

class QRCodeEncoderImpl : QRCodeEncoder {
    @Throws(QRCodeEncoder.MaximumCharactersLimitException::class)
    override fun encode(data: String): Bitmap {
        val compressedData = CompressionUtils.compress(data)

        return QRCodeCreator().generate(compressedData)
    }
}

interface QRCodeEncoder {
    @Throws(MaximumCharactersLimitException::class)
    fun encode(data: String): Bitmap

    class MaximumCharactersLimitException : Exception()
}
