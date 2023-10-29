package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.value.BoolValue
import net.minecraft.init.Items
import net.minecraft.network.play.server.S3FPacketCustomPayload

@ModuleInfo(name = "AntiBook", description = "Automatically close book", category = ModuleCategory.MISC)
class AntiBook: Module() {
    private val allowRightClick = BoolValue("AllowRightClick", true)

    private val bookItems = arrayOf(Items.book, Items.enchanted_book, Items.writable_book, Items.written_book)

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (allowRightClick.get() && mc.thePlayer.inventory.getCurrentItem() != null
            && mc.thePlayer.inventory.getCurrentItem().item in bookItems)
            return

        if (packet is S3FPacketCustomPayload && packet.channelName == "MC|BOpen")
            event.cancelEvent()
    }
}