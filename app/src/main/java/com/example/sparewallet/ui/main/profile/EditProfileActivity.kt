package com.example.sparewallet.ui.main.profile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.sparewallet.ui.theme.SpareWalletTheme

class EditProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            setContent {
                SpareWalletTheme {
                    EditProfileScreen(
                        onProfileUpdated = {
                            finish() // menutup activity setelah update
                        }
                    )
                }
            }
        } catch (e: Exception) {
            // Log error untuk debugging
            e.printStackTrace()
            // Tutup activity jika ada error saat setup
            finish()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EditProfileScreenPreview() {
    SpareWalletTheme {
        // Preview dengan data dummy
    }
}