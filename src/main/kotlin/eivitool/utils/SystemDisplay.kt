package eivitool.utils

import java.awt.*
import java.awt.image.BufferedImage


fun getDisplayList(): List<Triple<Int, String, String>> {
    val screens = GraphicsEnvironment.getLocalGraphicsEnvironment().screenDevices
    return screens.mapIndexed { index, device ->
        val bounds = device.defaultConfiguration.bounds
        Triple(index, device.iDstring, "${bounds.width}x${bounds.height}")
    }
}
