package com.neonoxus.qrcodes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(settings: AppSettings) {
    val lang = settings.lang
    val dark = settings.theme == ThemeMode.DARK

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("⚙ " + tr(lang, "settings"), style = MaterialTheme.typography.headlineMedium)

        SectionTitle(tr(lang, "theme"))
        ChipRow(
            ThemeMode.values().toList(), settings.theme,
            { tr(lang, it.name.lowercase()) }, { settings.updateTheme(it) }
        )

        SectionTitle(tr(lang, "accent"))
        ChipRow(
            Accent.values().toList(), settings.accent,
            { tr(lang, it.key) }, { settings.updateAccent(it) },
            leading = { a ->
                Box(
                    Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(if (dark) a.dark else a.light)
                )
            }
        )

        SectionTitle(tr(lang, "language"))
        ChipRow(Lang.values().toList(), settings.lang, { it.label }, { settings.updateLang(it) })
    }
}
