package eivitool.utils

import java.awt.*
import java.awt.image.BufferedImage

fun GetDisplayList(): List<Triple<Int, String, String>> {
    val screens = GraphicsEnvironment.getLocalGraphicsEnvironment().screenDevices
    return screens.mapIndexed { index, device ->
        val bounds = device.defaultConfiguration.bounds
        Triple(index, device.iDstring, "${bounds.width}x${bounds.height}")
    }
}

fun GetDisplayCapture(display: Int): BufferedImage {
    val screens = GraphicsEnvironment.getLocalGraphicsEnvironment().screenDevices
    if (display < 0 || display >= screens.size) {
        throw IllegalArgumentException("Invalid display index: $display (total ${screens.size})")
    }
    val bounds = screens[display].defaultConfiguration.bounds
    val robot = Robot(screens[display])
    val image: BufferedImage = robot.createScreenCapture(bounds)
    return image
}