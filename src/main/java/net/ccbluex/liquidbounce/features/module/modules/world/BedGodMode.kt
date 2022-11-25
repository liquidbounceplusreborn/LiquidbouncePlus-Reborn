/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo

@ModuleInfo(name = "BedGodMode", spacedName = "Bed God Mode", description = "Allows you to walk around lying in a bed. (For 1.9)", category = ModuleCategory.WORLD)
class BedGodMode : Module() {

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (!mc.thePlayer.isPlayerSleeping)
            return

        mc.thePlayer.sleeping = false
        mc.thePlayer.sleepTimer = 0
    }

}