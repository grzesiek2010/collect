package org.odk.collect.android.utilities

import android.content.Context
import org.odk.collect.android.database.instances.DatabaseInstancesRepository
import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.android.storage.StorageSubdirectory
import org.odk.collect.forms.instances.InstancesRepository
import org.odk.collect.forms.instances.InstancesRepositoryProvider

class DatabaseInstancesRepositoryProvider @JvmOverloads constructor(
    private val context: Context,
    private val storagePathProvider: StoragePathProvider = StoragePathProvider()
) : InstancesRepositoryProvider {

    override fun get(projectId: String?): InstancesRepository {
        return DatabaseInstancesRepository(
            context,
            storagePathProvider.getOdkDirPath(StorageSubdirectory.METADATA, projectId),
            storagePathProvider.getOdkDirPath(StorageSubdirectory.INSTANCES, projectId),
            System::currentTimeMillis
        )
    }
}
