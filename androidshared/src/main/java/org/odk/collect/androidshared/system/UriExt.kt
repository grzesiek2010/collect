package org.odk.collect.androidshared.system

import android.content.ContentResolver
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
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

fun Uri.getFileExtension(contentResolver: ContentResolver): String? {
    val mimeType = contentResolver.getType(this)
    var extension = if (scheme != null && scheme == ContentResolver.SCHEME_CONTENT) {
        MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
    } else {
        MimeTypeMap.getFileExtensionFromUrl(this.toString())
    }
    if (extension.isNullOrEmpty()) {
        contentResolver.query(this, null, null, null, null).use { cursor ->
            var name: String? = null
            if (cursor != null && cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                name = cursor.getString(nameIndex)
            }
            extension = name?.substring(name.lastIndexOf('.') + 1) ?: ""
        }
    }

    if (extension.isNullOrEmpty() && mimeType != null && mimeType.contains("/")) {
        extension = mimeType.substring(mimeType.lastIndexOf('/') + 1)
    }

    return extension
}
