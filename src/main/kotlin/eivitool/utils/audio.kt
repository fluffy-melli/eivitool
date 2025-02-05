package eivitool.utils

import org.bytedeco.javacv.Frame
import java.io.ByteArrayInputStream
import java.nio.ByteBuffer
import javax.sound.sampled.*

fun GetTargetDataLine(selectedDevice: Mixer.Info): Pair<TargetDataLine?, AudioFormat?> {
    val mixer = AudioSystem.getMixer(selectedDevice)
    val possibleFormats = listOf(
        AudioFormat(44100.0f, 16, 1, true, false),
        AudioFormat(48000.0f, 16, 1, true, false),
        AudioFormat(22050.0f, 16, 1, true, false)
    )
    for (format in possibleFormats) {
        val info = DataLine.Info(TargetDataLine::class.java, format)
        if (mixer.isLineSupported(info)) {
            val line = mixer.getLine(info) as TargetDataLine
            line.open(format)
            return line to format
        } else {
            continue
        }
    }
    return null to null
}

fun AudioFrame(audioData: ByteArray, audioFormat: AudioFormat): Frame {
    val byteArrayInputStream = ByteArrayInputStream(audioData)
    val audioInputStream = AudioInputStream(byteArrayInputStream, audioFormat, audioData.size.toLong())
    val sampleSizeInBits = audioFormat.sampleSizeInBits
    val sampleRate = audioFormat.sampleRate
    val channels = audioFormat.channels
    val frame = Frame()
    val byteBuffer = ByteBuffer.wrap(audioData)
    frame.samples = arrayOf(byteBuffer)
    return frame
}