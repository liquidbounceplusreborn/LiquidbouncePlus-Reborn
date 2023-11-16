package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.matrix

import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils

class MatrixMultiply : SpeedMode("MatrixMultiply") {
    override fun onEnable() {
        mc.thePlayer.jumpMovementFactor = 0.02f
        mc.timer.timerSpeed = 1.0f
    }

    override fun onDisable() {
        mc.thePlayer.jumpMovementFactor = 0.02f
        mc.timer.timerSpeed = 1.0f
    }

    override fun onMotion() {
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
    }

    
    
}
