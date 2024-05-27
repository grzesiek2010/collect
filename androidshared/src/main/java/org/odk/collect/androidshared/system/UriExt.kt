package org.odk.collect.androidshared.system

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

fun Uri.toFile(contentResolver: ContentResolver, dest: File) {
    try {
        contentResolver.openInputStream(this)?.use { inputStream ->
            FileOutputStream(dest).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    } catch (e: IOException) {
        Timber.e(e)
    }
}

fun Uri.getFileName(contentResolver: ContentResolver): String? {
    var fileName: String? = null
    if (scheme == ContentResolver.SCHEME_CONTENT) {
        val cursor = contentResolver.query(this, null, null, null, null)
        cursor.use {
            if (it != null && it.moveToFirst()) {
                val fileNameColumnIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                fileName = it.getString(fileNameColumnIndex)
            }
        }
    }
    if (fileName == null) {
        fileName = path?.substringAfterLast("/")
    }
    return fileName
}
