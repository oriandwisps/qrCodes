package com.neonoxus.qrcodes

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel

class CreateViewModel : ViewModel() {
    var text by mutableStateOf("")
    var ecc by mutableStateOf(Ecc.M)
    var size by mutableStateOf(512f)
    var margin by mutableStateOf(2f)
    var shape by mutableStateOf(ModuleShape.SQUARE)
    var fg by mutableStateOf(0xFF000000.toInt())
    var bg by mutableStateOf(0xFFFFFFFF.toInt())
    var transparent by mutableStateOf(false)
}

private val palette = listOf(
    0xFF000000.toInt(), 0xFFFFFFFF.toInt(), 0xFFD32F2F.toInt(), 0xFFF9A825.toInt(),
    0xFF2E7D32.toInt(), 0xFF1565C0.toInt(), 0xFF6A1B9A.toInt(), 0xFF00838F.toInt()
)

@Composable
fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> ChipRow(
    items: List<T>, selected: T, label: (T) -> String, onSelect: (T) -> Unit,
    leading: (@Composable (T) -> Unit)? = null
) {
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { item ->
            if (leading == null) {
                FilterChip(selected = item == selected, onClick = { onSelect(item) }, label = { Text(label(item)) })
            } else {
                FilterChip(
                    selected = item == selected, onClick = { onSelect(item) },
                    label = { Text(label(item)) },
                    leadingIcon = { leading(item) }
                )
            }
        }
    }
}

@Composable
private fun Swatches(selected: Int, onSelect: (Int) -> Unit) {
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        palette.forEach { c ->
            val sel = c == selected
            Box(
                Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(c))
                    .border(
                        if (sel) 3.dp else 1.dp,
                        if (sel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        CircleShape
                    )
                    .clickable { onSelect(c) }
            )
        }
    }
}

@Composable
fun CreateScreen(vm: CreateViewModel, lang: Lang) {
    val ctx = LocalContext.current
    val bmp = remember(vm.text, vm.ecc, vm.size, vm.margin, vm.shape, vm.fg, vm.bg, vm.transparent) {
        QrUtils.generate(
            vm.text, vm.ecc, vm.size.toInt(), vm.margin.toInt(),
            vm.shape, vm.fg, vm.bg, vm.transparent
        )
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("▦ " + tr(lang, "create"), style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = vm.text, onValueChange = { vm.text = it },
            label = { Text(tr(lang, "data_hint")) },
            modifier = Modifier.fillMaxWidth(), minLines = 2, maxLines = 6
        )

        SectionTitle(tr(lang, "preview"))
        Box(
            Modifier
                .fillMaxWidth().aspectRatio(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(if (vm.transparent) Color(0xFF808080) else MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (bmp != null) {
                Image(bitmap = bmp.asImageBitmap(), contentDescription = null, modifier = Modifier.fillMaxSize())
            } else {
                Text(
                    tr(lang, if (vm.text.isEmpty()) "empty_hint" else "too_big"),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(24.dp)
                )
            }
        }

        Button(
            onClick = {
                val b = bmp
                if (b != null) {
                    val ok = QrUtils.savePng(ctx, b)
                    Toast.makeText(ctx, tr(lang, if (ok) "saved" else "save_failed"), Toast.LENGTH_SHORT).show()
                }
            },
            enabled = bmp != null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.Download, contentDescription = null)
            Text(tr(lang, "download"), modifier = Modifier.padding(start = 8.dp))
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionTitle(tr(lang, "ecc"))
                ChipRow(Ecc.values().toList(), vm.ecc, { it.label }, { vm.ecc = it })

                SectionTitle(tr(lang, "size") + ": ${vm.size.toInt()} px")
                Slider(value = vm.size, onValueChange = { vm.size = it }, valueRange = 256f..1024f, steps = 5)

                SectionTitle(tr(lang, "margin") + ": ${vm.margin.toInt()}")
                Slider(value = vm.margin, onValueChange = { vm.margin = it }, valueRange = 0f..8f, steps = 7)

                SectionTitle(tr(lang, "shape"))
                ChipRow(
                    ModuleShape.values().toList(), vm.shape,
                    { tr(lang, it.name.lowercase()) }, { vm.shape = it }
                )

                SectionTitle(tr(lang, "fg"))
                Swatches(vm.fg) { vm.fg = it }

                SectionTitle(tr(lang, "bg"))
                Swatches(vm.bg) { vm.bg = it }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(tr(lang, "transparent"), Modifier.weight(1f))
                    Switch(checked = vm.transparent, onCheckedChange = { vm.transparent = it })
                }
            }
        }
    }
}
