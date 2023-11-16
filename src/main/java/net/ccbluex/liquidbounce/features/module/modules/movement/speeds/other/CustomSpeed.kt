/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.other

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventState
import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import java.util.*

class CustomSpeed : SpeedMode("Custom") {
    private var groundTick = 0
    override fun onMotion(eventMotion: MotionEvent) {
        val speed = LiquidBounce.moduleManager.getModule(Speed::class.java)
        if (speed == null || eventMotion.eventState !== EventState.PRE) return
        if (MovementUtils.isMoving()) {
            mc.timer.timerSpeed = if (mc.thePlayer.motionY > 0) speed.upTimerValue.get() else speed.downTimerValue.get()
            if (mc.thePlayer.onGround) {
                if (groundTick >= speed.groundStay.get()) {
                    if (speed.doLaunchSpeedValue.get()) {
                        MovementUtils.strafe(speed.launchSpeedValue.get())
                    }
                    if (speed.yValue.get() != 0f) {
                        mc.thePlayer.motionY = speed.yValue.get().toDouble()
                    }
                } else if (speed.groundResetXZValue.get()) {
                    mc.thePlayer.motionX = 0.0
                    mc.thePlayer.motionZ = 0.0
                }
                groundTick++
            } else {
                groundTick = 0
                when (speed.strafeValue.get().lowercase(Locale.getDefault())) {
                    "strafe" -> MovementUtils.strafe(speed.speedValue.get())
                    "boost" -> MovementUtils.strafe()
                    "plus" -> MovementUtils.accelerate(speed.speedValue.get() * 0.1f)
                    "plusonlyup" -> if (mc.thePlayer.motionY > 0) {
                        MovementUtils.accelerate(speed.speedValue.get() * 0.1f)
                    } else {
                        MovementUtils.strafe()
                    }
                }
                mc.thePlayer.motionY += speed.addYMotionValue.get() * 0.03
            }
        } else if (speed.resetXZValue.get()) {
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionZ = 0.0
        }
    }

    override fun onEnable() {
        val speed = LiquidBounce.moduleManager.getModule(Speed::class.java) ?: return
        if (speed.resetXZValue.get()) {
            mc.thePlayer.motionZ = 0.0
            mc.thePlayer.motionX = mc.thePlayer.motionZ
        }
        if (speed.resetYValue.get()) mc.thePlayer.motionY = 0.0
        super.onEnable()
    }

    override fun onDisable() {
        mc.timer.timerSpeed = 1f
        super.onDisable()
    }

    
    
    
}
