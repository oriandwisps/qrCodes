package com.neonoxus.qrcodes

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Size
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.zxing.PlanarYUVLuminanceSource
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

@Composable
fun ScanScreen(lang: Lang) {
    val ctx = LocalContext.current
    var cameraOn by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf<String?>(null) }

    val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) cameraOn = true else message = tr(lang, "camera_perm")
    }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            cameraOn = false
            val r = QrUtils.loadBitmap(ctx, uri)?.let { QrUtils.decode(it) }
            if (r != null) { message = null; result = r } else message = tr(lang, "no_qr")
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("◳ " + tr(lang, "scan"), style = MaterialTheme.typography.headlineMedium)

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = { picker.launch("image/*") }, modifier = Modifier.weight(1f)) {
                Text(tr(lang, "upload"))
            }
            Button(
                onClick = {
                    message = null
                    if (cameraOn) {
                        cameraOn = false
                    } else if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED
                    ) {
                        cameraOn = true
                    } else {
                        permLauncher.launch(Manifest.permission.CAMERA)
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(tr(lang, if (cameraOn) "stop" else "camera"))
            }
        }

        Box(
            Modifier
                .weight(1f).fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (cameraOn) {
                CameraPreview { text ->
                    cameraOn = false
                    result = text
                }
            } else {
                Text(
                    message ?: tr(lang, "scan_hint"),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(24.dp)
                )
            }
        }
    }

    val r = result
    if (r != null) {
        AlertDialog(
            onDismissRequest = { result = null },
            title = { Text(tr(lang, "result")) },
            text = {
                SelectionContainer {
                    Text(r, Modifier.heightIn(max = 240.dp).verticalScroll(rememberScrollState()))
                }
            },
            confirmButton = {
                Row {
                    if (QrUtils.isLink(r)) {
                        TextButton(onClick = {
                            try {
                                ctx.startActivity(Intent(Intent.ACTION_VIEW, QrUtils.toUri(r)))
                            } catch (e: Exception) {
                                // no app can open it
                            }
                        }) { Text(tr(lang, "open_link")) }
                    }
                    TextButton(onClick = { result = null }) { Text(tr(lang, "close")) }
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    cm.setPrimaryClip(ClipData.newPlainText("qr", r))
                    Toast.makeText(ctx, tr(lang, "copied"), Toast.LENGTH_SHORT).show()
                }) { Text(tr(lang, "copy")) }
            }
        )
    }
}

@Composable
private fun CameraPreview(onResult: (String) -> Unit) {
    val ctx = LocalContext.current
    val owner = LocalLifecycleOwner.current
    val currentOnResult by rememberUpdatedState(onResult)
    val previewView = remember {
        PreviewView(ctx).apply { scaleType = PreviewView.ScaleType.FILL_CENTER }
    }

    DisposableEffect(owner) {
        val executor = Executors.newSingleThreadExecutor()
        val done = AtomicBoolean(false)
        val future = ProcessCameraProvider.getInstance(ctx)
        future.addListener({
            val provider = future.get()
            val preview = Preview.Builder().build()
            preview.setSurfaceProvider(previewView.surfaceProvider)
            val analysis = ImageAnalysis.Builder()
                .setTargetResolution(Size(1280, 720))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
            analysis.setAnalyzer(executor) { proxy ->
                try {
                    if (!done.get()) {
                        val plane = proxy.planes[0]
                        val buf = plane.buffer
                        val bytes = ByteArray(buf.remaining())
                        buf.get(bytes)
                        val source = PlanarYUVLuminanceSource(
                            bytes, plane.rowStride, proxy.height, 0, 0, proxy.width, proxy.height, false
                        )
                        val text = QrUtils.decodeSource(source, false)
                        if (text != null && done.compareAndSet(false, true)) {
                            previewView.post { currentOnResult(text) }
                        }
                    }
                } catch (e: Exception) {
                    // skip frame
                } finally {
                    proxy.close()
                }
            }
            provider.unbindAll()
            provider.bindToLifecycle(owner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis)
        }, ContextCompat.getMainExecutor(ctx))

        onDispose {
            done.set(true)
            if (future.isDone) {
                try { future.get().unbindAll() } catch (e: Exception) { }
            }
            executor.shutdown()
        }
    }

    AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
}
