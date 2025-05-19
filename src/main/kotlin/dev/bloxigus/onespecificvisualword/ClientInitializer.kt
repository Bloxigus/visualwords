package dev.bloxigus.onespecificvisualword

import com.google.gson.Gson
import net.fabricmc.api.ClientModInitializer

class ClientInitializer : ClientModInitializer {
    override fun onInitializeClient() {
        val configStream = javaClass.getResourceAsStream("/assets/onespecificvisualword/config.json") ?: throw Error("config error (oosies)")
        val tempConfig = gson.fromJson(
            configStream.readAllBytes().toString(Charsets.UTF_8),
            Map::class.java)
        for (keyVal in tempConfig) {
            val key = keyVal.key as String
            val value = keyVal.value as String
            WordReplacer.parseComponentFromString(value) ?.let {
                Config.words[key] = it
            }
        }
    }
    companion object {
        private val gson = Gson()
    }
}