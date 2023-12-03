/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.aac

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.minecraft.client.settings.GameSettings
import net.minecraft.network.play.server.S12PacketEntityVelocity

class AACHop3310 : SpeedMode("AACHop3.3.10") {
    
    
    override fun onMove(event: MoveEvent) {
        val speed = LiquidBounce.moduleManager.getModule(Speed::class.java) ?: return
        val player = mc.thePlayer
        mc.gameSettings.keyBindJump.pressed = false
        MovementUtils.strafe((MovementUtils.getBaseMoveSpeed() * 1.0164f).toFloat())
        if (mc.thePlayer.onGround && mc.thePlayer.isCollidedVertically && GameSettings.isKeyDown(mc.gameSettings.keyBindSneak)) {
            player.motionY = MovementUtils.getJumpBoostModifier(0.42F)
            event.y = player.motionY
        }
        if (mc.thePlayer.onGround && mc.thePlayer.isCollidedVertically && !GameSettings.isKeyDown(mc.gameSettings.keyBindSneak)) {
            player.motionY = MovementUtils.getJumpBoostModifier(0.42F)
            event.y = player.motionY
        }
    }

    
    override fun onPacket(event: PacketEvent) {
        if (event.packet is S12PacketEntityVelocity) {
            if (mc.thePlayer.onGround && mc.thePlayer.isSneaking && MovementUtils.isMoving()) return
            event.cancelEvent()
        }
    }
}
