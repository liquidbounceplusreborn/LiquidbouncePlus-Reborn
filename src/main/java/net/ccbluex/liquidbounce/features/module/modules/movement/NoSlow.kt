package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.item.*
import net.minecraft.network.Packet
import net.minecraft.network.play.INetHandlerPlayServer
import net.minecraft.network.play.client.*
import net.minecraft.network.play.server.S2FPacketSetSlot
import net.minecraft.network.play.server.S30PacketWindowItems
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.MovingObjectPosition.MovingObjectType

@ModuleInfo(name = "NoSlow", spacedName = "No Slow", category = ModuleCategory.MOVEMENT, description = "Prevent you from getting slowed down by items (swords, foods, etc.) and liquids.")
class NoSlow : Module() {
    private val sword = BoolValue("Sword", false)
    private val swordMode = ListValue(
        "SwordMode",
        arrayOf("Vanilla", "AAC5", "SwitchItem", "ReverseEventSwitchItem", "OldIntave","Bug","GrimPost"), "Vanilla") { sword.get() } //grimpost work on hyt?idk
    private val blockForwardMultiplier = FloatValue("BlockForwardMultiplier", 1.0F, 0.2F, 1.0F, "x")
    private val blockStrafeMultiplier = FloatValue("BlockStrafeMultiplier", 1.0F, 0.2F, 1.0F, "x")

    private val bow = BoolValue("Bow", false)
    private val bowMode = ListValue(
        "BowMode",
        arrayOf("Vanilla", "SwitchItem", "ReverseEventSwitchItem", "OldIntave"),
        "Vanilla"
    ) { bow.get() }
    private val bowForwardMultiplier = FloatValue("BowForwardMultiplier", 1.0F, 0.2F, 1.0F, "x")
    private val bowStrafeMultiplier = FloatValue("BowStrafeMultiplier", 1.0F, 0.2F, 1.0F, "x")

    private val consume = BoolValue("Consume", false)
    private val consumeMode = ListValue(
        "ConsumeMode",
        arrayOf("Vanilla", "SwitchItem", "ReverseEventSwitchItem", "OldIntave", "Bug","Intave","Grim"),
        "Vanilla"
    ) { consume.get() }

    private val consumeForwardMultiplier = FloatValue("ConsumeForwardMultiplier", 1.0F, 0.2F, 1.0F, "x")
    private val consumeStrafeMultiplier = FloatValue("ConsumeStrafeMultiplier", 1.0F, 0.2F, 1.0F, "x")

    private val ciucValue = BoolValue("CheckInUseCount", true) { consumeMode.get() == "Blink" }
    private val packetTriggerValue =
        ListValue("PacketTrigger", arrayOf("PreRelease", "PostRelease"), "PostRelease") { consumeMode.get() == "Blink" }
    private val debugValue = BoolValue("Debug", false) { consumeMode.get() == "Blink" }

    val soulsandValue = BoolValue("Soulsand", true)
    val liquidPushValue = BoolValue("LiquidPush", true)

    private val msTimer = MSTimer()
    private val blinkPackets = mutableListOf<Packet<INetHandlerPlayServer>>()
    private var lastX = 0.0
    private var lastY = 0.0
    private var lastZ = 0.0
    private var lastOnGround = false

    private var lastItem: ItemStack? = null
    private var count = 0

    override fun onEnable() {
        blinkPackets.clear()
        msTimer.reset()
    }

    override fun onDisable() {
        blinkPackets.forEach {
            PacketUtils.sendPacketNoEvent(it)
        }
        blinkPackets.clear()
    }

    @EventTarget
    fun onMotion(event: MotionEvent) {
        val player = mc.thePlayer ?: return
        val heldItem = player.heldItem ?: return
        val currentItem = player.inventory.currentItem
        val isUsingItem =
            mc.thePlayer?.heldItem != null && (mc.thePlayer.isUsingItem || (mc.thePlayer.heldItem?.item is ItemSword && LiquidBounce.moduleManager[KillAura::class.java]?.blockingStatus == true))

        if (mc.thePlayer.motionX == 0.0 && mc.thePlayer.motionZ == 0.0)
            return

        if (sword.get() && heldItem.item is ItemSword && isUsingItem) {
            when (swordMode.get()) {
                "AAC5" -> {
                    if (event.eventState == EventState.POST) {
                        mc.netHandler.addToSendQueue(
                            C08PacketPlayerBlockPlacement(
                                BlockPos(-1, -1, -1), 255, player.heldItem, 0f, 0f, 0f
                            )
                        )
                    }
                }

                "SwitchItem" -> {
                    if (event.eventState == EventState.PRE) {
                        mc.netHandler.addToSendQueue(C09PacketHeldItemChange((mc.thePlayer.inventory.currentItem + 1) % 9))
                        mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
                    }
                }

                "ReverseEventSwitchItem" -> {
                    if (event.eventState == EventState.PRE) {
                        mc.netHandler.addToSendQueue(C09PacketHeldItemChange((mc.thePlayer.inventory.currentItem + 1) % 9))
                    } else {
                        mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
                    }
                }

                "OldIntave" -> {
                    if (event.eventState == EventState.PRE) {
                        mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem % 8 + 1))
                        mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
                    }
                    if (event.eventState == EventState.POST) {
                        mc.netHandler.addToSendQueue(
                            C08PacketPlayerBlockPlacement(
                                mc.thePlayer.inventoryContainer.getSlot(
                                    mc.thePlayer.inventory.currentItem + 36
                                ).stack
                            )
                        )
                    }
                }

                "GrimPost" -> {
                    mc.netHandler.addToSendQueue(C0FPacketConfirmTransaction())
                    mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(BlockPos(-1, -1, -1), 255, mc.thePlayer!!.inventory.getCurrentItem(), 0.0F, 0.0F, 0.0F))
                    if (event.eventState == EventState.PRE) {
                        mc.netHandler.addToSendQueue(
                            C07PacketPlayerDigging(
                                C07PacketPlayerDigging.Action.RELEASE_USE_ITEM,
                                BlockPos.ORIGIN,
                                EnumFacing.DOWN
                            )
                        )
                    }
                }
            }
        }

        if (bow.get() && heldItem.item is ItemBow && isUsingItem) {
            when (bowMode.get()) {
                "SwitchItem" -> {
                    if (event.eventState == EventState.PRE) {
                        mc.netHandler.addToSendQueue(C09PacketHeldItemChange((mc.thePlayer.inventory.currentItem + 1) % 9))
                        mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
                    }
                }

                "ReverseEventSwitchItem" -> {
                    if (event.eventState == EventState.PRE) {
                        mc.netHandler.addToSendQueue(C09PacketHeldItemChange((mc.thePlayer.inventory.currentItem + 1) % 9))
                    } else {
                        mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
                    }
                }

                "OldIntave" -> {
                    if (event.eventState == EventState.PRE) {
                        mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem % 8 + 1))
                        mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
                    }
                    if (event.eventState == EventState.POST) {
                        mc.netHandler.addToSendQueue(
                            C08PacketPlayerBlockPlacement(
                                mc.thePlayer.inventoryContainer.getSlot(
                                    mc.thePlayer.inventory.currentItem + 36
                                ).stack
                            )
                        )
                    }
                }
            }
        }

        if (consume.get() && (heldItem.item is ItemFood || heldItem.item is ItemPotion || heldItem.item is ItemBucketMilk) && isUsingItem) {
            when (consumeMode.get()) {
                "SwitchItem" -> {
                    if (event.eventState == EventState.PRE) {
                        mc.netHandler.addToSendQueue(C09PacketHeldItemChange((mc.thePlayer.inventory.currentItem + 1) % 9))
                        mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
                    }
                }

                "ReverseEventSwitchItem" -> {
                    if (event.eventState == EventState.PRE) {
                        mc.netHandler.addToSendQueue(C09PacketHeldItemChange((mc.thePlayer.inventory.currentItem + 1) % 9))
                    } else {
                        mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
                    }
                }

                "OldIntave" -> {
                    if (event.eventState == EventState.PRE) {
                        mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem % 8 + 1))
                        mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
                    }
                    if (event.eventState == EventState.POST) {
                        mc.netHandler.addToSendQueue(
                            C08PacketPlayerBlockPlacement(
                                mc.thePlayer.inventoryContainer.getSlot(
                                    mc.thePlayer.inventory.currentItem + 36
                                ).stack
                            )
                        )
                    }
                }

                "Blink" -> {
                    if (event.eventState == EventState.PRE && !mc.thePlayer.isUsingItem && !mc.thePlayer.isBlocking) {
                        lastX = event.x
                        lastY = event.y
                        lastZ = event.z
                        lastOnGround = event.onGround
                        if (blinkPackets.size > 0 && packetTriggerValue.get().equals("postrelease", true)) {
                            blinkPackets.forEach {
                                PacketUtils.sendPacketNoEvent(it)
                            }
                            if (debugValue.get())
                                ClientUtils.displayChatMessage("sent ${blinkPackets.size} packets.")
                            blinkPackets.clear()
                        }
                    }
                }

                "Intave" -> {
                    mc.netHandler.addToSendQueue(C09PacketHeldItemChange((mc.thePlayer.inventory.currentItem + 1) % 9))
                    mc.netHandler.addToSendQueue(
                        C07PacketPlayerDigging(
                            C07PacketPlayerDigging.Action.DROP_ITEM,
                            BlockPos(-1, -1, -1), EnumFacing.DOWN
                        )
                    )
                    mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
                }

                "Grim" -> {
                    if (event.eventState == EventState.PRE) {
                        mc.netHandler.addToSendQueue(
                            C0EPacketClickWindow(
                                0,
                                36,
                                0,
                                2,
                                ItemStack(Block.getBlockById(166)),
                                0
                            )
                        )
                    }
                }
            }
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        mc.thePlayer ?: return
        mc.thePlayer.heldItem ?: return
        val packet = event.packet
        val killAura = LiquidBounce.moduleManager[KillAura::class.java]!!

        if (consumeMode.get() == "Blink" && !(killAura.state && killAura.blockingStatus) && (mc.thePlayer.heldItem.item is ItemFood || mc.thePlayer.heldItem.item is ItemPotion || mc.thePlayer.heldItem.item is ItemBucketMilk) && mc.thePlayer.isUsingItem) {
            val item = mc.thePlayer.itemInUse.item
            if (mc.thePlayer.isUsingItem && (item is ItemFood || item is ItemBucketMilk || item is ItemPotion) && (!ciucValue.get() || mc.thePlayer.itemInUseCount >= 1)) {
                if (packet is C03PacketPlayer.C04PacketPlayerPosition || packet is C03PacketPlayer.C06PacketPlayerPosLook) {
                    if (mc.thePlayer.positionUpdateTicks >= 20 && packetTriggerValue.get()
                            .equals("postrelease", true)
                    ) {
                        (packet as C03PacketPlayer).x = lastX
                        packet.y = lastY
                        packet.z = lastZ
                        packet.onGround = lastOnGround
                        if (debugValue.get())
                            ClientUtils.displayChatMessage("pos update reached 20")
                    } else {
                        event.cancelEvent()
                        if (packetTriggerValue.get().equals("postrelease", true))
                            PacketUtils.sendPacketNoEvent(C03PacketPlayer(lastOnGround))
                        blinkPackets.add(packet as Packet<INetHandlerPlayServer>)
                        if (debugValue.get())
                            ClientUtils.displayChatMessage("packet player (movement) added at ${blinkPackets.size - 1}")
                    }
                } else if (packet is C03PacketPlayer.C05PacketPlayerLook) {
                    event.cancelEvent()
                    if (packetTriggerValue.get().equals("postrelease", true))
                        PacketUtils.sendPacketNoEvent(C03PacketPlayer(lastOnGround))
                    blinkPackets.add(packet as Packet<INetHandlerPlayServer>)
                    if (debugValue.get())
                        ClientUtils.displayChatMessage("packet player (rotation) added at ${blinkPackets.size - 1}")
                } else if (packet is C03PacketPlayer) {
                    if (packetTriggerValue.get().equals("prerelease", true) || packet.onGround != lastOnGround) {
                        event.cancelEvent()
                        blinkPackets.add(packet as Packet<INetHandlerPlayServer>)
                        if (debugValue.get())
                            ClientUtils.displayChatMessage("packet player (idle) added at ${blinkPackets.size - 1}")
                    }
                }
                if (packet is C0BPacketEntityAction) {
                    event.cancelEvent()
                    blinkPackets.add(packet as Packet<INetHandlerPlayServer>)
                    if (debugValue.get())
                        ClientUtils.displayChatMessage("packet action added at ${blinkPackets.size - 1}")
                }
                if (packet is C07PacketPlayerDigging && packetTriggerValue.get().equals("prerelease", true)) {
                    if (blinkPackets.size > 0) {
                        blinkPackets.forEach {
                            PacketUtils.sendPacketNoEvent(it)
                        }
                        if (debugValue.get())
                            ClientUtils.displayChatMessage("sent ${blinkPackets.size} packets.")
                        blinkPackets.clear()
                    }
                }
            }
        }

        if(consume.get() && consumeMode.get() == "Grim" && (mc.thePlayer.heldItem.item is ItemFood || mc.thePlayer.heldItem.item is ItemPotion || mc.thePlayer.heldItem.item is ItemBucketMilk) && mc.thePlayer.isUsingItem) {
            run {
                if (packet is S30PacketWindowItems) {
                    event.cancelEvent()
                }
                if (packet is S2FPacketSetSlot) {
                    event.cancelEvent()
                }
            }
        }
    }

    @EventTarget
    fun onClick(event: ClickUpdateEvent) {
        val player = mc.thePlayer ?: return
        val heldItem = player.heldItem ?: return
        val currentItem = player.currentEquippedItem
        val isUsingItem =
            mc.thePlayer?.heldItem != null && (mc.thePlayer.isUsingItem || (mc.thePlayer.heldItem?.item is ItemSword && LiquidBounce.moduleManager[KillAura::class.java]?.blockingStatus == true))
        if ((consume.get() && (heldItem.item is ItemFood || heldItem.item is ItemPotion || heldItem.item is ItemBucketMilk) && isUsingItem && consumeMode.get() == "Bug") || (sword.get() && heldItem.item is ItemSword && isUsingItem && swordMode.get() == "Bug")) {
            var idk = false
            if (lastItem != null && lastItem!! != currentItem) {
                count = 0
            }
            val state = if (currentItem.item is ItemSword) 1 else 3
            if (count != state) {
                idk = true
                PacketUtils.sendPacketNoEvent(C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT))
                player.stopUsingItem()
                player.closeScreen()
                count = state
            }
            if (idk) sendClickBlockToController(mc.currentScreen == null && mc.gameSettings.keyBindAttack.isKeyDown && mc.inGameHasFocus)
            lastItem = currentItem
        } else {
            count = 0
        }
    }

    private fun sendClickBlockToController(p_sendClickBlockToController_1_: Boolean) {
        if (!p_sendClickBlockToController_1_) {
            mc.leftClickCounter = 0
        }
        if (mc.leftClickCounter <= 0 && !mc.thePlayer.isUsingItem) {
            if (p_sendClickBlockToController_1_ && mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectType.BLOCK) {
                val blockpos: BlockPos = mc.objectMouseOver.blockPos
                if (mc.theWorld.getBlockState(blockpos).block
                        .material !== Material.air && mc.playerController.onPlayerDamageBlock(
                        blockpos,
                        mc.objectMouseOver.sideHit
                    )
                ) {
                    mc.effectRenderer.addBlockHitEffects(blockpos, mc.objectMouseOver)
                    mc.thePlayer.swingItem()
                }
            } else {
                mc.playerController.resetBlockRemoving()
            }
        }
    }

    @EventTarget
    fun onSlowDown(event: SlowDownEvent) {
        val heldItem = mc.thePlayer.heldItem?.item

        event.forward = getMultiplier(heldItem, true)
        event.strafe = getMultiplier(heldItem, false)
    }

    private fun getMultiplier(item: Item?, isForward: Boolean) =
        when (item) {
            is ItemFood, is ItemPotion, is ItemBucketMilk -> {
                if (isForward) this.consumeForwardMultiplier.get() else this.consumeStrafeMultiplier.get()
            }

            is ItemSword -> {
                if (isForward) this.blockForwardMultiplier.get() else this.blockStrafeMultiplier.get()
            }

            is ItemBow -> {
                if (isForward) this.bowForwardMultiplier.get() else this.bowStrafeMultiplier.get()
            }

            else -> 0.2F
        }
}