package com.example.sparewallet.ui.main

import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.sparewallet.R
import com.example.sparewallet.ui.theme.SpareWalletTheme
import com.example.sparewallet.ui.main.home.HomeScreen
import com.example.sparewallet.ui.main.profile.ProfileFragment
import com.example.sparewallet.ui.main.profile.ProfileScreen
import com.example.sparewallet.ui.main.scanQris.ScanScreen
import androidx.compose.foundation.layout.size

sealed class MainNavItem(val route: String, val icon: Int, val label: String) {
    object Home : MainNavItem("home", R.drawable.ic_home, "Home")
    object Dashboard : MainNavItem("dashboard", R.drawable.ic_scan, "Scan")
    object Profile : MainNavItem("profile", R.drawable.ic_user, "Profile")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val items = listOf(
        MainNavItem.Home,
        MainNavItem.Dashboard,
        MainNavItem.Profile
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("SpareWallet", fontWeight = FontWeight.Bold)
                }
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.height(56.dp),
                tonalElevation = 4.dp
            ) {
                val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
                items.forEach { item ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                painter = painterResource(item.icon),
                                contentDescription = item.label,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = {
                            Text(
                                text = item.label,
                                fontSize = MaterialTheme.typography.labelSmall.fontSize // Smaller font
                            )
                        },
                        selected = currentRoute == item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.padding(innerPadding)
        ) {
            NavHost(navController, startDestination = MainNavItem.Home.route) {
                composable(MainNavItem.Home.route) {
                    HomeScreen()
                }
                composable(MainNavItem.Dashboard.route) {
                    ScanScreen()
                }
                composable(MainNavItem.Profile.route) {
                    ProfileScreen()
                }
            }
        }
    }
}


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        com.google.firebase.database.FirebaseDatabase.getInstance().setPersistenceEnabled(true)

        setContent {
            SpareWalletTheme {
                DisposableEffect(Unit) {
                    val mediaPlayer = MediaPlayer.create(this@MainActivity, R.raw.app_intro_sound)
                    mediaPlayer.start()
                    onDispose {
                        mediaPlayer.release()
                    }
                }
                MainScreen()
            }
        }
    }
}
