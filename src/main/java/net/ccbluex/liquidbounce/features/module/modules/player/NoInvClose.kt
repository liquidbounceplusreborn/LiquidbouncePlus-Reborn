/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.minecraft.network.play.server.S2EPacketCloseWindow
import net.minecraft.client.gui.inventory.GuiInventory

@ModuleInfo(name = "NoInvClose", spacedName = "No Inv Close", description = "Stops server from closing your Inventory.", category = ModuleCategory.WORLD)
class NoInvClose : Module() {
    @EventTarget
    fun onPacket(event: PacketEvent){
        if (mc.theWorld == null || mc.thePlayer == null) return
        
        if (event.packet is S2EPacketCloseWindow && mc.currentScreen is GuiInventory)
            event.cancelEvent()
    }
}