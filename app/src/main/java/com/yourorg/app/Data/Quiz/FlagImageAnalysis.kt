package com.yourorg.app.Data.Quiz

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color as AndroidColor
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
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

private fun decodeAsset(context: Context, assetPath: String): Bitmap =
    context.assets.open(assetPath).use { BitmapFactory.decodeStream(it) }

/**
 * Every color in the flag that covers more than 15% of it, excluding black/white/gray — one is
 * picked at random each round so replays don't always target the same color. Falls back to the
 * single largest non-neutral color if nothing clears that bar.
 */
fun findFlagColorCandidates(context: Context, assetPath: String): List<FlagColorCandidate> {
    val bitmap = decodeAsset(context, assetPath)
    val width = bitmap.width
    val height = bitmap.height
    val pixels = IntArray(width * height)
    bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

    val bucketCounts = DoubleArray(HUE_BUCKETS)
    val bucketHueSum = DoubleArray(HUE_BUCKETS)
    val bucketSatSum = DoubleArray(HUE_BUCKETS)
    val bucketValSum = DoubleArray(HUE_BUCKETS)
    val hsv = FloatArray(3)
    val total = pixels.size.toDouble()

    for (pixel in pixels) {
        AndroidColor.colorToHSV(pixel, hsv)
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
        AndroidColor.colorToHSV(pixel, hsv)
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
fun buildFlagColorAnalysis(context: Context, assetPath: String, target: FlagColorCandidate): FlagColorAnalysis {
    val bitmap = decodeAsset(context, assetPath)
    val width = bitmap.width
    val height = bitmap.height
    val pixels = IntArray(width * height)
    bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

    val basePixels = pixels.copyOf()
    val maskPixels = IntArray(pixels.size)
    val hsv = FloatArray(3)
    for (i in pixels.indices) {
        AndroidColor.colorToHSV(pixels[i], hsv)
        val isMember = !isNeutral(hsv) && hueDistance(hsv[0], target.hue.toFloat()) <= MASK_HUE_TOLERANCE
        if (isMember) {
            basePixels[i] = basePixels[i] and 0x00FFFFFF
            maskPixels[i] = 0xFFFFFFFF.toInt()
        } else {
            maskPixels[i] = 0x00000000
        }
    }

    val baseBitmap = Bitmap.createBitmap(basePixels, width, height, Bitmap.Config.ARGB_8888)
    val maskBitmap = Bitmap.createBitmap(maskPixels, width, height, Bitmap.Config.ARGB_8888)

    return FlagColorAnalysis(
        targetHue = target.hue,
        targetSaturation = target.saturation,
        targetBrightness = target.brightness,
        originalBitmap = bitmap.asImageBitmap(),
        baseBitmap = baseBitmap.asImageBitmap(),
        maskBitmap = maskBitmap.asImageBitmap()
    )
}
