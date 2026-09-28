package com.example.duniya.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DuniyaPaperLightScheme = lightColorScheme(
    primary = ArchivalPaperPalette.vermilion,
    onPrimary = Color.White,
    primaryContainer = ArchivalPaperPalette.vermilionSoft,
    onPrimaryContainer = ArchivalPaperPalette.inkPrimary,
    secondary = ArchivalPaperPalette.verdigris,
    onSecondary = Color.White,
    secondaryContainer = ArchivalPaperPalette.verdigrisSoft,
    onSecondaryContainer = ArchivalPaperPalette.inkPrimary,
    tertiary = ArchivalPaperPalette.brass,
    background = ArchivalPaperPalette.pageBg,
    onBackground = ArchivalPaperPalette.inkPrimary,
    surface = ArchivalPaperPalette.sheetSurface,
    onSurface = ArchivalPaperPalette.inkPrimary,
    surfaceVariant = ArchivalPaperPalette.recessedWell,
    onSurfaceVariant = ArchivalPaperPalette.inkSecondary,
    outline = ArchivalPaperPalette.ruleLine
)

private val DuniyaObsidianDarkScheme = darkColorScheme(
    primary = ObsidianNightPalette.vermilion,
    onPrimary = Color.White,
    primaryContainer = ObsidianNightPalette.vermilionSoft,
    onPrimaryContainer = ObsidianNightPalette.inkPrimary,
    secondary = ObsidianNightPalette.verdigris,
    onSecondary = Color.Black,
    secondaryContainer = ObsidianNightPalette.verdigrisSoft,
    onSecondaryContainer = ObsidianNightPalette.inkPrimary,
    tertiary = ObsidianNightPalette.brass,
    background = ObsidianNightPalette.pageBg,
    onBackground = ObsidianNightPalette.inkPrimary,
    surface = ObsidianNightPalette.sheetSurface,
    onSurface = ObsidianNightPalette.inkPrimary,
    surfaceVariant = ObsidianNightPalette.recessedWell,
    onSurfaceVariant = ObsidianNightPalette.inkSecondary,
    outline = ObsidianNightPalette.ruleLine
)

val LocalDuniyaThemeToggle = staticCompositionLocalOf<() -> Unit> { {} }

@Composable
fun DuniyaTheme(
    initialDarkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    var isDark by remember { mutableStateOf(initialDarkTheme) }
    val palette = if (isDark) ObsidianNightPalette else ArchivalPaperPalette
    val colorScheme = if (isDark) DuniyaObsidianDarkScheme else DuniyaPaperLightScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val context = view.context
            if (context is Activity) {
                val window = context.window
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !isDark
                controller.isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    CompositionLocalProvider(
        LocalDuniyaPalette provides palette,
        LocalDuniyaThemeToggle provides { isDark = !isDark }
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
