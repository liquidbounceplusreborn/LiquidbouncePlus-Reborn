/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speeds

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.JumpEvent
import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed
import net.ccbluex.liquidbounce.utils.MinecraftInstance

abstract class SpeedMode(val modeName: String) : MinecraftInstance() {
    val isActive: Boolean
        get() {
            val speed = LiquidBounce.moduleManager.getModule(Speed::class.java)
            return speed != null && !mc.thePlayer.isSneaking && speed.state && speed.modeName == modeName
        }

    open fun onMotion() {}
    open fun onMotion(eventMotion: MotionEvent) {}
    open fun onUpdate() {}
    open fun onMove(event: MoveEvent) {}
    open fun onPacket(eventPacket: PacketEvent) {}
    open fun onJump(event: JumpEvent) {}
    open fun onTick() {}
    open fun onEnable() {}
    open fun onDisable() {}
}
