package org.odk.collect.maps.layers

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.odk.collect.maps.MapsDependencyComponentProvider
import org.odk.collect.maps.databinding.OfflineMapLayersPickerBinding
import org.odk.collect.maps.databinding.OfflineMapLayersPickerBottomButtonsBinding
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.keys.ProjectKeys
import org.odk.collect.shared.TempFiles
import javax.inject.Inject

class OfflineMapLayersPicker : BottomSheetDialogFragment() {

    @Inject
    lateinit var referenceLayerRepository: ReferenceLayerRepository

    @Inject
    lateinit var settingsProvider: SettingsProvider

    private lateinit var offlineMapLayersPickerBinding: OfflineMapLayersPickerBinding
    private lateinit var bottomButtonsBinding: OfflineMapLayersPickerBottomButtonsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        offlineMapLayersPickerBinding = OfflineMapLayersPickerBinding.inflate(inflater)
        bottomButtonsBinding = OfflineMapLayersPickerBottomButtonsBinding.inflate(inflater)

        val selectedLayerId = settingsProvider.getUnprotectedSettings().getString(ProjectKeys.KEY_REFERENCE_LAYER)
        val layers = listOf(
            ReferenceLayer("1", TempFiles.createTempFile()),
            ReferenceLayer("2", TempFiles.createTempFile()),
            ReferenceLayer("3", TempFiles.createTempFile()),
            ReferenceLayer("4", TempFiles.createTempFile()),
            ReferenceLayer("5", TempFiles.createTempFile()),
            ReferenceLayer("6", TempFiles.createTempFile()),
            ReferenceLayer("7", TempFiles.createTempFile()),
            ReferenceLayer("8", TempFiles.createTempFile()),
            ReferenceLayer("9", TempFiles.createTempFile()),
            ReferenceLayer("10", TempFiles.createTempFile()),
            ReferenceLayer("11", TempFiles.createTempFile()),
            ReferenceLayer("12", TempFiles.createTempFile()),
            ReferenceLayer("13", TempFiles.createTempFile("blah0", ".mbtiles")),
            ReferenceLayer("14", TempFiles.createTempFile("blah1", ".mbtiles")),
            ReferenceLayer("15", TempFiles.createTempFile("blah2", ".mbtiles"))
        )
        val offlineMapLayersAdapter = OfflineMapLayersAdapter(layers, selectedLayerId)
        offlineMapLayersPickerBinding.layers.setAdapter(offlineMapLayersAdapter)

        bottomButtonsBinding.cancel.setOnClickListener {
            dismiss()
        }
        bottomButtonsBinding.save.setOnClickListener {
            val selectedLayer = offlineMapLayersAdapter.getSelectedLayer()
            if (selectedLayer != null) {
                settingsProvider.getUnprotectedSettings().save(ProjectKeys.KEY_REFERENCE_LAYER, selectedLayer.id)
            } else {
                settingsProvider.getUnprotectedSettings().save(ProjectKeys.KEY_REFERENCE_LAYER, null)
            }
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

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val component = (context.applicationContext as MapsDependencyComponentProvider).mapsDependencyComponent
        component.inject(this)
    }

    // Add bottom sticky buttons: https://github.com/lixw1021/Learning-Lib/blob/master/app/src/main/java/com/xianwei/learninglib/lib_android/bottomsheet/CustomBottomSheet.kt
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheetDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        bottomSheetDialog.setOnShowListener {
            // You can find the bottom sheet parent view and ids: https://github.com/lixw1021/material-components-android/blob/master/lib/java/com/google/android/material/bottomsheet/res/layout/design_bottom_sheet_dialog.xml
            val coordinator = (it as BottomSheetDialog)
                .findViewById<CoordinatorLayout>(com.google.android.material.R.id.coordinator)
            val containerLayout =
                it.findViewById<FrameLayout>(com.google.android.material.R.id.container)

            bottomButtonsBinding.root.layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.BOTTOM
            }
            containerLayout!!.addView(bottomButtonsBinding.root)

            // Dynamically update bottom sheet containerLayout bottom margin to buttons view height
            bottomButtonsBinding.root.post {
                (coordinator!!.layoutParams as ViewGroup.MarginLayoutParams).apply {
                    bottomButtonsBinding.root.measure(
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                    )
                    this.bottomMargin = bottomButtonsBinding.root.measuredHeight
                    containerLayout.requestLayout()
                }
            }
        }
        return bottomSheetDialog
    }

    companion object {
        const val TAG = "OfflineMapLayersPicker"
    }
}
