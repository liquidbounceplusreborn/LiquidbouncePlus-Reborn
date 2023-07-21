package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.*
import net.ccbluex.liquidbounce.utils.MovementUtils.strafe
import net.ccbluex.liquidbounce.utils.RotationUtils.setTargetRotation
import net.ccbluex.liquidbounce.utils.block.BlockUtils
import net.ccbluex.liquidbounce.utils.block.PlaceInfo
import net.ccbluex.liquidbounce.utils.extensions.eyes
import net.ccbluex.liquidbounce.utils.extensions.rotation
import net.ccbluex.liquidbounce.utils.math.toRadians
import net.ccbluex.liquidbounce.utils.math.toRadiansD
import net.ccbluex.liquidbounce.utils.misc.RandomUtils
import net.ccbluex.liquidbounce.utils.render.BlurUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.utils.timer.TimerUtils.randomClickDelay
import net.ccbluex.liquidbounce.utils.timer.TimerUtils.randomDelay
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.block.BlockBush
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.settings.GameSettings
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.C09PacketHeldItemChange
import net.minecraft.network.play.client.C0APacketAnimation
import net.minecraft.network.play.client.C0BPacketEntityAction
import net.minecraft.potion.Potion
import net.minecraft.stats.StatList
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.MovingObjectPosition
import net.minecraft.util.Vec3
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.util.*
import kotlin.math.*

@ModuleInfo(
    name = "Scaffold",
    description = "Automatically places blocks beneath your feet.",
    category = ModuleCategory.WORLD
)
class Scaffold : Module() {
    private val mode = ListValue("Mode", arrayOf("Normal", "Rewinside", "Expand"), "Normal")

    // Placeable delay
    private val placeDelay = BoolValue("PlaceDelay", true)

    private val extraClicks = BoolValue("DoExtraClicks", false)

    private val extraClickMaxCPSValue: IntegerValue = object : IntegerValue("ExtraClickMaxCPS", 7, 0,50) {
        override fun onChange(oldValue: Int, newValue: Int) {
            newValue.coerceAtLeast(extraClickMinCPS.get())
        }
    }

    private val extraClickMinCPS = object : IntegerValue("ExtraClickMinCPS", 3, 0,50) {
        override fun onChange(oldValue: Int, newValue: Int) {
            newValue.coerceAtMost(extraClickMaxCPSValue.get())
        }
    }

    private val placementAttempt = ListValue(
        "PlacementAttempt", arrayOf("Fail", "Independent"), "Fail"
    ) { extraClicks.get() }

    // Delay
    private val maxDelayValue: IntegerValue = object : IntegerValue("MaxDelay", 0, 0,1000) {
        override fun onChange(oldValue: Int, newValue: Int) {
            newValue.coerceAtLeast(minDelay.get())
        }
    }

    private val minDelay = object : IntegerValue("MinDelay", 0, 0,1000) {
        override fun onChange(oldValue: Int, newValue: Int) {
            newValue.coerceAtMost(maxDelayValue.get())
        }
    }

    // Autoblock
    private val autoBlock = ListValue("AutoBlock", arrayOf("Off", "Pick", "Spoof", "Switch"), "Spoof")

    // Basic stuff
    val sprint = ListValue("Sprint", arrayOf("Same", "Ground", "Air", "PlaceOff", "Off"), "Off")
    private val swing = BoolValue("Swing", true)
    private val search = BoolValue("Search", true)
    private val down = BoolValue("Down", true)

    // Eagle
    private val eagle = ListValue("Eagle", arrayOf("Normal", "Silent", "Off"), "Normal")
    private val blocksToEagle = IntegerValue("BlocksToEagle", 0, 0,10) { eagle.get() != "Off" }
    private val edgeDistance = FloatValue("EagleEdgeDistance", 0f, 0f,0.5f) { eagle.get() != "Off" }

    // Expand
    private val omniDirectionalExpand = BoolValue("OmniDirectionalExpand", false)
    private val expandLength = IntegerValue("ExpandLength", 1, 1,6)

    // Rotation Options
    private val rotations = BoolValue("Rotations", true)
    private val stabilizedRotation = BoolValue("StabilizedRotation", false)
    private val silentRotation = BoolValue("SilentRotation", true)
    private val keepRotation = BoolValue("KeepRotation", true)
    private val keepTicks = object : IntegerValue("KeepTicks", 1, 1,20) {
        override fun onChange(oldValue: Int, newValue: Int) {
            newValue.coerceAtLeast(minimum)
        }
    }

    // Search options
    private val searchMode = ListValue("SearchMode", arrayOf("Area", "Center"), "Area")
    private val minDist = FloatValue("MinDist", 0f, 0f,0.2f)

    // Turn Speed
    private val maxTurnSpeedValue: FloatValue = object : FloatValue("MaxTurnSpeed", 180f, 1f,180f) {
        override fun onChange(oldValue: Float, newValue: Float) {
            newValue.coerceAtLeast(minTurnSpeed.get())
        }
    }
    private val maxTurnSpeed = maxTurnSpeedValue
    private val minTurnSpeed = object : FloatValue("MinTurnSpeed", 180f, 1f,180f) {
        override fun onChange(oldValue: Float, newValue: Float) {
            newValue.coerceAtMost(maxTurnSpeed.get())
        }
    }

    // Zitter
    private val zitterMode = ListValue("Zitter", arrayOf("Off", "Teleport", "Smooth"), "Off")
    private val zitterSpeed = FloatValue("ZitterSpeed", 0.13f, 0.1f,0.3f){ zitterMode.get() == "Teleport" }
    private val zitterStrength = FloatValue("ZitterStrength", 0.05f, 0f,0.2f) { zitterMode.get() == "Teleport" }

    // Game
    private val placeConditionValue =
        ListValue("Place-Condition", arrayOf("Air", "FallDown", "NegativeMotion", "Always"), "Always")
    private val timer = FloatValue("Timer", 1f, 0.1f,10f)
    private val speedModifier = FloatValue("SpeedModifier", 1f, 0f,2f)
    private val speedEffectDetect = BoolValue("SpeedPotDetect", true)


    // Safety
    private val keepY = BoolValue("KeepY", false)
    private val safeWalk = BoolValue("SafeWalk", true)
    private val airSafe = BoolValue("AirSafe", false)

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



    // Target placement
    private var targetPlace: PlaceInfo? = null

    // Launch position
    private var launchY = 0

    // AutoBlock
    private var slot = -1

    // Zitter Direction
    private var zitterDirection = false

    // Delay
    private val delayTimer = MSTimer()
    private val zitterTimer = MSTimer()
    private var delay = 0

    // Eagle
    private var placedBlocksWithoutEagle = 0
    private var eagleSneaking = false

    // Downwards
    private val shouldGoDown
        get() = down.get() && !keepY.get() && GameSettings.isKeyDown(mc.gameSettings.keyBindSneak) && blocksAmount > 1

    // Current rotation
    private val currRotation
        get() = RotationUtils.targetRotation ?: mc.thePlayer.rotation

    // Extra clicks
    private var extraClick = ExtraClickInfo(randomClickDelay(extraClickMinCPS.get(), extraClickMaxCPSValue.get()).toInt(), 0L, 0)

    private var jumpGround = 0.0

    private fun shouldPlace(): Boolean {
        val placeWhenAir = placeConditionValue.get().equals("air", ignoreCase = true)
        val placeWhenFall = placeConditionValue.get().equals("falldown", ignoreCase = true)
        val placeWhenNegativeMotion = placeConditionValue.get().equals("negativemotion", ignoreCase = true)
        val alwaysPlace = placeConditionValue.get().equals("always", ignoreCase = true)
        return alwaysPlace || placeWhenAir && !mc.thePlayer.onGround || placeWhenFall && mc.thePlayer.fallDistance > 0 || placeWhenNegativeMotion && mc.thePlayer.motionY < 0
    }

    // Enabling module
    override fun onEnable() {
        val player = mc.thePlayer ?: return

        launchY = player.posY.roundToInt()
        slot = player.inventory.currentItem
    }

    // Events
    @EventTarget
    private fun onUpdate(event: UpdateEvent) {
        val player = mc.thePlayer ?: return

        mc.timer.timerSpeed = timer.get()

        if (shouldGoDown) {
            mc.gameSettings.keyBindSneak.pressed = false
        }

        if (towering()) {
            tower()
        }

        if (sprint.get().equals("ground", ignoreCase = true) && !mc.thePlayer.onGround || sprint.get().equals("air", ignoreCase = true) && mc.thePlayer.onGround) {
            mc.thePlayer.isSprinting = false
        }

        // Eagle
        if (eagle.get() != "Off" && !shouldGoDown) {
            var dif = 0.5
            val blockPos = BlockPos(player).down()

            for (side in EnumFacing.values()) {
                if (side.axis == EnumFacing.Axis.Y) {
                    continue
                }

                val neighbor = blockPos.offset(side)

                if (BlockUtils.isReplaceable(neighbor)) {
                    val calcDif = (if (side.axis == EnumFacing.Axis.Z) {
                        abs(neighbor.z + 0.5 - player.posZ)
                    } else {
                        abs(neighbor.x + 0.5 - player.posX)
                    }) - 0.5

                    if (calcDif < dif) {
                        dif = calcDif
                    }
                }
            }

            if (placedBlocksWithoutEagle >= blocksToEagle.get()) {
                val shouldEagle = BlockUtils.isReplaceable(blockPos) || dif < edgeDistance.get()
                if (eagle.get() == "Silent") {
                    if (eagleSneaking != shouldEagle) {
                        mc.netHandler.addToSendQueue(C0BPacketEntityAction(player, if (shouldEagle) C0BPacketEntityAction.Action.START_SNEAKING else C0BPacketEntityAction.Action.STOP_SNEAKING))
                    }
                    eagleSneaking = shouldEagle
                } else {
                    mc.gameSettings.keyBindSneak.pressed = shouldEagle
                }
                placedBlocksWithoutEagle = 0
            } else {
                placedBlocksWithoutEagle++
            }
        }

        if (player.onGround) {
            if (mode.get() == "Rewinside") {
                strafe(0.2F)
                player.motionY = 0.0
            }
            when (zitterMode.get().lowercase()) {
                "off" -> {
                    return
                }

                "smooth" -> {
                    if (!GameSettings.isKeyDown(mc.gameSettings.keyBindRight)) {
                        mc.gameSettings.keyBindRight.pressed = false
                    }
                    if (!GameSettings.isKeyDown(mc.gameSettings.keyBindLeft)) {
                        mc.gameSettings.keyBindLeft.pressed = false
                    }

                    if (zitterTimer.hasTimePassed(100)) {
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

                "teleport" -> {
                    strafe(zitterSpeed.get())
                    val yaw = (player.rotationYaw + if (zitterDirection) 90.0 else -90.0).toRadians()
                    player.motionX -= sin(yaw) * zitterStrength.get()
                    player.motionZ += cos(yaw) * zitterStrength.get()
                    zitterDirection = !zitterDirection
                }
            }
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (mc.thePlayer == null) {
            return
        }

        val packet = event.packet

        if (packet is C09PacketHeldItemChange) {
            if (slot == packet.slotId) {
                event.cancelEvent()
                return
            }

            slot = packet.slotId
        }
    }

    @EventTarget
    fun onMotion(event: MotionEvent) {
        val rotation = RotationUtils.targetRotation

        if (rotations.get() && keepRotation.get() && rotation != null) {
            setRotation(rotation, 20)
        }

        if (event.eventState == EventState.POST) {
            update()
        }

        if (speedEffectDetect.get()) {
            if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                mc.thePlayer.motionX = mc.thePlayer.motionX * 0.85f
                mc.thePlayer.motionZ = mc.thePlayer.motionZ * 0.85f
            }
        }
            if(mc.thePlayer.onGround && MovementUtils.isMoving() && keepY.get()){
                mc.thePlayer.jump()
            }
    }

    @EventTarget
    fun onTick(event: TickEvent) {
        val target = targetPlace

        if (extraClicks.get()) {
            while (extraClick.clicks > 0) {
                extraClick.clicks--

                doPlaceAttempt()
            }
        }

        if (target == null) {
            if (placeDelay.get()) {
                delayTimer.reset()
            }
            return
        }

        val raycastProperly = !(mode.get() == "Expand" && expandLength.get() > 1 || shouldGoDown)

        performBlockRaytrace(currRotation, mc.playerController.blockReachDistance).let {
            if (it != null && it.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && it.blockPos == target.blockPos && (!raycastProperly || it.sideHit == target.enumFacing)) {
                val result = if (raycastProperly) {
                    PlaceInfo(it.blockPos, it.sideHit, it.hitVec)
                } else {
                    target
                }

                if (shouldPlace()) {
                    place(result)
                }
            }
        }
    }

    fun update() {
        val player = mc.thePlayer ?: return
        val holdingItem = player.heldItem?.item is ItemBlock

        if (!holdingItem && (autoBlock.get() == "Off" || InventoryUtils.findAutoBlockBlock() == null)) {
            return
        }

        findBlock(mode.get() == "Expand" && expandLength.get() > 1, searchMode.get() == "Area")
    }

    private fun setRotation(rotation: Rotation, ticks: Int) {
        val player = mc.thePlayer ?: return

        if (silentRotation.get()) {
            setTargetRotation(
                rotation,
                ticks,
            )
        } else {
            rotation.toPlayer(player)
        }
    }

    // Search for new target block
    private fun findBlock(expand: Boolean, area: Boolean) {
        val player = mc.thePlayer ?: return

        val blockPosition = if (shouldGoDown) {
            if (player.posY == player.posY.roundToInt() + 0.5) {
                BlockPos(player.posX, player.posY - 0.6, player.posZ)
            } else {
                BlockPos(player.posX, player.posY - 0.6, player.posZ).down()
            }
        } else if (keepY.get() && launchY <= player.posY && Keyboard.isKeyDown(mc.gameSettings.keyBindJump.keyCode)) {
            BlockPos(player.posX, launchY - 1.0, player.posZ)
        } else if (player.posY == player.posY.roundToInt() + 0.5) {
            BlockPos(player)
        } else {
            BlockPos(player).down()
        }

        if (!expand && (!BlockUtils.isReplaceable(blockPosition) || search(blockPosition, !shouldGoDown, area))) {
            return
        }

        if (expand) {
            val yaw = player.rotationYaw.toRadiansD()
            val x = if (omniDirectionalExpand.get()) -sin(yaw).roundToInt() else player.horizontalFacing.directionVec.x
            val z = if (omniDirectionalExpand.get()) cos(yaw).roundToInt() else player.horizontalFacing.directionVec.z
            for (i in 0 until expandLength.get()) {
                if (search(blockPosition.add(x * i, 0, z * i), false, area)) {
                    return
                }
            }
        } else if (search.get()) {
            for (x in -1..1) {
                for (z in -1..1) {
                    if (search(blockPosition.add(x, 0, z), !shouldGoDown, area)) {
                        return
                    }
                }
            }
        }
    }

    private fun place(placeInfo: PlaceInfo) {
        val player = mc.thePlayer ?: return
        val world = mc.theWorld ?: return

        if(sprint.get() == "PlaceOff"){
            player.isSprinting = false
        }

        if (!delayTimer.hasTimePassed(delay.toLong()) || keepY.get() && launchY - 1 != placeInfo.vec3.yCoord.toInt()) {
            return
        }

        var itemStack = player.heldItem
        //TODO: blacklist more blocks than only bushes
        if (itemStack == null || itemStack.item !is ItemBlock || (itemStack.item as ItemBlock).block is BlockBush || player.heldItem.stackSize <= 0) {
            val blockSlot = InventoryUtils.findAutoBlockBlock()

            when (autoBlock.get().lowercase()) {
                "off" -> return

                "pick" -> {
                    player.inventory.currentItem = blockSlot - 36
                    mc.playerController.updateController()
                }

                "spoof", "switch" -> {
                    if (blockSlot - 36 != slot) {
                         mc.netHandler.addToSendQueue(C09PacketHeldItemChange(blockSlot - 36))
                    }
                }
            }
            itemStack = player.inventoryContainer.getSlot(blockSlot).stack
        }

        if (mc.playerController.onPlayerRightClick(
                player, world, itemStack, placeInfo.blockPos, placeInfo.enumFacing, placeInfo.vec3
            )
        ) {
            delayTimer.reset()
            delay = if (!placeDelay.get()) 0 else randomDelay(minDelay.get(), maxDelayValue.get()).toInt()

            if (player.onGround) {
                player.motionX *= speedModifier.get()
                player.motionZ *= speedModifier.get()
            }

            if (swing.get()) {
                player.swingItem()
            } else {
                 mc.netHandler.addToSendQueue(C0APacketAnimation())
            }
        } else {
            if (mc.playerController.sendUseItem(player, world, itemStack)) {
                mc.entityRenderer.itemRenderer.resetEquippedProgress2()
            }
        }

        if (autoBlock.get() == "Switch") {
            if (slot != player.inventory.currentItem) {
                 mc.netHandler.addToSendQueue(C09PacketHeldItemChange(player.inventory.currentItem))
            }
        }

        targetPlace = null
    }

    private fun doPlaceAttempt() {
        val player = mc.thePlayer ?: return
        val world = mc.theWorld ?: return

        if (slot == -1) {
            return
        }

        val stack = player.inventoryContainer.getSlot(slot + 36).stack ?: return

        if (stack.item !is ItemBlock || InventoryUtils.BLOCK_BLACKLIST.contains((stack.item as ItemBlock).block)) {
            return
        }

        val block = stack.item as ItemBlock

        val raytrace = performBlockRaytrace(currRotation, mc.playerController.blockReachDistance) ?: return

        val isOnTheSamePos = raytrace.blockPos.x == player.posX.toInt() && raytrace.blockPos.z == player.posZ.toInt()

        val isBlockBelowPlayer = if (keepY.get()) {
            raytrace.blockPos.y == launchY - 1 && !block.canPlaceBlockOnSide(
                world, raytrace.blockPos, EnumFacing.UP, player, stack
            )
        } else {
            raytrace.blockPos.y <= player.posY - 1 && (placementAttempt.get() == "Independent" && isOnTheSamePos || !block.canPlaceBlockOnSide(
                world, raytrace.blockPos, EnumFacing.UP, player, stack
            ))
        }

        val shouldPlace = placementAttempt.get() == "Independent" || !block.canPlaceBlockOnSide(
            world, raytrace.blockPos, raytrace.sideHit, player, stack
        )

        if (raytrace.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK || !isBlockBelowPlayer || !shouldPlace) {
            return
        }

        if (mc.playerController.onPlayerRightClick(
                player, world, stack, raytrace.blockPos, raytrace.sideHit, raytrace.hitVec
            )
        ) {
            if (swing.get()) {
                player.swingItem()
            } else {
                 mc.netHandler.addToSendQueue(C0APacketAnimation())
            }
        } else {
            if (mc.playerController.sendUseItem(player, world, stack)) {
                mc.entityRenderer.itemRenderer.resetEquippedProgress2()
            }
        }
    }

    // Disabling module
    override fun onDisable() {
        val player = mc.thePlayer ?: return

        if (!GameSettings.isKeyDown(mc.gameSettings.keyBindSneak)) {
            mc.gameSettings.keyBindSneak.pressed = false
            if (eagleSneaking) {
                 mc.netHandler.addToSendQueue(C0BPacketEntityAction(player, C0BPacketEntityAction.Action.STOP_SNEAKING))
            }
        }

        if (!GameSettings.isKeyDown(mc.gameSettings.keyBindRight)) {
            mc.gameSettings.keyBindRight.pressed = false
        }
        if (!GameSettings.isKeyDown(mc.gameSettings.keyBindLeft)) {
            mc.gameSettings.keyBindLeft.pressed = false
        }

        targetPlace = null
        mc.timer.timerSpeed = 1f

        if (slot != player.inventory.currentItem) {
            mc.netHandler.addToSendQueue(C09PacketHeldItemChange(player.inventory.currentItem))
        }
    }

    // Entity movement event
    @EventTarget
    fun onMove(event: MoveEvent) {
        val player = mc.thePlayer ?: return

        if (!safeWalk.get() || shouldGoDown) {
            return
        }

        if (airSafe.get() || player.onGround) {
            event.isSafeWalk = true
        }
    }

    @EventTarget
    fun onJump(event: JumpEvent) {
        if (towerEnabled.get()) {
            event.cancelEvent()
        }
    }

    // Visuals
    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        val player = mc.thePlayer ?: return

        val shouldBother =
            !(shouldGoDown || mode.get() == "Expand" && expandLength.get() > 1) && extraClicks.get() && MovementUtils.isMoving()

        if (shouldBother) {
            currRotation.let {
                performBlockRaytrace(it, mc.playerController.blockReachDistance)?.let { raytrace ->
                    val timePassed = System.currentTimeMillis() - extraClick.lastClick >= extraClick.delay

                    if (raytrace.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && timePassed) {
                        extraClick = ExtraClickInfo(
                            randomClickDelay(extraClickMinCPS.get(), extraClickMaxCPSValue.get()).toInt(),
                            System.currentTimeMillis(),
                            extraClick.clicks + 1
                        )
                    }
                }
            }
        }
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
            if (blurValue.get()) BlurUtils.blurArea(
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
            val alpha = 255f
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


    /**
     * Search for placeable block
     *
     * @param blockPosition pos
     * @param raycast visible
     * @param area spot
     * @return
     */

    private fun search(blockPosition: BlockPos, raycast: Boolean, area: Boolean): Boolean {
        val player = mc.thePlayer ?: return false

        if (!BlockUtils.isReplaceable(blockPosition)) {
            return false
        }

        val maxReach = mc.playerController.blockReachDistance

        val eyes = player.eyes
        var placeRotation: PlaceRotation? = null

        var currPlaceRotation: PlaceRotation?

        for (side in EnumFacing.values()) {
            val neighbor = blockPosition.offset(side)

            if (!BlockUtils.canBeClicked(neighbor)) {
                continue
            }

            if (!area) {
                currPlaceRotation =
                    findTargetPlace(blockPosition, neighbor, Vec3(0.5, 0.5, 0.5), side, eyes, maxReach, raycast)
                        ?: continue

                if (placeRotation == null || RotationUtils.getRotationDifference(
                        currPlaceRotation.rotation, currRotation
                    ) < RotationUtils.getRotationDifference(placeRotation.rotation, currRotation)
                ) {
                    placeRotation = currPlaceRotation
                }
            } else {
                var x = 0.1
                while (x < 0.9) {
                    var y = 0.1
                    while (y < 0.9) {
                        var z = 0.1
                        while (z < 0.9) {
                            currPlaceRotation =
                                findTargetPlace(blockPosition, neighbor, Vec3(x, y, z), side, eyes, maxReach, raycast)

                            if (currPlaceRotation == null) {
                                z += 0.1
                                continue
                            }

                            if (placeRotation == null || RotationUtils.getRotationDifference(
                                    currPlaceRotation.rotation, currRotation
                                ) < RotationUtils.getRotationDifference(placeRotation.rotation, currRotation)
                            ) {
                                placeRotation = currPlaceRotation
                            }

                            z += 0.1
                        }
                        y += 0.1
                    }
                    x += 0.1
                }
            }
        }

        placeRotation ?: return false

        if (rotations.get()) {
            val limitedRotation = RotationUtils.limitAngleChange(
                currRotation, placeRotation.rotation, RandomUtils.nextFloat(minTurnSpeed.get(), maxTurnSpeed.get())
            )

            setRotation(limitedRotation, keepTicks.get())
        }
        targetPlace = placeRotation.placeInfo
        return true
    }

    /**
     * For expand scaffold, fixes vector values that should match according to direction vector
     */
    private fun modifyVec(original: Vec3, direction: EnumFacing, pos: Vec3, shouldModify: Boolean): Vec3 {
        if (!shouldModify) {
            return original
        }

        val x = original.xCoord
        val y = original.yCoord
        val z = original.zCoord

        val side = direction.opposite

        return when (side.axis ?: return original) {
            EnumFacing.Axis.Y -> Vec3(x, pos.yCoord + side.directionVec.y.coerceAtLeast(0), z)
            EnumFacing.Axis.X -> Vec3(pos.xCoord + side.directionVec.x.coerceAtLeast(0), y, z)
            EnumFacing.Axis.Z -> Vec3(x, y, pos.zCoord + side.directionVec.z.coerceAtLeast(0))
        }

    }

    private fun findTargetPlace(
        pos: BlockPos, offsetPos: BlockPos, vec3: Vec3, side: EnumFacing, eyes: Vec3, maxReach: Float, raycast: Boolean
    ): PlaceRotation? {
        val world = mc.theWorld ?: return null

        val vec = Vec3(pos).add(vec3).addVector(
            side.directionVec.x * vec3.xCoord, side.directionVec.y * vec3.yCoord, side.directionVec.z * vec3.zCoord
        )

        val distance = eyes.distanceTo(vec)

        if (raycast && (distance > maxReach || world.rayTraceBlocks(eyes, vec, false, true, false) != null)) {
            return null
        }

        val diff = vec.subtract(eyes)

        if (side.axis != EnumFacing.Axis.Y) {
            val dist = abs(if (side.axis == EnumFacing.Axis.Z) diff.zCoord else diff.xCoord)

            if (dist < minDist.get()) {
                return null
            }
        }

        var rotation = RotationUtils.toRotation(vec, false)

        rotation = if (stabilizedRotation.get()) {
            Rotation(round(rotation.yaw / 45f) * 45f, rotation.pitch)
        } else {
            rotation
        }

        // If the current rotation already looks at the target block and side, then return right here
        performBlockRaytrace(currRotation, maxReach)?.let { raytrace ->
            if (raytrace.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && raytrace.blockPos == offsetPos && (!raycast || raytrace.sideHit == side.opposite)) {
                return PlaceRotation(
                    PlaceInfo(
                        raytrace.blockPos, side.opposite, modifyVec(raytrace.hitVec, side, Vec3(offsetPos), !raycast)
                    ), currRotation
                )
            }
        }

        val raytrace = performBlockRaytrace(rotation, maxReach) ?: return null

        if (raytrace.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && raytrace.blockPos == offsetPos && (!raycast || raytrace.sideHit == side.opposite)) {
            return PlaceRotation(
                PlaceInfo(
                    raytrace.blockPos, side.opposite, modifyVec(raytrace.hitVec, side, Vec3(offsetPos), !raycast)
                ), rotation
            )
        }

        return null
    }

    private fun performBlockRaytrace(rotation: Rotation, maxReach: Float): MovingObjectPosition? {
        val player = mc.thePlayer ?: return null
        val world = mc.theWorld ?: return null

        val eyes = player.eyes
        val rotationVec = RotationUtils.getVectorForRotation(rotation)

        val reach =
            eyes.addVector(rotationVec.xCoord * maxReach, rotationVec.yCoord * maxReach, rotationVec.zCoord * maxReach)

        return world.rayTraceBlocks(eyes, reach, false, false, true)
    }

    private fun towering(): Boolean {
        return towerEnabled.get() && Keyboard.isKeyDown(mc.gameSettings.keyBindJump.keyCode)
    }

    private fun fakeJump() {
        mc.thePlayer.isAirBorne = true
        mc.thePlayer.triggerAchievement(StatList.jumpStat)
    }

    private fun tower() {
        mc.thePlayer.cameraYaw = 0f
        mc.thePlayer.cameraPitch = 0f
        if (noMoveOnlyValue.get()) {
            mc.thePlayer.motionZ = 0.0
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.jumpMovementFactor = 0.0f
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


    /**
     * Returns the amount of blocks
     */
    private val blocksAmount: Int
        get() {
            var amount = 0
            for (i in 36..44) {
                val stack = mc.thePlayer.inventoryContainer.getSlot(i).stack ?: continue
                val item = stack.item
                if (item is ItemBlock) {
                    val block = item.block
                    val heldItem = mc.thePlayer.heldItem
                    if (heldItem != null && heldItem == stack || block !in InventoryUtils.BLOCK_BLACKLIST && block !is BlockBush) {
                        amount += stack.stackSize
                    }
                }
            }
            return amount
        }
    override val tag
        get() = mode.get()

    data class ExtraClickInfo(val delay: Int, val lastClick: Long, var clicks: Int)
}
