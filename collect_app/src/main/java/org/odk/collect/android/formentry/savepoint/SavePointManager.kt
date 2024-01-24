package org.odk.collect.android.formentry.savepoint

import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.android.storage.StorageSubdirectory
import org.odk.collect.forms.Form
import timber.log.Timber
import java.io.File

object SavePointManager {
    fun find(form: Form): File? {
        val filePrefix = form.formFilePath.substringAfterLast('/').substringBeforeLast('.') + "_"
        val fileSuffix = ".xml.save"

        val cacheDir = File(StoragePathProvider().getOdkDirPath(StorageSubdirectory.CACHE))
        val files = cacheDir.listFiles { pathname ->
            pathname.name.startsWith(filePrefix) && pathname.name.endsWith(fileSuffix)
        }

        if (files != null) {
            for (candidate in files) {
                val instanceDirName = candidate.name.removeSuffix(fileSuffix)
                val instanceDir = File(
                    StoragePathProvider().getOdkDirPath(StorageSubdirectory.INSTANCES),
                    instanceDirName
                )
                val instanceFile = File(instanceDir, "$instanceDirName.xml")

                if (instanceDir.exists() && instanceDir.isDirectory && !instanceFile.exists()) {
                    val savepointFile = File(cacheDir, instanceFile.name + ".save")
                    if (savepointFile.exists() && savepointFile.lastModified() > instanceFile.lastModified()) {
                        return savepointFile
                    }
                }
            }
        } else {
            Timber.e(Error("Couldn't access cache directory when looking for save points!"))
        }
        return null
    }

    fun remove(savePoint: File) {
        savePoint.delete()
    }
}
