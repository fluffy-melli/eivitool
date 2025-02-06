package eivitool.utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun timeStamp(): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm")
    return LocalDateTime.now().format(formatter)
}