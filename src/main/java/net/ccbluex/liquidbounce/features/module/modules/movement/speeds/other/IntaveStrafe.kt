package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.other

import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils

class IntaveStrafe : SpeedMode("IntaveStrafe") {
    var offGroundTicks = 0
    override fun onMotion(event: MotionEvent) {
        mc.timer.timerSpeed = 1.2f
        if(!mc.thePlayer.onGround)
            offGroundTicks++
        else offGroundTicks = 0

        if(mc.thePlayer.onGround)
        mc.thePlayer.jump()

        if (offGroundTicks >= 10) {
            MovementUtils.setMoveSpeed(MovementUtils.getSpeed().toDouble())
        }
    }
}