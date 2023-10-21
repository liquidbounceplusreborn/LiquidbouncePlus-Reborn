/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 * 
 * This code belongs to WYSI-Foundation. Please give credits when using this in your repository.
 */
package net.ccbluex.liquidbounce.features.command.commands

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.command.Command
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.client.AutoDisable.DisableEvent
import java.util.*

class AutoDisableCommand : Command("autodisable", arrayOf("ad")) {

    private val autodisableModules: List<Module>
        get() = LiquidBounce.moduleManager.modules.filter { it.autoDisables.size > 0 }
    /**
     * Execute commands with provided [args]
     */
    override fun execute(args: Array<String>) {
        if (args.size == 2) {
            when (args[1].lowercase(Locale.getDefault())) {
                "list", "l" -> {
                    if (autodisableModules.isEmpty())
                        chat("AutoDisable trigger list is empty.")
                    else {
                        chat("${autodisableModules.size} module${if (autodisableModules.size > 1) "s" else ""} with AutoDisable trigger(s):")
                        autodisableModules.forEach {
                            chat(
                                "> ${highlightModule(it)}: ${
                                    it.autoDisables.joinToString { d ->
                                        d.name.lowercase(Locale.getDefault())
                                    }
                                }"
                            )
                        }
                    }
                    return
                }

                "clear", "c" -> {
                    chat(
                        "Cleared the AutoDisable list (${autodisableModules.size} module${
                            if (autodisableModules.size > 1) "s" else ""
                        })."
                    )
                    autodisableModules.forEach {
                        it.autoDisables.clear()
                    }
                    return
                }
            }
        }
        else if (args.size > 2) {
            // Get module by name
            val module = LiquidBounce.moduleManager.getModule(args[1])

            if (module == null) {
                chat("Module ${highlightModule(args[1])} not found.")
                return
            }

            if (args[2].equals("clear", true)) {
                module.autoDisables.clear()
                chat("Removed ${highlightModule(module)} from AutoDisable trigger list.")
                playEdit()
                return
            }

            try {
                val disableWhen = DisableEvent.valueOf(args[2].toUpperCase())

                var added = "§awill now§r"
                if (module.autoDisables.contains(disableWhen)) {
                    if (module.autoDisables.remove(disableWhen)) {
                        added = "§cwill no longer§r"
                    }
                } else {
                    module.autoDisables.add(disableWhen)
                }

                val disableType = when (disableWhen) {
                    DisableEvent.FLAG -> "when you get flagged."
                    DisableEvent.WORLD_CHANGE -> "when you change the world."
                    DisableEvent.GAME_END -> "when the game end."
                }

                // Response to user
                chat("${highlightModule(module)} $added be disabled $disableType")
                playEdit()
                return
            } catch (e: IllegalArgumentException) {
                chat("§cWrong auto disable type!")
                chatSyntax("autodisable <module> <clear/flag/world_change/game_end>")
                return
            }
        }

        chatSyntax(arrayOf("list", "<module> <clear/flag/world_change/game_end>"))
    }

    override fun tabComplete(args: Array<String>): List<String> {
        if (args.isEmpty()) return emptyList()

        val moduleName = args[0]

        return when (args.size) {
            1 -> LiquidBounce.moduleManager.modules
                    .map { it.name }
                    .filter { it.startsWith(moduleName, true) }
                    .toList()

            2 -> listOf("clear", "flag", "world_change", "game_end").filter { it.startsWith(args[1], true) }
            else -> emptyList()
        }
    }

}