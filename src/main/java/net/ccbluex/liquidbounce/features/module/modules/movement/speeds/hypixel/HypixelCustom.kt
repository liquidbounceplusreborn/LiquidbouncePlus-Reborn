package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.hypixel

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.features.module.modules.world.GameSpeed
import net.ccbluex.liquidbounce.features.module.modules.world.Scaffold
import net.ccbluex.liquidbounce.utils.MovementUtils.*
import kotlin.math.max

class HypixelCustom: SpeedMode("HypixelCustom") {

    override fun onJump(event: JumpEvent) {
        if (mc.thePlayer != null && isMoving())
            event.cancelEvent()
    }

    override fun onUpdate() {}

    override fun onMotion() {}

    override fun onMotion(event: MotionEvent) {
        val thePlayer = mc.thePlayer ?: return

        val speedModule = (LiquidBounce.moduleManager.getModule(Speed::class.java) as Speed?)!!
        val scaffoldModule = LiquidBounce.moduleManager.getModule(Scaffold::class.java)
        val timer = LiquidBounce.moduleManager.getModule(GameSpeed::class.java)

        if (isMoving()) {
            when {
                thePlayer.onGround && thePlayer.isCollidedVertically -> {
                    thePlayer.motionY = getJumpBoostModifier(if (scaffoldModule!!.state) 0.41999 else speedModule.motionYValue.get().toDouble(), true)

                    if (scaffoldModule.state) {
                        strafe(0.37F)
                    } else {
                        strafe((max(speedModule.customSpeedValue.get() + getSpeedEffect() * 0.1, getBaseMoveSpeed(0.2873))).toFloat())
                    }
                }

                else -> {
                    if (!timer!!.state && speedModule.timerValue.get())
                        mc.timer.timerSpeed = 1.07f

                    setMotion(getSpeed().toDouble(), speedModule.smoothStrafe.get())
                }
            }
        } else {
            thePlayer.motionX *= 0.0
            thePlayer.motionZ *= 0.0
        }
        if (!(mc.thePlayer!!.movementInput.moveForward != 0F || mc.thePlayer!!.movementInput.moveStrafe != 0F)) {
                mc.thePlayer!!.motionX = 0.0
                mc.thePlayer!!.motionZ = 0.0
            return
        }
    }

    override fun onMove(event: MoveEvent) {
        val thePlayer = mc.thePlayer ?: return
        val speedModule = (LiquidBounce.moduleManager.getModule(Speed::class.java) as Speed?)!!

        if (isMoving()) {
            when {
                thePlayer.isCollidedHorizontally -> {
                    setMotion(event, getBaseMoveSpeed(0.258), 1.0, speedModule.smoothStrafe.get())
                }
            }
        }
    }
    
}