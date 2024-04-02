package org.odk.collect.android.application.initialization

import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.android.storage.StorageSubdirectory
import org.odk.collect.android.utilities.FormsRepositoryProvider
import org.odk.collect.android.utilities.DatabaseInstancesRepositoryProvider
import org.odk.collect.android.utilities.SavepointsRepositoryProvider
import org.odk.collect.forms.savepoints.Savepoint
import org.odk.collect.projects.ProjectsRepository
import org.odk.collect.settings.keys.MetaKeys
import org.odk.collect.upgrade.Upgrade
import java.io.File

class SavepointsMigrator(
        private val projectsRepository: ProjectsRepository,
        private val instancesRepositoryProvider: DatabaseInstancesRepositoryProvider,
        private val formsRepositoryProvider: FormsRepositoryProvider,
        private val savepointsRepositoryProvider: SavepointsRepositoryProvider,
        private val storagePathProvider: StoragePathProvider
) : Upgrade {
    override fun key(): String {
        return MetaKeys.OLD_SAVEPOINTS_MIGRATED
    }

    override fun run() {
        projectsRepository.getAll().forEach { project ->
            val instancesRepository = instancesRepositoryProvider.get(project.uuid)
            val formsRepository = formsRepositoryProvider.get(project.uuid)
            val savepointsRepository = savepointsRepositoryProvider.get(project.uuid)
            val cacheDir = File(storagePathProvider.getOdkDirPath(StorageSubdirectory.CACHE, project.uuid))

            instancesRepository.all.forEach { instance ->
                val savepointFileName = instance.instanceFilePath
                    .substringAfterLast('/')
                    .plus(".save")

                val savepointFile = File("$cacheDir/$savepointFileName")
                if (savepointFile.exists()) {
                    formsRepository.getAllByFormIdAndVersion(instance.formId, instance.formVersion).firstOrNull()?.let { form ->
                        savepointsRepository.save(Savepoint(form.dbId, instance.dbId, savepointFileName, instance.instanceFilePath))
                    }
                    savepointFile.renameTo(File("$savepointFile.point"))
                }
            }

            formsRepository.all.sortedBy { form -> form.version }.forEach { form ->
                val formDirName = form.formFilePath.substringBeforeLast(".xml")

                cacheDir.listFiles()?.firstOrNull { file ->
                    file.name.startsWith(formDirName) &&
                    file.name.endsWith(".xml.save")
                }?.let { savepointFile ->
                    savepointsRepository.save(Savepoint(form.dbId, null, savepointFile.name, "$formDirName/$formDirName.xml"))
                    savepointFile.renameTo(File("$savepointFile.point"))
                }
            }
        }
    }
}
