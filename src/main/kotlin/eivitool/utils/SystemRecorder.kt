package eivitool.utils

import org.bytedeco.ffmpeg.global.avcodec
import org.bytedeco.ffmpeg.global.avutil
import org.bytedeco.javacv.FFmpegFrameRecorder
import org.bytedeco.javacv.FFmpegLogCallback
import org.bytedeco.javacv.Frame
import org.bytedeco.javacv.Java2DFrameConverter
import java.awt.*
import java.awt.image.BufferedImage
import java.io.*
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import javax.sound.sampled.*

class SystemRecorder {
    @Volatile
    var startTime = 0L
    var isRecording = false
    private var videoThread: Thread? = null
    private var audioThread: Thread? = null
    private var recorder: FFmpegFrameRecorder? = null
    private var lines: TargetDataLine? = null
    private val robot = Robot()
    private val screenBounds = mutableMapOf<Int, Rectangle>()

    private var executor = Executors.newSingleThreadExecutor()

    init {
        GraphicsEnvironment.getLocalGraphicsEnvironment().screenDevices.forEachIndexed { index, screen ->
            screenBounds[index] = screen.defaultConfiguration.bounds
        }
    }

    fun afterTime(): String {
        if (!isRecording) {
            return timeFormat(0)
        }
        val elapsedTime = System.currentTimeMillis() - startTime
        return timeFormat(elapsedTime / 1000)
    }

    fun start(config:AppConfig, audioDevice: Mixer.Info, outfile: String) {
        if (isRecording) return
        isRecording = true
        startTime = System.currentTimeMillis()
        recorder = FFmpegFrameRecorder("${config.recordFolderPath}/$outfile.mp4", config.recordResolution[0], config.recordResolution[1], 2).apply {
            format = "mp4"

            videoCodec = avcodec.AV_CODEC_ID_H264
            pixelFormat = avutil.AV_PIX_FMT_YUV420P
            videoBitrate = config.recordVideoBitrateKB * 1000
            frameRate = config.recordFPS.toDouble()
            gopSize = config.recordFPS * 2
            videoQuality = 30.0

            setOption("preset", "ultrafast")
            setOption("crf", "23")
            setOption("tune", "zerolatency")

            audioChannels = 2
            audioCodec = avcodec.AV_CODEC_ID_AAC
            audioBitrate = config.recordAudioBitrateKB * 1000
        }

        recorder?.start()
        startAudioRecorder(audioDevice)
        startVideoRecorder(config.recordDisplay, config.recordFPS, config.recordResolution[0], config.recordResolution[1])
    }

    private fun startAudioRecorder(audioDevice: Mixer.Info) {
        val (line, audioFormat) = GetAudioDataLine(audioDevice)
        if (line != null && audioFormat != null) {
            line.start()
            val buffer = ByteArray(2048)
            val outputStream = ByteArrayOutputStream()
            audioThread = Thread {
                try {
                    while (isRecording) {
                        val bytesRead = line.read(buffer, 0, buffer.size)
                        if (bytesRead > 0) {
                            outputStream.write(buffer, 0, bytesRead)
                        }
                    }
                    recorder?.recordSamples(audioFormat.sampleRate.toInt(), audioFormat.channels, audioBuffer(outputStream))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }.apply { start() }
        }
    }


    private fun startVideoRecorder(display: Int, fps: Int, width: Int, height: Int) {
        videoThread = Thread {
            val frameTimeNanos = 1_000_000_000L / fps
            try {
                val bounds = screenBounds[display]
                val converter = Java2DFrameConverter()
                if (executor.isTerminated() || executor.isShutdown()) {
                    executor = Executors.newSingleThreadExecutor()
                }
                while (isRecording) {
                    val startTime = System.nanoTime()
                    val image = robot.createScreenCapture(bounds)
                    executor.submit {
                        try {
                            val resized = ResizeImage(image, width, height)
                            val frame = converter.convert(resized)
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
            lines?.stop()
            lines?.close()
            executor.shutdown()
            recorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
