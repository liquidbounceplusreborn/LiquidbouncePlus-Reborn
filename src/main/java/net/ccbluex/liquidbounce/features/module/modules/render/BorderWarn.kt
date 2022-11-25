/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.IntegerValue

@ModuleInfo(name = "BorderWarn", description = "Warning you when the border is coming.", category = ModuleCategory.RENDER)
class BorderWarn : Module() {
    val rangeValue = IntegerValue("Range", 25, 5, 40)

    val timer = MSTimer()

    @EventTarget
    fun onUpdate(e: UpdateEvent) {
        if (mc.theWorld == null || mc.thePlayer == null) return
        val world = mc.theWorld!!
        val player = mc.thePlayer!!

        val playerDistance = world.worldBorder.getClosestDistance(player)

        if (playerDistance <= rangeValue.get()) {
            if (timer.hasTimePassed(1000L)) {
                timer.reset()

                ClientUtils.displayChatMessage("§8[§bBorder Warn§8]§r §lThe Border is §c§dCOMING!!! §r§aDistance: $playerDistance")
            }
        }
    }
}