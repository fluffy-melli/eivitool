package eivitool.utils

import javax.sound.sampled.*

fun GetAudioDataLine(selectedDevice: Mixer.Info): Pair<TargetDataLine?, AudioFormat?> {
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