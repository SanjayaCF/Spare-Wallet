package com.example.sparewallet.ui.main.home

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.example.sparewallet.ui.theme.SpareWalletTheme

class TopUpActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SpareWalletTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    TopUpScreen(onFinished = { finish() })
                }
            }
        }
    }
}