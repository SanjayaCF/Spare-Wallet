package com.example.sparewallet.ui.auth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.sparewallet.ui.theme.SpareWalletTheme
import com.example.sparewallet.ui.theme.SpareWalletTypography


class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SpareWalletTheme { LoginScreen() }
        }
    }
}