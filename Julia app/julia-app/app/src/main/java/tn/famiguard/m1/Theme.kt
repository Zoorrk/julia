package tn.famiguard.m1

import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

/** Palette FamiGuard — sombre, sobre, "produit". */
object Brand {
    val teal = Color(0xFF34D3B7)
    val tealInk = Color(0xFF03201C)
    val bg = Color(0xFF0D1320)
    val surface = Color(0xFF151D2E)
    val surface2 = Color(0xFF1E2840)
    val ink = Color(0xFFEAF0F8)
    val muted = Color(0xFF8E9BB2)
    val line = Color(0xFF28334C)
    val amber = Color(0xFFF2B33D)
    val amberBg = Color(0xFF2E2512)
    val red = Color(0xFFF37165)
    val redBg = Color(0xFF34201E)
    val green = Color(0xFF49DC86)
    val greenBg = Color(0xFF12301F)
}

val FamiColors = darkColorScheme(
    primary = Brand.teal,
    onPrimary = Brand.tealInk,
    primaryContainer = Brand.surface2,
    onPrimaryContainer = Brand.ink,
    secondaryContainer = Brand.surface2,
    onSecondaryContainer = Brand.ink,
    background = Brand.bg,
    onBackground = Brand.ink,
    surface = Brand.surface,
    onSurface = Brand.ink,
    surfaceVariant = Brand.surface2,
    onSurfaceVariant = Brand.muted,
    outline = Brand.line,
    error = Brand.red,
    errorContainer = Brand.redBg,
    onErrorContainer = Color(0xFFFFD9D4),
)
