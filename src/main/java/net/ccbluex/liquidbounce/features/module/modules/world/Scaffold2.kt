package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.*
import net.ccbluex.liquidbounce.utils.render.BlurUtils.blurArea
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.block.*
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.settings.GameSettings
import net.minecraft.entity.passive.EntityPig
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.C09PacketHeldItemChange
import net.minecraft.network.play.client.C0APacketAnimation
import net.minecraft.network.play.client.C0BPacketEntityAction
import net.minecraft.stats.StatList
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.Vec3
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.util.*
import kotlin.math.roundToInt

@ModuleInfo(
    name = "Scaffold2",
    description = "Automatically places blocks beneath your feet.",
    category = ModuleCategory.WORLD
)
class Scaffold2 : Module() {
    private val rotationModeValue = ListValue("RotationMode", arrayOf("Novoline"), "Novoline")
    private val autoBlockMode = ListValue("AutoBlock", arrayOf("Spoof", "Switch", "Off"), "Switch")
    val sprintModeValue = ListValue(
        "SprintMode", arrayOf(
            "Vanilla", "NoPacket", "None"
        ), "Vanilla"
    )

    private val swingValue = BoolValue("Swing", false)
    private val safeWalkValue = BoolValue("SafeWalk", true)
    private val eagleValue = BoolValue("Eagle", false)
    //private val keepYValue = BoolValue("KeepY", true)
    private val zitterValue = BoolValue("Zitter", false)
    private val zitterDelay = IntegerValue("ZitterDelay", 100, 0, 500, "ms") { zitterValue.get() }
    private val speedModifierValue = FloatValue("SpeedModifier", 1f, 0f, 2f, "x")
    private val xzMultiplier = FloatValue("XZ-Multiplier", 1f, 0f, 4f, "x")
    private val placeConditionValue =
        ListValue("Place-Condition", arrayOf("Air", "FallDown", "NegativeMotion", "Always"), "Always")
    private val towerEnabled = BoolValue("Tower", false)
    private val towerModeValue = ListValue(
        "TowerMode", arrayOf(
            "Jump", "Motion", "ConstantMotion", "MotionTP", "Hypixel"
        ), "Motion"
    ) { towerEnabled.get() }
    private val constantMotionValue = FloatValue("ConstantMotion", 0.42f, 0.1f, 1f) {
        towerEnabled.get() && towerModeValue.get().equals("ConstantMotion", ignoreCase = true)
    }
    private val constantMotionJumpGroundValue = FloatValue("ConstantMotionJumpGround", 0.79f, 0.76f, 1f) {
        towerEnabled.get() && towerModeValue.get().equals("ConstantMotion", ignoreCase = true)
    }
    private val jumpMotionValue = FloatValue("JumpMotion", 0.42f, 0.3681289f, 0.79f) {
        towerEnabled.get() && towerModeValue.get().equals("Jump", ignoreCase = true)
    }
    private val noMoveOnlyValue = BoolValue("NoMove", true) { towerEnabled.get() }
    private val counterDisplayValue =
        ListValue("Counter", arrayOf("Off", "Simple", "Advanced", "Sigma", "Novoline", "Exhibition"), "Simple")
    private val blurValue = BoolValue("Blur-Advanced", false) {
        counterDisplayValue.get().equals("advanced", ignoreCase = true)
    }
    private val blurStrength = FloatValue("Blur-Strength", 1f, 0f, 30f, "x") {
        counterDisplayValue.get().equals("advanced", ignoreCase = true)
    }

    private var blockData: BlockData? = null
    private var jumpGround = 0.0
    private var alpha = 0f
    private var slot = 0
    private var lastSlot = 0
    private val zitterTimer = MSTimer()
    private var zitterDirection = false

    override fun onEnable() {
        slot = mc.thePlayer.inventory.currentItem
        lastSlot = mc.thePlayer.inventory.currentItem
    }

    override fun onDisable() {
        mc.timer.timerSpeed = 1f
        if (eagleValue.get()) {
            mc.gameSettings.keyBindSneak.pressed = Keyboard.isKeyDown(mc.gameSettings.keyBindSneak.keyCode)
        }
        if (lastSlot != mc.thePlayer.inventory.currentItem && autoBlockMode.get().equals("switch", ignoreCase = true)) {
            mc.thePlayer.inventory.currentItem = lastSlot
            mc.playerController.updateController()
        }
        if (slot != mc.thePlayer.inventory.currentItem && autoBlockMode.get()
                .equals("spoof", ignoreCase = true)
        ) mc.netHandler.addToSendQueue(
            C09PacketHeldItemChange(
                mc.thePlayer.inventory.currentItem
            )
        )
    }

    private fun shouldPlace(): Boolean {
        val placeWhenAir = placeConditionValue.get().equals("air", ignoreCase = true)
        val placeWhenFall = placeConditionValue.get().equals("falldown", ignoreCase = true)
        val placeWhenNegativeMotion = placeConditionValue.get().equals("negativemotion", ignoreCase = true)
        val alwaysPlace = placeConditionValue.get().equals("always", ignoreCase = true)
        return alwaysPlace || placeWhenAir && !mc.thePlayer.onGround || placeWhenFall && mc.thePlayer.fallDistance > 0 || placeWhenNegativeMotion && mc.thePlayer.motionY < 0
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        //XZMultiplier
        mc.thePlayer.motionX *= xzMultiplier.get().toDouble()
        mc.thePlayer.motionZ *= xzMultiplier.get().toDouble()

        //if (MovementUtils.isMoving() && mc.thePlayer.onGround && keepYValue.get()) {
            //mc.thePlayer.jump()
        //}

        //Autoblock
        var blockSlot = -1
        var itemStack = mc.thePlayer.heldItem

        if (mc.thePlayer.heldItem == null || mc.thePlayer.heldItem.item !is ItemBlock) {
            if (autoBlockMode.get().equals("Off", ignoreCase = true)) return
            blockSlot = InventoryUtils.findAutoBlockBlock()
            if (blockSlot == -1) return
            if (autoBlockMode.get().equals("Spoof", ignoreCase = true)) {
                mc.netHandler.addToSendQueue(C09PacketHeldItemChange(blockSlot - 36))
                itemStack = mc.thePlayer.inventoryContainer.getSlot(blockSlot).stack
            } else {
                mc.thePlayer.inventory.currentItem = blockSlot - 36
                mc.playerController.updateController()
            }
        }

        // blacklist check
        if (itemStack != null && itemStack.item != null && itemStack.item is ItemBlock) {
            val block = (itemStack.item as ItemBlock).getBlock()
            if (InventoryUtils.BLOCK_BLACKLIST.contains(block) || !block.isFullCube || itemStack.stackSize <= 0) return
        }

        blockData = if (getBlockData(BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.0, mc.thePlayer.posZ)) == null
        ) getBlockData(
            BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.0, mc.thePlayer.posZ).down(1)
        ) else getBlockData(
            BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.0, mc.thePlayer.posZ)
        )
        if (shouldPlace()) {
            if (zitterValue.get()) {
                if (!GameSettings.isKeyDown(mc.gameSettings.keyBindRight)) mc.gameSettings.keyBindRight.pressed = false
                if (!GameSettings.isKeyDown(mc.gameSettings.keyBindLeft)) mc.gameSettings.keyBindLeft.pressed = false
                if (zitterTimer.hasTimePassed(zitterDelay.get().toLong())) {
                    zitterDirection = !zitterDirection
                    zitterTimer.reset()
                }
                if (zitterDirection) {
                    mc.gameSettings.keyBindRight.pressed = true
                    mc.gameSettings.keyBindLeft.pressed = false
                } else {
                    mc.gameSettings.keyBindRight.pressed = false
                    mc.gameSettings.keyBindLeft.pressed = true
                }
            }
            if (eagleValue.get()) mc.gameSettings.keyBindSneak.pressed = PlayerUtils.isAirUnder(mc.thePlayer)
            if (towerMoving()) {
                move(event)
            }
            if (blockData != null) {
                if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.currentEquippedItem, blockData!!.position, blockData!!.facing, getVec3(blockData!!.position, blockData!!.facing))) {
                    //swing
                    if (swingValue.get()) {
                        mc.thePlayer.swingItem()
                    } else {
                        mc.netHandler.addToSendQueue(C0APacketAnimation())
                    }
                    if (mc.thePlayer.onGround) {
                        val modifier = speedModifierValue.get()
                        mc.thePlayer.motionX *= modifier.toDouble()
                        mc.thePlayer.motionZ *= modifier.toDouble()
                    }
                }
            }
            //rot
            when (rotationModeValue.get()) {
                "Novoline" -> {
                    if (blockData != null) {
                        val entity = EntityPig(mc.theWorld)
                        entity.posX = blockData!!.position.x + 0.5
                        entity.posY = blockData!!.position.y + 0.5
                        entity.posZ = blockData!!.position.z + 0.5
                        val rots = RotationUtils.getAngles(entity)
                        RotationUtils.setTargetRotation(rots)
                    }
                }
            }
        }
    }

    //tower enable
    private fun towerMoving(): Boolean {
        return towerEnabled.get() && Keyboard.isKeyDown(Keyboard.KEY_SPACE)
    }

    //tower
    private fun move(event: UpdateEvent) {
        mc.thePlayer.cameraYaw = 0f
        mc.thePlayer.cameraPitch = 0f
        if (noMoveOnlyValue.get()) {
            mc.thePlayer.motionZ = 0.0
            mc.thePlayer.motionX = 0.0
        }
        when (towerModeValue.get().lowercase(Locale.getDefault())) {
            "jump" -> if (mc.thePlayer.onGround) {
                fakeJump()
                mc.thePlayer.motionY = jumpMotionValue.get().toDouble()
            }

            "motion" -> if (mc.thePlayer.onGround) {
                fakeJump()
                mc.thePlayer.motionY = 0.42
            } else if (mc.thePlayer.motionY < 0.1) mc.thePlayer.motionY = -0.3

            "motiontp" -> if (mc.thePlayer.onGround) {
                fakeJump()
                mc.thePlayer.motionY = 0.42
            } else if (mc.thePlayer.motionY < 0.23) mc.thePlayer.setPosition(
                mc.thePlayer.posX, mc.thePlayer.posY.toInt()
                    .toDouble(), mc.thePlayer.posZ
            )

            "constantmotion" -> {
                if (mc.thePlayer.onGround) {
                    fakeJump()
                    jumpGround = mc.thePlayer.posY
                    mc.thePlayer.motionY = constantMotionValue.get().toDouble()
                }
                if (mc.thePlayer.posY > jumpGround + constantMotionJumpGroundValue.get()) {
                    fakeJump()
                    mc.thePlayer.setPosition(
                        mc.thePlayer.posX, mc.thePlayer.posY.toInt()
                            .toDouble(), mc.thePlayer.posZ
                    )
                    mc.thePlayer.motionY = constantMotionValue.get().toDouble()
                    jumpGround = mc.thePlayer.posY
                }
            }

            "hypixel" -> hypixelTower()
        }
    }

    private fun getBlockData(pos: BlockPos): BlockData? {
        if (isPosValid(pos.add(0, -1, 0))) {
            return BlockData(pos.add(0, -1, 0), EnumFacing.UP)
        }
        if (isPosValid(pos.add(-1, 0, 0))) {
            return BlockData(pos.add(-1, 0, 0), EnumFacing.EAST)
        }
        if (isPosValid(pos.add(1, 0, 0))) {
            return BlockData(pos.add(1, 0, 0), EnumFacing.WEST)
        }
        if (isPosValid(pos.add(0, 0, 1))) {
            return BlockData(pos.add(0, 0, 1), EnumFacing.NORTH)
        }
        if (isPosValid(pos.add(0, 0, -1))) {
            return BlockData(pos.add(0, 0, -1), EnumFacing.SOUTH)
        }
        val pos2 = pos.add(-1, 0, 0)
        if (isPosValid(pos2.add(0, -1, 0))) {
            return BlockData(pos2.add(0, -1, 0), EnumFacing.UP)
        }
        if (isPosValid(pos2.add(-1, 0, 0))) {
            return BlockData(pos2.add(-1, 0, 0), EnumFacing.EAST)
        }
        if (isPosValid(pos2.add(1, 0, 0))) {
            return BlockData(pos2.add(1, 0, 0), EnumFacing.WEST)
        }
        if (isPosValid(pos2.add(0, 0, 1))) {
            return BlockData(pos2.add(0, 0, 1), EnumFacing.NORTH)
        }
        if (isPosValid(pos2.add(0, 0, -1))) {
            return BlockData(pos2.add(0, 0, -1), EnumFacing.SOUTH)
        }
        val pos3 = pos.add(1, 0, 0)
        if (isPosValid(pos3.add(0, -1, 0))) {
            return BlockData(pos3.add(0, -1, 0), EnumFacing.UP)
        }
        if (isPosValid(pos3.add(-1, 0, 0))) {
            return BlockData(pos3.add(-1, 0, 0), EnumFacing.EAST)
        }
        if (isPosValid(pos3.add(1, 0, 0))) {
            return BlockData(pos3.add(1, 0, 0), EnumFacing.WEST)
        }
        if (isPosValid(pos3.add(0, 0, 1))) {
            return BlockData(pos3.add(0, 0, 1), EnumFacing.NORTH)
        }
        if (isPosValid(pos3.add(0, 0, -1))) {
            return BlockData(pos3.add(0, 0, -1), EnumFacing.SOUTH)
        }
        val pos4 = pos.add(0, 0, 1)
        if (isPosValid(pos4.add(0, -1, 0))) {
            return BlockData(pos4.add(0, -1, 0), EnumFacing.UP)
        }
        if (isPosValid(pos4.add(-1, 0, 0))) {
            return BlockData(pos4.add(-1, 0, 0), EnumFacing.EAST)
        }
        if (isPosValid(pos4.add(1, 0, 0))) {
            return BlockData(pos4.add(1, 0, 0), EnumFacing.WEST)
        }
        if (isPosValid(pos4.add(0, 0, 1))) {
            return BlockData(pos4.add(0, 0, 1), EnumFacing.NORTH)
        }
        if (isPosValid(pos4.add(0, 0, -1))) {
            return BlockData(pos4.add(0, 0, -1), EnumFacing.SOUTH)
        }
        val pos5 = pos.add(0, 0, -1)
        if (isPosValid(pos5.add(0, -1, 0))) {
            return BlockData(pos5.add(0, -1, 0), EnumFacing.UP)
        }
        if (isPosValid(pos5.add(-1, 0, 0))) {
            return BlockData(pos5.add(-1, 0, 0), EnumFacing.EAST)
        }
        if (isPosValid(pos5.add(1, 0, 0))) {
            return BlockData(pos5.add(1, 0, 0), EnumFacing.WEST)
        }
        if (isPosValid(pos5.add(0, 0, 1))) {
            return BlockData(pos5.add(0, 0, 1), EnumFacing.NORTH)
        }
        if (isPosValid(pos5.add(0, 0, -1))) {
            return BlockData(pos5.add(0, 0, -1), EnumFacing.SOUTH)
        }
        pos.add(-2, 0, 0)
        if (isPosValid(pos2.add(0, -1, 0))) {
            return BlockData(pos2.add(0, -1, 0), EnumFacing.UP)
        }
        if (isPosValid(pos2.add(-1, 0, 0))) {
            return BlockData(pos2.add(-1, 0, 0), EnumFacing.EAST)
        }
        if (isPosValid(pos2.add(1, 0, 0))) {
            return BlockData(pos2.add(1, 0, 0), EnumFacing.WEST)
        }
        if (isPosValid(pos2.add(0, 0, 1))) {
            return BlockData(pos2.add(0, 0, 1), EnumFacing.NORTH)
        }
        if (isPosValid(pos2.add(0, 0, -1))) {
            return BlockData(pos2.add(0, 0, -1), EnumFacing.SOUTH)
        }
        pos.add(2, 0, 0)
        if (isPosValid(pos3.add(0, -1, 0))) {
            return BlockData(pos3.add(0, -1, 0), EnumFacing.UP)
        }
        if (isPosValid(pos3.add(-1, 0, 0))) {
            return BlockData(pos3.add(-1, 0, 0), EnumFacing.EAST)
        }
        if (isPosValid(pos3.add(1, 0, 0))) {
            return BlockData(pos3.add(1, 0, 0), EnumFacing.WEST)
        }
        if (isPosValid(pos3.add(0, 0, 1))) {
            return BlockData(pos3.add(0, 0, 1), EnumFacing.NORTH)
        }
        if (isPosValid(pos3.add(0, 0, -1))) {
            return BlockData(pos3.add(0, 0, -1), EnumFacing.SOUTH)
        }
        pos.add(0, 0, 2)
        if (isPosValid(pos4.add(0, -1, 0))) {
            return BlockData(pos4.add(0, -1, 0), EnumFacing.UP)
        }
        if (isPosValid(pos4.add(-1, 0, 0))) {
            return BlockData(pos4.add(-1, 0, 0), EnumFacing.EAST)
        }
        if (isPosValid(pos4.add(1, 0, 0))) {
            return BlockData(pos4.add(1, 0, 0), EnumFacing.WEST)
        }
        if (isPosValid(pos4.add(0, 0, 1))) {
            return BlockData(pos4.add(0, 0, 1), EnumFacing.NORTH)
        }
        if (isPosValid(pos4.add(0, 0, -1))) {
            return BlockData(pos4.add(0, 0, -1), EnumFacing.SOUTH)
        }
        pos.add(0, 0, -2)
        if (isPosValid(pos5.add(0, -1, 0))) {
            return BlockData(pos5.add(0, -1, 0), EnumFacing.UP)
        }
        if (isPosValid(pos5.add(-1, 0, 0))) {
            return BlockData(pos5.add(-1, 0, 0), EnumFacing.EAST)
        }
        if (isPosValid(pos5.add(1, 0, 0))) {
            return BlockData(pos5.add(1, 0, 0), EnumFacing.WEST)
        }
        if (isPosValid(pos5.add(0, 0, 1))) {
            return BlockData(pos5.add(0, 0, 1), EnumFacing.NORTH)
        }
        if (isPosValid(pos5.add(0, 0, -1))) {
            return BlockData(pos5.add(0, 0, -1), EnumFacing.SOUTH)
        }
        val pos6 = pos.add(0, -1, 0)
        if (isPosValid(pos6.add(0, -1, 0))) {
            return BlockData(pos6.add(0, -1, 0), EnumFacing.UP)
        }
        if (isPosValid(pos6.add(-1, 0, 0))) {
            return BlockData(pos6.add(-1, 0, 0), EnumFacing.EAST)
        }
        if (isPosValid(pos6.add(1, 0, 0))) {
            return BlockData(pos6.add(1, 0, 0), EnumFacing.WEST)
        }
        if (isPosValid(pos6.add(0, 0, 1))) {
            return BlockData(pos6.add(0, 0, 1), EnumFacing.NORTH)
        }
        if (isPosValid(pos6.add(0, 0, -1))) {
            return BlockData(pos6.add(0, 0, -1), EnumFacing.SOUTH)
        }
        val pos7 = pos6.add(1, 0, 0)
        if (isPosValid(pos7.add(0, -1, 0))) {
            return BlockData(pos7.add(0, -1, 0), EnumFacing.UP)
        }
        if (isPosValid(pos7.add(-1, 0, 0))) {
            return BlockData(pos7.add(-1, 0, 0), EnumFacing.EAST)
        }
        if (isPosValid(pos7.add(1, 0, 0))) {
            return BlockData(pos7.add(1, 0, 0), EnumFacing.WEST)
        }
        if (isPosValid(pos7.add(0, 0, 1))) {
            return BlockData(pos7.add(0, 0, 1), EnumFacing.NORTH)
        }
        if (isPosValid(pos7.add(0, 0, -1))) {
            return BlockData(pos7.add(0, 0, -1), EnumFacing.SOUTH)
        }
        val pos8 = pos6.add(-1, 0, 0)
        if (isPosValid(pos8.add(0, -1, 0))) {
            return BlockData(pos8.add(0, -1, 0), EnumFacing.UP)
        }
        if (isPosValid(pos8.add(-1, 0, 0))) {
            return BlockData(pos8.add(-1, 0, 0), EnumFacing.EAST)
        }
        if (isPosValid(pos8.add(1, 0, 0))) {
            return BlockData(pos8.add(1, 0, 0), EnumFacing.WEST)
        }
        if (isPosValid(pos8.add(0, 0, 1))) {
            return BlockData(pos8.add(0, 0, 1), EnumFacing.NORTH)
        }
        if (isPosValid(pos8.add(0, 0, -1))) {
            return BlockData(pos8.add(0, 0, -1), EnumFacing.SOUTH)
        }
        val pos9 = pos6.add(0, 0, 1)
        if (isPosValid(pos9.add(0, -1, 0))) {
            return BlockData(pos9.add(0, -1, 0), EnumFacing.UP)
        }
        if (isPosValid(pos9.add(-1, 0, 0))) {
            return BlockData(pos9.add(-1, 0, 0), EnumFacing.EAST)
        }
        if (isPosValid(pos9.add(1, 0, 0))) {
            return BlockData(pos9.add(1, 0, 0), EnumFacing.WEST)
        }
        if (isPosValid(pos9.add(0, 0, 1))) {
            return BlockData(pos9.add(0, 0, 1), EnumFacing.NORTH)
        }
        if (isPosValid(pos9.add(0, 0, -1))) {
            return BlockData(pos9.add(0, 0, -1), EnumFacing.SOUTH)
        }
        val pos10 = pos6.add(0, 0, -1)
        if (isPosValid(pos10.add(0, -1, 0))) {
            return BlockData(pos10.add(0, -1, 0), EnumFacing.UP)
        }
        if (isPosValid(pos10.add(-1, 0, 0))) {
            return BlockData(pos10.add(-1, 0, 0), EnumFacing.EAST)
        }
        if (isPosValid(pos10.add(1, 0, 0))) {
            return BlockData(pos10.add(1, 0, 0), EnumFacing.WEST)
        }
        if (isPosValid(pos10.add(0, 0, 1))) {
            return BlockData(pos10.add(0, 0, 1), EnumFacing.NORTH)
        }
        return if (isPosValid(pos10.add(0, 0, -1))) {
            BlockData(pos10.add(0, 0, -1), EnumFacing.SOUTH)
        } else null
    }

    private fun isPosValid(pos: BlockPos): Boolean {
        val block = mc.theWorld.getBlockState(pos).block
        return ((block.material.isSolid || !block.isTranslucent || block.isVisuallyOpaque
                || block is BlockLadder || block is BlockCarpet || block is BlockSnow
                || block is BlockSkull) && !block.material.isLiquid
                && block !is BlockContainer)
    }

    private class BlockData(val position: BlockPos, val facing: EnumFacing)

    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (mc.thePlayer == null) return
        val packet = event.packet

        //sprint packet
        if (sprintModeValue.get().equals("NoPacket", ignoreCase = true)) {
            if (packet is C0BPacketEntityAction &&
                (packet.action == C0BPacketEntityAction.Action.STOP_SPRINTING || packet.action == C0BPacketEntityAction.Action.START_SPRINTING)
            ) event.cancelEvent()
        }

        // AutoBlock
        if (packet is C09PacketHeldItemChange) {
            slot = packet.slotId
        }
    }

    private fun fakeJump() {
        mc.thePlayer.isAirBorne = true
        mc.thePlayer.triggerAchievement(StatList.jumpStat)
    }

    private val blocksAmount: Int
        get() {
            var amount = 0
            for (i in 36..44) {
                val itemStack = mc.thePlayer.inventoryContainer.getSlot(i).stack
                if (itemStack != null && itemStack.item is ItemBlock) {
                    val block = (itemStack.item as ItemBlock).getBlock()
                    if (!InventoryUtils.BLOCK_BLACKLIST.contains(block) && block.isFullCube) amount += itemStack.stackSize
                }
            }
            return amount
        }

    private fun getBlockColor(count: Int): Int {
        val f = count.toFloat()
        val f1 = 64f
        val f2 = 0.0f.coerceAtLeast(f.coerceAtMost(f1) / f1)
        return Color.HSBtoRGB(f2 / 3.0f, 1.0f, 1.0f) or -0x1000000
    }

    private fun renderItemStack(stack: ItemStack, x: Int, y: Int) {
        GlStateManager.pushMatrix()
        GlStateManager.enableRescaleNormal()
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        RenderHelper.enableGUIStandardItemLighting()
        mc.renderItem.renderItemAndEffectIntoGUI(stack, x, y)
        RenderHelper.disableStandardItemLighting()
        GlStateManager.disableRescaleNormal()
        GlStateManager.disableBlend()
        GlStateManager.popMatrix()
    }

    @EventTarget
    fun onRender2D(event: Render2DEvent?) {
        val lastMS = 0L
        var progress = (System.currentTimeMillis() - lastMS).toFloat() / 100f
        if (progress >= 1) progress = 1f
        val counterMode = counterDisplayValue.get()
        val scaledResolution = ScaledResolution(mc)
        val info = "$blocksAmount blocks"
        val infoWidth = Fonts.fontSFUI40.getStringWidth(info)
        val info3 = "" + blocksAmount
        val infoWidth2 = Fonts.minecraftFont.getStringWidth(blocksAmount.toString() + "")
        if (counterMode.equals("advanced", ignoreCase = true)) {
            val canRenderStack =
                slot in 0..8 && mc.thePlayer.inventory.mainInventory[slot] != null && mc.thePlayer.inventory.mainInventory[slot].item != null && mc.thePlayer.inventory.mainInventory[slot].item is ItemBlock
            if (blurValue.get()) blurArea(
                (scaledResolution.scaledWidth / 2 - infoWidth / 2 - 4).toFloat(),
                (scaledResolution.scaledHeight / 2 - 39).toFloat(),
                (scaledResolution.scaledWidth / 2 + infoWidth / 2 + 4).toFloat(),
                (scaledResolution.scaledHeight / 2 - if (canRenderStack) 5 else 26).toFloat(),
                blurStrength.get()
            )
            RenderUtils.drawRect(
                (scaledResolution.scaledWidth / 2 - infoWidth / 2 - 4).toFloat(),
                (scaledResolution.scaledHeight / 2 - 40).toFloat(),
                (scaledResolution.scaledWidth / 2 + infoWidth / 2 + 4).toFloat(),
                (scaledResolution.scaledHeight / 2 - 39).toFloat(),
                if (blocksAmount > 1) -0x1 else -0xeff0
            )
            RenderUtils.drawRect(
                (scaledResolution.scaledWidth / 2 - infoWidth / 2 - 4).toFloat(),
                (scaledResolution.scaledHeight / 2 - 39).toFloat(),
                (scaledResolution.scaledWidth / 2 + infoWidth / 2 + 4).toFloat(),
                (scaledResolution.scaledHeight / 2 - 26).toFloat(),
                -0x60000000
            )
            if (canRenderStack) {
                RenderUtils.drawRect(
                    (scaledResolution.scaledWidth / 2 - infoWidth / 2 - 4).toFloat(),
                    (scaledResolution.scaledHeight / 2 - 26).toFloat(),
                    (scaledResolution.scaledWidth / 2 + infoWidth / 2 + 4).toFloat(),
                    (scaledResolution.scaledHeight / 2 - 5).toFloat(),
                    -0x60000000
                )
                GlStateManager.pushMatrix()
                GlStateManager.translate(
                    (scaledResolution.scaledWidth / 2 - 8).toFloat(),
                    (scaledResolution.scaledHeight / 2 - 25).toFloat(),
                    (scaledResolution.scaledWidth / 2 - 8).toFloat()
                )
                renderItemStack(mc.thePlayer.inventory.mainInventory[slot], 0, 0)
                GlStateManager.popMatrix()
            }
            GlStateManager.resetColor()
            Fonts.fontSFUI40.drawCenteredString(
                info,
                (scaledResolution.scaledWidth / 2).toFloat(),
                (scaledResolution.scaledHeight / 2 - 36).toFloat(),
                -1
            )
        }
        if (counterMode.equals("sigma", ignoreCase = true)) {
            GlStateManager.translate(0f, -14f - progress * 4f, 0f)
            //GL11.glPushMatrix();
            GL11.glEnable(GL11.GL_BLEND)
            GL11.glDisable(GL11.GL_TEXTURE_2D)
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
            GL11.glEnable(GL11.GL_LINE_SMOOTH)
            GL11.glColor4f(0.15f, 0.15f, 0.15f, progress)
            GL11.glBegin(GL11.GL_TRIANGLE_FAN)
            GL11.glVertex2d(
                (scaledResolution.scaledWidth / 2 - 3).toDouble(),
                (scaledResolution.scaledHeight - 60).toDouble()
            )
            GL11.glVertex2d(
                (scaledResolution.scaledWidth / 2).toDouble(),
                (scaledResolution.scaledHeight - 57).toDouble()
            )
            GL11.glVertex2d(
                (scaledResolution.scaledWidth / 2 + 3).toDouble(),
                (scaledResolution.scaledHeight - 60).toDouble()
            )
            GL11.glEnd()
            GL11.glEnable(GL11.GL_TEXTURE_2D)
            GL11.glDisable(GL11.GL_BLEND)
            GL11.glDisable(GL11.GL_LINE_SMOOTH)
            //GL11.glPopMatrix();
            RenderUtils.drawRoundedRect(
                (scaledResolution.scaledWidth / 2 - infoWidth / 2 - 4).toFloat(),
                (scaledResolution.scaledHeight - 60).toFloat(),
                (scaledResolution.scaledWidth / 2 + infoWidth / 2 + 4).toFloat(),
                (scaledResolution.scaledHeight - 74).toFloat(),
                2f,
                Color(0.15f, 0.15f, 0.15f, progress).rgb
            )
            GlStateManager.resetColor()
            Fonts.fontSFUI35.drawCenteredString(
                info,
                scaledResolution.scaledWidth / 2 + 0.1f,
                (scaledResolution.scaledHeight - 70).toFloat(),
                Color(1f, 1f, 1f, 0.8f * progress).rgb,
                false
            )
            GlStateManager.translate(0f, 14f + progress * 4f, 0f)
        }
        if (counterMode.equals("novoline", ignoreCase = true)) {
            if (slot in 0..8 && mc.thePlayer.inventory.mainInventory[slot] != null && mc.thePlayer.inventory.mainInventory[slot].item != null && mc.thePlayer.inventory.mainInventory[slot].item is ItemBlock) {
                //RenderUtils.drawRect(scaledResolution.getScaledWidth() / 2 - (infoWidth / 2) - 4, scaledResolution.getScaledHeight() / 2 - 26, scaledResolution.getScaledWidth() / 2 + (infoWidth / 2) + 4, scaledResolution.getScaledHeight() / 2 - 5, 0xA0000000);
                GlStateManager.pushMatrix()
                GlStateManager.translate(
                    (scaledResolution.scaledWidth / 2 - 22).toFloat(),
                    (scaledResolution.scaledHeight / 2 + 16).toFloat(),
                    (scaledResolution.scaledWidth / 2 - 22).toFloat()
                )
                renderItemStack(mc.thePlayer.inventory.mainInventory[slot], 0, 0)
                GlStateManager.popMatrix()
            }
            GlStateManager.resetColor()
            Fonts.minecraftFont.drawString(
                "$blocksAmount blocks",
                (scaledResolution.scaledWidth / 2).toFloat(),
                (scaledResolution.scaledHeight / 2 + 20).toFloat(),
                -1,
                true
            )
        }
        if (counterMode.equals("simple", ignoreCase = true)) {
            val delta = RenderUtils.deltaTime.toFloat()
            if (state) {
                alpha += 2 * delta
                if (alpha >= 250) alpha = 250f
            } else {
                alpha -= 2 * delta
                if (alpha <= 30) alpha = 0f
            }
            if (alpha > 1) {
                GlStateManager.pushMatrix()
                mc.fontRendererObj.drawString(
                    info3,
                    scaledResolution.scaledWidth / 2 + 10 - 1,
                    scaledResolution.scaledHeight / 2,
                    RenderUtils.reAlpha(
                        Color.BLACK.rgb, alpha / 255
                    )
                )
                mc.fontRendererObj.drawString(
                    info3,
                    scaledResolution.scaledWidth / 2 + 10 + 1,
                    scaledResolution.scaledHeight / 2,
                    RenderUtils.reAlpha(
                        Color.BLACK.rgb, alpha / 255
                    )
                )
                mc.fontRendererObj.drawString(
                    info3,
                    scaledResolution.scaledWidth / 2 + 10,
                    scaledResolution.scaledHeight / 2 - 1,
                    RenderUtils.reAlpha(
                        Color.BLACK.rgb, alpha / 255
                    )
                )
                mc.fontRendererObj.drawString(
                    info3,
                    scaledResolution.scaledWidth / 2 + 10,
                    scaledResolution.scaledHeight / 2 + 1,
                    RenderUtils.reAlpha(
                        Color.BLACK.rgb, alpha / 255
                    )
                )
                mc.fontRendererObj.drawString(
                    info3,
                    scaledResolution.scaledWidth / 2 + 10,
                    scaledResolution.scaledHeight / 2,
                    RenderUtils.reAlpha(
                        getBlockColor(
                            blocksAmount
                        ), alpha / 255
                    )
                )
                mc.fontRendererObj.drawString(
                    info3, scaledResolution.scaledWidth / 2 + 10, scaledResolution.scaledHeight / 2, getBlockColor(
                        blocksAmount
                    )
                )
                GlStateManager.popMatrix()
            }
        }
        if (counterMode.equals("exhibition", ignoreCase = true)) {
            var c = Colors.getColor(255, 0, 0, 150)
            if (blocksAmount in 64..127) {
                c = Colors.getColor(255, 255, 0, 150)
            } else if (blocksAmount >= 128) {
                c = Colors.getColor(0, 255, 0, 150)
            }
            Fonts.minecraftFont.drawString(
                blocksAmount.toString() + "",
                (scaledResolution.scaledWidth / 2 - infoWidth2 / 2 - 1).toFloat(),
                (scaledResolution.scaledHeight / 2 - 36).toFloat(),
                -0x1000000,
                false
            )
            Fonts.minecraftFont.drawString(
                blocksAmount.toString() + "",
                (scaledResolution.scaledWidth / 2 - infoWidth2 / 2 + 1).toFloat(),
                (scaledResolution.scaledHeight / 2 - 36).toFloat(),
                -0x1000000,
                false
            )
            Fonts.minecraftFont.drawString(
                blocksAmount.toString() + "",
                (scaledResolution.scaledWidth / 2 - infoWidth2 / 2).toFloat(),
                (scaledResolution.scaledHeight / 2 - 35).toFloat(),
                -0x1000000,
                false
            )
            Fonts.minecraftFont.drawString(
                blocksAmount.toString() + "",
                (scaledResolution.scaledWidth / 2 - infoWidth2 / 2).toFloat(),
                (scaledResolution.scaledHeight / 2 - 37).toFloat(),
                -0x1000000,
                false
            )
            Fonts.minecraftFont.drawString(
                blocksAmount.toString() + "",
                (scaledResolution.scaledWidth / 2 - infoWidth2 / 2).toFloat(),
                (scaledResolution.scaledHeight / 2 - 36).toFloat(),
                c,
                false
            )
        }
    }

    @EventTarget
    fun onMove(event: MoveEvent) {
        if (safeWalkValue.get() && mc.thePlayer.onGround) event.isSafeWalk = true
    }

    @EventTarget
    fun onJump(event: JumpEvent) {
        if (towerMoving()) {
            event.cancelEvent()
        }
    }

    private fun hypixelTower() {
        if (mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, mc.thePlayer.entityBoundingBox.offset(0.0, -0.76, 0.0)).isNotEmpty() && mc.theWorld.getCollidingBoundingBoxes(
                mc.thePlayer, mc.thePlayer.entityBoundingBox.offset(0.0, -0.75, 0.0)
            ).isEmpty() && mc.thePlayer.motionY > 0.23 && mc.thePlayer.motionY < 0.25
        ) {
            mc.thePlayer.motionY = mc.thePlayer.posY.roundToInt() - mc.thePlayer.posY
        }
        if (mc.theWorld.getCollidingBoundingBoxes(
                mc.thePlayer,
                mc.thePlayer.entityBoundingBox.offset(0.0, -0.0001, 0.0)
            ).isNotEmpty()
        ) {
            mc.thePlayer.motionY = 0.41999998688698
        } else if (mc.thePlayer.posY >= mc.thePlayer.posY.roundToInt() - 0.0001 && mc.thePlayer.posY <= mc.thePlayer.posY.roundToInt() + 0.0001 && !Keyboard.isKeyDown(
                mc.gameSettings.keyBindSneak.keyCode
            )
        ) {
            mc.thePlayer.motionY = 0.0
        }
    }

    override val tag: String
        get() = rotationModeValue.get()

    companion object {
        private fun randomNumber(max: Double, min: Double): Double {
            return Math.random() * (max - min) + min
        }

        fun getVec3(pos: BlockPos, facing: EnumFacing): Vec3 {
            var x = pos.x + 0.5
            var y = pos.y + 0.5
            var z = pos.z + 0.5
            if (facing == EnumFacing.UP || facing == EnumFacing.DOWN) {
                x += randomNumber(0.3, -0.3)
                z += randomNumber(0.3, -0.3)
            } else {
                y += randomNumber(0.3, -0.3)
            }
            if (facing == EnumFacing.WEST || facing == EnumFacing.EAST) {
                z += randomNumber(0.3, -0.3)
            }
            if (facing == EnumFacing.SOUTH || facing == EnumFacing.NORTH) {
                x += randomNumber(0.3, -0.3)
            }
            return Vec3(x, y, z)
        }
    }
}