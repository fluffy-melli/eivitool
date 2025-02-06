package eivitool.utils

import org.bytedeco.ffmpeg.global.avcodec
import org.bytedeco.javacv.FFmpegFrameRecorder
import org.bytedeco.javacv.Java2DFrameConverter
import java.awt.*
import java.io.*
import java.nio.ShortBuffer
import java.util.concurrent.Executors
import javax.sound.sampled.*

class Recorder {
    @Volatile
    var isRecording = false
    private var videoThread: Thread? = null
    private var audioThread: Thread? = null
    private var recorder: FFmpegFrameRecorder? = null
    private var lines: TargetDataLine? = null
    private val robot = Robot()
    private val screenBounds = mutableMapOf<Int, Rectangle>()

    private val executor = Executors.newFixedThreadPool(2)

    init {
        GraphicsEnvironment.getLocalGraphicsEnvironment().screenDevices.forEachIndexed { index, screen ->
            screenBounds[index] = screen.defaultConfiguration.bounds
        }
    }

    fun start(config:AppConfig, audioDevice: Mixer.Info, outfile: String) {
        if (isRecording) return
        isRecording = true
        recorder = FFmpegFrameRecorder("${config.recordFolderPath}/$outfile.mp4", config.recordResolution[0], config.recordResolution[1], 2).apply {
            format = "mp4"
            videoCodec = avcodec.AV_CODEC_ID_H264
            setVideoBitrate(config.recordVideoBitrateKB*1000)
            setFrameRate(config.recordFPS.toDouble())

            setAudioChannels(2)
            setAudioCodec(avcodec.AV_CODEC_ID_AAC)
            setAudioBitrate(config.recordAudioBitrateKB*1000)

            setVideoOption("bEnableFrameSkip", "1")

            start()
        }

        startAudio(audioDevice)
        startVideo(config.recordDisplay, config.recordFPS, config.recordResolution[0], config.recordResolution[1])
    }

    private fun startAudio(audioDevice: Mixer.Info) {
        val (line, audioFormat) = GetTargetDataLine(audioDevice)
        if (line != null && audioFormat != null) {
            line.start()
            val buffer = ByteArray(2048)
            val byteArrayOutputStream = ByteArrayOutputStream()
            audioThread = Thread {
                try {
                    while (isRecording) {
                        val bytesRead = line.read(buffer, 0, buffer.size)
                        if (bytesRead > 0) {
                            byteArrayOutputStream.write(buffer, 0, bytesRead)
                        }
                    }
                    val audioData = byteArrayOutputStream.toByteArray()
                    val samples = ShortArray(audioData.size / 2)
                    for (i in audioData.indices step 2) {
                        samples[i / 2] = ((audioData[i + 1].toInt() shl 8) or (audioData[i].toInt() and 0xFF)).toShort()
                    }
                    val buffer = ShortBuffer.wrap(samples)
                    recorder?.recordSamples(audioFormat.sampleRate.toInt(), audioFormat.channels, buffer)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }.apply { start() }
        }
    }


    private fun startVideo(display: Int, fps: Int, width: Int, height: Int) {
        videoThread = Thread {
            val frameTimeNanos = 1_000_000_000L / fps
            try {
                val bounds = screenBounds[display]
                while (isRecording) {
                    val startTime = System.nanoTime()
                    executor.submit {
                        try {
                            val image = robot.createScreenCapture(bounds)
                            val resizedImage = ResizeImageFast(image, width, height)
                            val converter = Java2DFrameConverter()
                            val frame = converter.convert(resizedImage)
                            recorder?.record(frame)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    val elapsedTime = System.nanoTime() - startTime
                    val sleepTime = ((frameTimeNanos - elapsedTime) / 1_000_000).coerceAtLeast(0)
                    if (sleepTime > 0) Thread.sleep(sleepTime)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.apply { start() }
    }

    fun stop() {
        if (!isRecording) return
        isRecording = false
        try {
            audioThread?.join()
            videoThread?.join()
            lines?.stop()
            lines?.close()
            recorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
