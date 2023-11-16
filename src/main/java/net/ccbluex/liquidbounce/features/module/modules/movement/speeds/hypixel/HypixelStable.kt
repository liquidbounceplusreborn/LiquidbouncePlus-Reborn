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
import kotlin.math.max

class HypixelStable : SpeedMode("HypixelStable") {
    
    
    override fun onMove(event: MoveEvent) {
        mc.timer.timerSpeed = 1f
        val speed = LiquidBounce.moduleManager.getModule(Speed::class.java) ?: return
        val targetStrafe = LiquidBounce.moduleManager.getModule(TargetStrafe::class.java) ?: return
        if (MovementUtils.isMoving() && !(mc.thePlayer.isInWater || mc.thePlayer.isInLava) && !mc.gameSettings.keyBindJump.isKeyDown) {
            var moveSpeed = max(MovementUtils.getBaseMoveSpeed() * speed.baseStrengthValue.get(), MovementUtils.getSpeed().toDouble())
            if (mc.thePlayer.onGround) {
                if (speed.sendJumpValue.get()) mc.thePlayer.jump()
                if (speed.recalcValue.get()) moveSpeed = max(MovementUtils.getBaseMoveSpeed() * speed.baseStrengthValue.get(), MovementUtils.getSpeed().toDouble())
                mc.thePlayer.motionY = MovementUtils.getJumpBoostModifier(if (mc.thePlayer.isCollidedHorizontally) 0.42 else speed.jumpYValue.get().toDouble())
                event.y = mc.thePlayer.motionY
                moveSpeed *= speed.moveSpeedValue.get().toDouble()
            } else if (speed.glideStrengthValue.get() > 0 && event.y < 0) {
                mc.thePlayer.motionY += speed.glideStrengthValue.get().toDouble()
                event.y = mc.thePlayer.motionY
            }
            if (targetStrafe.canStrafe) targetStrafe.strafe(event, moveSpeed) else MovementUtils.setSpeed(event, moveSpeed)
        }
    }
}
