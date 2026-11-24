package com.reelreminder.app.ui.theme

import androidx.compose.ui.graphics.Color

// Light mode colors from prototype / AGENTS.md:
// Light: gradient #F7FCFC to #E4F4F2, ink #082A29, muted #58787A, glass = white 55% + blur 26dp + 1dp white edge + 1dp inner top highlight.
// Soft blurred aqua (#5EEAD4) and sky (#7DD3FC) blobs behind content.
val LightB1 = Color(0xFFF7FCFC)
val LightB2 = Color(0xFFE4F4F2)
val LightInk = Color(0xFF082A29)
val LightMute = Color(0xFF58787A)
val LightGlass = Color(0x8CFFFFFF) // 55% white
val LightEdge = Color(0xD9FFFFFF)  // 85% white
val LightHi = Color(0xF2FFFFFF)    // 95% white
val LightChip = Color(0x0F082A29)

// Dark mode colors:
// Dark mode: #061615 to #0A2523, ink #E8F8F6, glass white 7%.
val DarkB1 = Color(0xFF061615)
val DarkB2 = Color(0xFF0A2523)
val DarkInk = Color(0xFFE8F8F6)
val DarkMute = Color(0xFF86A9A7)
val DarkGlass = Color(0x12FFFFFF) // 7% white
val DarkEdge = Color(0x24FFFFFF)
val DarkHi = Color(0x29FFFFFF)
val DarkChip = Color(0x14FFFFFF)

// Accent: #14B8A6 to #0A7F86 gradient (+ button, Save, Play-in-list), glow rgba(34,211,238,.4).
// Red #FF3B30 ONLY for the "due soon" dot. No other hues in the UI chrome.
val TealPrimary = Color(0xFF14B8A6)
val TealDark = Color(0xFF0A7F86)
val AquaGlow = Color(0x6622D3EE)
val RedDot = Color(0xFFFF3B30)

// Blobs behind content:
val AquaBlob = Color(0x805EEAD4)
val SkyBlob = Color(0x667DD3FC)

// Category poster gradients:
// Morning #0891B2 to #155E75, Motivation #0D9488 to #134E4A, Evening #2563EB to #1E3A8A, Study #059669 to #064E3B.
val MorningGradient = listOf(Color(0xFF0891B2), Color(0xFF155E75))
val MotivationGradient = listOf(Color(0xFF0D9488), Color(0xFF134E4A))
val EveningGradient = listOf(Color(0xFF2563EB), Color(0xFF1E3A8A))
val StudyGradient = listOf(Color(0xFF059669), Color(0xFF064E3B))

fun parseColorPair(start: String, end: String): List<Color> {
    return try {
        listOf(
            Color(android.graphics.Color.parseColor(start)),
            Color(android.graphics.Color.parseColor(end))
        )
    } catch (_: Exception) {
        MotivationGradient
    }
}
