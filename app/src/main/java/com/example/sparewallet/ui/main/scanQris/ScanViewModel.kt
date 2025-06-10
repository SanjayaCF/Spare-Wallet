package com.example.sparewallet.ui.main.scanQris

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.budiyev.android.codescanner.*

class ScanViewModel : ViewModel() {
    var scanResult by mutableStateOf<String?>(null)
        private set
    lateinit var codeScanner: CodeScanner
    var currentCameraId: Int = CodeScanner.CAMERA_BACK
        private set

    fun initializeScanner(
        scannerView: CodeScannerView,
        activity: ComponentActivity,
        permissionLauncher: ActivityResultLauncher<String>
    ) {
        codeScanner = CodeScanner(activity, scannerView).apply {
            camera = currentCameraId
            formats = CodeScanner.ALL_FORMATS
            autoFocusMode = AutoFocusMode.SAFE
            scanMode = ScanMode.SINGLE
            isAutoFocusEnabled = true
            isFlashEnabled = false
            decodeCallback = DecodeCallback {
                scanResult = it.text
            }
            errorCallback = ErrorCallback {
                Log.e("ScanViewModel", "Camera error: ${it.message}")
            }
        }
        checkPermission(activity, permissionLauncher)
    }

    fun switchCamera(activity: ComponentActivity) {
        try {
            val manager = activity.getSystemService(CameraManager::class.java)
            manager.cameraIdList.forEach { id ->
                val chars = manager.getCameraCharacteristics(id)
                val lens = chars.get(CameraCharacteristics.LENS_FACING)
                if (lens != null && lens != currentCameraId) {
                    currentCameraId = lens
                    codeScanner.releaseResources()
                    codeScanner.camera = currentCameraId
                    codeScanner.startPreview()
                    return
                }
            }
        } catch (e: CameraAccessException) {
            Log.e("ScanViewModel", "switchCamera error", e)
        }
    }

    private fun checkPermission(
        activity: ComponentActivity,
        permissionLauncher: ActivityResultLauncher<String>
    ) {
        val perm = Manifest.permission.CAMERA
        if (ContextCompat.checkSelfPermission(activity, perm) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(perm)
        } else {
            codeScanner.startPreview()
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (this::codeScanner.isInitialized) {
            codeScanner.releaseResources()
        }
    }
}