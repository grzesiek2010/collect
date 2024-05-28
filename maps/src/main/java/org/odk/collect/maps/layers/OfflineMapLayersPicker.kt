package org.odk.collect.maps.layers

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.odk.collect.androidshared.ui.DialogFragmentUtils
import org.odk.collect.androidshared.ui.FragmentFactoryBuilder
import org.odk.collect.androidshared.ui.GroupClickListener.addOnClickListener
import org.odk.collect.async.Scheduler
import org.odk.collect.maps.databinding.OfflineMapLayersPickerBinding
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.strings.localization.getLocalizedString
import org.odk.collect.webpage.ExternalWebPageHelper

class OfflineMapLayersPicker(
    private val referenceLayerRepository: ReferenceLayerRepository,
    private val scheduler: Scheduler,
    private val settingsProvider: SettingsProvider,
    private val externalWebPageHelper: ExternalWebPageHelper
) : BottomSheetDialogFragment(), OfflineMapLayersPickerAdapter.OfflineMapLayersAdapterInterface {
    private val viewModel: OfflineMapLayersPickerViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return OfflineMapLayersPickerViewModel(referenceLayerRepository, scheduler, settingsProvider) as T
            }
        }
    }

    private lateinit var binding: OfflineMapLayersPickerBinding

    private val getLayers = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        if (uris.isNotEmpty()) {
            val uriStrings: MutableList<String> = ArrayList()
            for (uri in uris) {
                uriStrings.add(uri.toString())
            }

            DialogFragmentUtils.showIfNotShowing(
                OfflineMapLayersImporter::class.java,
                Bundle().apply { putStringArrayList(OfflineMapLayersImporter.URIS, ArrayList<String>(uriStrings)) },
                childFragmentManager
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        childFragmentManager.fragmentFactory = FragmentFactoryBuilder()
            .forClass(OfflineMapLayersImporter::class) {
                OfflineMapLayersImporter(
                    scheduler,
                    referenceLayerRepository.getSharedLayersDirPath(),
                    referenceLayerRepository.getProjectLayersDirPath()
                )
            }
            .build()

        super.onCreate(savedInstanceState)

        childFragmentManager.setFragmentResultListener(OfflineMapLayersImporter.RESULT_KEY, this) { _, _ ->
            viewModel.loadLayers()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = OfflineMapLayersPickerBinding.inflate(inflater)

        val adapter = OfflineMapLayersPickerAdapter(this)
        binding.layers.setAdapter(adapter)

        viewModel.data.observe(this) { data ->
            if (data == null) {
                binding.progressIndicator.visibility = View.VISIBLE
                binding.layers.visibility = View.GONE
                binding.save.isEnabled = false
            } else {
                binding.progressIndicator.visibility = View.GONE
                binding.layers.visibility = View.VISIBLE
                binding.save.isEnabled = true

                adapter.setData(data)
            }
        }

        binding.mbtilesInfoGroup.addOnClickListener {
            externalWebPageHelper.openWebPageInCustomTab(
                requireActivity(),
                Uri.parse("https://docs.getodk.org/collect-offline-maps/#transferring-offline-tilesets-to-devices")
            )
        }

        binding.addLayer.setOnClickListener {
            getLayers.launch("*/*")
        }

        binding.cancel.setOnClickListener {
            dismiss()
        }

        binding.save.setOnClickListener {
            viewModel.saveCheckedLayer()
            dismiss()
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        try {
            BottomSheetBehavior.from(requireView().parent as View).apply {
                maxWidth = ViewGroup.LayoutParams.MATCH_PARENT
            }
        } catch (e: Exception) {
            // ignore
        }
    }

    override fun onLayerChecked(layerId: String?) {
        viewModel.onLayerChecked(layerId)
    }

    override fun onLayerToggled(layerId: String?) {
        viewModel.onLayerToggled(layerId)
    }

    override fun onDeleteLayer(layerItem: OfflineMapLayersPickerAdapter.ReferenceLayerItem) {
        MaterialAlertDialogBuilder(requireActivity())
            .setMessage(requireActivity().getLocalizedString(org.odk.collect.strings.R.string.delete_layer_confirmation_message, layerItem.name))
            .setPositiveButton(org.odk.collect.strings.R.string.delete_layer) { _, _ ->
                layerItem.file?.delete()
                if (layerItem.id == viewModel.getCheckedLayer()) {
                    viewModel.onLayerChecked(null)
                }
                viewModel.onLayerDeleted(layerItem.id)
            }
            .setNegativeButton(org.odk.collect.strings.R.string.cancel, null)
            .create()
            .show()
    }
}
