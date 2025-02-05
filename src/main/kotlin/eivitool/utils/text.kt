package eivitool.utils

import java.awt.Color
import java.awt.Font
import java.awt.RenderingHints
import java.awt.image.BufferedImage

fun DrawTextOnImage(image: BufferedImage, text: String, x: Int, y: Int): BufferedImage {
    val newImage = BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_ARGB)
    val g2d = newImage.createGraphics()
    g2d.drawImage(image, 0, 0, null)
    g2d.font = Font("Arial", Font.BOLD, 12)
    g2d.color = Color.YELLOW
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    g2d.drawString(text, x, y)
    g2d.dispose()
    return newImage
}

fun DrawTextRightOnImage(image: BufferedImage, text: String, x: Int, y: Int): BufferedImage {
    val newImage = BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_ARGB)
    val g2d = newImage.createGraphics()
    g2d.drawImage(image, 0, 0, null)
    g2d.font = Font("Arial", Font.BOLD, 12)
    g2d.color = Color.YELLOW
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    val textWidth = g2d.fontMetrics.stringWidth(text)
    val finalX = image.width - textWidth - x
    g2d.drawString(text, finalX, y)
    g2d.dispose()
    return newImage
}

fun TruncatePath(path: String, maxLength: Int = 7): String {
    return if (path.length > maxLength) {
        "...${path.takeLast(maxLength)}"
    } else {
        path
    }
}
