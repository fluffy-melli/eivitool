package eivitool.utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun timeStamp(): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm")
    return LocalDateTime.now().format(formatter)
}

fun timeFormat(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val remainingSeconds = seconds % 60
    return String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds)
}