package com.neonoxus.qrcodes

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import java.util.Locale

enum class ThemeMode { DARK, LIGHT }

enum class Accent(val dark: Color, val light: Color, val key: String) {
    DEFAULT(Color.White, Color(0xFF1C1B1F), "default"),
    RED(Color(0xFFFF6B6B), Color(0xFFB3261E), "red"),
    YELLOW(Color(0xFFFFD54F), Color(0xFF8A6D00), "yellow"),
    GREEN(Color(0xFF81C784), Color(0xFF2E7D32), "green"),
    BLUE(Color(0xFF64B5F6), Color(0xFF1565C0), "blue"),
    PURPLE(Color(0xFFCE93D8), Color(0xFF6A1B9A), "purple")
}

class AppSettings(context: Context) {
    private val sp = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    var theme by mutableStateOf(ThemeMode.valueOf(sp.getString("theme", ThemeMode.DARK.name)!!))
        private set
    var accent by mutableStateOf(Accent.valueOf(sp.getString("accent", Accent.DEFAULT.name)!!))
        private set
    var lang by mutableStateOf(
        Lang.valueOf(
            sp.getString("lang", if (Locale.getDefault().language == "ru") Lang.RU.name else Lang.EN.name)!!
        )
    )
        private set

    fun updateTheme(v: ThemeMode) { theme = v; sp.edit().putString("theme", v.name).apply() }
    fun updateAccent(v: Accent) { accent = v; sp.edit().putString("accent", v.name).apply() }
    fun updateLang(v: Lang) { lang = v; sp.edit().putString("lang", v.name).apply() }
}

@Composable
fun QrCodesTheme(mode: ThemeMode, accent: Accent, content: @Composable () -> Unit) {
    val dark = mode == ThemeMode.DARK
    val a = if (dark) accent.dark else accent.light
    val onA = if (a.luminance() > 0.5f) Color.Black else Color.White
    val base = if (dark) Color(0xFF141416) else Color(0xFFFBFBFD)
    val onBase = if (dark) Color(0xFFE6E6EA) else Color(0xFF1B1B1F)
    val onVariant = if (dark) Color(0xFFC5C5CC) else Color(0xFF45454B)
    val outline = if (dark) Color(0xFF8E8E96) else Color(0xFF76767E)

    val scheme = if (dark) {
        darkColorScheme(
            primary = a, onPrimary = onA,
            primaryContainer = lerp(base, a, 0.28f), onPrimaryContainer = onBase,
            secondary = a, onSecondary = onA,
            secondaryContainer = lerp(base, a, 0.22f), onSecondaryContainer = onBase,
            background = base, onBackground = onBase,
            surface = base, onSurface = onBase,
            surfaceVariant = lerp(base, a, 0.10f), onSurfaceVariant = onVariant,
            surfaceTint = a, outline = outline
        )
    } else {
        lightColorScheme(
            primary = a, onPrimary = onA,
            primaryContainer = lerp(base, a, 0.18f), onPrimaryContainer = onBase,
            secondary = a, onSecondary = onA,
            secondaryContainer = lerp(base, a, 0.16f), onSecondaryContainer = onBase,
            background = base, onBackground = onBase,
            surface = base, onSurface = onBase,
            surfaceVariant = lerp(base, a, 0.08f), onSurfaceVariant = onVariant,
            surfaceTint = a, outline = outline
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = scheme.background.toArgb()
            window.navigationBarColor = scheme.surfaceVariant.toArgb()
            val c = WindowCompat.getInsetsController(window, view)
            c.isAppearanceLightStatusBars = !dark
            c.isAppearanceLightNavigationBars = !dark
        }
    }

    MaterialTheme(colorScheme = scheme, content = content)
}
