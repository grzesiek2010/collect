package org.odk.collect.maps.layers

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.odk.collect.async.Scheduler
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.keys.ProjectKeys

class OfflineMapLayersPickerViewModel(
    private val referenceLayerRepository: ReferenceLayerRepository,
    private val scheduler: Scheduler,
    private val settingsProvider: SettingsProvider
) : ViewModel() {
    private val _data = MutableLiveData<List<OfflineMapLayersAdapter.ReferenceLayerItem>?>(null)
    val data: LiveData<List<OfflineMapLayersAdapter.ReferenceLayerItem>?> = _data

    init {
        loadLayers()
    }

    fun saveCheckedLayer() {
        val checkedLayerId = _data.value?.find { it.isChecked }?.id
        settingsProvider.getUnprotectedSettings().save(ProjectKeys.KEY_REFERENCE_LAYER, checkedLayerId)
    }

    fun onLayerChecked(layerId: String?) {
        _data.value = _data.value?.map {
            it.copy(isChecked = it.id == layerId)
        }
    }

    fun onLayerToggled(layerId: String?) {
        _data.value = _data.value?.map {
            val isExpanded = if (it.id == layerId) {
                !it.isExpanded
            } else {
                it.isExpanded
            }
            it.copy(isExpanded = isExpanded)
        }
    }

    fun onLayerDeleted(deletedLayerId: String?) {
        _data.value = _data.value?.filter { it.id != deletedLayerId }
    }

    fun loadLayers() {
        _data.value = null

        scheduler.immediate(
            background = {
                val layers = referenceLayerRepository.getAllSupported()
                val checkedLayerId = settingsProvider.getUnprotectedSettings().getString(ProjectKeys.KEY_REFERENCE_LAYER)

                val newData = mutableListOf(OfflineMapLayersAdapter.ReferenceLayerItem(null, null, "", checkedLayerId == null, false))
                newData.addAll(layers.map { OfflineMapLayersAdapter.ReferenceLayerItem(it.id, it.file, it.name, it.id == checkedLayerId, false) })
                _data.postValue(newData)
            },
            foreground = { }
        )
    }

    fun getCheckedLayer(): String? {
        return _data.value?.find { it.isChecked }?.id
    }
}
