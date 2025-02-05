package eivitool.ui

import eivitool.utils.GetTargetDataLine
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import javax.sound.sampled.*
import javax.swing.JPanel
import javax.swing.Timer
import kotlin.math.log10
import kotlin.math.sqrt

class AudioVisualizer : JPanel() {
    private var volumeLevel = -50.0
    private val maxBars = 75
    private val bufferSize = 1024
    private val maxAmplitude = 32768.0
    private var line: TargetDataLine? = null

    init {
        preferredSize = Dimension(maxBars * 4, 25)
        background = Color.BLACK
        Timer(16) {
            repaint()
        }.start()
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        g.color = Color.DARK_GRAY
        g.fillRect(0, 0, maxBars * 4, 10)
        val normalized = ((volumeLevel + 50) / 50.0 * maxBars).toInt().coerceIn(0, maxBars)
        val barColor = when {
            volumeLevel >= -10 -> Color.RED
            volumeLevel >= -20 -> Color.ORANGE
            else -> Color.GREEN
        }
        g.color = barColor
        g.fillRect(0, 0, normalized * 4, 10)
        g.color = Color.WHITE
        g.drawString("%.1f dB".format(volumeLevel), 0, 20)
    }

    fun startAudioCapture(device: Mixer.Info) {
        stopAudioCapture()
        Thread {
            val (line) = GetTargetDataLine(device)
            if (line != null) {
                line.start()
                this.line = line
                val buffer = ByteArray(bufferSize)

                while (true) {
                    val bytesRead = line.read(buffer, 0, buffer.size)
                    if (bytesRead > 0) {
                        volumeLevel = calculateVolumeLevel(buffer)
                    }
                }
            }
        }.start()
    }

    private fun stopAudioCapture() {
        line?.stop()
        line?.close()
        line = null
    }

    private fun calculateVolumeLevel(buffer: ByteArray): Double {
        var sum = 0.0
        for (i in buffer.indices step 2) {
            val sample = ((buffer[i + 1].toInt() shl 8) or (buffer[i].toInt() and 0xFF)).toShort()
            sum += sample * sample
        }
        val rms = sqrt(sum / (buffer.size / 2))
        return 20 * log10(rms / maxAmplitude)
    }
}
