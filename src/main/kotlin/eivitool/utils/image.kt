package eivitool.utils

import java.awt.*
import java.awt.image.BufferedImage

fun PaddingImage(original: BufferedImage, targetWidth: Int, targetHeight: Int): BufferedImage {
    val aspectRatio = original.width.toDouble() / original.height.toDouble()
    val scaledHeight = targetHeight
    val scaledWidth = (targetHeight * aspectRatio).toInt()
    val finalWidth: Int
    val finalHeight: Int
    if (scaledWidth > targetWidth) {
        finalWidth = targetWidth
        finalHeight = (targetWidth / aspectRatio).toInt()
    } else {
        finalWidth = scaledWidth
        finalHeight = scaledHeight
    }
    val resizedImage = BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB)
    val g2d: Graphics2D = resizedImage.createGraphics()
    g2d.color = Color.BLACK
    g2d.fillRect(0, 0, targetWidth, targetHeight)
    val x = (targetWidth - finalWidth) / 2
    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)
    g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
    g2d.drawImage(original, x, 0, finalWidth, finalHeight, null)
    g2d.dispose()
    return resizedImage
}

fun ResizeImageFast(img: BufferedImage, w: Int, h: Int): BufferedImage {
    val resized = BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR)
    val g2d = resized.createGraphics()
    g2d.drawImage(img, 0, 0, w, h, null)
    g2d.dispose()
    return resized
}