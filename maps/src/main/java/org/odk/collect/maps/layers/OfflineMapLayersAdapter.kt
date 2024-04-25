package org.odk.collect.maps.layers

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.recyclerview.widget.RecyclerView
import org.odk.collect.maps.databinding.OfflineMapLayerBinding
import org.odk.collect.strings.localization.getLocalizedString

class OfflineMapLayersAdapter(
    private val layers: List<ReferenceLayer>,
    private val selectedLayerId: String?
) : RecyclerView.Adapter<OfflineMapLayersAdapter.ViewHolder>() {
    private var selectedLayer: ReferenceLayer? = null
    private var selectedRadioButton: RadioButton? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = OfflineMapLayerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.radio.setOnCheckedChangeListener { _, selected ->
            if (selected) {
                selectedRadioButton?.setChecked(false)
                selectedRadioButton = holder.binding.radio
                selectedLayer = if (position == 0) {
                    null
                } else {
                    layers[position - 1]
                }
            }
        }
        if (position == 0) {
            holder.binding.radio.text = holder.binding.root.context.getLocalizedString(org.odk.collect.strings.R.string.none)
            if (selectedLayerId == null) {
                holder.binding.radio.setChecked(true)
            }
        } else {
            holder.binding.radio.text = MbtilesFile.getDisplayName(layers[position - 1].file)
            if (selectedLayerId == layers[position - 1].id) {
                holder.binding.radio.setChecked(true)
            }
        }
    }

    override fun getItemCount() = layers.size + 1

    class ViewHolder(val binding: OfflineMapLayerBinding) : RecyclerView.ViewHolder(binding.root)

    fun getSelectedLayer(): ReferenceLayer? {
        return selectedLayer
    }
}
