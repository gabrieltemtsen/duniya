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

enum class ThemeMode {
    PAPER,
    OBSIDIAN,
    ETHEREUM_SILVER
}

private val DuniyaEthereumSilverScheme = darkColorScheme(
    primary = EthereumSilverPalette.vermilion,
    onPrimary = Color.White,
    primaryContainer = EthereumSilverPalette.vermilionSoft,
    onPrimaryContainer = EthereumSilverPalette.inkPrimary,
    secondary = EthereumSilverPalette.verdigris,
    onSecondary = Color.Black,
    secondaryContainer = EthereumSilverPalette.verdigrisSoft,
    onSecondaryContainer = EthereumSilverPalette.inkPrimary,
    tertiary = EthereumSilverPalette.brass,
    background = EthereumSilverPalette.pageBg,
    onBackground = EthereumSilverPalette.inkPrimary,
    surface = EthereumSilverPalette.sheetSurface,
    onSurface = EthereumSilverPalette.inkPrimary,
    surfaceVariant = EthereumSilverPalette.recessedWell,
    onSurfaceVariant = EthereumSilverPalette.inkSecondary,
    outline = EthereumSilverPalette.ruleLine
)

val LocalDuniyaThemeToggle = staticCompositionLocalOf<() -> Unit> { {} }

@Composable
fun DuniyaTheme(
    initialDarkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    var themeMode by remember { 
        mutableStateOf(if (initialDarkTheme) ThemeMode.OBSIDIAN else ThemeMode.PAPER) 
    }
    val palette = when (themeMode) {
        ThemeMode.PAPER -> ArchivalPaperPalette
        ThemeMode.OBSIDIAN -> ObsidianNightPalette
        ThemeMode.ETHEREUM_SILVER -> EthereumSilverPalette
    }
    val colorScheme = when (themeMode) {
        ThemeMode.PAPER -> DuniyaPaperLightScheme
        ThemeMode.OBSIDIAN -> DuniyaObsidianDarkScheme
        ThemeMode.ETHEREUM_SILVER -> DuniyaEthereumSilverScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val context = view.context
            if (context is Activity) {
                val window = context.window
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !palette.isDark
                controller.isAppearanceLightNavigationBars = !palette.isDark
            }
        }
    }

    CompositionLocalProvider(
        LocalDuniyaPalette provides palette,
        LocalDuniyaThemeToggle provides {
            themeMode = when (themeMode) {
                ThemeMode.PAPER -> ThemeMode.OBSIDIAN
                ThemeMode.OBSIDIAN -> ThemeMode.ETHEREUM_SILVER
                ThemeMode.ETHEREUM_SILVER -> ThemeMode.PAPER
            }
        }
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
