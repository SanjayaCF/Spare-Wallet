package com.example.sparewallet.ui.main.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sparewallet.R
import com.example.sparewallet.common.ComingSoonActivity
import com.example.sparewallet.ui.history.HistoryActivity
import com.example.sparewallet.ui.transfer.TransferActivity

@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel = viewModel()
) {
    val balance by homeViewModel.balance.observeAsState("0")
    val accountNumber by homeViewModel.accountNumber.observeAsState("")
    val name by homeViewModel.name.observeAsState("")
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        homeViewModel.refreshData()
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ElevatedCard(
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_user),
                            contentDescription = "Profile Icon",
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onPrimary),
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Text(
                                text = "Account Number: ",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Text(
                                text = accountNumber,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.clickable {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Account Number", accountNumber)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Account number copied", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }

                    Divider(
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )

                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "My Wallet",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            val amount = balance.toDoubleOrNull() ?: 0.0
                            val formattedBalance = "%,.0f".format(amount)
                            Text(
                                text = "Rp. $formattedBalance",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }

            ElevatedCard(
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val menuItems = listOf(
                        Triple(R.drawable.ic_top_up, "Top Up") {
                            context.startActivity(Intent(context, TopUpActivity::class.java))
                        },
                        Triple(R.drawable.ic_transfer, "Transfer") {
                            context.startActivity(Intent(context, TransferActivity::class.java))
                        },
                        Triple(R.drawable.ic_cart, "Cart") {
                            context.startActivity(
                                Intent(context, ComingSoonActivity::class.java)
                                    .apply { putExtra("menuName", "Cart") }
                            )
                        },
                        Triple(R.drawable.ic_debit, "Debit") {
                            context.startActivity(
                                Intent(context, ComingSoonActivity::class.java)
                                    .apply { putExtra("menuName", "Debit Card") }
                            )
                        },
                        Triple(R.drawable.ic_flazz, "Flazz") {
                            context.startActivity(
                                Intent(context, ComingSoonActivity::class.java)
                                    .apply { putExtra("menuName", "Flazz") }
                            )
                        },
                        Triple(R.drawable.ic_history, "History") {
                            context.startActivity(Intent(context, HistoryActivity::class.java))
                        }
                    )

                    menuItems.chunked(3).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            rowItems.forEach { (iconRes, title, action) ->
                                Column(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    IconButton(
                                        onClick = action,
                                        modifier = Modifier.size(64.dp),
                                        colors = IconButtonDefaults.iconButtonColors(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary
                                        )
                                    ) {
                                        Icon(
                                            painter = painterResource(id = iconRes),
                                            contentDescription = title,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.labelMedium,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}