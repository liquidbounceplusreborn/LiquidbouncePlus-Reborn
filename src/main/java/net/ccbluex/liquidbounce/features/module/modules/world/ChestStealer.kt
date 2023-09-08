/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.player.InvManager
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.NotifyType
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.utils.timer.TimerUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.inventory.Slot
import net.minecraft.item.Item
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.C0EPacketClickWindow
import net.minecraft.network.play.server.S30PacketWindowItems
import net.minecraft.util.ResourceLocation
import kotlin.random.Random


@ModuleInfo(name = "ChestStealer", spacedName = "Chest Stealer", description = "Automatically steals all items from a chest.", category = ModuleCategory.WORLD)
class ChestStealer : Module() {

    /**
     * OPTIONS
     */

    private val maxDelayValue: IntegerValue = object : IntegerValue("MaxDelay", 200, 0, 400, "ms") {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val i = minDelayValue.get()
            if (i > newValue)
                set(i)

            nextDelay = TimerUtils.randomDelay(minDelayValue.get(), get())
        }
    }

    private val minDelayValue: IntegerValue = object : IntegerValue("MinDelay", 150, 0, 400, "ms") {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val i = maxDelayValue.get()

            if (i < newValue)
                set(i)

            nextDelay = TimerUtils.randomDelay(get(), maxDelayValue.get())
        }
    }
    private val instantexploit = BoolValue("InstantExploit", false)

    private val eventModeValue = ListValue("OnEvent", arrayOf("Render3D", "Update", "MotionPre", "MotionPost"), "Render3D")

    private val takeRandomizedValue = BoolValue("TakeRandomized", false)
    private val onlyItemsValue = BoolValue("OnlyItems", false)
    private val noCompassValue = BoolValue("NoCompass", false)
    private val noDuplicateValue = BoolValue("NoDuplicateNonStackable", false)
    private val autoCloseValue = BoolValue("AutoClose", true)
    public val silenceValue = BoolValue("SilentMode", true)
    public val showStringValue = BoolValue("Silent-ShowString", false, { silenceValue.get() })
    public val stillDisplayValue = BoolValue("Silent-StillDisplay", false, { silenceValue.get() })
    private val delayOnStart = BoolValue("delayOnStart",true)

    private val autoCloseMaxDelayValue: IntegerValue = object : IntegerValue("AutoCloseMaxDelay", 0, 0, 400, "ms") {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val i = autoCloseMinDelayValue.get()
            if (i > newValue) set(i)
            nextCloseDelay = TimerUtils.randomDelay(autoCloseMinDelayValue.get(), this.get())
        }
    }

    private val autoCloseMinDelayValue: IntegerValue = object : IntegerValue("AutoCloseMinDelay", 0, 0, 400, "ms") {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val i = autoCloseMaxDelayValue.get()
            if (i < newValue) set(i)
            nextCloseDelay = TimerUtils.randomDelay(this.get(), autoCloseMaxDelayValue.get())
        }
    }

    private val closeOnFullValue = BoolValue("CloseOnFull", true)
    private val chestTitleValue = BoolValue("ChestTitle", false)

    /**
     * VALUES
     */

    private val delayTimer = MSTimer()
    private var nextDelay = TimerUtils.randomDelay(minDelayValue.get(), maxDelayValue.get())

    private val autoCloseTimer = MSTimer()
    private var nextCloseDelay = TimerUtils.randomDelay(autoCloseMinDelayValue.get(), autoCloseMaxDelayValue.get())

    public var contentReceived = 0

    public var once = false

    override fun onDisable() {
        once = false
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent?) {
        val screen = mc.currentScreen ?: return

        if (eventModeValue.get().equals("render3d", true))
            performStealer(screen)
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {

        if (mc.currentScreen !is GuiChest || mc.currentScreen == null) {
            if (delayOnStart.get())
                delayTimer.reset()
                autoCloseTimer.reset()
                return
        }

        if (instantexploit.get()) {
            if (mc.currentScreen is GuiChest) {
                val chest = mc.currentScreen as GuiChest
                val rows = chest.inventoryRows * 9
                for (i in 0 until rows) {
                    val slot = chest.inventorySlots.getSlot(i)
                    if (slot.hasStack) {
                        mc.thePlayer.sendQueue.addToSendQueue(
                            C0EPacketClickWindow(
                                chest.inventorySlots.windowId,
                                i,
                                0,
                                1,
                                slot.stack,
                                1.toShort()
                            )
                        )
                    }
                }
                mc.thePlayer.closeScreen()
            }
        }
        val screen = mc.currentScreen ?: return

        if (eventModeValue.get().equals("update", true))
            performStealer(screen)
    }

    @EventTarget
    fun onMotion(event: MotionEvent) {
        val screen = mc.currentScreen ?: return

        if (eventModeValue.get().equals("motion${event.eventState.stateName}", true))
            performStealer(screen)
    }

    fun performStealer(screen: GuiScreen) {
        if (once && screen !is GuiChest) {
            // prevent a bug where the chest suddenly closed while not finishing stealing items inside, leaving cheststealer turned on alone.
            state = false
            return
        }

        if (screen !is GuiChest || !delayTimer.hasTimePassed(nextDelay)) {
            autoCloseTimer.reset()
            return
        }

        // No Compass
        if (!once && noCompassValue.get() && mc.thePlayer.inventory.getCurrentItem()?.item?.unlocalizedName == "item.compass")
            return

        // Chest title
        if (!once && chestTitleValue.get() && (screen.lowerChestInventory == null || !screen.lowerChestInventory.name.contains(ItemStack(Item.itemRegistry.getObject(ResourceLocation("minecraft:chest"))).displayName)))
            return

        // inventory cleaner
        val inventoryCleaner = LiquidBounce.moduleManager[InvManager::class.java] as InvManager

        // Is empty?
        if (!isEmpty(screen) && !(closeOnFullValue.get() && fullInventory)) {
            autoCloseTimer.reset()

            // Randomized
            if (takeRandomizedValue.get()) {
                var noLoop = false
                do {
                    val items = mutableListOf<Slot>()

                    for (slotIndex in 0 until screen.inventoryRows * 9) {
                        val slot = screen.inventorySlots.inventorySlots[slotIndex]

                        if (slot.stack != null && (!onlyItemsValue.get() || slot.stack.item !is ItemBlock) && (!noDuplicateValue.get() || slot.stack.maxStackSize > 1 || !mc.thePlayer.inventory.mainInventory.filter { it != null && it.item != null }.map { it.item!! }.contains(slot.stack.item)) && (!inventoryCleaner.state || inventoryCleaner.isUseful(slot.stack, -1)))
                            items.add(slot)
                    }

                    val randomSlot = Random.nextInt(items.size)
                    val slot = items[randomSlot]

                    move(screen, slot)
                    if (nextDelay == 0L || delayTimer.hasTimePassed(nextDelay))
                        noLoop = true
                } while (delayTimer.hasTimePassed(nextDelay) && items.isNotEmpty() && !noLoop)
                return
            }

            // Non randomized
            for (slotIndex in 0 until screen.inventoryRows * 9) {
                val slot = screen.inventorySlots.inventorySlots[slotIndex]

                if (delayTimer.hasTimePassed(nextDelay) && slot.stack != null &&
                        (!onlyItemsValue.get() || slot.stack.item !is ItemBlock) && (!noDuplicateValue.get() || slot.stack.maxStackSize > 1 || !mc.thePlayer.inventory.mainInventory.filter { it != null && it.item != null }.map { it.item!! }.contains(slot.stack.item)) && (!inventoryCleaner.state || inventoryCleaner.isUseful(slot.stack, -1))) {
                    move(screen, slot)
                }
            }
        } else if (autoCloseValue.get() && screen.inventorySlots.windowId == contentReceived && autoCloseTimer.hasTimePassed(nextCloseDelay)) {
            mc.thePlayer.closeScreen()

            if (silenceValue.get() && !stillDisplayValue.get()) LiquidBounce.hud.addNotification(Notification("ChestStealer","Closed chest.", NotifyType.INFO))
            nextCloseDelay = TimerUtils.randomDelay(autoCloseMinDelayValue.get(), autoCloseMaxDelayValue.get())

            if (once) {
                once = false
                state = false
                return
            }
        }
    }

    @EventTarget
    private fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (packet is S30PacketWindowItems)
            contentReceived = packet.func_148911_c()
    }


    private fun move(screen: GuiChest, slot: Slot) {
        screen.handleMouseClick(slot, slot.slotNumber, 0, 1)
        delayTimer.reset()
        nextDelay = TimerUtils.randomDelay(minDelayValue.get(), maxDelayValue.get())
    }

    private fun isEmpty(chest: GuiChest): Boolean {
        val inventoryCleaner = LiquidBounce.moduleManager[InvManager::class.java] as InvManager

        for (i in 0 until chest.inventoryRows * 9) {
            val slot = chest.inventorySlots.inventorySlots[i]

            if (slot.stack != null && (!onlyItemsValue.get() || slot.stack.item !is ItemBlock) && (!noDuplicateValue.get() || slot.stack.maxStackSize > 1 || !mc.thePlayer.inventory.mainInventory.filter { it != null && it.item != null }.map { it.item!! }.contains(slot.stack.item)) && (!inventoryCleaner.state || inventoryCleaner.isUseful(slot.stack, -1)))
                return false
        }

        return true
    }

    private val fullInventory: Boolean
        get() = mc.thePlayer.inventory.mainInventory.none { it == null }

}