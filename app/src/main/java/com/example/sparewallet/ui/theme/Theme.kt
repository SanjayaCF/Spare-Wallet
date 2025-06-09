package com.example.sparewallet.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary            = md_theme_light_primary,
    onPrimary          = md_theme_light_onPrimary,
    primaryContainer   = md_theme_light_primaryContainer,
    secondary          = md_theme_light_secondary,
    onSecondary        = md_theme_light_onSecondary,
    secondaryContainer = md_theme_light_secondaryContainer,
    background         = Color.White,
    surface            = Color.White,
    onBackground       = Color.Black,
    onSurface          = Color.Black
)

@Composable
fun SpareWalletTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = SpareWalletTypography,
        shapes = MaterialTheme.shapes,
        content = content
    )
}
