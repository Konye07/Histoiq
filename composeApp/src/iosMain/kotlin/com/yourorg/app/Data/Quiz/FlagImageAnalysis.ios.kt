package com.yourorg.app.Data.Quiz

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeImageBitmap
import org.jetbrains.skia.Bitmap

/**
 * PLACEHOLDER — returns a correctly-sized but blank (fully transparent) bitmap, not the real
 * recolor mask. Real pixel data needs Skia's `Bitmap.installPixels(ImageInfo, ByteArray, rowBytes)`,
 * which — unlike Android's `Bitmap.createBitmap(IntArray, ...)` — takes raw bytes, not an IntArray,
 * so it needs the exact ColorType/ColorAlphaType byte layout confirmed against a real iOS build
 * before it's trustworthy (this machine has no Mac/Xcode to verify pixel-level color output; a
 * wrong byte order or premultiplied-alpha assumption here would compile fine and just look wrong —
 * exactly the kind of mistake that wouldn't show up in CI's compile/link check). Flag Color Match
 * isn't wired into any iOS screen yet, so nothing depends on this being correct yet either — this
 * gets replaced with a real implementation, verified on-device, in the pass that covers the other
 * isolated platform-specific concerns (share, audio, notifications).
 */
actual fun imageBitmapFromArgbPixels(pixels: IntArray, width: Int, height: Int): ImageBitmap {
    val bitmap = Bitmap()
    bitmap.allocN32Pixels(width, height, false)
    return bitmap.asComposeImageBitmap()
}
