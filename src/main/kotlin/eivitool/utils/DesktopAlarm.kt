package eivitool.utils

import java.awt.*

fun SendDesktopAlarm(title: String, message: String) {
    if (SystemTray.isSupported()) {
        try {
            val tray = SystemTray.getSystemTray()
            val trayIcon = TrayIcon(Toolkit.getDefaultToolkit().createImage(""), "알림")
            trayIcon.isImageAutoSize = true
            trayIcon.toolTip = "알림"
            tray.add(trayIcon)
            trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO)
        } catch (e: Exception) {
            println("SystemTray 알림 실패: ${e.message}")
        }
    } else {
        try {
            ProcessBuilder("notify-send", title, message).start()
        } catch (e: Exception) {
            println("notify-send 알림 실패: ${e.message}")
        }
    }
}