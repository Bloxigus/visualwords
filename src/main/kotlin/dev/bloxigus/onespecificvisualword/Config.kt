package dev.bloxigus.onespecificvisualword

import com.google.gson.GsonBuilder
import com.google.gson.annotations.Expose
import dev.bloxigus.onespecificvisualword.types.ComponentAdapter
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.MutableComponent
import java.io.InputStream
import java.io.OutputStreamWriter
import kotlin.io.path.Path

class Config {
    @Expose
    val words: List<WordReplacement> = mutableListOf()



    companion object {
        @JvmStatic
        private val configDir = Path(Minecraft.getInstance().gameDirectory.path, "config", "visualwords").toFile()
        @JvmStatic
        private val configFile = Path(configDir.path, "config.json").toFile()
        @JvmStatic
        private val backupConfig = Path(configDir.path, "config_backup.json").toFile()
        @JvmStatic
        private val stagingConfig = Path(configDir.path, "config_staging.json").toFile()
        @JvmStatic
        private val gson = GsonBuilder()
            .registerTypeAdapter(
                MutableComponent::class.java,
                ComponentAdapter()
            )
            .setPrettyPrinting()
            .create()
        @JvmStatic
        var config = Config()
        @JvmStatic
        fun loadConfig() {
            if (configFile.exists()) {
                configFile.inputStream()
            } else {
                Config::class.java.getResourceAsStream("/assets/onespecificvisualword/default_config.json") ?: throw Error("config error (oosies)")
            }.use {
                try {
                    config = internalLoadConfig(it)
                    WordReplacer.updateCachedConfig()
                    println("Loaded ${config.words.size} visual words")
                } catch (ex: Exception) {
                    println("Critical error loading config! Resetting to default!")
                }
            }

            saveConfig()
        }
        private fun internalLoadConfig(stream: InputStream): Config {
            val configString = stream.readAllBytes().toString(Charsets.UTF_8)
            val newConfig: Config = gson.fromJson(
                configString,
                Config::class.java)
            return newConfig
        }
        @JvmStatic
        fun saveConfig() {
            if (!configDir.exists()) configDir.mkdirs()
            if (!configFile.exists()) configFile.createNewFile()

            if (backupConfig.exists()) backupConfig.delete()
            if (stagingConfig.exists()) stagingConfig.delete()

            // Backup previous save
            configFile.renameTo(backupConfig)

            // Write to a staging file
            OutputStreamWriter(stagingConfig.outputStream()).use {
                it.write(gson.toJson(config))
            }

            // Ensure staging file reads correctly
            stagingConfig.inputStream().use {
                internalLoadConfig(it)
            }

            // Renames the file
            stagingConfig.renameTo(configFile)
        }
    }
}

data class WordReplacement(
    @Expose
    var replace: MutableComponent,
    @Expose
    var with: MutableComponent
) {
    fun getReplaceStyledLetters() = StyledLetter.fromComponent(replace)
    fun getWithStyledLetters() = StyledLetter.fromComponent(with)
}