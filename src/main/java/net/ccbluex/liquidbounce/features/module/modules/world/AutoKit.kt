/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.world

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
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.utils.timer.TickTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.TextValue
import net.minecraft.network.play.client.*
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.network.play.server.S2DPacketOpenWindow
import net.minecraft.network.play.server.S2FPacketSetSlot
import java.util.*
import kotlin.concurrent.schedule

@ModuleInfo(name = "AutoKit", spacedName = "Auto Kit", description = "Automatically selects kits for you in BlocksMC Skywars.", category = ModuleCategory.WORLD)
class AutoKit : Module() {

    private val kitNameValue = TextValue("Kit-Name", "Armorer")

    // for easier selection
    private val kitTimeOutValue = IntegerValue("Timeout-After", 40, 40, 100, " tick")
    private val editMode: BoolValue = object : BoolValue("Edit-Mode", false) {
        override fun onChanged(oldValue: Boolean, newValue: Boolean) {
            if (newValue)
                LiquidBounce.hud.addNotification(Notification("AutoKit","Change default kit by right clicking the kit selector and select.", NotifyType.INFO))
        }
    }
    private val debugValue = BoolValue("Debug", false)

    private var clickStage = 0

    private var listening = false
    private var expectSlot = -1

    private var timeoutTimer = TickTimer()
    private var delayTimer = MSTimer()

    private fun debug(s: String) {
        if (debugValue.get()) ClientUtils.displayChatMessage("§7[§4§lAuto Kit§7] §r$s")
    }

    override fun onEnable() {
        clickStage = 0
        listening = false
        expectSlot = -1

        timeoutTimer.reset()
        delayTimer.reset()
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (clickStage == 1) { // minimum requirement in case of duplicated s2f packets
            if (!delayTimer.hasTimePassed(1000L)) return
            mc.netHandler.addToSendQueue(C09PacketHeldItemChange(expectSlot - 36))
            mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(mc.thePlayer.inventoryContainer.getSlot(expectSlot).getStack()))
            clickStage = 2
            delayTimer.reset()
            debug("clicked kit selector")
        } else if (!listening) {
            delayTimer.reset()
        }

        if (clickStage == 2) {
            timeoutTimer.update()
            if (timeoutTimer.hasTimePassed(kitTimeOutValue.get())) {
                // close the things and notify
                clickStage = 0
                listening = false
                mc.netHandler.addToSendQueue(C0DPacketCloseWindow())
                mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
                LiquidBounce.hud.addNotification(Notification("AutoKit","Kit checker timed out. Please use the right kit name.", NotifyType.ERROR))
                debug("can't find any kit with that name")
            }
        } else {
            timeoutTimer.reset()
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (!editMode.get() && listening && packet is S2DPacketOpenWindow) {
            event.cancelEvent()
            debug("listening so cancel open window packet")
            return
        }

        if (packet is C0DPacketCloseWindow && editMode.get()) {
            editMode.set(false)
            LiquidBounce.hud.addNotification(Notification("AutoKit","Edit mode aborted.", NotifyType.INFO))
            debug("abort edit mode")
            return
        }

        if (packet is S2FPacketSetSlot) {
            val item = packet.func_149174_e() ?: return
            val windowId = packet.func_149175_c()
            val slot = packet.func_149173_d()
            val itemName = item.unlocalizedName
            val displayName = item.displayName

            if (clickStage == 0 && windowId == 0 && slot >= 36 && slot <= 44 && itemName.contains("bow", true) && displayName.contains("kit selector", true)) { // dynamic for solo/teams
                if (editMode.get()) {
                    listening = true
                    debug("found item, listening to kit selection cuz of edit mode")
                    return
                } else {
                    listening = true
                    clickStage = 1
                    expectSlot = slot
                    debug("found item, sent trigger")
                    return
                }
            }

            if (clickStage == 2 && displayName.contains(kitNameValue.get(), true)) {
                timeoutTimer.reset()
                clickStage = 3
                debug("detected kit selection")
                Timer().schedule(250L) {
                    mc.netHandler.addToSendQueue(C0EPacketClickWindow(windowId, slot, 0, 0, item, 1919))
                    //mc.netHandler.addToSendQueue(C0EPacketClickWindow(windowId, slot, 0, 0, item, 1919))
                    mc.netHandler.addToSendQueue(C0DPacketCloseWindow(windowId))
                    mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
                    debug("selected")
                }
                return
            }
        }
        
        if (packet is S02PacketChat) {
            val text = packet.chatComponent.unformattedText

            if (text.contains("kit has been selected", true)) {
                if (editMode.get()) {
                    val kitName = text.replace(" kit has been selected!", "")
                    kitNameValue.set(kitName)
                    editMode.set(false)
                    clickStage = 0
                    listening = false
                    LiquidBounce.hud.addNotification(Notification("AutoKit","Successfully detected and added $kitName kit.", NotifyType.SUCCESS))
                    debug("finished detecting kit")
                    return
                } else {
                    listening = false
                    event.cancelEvent()
                    LiquidBounce.hud.addNotification(Notification("AutoKit","Successfully selected ${kitNameValue.get()} kit.", NotifyType.SUCCESS))
                    debug("finished")
                    return
                }
            }
        }
    }

    @EventTarget
    fun onWorld(event: WorldEvent) {
        clickStage = 0
        listening = false
        expectSlot = -1

        timeoutTimer.reset()
        delayTimer.reset()
    }

    override val tag: String
        get() = if (editMode.get() && listening) "Listening..." else kitNameValue.get()
}
