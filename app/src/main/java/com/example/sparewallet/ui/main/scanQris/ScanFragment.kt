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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.budiyev.android.codescanner.*
import com.example.sparewallet.R
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

class ScanFragment : Fragment() {

    private lateinit var codeScanner: CodeScanner
    private val scanViewModel: ScanViewModel by activityViewModels()
    private var currentCameraId = CodeScanner.CAMERA_BACK.toString()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                ScanQrisScreen(
                    onFlipCamera = { switchCamera() },
                    onOpenGallery = { openGallery() },
                    onInitializeScanner = { scannerView -> initializeScanner(scannerView) }
                )
            }
        }
    }

    @Composable
    fun ScanQrisScreen(
        onFlipCamera: () -> Unit,
        onOpenGallery: () -> Unit,
        onInitializeScanner: (CodeScannerView) -> Unit
    ) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(Color.White)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "Scan Qris",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                // Scanner View
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    AndroidView(
                        factory = { context ->
                            CodeScannerView(context).apply {
                                onInitializeScanner(this)
                            }
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable {
                                if (::codeScanner.isInitialized) {
                                    codeScanner.startPreview()
                                }
                            }
                    )
                }
            }

            // Floating Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp, start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Flip Camera Button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = Color.White.copy(alpha = 0.1f),
                            shape = CircleShape
                        )
                        .clickable { onFlipCamera() }
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.cameraswitch_icon),
                        contentDescription = "Flip Camera",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Open Gallery Button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = Color.White.copy(alpha = 0.1f),
                            shape = CircleShape
                        )
                        .clickable { onOpenGallery() }
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.gallery_icon),
                        contentDescription = "Open Gallery",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }

    private fun initializeScanner(scannerView: CodeScannerView) {
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
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val uri = result.data!!.data
                try {
                    val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        val source = android.graphics.ImageDecoder.createSource(requireContext().contentResolver, uri!!)
                        android.graphics.ImageDecoder.decodeBitmap(source)
                    } else {
                        @Suppress("DEPRECATION")
                        MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
                    }

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

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                pendingPermissionCallback?.invoke()
                pendingPermissionCallback = null
            } else {
                Toast.makeText(requireContext(), "Permission required", Toast.LENGTH_SHORT).show()
            }
        }

    private var pendingPermissionCallback: (() -> Unit)? = null

    private fun checkPermission(permission: String, onGranted: (() -> Unit)? = null) {
        if (ContextCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED) {
            pendingPermissionCallback = onGranted
            permissionLauncher.launch(permission)
        } else {
            onGranted?.invoke()
        }
    }

    override fun onResume() {
        super.onResume()
        if (::codeScanner.isInitialized) {
            codeScanner.startPreview()
        }
    }

    override fun onPause() {
        if (::codeScanner.isInitialized) {
            codeScanner.releaseResources()
        }
        super.onPause()
    }
}