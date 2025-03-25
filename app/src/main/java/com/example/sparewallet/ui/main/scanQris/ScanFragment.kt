package com.example.sparewallet.ui.main.scanQris

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.budiyev.android.codescanner.*
import com.example.sparewallet.databinding.FragmentScanBinding
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

class ScanFragment : Fragment() {

    private var _binding: FragmentScanBinding? = null
    private val binding get() = _binding!!

    private lateinit var codeScanner: CodeScanner
    private val scanViewModel: ScanViewModel by activityViewModels()

    private var currentCameraId = CodeScanner.CAMERA_BACK.toString()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val scannerView = binding.scannerView
        codeScanner = CodeScanner(requireContext(), scannerView)

        codeScanner.camera = CodeScanner.CAMERA_BACK
        codeScanner.formats = CodeScanner.ALL_FORMATS
        codeScanner.autoFocusMode = AutoFocusMode.SAFE
        codeScanner.scanMode = ScanMode.SINGLE
        codeScanner.isAutoFocusEnabled = true
        codeScanner.isFlashEnabled = false

        codeScanner.decodeCallback = DecodeCallback {
            requireActivity().runOnUiThread {
                scanViewModel.setScanResult(it.text)
                Toast.makeText(requireContext(), "Scan result: ${it.text}", Toast.LENGTH_LONG).show()
            }
        }
        codeScanner.errorCallback = ErrorCallback {
            requireActivity().runOnUiThread {
                Toast.makeText(
                    requireContext(), "Camera error: ${it.message}", Toast.LENGTH_LONG
                ).show()
            }
        }

        scannerView.setOnClickListener { codeScanner.startPreview() }

        binding.btnFlipCamera.setOnClickListener { switchCamera() }
        binding.btnOpenGallery.setOnClickListener { openGallery() }

        checkPermission(Manifest.permission.CAMERA)
    }

    private fun switchCamera() {
        try {
            val cameraManager = requireContext().getSystemService(CameraManager::class.java)
            val cameraIdList = cameraManager.cameraIdList
            for (cameraId in cameraIdList) {
                val characteristics = cameraManager.getCameraCharacteristics(cameraId)
                val lensFacing = characteristics.get(CameraCharacteristics.LENS_FACING)

                if (lensFacing != null) {
                    currentCameraId = if (currentCameraId == CodeScanner.CAMERA_BACK.toString()
                        && lensFacing == CameraCharacteristics.LENS_FACING_FRONT
                    ) {
                        cameraId
                    } else if (currentCameraId != CodeScanner.CAMERA_BACK.toString()
                        && lensFacing == CameraCharacteristics.LENS_FACING_BACK
                    ) {
                        cameraId
                    } else {
                        continue
                    }

                    codeScanner.releaseResources()
                    codeScanner.camera = cameraId.toInt()
                    codeScanner.startPreview()
                    break
                }
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Gagal mengubah kamera: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openGallery() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        checkPermission(permission) {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            galleryLauncher.launch(intent)
        }
    }

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null)
            {
                val uri = result.data!!.data
                try {
                    val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
                    val inputImage = InputImage.fromBitmap(bitmap, 0)
                    val scanner = BarcodeScanning.getClient()

                    scanner.process(inputImage)
                        .addOnSuccessListener { barcodes ->
                            if (barcodes.isNotEmpty()) {
                                val resultText = barcodes.first().rawValue
                                scanViewModel.setScanResult(resultText ?: "Tidak ada hasil")
                                Toast.makeText(requireContext(), "Scan result: $resultText", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(requireContext(), "Gambar tidak mengandung QR Code", Toast.LENGTH_LONG).show()
                            }
                        }
                        .addOnFailureListener { e ->
                            e.printStackTrace()
                            Toast.makeText(requireContext(), "Gagal mendekode QR Code", Toast.LENGTH_SHORT).show()
                        }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(requireContext(), "Gagal memuat gambar", Toast.LENGTH_SHORT).show()
                }
            }
        }

    private fun checkPermission(permission: String, onGranted: (() -> Unit)? = null) {
        if (ContextCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(permission), 200)
        } else {
            onGranted?.invoke()
        }
    }

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
