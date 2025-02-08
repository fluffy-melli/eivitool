package eivitool.utils

import org.bytedeco.ffmpeg.global.avcodec
import org.bytedeco.ffmpeg.global.avutil
import org.bytedeco.javacv.FFmpegFrameRecorder
import org.bytedeco.javacv.Java2DFrameConverter
import java.awt.*
import java.awt.image.BufferedImage
import java.io.*
import java.util.concurrent.TimeUnit
import javax.sound.sampled.*

class Recorder {
    @Volatile
    var startTime = 0L
    val FPS = FPSCounter()
    var isRecording = false
    var lastFrame: BufferedImage
    val cacheing = RecorderClip()
    private var videoThread: Thread? = null
    private var audioThread: Thread? = null
    private var encodingThread: Thread? = null
    private var audioencodingThread: Thread? = null
    private var recorder: FFmpegFrameRecorder? = null
    private var lines: TargetDataLine? = null
    private val robot = Robot()
    private val screenBounds = mutableMapOf<Int, Rectangle>()
    private val converter = Java2DFrameConverter()
    private var audioFormat: AudioFormat? = null

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

    fun StartCacheing(config:AppConfig, audioDevice: Mixer.Info) {
        cacheing.init(config.recordFPS)
        val (line, audioFormat) = GetAudioDataLine(audioDevice)
        if (line != null && audioFormat != null) {
            line.start()
            this.audioFormat = audioFormat
            val buffer = ByteArray(1024)
            val outputStream = ByteArrayOutputStream()
            audioThread = Thread {
                try {
                    while (true) {
                        if (!isRecording && !config.recordClip) {
                            Thread.sleep(50)
                            continue
                        }
                        outputStream.reset()
                        val bytesRead = line.read(buffer, 0, buffer.size)
                        if (bytesRead > 0) {
                            outputStream.write(buffer, 0, bytesRead)
                        }
                        val audiof = audioBuffer(outputStream)
                        cacheing.cacheAudio(audiof)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }.apply { start() }
            val frameTimeNanos = 1_000_000_000L / config.recordFPS
            val bounds = screenBounds[config.recordDisplay] ?: return
            videoThread = Thread {
                try {
                    while (true) {
                        val startTime = System.nanoTime()
                        if (!isRecording && !config.recordClip) {
                            Thread.sleep(50)
                            continue
                        }
                        if (!isRecording) {
                            val width = screenBounds[0]?.width ?: 1920
                            val height = screenBounds[0]?.height ?: 1080
                            val blackImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
                            val blackGraphics = blackImage.createGraphics()
                            blackGraphics.fillRect(0, 0, width, height)
                            blackGraphics.dispose()
                            val frames = converter.convert(blackImage)
                            if (frames != null) {
                                cacheing.cacheFrame(frames)
                                FPS.update()
                            }
                        } else {
                            try {
                                lastFrame = ResizeImage(robot.createScreenCapture(bounds), config.recordResolution[0], config.recordResolution[1])
                                val frames = converter.convert(lastFrame)
                                if (frames != null) {
                                    cacheing.cacheFrame(frames)
                                    FPS.update()
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
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
        }
    }

    fun start(config:AppConfig, outfile: String) {
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
        startVideoRecorder()
        startAudioRecorder()
    }

    private fun startAudioRecorder() {
        audioencodingThread = Thread {
            try {
                while (isRecording) {
                    if (cacheing.audioQueue?.isEmpty() == true) {
                        continue
                    }
                    val audios = cacheing.audioQueue!!.poll(1000, TimeUnit.MILLISECONDS)
                    if (audios != null) {
                        val currentTimestamp = ((System.nanoTime() - startTime * 1_000_000L) / 1000L).coerceAtLeast(recorder?.timestamp ?: 0)
                        recorder?.timestamp = currentTimestamp
                        if (audioFormat != null) {
                            recorder?.recordSamples(audioFormat!!.sampleRate.toInt(), audioFormat!!.channels, audios)
                        } else {
                            println("Audio format is null!")
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.apply { start() }
    }

    private fun startVideoRecorder() {
        encodingThread = Thread {
            try {
                while (isRecording) {
                    if (cacheing.frameQueue?.isEmpty() == true) {
                        continue
                    }
                    val frame = cacheing.frameQueue?.poll(1000, TimeUnit.MILLISECONDS)
                    try {
                        val currentTimestamp = ((System.currentTimeMillis() - startTime) * 1000L).coerceAtLeast(0)
                        if ((recorder?.timestamp ?: 0) < currentTimestamp) {
                            recorder?.timestamp = currentTimestamp
                        }
                        if (frame != null) {
                            recorder?.record(frame)
                        } else {
                            println("Frame format is null!")
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
            audioencodingThread?.join()
            encodingThread?.join()
            lines?.stop()
            lines?.close()
            recorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            encodingThread = null
            audioencodingThread = null
            recorder = null
        }
    }
}
