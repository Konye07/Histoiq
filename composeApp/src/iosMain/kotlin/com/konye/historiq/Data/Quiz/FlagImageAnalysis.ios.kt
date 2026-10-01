package com.konye.historiq.Data.Quiz

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeImageBitmap
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.ImageInfo

/**
 * Explicit RGBA_8888 byte layout (not Skia's native/N32 type, whose byte order is
 * platform-dependent) so the mapping from [pixels]'s `0xAARRGGBB` ints — Android's
 * `Bitmap.createBitmap(IntArray, ...)` convention, see the actual on that platform — is unambiguous
 * regardless of endianness. UNPREMUL matches Android's own `Bitmap.createBitmap(IntArray, ...)`,
 * which treats the array as straight (not premultiplied) color ints. Verified against the real
 * Skiko 0.144.6 klib (installPixels/ImageInfo/ColorType/ColorAlphaType all confirmed via
 * `klib dump-metadata`, per this project's own technique for checking iOS-adjacent library APIs
 * without a Mac) rather than guessed from general Skia API familiarity.
 */
actual fun imageBitmapFromArgbPixels(pixels: IntArray, width: Int, height: Int): ImageBitmap {
    val bytes = ByteArray(pixels.size * 4)
    for (i in pixels.indices) {
        val p = pixels[i]
        val o = i * 4
        bytes[o] = ((p shr 16) and 0xFF).toByte()
        bytes[o + 1] = ((p shr 8) and 0xFF).toByte()
        bytes[o + 2] = (p and 0xFF).toByte()
        bytes[o + 3] = ((p shr 24) and 0xFF).toByte()
    }
    val bitmap = Bitmap()
    bitmap.installPixels(ImageInfo(width, height, ColorType.RGBA_8888, ColorAlphaType.UNPREMUL), bytes, width * 4)
    bitmap.setImmutable()
    return bitmap.asComposeImageBitmap()
}
