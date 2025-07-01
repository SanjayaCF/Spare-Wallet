package com.example.sparewallet.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.sparewallet.R

@Composable
fun AuthScreenLayout(
    // Tambahkan parameter untuk judul layar agar lebih dinamis
    screenTitle: String,
    content: @Composable ColumnScope.() -> Unit
) {
    // Column utama yang mengisi seluruh layar
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary) // Warna dasar adalah warna primer
    ) {
        // Bagian 1: Header Biru (Tidak bisa di-scroll)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground_white),
                contentDescription = "App Logo",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(100.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = screenTitle,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White
            )
        }

        // Bagian 2: Konten Putih (Bisa di-scroll)
        Column(
            modifier = Modifier
                .fillMaxSize() // Mengisi sisa ruang
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Spacer untuk memberi ruang di bagian atas form
            Spacer(modifier = Modifier.height(16.dp))
            // Menampilkan konten dari Login atau Register
            content()
        }
    }
}