/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.ClickBlockEvent
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.value.BoolValue
import net.minecraft.network.play.client.C09PacketHeldItemChange
import net.minecraft.util.BlockPos

@ModuleInfo(name = "AutoTool", spacedName = "Auto Tool", description = "Automatically selects the best tool in your inventory to mine a block.", category = ModuleCategory.PLAYER)
class AutoTool : Module() {
    private var bestSlot = -1
    private val silent = BoolValue("Silent", false)
    private val nousing = BoolValue("NoPlayerUsing", false)

    @EventTarget
    fun onClick(event: ClickBlockEvent) {
        switchSlot(event.clickedBlock ?: return)
    }

    fun switchSlot(blockPos: BlockPos) {
        if (!nousing.get() || !mc.thePlayer.isUsingItem) {
            var bestSpeed = 1F

            val block = mc.theWorld.getBlockState(blockPos).block

            for (i in 0..8) {
                val item = mc.thePlayer.inventory.getStackInSlot(i) ?: continue
                val speed = item.getStrVsBlock(block)

                if (speed > bestSpeed) {
                    bestSpeed = speed
                    bestSlot = i
                }
            }

            if (bestSlot != -1) {
                if (!silent.get()) {
                    mc.thePlayer.inventory.currentItem = bestSlot
                } else {
                    mc.netHandler.addToSendQueue(C09PacketHeldItemChange(bestSlot))
                    mc.playerController.updateController()
                }
            }
        }
    }
}