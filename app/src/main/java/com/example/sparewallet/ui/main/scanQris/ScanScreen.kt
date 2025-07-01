package com.example.sparewallet.ui.main.scanQris

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.budiyev.android.codescanner.CodeScannerView
import com.example.sparewallet.R

@Composable
fun ScanScreen(
    scanViewModel: ScanViewModel = viewModel()
) {
    val context = LocalContext.current as ComponentActivity

    // Box to layer scanner preview and overlays
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // 1) permission launcher in UI
        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { granted ->
            scanViewModel.onPermissionResult(granted)
        }

        // 2) initialize scanner view once
        AndroidView(
            factory = { ctx ->
                CodeScannerView(ctx).also { scannerView ->
                    scanViewModel.initializeScanner(scannerView, context)
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .clickable { scanViewModel.codeScanner.startPreview() }
        )

        // 3) trigger permission request when VM asks
        if (scanViewModel.shouldRequestPermission) {
            LaunchedEffect(Unit) {
                permissionLauncher.launch(Manifest.permission.CAMERA)
                scanViewModel.onPermissionLaunched()
            }
        }

        // overlay scanned result
        scanViewModel.scanResult?.let { result ->
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
                    .background(Color.White.copy(alpha = 0.7f), CircleShape)
            ) {
                Text(
                    text = result,
                    color = Color.Black,
                    modifier = Modifier.padding(12.dp),
                    fontSize = 16.sp
                )
            }
        }

        // camera switch button
        Box(
            modifier = Modifier
                .size(48.dp)
                .align(Alignment.BottomStart)
                .padding(16.dp)
                .background(Color.White.copy(alpha = 0.2f), CircleShape)
                .clickable { scanViewModel.switchCamera(context) },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.cameraswitch_icon),
                contentDescription = "Flip Camera",
                tint = Color.White
            )
        }

        // gallery picker launcher
        val galleryLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data ?: return@rememberLauncherForActivityResult
                // handle image picking here
            }
        }
        Box(
            modifier = Modifier
                .size(48.dp)
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .background(Color.White.copy(alpha = 0.2f), CircleShape)
                .clickable {
                    galleryLauncher.launch(
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.gallery_icon),
                contentDescription = "Open Gallery",
                tint = Color.White
            )
        }
    }
}
