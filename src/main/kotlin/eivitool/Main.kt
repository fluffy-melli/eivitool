package eivitool

import eivitool.ui.App
import javax.swing.SwingUtilities

fun main() {
    SwingUtilities.invokeLater {
        App().isVisible = true
    }
}