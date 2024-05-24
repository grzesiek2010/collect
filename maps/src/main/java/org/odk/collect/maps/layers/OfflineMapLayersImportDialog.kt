package org.odk.collect.maps.layers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.transition.Visibility
import org.odk.collect.async.Scheduler
import org.odk.collect.maps.databinding.OfflineMapLayersImportDialogBinding
import org.odk.collect.material.MaterialFullScreenDialogFragment

class OfflineMapLayersImportDialog(
    private val scheduler: Scheduler,
    private val sharedLayersDirPath: String,
    private val projectLayersDirPath: String
) : MaterialFullScreenDialogFragment() {
    private val viewModel: OfflineMapLayersImportDialogViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return OfflineMapLayersImportDialogViewModel(scheduler, requireContext().contentResolver) as T
            }
        }
    }

    private lateinit var binding: OfflineMapLayersImportDialogBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = OfflineMapLayersImportDialogBinding.inflate(inflater)

        viewModel.data.observe(this) { data ->
            binding.progressIndicator.visibility = View.GONE
            binding.layersList.visibility = View.VISIBLE

            val adapter = OfflineMapLayersImportAdapter(data)
            binding.layersList.setAdapter(adapter)
        }
        viewModel.init(requireArguments().getStringArrayList(URIS))

        binding.cancelButton.setOnClickListener {
            dismiss()
        }

        binding.addLayerButton.setOnClickListener {
            val layersDir = if (binding.allProjectsOption.isChecked) {
                sharedLayersDirPath
            } else {
                projectLayersDirPath
            }
            viewModel.addLayers(layersDir).observe(this) { isLoading ->
                if (isLoading) {
                    binding.progressIndicator.visibility = View.VISIBLE
                } else {
                    setFragmentResult(RESULT_KEY, bundleOf())
                    dismiss()
                }
            }
        }
        return binding.root
    }

    override fun onCloseClicked() {
    }

    override fun onBackPressed() {
        dismiss()
    }

    override fun getToolbar(): Toolbar {
        return binding.toolbar
    }

    companion object {
        const val URIS = "uris"
        const val RESULT_KEY = "layersAdded"
    }
}
