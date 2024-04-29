package org.odk.collect.maps.layers

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.odk.collect.androidshared.ui.GroupClickListener.addOnClickListener
import org.odk.collect.async.Scheduler
import org.odk.collect.maps.MapsDependencyComponentProvider
import org.odk.collect.maps.databinding.OfflineMapLayersPickerBinding
import org.odk.collect.webpage.ExternalWebPageHelper
import javax.inject.Inject

class OfflineMapLayersPicker : BottomSheetDialogFragment() {
    @Inject
    lateinit var externalWebPageHelper: ExternalWebPageHelper

    @Inject
    lateinit var referenceLayerRepository: ReferenceLayerRepository

    @Inject
    lateinit var scheduler: Scheduler

    private lateinit var offlineMapLayersPickerBinding: OfflineMapLayersPickerBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val component = (context.applicationContext as MapsDependencyComponentProvider).mapsDependencyComponent
        component.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        offlineMapLayersPickerBinding = OfflineMapLayersPickerBinding.inflate(inflater)

        scheduler.immediate(
            background = {
                referenceLayerRepository.getAll()
            },
            foreground = { layers ->
                offlineMapLayersPickerBinding.progressIndicator.visibility = View.GONE
                offlineMapLayersPickerBinding.layers.visibility = View.VISIBLE

                val offlineMapLayersAdapter = OfflineMapLayersAdapter(layers, null)
                offlineMapLayersPickerBinding.layers.setAdapter(offlineMapLayersAdapter)

                offlineMapLayersPickerBinding.cancel.setOnClickListener {
                    dismiss()
                }
                offlineMapLayersPickerBinding.save.setOnClickListener {
                    dismiss()
                }
                offlineMapLayersPickerBinding.mbtilesInfoGroup.addOnClickListener {
                    externalWebPageHelper.openWebPageInCustomTab(
                        requireActivity(),
                        Uri.parse("https://docs.getodk.org/collect-offline-maps/#transferring-offline-tilesets-to-devices")
                    )
                }
            }
        )
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
