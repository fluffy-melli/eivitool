package eivitool.ui

import java.awt.*
import java.awt.image.*
import javax.swing.*

import java.io.File
import javax.sound.sampled.AudioSystem
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent
import eivitool.utils.*
import eivitool.value.GetResolutionList

class App : JFrame("Eivitool") {
    private val label = JLabel()

    private var timer: Timer? = null
    private val recorder = Recorder()
    private var config = Config.load()
    private val audioVisualizer = AudioVisualizer()

    private val recordButton = JButton("녹화 시작").apply {}

    init {
        defaultCloseOperation = EXIT_ON_CLOSE
        setSize(800, 630)
        layout = BorderLayout()

        val topPanel = JScrollPane(label).apply {
            horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
            verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_NEVER
            preferredSize = Dimension(this.width, this.height - 140)
        }

        val bottomPanel = JPanel().apply {
            background = Color.BLACK
            preferredSize = Dimension(this.width, 140)
            layout = FlowLayout(FlowLayout.LEFT)
            val displayList = GetDisplayList()
            val displayDropdown = JComboBox(displayList.map { "디스플레이 ${it.first}: (${it.third})" }.toTypedArray())
            displayDropdown.selectedIndex = config.recordDisplay
            displayDropdown.addActionListener {
                val selected = displayDropdown.selectedIndex
                config.recordDisplay = displayList[selected].first
                Config.save(config)
                SendDesktopAlarm("출력 디스플레이 변경", "디스플레이 ${config.recordDisplay}: (${displayList[selected].third}) 으로 변경되었어요")
            }

            val deviceList = AudioSystem.getMixerInfo()
            var selectedDevice = deviceList[config.recordAudioSystem]
            audioVisualizer.startAudioCapture(selectedDevice)
            val deviceDropdown = JComboBox(deviceList.map { it.name }.toTypedArray())
            deviceDropdown.selectedIndex = config.recordAudioSystem
            deviceDropdown.addActionListener {
                config.recordAudioSystem = deviceDropdown.selectedIndex
                if (config.recordAudioSystem >= 0) {
                    selectedDevice = deviceList[config.recordAudioSystem]
                    audioVisualizer.startAudioCapture(selectedDevice)
                    Config.save(config)
                    SendDesktopAlarm("오디오 장치 변경", "오디오 장치 '${selectedDevice.name}' 으로 변경되었어요")
                }
            }

            val resolutionList = GetResolutionList()
            val resolutionDropdown = JComboBox(resolutionList.map { it.first }.toTypedArray())
            resolutionDropdown.selectedIndex = config.recordResolutionIndex
            resolutionDropdown.addActionListener {
                val selected = resolutionDropdown.selectedIndex
                config.recordResolutionIndex = selected
                config.recordResolution = listOf(resolutionList[selected].second, resolutionList[selected].third)
                Config.save(config)
                SendDesktopAlarm("출력 해상도 변경", "${resolutionList[selected].first} 으로 변경되었어요")
            }

            val fpsLabel = JLabel("출력 FPS 선택:").apply {}
            val fpsInput = JTextField("60", 5)
            fpsInput.text = "${config.recordFPS}"
            fpsInput.addActionListener {
                val fps = fpsInput.text.toIntOrNull()?.coerceIn(1, 240) ?: 60
                config.recordFPS = fps
                Config.save(config)
                SendDesktopAlarm("출력 FPS 변경", "${config.recordFPS} 으로 변경되었어요")
                restartTimer()
            }

            val folderChooserButton = JButton(TruncatePath(config.recordFolderPath)).apply {
                background = Color.WHITE
            }
            folderChooserButton.addActionListener {
                val folderChooser = JFileChooser().apply {
                    folderChooserButton.text = TruncatePath(config.recordFolderPath)
                    fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
                    currentDirectory = File(config.recordFolderPath)
                }

                val userSelection = folderChooser.showSaveDialog(null)
                if (userSelection == JFileChooser.APPROVE_OPTION) {
                    config.recordFolderPath = folderChooser.selectedFile.absolutePath
                    folderChooserButton.text = TruncatePath(config.recordFolderPath)
                    Config.save(config)
                    SendDesktopAlarm("출력 폴더 변경", "새로운 폴더 경로: ${config.recordFolderPath}")
                }
            }

            recordButton.addActionListener {
                if (!recorder.isRecording) {
                    recordButton.text = "녹화 종료"
                    recorder.start(config.recordDisplay, config.recordFPS, config.recordResolution[0],config.recordResolution[1],deviceList[config.recordAudioSystem], config.recordFolderPath, Timestamp())
                    SendDesktopAlarm("화면 녹화 시작", "화면 녹화가 시작되었습니다.")
                } else {
                    recordButton.text = "녹화 시작"
                    SendDesktopAlarm("인코딩 시작", "화면 녹화가 중지되었습니다. 인코딩을 시작할게요.")
                    recorder.stop()
                    SendDesktopAlarm("인코딩 성공", "인코딩이 성공적으로 실행되었어요.")
                }
            }

            val panel = JPanel()
            panel.layout = GridBagLayout()
            panel.background = Color.BLACK
            val gbc = GridBagConstraints().apply {
                gridx = 0
                anchor = GridBagConstraints.WEST
                fill = GridBagConstraints.HORIZONTAL
                insets = Insets(5, 5, 5, 5)
            }

            gbc.gridx = 0
            gbc.gridy = 0
            gbc.gridwidth = 2
            panel.add(audioVisualizer, gbc)

            gbc.gridx = 0
            gbc.gridy = 1
            gbc.gridwidth = 1
            panel.add(JLabel("오디오 장치 선택:"), gbc)
            gbc.gridx = 1
            panel.add(deviceDropdown, gbc)

            gbc.gridx = 0
            gbc.gridy = 2
            panel.add(JLabel("디스플레이 선택:"), gbc)
            gbc.gridx = 1
            panel.add(displayDropdown, gbc)

            gbc.gridx = 3
            gbc.gridy = 0
            panel.add(JLabel("출력 폴더 선택:"), gbc)
            gbc.gridx = 4
            panel.add(folderChooserButton, gbc)

            gbc.gridx = 3
            gbc.gridy = 1
            panel.add(JLabel("출력 해상도 선택:"), gbc)
            gbc.gridx = 4
            panel.add(resolutionDropdown, gbc)

            gbc.gridx = 3
            gbc.gridy = 2
            panel.add(fpsLabel, gbc)
            gbc.gridx = 4
            panel.add(fpsInput, gbc)

            gbc.gridx = 5
            gbc.gridy = 0
            panel.add(recordButton, gbc)

            add(panel)
        }

        add(topPanel, BorderLayout.CENTER)
        add(bottomPanel, BorderLayout.SOUTH)

        KeyListener(listOf(Pair(NativeKeyEvent.VC_F12, fun (): Unit {
            val deviceList = AudioSystem.getMixerInfo()
            if (!recorder.isRecording) {
                recordButton.text = "녹화 종료"
                recorder.start(config.recordDisplay, config.recordFPS, config.recordResolution[0],config.recordResolution[1],deviceList[config.recordAudioSystem], config.recordFolderPath, Timestamp())
                SendDesktopAlarm("화면 녹화 시작", "화면 녹화가 시작되었습니다.")
            } else {
                recordButton.text = "녹화 시작"
                SendDesktopAlarm("인코딩 시작", "화면 녹화가 중지되었습니다. 인코딩을 시작할게요.")
                recorder.stop()
                SendDesktopAlarm("인코딩 성공", "인코딩이 성공적으로 실행되었어요.")
            }
        })))

        this.restartTimer()
    }

    private fun restartTimer() {
        timer?.stop()
        timer = Timer(1000 / 30) {
            this.update()
        }
        timer?.start()
    }

    private fun update() {
        var image: BufferedImage = GetDisplayCapture(config.recordDisplay)
        image = PaddingImage(image, this.width, this.height - 140)
        label.icon = ImageIcon(image)
    }

}