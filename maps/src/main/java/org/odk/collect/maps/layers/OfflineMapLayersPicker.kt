package org.odk.collect.maps.layers

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.odk.collect.androidshared.ui.GroupClickListener.addOnClickListener
import org.odk.collect.maps.databinding.OfflineMapLayersPickerBinding
import org.odk.collect.webpage.ExternalWebPageHelper

class OfflineMapLayersPicker(
    private val viewModelFactory: ViewModelProvider.Factory,
    private val externalWebPageHelper: ExternalWebPageHelper
) : BottomSheetDialogFragment() {
    private val viewModel: OfflineMapLayersPickerViewModel by viewModels {
        viewModelFactory
    }

    private lateinit var offlineMapLayersPickerBinding: OfflineMapLayersPickerBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        offlineMapLayersPickerBinding = OfflineMapLayersPickerBinding.inflate(inflater)

        viewModel.data.observe(this) { data ->
            offlineMapLayersPickerBinding.progressIndicator.visibility = View.GONE
            offlineMapLayersPickerBinding.layers.visibility = View.VISIBLE

            val offlineMapLayersAdapter = OfflineMapLayersAdapter(data.first, data.second)
            offlineMapLayersPickerBinding.layers.setAdapter(offlineMapLayersAdapter)
        }

        offlineMapLayersPickerBinding.mbtilesInfoGroup.addOnClickListener {
            externalWebPageHelper.openWebPageInCustomTab(
                requireActivity(),
                Uri.parse("https://docs.getodk.org/collect-offline-maps/#transferring-offline-tilesets-to-devices")
            )
        }

        offlineMapLayersPickerBinding.cancel.setOnClickListener {
            dismiss()
        }

        offlineMapLayersPickerBinding.save.setOnClickListener {
            dismiss()
        }
        return offlineMapLayersPickerBinding.root
    }

    override fun onStart() {
        super.onStart()
        BottomSheetBehavior.from(requireView().parent as View).apply {
            maxWidth = ViewGroup.LayoutParams.MATCH_PARENT
        }
    }

    companion object {
        const val TAG = "OfflineMapLayersPicker"
    }
}
