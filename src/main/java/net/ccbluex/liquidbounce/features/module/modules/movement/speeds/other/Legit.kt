/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.other

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.InvMove
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.minecraft.client.gui.GuiChat
import net.minecraft.client.gui.GuiIngameMenu
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.settings.GameSettings

class Legit : SpeedMode("Legit") {
    override fun onMotion() {
        val invMove = LiquidBounce.moduleManager.getModule(InvMove::class.java)
        mc.gameSettings.keyBindJump.pressed = (MovementUtils.isMoving() || GameSettings.isKeyDown(mc.gameSettings.keyBindJump)) && (mc.inGameHasFocus || invMove!!.state && !(mc.currentScreen is GuiChat || mc.currentScreen is GuiIngameMenu) && mc.currentScreen !is GuiContainer)
    }

    
    
}
