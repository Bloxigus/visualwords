package dev.bloxigus.onespecificvisualword.command

import com.mojang.brigadier.CommandDispatcher
import dev.bloxigus.onespecificvisualword.Config
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.network.chat.Component

object ReloadCommand {
    fun register(commandDispatcher: CommandDispatcher<FabricClientCommandSource?>) {
        commandDispatcher.register(
            ClientCommandManager.literal("vwreload")
                .executes {
                    Config.loadConfig()
                    it.source.sendFeedback(
                        Component.literal("Successfully reloaded visual words!")
                    )
                    1
                }
        )
    }
}