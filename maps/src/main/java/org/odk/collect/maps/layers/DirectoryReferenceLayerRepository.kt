package org.odk.collect.maps.layers

import org.odk.collect.maps.MapConfigurator
import org.odk.collect.shared.PathUtils
import org.odk.collect.shared.files.DirectoryUtils.listFilesRecursively
import java.io.File

class DirectoryReferenceLayerRepository(
    private val directoryPaths: List<String>,
    private val getMapConfigurator: () -> MapConfigurator
) : ReferenceLayerRepository {

    override fun getAll(): List<ReferenceLayer> {
        return getAllFilesWithDirectory().map {
            ReferenceLayer(getIdForFile(it.second, it.first), it.first, getName(it.first))
        }.distinctBy { it.id }
    }

    override fun getAllSupported(): List<ReferenceLayer> {
        return getAll().filter { getMapConfigurator().supportsLayer(it.file) }
    }

    override fun get(id: String): ReferenceLayer? {
        val file = getAllFilesWithDirectory().firstOrNull { getIdForFile(it.second, it.first) == id }

        return if (file != null) {
            ReferenceLayer(getIdForFile(file.second, file.first), file.first, getName(file.first))
        } else {
            null
        }
    }

    private fun getAllFilesWithDirectory() = directoryPaths.flatMap { dir ->
        listFilesRecursively(File(dir)).map { file ->
            Pair(file, dir)
        }
    }

    private fun getIdForFile(directoryPath: String, file: File) =
        PathUtils.getRelativeFilePath(directoryPath, file.absolutePath)

    private fun getName(file: File): String {
        val name = MbtilesFile.readName(file)
        return name ?: file.getName()
    }
}
