package com.example.sparewallet.ui.main.profile

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.sparewallet.ui.main.MainActivity
import com.example.sparewallet.ui.theme.SpareWalletTheme

class ChangePinActivity : ComponentActivity() {

    private val viewModel: EditProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SpareWalletTheme {
                ChangePinScreen(
                    viewModel = viewModel,
                    onPinChangedSuccessfully = {
                        // Logika navigasi setelah PIN berhasil diubah
                        val intent = Intent(this, MainActivity::class.java).apply {
                            // Membersihkan stack activity agar tidak kembali ke halaman PIN
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        startActivity(intent)
                        finish() // Tutup ChangePinActivity
                    }
                )
            }
        }
    }
}