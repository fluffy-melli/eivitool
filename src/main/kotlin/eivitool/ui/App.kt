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
    private val systemRecorder = SystemRecorder()
    private var config = Config.load()
    private val audioVisualizer = AudioVisualizer()
    private val recordButton = JButton("녹화 시작")

    init {
        defaultCloseOperation = EXIT_ON_CLOSE
        setSize(800, 630)
        setLocationRelativeTo(null)
        layout = BorderLayout()

        val topPanel = JPanel().apply {
            preferredSize = Dimension(this.width, 25)
            layout = BorderLayout()
            background = Color(34, 34, 34)

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
            preferredSize = Dimension(this.width, this.height - 165)
            background = Color(34, 34, 34)
        }

        val bottomPanel = JPanel().apply {
            preferredSize = Dimension(this.width, 140)
            layout = FlowLayout(FlowLayout.LEFT)
            background = Color.BLACK

            recordButton.addActionListener {
                if (!systemRecorder.isRecording) {
                    recordButton.text = "녹화 종료"
                    systemRecorder.start(config, AudioSystem.getMixerInfo()[config.recordAudioSystem], timeStamp())
                    SendDesktopAlarm("화면 녹화 시작", "화면 녹화가 시작되었습니다.")
                } else {
                    recordButton.text = "녹화 시작"
                    SendDesktopAlarm("인코딩 시작", "화면 녹화가 중지되었습니다. 인코딩을 시작할게요.")
                    systemRecorder.stop()
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

        add(topPanel, BorderLayout.NORTH)
        add(middlePanel, BorderLayout.CENTER)
        add(bottomPanel, BorderLayout.SOUTH)

        KeyListener(listOf(Pair(NativeKeyEvent.VC_F12, fun (): Unit {
            if (!systemRecorder.isRecording) {
                recordButton.text = "녹화 종료"
                systemRecorder.start(config, AudioSystem.getMixerInfo()[config.recordAudioSystem], timeStamp())
                SendDesktopAlarm("화면 녹화 시작", "화면 녹화가 시작되었습니다.")
            } else {
                recordButton.text = "녹화 시작"
                SendDesktopAlarm("인코딩 시작", "화면 녹화가 중지되었습니다. 인코딩을 시작할게요.")
                systemRecorder.stop()
                SendDesktopAlarm("인코딩 성공", "인코딩이 성공적으로 실행되었어요.")
            }
        })))

        audioVisualizer.start(AudioSystem.getMixerInfo()[config.recordAudioSystem])
        this.restartTimer()
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

    private fun update() {
        var image: BufferedImage = getDisplayCapture(config.recordDisplay)
        image = PaddingImage(image, this.width, this.height - 140)
        label.icon = ImageIcon(image)
    }
}
