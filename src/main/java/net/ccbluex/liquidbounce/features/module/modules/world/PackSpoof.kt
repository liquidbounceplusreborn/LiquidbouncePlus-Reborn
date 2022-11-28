/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.minecraft.network.play.client.C19PacketResourcePackStatus
import net.minecraft.network.play.server.S48PacketResourcePackSend
import java.net.URI
import java.net.URISyntaxException
import java.io.File

@ModuleInfo(name = "PackSpoof", spacedName = "Pack Spoof", description = "Prevents servers from forcing you to download their resource pack.", category = ModuleCategory.WORLD)
class PackSpoof : Module() {

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        val antiExploit = LiquidBounce.moduleManager[AntiExploit::class.java]!! as AntiExploit

        if (packet is S48PacketResourcePackSend) {
            val url = packet.url
            val hash = packet.hash

            try {
                val scheme = URI(url).scheme
                val isLevelProtocol = "level" == scheme

                if ("http" != scheme && "https" != scheme && !isLevelProtocol)
                    throw URISyntaxException(url, "Wrong protocol")

                if (isLevelProtocol && (url.contains("..") || !url.endsWith(".zip"))) {
                    val s2 = url.substring("level://".length)
                    val file1 = File(mc.mcDataDir, "saves")
                    val file2 = File(file1, s2)

                    if (!file2.isFile() || url.contains("liquidbounce", true)) {
                        if (antiExploit.state && antiExploit.notifyValue.get()) {
                            ClientUtils.displayChatMessage("§8[§9§lLiquidBounce+§8] §6Resourcepack exploit detected.")
                            ClientUtils.displayChatMessage("§8[§9§lLiquidBounce+§8] §7Exploit target directory: §r$url")

                            throw URISyntaxException(url, "Invalid levelstorage resourcepack path")
                        } else {
                            mc.netHandler.addToSendQueue(C19PacketResourcePackStatus(hash, C19PacketResourcePackStatus.Action.FAILED_DOWNLOAD))
                            event.cancelEvent()
                            return
                        }
                    }
                }

                mc.netHandler.addToSendQueue(C19PacketResourcePackStatus(packet.hash,
                    C19PacketResourcePackStatus.Action.ACCEPTED))
                mc.netHandler.addToSendQueue(C19PacketResourcePackStatus(packet.hash,
                    C19PacketResourcePackStatus.Action.SUCCESSFULLY_LOADED))
            } catch (e: URISyntaxException) {
                ClientUtils.getLogger().error("Failed to handle resource pack", e)
                mc.netHandler.addToSendQueue(C19PacketResourcePackStatus(hash, C19PacketResourcePackStatus.Action.FAILED_DOWNLOAD))
            }

            event.cancelEvent()
        }
    }

}