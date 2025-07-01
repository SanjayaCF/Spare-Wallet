package com.example.sparewallet.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sparewallet.R

@Composable
fun PinKeypad(
    onNumberClick: (String) -> Unit,
    onBackspaceClick: () -> Unit,
    onBiometricClick: (() -> Unit)? = null
) {
    val buttons = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "biometric", "0", "back")
    val lighterGrayishBlue = Color(0xFFECEFF1)

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        // Padding horizontal ditambah agar keypad tidak terlalu dekat dengan tepi layar
        modifier = Modifier.padding(horizontal = 32.dp),
        userScrollEnabled = false
    ) {
        items(buttons) { symbol ->
            when (symbol) {
                "biometric" -> {
                    if (onBiometricClick != null) {
                        TextButton(
                            onClick = { onBiometricClick() },
                            // Rasio aspek disesuaikan untuk mengecilkan tombol
                            modifier = Modifier.aspectRatio(1.3f)
                        ) {
                            Icon(
                                Icons.Default.Fingerprint,
                                contentDescription = "Use Biometric",
                                // Ukuran ikon disesuaikan
                                modifier = Modifier.size(28.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    } else {
                        Box(Modifier.aspectRatio(1.3f))
                    }
                }
                "back" -> {
                    TextButton(
                        onClick = { onBackspaceClick() },
                        modifier = Modifier.aspectRatio(1.3f)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_backspace_modern),
                            contentDescription = "Backspace",
                            modifier = Modifier.size(28.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
                else -> {
                    Button(
                        onClick = { onNumberClick(symbol) },
                        modifier = Modifier.aspectRatio(1.3f),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = lighterGrayishBlue,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        // Ukuran font disesuaikan
                        Text(text = symbol, fontSize = 24.sp)
                    }
                }
            }
        }
    }
}