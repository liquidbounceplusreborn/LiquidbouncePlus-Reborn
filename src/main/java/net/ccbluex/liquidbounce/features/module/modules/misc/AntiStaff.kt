/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.NotifyType
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.utils.misc.HttpUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.minecraft.network.play.server.*
import kotlin.concurrent.thread

@ModuleInfo(
    name = "AntiStaff",
    spacedName = "Anti Staff",
    description = "Anti staff on BlocksMC. Automatically leaves a map if detected known staffs.",
    category = ModuleCategory.MISC
)
class AntiStaff : Module() {
    private val leave = BoolValue("Leave", true)

    private var staffs = mutableListOf<String>()
    private var staffsInWorld = mutableListOf<String>()
    private var detected = false

    private var staffList: String = "${LiquidBounce.CLIENT_CLOUD}/staffs.txt"

    override fun onInitialize() {
        thread {
            staffs.addAll(HttpUtils.get(staffList).split(","))
            ClientUtils.getLogger().info("[Staff/main] $staffs")
        }
    }

    override fun onEnable() {
        detected = false
        staffsInWorld.clear()
    }

    @EventTarget
    fun onWorld(e: WorldEvent) {
        detected = false
        staffsInWorld.clear()
    }

    private fun warn(name: String, reason: String) {
        if (name in staffsInWorld)
            return
        chat("[AntiStaff] ${name}: $reason")
        LiquidBounce.hud.addNotification(Notification("AntiStaff", "${name}: $reason.", NotifyType.ERROR))
        if (leave.get())
            mc.thePlayer.sendChatMessage("/leave")
        staffsInWorld.add(name)
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (mc.theWorld == null || mc.thePlayer == null) return

        val packet = event.packet // smart convert
        if (packet is S1DPacketEntityEffect) {
            val entity = mc.theWorld.getEntityByID(packet.entityId) ?: return
            if (staffs.contains(entity.name) || staffs.contains(entity.displayName.unformattedText)) {
                warn(entity.name, "effect")
            }
        }
        if (packet is S18PacketEntityTeleport) {
            val entity = mc.theWorld.getEntityByID(packet.entityId) ?: return
            if (staffs.contains(entity.name) || staffs.contains(entity.displayName.unformattedText)) {
                warn(entity.name, "teleport")
            }
        }
        if (packet is S20PacketEntityProperties) {
            val entity = mc.theWorld.getEntityByID(packet.entityId) ?: return
            if (staffs.contains(entity.name) || staffs.contains(entity.displayName.unformattedText)) {
                warn(entity.name, "properties")
            }
        }
        if (packet is S0BPacketAnimation) {
            val entity = mc.theWorld.getEntityByID(packet.entityID) ?: return
            if (staffs.contains(entity.name) || staffs.contains(entity.displayName.unformattedText)) {
                warn(entity.name, "animation")
            }
        }
        if (packet is S14PacketEntity) {
            val entity = packet.getEntity(mc.theWorld) ?: return
            if (staffs.contains(entity.name) || staffs.contains(entity.displayName.unformattedText)) {
                warn(entity.name, "update")
            }
        }
        if (packet is S19PacketEntityStatus) {
            val entity = packet.getEntity(mc.theWorld) ?: return
            if (staffs.contains(entity.name) || staffs.contains(entity.displayName.unformattedText)) {
                warn(entity.name, "status")

            }
        }
        if (packet is S19PacketEntityHeadLook) {
            val entity = packet.getEntity(mc.theWorld) ?: return
            if (staffs.contains(entity.name) || staffs.contains(entity.displayName.unformattedText)) {
                warn(entity.name, "head")
            }
        }
        if (packet is S49PacketUpdateEntityNBT) {
            val entity = packet.getEntity(mc.theWorld) ?: return
            if (staffs.contains(entity.name) || staffs.contains(entity.displayName.unformattedText)) {
                warn(entity.name, "nbt")
            }
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        for (networkPlayerInfo in mc.netHandler.playerInfoMap) {
            val networkName = ColorUtils.stripColor(EntityUtils.getName(networkPlayerInfo)) ?: continue
            if (networkName in staffs)
                warn(networkName, "tab")

        }
    }
}