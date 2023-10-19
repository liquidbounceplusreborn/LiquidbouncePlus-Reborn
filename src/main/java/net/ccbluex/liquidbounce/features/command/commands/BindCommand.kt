/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.command.commands

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.command.Command
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.NotifyType
import org.lwjgl.input.Keyboard
import java.util.*

class BindCommand : Command("bind", emptyArray()) {
    /**
     * Execute commands with provided [args]
     */
    override fun execute(args: Array<String>) {
        if (args.size > 2) {
            val module = LiquidBounce.moduleManager.getModule(args[1])

            if (module == null) {
                chat("Module ${highlightModule(args[1])} not found.")
                return
            }

            val key = Keyboard.getKeyIndex(args[2].uppercase())
            val keyName = Keyboard.getKeyName(key)
            module.keyBind = key

            if (key == Keyboard.KEY_NONE) {
                chat("Removed ${highlightModule(module)}'s bind.")
                LiquidBounce.hud.addNotification(Notification("Bind","Removed ${module.name}'s bind", NotifyType.SUCCESS))
            } else {
                chat("${highlightModule(module)} is now bound to §9$keyName.")
                LiquidBounce.hud.addNotification(Notification("Bind","${module.name} bound to $keyName", NotifyType.SUCCESS))
            }

            playEdit()
            return
        }

        chatSyntax(arrayOf("<module> <key>", "<module> none"))
    }

    override fun tabComplete(args: Array<String>): List<String> {
        if (args.isEmpty()) return emptyList()

        val moduleName = args[0]

        return when (args.size) {
            1 -> LiquidBounce.moduleManager.modules
                .map { it.name }
                .filter { it.startsWith(moduleName, true) }
                .toList()
            else -> emptyList()
        }
    }
}