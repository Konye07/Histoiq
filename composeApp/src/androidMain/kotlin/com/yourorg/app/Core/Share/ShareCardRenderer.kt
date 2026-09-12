package com.yourorg.app.Core.Share

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.text.TextPaint

private const val CARD_SIZE = 1080

/**
 * Draws a square, purpose-made "share card" (app name, role, score, ranking) rather than
 * screenshotting the live UI — a raw capture would include the status bar/back button/nav bar,
 * and different games' result layouts vary in height, so a dedicated render is both simpler and
 * more consistent to share.
 *
 * Android-only for now (not an expect/actual): this uses android.graphics.Canvas/Bitmap directly.
 * Porting it to common code via Compose Multiplatform's own Canvas/ImageBitmap + Skia's PNG encode
 * is a real possibility (both are confirmed viable APIs) but needs verifying on an actual iOS
 * build before trusting it, alongside the other isolated platform-specific concerns.
 */
object ShareCardRenderer {
    fun render(
        appName: String,
        roleName: String,
        gameTitle: String,
        scoreLabel: String,
        scoreText: String,
        rankingText: String,
        accentColorArgb: Int,
        footerText: String
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(CARD_SIZE, CARD_SIZE, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(accentColorArgb)

        val centerX = CARD_SIZE / 2f
        val white = android.graphics.Color.WHITE
        val translucentWhite = android.graphics.Color.argb(200, 255, 255, 255)

        fun textPaint(sizePx: Float, bold: Boolean, color: Int = white): TextPaint =
            TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = sizePx
                this.color = color
                textAlign = Paint.Align.CENTER
                typeface = Typeface.create(Typeface.SANS_SERIF, if (bold) Typeface.BOLD else Typeface.NORMAL)
            }

        val maxTextWidth = (CARD_SIZE - 160).toFloat()

        /** Draws one centered line, shrinking the paint's text size until it fits maxTextWidth — always
         * a single canvas-native drawText call (Paint.Align.CENTER), so there's no StaticLayout-vs-manual-
         * translate width mismatch that could clip text off the edge for a long/translated string. */
        fun drawFitted(text: String, y: Float, paint: TextPaint): Float {
            var size = paint.textSize
            while (paint.measureText(text) > maxTextWidth && size > 16f) {
                size -= 2f
                paint.textSize = size
            }
            canvas.drawText(text, centerX, y, paint)
            return size
        }

        drawFitted(appName.uppercase(), 130f, textPaint(32f, bold = true, color = translucentWhite).apply { letterSpacing = 0.12f })

        drawFitted(roleName, 250f, textPaint(64f, bold = true))
        drawFitted(gameTitle, 320f, textPaint(38f, bold = false, color = translucentWhite))

        drawFitted(scoreLabel.uppercase(), 480f, textPaint(30f, bold = true, color = translucentWhite).apply { letterSpacing = 0.1f })
        drawFitted(scoreText, 630f, textPaint(150f, bold = true))

        drawFitted(rankingText, 720f, textPaint(36f, bold = false))

        drawFitted(footerText, 940f, textPaint(30f, bold = false, color = translucentWhite))

        return bitmap
    }
}
