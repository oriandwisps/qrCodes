package com.neonoxus.qrcodes

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ImageDecoder
import android.graphics.Paint
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Patterns
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.EncodeHintType
import com.google.zxing.LuminanceSource
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max

enum class ModuleShape { SQUARE, ROUNDED, CIRCLE }

enum class Ecc(val level: ErrorCorrectionLevel, val label: String) {
    L(ErrorCorrectionLevel.L, "L · 7%"),
    M(ErrorCorrectionLevel.M, "M · 15%"),
    Q(ErrorCorrectionLevel.Q, "Q · 25%"),
    H(ErrorCorrectionLevel.H, "H · 30%")
}

object QrUtils {

    fun generate(
        text: String, ecc: Ecc, sizePx: Int, marginModules: Int,
        shape: ModuleShape, fg: Int, bg: Int, transparent: Boolean
    ): Bitmap? {
        if (text.isEmpty()) return null
        val matrix = try {
            QRCodeWriter().encode(
                text, BarcodeFormat.QR_CODE, 0, 0,
                mapOf(
                    EncodeHintType.ERROR_CORRECTION to ecc.level,
                    EncodeHintType.MARGIN to 0,
                    EncodeHintType.CHARACTER_SET to "UTF-8"
                )
            )
        } catch (e: Exception) {
            return null
        }
        val n = matrix.width
        val total = n + marginModules * 2
        val cell = sizePx.toFloat() / total
        val bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        if (!transparent) canvas.drawColor(bg)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = fg }
        for (y in 0 until n) {
            for (x in 0 until n) {
                if (!matrix.get(x, y)) continue
                val l = (x + marginModules) * cell
                val t = (y + marginModules) * cell
                when (shape) {
                    ModuleShape.SQUARE ->
                        canvas.drawRect(floor(l), floor(t), ceil(l + cell), ceil(t + cell), paint)
                    ModuleShape.ROUNDED ->
                        canvas.drawRoundRect(l, t, l + cell, t + cell, cell * 0.35f, cell * 0.35f, paint)
                    ModuleShape.CIRCLE ->
                        canvas.drawCircle(l + cell / 2f, t + cell / 2f, cell / 2f, paint)
                }
            }
        }
        return bmp
    }

    fun decodeSource(source: LuminanceSource, hard: Boolean): String? {
        val hints = mapOf(
            DecodeHintType.TRY_HARDER to hard,
            DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.QR_CODE)
        )
        val reader = MultiFormatReader()
        reader.setHints(hints)
        for (s in listOf(source, source.invert())) {
            try {
                return reader.decodeWithState(BinaryBitmap(HybridBinarizer(s))).text
            } catch (e: Exception) {
                // not found, try next
            } finally {
                reader.reset()
            }
        }
        return null
    }

    fun decode(bitmap: Bitmap): String? {
        val w = bitmap.width
        val h = bitmap.height
        val pixels = IntArray(w * h)
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h)
        return decodeSource(RGBLuminanceSource(w, h, pixels), true)
    }

    fun loadBitmap(ctx: Context, uri: Uri): Bitmap? = try {
        val src = ImageDecoder.createSource(ctx.contentResolver, uri)
        ImageDecoder.decodeBitmap(src) { dec, info, _ ->
            dec.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            val m = max(info.size.width, info.size.height)
            if (m > 2000) {
                val s = 2000f / m
                dec.setTargetSize((info.size.width * s).toInt(), (info.size.height * s).toInt())
            }
        }
    } catch (e: Exception) {
        null
    }

    fun savePng(ctx: Context, bmp: Bitmap): Boolean {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "qr_${System.currentTimeMillis()}.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/qrCodes")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val resolver = ctx.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return false
        return try {
            resolver.openOutputStream(uri)?.use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
            true
        } catch (e: Exception) {
            resolver.delete(uri, null, null)
            false
        }
    }

    fun isLink(s: String): Boolean {
        val t = s.trim()
        if (t.contains(' ') || t.contains('\n')) return false
        return t.startsWith("http://", true) || t.startsWith("https://", true) ||
            Patterns.WEB_URL.matcher(t).matches()
    }

    fun toUri(s: String): Uri {
        val t = s.trim()
        return Uri.parse(if (t.startsWith("http://", true) || t.startsWith("https://", true)) t else "https://$t")
    }
}
