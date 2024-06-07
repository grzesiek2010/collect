package org.odk.collect.maps.layers

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.odk.collect.androidshared.async.TrackableWorker
import org.odk.collect.androidshared.system.copyToFile
import org.odk.collect.androidshared.system.getFileExtension
import org.odk.collect.androidshared.system.getFileName
import org.odk.collect.async.Scheduler
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.keys.ProjectKeys
import org.odk.collect.shared.TempFiles
import java.io.File

class OfflineMapLayersViewModel(
    private val referenceLayerRepository: ReferenceLayerRepository,
    scheduler: Scheduler,
    private val settingsProvider: SettingsProvider
) : ViewModel() {
    val trackableWorker = TrackableWorker(scheduler)

    private val _existingLayers = MutableLiveData<List<CheckableReferenceLayer>>()
    val existingLayers: LiveData<List<CheckableReferenceLayer>> = _existingLayers

    private val _layersToImport = MutableLiveData<List<ReferenceLayer>>()
    val layersToImport: LiveData<List<ReferenceLayer>> = _layersToImport

    private lateinit var tempLayersDir: File

    init {
        loadExistingLayers()
    }

    private fun loadExistingLayers() {
        trackableWorker.immediate(
            background = {
                val layers = referenceLayerRepository.getAll()
                val checkedLayerId =
                    settingsProvider.getUnprotectedSettings().getString(ProjectKeys.KEY_REFERENCE_LAYER)

                val newData = mutableListOf(CheckableReferenceLayer(null, null, "", checkedLayerId == null, false))
                newData.addAll(layers.map { CheckableReferenceLayer(it.id, it.file, it.name, it.id == checkedLayerId, false) })
                _existingLayers.postValue(newData)
            },
            foreground = { }
        )
    }

    fun loadLayersToImport(uris: List<Uri>, context: Context) {
        trackableWorker.immediate(
            background = {
                tempLayersDir = TempFiles.createTempDir().also {
                    it.deleteOnExit()
                }
                val layers = mutableListOf<ReferenceLayer>()
                uris.forEach { uri ->
                    if (uri.getFileExtension(context) == MbtilesFile.FILE_EXTENSION) {
                        uri.getFileName(context)?.let { fileName ->
                            val layerFile = File(tempLayersDir, fileName).also { file ->
                                uri.copyToFile(context, file)
                            }
                            layers.add(ReferenceLayer(layerFile.absolutePath, layerFile, MbtilesFile.readName(layerFile) ?: layerFile.name))
                        }
                    }
                }
                _layersToImport.postValue(layers)
            },
            foreground = { }
        )
    }

    fun importNewLayers(shared: Boolean) {
        trackableWorker.immediate(
            background = {
                tempLayersDir.listFiles()?.forEach {
                    referenceLayerRepository.addLayer(it, shared)
                }
                tempLayersDir.delete()
            },
            foreground = {
                loadExistingLayers()
            }
        )
    }

    fun saveCheckedLayer() {
        settingsProvider.getUnprotectedSettings().save(ProjectKeys.KEY_REFERENCE_LAYER, getCheckedLayer())
    }

    fun onLayerChecked(layerId: String?) {
        _existingLayers.value = _existingLayers.value?.map {
            it.copy(isChecked = it.id == layerId)
        }
    }

    fun onLayerToggled(layerId: String?) {
        _existingLayers.value = _existingLayers.value?.map {
            val isExpanded = if (it.id == layerId) {
                !it.isExpanded
            } else {
                it.isExpanded
            }
            it.copy(isExpanded = isExpanded)
        }
    }

    fun onLayerDeleted(deletedLayerId: String?) {
        _existingLayers.value = _existingLayers.value?.filter { it.id != deletedLayerId }
    }

    fun getCheckedLayer(): String? {
        return _existingLayers.value?.find { it.isChecked }?.id
    }
}
