package com.example.ui

import androidx.compose.ui.graphics.Color

data class TransparencyColor(
    val name: String,
    val color: Color,
    val hexCode: String
)

data class WallpaperPreset(
    val id: String,
    val name: String,
    val isGradient: Boolean,
    val colors: List<Color> = emptyList(),
    val desc: String = ""
)

object SettingsPresets {
    val transparencyColors = listOf(
        TransparencyColor("Corporate Indigo", Color(0x334F46E5), "#334F46E5"),
        TransparencyColor("Emerald Teal", Color(0x3300C9A7), "#3300C9A7"),
        TransparencyColor("Sapphire Blue", Color(0x331F8EFC), "#331F8EFC"),
        TransparencyColor("Crimson Red", Color(0x33FF3366), "#33FF3366"),
        TransparencyColor("Amethyst Purple", Color(0x338E44AD), "#338E44AD"),
        TransparencyColor("Amber Gold", Color(0x33F1C40F), "#33F1C40F"),
        TransparencyColor("Slate Grey", Color(0x337F8C8D), "#337F8C8D"),
        TransparencyColor("Rose Pink", Color(0x33FF85A2), "#33FF85A2"),
        TransparencyColor("Midnight Navy", Color(0x331F2A44), "#331F2A44"),
        TransparencyColor("Forest Green", Color(0x3327AE60), "#3327AE60"),
        TransparencyColor("Solar Orange", Color(0x33E67E22), "#33E67E22"),
        TransparencyColor("Electric Cyan", Color(0x3300D2D3), "#3300D2D3"),
        TransparencyColor("Pearl White", Color(0x44FFFFFF), "#44FFFFFF")
    )

    val wallpaperPresets = listOf(
        WallpaperPreset(
            id = "preset_default",
            name = "Professional Polish",
            isGradient = true,
            colors = listOf(Color(0xFFF3F4F9), Color(0xFFE2E8F0)),
            desc = "Crisp slate executive background from Design guidelines."
        ),
        WallpaperPreset(
            id = "preset_aurora",
            name = "Aurora Green",
            isGradient = true,
            colors = listOf(Color(0xFF051937), Color(0xFF004D7A), Color(0xFF008793), Color(0xFF00BF72)),
            desc = "Soft shimmering polar light gradient."
        ),
        WallpaperPreset(
            id = "preset_sunset",
            name = "Royal Dusk",
            isGradient = true,
            colors = listOf(Color(0xFF141E30), Color(0xFF243B55), Color(0xFFD43243)),
            desc = "Elegantly lit sunset shadows."
        ),
        WallpaperPreset(
            id = "preset_cyber",
            name = "Neon Cyberpunk",
            isGradient = true,
            colors = listOf(Color(0xFF2E0854), Color(0xFF1F0022), Color(0xFF002244)),
            desc = "Deep violet and midnight electric ambiance."
        ),
        WallpaperPreset(
            id = "preset_glass",
            name = "Clean Slate",
            isGradient = true,
            colors = listOf(Color(0xFFE0EAFC), Color(0xFFCFDEF3)),
            desc = "Bright pristine morning glaze."
        ),
        WallpaperPreset(
            id = "preset_minimal",
            name = "Void Black",
            isGradient = false,
            colors = listOf(Color(0xFF121212)),
            desc = "Simple solid darkness for maximum battery savings."
        )
    )
}
