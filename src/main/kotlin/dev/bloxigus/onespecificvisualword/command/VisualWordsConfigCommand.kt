package dev.bloxigus.onespecificvisualword.command

import com.mojang.brigadier.CommandDispatcher
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.network.chat.Component

object VisualWordsConfigCommand {
    fun register(commandDispatcher: CommandDispatcher<FabricClientCommandSource?>) {
        commandDispatcher.register(
            ClientCommandManager.literal("vw")
                .executes {
                    it.source.sendFeedback(
                        Component.literal("open config editor")
                    )
                    1
                }
        )
    }
}