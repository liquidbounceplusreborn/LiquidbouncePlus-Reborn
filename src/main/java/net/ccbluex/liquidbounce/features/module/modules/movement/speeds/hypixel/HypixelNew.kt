package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.hypixel

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.minecraft.potion.Potion

class HypixelNew : SpeedMode("HypixelNew") {

    private var oldMotionX = 0.0
    private var oldMotionZ = 0.0
    private var watchdogMultiplier = 1.0

    override fun onMotion() {
    }

    override fun onUpdate() {
        val speedModule = (LiquidBounce.moduleManager.getModule(Speed::class.java) as Speed)!!
        if (mc.thePlayer.onGround) {
            mc.thePlayer.jump()
            mc.thePlayer.motionY = speedModule.jumpYValue.get().toDouble()

            oldMotionX = mc.thePlayer.motionX
            oldMotionZ = mc.thePlayer.motionZ
            MovementUtils.strafe(MovementUtils.getSpeed() * 1.01f)
            mc.thePlayer.motionX = (mc.thePlayer.motionX * 1 + oldMotionX * 2) / 3
            mc.thePlayer.motionZ = (mc.thePlayer.motionZ * 1 + oldMotionZ * 2) / 3

            if (MovementUtils.getSpeed() < 0.47) {
                watchdogMultiplier = 0.47 / (MovementUtils.getSpeed().toDouble() + 0.001)
                mc.thePlayer.motionX *= watchdogMultiplier
                mc.thePlayer.motionZ *= watchdogMultiplier
            }
            if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                mc.thePlayer.motionX *= (1.0 + speedModule.customSpeedBoost.get().toDouble() * (mc.thePlayer.getActivePotionEffect(
                    Potion.moveSpeed).amplifier + 1))
                mc.thePlayer.motionZ *= (1.0 + speedModule.customSpeedBoost.get().toDouble() * (mc.thePlayer.getActivePotionEffect(
                    Potion.moveSpeed).amplifier + 1))
            }
        }
    }

    override fun onMove(event: MoveEvent?) {
    }
}
