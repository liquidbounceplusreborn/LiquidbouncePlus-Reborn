package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.matrix

import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.minecraft.client.settings.GameSettings

class Matrix692 : SpeedMode("Matrix6.9.2") {
    private var wasTimer = false

    override fun onDisable() {
        wasTimer = false
        mc.timer.timerSpeed = 1.0f
    }

    override fun onMotion() {}

    override fun onUpdate() {
        if (wasTimer) {
            wasTimer = false
            mc.timer.timerSpeed = 1.0f
        }
        mc.thePlayer.motionY -= 0.00348
        mc.thePlayer.jumpMovementFactor = 0.026f
        mc.gameSettings.keyBindJump.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindJump)
        if (MovementUtils.isMoving() && mc.thePlayer.onGround) {
            mc.gameSettings.keyBindJump.pressed = false
            mc.timer.timerSpeed = 1.35f
            wasTimer = true
            mc.thePlayer.jump()
            MovementUtils.strafe()
        }else if (mc.thePlayer.motionY > 0.003) {
            mc.thePlayer.motionX *= 1.0010
            mc.thePlayer.motionZ *= 1.0010
            mc.timer.timerSpeed = 1.05f
        }
    }

    override fun onMove(event: MoveEvent?) {} }
