/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.injection.access.StaticStorage
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.utils.InventoryUtils
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.utils.block.BlockUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.minecraft.block.BlockAir
import net.minecraft.entity.Entity
import net.minecraft.init.Items
import net.minecraft.item.ItemBucket
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.*
import net.minecraft.network.play.client.C09PacketHeldItemChange
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.MathHelper
import net.minecraft.util.Vec3

import java.lang.Math.*

@ModuleInfo(name = "Ignite", description = "Automatically sets targets around you on fire.", category = ModuleCategory.COMBAT)
class Ignite : Module() {

    private val lighterValue = BoolValue("Lighter", true)
    private val lavaBucketValue = BoolValue("Lava", true)

    private val msTimer = MSTimer()

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (!msTimer.hasTimePassed(500L))
            return
        
        val lighterInHotbar = if (lighterValue.get()) InventoryUtils.findItem(36, 45, Items.flint_and_steel) else -1
        val lavaInHotbar = if (lavaBucketValue.get()) InventoryUtils.findItem(36, 45, Items.lava_bucket) else -1

        if (lighterInHotbar == -1 && lavaInHotbar == -1)
            return

        val fireInHotbar = if (lighterInHotbar != -1) lighterInHotbar else lavaInHotbar

        for (entity in mc.theWorld.loadedEntityList)
            if (EntityUtils.isSelected(entity, true) && !entity.isBurning()) {
                val blockPos = entity.position

                if (mc.thePlayer.getDistanceSq(blockPos) >= 22.3 ||
                    !BlockUtils.isReplaceable(blockPos) ||
                    !(BlockUtils.getBlock(blockPos) is BlockAir))
                    continue

                RotationUtils.keepCurrentRotation = true
                mc.netHandler.addToSendQueue(C09PacketHeldItemChange(fireInHotbar - 36))
                val itemStack = mc.thePlayer.inventoryContainer.getSlot(fireInHotbar).stack ?: return

                if (itemStack.item!! is ItemBucket) {
                    val diffX = blockPos.x + 0.5 - mc.thePlayer.posX
                    val diffY = blockPos.y + 0.5 - (mc.thePlayer.entityBoundingBox.minY + mc.thePlayer.eyeHeight)
                    val diffZ = blockPos.z + 0.5 - mc.thePlayer.posZ
                    
                    val sqrtz = sqrt(diffX * diffX + diffZ * diffZ)
                    val yaw = (atan2(diffZ, diffX) * 180.0 / PI).toFloat() - 90F
                    val pitch = -(atan2(diffY, sqrtz) * 180.0 / PI).toFloat()

                    mc.netHandler.addToSendQueue(C05PacketPlayerLook(
                        mc.thePlayer.rotationYaw + MathHelper.wrapAngleTo180_float(yaw - mc.thePlayer.rotationYaw),
                        mc.thePlayer.rotationPitch + MathHelper.wrapAngleTo180_float(pitch - mc.thePlayer.rotationPitch),
                        mc.thePlayer.onGround))

                    mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, itemStack)
                } else {
                    for (side in StaticStorage.facings()) {
                        val neighbor = blockPos.offset(side)

                        if (!BlockUtils.canBeClicked(neighbor)) continue

                        val diffX = neighbor.x + 0.5 - mc.thePlayer.posX
                        val diffY = neighbor.y + 0.5 - (mc.thePlayer.entityBoundingBox.minY + mc.thePlayer.eyeHeight)
                        val diffZ = neighbor.z + 0.5 - mc.thePlayer.posZ

                        val sqrtz = sqrt(diffX * diffX + diffZ * diffZ)
                        val yaw = (atan2(diffZ, diffX) * 180.0 / PI).toFloat() - 90F
                        val pitch = -(atan2(diffY, sqrtz) * 180.0 / PI).toFloat()

                        mc.netHandler.addToSendQueue(C05PacketPlayerLook(
                            mc.thePlayer.rotationYaw +
                            MathHelper.wrapAngleTo180_float(yaw - mc.thePlayer.rotationYaw),
                            mc.thePlayer.rotationPitch +
                            MathHelper.wrapAngleTo180_float(pitch - mc.thePlayer.rotationPitch),
                            mc.thePlayer.onGround))

                        if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, itemStack, neighbor,
                                                side.opposite, Vec3(side.directionVec))) {
                            mc.thePlayer.swingItem()
                            break
                        }
                    }
                }

                mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
                RotationUtils.keepCurrentRotation = false
                mc.netHandler.addToSendQueue(C05PacketPlayerLook(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, mc.thePlayer.onGround))

                msTimer.reset()
                break
            }
    }

}
