/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.hypixel

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed
import net.ccbluex.liquidbounce.features.module.modules.movement.TargetStrafe
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import kotlin.math.abs
import kotlin.math.max

class HypixelBoost : SpeedMode("HypixelBoost") {
    
    
    override fun onMove(event: MoveEvent) {
        val speed = LiquidBounce.moduleManager.getModule(Speed::class.java) ?: return
        val targetStrafe = LiquidBounce.moduleManager.getModule(TargetStrafe::class.java) ?: return
        mc.timer.timerSpeed = 1f
        if (MovementUtils.isMoving() && !(mc.thePlayer.isInWater || mc.thePlayer.isInLava) && !mc.gameSettings.keyBindJump.isKeyDown) {
            var moveSpeed = max(MovementUtils.getBaseMoveSpeed() * speed.baseStrengthValue.get(), MovementUtils.getSpeed().toDouble())
            if (mc.thePlayer.onGround) {
                if (speed.sendJumpValue.get()) mc.thePlayer.jump()
                if (speed.recalcValue.get()) moveSpeed = max(MovementUtils.getBaseMoveSpeed() * speed.baseStrengthValue.get(), MovementUtils.getSpeed().toDouble())
                mc.thePlayer.motionY = MovementUtils.getJumpBoostModifier(if (mc.thePlayer.isCollidedHorizontally) 0.42F else speed.jumpYValue.get())
                event.y = mc.thePlayer.motionY
                moveSpeed *= speed.moveSpeedValue.get().toDouble()
            } else if (speed.glideStrengthValue.get() > 0 && event.y < 0) {
                mc.thePlayer.motionY += speed.glideStrengthValue.get().toDouble()
                event.y = mc.thePlayer.motionY
            }
            mc.timer.timerSpeed = max((speed.baseTimerValue.get() + abs(mc.thePlayer.motionY.toFloat().toDouble()) * speed.baseMTimerValue.get()).toDouble(), 1.0).toFloat()
            if (targetStrafe.canStrafe) targetStrafe.strafe(event, moveSpeed) else MovementUtils.setSpeed(event, moveSpeed)
        }
    }
}
