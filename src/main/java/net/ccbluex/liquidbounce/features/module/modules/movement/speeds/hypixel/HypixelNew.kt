package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.hypixel

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.minecraft.potion.Potion

class HypixelNew : SpeedMode("HypixelNew") {

    override fun onMotion() {
    }

    override fun onUpdate() {
        if (mc.thePlayer.onGround && MovementUtils.isMoving()) {
            if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                MovementUtils.strafe(0.5f)
            } else {
                MovementUtils.strafe(0.44f)
            }
            mc.thePlayer.motionY = MovementUtils.getJumpBoostModifier(0.42F, true)
        }
    }

    override fun onMove(event: MoveEvent) {

    }
}
