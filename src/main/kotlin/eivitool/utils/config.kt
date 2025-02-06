package eivitool.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.File

data class AppConfig(
    var recordFPS: Int = 60,
    var recordDisplay: Int = 0,
    var recordResolution: List<Int> = listOf(1920, 1080),
    var recordFolderPath: String = "./",
    var recordAudioSystem: Int = 0,
    var recordResolutionIndex: Int = 0,
    var recordVideoBitrateKB: Int = 400,
    var recordAudioBitrateKB: Int = 128,
)

object Config {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private val configFile = File("config.json")

    fun save(config: AppConfig) {
        configFile.writeText(gson.toJson(config))
    }

    fun load(): AppConfig {
        return if (configFile.exists()) {
            val type = object : TypeToken<AppConfig>() {}.type
            gson.fromJson(configFile.readText(), type)
        } else {
            AppConfig()
        }
    }
}
