package com.example.sparewallet.ui.main.scanQris

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.budiyev.android.codescanner.*

class ScanViewModel : ViewModel() {
    var scanResult by mutableStateOf<String?>(null)
        private set

    lateinit var codeScanner: CodeScanner

    var currentCameraId = CodeScanner.CAMERA_BACK
        private set

    var shouldRequestPermission by mutableStateOf(false)
        private set

    fun initializeScanner(
        scannerView: CodeScannerView,
        activity: ComponentActivity
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

        if (ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            shouldRequestPermission = true
        } else {
            codeScanner.startPreview()
        }
    }

    fun onPermissionLaunched() {
        shouldRequestPermission = false
    }

    fun onPermissionResult(granted: Boolean) {
        if (granted) {
            codeScanner.startPreview()
        } else {
            Log.w("ScanViewModel", "Camera permission denied")
        }
    }

    fun switchCamera(activity: ComponentActivity) {
        try {
            val manager = activity.getSystemService(CameraManager::class.java)
            manager.cameraIdList.forEach { id ->
                val chars = manager.getCameraCharacteristics(id)
                val lens = chars.get(CameraCharacteristics.LENS_FACING)
                if (lens != null && lens != currentCameraId) {
                    currentCameraId = lens
                    codeScanner.apply {
                        releaseResources()
                        camera = currentCameraId
                        startPreview()
                    }
                    return
                }
            }
        } catch (e: CameraAccessException) {
            Log.e("ScanViewModel", "switchCamera error", e)
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (this::codeScanner.isInitialized) {
            codeScanner.releaseResources()
        }
    }
}
