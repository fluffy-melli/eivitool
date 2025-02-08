package eivitool.utils

import org.bytedeco.ffmpeg.global.avcodec
import org.bytedeco.ffmpeg.global.avutil
import org.bytedeco.javacv.FFmpegFrameRecorder
import org.bytedeco.javacv.Java2DFrameConverter
import java.awt.*
import java.awt.image.BufferedImage
import java.io.*
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import javax.sound.sampled.*

class SystemRecorder {
    @Volatile
    var startTime = 0L
    val FPS = FPSCounter()
    var isRecording = false
    var lastFrame: BufferedImage
    private var videoThread: Thread? = null
    private var audioThread: Thread? = null
    private var encodingThread: Thread? = null
    private var recorder: FFmpegFrameRecorder? = null
    private var lines: TargetDataLine? = null
    private val robot = Robot()
    private val screenBounds = mutableMapOf<Int, Rectangle>()
    private val converter = Java2DFrameConverter()

    private val frameQueue: BlockingQueue<BufferedImage> = LinkedBlockingQueue(60)

    init {
        GraphicsEnvironment.getLocalGraphicsEnvironment().screenDevices.forEachIndexed { index, screen ->
            screenBounds[index] = screen.defaultConfiguration.bounds
        }
        lastFrame = robot.createScreenCapture(screenBounds[0])
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

            recorder?.setOption("b:v", "${config.recordVideoBitrateKB * 1000}")
            recorder?.setOption("rc-lookahead", "10")
            recorder?.setOption("tune", "zerolatency")

            setOption("preset", "veryfast")
            setOption("crf", "18")
            setOption("tune", "zerolatency")

            setOption("vsync", "vfr")
            setOption("fflags", "nobuffer")
            setOption("flags", "low_delay")
            setOption("threads", "1")

            audioChannels = 2
            audioCodec = avcodec.AV_CODEC_ID_AAC
            audioBitrate = config.recordAudioBitrateKB * 1000
        }

        recorder?.start()
        startVideoRecorder(config.recordDisplay, config.recordFPS, config.recordResolution[0], config.recordResolution[1])
        startAudioRecorder(audioDevice)
    }

    private fun startAudioRecorder(audioDevice: Mixer.Info) {
        val (line, audioFormat) = GetAudioDataLine(audioDevice)
        if (line != null && audioFormat != null) {
            line.start()
            val buffer = ByteArray(1024)
            val outputStream = ByteArrayOutputStream()
            audioThread = Thread {
                try {
                    while (isRecording) {
                        outputStream.reset()
                        val bytesRead = line.read(buffer, 0, buffer.size)
                        if (bytesRead > 0) {
                            outputStream.write(buffer, 0, bytesRead)
                        }
                        val currentTimestamp = ((System.nanoTime() - startTime * 1_000_000L) / 1000L).coerceAtLeast(recorder?.timestamp ?: 0)
                        recorder?.timestamp = currentTimestamp
                        recorder?.recordSamples(audioFormat.sampleRate.toInt(), audioFormat.channels, audioBuffer(outputStream))
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }.apply { start() }
        }
    }



    private fun startVideoRecorder(display: Int, fps: Int, width: Int, height: Int) {
        val frameTimeNanos = 1_000_000_000L / fps
        val bounds = screenBounds[display] ?: return
        videoThread = Thread {
            try {
                while (isRecording) {
                    val startTime = System.nanoTime()
                    try {
                        lastFrame = ResizeImage(robot.createScreenCapture(bounds), width, height)
                        if (!frameQueue.offer(lastFrame, (1000 / fps).toLong(), TimeUnit.MILLISECONDS)) {
                            println("⚠️ Frame drops occur!")
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    val elapsedTime = System.nanoTime() - startTime
                    val sleepTime = ((frameTimeNanos - elapsedTime) / 1_000_000).coerceAtLeast(0)
                    if (sleepTime > 0) {
                        Thread.sleep(sleepTime)
                    } else {
                        println("⚠️ Frame drops occur!")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.apply { start() }
        encodingThread = Thread {
            try {
                var frame: BufferedImage
                while (isRecording) {
                    if (frameQueue.isEmpty()) {
                        continue
                    }
                    frame = frameQueue.poll(1000, TimeUnit.MILLISECONDS) ?: continue
                    try {
                        val frames = converter.convert(frame)
                        if (frames != null) {
                            val currentTimestamp = ((System.currentTimeMillis() - startTime) * 1000L).coerceAtLeast(0)
                            if (recorder?.timestamp ?: 0 < currentTimestamp) {
                                recorder?.timestamp = currentTimestamp
                            }
                            recorder?.record(frames)
                            FPS.update()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
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
            while (!frameQueue.isEmpty()) {
                val frame = converter.convert(frameQueue.poll() ?: continue)
                val currentTimestamp = ((System.currentTimeMillis() - startTime) * 1000L).coerceAtLeast(0)
                if (recorder?.timestamp ?: 0 < currentTimestamp) {
                    recorder?.timestamp = currentTimestamp
                }
                recorder?.record(frame)
            }
            audioThread?.join()
            videoThread?.join()
            encodingThread?.join()
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
