/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.verus

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import kotlin.math.max

class VerusHard : SpeedMode("VerusHard") {
    override fun onDisable() {
        mc.timer.timerSpeed = 1f
        super.onDisable()
    }

    override fun onMotion() {
        val speed = LiquidBounce.moduleManager.getModule(Speed::class.java) ?: return
        if (!mc.gameSettings.keyBindForward.isKeyDown && !mc.gameSettings.keyBindLeft.isKeyDown && !mc.gameSettings.keyBindRight.isKeyDown && !mc.gameSettings.keyBindBack.isKeyDown) return
        mc.timer.timerSpeed = speed.verusTimer.get()
        if (mc.thePlayer.onGround) {
            mc.thePlayer.jump()
            if (mc.thePlayer.isSprinting) {
                MovementUtils.strafe(MovementUtils.getSpeed() + 0.2f)
            }
        }
        MovementUtils.strafe(max(MovementUtils.getBaseMoveSpeed().toFloat().toDouble(), MovementUtils.getSpeed().toDouble()).toFloat()) // no sprint = faster - verus, since 2018
    }

    
    
}
