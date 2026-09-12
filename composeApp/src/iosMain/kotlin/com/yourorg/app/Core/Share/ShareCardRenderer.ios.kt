package com.yourorg.app.Core.Share

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import platform.CoreGraphics.CGPointMake
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSAttributedString
import platform.Foundation.NSData
import platform.Foundation.create
import platform.UIKit.NSFontAttributeName
import platform.UIKit.NSForegroundColorAttributeName
import platform.UIKit.drawAtPoint
import platform.UIKit.size
import platform.UIKit.UIColor
import platform.UIKit.UIFont
import platform.UIKit.UIGraphicsBeginImageContextWithOptions
import platform.UIKit.UIGraphicsEndImageContext
import platform.UIKit.UIGraphicsGetImageFromCurrentImageContext
import platform.UIKit.UIImagePNGRepresentation
import platform.UIKit.UIRectFill
import platform.posix.memcpy

private const val CARD_SIZE = 1080.0

/**
 * Uses the older UIGraphicsBeginImageContextWithOptions imperative API rather than the newer
 * block-based UIGraphicsImageRenderer — both draw into a bitmap context the same way, but this one
 * avoids needing raw CGContext pointer manipulation for anything beyond a solid background fill.
 * Text is drawn via NSAttributedString's own size()/drawAtPoint() instance methods (not the
 * NSString(UIKit) category extensions, which Kotlin/Native doesn't surface as callable directly on
 * a Kotlin String) — keeps this a smaller surface of uncertain Kotlin/Native bindings for a file
 * that can only be verified by real iOS CI (see the migration notes).
 */
@OptIn(ExperimentalForeignApi::class)
actual object ShareCardRenderer {
    actual fun renderPng(
        appName: String,
        roleName: String,
        gameTitle: String,
        scoreLabel: String,
        scoreText: String,
        rankingText: String,
        accentColorArgb: Int,
        footerText: String
    ): ByteArray {
        val size = CGSizeMake(CARD_SIZE, CARD_SIZE)
        UIGraphicsBeginImageContextWithOptions(size, true, 1.0)
        try {
            argbToUiColor(accentColorArgb).setFill()
            UIRectFill(CGRectMake(0.0, 0.0, CARD_SIZE, CARD_SIZE))

            val white = UIColor.whiteColor
            val translucentWhite = UIColor(white = 1.0, alpha = 200.0 / 255.0)
            val maxTextWidth = CARD_SIZE - 160.0

            drawFitted(appName.uppercase(), 130.0, 32.0, bold = true, color = translucentWhite, maxWidth = maxTextWidth)
            drawFitted(roleName, 250.0, 64.0, bold = true, color = white, maxWidth = maxTextWidth)
            drawFitted(gameTitle, 320.0, 38.0, bold = false, color = translucentWhite, maxWidth = maxTextWidth)
            drawFitted(scoreLabel.uppercase(), 480.0, 30.0, bold = true, color = translucentWhite, maxWidth = maxTextWidth)
            drawFitted(scoreText, 630.0, 150.0, bold = true, color = white, maxWidth = maxTextWidth)
            drawFitted(rankingText, 720.0, 36.0, bold = false, color = white, maxWidth = maxTextWidth)
            drawFitted(footerText, 940.0, 30.0, bold = false, color = translucentWhite, maxWidth = maxTextWidth)

            val image = UIGraphicsGetImageFromCurrentImageContext() ?: return ByteArray(0)
            val pngData = UIImagePNGRepresentation(image) ?: return ByteArray(0)
            return pngData.toByteArray()
        } finally {
            UIGraphicsEndImageContext()
        }
    }

    /** Draws one horizontally-centered line at [centerY], shrinking the font until it fits [maxWidth]. */
    private fun drawFitted(text: String, centerY: Double, initialSize: Double, bold: Boolean, color: UIColor, maxWidth: Double) {
        var fontSize = initialSize
        var attributed = NSAttributedString.create(string = text, attributes = attrsFor(fontSize, bold, color))
        var textSize = attributed.size()
        while (textSize.useContents { width } > maxWidth && fontSize > 16.0) {
            fontSize -= 2.0
            attributed = NSAttributedString.create(string = text, attributes = attrsFor(fontSize, bold, color))
            textSize = attributed.size()
        }
        val width = textSize.useContents { width }
        val height = textSize.useContents { height }
        attributed.drawAtPoint(CGPointMake(CARD_SIZE / 2.0 - width / 2.0, centerY - height / 2.0))
    }

    private fun attrsFor(size: Double, bold: Boolean, color: UIColor): Map<Any?, *> =
        mapOf(NSFontAttributeName to fontFor(size, bold), NSForegroundColorAttributeName to color)

    private fun fontFor(size: Double, bold: Boolean): UIFont =
        if (bold) UIFont.boldSystemFontOfSize(size) else UIFont.systemFontOfSize(size)

    private fun argbToUiColor(argb: Int): UIColor {
        val a = ((argb shr 24) and 0xFF) / 255.0
        val r = ((argb shr 16) and 0xFF) / 255.0
        val g = ((argb shr 8) and 0xFF) / 255.0
        val b = (argb and 0xFF) / 255.0
        return UIColor(red = r, green = g, blue = b, alpha = a)
    }

    private fun NSData.toByteArray(): ByteArray {
        val len = length.toInt()
        val result = ByteArray(len)
        if (len > 0) {
            result.usePinned { pinned ->
                memcpy(pinned.addressOf(0), bytes, len.toULong())
            }
        }
        return result
    }
}
