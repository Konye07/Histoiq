package com.yourorg.app.Data.Quiz

import androidx.compose.ui.graphics.ImageBitmap
import kotlin.math.abs
import kotlin.math.min

data class FlagColorCandidate(
    val hue: Int,
    val saturation: Int,
    val brightness: Int
)

data class FlagColorAnalysis(
    val targetHue: Int,
    val targetSaturation: Int,
    val targetBrightness: Int,
    val originalBitmap: ImageBitmap,
    val baseBitmap: ImageBitmap,
    val maskBitmap: ImageBitmap
)

private const val MIN_SHARE = 0.15
private const val HUE_BUCKETS = 24
private const val HUE_BUCKET_WIDTH = 360f / HUE_BUCKETS
private const val MASK_HUE_TOLERANCE = 25f

/**
 * Builds a new [ImageBitmap] from a raw ARGB pixel buffer (`0xAARRGGBB` per int, Android's
 * `Bitmap.createBitmap(IntArray, ...)` convention). Compose Multiplatform's common `ImageBitmap`
 * API can *read* pixels (see [readArgbPixels] below) but has no common way to *construct* one from
 * a pixel buffer — confirmed via JetBrains/compose-multiplatform#108 — so this needs a real
 * per-platform bitmap type on each side.
 */
expect fun imageBitmapFromArgbPixels(pixels: IntArray, width: Int, height: Int): ImageBitmap

/** Excludes near-black, near-white and gray pixels — never a valid target color. */
private fun isNeutral(hsv: FloatArray): Boolean {
    val s = hsv[1]
    val v = hsv[2]
    if (v < 0.12f) return true
    if (v > 0.92f && s < 0.15f) return true
    if (s < 0.08f) return true
    return false
}

private fun hueDistance(a: Float, b: Float): Float {
    val d = abs(a - b) % 360f
    return min(d, 360f - d)
}

/** Bucket index with the seam placed at 180° instead of 0°, since 0°/360° (red) is the most common flag color. */
private fun hueBucket(hue: Float): Int {
    val shifted = (hue + HUE_BUCKET_WIDTH / 2f) % 360f
    return (shifted / HUE_BUCKET_WIDTH).toInt().coerceIn(0, HUE_BUCKETS - 1)
}

/**
 * android.graphics.Color.colorToHSV's portable replacement (same conventions: hue 0-360,
 * saturation/value 0-1) — hand-written since Compose Multiplatform's common graphics API has no
 * pixel-to-HSV conversion of its own.
 */
private fun argbToHsv(argb: Int, out: FloatArray) {
    val r = (argb shr 16 and 0xFF) / 255f
    val g = (argb shr 8 and 0xFF) / 255f
    val b = (argb and 0xFF) / 255f
    val max = maxOf(r, g, b)
    val min = minOf(r, g, b)
    val delta = max - min

    val hue = when {
        delta == 0f -> 0f
        max == r -> 60f * (((g - b) / delta) % 6f)
        max == g -> 60f * (((b - r) / delta) + 2f)
        else -> 60f * (((r - g) / delta) + 4f)
    }
    out[0] = if (hue < 0f) hue + 360f else hue
    out[1] = if (max == 0f) 0f else delta / max
    out[2] = max
}

private fun readArgbPixels(bitmap: ImageBitmap): IntArray {
    val pixels = IntArray(bitmap.width * bitmap.height)
    bitmap.readPixels(pixels)
    return pixels
}

/**
 * Every color in the flag that covers more than 15% of it, excluding black/white/gray — one is
 * picked at random each round so replays don't always target the same color. Falls back to the
 * single largest non-neutral color if nothing clears that bar. Takes an already-decoded
 * [originalBitmap] (via [tryLoadImageBitmap]) rather than a path, so a caller building both
 * candidates and a [FlagColorAnalysis] for the same flag only decodes it once.
 */
fun findFlagColorCandidates(originalBitmap: ImageBitmap): List<FlagColorCandidate> {
    val pixels = readArgbPixels(originalBitmap)

    val bucketCounts = DoubleArray(HUE_BUCKETS)
    val bucketHueSum = DoubleArray(HUE_BUCKETS)
    val bucketSatSum = DoubleArray(HUE_BUCKETS)
    val bucketValSum = DoubleArray(HUE_BUCKETS)
    val hsv = FloatArray(3)
    val total = pixels.size.toDouble()

    for (pixel in pixels) {
        argbToHsv(pixel, hsv)
        if (isNeutral(hsv)) continue
        val bucket = hueBucket(hsv[0])
        bucketCounts[bucket] += 1.0
        bucketHueSum[bucket] += hsv[0].toDouble()
        bucketSatSum[bucket] += hsv[1].toDouble()
        bucketValSum[bucket] += hsv[2].toDouble()
    }

    fun candidateFor(bucket: Int): FlagColorCandidate {
        val count = bucketCounts[bucket]
        val hue = if (count > 0) bucketHueSum[bucket] / count else 0.0
        val sat = if (count > 0) bucketSatSum[bucket] / count else 0.5
        val value = if (count > 0) bucketValSum[bucket] / count else 0.5
        return FlagColorCandidate(
            hue = hue.toInt().coerceIn(0, 359),
            saturation = (sat * 100).toInt().coerceIn(0, 100),
            brightness = (value * 100).toInt().coerceIn(0, 100)
        )
    }

    val qualifying = (0 until HUE_BUCKETS).filter { bucketCounts[it] / total > MIN_SHARE }
    val chosenBuckets = qualifying.ifEmpty {
        listOfNotNull((0 until HUE_BUCKETS).filter { bucketCounts[it] > 0 }.maxByOrNull { bucketCounts[it] })
    }
    if (chosenBuckets.isNotEmpty()) return chosenBuckets.map { candidateFor(it) }

    // Every pixel was neutral (e.g. a black-and-white flag) — fall back to the image's overall
    // average color so callers can always rely on at least one candidate being returned.
    var sumHue = 0.0
    var sumSat = 0.0
    var sumVal = 0.0
    for (pixel in pixels) {
        argbToHsv(pixel, hsv)
        sumHue += hsv[0]
        sumSat += hsv[1]
        sumVal += hsv[2]
    }
    return listOf(
        FlagColorCandidate(
            hue = (sumHue / total).toInt().coerceIn(0, 359),
            saturation = ((sumSat / total) * 100).toInt().coerceIn(0, 100),
            brightness = ((sumVal / total) * 100).toInt().coerceIn(0, 100)
        )
    )
}

/** Builds the original image plus a recolor mask for one already-chosen target color. */
fun buildFlagColorAnalysis(originalBitmap: ImageBitmap, target: FlagColorCandidate): FlagColorAnalysis {
    val pixels = readArgbPixels(originalBitmap)
    val width = originalBitmap.width
    val height = originalBitmap.height

    val basePixels = pixels.copyOf()
    val maskPixels = IntArray(pixels.size)
    val hsv = FloatArray(3)
    for (i in pixels.indices) {
        argbToHsv(pixels[i], hsv)
        val isMember = !isNeutral(hsv) && hueDistance(hsv[0], target.hue.toFloat()) <= MASK_HUE_TOLERANCE
        if (isMember) {
            basePixels[i] = basePixels[i] and 0x00FFFFFF
            maskPixels[i] = 0xFFFFFFFF.toInt()
        } else {
            maskPixels[i] = 0x00000000
        }
    }

    return FlagColorAnalysis(
        targetHue = target.hue,
        targetSaturation = target.saturation,
        targetBrightness = target.brightness,
        originalBitmap = originalBitmap,
        baseBitmap = imageBitmapFromArgbPixels(basePixels, width, height),
        maskBitmap = imageBitmapFromArgbPixels(maskPixels, width, height)
    )
}
