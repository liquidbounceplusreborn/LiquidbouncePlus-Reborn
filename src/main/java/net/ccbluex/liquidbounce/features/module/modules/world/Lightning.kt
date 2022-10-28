/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.NotifyType
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.minecraft.network.play.server.S2CPacketSpawnGlobalEntity

import kotlin.math.roundToInt

@ModuleInfo(name = "Lightning", description = "Checks for lightning spawn and notify you.", category = ModuleCategory.WORLD)
class Lightning : Module() {
    val chatValue = BoolValue("Chat", true)
    val notifValue = BoolValue("Notification", false)

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is S2CPacketSpawnGlobalEntity && packet.func_149053_g() == 1) {
            val x = packet.func_149051_d() / 32.0
            val y = packet.func_149050_e() / 32.0
            val z = packet.func_149049_f() / 32.0
            val dist = mc.thePlayer.getDistance(x, mc.thePlayer.entityBoundingBox.minY, z).roundToInt()

            if (chatValue.get())
                ClientUtils.displayChatMessage("§7[§6§lLightning§7] §fDetected lightning at §a$x $y $z §7($dist blocks away)")
            
            if (notifValue.get())
                LiquidBounce.hud.addNotification(Notification("Lightning","Lightning [$x $y $z] ($dist blocks away)", NotifyType.WARNING, 3000))
        }
    }
}