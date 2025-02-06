package eivitool.utils

import java.io.ByteArrayOutputStream
import java.nio.ShortBuffer

fun audioBuffer(audioStream : ByteArrayOutputStream): ShortBuffer {
    val audioData = audioStream.toByteArray()
    val samples = ShortArray(audioData.size / 2)
    for (i in audioData.indices step 2) {
        samples[i / 2] = ((audioData[i + 1].toInt() shl 8) or (audioData[i].toInt() and 0xFF)).toShort()
    }
    return ShortBuffer.wrap(samples)
}