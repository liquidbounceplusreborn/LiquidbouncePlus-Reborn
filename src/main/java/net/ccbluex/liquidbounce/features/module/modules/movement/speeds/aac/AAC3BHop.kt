/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.aac

import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils

class AAC3BHop : SpeedMode("AAC3BHop") {
    private var legitJump = false
    override fun onTick() {
        mc.timer.timerSpeed = 1f
        if (mc.thePlayer.isInWater) return
        if (MovementUtils.isMoving()) {
            if (mc.thePlayer.onGround) {
                if (legitJump) {
                    mc.thePlayer.jump()
                    legitJump = false
                    return
                }
                mc.thePlayer.motionY = 0.3852
                mc.thePlayer.onGround = false
                MovementUtils.strafe(0.374f)
            } else if (mc.thePlayer.motionY < 0.0) {
                mc.thePlayer.speedInAir = 0.0201f
                mc.timer.timerSpeed = 1.02f
            } else mc.timer.timerSpeed = 1.01f
        } else {
            legitJump = true
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionZ = 0.0
        }
    }
}