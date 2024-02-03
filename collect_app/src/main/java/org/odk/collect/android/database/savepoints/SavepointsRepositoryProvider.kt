package org.odk.collect.android.database.savepoints

import android.content.Context
import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.android.storage.StorageSubdirectory

class SavepointsRepositoryProvider(
    private val context: Context,
    private val storagePathProvider: StoragePathProvider
) {

    @JvmOverloads
    fun get(projectId: String? = null): SavepointsRepository {
        val dbPath = storagePathProvider.getOdkDirPath(StorageSubdirectory.METADATA, projectId)
        val cachePath = storagePathProvider.getOdkDirPath(StorageSubdirectory.CACHE, projectId)
        val instancesPath = storagePathProvider.getOdkDirPath(StorageSubdirectory.INSTANCES, projectId)

        return SavepointsDatabaseRepository(context, dbPath, cachePath, instancesPath)
    }
}
