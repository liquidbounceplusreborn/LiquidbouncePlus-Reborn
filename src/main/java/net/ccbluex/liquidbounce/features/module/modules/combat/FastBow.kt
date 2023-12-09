/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.IntegerValue
import net.minecraft.item.ItemBow
import net.minecraft.network.play.client.C03PacketPlayer.C05PacketPlayerLook
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing

@ModuleInfo(name = "FastBow", spacedName = "Fast Bow", description = "Turns your bow into a machine gun.", category = ModuleCategory.COMBAT)
class FastBow : Module() {

    private val packetsValue = IntegerValue("Packets", 20, 3, 20)
    private val delay = IntegerValue("Delay", 0, 0, 500, "ms")


    val timer = MSTimer()

    var packetCount = 0

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (!mc.thePlayer.isUsingItem) {
            packetCount = 0
            return
        }

        if (mc.thePlayer.inventory.getCurrentItem() != null && mc.thePlayer.inventory.getCurrentItem().item is ItemBow) {
            if (packetCount == 0)
                PacketUtils.sendPacketNoEvent(C08PacketPlayerBlockPlacement(BlockPos.ORIGIN, 255, mc.thePlayer.currentEquippedItem, 0F, 0F, 0F))

            val yaw = if (RotationUtils.targetRotation != null)
                RotationUtils.targetRotation!!.yaw
            else
                mc.thePlayer.rotationYaw

            val pitch = if (RotationUtils.targetRotation != null)
                RotationUtils.targetRotation!!.pitch
            else
                mc.thePlayer.rotationPitch

            if (delay.get() == 0) {
                repeat (packetsValue.get()) {
                    mc.netHandler.addToSendQueue(C05PacketPlayerLook(yaw, pitch, true))
                }
                PacketUtils.sendPacketNoEvent(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN))
            } else {
                if (timer.hasTimePassed(delay.get().toLong())) {
                    packetCount++
                    mc.netHandler.addToSendQueue(C05PacketPlayerLook(yaw, pitch, true))
                    timer.reset()
                }
                if (packetCount == packetsValue.get())
                    PacketUtils.sendPacketNoEvent(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN))
            }
            mc.thePlayer.itemInUseCount = mc.thePlayer.inventory.getCurrentItem().maxItemUseDuration - 1
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) { 
        mc.thePlayer ?: return
        mc.thePlayer.inventory ?: return
        val packet = event.packet
        if (mc.thePlayer.inventory.getCurrentItem() != null && mc.thePlayer.inventory.getCurrentItem().item is ItemBow) {
            if (packet is C08PacketPlayerBlockPlacement || (packet is C07PacketPlayerDigging && packet.status == C07PacketPlayerDigging.Action.RELEASE_USE_ITEM))
                event.cancelEvent()
        }
    }
}