package com.example.duniya.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class DuniyaPalette(
    val isDark: Boolean,
    val pageBg: Color,
    val sheetSurface: Color,
    val recessedWell: Color,
    val elevatedCard: Color,
    val inkPrimary: Color,
    val inkSecondary: Color,
    val inkMuted: Color,
    val vermilion: Color,
    val vermilionSoft: Color,
    val verdigris: Color,
    val verdigrisSoft: Color,
    val brass: Color,
    val brassSoft: Color,
    val crimson: Color,
    val crimsonSoft: Color,
    val ruleLine: Color,
    val hardwareChassis: Color,
    val hardwareWell: Color,
    val hardwareInk: Color,
    val hardwareMuted: Color,
    val hardwareRule: Color
)

val ArchivalPaperPalette = DuniyaPalette(
    isDark = false,
    pageBg = Color(0xFFF4EFE6),           // Warm archival cotton paper
    sheetSurface = Color(0xFFFCF9F2),     // Crisp monograph page sheet
    recessedWell = Color(0xFFE9E2D4),     // Tactile recessed paper well
    elevatedCard = Color(0xFFFAF6EC),     // Elevated index card
    inkPrimary = Color(0xFF171614),       // Deep sumi charcoal ink
    inkSecondary = Color(0xFF4E4A42),     // Warm graphite prose ink
    inkMuted = Color(0xFF7D776B),         // Archival catalog index muted
    vermilion = Color(0xFFC84628),        // Teenage Engineering / Braun vermilion terracotta
    vermilionSoft = Color(0xFFF9E6E0),    // Soft terracotta wash
    verdigris = Color(0xFF1B6349),        // Deep botanical forest green
    verdigrisSoft = Color(0xFFE1F0EA),    // Soft verdigris tint
    brass = Color(0xFFB56A12),            // Warm brass ochre
    brassSoft = Color(0xFFF7ECD9),        // Soft ochre tint
    crimson = Color(0xFF9E2A2B),          // Editorial diagnostic crimson
    crimsonSoft = Color(0xFFF9E5E5),      // Soft crimson wash
    ruleLine = Color(0xFFD8D0C0),         // Crisp 1dp archival rule line
    hardwareChassis = Color(0xFF1B1A17),  // Matte anodized carbon silicon instrument
    hardwareWell = Color(0xFF12110F),     // Recessed silicon die well
    hardwareInk = Color(0xFFF3EFE6),      // Warm phosphor white
    hardwareMuted = Color(0xFF9A9488),    // Anodized silkscreen label
    hardwareRule = Color(0xFF2E2C27)      // Hardware chassis seam
)

val ObsidianNightPalette = DuniyaPalette(
    isDark = true,
    pageBg = Color(0xFF11100E),           // Warm carbon obsidian
    sheetSurface = Color(0xFF1A1916),     // Tactile keycap dark surface
    recessedWell = Color(0xFF0C0B0A),     // Deep OLED well
    elevatedCard = Color(0xFF22201C),     // Elevated carbon card
    inkPrimary = Color(0xFFF2EDE4),       // Warm parchment ink
    inkSecondary = Color(0xFFB5AFA2),     // Warm ash subtext
    inkMuted = Color(0xFF7A7468),         // Muted catalog label
    vermilion = Color(0xFFE55B3C),        // Luminous vermilion LED
    vermilionSoft = Color(0xFF331B16),    // Dark vermilion well
    verdigris = Color(0xFF3CA680),        // Phosphor sage verdigris
    verdigrisSoft = Color(0xFF132B22),    // Dark verdigris well
    brass = Color(0xFFE59F38),            // Amber phosphor
    brassSoft = Color(0xFF2E2312),        // Dark amber well
    crimson = Color(0xFFE05252),          // Diagnostic red
    crimsonSoft = Color(0xFF2E1517),      // Dark crimson well
    ruleLine = Color(0xFF2B2924),         // Warm carbon hairline
    hardwareChassis = Color(0xFF171613),  // Anodized silicon module
    hardwareWell = Color(0xFF0E0D0B),     // Recessed silicon die well
    hardwareInk = Color(0xFFF3EFE6),      // Warm phosphor white
    hardwareMuted = Color(0xFF969084),    // Silkscreen label
    hardwareRule = Color(0xFF2B2924)      // Hardware seam
)

val LocalDuniyaPalette = staticCompositionLocalOf { ArchivalPaperPalette }
