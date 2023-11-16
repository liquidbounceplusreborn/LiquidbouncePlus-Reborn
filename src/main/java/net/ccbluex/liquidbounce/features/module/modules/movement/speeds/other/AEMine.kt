/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.other

import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils

class AEMine : SpeedMode("AEMine") {
    override fun onDisable() {
        mc.timer.timerSpeed = 1f
    }

    override fun onMotion() {
        if (mc.thePlayer.onGround && MovementUtils.isMoving()) {
            mc.thePlayer.jump()
            mc.timer.timerSpeed = 1f
        } else {
            mc.timer.timerSpeed = 1.30919551f
        }
    }

    
    
}