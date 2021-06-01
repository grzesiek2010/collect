package org.odk.collect.android.projects

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import com.google.zxing.client.android.BeepManager
import com.google.zxing.client.android.Intents
import com.google.zxing.integration.android.IntentIntegrator
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.CaptureManager
import org.odk.collect.android.databinding.AddNewProjectDialogLayoutBinding
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.listeners.PermissionListener
import org.odk.collect.android.permissions.PermissionsProvider
import org.odk.collect.android.utilities.DialogUtils
import org.odk.collect.android.utilities.ToastUtils
import org.odk.collect.android.views.BarcodeViewDecoder
import org.odk.collect.material.MaterialFullScreenDialogFragment
import org.odk.collect.projects.AddProjectDialog
import org.odk.collect.projects.R
import javax.inject.Inject

class AddNewProjectDialog : MaterialFullScreenDialogFragment() {

    @Inject
    lateinit var barcodeViewDecoder: BarcodeViewDecoder

    @Inject
    lateinit var permissionsProvider: PermissionsProvider

    private var capture: CaptureManager? = null

    private lateinit var beepManager: BeepManager
    private lateinit var binding: AddNewProjectDialogLayoutBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)
        DaggerUtils.getComponent(context).inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = AddNewProjectDialogLayoutBinding.inflate(inflater)
        setUpToolbar()
        binding.configureManuallyButton.setOnClickListener {
            DialogUtils.showIfNotShowing(AddProjectDialog::class.java, requireActivity().supportFragmentManager)
            dismiss()
        }
        binding.cancelButton.setOnClickListener {
            dismiss()
        }
        beepManager = BeepManager(requireActivity())
        permissionsProvider.requestCameraPermission(
            requireActivity(),
            object : PermissionListener {
                override fun granted() {
                    startScanning(savedInstanceState)
                }

                override fun denied() {
                    dismiss()
                }
            }
        )
        return binding.root
    }

    override fun onCloseClicked() {
    }

    override fun onBackPressed() {
        dismiss()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        capture?.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onPause() {
        super.onPause()
        binding.barcodeView.pauseAndWait()
        capture?.onPause()
    }

    override fun onResume() {
        super.onResume()
        binding.barcodeView.resume()
        capture?.onResume()
    }

    override fun onDestroy() {
        capture?.onDestroy()
        super.onDestroy()
    }

    override fun getToolbar(): Toolbar {
        return binding.toolbar
    }

    private fun setUpToolbar() {
        toolbar.setTitle(R.string.add_project)
        toolbar.navigationIcon = null
    }

    private fun startScanning(savedInstanceState: Bundle?) {
        val intent = IntentIntegrator(requireActivity())
            .setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
            .setPrompt("")
            .createScanIntent().apply {
                putExtra(Intents.Scan.SCAN_TYPE, Intents.Scan.MIXED_SCAN)
            }

        capture = CaptureManager(requireActivity(), binding.barcodeView)
        capture!!.initializeFromIntent(intent, savedInstanceState)
        capture!!.decode()

        barcodeViewDecoder.waitForBarcode(binding.barcodeView).observe(
            viewLifecycleOwner,
            { barcodeResult: BarcodeResult ->
                beepManager.playBeepSoundAndVibrate()
                try {
                    handleScanningResult(barcodeResult)
                } catch (e: Exception) {
                    ToastUtils.showShortToast(getString(R.string.invalid_qrcode))
                }
            }
        )
    }

    private fun handleScanningResult(result: BarcodeResult) {
    }
}
