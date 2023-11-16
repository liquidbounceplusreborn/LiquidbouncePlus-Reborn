/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.other

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils

class Jump : SpeedMode("Jump") {
    
    override fun onUpdate() {
        val speed = LiquidBounce.moduleManager.getModule(Speed::class.java) ?: return
        if (MovementUtils.isMoving() && mc.thePlayer.onGround && !mc.gameSettings.keyBindJump.isKeyDown && !(mc.thePlayer.isInWater || mc.thePlayer.isInLava) && mc.thePlayer.jumpTicks == 0) {
            mc.thePlayer.jump()
            mc.thePlayer.jumpTicks = 10
        }
        if (speed.jumpStrafe.get() && MovementUtils.isMoving() && !mc.thePlayer.onGround && !(mc.thePlayer.isInWater || mc.thePlayer.isInLava)) MovementUtils.strafe()
    }

    
}
