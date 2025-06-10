package com.example.sparewallet.ui.main.scanQris

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.sparewallet.ui.theme.SpareWalletTheme

class ScanActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SpareWalletTheme {
                ScanScreen()
            }
        }
    }
}