package org.odk.collect.maps.layers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import org.odk.collect.maps.databinding.OfflineMapLayersPickerItemBinding
import org.odk.collect.strings.localization.getLocalizedString
import java.io.File

class OfflineMapLayersPickerAdapter(
    private val listener: OfflineMapLayersAdapterInterface
) : RecyclerView.Adapter<OfflineMapLayersPickerAdapter.ViewHolder>() {
    interface OfflineMapLayersAdapterInterface {
        fun onLayerChecked(layerId: String?)

        fun onLayerToggled(layerId: String?)

        fun onDeleteLayer(layerItem: ReferenceLayerItem)
    }

    private val diffUtil = object : DiffUtil.ItemCallback<ReferenceLayerItem>() {
        override fun areItemsTheSame(oldItem: ReferenceLayerItem, newItem: ReferenceLayerItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ReferenceLayerItem, newItem: ReferenceLayerItem): Boolean {
            return oldItem == newItem
        }
    }
    private val asyncListDiffer = AsyncListDiffer(this, diffUtil)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = OfflineMapLayersPickerItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val layer = asyncListDiffer.currentList[position]

        holder.binding.radioButton.setChecked(layer.isChecked)

        if (layer.id == null) {
            holder.binding.title.text = holder.binding.root.context.getLocalizedString(org.odk.collect.strings.R.string.none)
            holder.binding.arrow.visibility = View.GONE
        } else {
            holder.binding.title.text = layer.name
            holder.binding.path.text = layer.file?.absolutePath
            holder.binding.arrow.visibility = View.VISIBLE
        }

        if (layer.isExpanded) {
            holder.binding.arrow.setImageDrawable(ContextCompat.getDrawable(holder.binding.root.context, org.odk.collect.icons.R.drawable.ic_baseline_expand_less_24))
            holder.binding.path.visibility = View.VISIBLE
            holder.binding.deleteLayer.visibility = View.VISIBLE
        } else {
            holder.binding.arrow.setImageDrawable(ContextCompat.getDrawable(holder.binding.root.context, org.odk.collect.icons.R.drawable.ic_baseline_expand_more_24))
            holder.binding.path.visibility = View.GONE
            holder.binding.deleteLayer.visibility = View.GONE
        }

        holder.binding.radioButton.setOnClickListener {
            checkLayer(layer)
        }
        holder.binding.title.setOnClickListener {
            checkLayer(layer)
        }
        holder.binding.path.setOnClickListener {
            checkLayer(layer)
        }

        holder.binding.arrow.setOnClickListener {
            listener.onLayerToggled(layer.id)
        }

        holder.binding.deleteLayer.setOnClickListener {
            listener.onDeleteLayer(layer)
        }
    }

    private fun checkLayer(layer: ReferenceLayerItem) {
        if (!layer.isChecked) {
            listener.onLayerChecked(layer.id)
        }
    }

    override fun getItemCount() = asyncListDiffer.currentList.size

    fun setData(layers: List<ReferenceLayerItem>) {
        asyncListDiffer.submitList(layers)
    }

    class ViewHolder(val binding: OfflineMapLayersPickerItemBinding) : RecyclerView.ViewHolder(binding.root)

    data class ReferenceLayerItem(
        val id: String?,
        val file: File?,
        val name: String,
        val isChecked: Boolean,
        val isExpanded: Boolean
    )
}
