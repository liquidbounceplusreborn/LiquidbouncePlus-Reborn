package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.matrix

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils

class MatrixDynamic : SpeedMode("MatrixDynamic") {
    var ka = LiquidBounce.moduleManager.getModule(KillAura::class.java)
    override fun onEnable() {
        mc.thePlayer.jumpMovementFactor = 0.02f
        mc.timer.timerSpeed = 1.0f
    }

    override fun onDisable() {
        mc.thePlayer.jumpMovementFactor = 0.02f
        mc.timer.timerSpeed = 1.0f
    }

    override fun onMotion() {
        if (!ka!!.hitable) {
            if (!MovementUtils.isMoving()) {
                return
            }
            if (mc.thePlayer.onGround) {
                mc.timer.timerSpeed = 1.0f
                mc.thePlayer.jump()
            }
            if (mc.thePlayer.motionY > 0.003) {
                mc.thePlayer.motionX *= 1.0012
                mc.thePlayer.motionZ *= 1.0012
                mc.timer.timerSpeed = 1.05f
            }
        } else {
            if (MovementUtils.isMoving() && mc.thePlayer.onGround) {
                mc.thePlayer.jump()
                MovementUtils.strafe(0.3f)
            }
            if (mc.thePlayer.fallDistance > 0.1) {
                MovementUtils.strafe(0.22f)
            }
        }
    }
}
