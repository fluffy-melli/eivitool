package eivitool.ui

import java.awt.*
import javax.swing.*
import eivitool.utils.*
import eivitool.value.GetResolutionList
import eivitool.value.RecordType
import javax.sound.sampled.AudioSystem
import java.io.File

class SettingsDialog(parent: JFrame, config: AppConfig, audio: AudioVisualizer) : JDialog(parent, "설정", true) {
    private val displayDropdown: JComboBox<String> = JComboBox()
    private val audioDropdown: JComboBox<String> = JComboBox()
    private val readerTypeDropdown: JComboBox<String> = JComboBox()
    private val resolutionDropdown: JComboBox<String> = JComboBox()
    private val fpsInput: JTextField = JTextField(config.recordFPS.toString(), 5)
    private val videoBitrateInput: JTextField = JTextField(config.recordVideoBitrateKB.toString(), 5)
    private val audioBitrateInput: JTextField = JTextField(config.recordAudioBitrateKB.toString(), 5)
    private val folderChooserButton: JButton = JButton(paddingText(config.recordFolderPath)).apply {
        background = Color.WHITE
        addActionListener {
            val folderChooser = JFileChooser().apply {
                fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
                currentDirectory = File(config.recordFolderPath)
            }
            val userSelection = folderChooser.showOpenDialog(this)
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                config.recordFolderPath = folderChooser.selectedFile.absolutePath
                this.text = paddingText(config.recordFolderPath)
                Config.save(config)
            }
        }
    }

    init {
        var size = GraphicsEnvironment.getLocalGraphicsEnvironment().screenDevices[config.recordDisplay].defaultConfiguration.bounds
        var resolutionList = GetResolutionList(size.width, size.height)
        contentPane.background = Color(43, 45, 48)
        layout = GridBagLayout()
        val gbc = GridBagConstraints().apply {
            insets = Insets(10, 10, 10, 10)
            anchor = GridBagConstraints.WEST
            fill = GridBagConstraints.HORIZONTAL
        }

        val displayList = getDisplayList()
        displayDropdown.model = DefaultComboBoxModel(displayList.map { "디스플레이 ${it.first}: (${it.third})" }.toTypedArray())
        displayDropdown.selectedIndex = config.recordDisplay
        displayDropdown.addActionListener {
            config.recordDisplay = displayDropdown.selectedIndex
            size = GraphicsEnvironment.getLocalGraphicsEnvironment().screenDevices[config.recordDisplay].defaultConfiguration.bounds
            resolutionList = GetResolutionList(size.x, size.y)
        }

        val deviceList = AudioSystem.getMixerInfo()
        audioDropdown.model = DefaultComboBoxModel(deviceList.map { it.name }.toTypedArray())
        audioDropdown.selectedIndex = config.recordAudioSystem
        audioDropdown.addActionListener {
            config.recordAudioSystem = audioDropdown.selectedIndex
            audio.start(deviceList[config.recordAudioSystem])
        }

        resolutionDropdown.model = DefaultComboBoxModel(resolutionList.map { it.first }.toTypedArray())
        resolutionDropdown.selectedIndex = config.recordResolutionIndex
        resolutionDropdown.addActionListener {
            config.recordResolutionIndex = resolutionDropdown.selectedIndex
            config.recordResolution = listOf(resolutionList[resolutionDropdown.selectedIndex].second, resolutionList[resolutionDropdown.selectedIndex].third)
        }

        val typeList = RecordType()
        readerTypeDropdown.model = DefaultComboBoxModel(typeList.map { it.first }.toTypedArray())
        readerTypeDropdown.selectedIndex = if (config.recordClip) 1 else 0
        readerTypeDropdown.addActionListener {
            config.recordClip = typeList[readerTypeDropdown.selectedIndex].second
        }

        gbc.gridx = 0
        gbc.gridy = 0
        add(JLabel("디스플레이 선택:").apply { foreground = Color.WHITE }, gbc)

        gbc.gridx = 1
        add(displayDropdown, gbc)

        gbc.gridx = 0
        gbc.gridy = 1
        add(JLabel("오디오 장치 선택:").apply { foreground = Color.WHITE }, gbc)

        gbc.gridx = 1
        add(audioDropdown, gbc)

        gbc.gridx = 0
        gbc.gridy = 2
        add(JLabel("출력 해상도 선택:").apply { foreground = Color.WHITE }, gbc)

        gbc.gridx = 1
        add(resolutionDropdown, gbc)

        gbc.gridx = 0
        gbc.gridy = 3
        add(JLabel("FPS 선택:").apply { foreground = Color.WHITE }, gbc)

        gbc.gridx = 1
        add(fpsInput, gbc)

        gbc.gridx = 0
        gbc.gridy = 4
        add(JLabel("비디오 비트레이트 (KB):").apply { foreground = Color.WHITE }, gbc)

        gbc.gridx = 1
        add(videoBitrateInput, gbc)

        gbc.gridx = 0
        gbc.gridy = 5
        add(JLabel("오디오 비트레이트 (KB):").apply { foreground = Color.WHITE }, gbc)

        gbc.gridx = 1
        add(audioBitrateInput, gbc)

        gbc.gridx = 0
        gbc.gridy = 6
        add(JLabel("녹화 저장 경로:").apply { foreground = Color.WHITE }, gbc)

        gbc.gridx = 1
        add(folderChooserButton, gbc)

        gbc.gridx = 0
        gbc.gridy = 7
        add(JLabel("클립 활성화 (베타):").apply { foreground = Color.WHITE }, gbc)

        gbc.gridx = 1
        add(readerTypeDropdown, gbc)

        val saveButton = JButton("저장")
        saveButton.addActionListener {
            config.recordFPS = fpsInput.text.toIntOrNull() ?: 30
            config.recordVideoBitrateKB = videoBitrateInput.text.toIntOrNull() ?: 200
            config.recordAudioBitrateKB = audioBitrateInput.text.toIntOrNull() ?: 128
            Config.save(config)
            dispose()
        }

        gbc.gridx = 0
        gbc.gridy = 8
        gbc.gridwidth = 2
        add(saveButton, gbc)

        pack()
        setLocationRelativeTo(parent)
    }
}
