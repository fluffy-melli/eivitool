package eivitool.ui

import java.awt.*
import java.awt.image.*
import javax.swing.*

import javax.sound.sampled.AudioSystem
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent
import eivitool.utils.*

class App : JFrame("Eivitool") {
    private val label = JLabel()
    private var timer: Timer? = null
    private val recorder = Recorder()
    private var config = Config.load()
    private val audioVisualizer = AudioVisualizer()
    private val recordButton = JButton("녹화 시작")
    private val timeLabel: JLabel
    private val cpuLabel: JLabel
    private val memoryeLabel: JLabel
    private val fpsLabel: JLabel
    private val clipLabel: JLabel

    init {
        defaultCloseOperation = EXIT_ON_CLOSE
        setSize(800, 655)
        setLocationRelativeTo(null)
        layout = BorderLayout()

        val topPanel = JPanel().apply {
            preferredSize = Dimension(this.width, 25)
            layout = BorderLayout()
            background = Color(30, 31, 34)

            val settingsButton = JButton("설정").apply {
                addActionListener {
                    SettingsDialog(this@App, config, audioVisualizer).isVisible = true
                    applyConfigChanges()
                }
            }

            add(settingsButton, BorderLayout.WEST)
        }

        val middlePanel = JScrollPane(label).apply {
            horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
            verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_NEVER
            preferredSize = Dimension(this.width, this.height - 140)
            background = Color(30, 31, 34)
        }

        val bottomPanel = JPanel().apply {
            preferredSize = Dimension(this.width, 115)
            layout = FlowLayout(FlowLayout.LEFT)
            background = Color(43, 45, 48)

            recordButton.addActionListener {
                if (!recorder.isRecording) {
                    recordButton.text = "녹화 종료"
                    recorder.start(config, timeStamp())
                    SendDesktopAlarm("화면 녹화 시작", "화면 녹화가 시작되었습니다.")
                } else {
                    recordButton.text = "녹화 시작"
                    SendDesktopAlarm("인코딩 시작", "화면 녹화가 중지되었습니다. 인코딩을 시작할게요.")
                    recorder.stop()
                    SendDesktopAlarm("인코딩 성공", "인코딩이 성공적으로 실행되었어요.")
                }
            }

            val gbc = GridBagConstraints().apply {
                insets = Insets(10, 10, 10, 10)
                anchor = GridBagConstraints.WEST
                fill = GridBagConstraints.HORIZONTAL
            }

            gbc.gridx = 0
            gbc.gridy = 0
            gbc.gridwidth = 2
            add(audioVisualizer, gbc)

            gbc.gridy = 1
            gbc.gridwidth = 1
            add(recordButton, gbc)
        }

        val infoPanel = JPanel().apply {
            preferredSize = Dimension(this.width, 25)
            layout = FlowLayout(FlowLayout.RIGHT)
            background = Color(30, 31, 34)

            timeLabel = JLabel("00:00:00").apply {
                foreground = Color.WHITE
            }

            cpuLabel = JLabel("CPU: 0.00%").apply {
                foreground = Color.WHITE
            }

            memoryeLabel = JLabel("Heap: 0 Byte").apply {
                foreground = Color.WHITE
            }

            fpsLabel = JLabel("FPS: 0/${config.recordFPS}").apply {
                foreground = Color.WHITE
            }

            clipLabel = JLabel("CLIP: 0/0").apply {
                foreground = Color.WHITE
            }

            val gbc = GridBagConstraints().apply {
                insets = Insets(10, 30, 10, 30)
                anchor = GridBagConstraints.WEST
                fill = GridBagConstraints.HORIZONTAL
            }

            val line1 = JLabel("|").apply {
                foreground = Color.WHITE
            }

            val line2 = JLabel("|").apply {
                foreground = Color.WHITE
            }

            val line3 = JLabel("|").apply {
                foreground = Color.WHITE
            }

            val line4 = JLabel("|").apply {
                foreground = Color.WHITE
            }

            gbc.gridx = 0
            add(timeLabel, gbc)

            gbc.gridx = 1
            add(line4, gbc)

            gbc.gridx = 2
            add(clipLabel, gbc)

            gbc.gridx = 3
            add(line1, gbc)

            gbc.gridx = 4
            add(memoryeLabel, gbc)

            gbc.gridx = 5
            add(line2, gbc)

            gbc.gridx = 6
            add(cpuLabel, gbc)

            gbc.gridx = 7
            add(line3, gbc)

            gbc.gridx = 8
            add(fpsLabel, gbc)

        }

        val contentPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            add(bottomPanel)
            add(infoPanel)
        }

        add(topPanel, BorderLayout.NORTH)
        add(middlePanel, BorderLayout.CENTER)
        add(contentPanel, BorderLayout.SOUTH)

        KeyListener(listOf(Pair(NativeKeyEvent.VC_F12, fun () {
            if (!recorder.isRecording) {
                recordButton.text = "녹화 종료"
                recorder.start(config, timeStamp())
                SendDesktopAlarm("화면 녹화 시작", "화면 녹화가 시작되었습니다.")
            } else {
                recordButton.text = "녹화 시작"
                SendDesktopAlarm("인코딩 시작", "화면 녹화가 중지되었습니다. 인코딩을 시작할게요.")
                recorder.stop()
                SendDesktopAlarm("인코딩 성공", "인코딩이 성공적으로 실행되었어요.")
            }
        })))

        audioVisualizer.start(AudioSystem.getMixerInfo()[config.recordAudioSystem])
        recorder.StartCacheing(config, AudioSystem.getMixerInfo()[config.recordAudioSystem])
        this.restartTimer()
        Timer(1000 ) {
            this.system()
        }.start()
    }

    private fun applyConfigChanges() {
        restartTimer()
    }

    private fun restartTimer() {
        timer?.stop()
        timer = Timer(1000 / 30) {
            this.update()
        }
        timer?.start()
    }

    private fun system() {
        if (recorder.isRecording) {
            timeLabel.foreground = Color.RED
        } else {
            timeLabel.foreground = Color.WHITE
        }
        timeLabel.text = recorder.afterTime()
        cpuLabel.text = "CPU: ${getUsageCpu()}"
        memoryeLabel.text = "Heap: ${getUsageMemory()}"
    }

    private fun update() {
        var image: BufferedImage = recorder.lastFrame
        image = PaddingImage(image, this.width, this.height - 140)
        label.icon = ImageIcon(image)
        fpsLabel.text = "FPS: ${recorder.FPS.fps}/${config.recordFPS}"
        clipLabel.text = "CLIP: ${recorder.cacheing.audioQueue?.size}/${recorder.cacheing.frameQueue?.size}"
    }
}
