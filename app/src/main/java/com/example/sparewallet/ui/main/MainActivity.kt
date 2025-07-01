package com.example.sparewallet.ui.main

import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.example.sparewallet.R
import com.example.sparewallet.ui.main.home.HomeScreen
import com.example.sparewallet.ui.main.profile.ProfileScreen
import com.example.sparewallet.ui.main.scanQris.ScanScreen
import com.example.sparewallet.ui.theme.SpareWalletTheme

sealed class MainNavItem(val route: String, val icon: Int, val label: String) {
    object Home : MainNavItem("home", R.drawable.ic_home, "Home")
    object Scan : MainNavItem("scan", R.drawable.ic_scan, "Scan")
    object Profile : MainNavItem("profile", R.drawable.ic_user, "Profile")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val items = listOf(
        MainNavItem.Home,
        MainNavItem.Scan,
        MainNavItem.Profile
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SpareWallet", fontWeight = FontWeight.Bold) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate(MainNavItem.Scan.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                shape = CircleShape,
                modifier = Modifier
                    .size(70.dp)
                    .offset(y = 60.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
            ) {
                Icon(
                    painter = painterResource(id = MainNavItem.Scan.icon),
                    contentDescription = MainNavItem.Scan.label,
                    modifier = Modifier.size(32.dp)
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                items.forEach { screen ->
                    val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    val isScanButton = screen.route == MainNavItem.Scan.route

                    NavigationBarItem(
                        selected = isSelected && !isScanButton,
                        onClick = {
                            if (!isScanButton) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        label = { Text(screen.label, fontSize = 12.sp) },
                        icon = {
                            val alpha = if (isScanButton) 0f else 1f
                            Icon(
                                painter = painterResource(id = screen.icon),
                                contentDescription = screen.label,
                                modifier = Modifier
                                    .size(26.dp)
                                    .alpha(alpha)
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(Modifier.padding(innerPadding)) {
            NavHost(navController, startDestination = MainNavItem.Home.route) {
                composable(MainNavItem.Home.route) { HomeScreen() }
                composable(MainNavItem.Scan.route) { ScanScreen() }
                composable(MainNavItem.Profile.route) { ProfileScreen() }
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