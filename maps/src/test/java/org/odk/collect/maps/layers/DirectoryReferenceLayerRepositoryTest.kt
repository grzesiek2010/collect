package org.odk.collect.maps.layers

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.odk.collect.maps.MapConfigurator
import org.odk.collect.shared.TempFiles

class DirectoryReferenceLayerRepositoryTest {
    private val mapConfigurator = mock<MapConfigurator>()

    @Test
    fun getAll_returnsAllLayersInTheDirectory() {
        val dir = TempFiles.createTempDir()
        val file1 = TempFiles.createTempFile(dir)
        val file2 = TempFiles.createTempFile(dir)
        whenever(mapConfigurator.getDisplayName(file1)).thenReturn(file1.name)
        whenever(mapConfigurator.getDisplayName(file2)).thenReturn(file2.name)

        val repository = DirectoryReferenceLayerRepository(dir.absolutePath, "") { mapConfigurator }
        assertThat(repository.getAll().map { it.file }, containsInAnyOrder(file1, file2))
    }

    @Test
    fun getAll_returnsAllLayersInSubDirectories() {
        val dir1 = TempFiles.createTempDir()
        val dir2 = TempFiles.createTempDir(dir1)
        val file1 = TempFiles.createTempFile(dir2)
        val file2 = TempFiles.createTempFile(dir2)
        whenever(mapConfigurator.getDisplayName(file1)).thenReturn(file1.name)
        whenever(mapConfigurator.getDisplayName(file2)).thenReturn(file2.name)

        val repository = DirectoryReferenceLayerRepository(dir1.absolutePath, "") { mapConfigurator }
        assertThat(repository.getAll().map { it.file }, containsInAnyOrder(file1, file2))
    }

    @Test
    fun getAll_withMultipleDirectories_returnsAllLayersInAllDirectories() {
        val dir1 = TempFiles.createTempDir()
        val dir2 = TempFiles.createTempDir()
        val file1 = TempFiles.createTempFile(dir1)
        val file2 = TempFiles.createTempFile(dir2)
        whenever(mapConfigurator.getDisplayName(file1)).thenReturn(file1.name)
        whenever(mapConfigurator.getDisplayName(file2)).thenReturn(file2.name)

        val repository = DirectoryReferenceLayerRepository(dir1.absolutePath, dir2.absolutePath) { mapConfigurator }
        assertThat(repository.getAll().map { it.file }, containsInAnyOrder(file1, file2))
    }

    /**
     * We do this so we don't end up returns a list of reference layers with non-unique IDs. If two
     * (or more) files have the same relative path, only the first one (in the order of declared
     * layer directories) will be returned.
     */
    @Test
    fun getAll_withMultipleDirectoriesWithFilesWithTheSameRelativePath_onlyReturnsTheFileFromTheFirstDirectory() {
        val dir1 = TempFiles.createTempDir()
        val dir2 = TempFiles.createTempDir()
        val file1 = TempFiles.createTempFile(dir1, "blah", ".temp")
        val file2 = TempFiles.createTempFile(dir2, "blah", ".temp")
        whenever(mapConfigurator.getDisplayName(file1)).thenReturn(file1.name)
        whenever(mapConfigurator.getDisplayName(file2)).thenReturn(file2.name)

        val repository = DirectoryReferenceLayerRepository(dir1.absolutePath, dir2.absolutePath) { mapConfigurator }
        assertThat(repository.getAll().map { it.file }, containsInAnyOrder(file1))
    }

    @Test
    fun getAllSupported_returnsAllSupportedLayersInTheDirectory() {
        val dir = TempFiles.createTempDir()
        val file1 = TempFiles.createTempFile(dir)
        val file2 = TempFiles.createTempFile(dir)
        val file3 = TempFiles.createTempFile(dir)
        whenever(mapConfigurator.supportsLayer(file1)).thenReturn(true)
        whenever(mapConfigurator.supportsLayer(file2)).thenReturn(false)
        whenever(mapConfigurator.supportsLayer(file3)).thenReturn(true)
        whenever(mapConfigurator.getDisplayName(file1)).thenReturn(file1.name)
        whenever(mapConfigurator.getDisplayName(file2)).thenReturn(file2.name)
        whenever(mapConfigurator.getDisplayName(file3)).thenReturn(file3.name)

        val repository = DirectoryReferenceLayerRepository(dir.absolutePath, "") { mapConfigurator }
        assertThat(repository.getAllSupported().map { it.file }, containsInAnyOrder(file1, file3))
    }

    @Test
    fun getAllSupported_returnsAllSupportedLayersInSubDirectories() {
        val dir1 = TempFiles.createTempDir()
        val dir2 = TempFiles.createTempDir(dir1)
        val dir3 = TempFiles.createTempDir(dir1)
        val file1 = TempFiles.createTempFile(dir2)
        val file2 = TempFiles.createTempFile(dir2)
        val file3 = TempFiles.createTempFile(dir3)
        whenever(mapConfigurator.supportsLayer(file1)).thenReturn(true)
        whenever(mapConfigurator.supportsLayer(file2)).thenReturn(false)
        whenever(mapConfigurator.supportsLayer(file3)).thenReturn(true)
        whenever(mapConfigurator.getDisplayName(file1)).thenReturn(file1.name)
        whenever(mapConfigurator.getDisplayName(file2)).thenReturn(file2.name)
        whenever(mapConfigurator.getDisplayName(file3)).thenReturn(file3.name)

        val repository = DirectoryReferenceLayerRepository(dir1.absolutePath, "") { mapConfigurator }
        assertThat(repository.getAllSupported().map { it.file }, containsInAnyOrder(file1, file3))
    }

    @Test
    fun getAllSupported_withMultipleDirectories_returnsAllSupportedLayersInAllDirectories() {
        val dir1 = TempFiles.createTempDir()
        val dir2 = TempFiles.createTempDir()
        val file1 = TempFiles.createTempFile(dir1)
        val file2 = TempFiles.createTempFile(dir2)
        whenever(mapConfigurator.supportsLayer(file1)).thenReturn(true)
        whenever(mapConfigurator.supportsLayer(file2)).thenReturn(false)
        whenever(mapConfigurator.getDisplayName(file1)).thenReturn(file1.name)
        whenever(mapConfigurator.getDisplayName(file2)).thenReturn(file2.name)

        val repository =
            DirectoryReferenceLayerRepository(dir1.absolutePath, dir2.absolutePath) { mapConfigurator }
        assertThat(repository.getAllSupported().map { it.file }, containsInAnyOrder(file1))
    }

    /**
     * We do this so we don't end up returns a list of reference layers with non-unique IDs. If two
     * (or more) files have the same relative path, only the first one (in the order of declared
     * layer directories) will be returned.
     */
    @Test
    fun getAllSupported_withMultipleDirectoriesWithFilesWithTheSameRelativePath_onlyReturnsTheSupportedFileFromTheFirstDirectory() {
        val dir1 = TempFiles.createTempDir()
        val dir2 = TempFiles.createTempDir()
        val file1 = TempFiles.createTempFile(dir1, "blah", ".temp")
        val file2 = TempFiles.createTempFile(dir2, "blah", ".temp")
        val file3 = TempFiles.createTempFile(dir2, "blah2", ".temp")
        val file4 = TempFiles.createTempFile(dir2, "blah3", ".temp")
        whenever(mapConfigurator.supportsLayer(file1)).thenReturn(true)
        whenever(mapConfigurator.supportsLayer(file2)).thenReturn(true)
        whenever(mapConfigurator.supportsLayer(file3)).thenReturn(false)
        whenever(mapConfigurator.supportsLayer(file4)).thenReturn(true)
        whenever(mapConfigurator.getDisplayName(file1)).thenReturn(file1.name)
        whenever(mapConfigurator.getDisplayName(file2)).thenReturn(file2.name)
        whenever(mapConfigurator.getDisplayName(file3)).thenReturn(file3.name)
        whenever(mapConfigurator.getDisplayName(file4)).thenReturn(file4.name)

        val repository =
            DirectoryReferenceLayerRepository(dir1.absolutePath, dir2.absolutePath) { mapConfigurator }
        assertThat(repository.getAllSupported().map { it.file }, containsInAnyOrder(file1, file4))
    }

    @Test
    fun get_returnsLayer() {
        val dir = TempFiles.createTempDir()
        val file1 = TempFiles.createTempFile(dir)
        val file2 = TempFiles.createTempFile(dir)
        whenever(mapConfigurator.getDisplayName(file2)).thenReturn(file2.name)
        whenever(mapConfigurator.getDisplayName(file1)).thenReturn(file1.name)

        val repository = DirectoryReferenceLayerRepository(dir.absolutePath, "") { mapConfigurator }
        val file2Layer = repository.getAll().first { it.file == file2 }
        assertThat(repository.get(file2Layer.id)!!.file, equalTo(file2))
    }

    @Test
    fun get_withMultipleDirectories_returnsLayer() {
        val dir1 = TempFiles.createTempDir()
        val dir2 = TempFiles.createTempDir()
        val file1 = TempFiles.createTempFile(dir1)
        val file2 = TempFiles.createTempFile(dir2)
        whenever(mapConfigurator.getDisplayName(file1)).thenReturn(file1.name)
        whenever(mapConfigurator.getDisplayName(file2)).thenReturn(file2.name)

        val repository = DirectoryReferenceLayerRepository(dir1.absolutePath, dir2.absolutePath) { mapConfigurator }
        val file2Layer = repository.getAll().first { it.file == file2 }
        assertThat(repository.get(file2Layer.id)!!.file, equalTo(file2))
    }

    @Test
    fun get_withMultipleDirectoriesWithFilesWithTheSameRelativePath_returnsLayerFromFirstDirectory() {
        val dir1 = TempFiles.createTempDir()
        val dir2 = TempFiles.createTempDir()
        val file1 = TempFiles.createTempFile(dir1, "blah", ".temp")
        val file2 = TempFiles.createTempFile(dir2, "blah", ".temp")
        whenever(mapConfigurator.getDisplayName(file1)).thenReturn(file1.name)
        whenever(mapConfigurator.getDisplayName(file2)).thenReturn(file2.name)

        val repository = DirectoryReferenceLayerRepository(dir1.absolutePath, dir2.absolutePath) { mapConfigurator }
        val layerId = repository.getAll().first().id
        assertThat(repository.get(layerId)!!.file, equalTo(file1))
    }

    @Test
    fun get_whenFileDoesNotExist_returnsNull() {
        val dir = TempFiles.createTempDir()
        val file1 = TempFiles.createTempFile(dir)
        whenever(mapConfigurator.getDisplayName(file1)).thenReturn(file1.name)

        val repository = DirectoryReferenceLayerRepository(dir.absolutePath, "") { mapConfigurator }
        val fileLayer = repository.getAll().first { it.file == file1 }

        file1.delete()
        assertThat(repository.get(fileLayer.id), equalTo(null))
    }
}
