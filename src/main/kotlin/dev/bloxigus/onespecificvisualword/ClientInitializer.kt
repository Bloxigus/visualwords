package dev.bloxigus.onespecificvisualword

import dev.bloxigus.onespecificvisualword.command.ReloadCommand
import dev.bloxigus.onespecificvisualword.command.VisualWordsConfigCommand
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback

class ClientInitializer : ClientModInitializer {
    override fun onInitializeClient() {
        Config.loadConfig()
        ClientCommandRegistrationCallback.EVENT.register() { dispatcher, registry ->
            ReloadCommand.register(dispatcher)
            VisualWordsConfigCommand.register(dispatcher)
        }
    }
}