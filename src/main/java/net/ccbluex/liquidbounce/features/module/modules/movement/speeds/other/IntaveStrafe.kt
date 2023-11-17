package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.other

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils

class IntaveStrafe : SpeedMode("IntaveStrafe") {
    var offGroundTicks = 0
    val speed = LiquidBounce.moduleManager.getModule(Speed::class.java)

    override fun onMotion(event: MotionEvent) {
        if(!mc.thePlayer.onGround)
            offGroundTicks++
        else offGroundTicks = 0

        if(mc.thePlayer.onGround) {
            mc.thePlayer.jump()
        }

        if (offGroundTicks >= 10) {
            MovementUtils.setMoveSpeed(MovementUtils.getSpeed().toDouble())
        }

        if(speed!!.intaveBoostTest.get()) {
            if (mc.thePlayer.motionY > 0.003) {
                mc.thePlayer.motionX *= 1.0015
                mc.thePlayer.motionZ *= 1.0015
                mc.timer.timerSpeed = 1.1f
            }
        }
    }
}