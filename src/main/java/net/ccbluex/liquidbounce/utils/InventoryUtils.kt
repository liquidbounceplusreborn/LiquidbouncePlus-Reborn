/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https:Blocks.github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.utils

import net.ccbluex.liquidbounce.event.ClickWindowEvent
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.minecraft.block.BlockBush
import net.minecraft.init.Blocks
import net.minecraft.item.Item
import net.minecraft.item.ItemBlock
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement

class InventoryUtils : MinecraftInstance(), Listenable {
    @EventTarget
    fun onClick(event: ClickWindowEvent?) {
        CLICK_TIMER.reset()
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is C08PacketPlayerBlockPlacement) CLICK_TIMER.reset()
    }

    override fun handleEvents(): Boolean {
        return true
    }

    companion object {
        val CLICK_TIMER = MSTimer()
        val BLOCK_BLACKLIST = listOf(
            Blocks.enchanting_table,
            Blocks.chest,
            Blocks.ender_chest,
            Blocks.trapped_chest,
            Blocks.anvil,
            Blocks.sand,
            Blocks.web,
            Blocks.torch,
            Blocks.crafting_table,
            Blocks.furnace,
            Blocks.waterlily,
            Blocks.dispenser,
            Blocks.stone_pressure_plate,
            Blocks.wooden_pressure_plate,
            Blocks.noteblock,
            Blocks.dropper,
            Blocks.tnt,
            Blocks.standing_banner,
            Blocks.wall_banner,
            Blocks.redstone_torch,  // recently added
            Blocks.gravel,
            Blocks.cactus,
            Blocks.bed,
            Blocks.lever,
            Blocks.standing_sign,
            Blocks.wall_sign,
            Blocks.jukebox,
            Blocks.oak_fence,
            Blocks.spruce_fence,
            Blocks.birch_fence,
            Blocks.jungle_fence,
            Blocks.dark_oak_fence,
            Blocks.oak_fence_gate,
            Blocks.spruce_fence_gate,
            Blocks.birch_fence_gate,
            Blocks.jungle_fence_gate,
            Blocks.dark_oak_fence_gate,
            Blocks.nether_brick_fence,  //Blocks.cake,
            Blocks.trapdoor,
            Blocks.melon_block,
            Blocks.brewing_stand,
            Blocks.cauldron,
            Blocks.skull,
            Blocks.hopper,
            Blocks.carpet,
            Blocks.redstone_wire,
            Blocks.light_weighted_pressure_plate,
            Blocks.heavy_weighted_pressure_plate,
            Blocks.daylight_detector
        )

        fun findItem(startSlot: Int, endSlot: Int, item: Item): Int {
            for (i in startSlot until endSlot) {
                val stack = mc.thePlayer.inventoryContainer.getSlot(i).stack
                if (stack != null && stack.item === item) return i
            }
            return -1
        }

        fun hasSpaceHotbar(): Boolean {
            for (i in 36..44) {
                val itemStack = mc.thePlayer.inventoryContainer.getSlot(i).stack ?: return true
            }
            return false
        }

        fun findBlockInHotbar(): Int? {
            val player = mc.thePlayer ?: return null
            val inventory = player.inventoryContainer

            return (36..44).filter {
                val stack = inventory.getSlot(it).stack ?: return@filter false
                val block = if (stack.item is ItemBlock) (stack.item as ItemBlock).block else return@filter false

                stack.item is ItemBlock && stack.stackSize > 0 && block !in BLOCK_BLACKLIST && block !is BlockBush
            }.minByOrNull { (inventory.getSlot(it).stack.item as ItemBlock).block.isFullCube }
        }

        fun findLargestBlockStackInHotbar(): Int? {
            val player = mc.thePlayer ?: return null
            val inventory = player.inventoryContainer

            return (36..44).filter {
                val stack = inventory.getSlot(it).stack ?: return@filter false
                val block = if (stack.item is ItemBlock) (stack.item as ItemBlock).block else return@filter false

                stack.item is ItemBlock && stack.stackSize > 0 && block.isFullCube && block !in BLOCK_BLACKLIST && block !is BlockBush
            }.maxByOrNull { inventory.getSlot(it).stack.stackSize }
        }
    }
}