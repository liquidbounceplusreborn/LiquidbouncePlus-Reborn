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
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.NotifyType
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.*
import net.ccbluex.liquidbounce.utils.block.BlockUtils.canBeClicked
import net.ccbluex.liquidbounce.utils.block.BlockUtils.isReplaceable
import net.ccbluex.liquidbounce.utils.block.PlaceInfo
import net.ccbluex.liquidbounce.utils.block.PlaceInfo.Companion.get
import net.ccbluex.liquidbounce.utils.extensions.eyes
import net.ccbluex.liquidbounce.utils.extensions.rayTraceCustom
import net.ccbluex.liquidbounce.utils.extensions.rotation
import net.ccbluex.liquidbounce.utils.math.toRadiansD
import net.ccbluex.liquidbounce.utils.misc.RandomUtils
import net.ccbluex.liquidbounce.utils.render.BlurUtils.blurArea
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.utils.timer.TickTimer
import net.ccbluex.liquidbounce.utils.timer.TimerUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.settings.GameSettings
import net.minecraft.entity.passive.EntityPig
import net.minecraft.init.Blocks
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.network.play.client.C09PacketHeldItemChange
import net.minecraft.network.play.client.C0APacketAnimation
import net.minecraft.network.play.client.C0BPacketEntityAction
import net.minecraft.potion.Potion
import net.minecraft.stats.StatList
import net.minecraft.util.*
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.util.*
import kotlin.math.*

@ModuleInfo(
    name = "Scaffold",
    description = "Automatically places blocks beneath your feet.",
    category = ModuleCategory.WORLD,
    keyBind = Keyboard.KEY_I
)
class Scaffold : Module() {
    /**
     * OPTIONS (Tower)
     */
    // Global settings
    private val towerEnabled = BoolValue("EnableTower", false)
    private val towerModeValue = ListValue(
        "TowerMode", arrayOf(
            "Jump",
            "Motion",
            "StableMotion",
            "ConstantMotion",
            "MotionTP",
            "Packet",
            "Teleport",
            "AAC3.3.9",
            "AAC3.6.4",
            "Verus",
            "Hypixel",
            "BlocksMC",
        ), "Motion"
    ) { towerEnabled.get() }
    private val noMoveOnlyValue = BoolValue("NoMove", true) { towerEnabled.get() }
    private val towerTimerValue = FloatValue("TowerTimer", 1f, 0.1f, 10f) { towerEnabled.get() }

    // Jump mode
    private val jumpMotionValue = FloatValue("JumpMotion", 0.42f, 0.3681289f, 0.79f) {
        towerEnabled.get() && towerModeValue.get().equals("Jump", ignoreCase = true)
    }
    private val jumpDelayValue = IntegerValue("JumpDelay", 0, 0, 20) {
        towerEnabled.get() && towerModeValue.get().equals("Jump", ignoreCase = true)
    }

    // StableMotion
    private val stableMotionValue = FloatValue("StableMotion", 0.41982f, 0.1f, 1f) {
        towerEnabled.get() && towerModeValue.get().equals("StableMotion", ignoreCase = true)
    }
    private val stableFakeJumpValue = BoolValue("StableFakeJump", false) {
        towerEnabled.get() && towerModeValue.get().equals("StableMotion", ignoreCase = true)
    }
    private val stableStopValue = BoolValue("StableStop", false) {
        towerEnabled.get() && towerModeValue.get().equals("StableMotion", ignoreCase = true)
    }
    private val stableStopDelayValue = IntegerValue("StableStopDelay", 1500, 0, 5000) {
        towerEnabled.get() && towerModeValue.get().equals("StableMotion", ignoreCase = true) && stableStopValue.get()
    }

    // ConstantMotion
    private val constantMotionValue = FloatValue("ConstantMotion", 0.42f, 0.1f, 1f) {
        towerEnabled.get() && towerModeValue.get().equals("ConstantMotion", ignoreCase = true)
    }
    private val constantMotionJumpGroundValue = FloatValue("ConstantMotionJumpGround", 0.79f, 0.76f, 1f) {
        towerEnabled.get() && towerModeValue.get().equals("ConstantMotion", ignoreCase = true)
    }

    // Teleport
    private val teleportHeightValue = FloatValue("TeleportHeight", 1.15f, 0.1f, 5f) {
        towerEnabled.get() && towerModeValue.get().equals("Teleport", ignoreCase = true)
    }
    private val teleportDelayValue = IntegerValue("TeleportDelay", 0, 0, 20) {
        towerEnabled.get() && towerModeValue.get().equals("Teleport", ignoreCase = true)
    }
    private val teleportGroundValue = BoolValue("TeleportGround", true) {
        towerEnabled.get() && towerModeValue.get().equals("Teleport", ignoreCase = true)
    }
    private val teleportNoMotionValue = BoolValue("TeleportNoMotion", false) {
        towerEnabled.get() && towerModeValue.get().equals("Teleport", ignoreCase = true)
    }

    /**
     * OPTIONS (Scaffold)
     */
    // Mode
    val modeValue = ListValue("Mode", arrayOf("Normal", "Expand"), "Normal")

    // Delay
    private val placeableDelay = BoolValue("PlaceableDelay", false)
    private val maxDelayValue: IntegerValue = object : IntegerValue("MaxDelay", 0, 0, 1000, "ms") {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val i = minDelayValue.get()
            if (i > newValue) set(i)
        }
    }
    private val minDelayValue: IntegerValue = object : IntegerValue("MinDelay", 0, 0, 1000, "ms") {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val i = maxDelayValue.get()
            if (i < newValue) set(i)
        }
    }

    // idfk what is this
    private val smartDelay = BoolValue("SmartDelay", true)

    // AutoBlock
    private val autoBlockMode = ListValue("AutoBlock", arrayOf("Spoof", "Switch", "Off"), "Spoof")
    private val stayAutoBlock = BoolValue("StayAutoBlock", false) {
        !autoBlockMode.get().equals("off", ignoreCase = true)
    }

    private val sortByHighestAmount = BoolValue("SortByHighestAmount", false) {
        !autoBlockMode.get().equals("off", ignoreCase = true)
    }

    //make sprint compatible with tower.add sprint tricks
    val sprintModeValue =
        ListValue("SprintMode", arrayOf("Same", "Ground", "Air","NoPacket", "PlaceOff", "PlaceOn", "FallDownOff","GrimOff","Legit", "Off"), "Off")

    // Basic stuff
    private val swingValue = BoolValue("Swing", true)
    private val downValue = BoolValue("Down", false)
    private val placeModeValue = ListValue("PlaceTiming", arrayOf("Pre", "Post", "Legit"), "Post")

    // Eagle
    private val eagleValue = BoolValue("Eagle", false)
    private val eagleSilentValue = BoolValue("EagleSilent", false) { eagleValue.get() }
    private val blocksToEagleValue = IntegerValue("BlocksToEagle", 0, 0, 10) { eagleValue.get() }
    private val eagleEdgeDistanceValue = FloatValue("EagleEdgeDistance", 0.2f, 0f, 0.5f, "m") { eagleValue.get() }

    // Expand
    private val omniDirectionalExpand = BoolValue("OmniDirectionalExpand", true) {
        modeValue.get().equals("expand", ignoreCase = true)
    }
    private val expandLengthValue = IntegerValue("ExpandLength", 5, 1, 6, " blocks") {
        modeValue.get().equals("expand", ignoreCase = true)
    }

    // Rotations
    private val rotationsValue = BoolValue("Rotations", true)
    private val noHitCheckValue = BoolValue("NoHitCheck", false) { rotationsValue.get() }
    private val keepRotation = BoolValue("KeepRotation", false) { rotationsValue.get() }
    private val stabilizedRotation = BoolValue("StabilizedRotation", false) { rotationsValue.get() && (rotationModeValue.isMode("Normal") ||rotationModeValue.isMode("GrimTest") || rotationModeValue.isMode("GrimTest2") ) }
    private val grimLock = BoolValue("GrimLock", true){ rotationsValue.get() && (rotationModeValue.isMode("GrimTest") || rotationModeValue.isMode("GrimTest2")) }
    private val rotationModeValue = ListValue(
        "RotationMode",
        arrayOf("Normal", "Spin", "Custom", "Novoline","Intave","GrimTest","GrimTest2","Rise","Rise2"),
        "Normal") // searching reason
    private val maxTurnSpeed: FloatValue =
        object : FloatValue("MaxTurnSpeed", 180f, 0f, 180f, "°", { rotationsValue.get() }) {
            override fun onChanged(oldValue: Float, newValue: Float) {
                val i = minTurnSpeed.get()
                if (i > newValue) set(i)
            }
        }
    private val minTurnSpeed: FloatValue =
        object : FloatValue("MinTurnSpeed", 180f, 0f, 180f, "°", { rotationsValue.get() }) {
            override fun onChanged(oldValue: Float, newValue: Float) {
                val i = maxTurnSpeed.get()
                if (i < newValue) set(i)
            }
        }
    private val customYawValue = FloatValue("Custom-Yaw", 180f, -180f, 180f, "°") {
        rotationModeValue.get().equals("custom", ignoreCase = true)
    }
    private val customPitchValue = FloatValue("Custom-Pitch", 82f, -90f, 90f, "°") {
        rotationModeValue.get().equals("custom", ignoreCase = true)
    }
    private val speenSpeedValue = FloatValue("Spin-Speed", 5f, -90f, 90f, "°") {
        rotationModeValue.get().equals("spin", ignoreCase = true)
    }
    private val speenPitchValue = FloatValue("Spin-Pitch", 90f, -90f, 90f, "°") {
        rotationModeValue.get().equals("spin", ignoreCase = true)
    }

    private val placeConditionValue =
        ListValue("Place-Condition", arrayOf("Air", "FallDown", "NegativeMotion", "Always"), "Always")
    private val rotationStrafeValue = BoolValue("RotationStrafe", false)
    private val speedPotSlow = BoolValue("SpeedPotDetect", true)

    // Zitter
    private val zitterValue = BoolValue("Zitter", false) { !isTowerOnly }
    private val zitterModeValue =
        ListValue("ZitterMode", arrayOf("Teleport", "Smooth"), "Teleport") { !isTowerOnly && zitterValue.get() }
    private val zitterSpeed = FloatValue("ZitterSpeed", 0.13f, 0.1f, 0.3f) {
        !isTowerOnly && zitterValue.get() && zitterModeValue.get().equals("teleport", ignoreCase = true)
    }
    private val zitterStrength = FloatValue("ZitterStrength", 0.072f, 0.05f, 0.2f) {
        !isTowerOnly && zitterValue.get() && zitterModeValue.get().equals("teleport", ignoreCase = true)
    }
    private val zitterDelay = IntegerValue("ZitterDelay", 100, 0, 500, "ms") {
        !isTowerOnly && zitterValue.get() && zitterModeValue.get().equals("smooth", ignoreCase = true)
    }

    // Game
    private val timerValue = FloatValue("Timer", 1f, 0.1f, 10f) { !isTowerOnly }
    private val speedModifierValue = FloatValue("SpeedModifier", 1f, 0f, 2f, "x")
    private val xzMultiplier = FloatValue("XZ-Multiplier", 1f, 0f, 4f, "x")
    private val customSpeedValue = BoolValue("CustomSpeed", false)
    private val customMoveSpeedValue = FloatValue("CustomMoveSpeed", 0.3f, 0f, 5f) { customSpeedValue.get() }

    // Safety

    private val sameYValue = BoolValue("SameY", false) { !towerEnabled.get() }
    private val autoJumpValue = BoolValue("AutoJump", false)
    private val motionY = BoolValue("MotionY", false)
    private val motionYValue = FloatValue("MotionYValue", 0.42f, 0f, 0.84f) { motionY.get() }
    private val smartSpeedValue = BoolValue("SmartSpeed", false)
    private val parkourValue = BoolValue("Parkour", true)
    private val safeWalkValue = BoolValue("SafeWalk", true)
    private val airSafeValue = BoolValue("AirSafe", false) { safeWalkValue.get() }
    private val autoDisableSpeedValue = BoolValue("AutoDisable-Speed", true)

    // Visuals
    private val counterDisplayValue =
        ListValue("Counter", arrayOf("Off", "Simple", "Advanced", "Sigma", "Novoline", "Exhibition"), "Simple")
    private val modeDisplay = BoolValue("ModeDisplay", true)
    private val markValue = BoolValue("Mark", false)
    private val redValue = IntegerValue("Red", 0, 0, 255) { markValue.get() }
    private val greenValue = IntegerValue("Green", 120, 0, 255) { markValue.get() }
    private val blueValue = IntegerValue("Blue", 255, 0, 255) { markValue.get() }
    private val alphaValue = IntegerValue("Alpha", 120, 0, 255) { markValue.get() }
    private val blurValue = BoolValue("Blur-Advanced", false) {
        counterDisplayValue.get().equals("advanced", ignoreCase = true)
    }
    private val blurStrength = FloatValue("Blur-Strength", 1f, 0f, 30f, "x") {
        counterDisplayValue.get().equals("advanced", ignoreCase = true)
    }

    /**
     * MODULE
     */
    // Target block
    private var targetPlace: PlaceInfo? = null

    // Launch position
    private var launchY = 0
    private var faceBlock = false

    // Rotation lock
    private var lockRotation: Rotation? = null
    private val currRotation
        get() = RotationUtils.targetRotation ?: mc.thePlayer.rotation

    // Auto block slot
    private var slot = 0
    private var lastSlot = 0

    // Zitter Smooth
    private var zitterDirection = false

    // Delay
    private val delayTimer = MSTimer()
    private val towerDelayTimer = MSTimer()
    private val zitterTimer = MSTimer()
    private var delay: Long = 0

    // Eagle
    private var placedBlocksWithoutEagle = 0
    private var eagleSneaking = false

    // Down
    private var shouldGoDown = false
    private var alpha = 0f

    // Render thingy
    private var progress = 0f
    private var spinYaw = 0f
    private var lastMS = 0L

    // Mode stuff
    private val timer = TickTimer()
    private var jumpGround = 0.0
    private var verusState = 0
    private var verusJumped = false
    private var offGroundTicks = 0
    private var onGroundTicks = 0

    private val isTowerOnly: Boolean
        get() = towerEnabled.get()


    private fun towerActivation(): Boolean {
        return towerEnabled.get() && mc.gameSettings.keyBindJump.isKeyDown
    }

    /**
     * Enable module
     */
    override fun onEnable() {
        if (mc.thePlayer == null) return
        progress = 0f
        spinYaw = 0f
        launchY = mc.thePlayer.posY.toInt()
        lastSlot = mc.thePlayer.inventory.currentItem
        slot = mc.thePlayer.inventory.currentItem
        if (autoDisableSpeedValue.get() && LiquidBounce.moduleManager.getModule(
                Speed::class.java
            )!!.state
        ) {
            LiquidBounce.moduleManager.getModule(
                Speed::class.java
            )!!.state = false
            LiquidBounce.hud.addNotification(
                Notification(
                    "Speed",
                    "Speed is disabled to prevent flags/errors.",
                    NotifyType.WARNING,
                    1500,
                    500
                )
            )
        }
        faceBlock = false
        lastMS = System.currentTimeMillis()
    }

    //Send jump packets, bypasses Hypixel.
    private fun fakeJump() {
        mc.thePlayer.isAirBorne = true
        mc.thePlayer.triggerAchievement(StatList.jumpStat)
    }

    /**
     * Move player
     */
    private fun move(event: MotionEvent) {
        mc.thePlayer.cameraYaw = 0f
        mc.thePlayer.cameraPitch = 0f
        if (noMoveOnlyValue.get()) {
            mc.thePlayer.motionZ = 0.0
            mc.thePlayer.motionX = mc.thePlayer.motionZ
            mc.thePlayer.jumpMovementFactor = 0f
        }
        when (towerModeValue.get().lowercase(Locale.getDefault())) {
            "jump" -> if (mc.thePlayer.onGround && timer.hasTimePassed(jumpDelayValue.get())) {
                fakeJump()
                mc.thePlayer.motionY = jumpMotionValue.get().toDouble()
                timer.reset()
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

            "packet" -> if (mc.thePlayer.onGround && timer.hasTimePassed(2)) {
                fakeJump()
                mc.netHandler.addToSendQueue(
                    C04PacketPlayerPosition(
                        mc.thePlayer.posX,
                        mc.thePlayer.posY + 0.42, mc.thePlayer.posZ, false
                    )
                )
                mc.netHandler.addToSendQueue(
                    C04PacketPlayerPosition(
                        mc.thePlayer.posX,
                        mc.thePlayer.posY + 0.76, mc.thePlayer.posZ, false
                    )
                )
                mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + 1.08, mc.thePlayer.posZ)
                timer.reset()
            }

            "teleport" -> {
                if (teleportNoMotionValue.get()) mc.thePlayer.motionY = 0.0
                if ((mc.thePlayer.onGround || !teleportGroundValue.get()) && timer.hasTimePassed(teleportDelayValue.get())) {
                    fakeJump()
                    mc.thePlayer.setPositionAndUpdate(
                        mc.thePlayer.posX,
                        mc.thePlayer.posY + teleportHeightValue.get(),
                        mc.thePlayer.posZ
                    )
                    timer.reset()
                }
            }

            "stablemotion" -> {
                if (stableFakeJumpValue.get()) fakeJump()
                mc.thePlayer.motionY = stableMotionValue.get().toDouble()
                if (stableStopValue.get() && towerDelayTimer.hasTimePassed(stableStopDelayValue.get().toLong())) {
                    mc.thePlayer.motionY = -0.28
                    towerDelayTimer.reset()
                }
            }

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

            "aac3.3.9" -> {
                if (mc.thePlayer.onGround) {
                    fakeJump()
                    mc.thePlayer.motionY = 0.4001
                }
                mc.timer.timerSpeed = 1f
                if (mc.thePlayer.motionY < 0) {
                    mc.thePlayer.motionY -= 0.00000945
                    mc.timer.timerSpeed = 1.6f
                }
                if (mc.thePlayer.ticksExisted % 4 == 1) {
                    mc.thePlayer.motionY = 0.4195464
                    mc.thePlayer.setPosition(mc.thePlayer.posX - 0.035, mc.thePlayer.posY, mc.thePlayer.posZ)
                } else if (mc.thePlayer.ticksExisted % 4 == 0) {
                    mc.thePlayer.motionY = -0.5
                    mc.thePlayer.setPosition(mc.thePlayer.posX + 0.035, mc.thePlayer.posY, mc.thePlayer.posZ)
                }
            }

            "aac3.6.4" -> if (mc.thePlayer.ticksExisted % 4 == 1) {
                mc.thePlayer.motionY = 0.4195464
                mc.thePlayer.setPosition(mc.thePlayer.posX - 0.035, mc.thePlayer.posY, mc.thePlayer.posZ)
            } else if (mc.thePlayer.ticksExisted % 4 == 0) {
                mc.thePlayer.motionY = -0.5
                mc.thePlayer.setPosition(mc.thePlayer.posX + 0.035, mc.thePlayer.posY, mc.thePlayer.posZ)
            }

            "verus" -> {
                if (mc.theWorld.getCollidingBoundingBoxes(
                        mc.thePlayer,
                        mc.thePlayer.entityBoundingBox.offset(0.0, -0.01, 0.0)
                    ).isNotEmpty() && mc.thePlayer.onGround && mc.thePlayer.isCollidedVertically
                ) {
                    verusState = 0
                    verusJumped = true
                }
                if (verusJumped) {
                    MovementUtils.strafe()
                    when (verusState) {
                        0 -> {
                            fakeJump()
                            mc.thePlayer.motionY = 0.41999998688697815
                            ++verusState
                        }

                        1 -> ++verusState
                        2 -> ++verusState
                        3 -> {
                            event.onGround = true
                            mc.thePlayer.motionY = 0.0
                            ++verusState
                        }

                        4 -> ++verusState
                    }
                    verusJumped = false
                }
                verusJumped = true
            }

            "hypixel" -> {
                hypixelTower()
            }
            "blocksmc" -> {
                if (mc.thePlayer.posY % 1 <= 0.00153598) {
                    mc.thePlayer.setPosition(
                        mc.thePlayer.posX,
                        floor(mc.thePlayer.posY),
                        mc.thePlayer.posZ
                    )
                    mc.thePlayer.motionY = 0.42
                } else if (mc.thePlayer.posY % 1 < 0.1 && offGroundTicks != 0) {
                    mc.thePlayer.setPosition(
                        mc.thePlayer.posX,
                        floor(mc.thePlayer.posY),
                        mc.thePlayer.posZ
                    )
                }
            }
        }
    }

    private fun hypixelTower() {
        if (mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, mc.thePlayer.entityBoundingBox.offset(0.0, -0.76, 0.0))
                .isNotEmpty() && mc.theWorld.getCollidingBoundingBoxes(
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

    private fun rotation(){
            val sameY = sameYValue.get()
            val smartSpeed = smartSpeedValue.get() && LiquidBounce.moduleManager.getModule(Speed::class.java)!!.state
            val autojump = autoJumpValue.get() && !GameSettings.isKeyDown(mc.gameSettings.keyBindJump)
            val blockPos = BlockPos(mc.thePlayer.posX, if ((!towerActivation() || smartSpeed || sameY || autojump) && launchY <= mc.thePlayer.posY) launchY - 1.0 else mc.thePlayer.posY - (if (mc.thePlayer.posY == mc.thePlayer.posY.toInt() + 0.5) 0.0 else 1.0) - if (shouldGoDown) 1.0 else 0.0, mc.thePlayer.posZ)
            val blockData = get(blockPos)
            when (rotationModeValue.get()) {
                "Novoline" -> {
                    val entity = EntityPig(mc.theWorld)
                    if (blockData != null) {
                        entity.posX = blockData.blockPos.x + 0.5
                    }
                    if (blockData != null) {
                        entity.posY = blockData.blockPos.y + 0.5
                    }
                    if (blockData != null) {
                        entity.posZ = blockData.blockPos.z + 0.5
                    }

                    lockRotation = RotationUtils.getAngles(entity)
                    faceBlock = true
                }

                "Spin" -> {
                    spinYaw += speenSpeedValue.get()
                    spinYaw = MathHelper.wrapAngleTo180_float(spinYaw)
                    lockRotation = Rotation(spinYaw, speenPitchValue.get())
                    faceBlock = true
                }

                "Normal" -> {
                    faceBlock = true
                }

                "Custom" -> {
                    lockRotation = Rotation(mc.thePlayer.rotationYaw + customYawValue.get(), customPitchValue.get())
                    faceBlock = true
                }

                "Intave" -> {
                    faceBlock = true
                }
                "GrimTest" ->{
                    if(offGroundTicks >= 3){
                        faceBlock = true
                    }else{
                        lockRotation = Rotation(mc.thePlayer.rotationYaw, if(grimLock.get()){
                            80f
                        } else{
                            mc.thePlayer.rotationPitch
                        }
                        )
                        faceBlock = false
                    }
                }
                "GrimTest2" ->{
                    if(!mc.thePlayer.onGround){
                        faceBlock = true
                    }else{
                        lockRotation = Rotation(mc.thePlayer.rotationYaw, if(grimLock.get()){
                            80f
                        } else{
                            mc.thePlayer.rotationPitch
                        }
                        )
                        faceBlock = false
                    }
                }
                "Rise" ->{
                    lockRotation = RotationUtils.getDirectionToBlock(blockData?.blockPos?.x!!.toDouble(), blockData.blockPos.y.toDouble(), blockData.blockPos.z.toDouble(), blockData.enumFacing)
                    faceBlock = true
                }
                "Rise2" ->{
                    var found = false
                    var possibleYaw = mc.thePlayer.rotationYaw - 180
                    while (possibleYaw <= mc.thePlayer.rotationYaw + 360 - 180 && !found) {
                        var possiblePitch = 90f
                        while (possiblePitch > 30 && !found) {
                            if (overBlock(
                                    Rotation(possibleYaw, possiblePitch),
                                    blockData!!.enumFacing,
                                    blockData.blockPos,
                                    true
                                )
                            ) {
                                lockRotation!!.yaw = possibleYaw
                                lockRotation!!.pitch = possiblePitch
                                found = true
                            }
                            possiblePitch -= (if (possiblePitch > (if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) 60 else 80)) 1 else 10).toFloat()
                        }
                        possibleYaw += 45f
                    }
                    if (!found) {
                        val rotations: Rotation = RotationUtils.calculate(
                            Vec3(blockData!!.blockPos.x.toDouble(), blockData.blockPos.y.toDouble(), blockData.blockPos.z.toDouble()),
                            blockData.enumFacing
                        )
                        lockRotation!!.pitch = rotations.pitch
                        lockRotation!!.yaw = rotations.yaw
                    }
                    faceBlock = true
                }
            }
            RotationUtils.setTargetRotation(lockRotation)

    }

    /**
     * Update event
     *
     * @param event
     */
    @EventTarget
    fun onUpdate(event: UpdateEvent?) {
        if (rotationsValue.get() && keepRotation.get()) {
            rotation()
        }

        if ((!rotationsValue.get() || noHitCheckValue.get() || faceBlock || !keepRotation.get()) && placeModeValue.get() === "Legit") {
            place()
        }
        if (towerActivation()) {
            shouldGoDown = false
            mc.gameSettings.keyBindSneak.pressed = false
        }
        if (mc.thePlayer.onGround) {
            offGroundTicks = 0
        } else offGroundTicks++
        if (sprintModeValue.get().equals("PlaceOff", ignoreCase = true)) {
            mc.thePlayer.isSprinting = true
        }
        if (sprintModeValue.get().equals("PlaceOn", ignoreCase = true)) {
            mc.thePlayer.isSprinting = false
        }
        mc.timer.timerSpeed = timerValue.get()
        shouldGoDown =
            downValue.get() && !sameYValue.get() && GameSettings.isKeyDown(mc.gameSettings.keyBindSneak) && blocksAmount > 1
        if (shouldGoDown) mc.gameSettings.keyBindSneak.pressed = false

        // scaffold custom speed if enabled
        if (customSpeedValue.get()) MovementUtils.strafe(customMoveSpeedValue.get())
        if (mc.thePlayer.onGround) {

            // Smooth Zitter
            if (zitterValue.get() && zitterModeValue.get().equals("smooth", ignoreCase = true)) {
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

            // Eagle
            if (eagleValue.get() && !shouldGoDown) {
                var dif = 0.5
                if (eagleEdgeDistanceValue.get() > 0) {
                    for (i in 0..3) {
                        val blockPos = BlockPos(
                            mc.thePlayer.posX + if (i == 0) -1 else if (i == 1) 1 else 0,
                            mc.thePlayer.posY - if (mc.thePlayer.posY == mc.thePlayer.posY.toInt() + 0.5) 0.0 else 1.0,
                            mc.thePlayer.posZ + if (i == 2) -1 else if (i == 3) 1 else 0
                        )
                        val placeInfo = get(blockPos)
                        if (isReplaceable(blockPos) && placeInfo != null) {
                            var calcDif = if (i > 1) mc.thePlayer.posZ - blockPos.z else mc.thePlayer.posX - blockPos.x
                            calcDif -= 0.5
                            if (calcDif < 0) calcDif *= -1.0
                            calcDif -= 0.5
                            if (calcDif < dif) dif = calcDif
                        }
                    }
                }
                if (placedBlocksWithoutEagle >= blocksToEagleValue.get()) {
                    val shouldEagle = mc.theWorld.getBlockState(
                        BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.0, mc.thePlayer.posZ)
                    ).block === Blocks.air || dif < eagleEdgeDistanceValue.get()
                    if (eagleSilentValue.get()) {
                        if (eagleSneaking != shouldEagle) {
                            mc.netHandler.addToSendQueue(
                                C0BPacketEntityAction(
                                    mc.thePlayer,
                                    if (shouldEagle) C0BPacketEntityAction.Action.START_SNEAKING else C0BPacketEntityAction.Action.STOP_SNEAKING
                                )
                            )
                        }
                        eagleSneaking = shouldEagle
                    } else mc.gameSettings.keyBindSneak.pressed = shouldEagle
                    placedBlocksWithoutEagle = 0
                } else placedBlocksWithoutEagle++
            }

            // Zitter
            if (zitterValue.get() && zitterModeValue.get().equals("teleport", ignoreCase = true)) {
                MovementUtils.strafe(zitterSpeed.get())
                val yaw = Math.toRadians(mc.thePlayer.rotationYaw + if (zitterDirection) 90.0 else -90.0)
                mc.thePlayer.motionX -= sin(yaw) * zitterStrength.get()
                mc.thePlayer.motionZ += cos(yaw) * zitterStrength.get()
                zitterDirection = !zitterDirection
            }
        }
        if (sprintModeValue.get().equals("off", ignoreCase = true) || sprintModeValue.get()
                .equals("ground", ignoreCase = true) && !mc.thePlayer.onGround || sprintModeValue.get()
                .equals("air", ignoreCase = true) && mc.thePlayer.onGround || sprintModeValue.get()
                .equals("falldownoff", ignoreCase = true) && mc.thePlayer.fallDistance > 0 ||
                sprintModeValue.get().equals("grimoff", ignoreCase = true) && offGroundTicks >= 3 ||
                sprintModeValue.get().equals("legit", ignoreCase = true) && abs(MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw) - MathHelper.wrapAngleTo180_float(RotationUtils.targetRotation.yaw)) > 90
        ) {
            mc.thePlayer.isSprinting = false
        }

        //Auto Jump thingy
        if (shouldGoDown) {
            launchY = mc.thePlayer.posY.toInt() - 1
        } else if (!sameYValue.get()) {
            if (!autoJumpValue.get() && !(smartSpeedValue.get() && LiquidBounce.moduleManager.getModule(Speed::class.java)!!.state) || GameSettings.isKeyDown(mc.gameSettings.keyBindJump) || mc.thePlayer.posY < launchY) launchY = mc.thePlayer.posY.toInt()
            if (autoJumpValue.get() && !LiquidBounce.moduleManager.getModule(Speed::class.java)!!.state && MovementUtils.isMoving() && mc.thePlayer.onGround) {
                mc.thePlayer.jump()
            }
        }
        if(motionY.get()){
            if(mc.thePlayer.onGround && MovementUtils.isMoving()){
                mc.thePlayer.motionY = motionYValue.get().toDouble()
            }
        }
        if(parkourValue.get()){
            if (MovementUtils.isMoving() && mc.thePlayer.onGround && !mc.thePlayer.isSneaking && !mc.gameSettings.keyBindSneak.isKeyDown && !mc.gameSettings.keyBindJump.isKeyDown &&
                mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, mc.thePlayer.entityBoundingBox
                    .offset(0.0, -0.5, 0.0).expand(-0.001, 0.0, -0.001)).isEmpty())
                mc.thePlayer.jump()
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (mc.thePlayer == null) return
        val packet = event.packet

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

    @EventTarget //took it from applyrotationstrafe XD. staticyaw comes from bestnub.
    fun onStrafe(event: StrafeEvent) {
        if (lockRotation != null && rotationStrafeValue.get()) {
            val dif =
                ((MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw - lockRotation!!.yaw - 23.5f - 135) + 180) / 45).toInt()
            val yaw = lockRotation!!.yaw
            val strafe = event.strafe
            val forward = event.forward
            val friction = event.friction
            var calcForward = 0f
            var calcStrafe = 0f
            when (dif) {
                0 -> {
                    calcForward = forward
                    calcStrafe = strafe
                }

                1 -> {
                    calcForward += forward
                    calcStrafe -= forward
                    calcForward += strafe
                    calcStrafe += strafe
                }

                2 -> {
                    calcForward = strafe
                    calcStrafe = -forward
                }

                3 -> {
                    calcForward -= forward
                    calcStrafe -= forward
                    calcForward += strafe
                    calcStrafe -= strafe
                }

                4 -> {
                    calcForward = -forward
                    calcStrafe = -strafe
                }

                5 -> {
                    calcForward -= forward
                    calcStrafe += forward
                    calcForward -= strafe
                    calcStrafe -= strafe
                }

                6 -> {
                    calcForward = -strafe
                    calcStrafe = forward
                }

                7 -> {
                    calcForward += forward
                    calcStrafe += forward
                    calcForward -= strafe
                    calcStrafe += strafe
                }
            }
            if (calcForward > 1f || calcForward < 0.9f && calcForward > 0.3f || calcForward < -1f || calcForward > -0.9f && calcForward < -0.3f) {
                calcForward *= 0.5f
            }
            if (calcStrafe > 1f || calcStrafe < 0.9f && calcStrafe > 0.3f || calcStrafe < -1f || calcStrafe > -0.9f && calcStrafe < -0.3f) {
                calcStrafe *= 0.5f
            }
            var f = calcStrafe * calcStrafe + calcForward * calcForward
            if (f >= 1.0E-4f) {
                f = MathHelper.sqrt_float(f)
                if (f < 1.0f) f = 1.0f
                f = friction / f
                calcStrafe *= f
                calcForward *= f
                val yawSin = MathHelper.sin((yaw * Math.PI / 180f).toFloat())
                val yawCos = MathHelper.cos((yaw * Math.PI / 180f).toFloat())
                mc.thePlayer.motionX += (calcStrafe * yawCos - calcForward * yawSin).toDouble()
                mc.thePlayer.motionZ += (calcForward * yawCos + calcStrafe * yawSin).toDouble()
            }
            event.cancelEvent()
        }
    }

    private fun shouldPlace(): Boolean {
        val placeWhenAir = placeConditionValue.get().equals("air", ignoreCase = true)
        val placeWhenFall = placeConditionValue.get().equals("falldown", ignoreCase = true)
        val placeWhenNegativeMotion = placeConditionValue.get().equals("negativemotion", ignoreCase = true)
        val alwaysPlace = placeConditionValue.get().equals("always", ignoreCase = true)
        return towerActivation() || alwaysPlace || placeWhenAir && !mc.thePlayer.onGround || placeWhenFall && mc.thePlayer.fallDistance > 0 || placeWhenNegativeMotion && mc.thePlayer.motionY < 0
    }

    @EventTarget
    fun onMotion(event: MotionEvent) {

        // XZReducer
        mc.thePlayer.motionX *= xzMultiplier.get().toDouble()
        mc.thePlayer.motionZ *= xzMultiplier.get().toDouble()
        if (speedPotSlow.get()) {
            if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                mc.thePlayer.motionX = mc.thePlayer.motionX * 0.85f
                mc.thePlayer.motionZ = mc.thePlayer.motionZ * 0.85f
            }
        }

        val mode = modeValue.get()
        val eventState = event.eventState

        // I think patches should be here instead
        for (i in 0..7) {
            if (mc.thePlayer.inventory.mainInventory[i] != null
                && mc.thePlayer.inventory.mainInventory[i].stackSize <= 0
            ) mc.thePlayer.inventory.mainInventory[i] = null
        }
        if ((!rotationsValue.get() || noHitCheckValue.get() || faceBlock || !keepRotation.get()) && placeModeValue.get()
                .equals(eventState.stateName, ignoreCase = true)
        ) {
            place()
        }
        if ((!rotationsValue.get() || noHitCheckValue.get() || faceBlock || !keepRotation.get()) && placeModeValue.get()
                .equals(eventState.stateName, ignoreCase = true) && towerActivation()
        ) {
            place()
        }
        if (eventState === EventState.PRE) {
            if (!shouldPlace() || (if (!autoBlockMode.get().equals("Off", ignoreCase = true))
                if(sortByHighestAmount.get()){
                    InventoryUtils.findLargestBlockStackInHotbar()!! == -1
                }else{
                    InventoryUtils.findBlockInHotbar()!! == -1
                }
                else
                    mc.thePlayer.heldItem == null ||
                        mc.thePlayer.heldItem.item !is ItemBlock)
            ) return
            findBlock(mode == "Expand" && expandLengthValue.get() > 1, area = true)
        }
        if (targetPlace == null) {
            if (placeableDelay.get()) delayTimer.reset()
        }
        if (!towerActivation()) {
            verusState = 0
            return
        }
        mc.timer.timerSpeed = towerTimerValue.get()
        if (placeModeValue.get().equals(eventState.stateName, ignoreCase = true)) place()

        if(event.eventState == EventState.POST){
            if (towerActivation()) {
                move(event)
            }
        }
    }

    /**
     * Search for new target block
     */
    private fun findBlock(expand: Boolean, area: Boolean) {
        val player = mc.thePlayer ?: return

        val sameY = sameYValue.get()
        val smartSpeed = smartSpeedValue.get() && LiquidBounce.moduleManager.getModule(Speed::class.java)!!.state
        val autojump = autoJumpValue.get() && !GameSettings.isKeyDown(mc.gameSettings.keyBindJump)
        val blockPos = BlockPos(
            mc.thePlayer.posX,
            if ((!towerActivation() || smartSpeed || sameY || autojump) && launchY <= mc.thePlayer.posY
            ) launchY - 1.0 else mc.thePlayer.posY - (if (mc.thePlayer.posY == mc.thePlayer.posY.toInt() + 0.5) 0.0 else 1.0) - if (shouldGoDown) 1.0 else 0.0,
            mc.thePlayer.posZ
        )

        if (!expand && (!isReplaceable(blockPos) || search(blockPos, !shouldGoDown, area))) {
            return
        }

        if (expand) {
            val yaw = player.rotationYaw.toRadiansD()
            val x = if (omniDirectionalExpand.get()) -sin(yaw).roundToInt() else player.horizontalFacing.directionVec.x
            val z = if (omniDirectionalExpand.get()) cos(yaw).roundToInt() else player.horizontalFacing.directionVec.z
            for (i in 0 until expandLengthValue.get()) {
                if (search(blockPos.add(x * i, 0, z * i), false, area)) {
                    return
                }
            }
        } else {
            for (x in -1..1) {
                for (z in -1..1) {
                    if (search(blockPos.add(x, 0, z), !shouldGoDown, area)) {
                        return
                    }
                }
            }
        }
    }

    /**
     * Place target block
     */
    private fun place() {
        if (targetPlace == null) {
            if (placeableDelay.get()) delayTimer.reset()
            return
        }

        if (!towerActivation() && (!delayTimer.hasTimePassed(delay) || smartDelay.get() && mc.rightClickDelayTimer > 0 || (sameYValue.get() || (autoJumpValue.get() || smartSpeedValue.get() && LiquidBounce.moduleManager.getModule(
                Speed::class.java
            )!!.state) && !GameSettings.isKeyDown(
                mc.gameSettings.keyBindJump
            )) && launchY - 1 != targetPlace!!.vec3.yCoord.toInt())
        ) return
        var blockSlot = -1
        var itemStack = mc.thePlayer.heldItem
        if (mc.thePlayer.heldItem == null || mc.thePlayer.heldItem.item !is ItemBlock) {
            if (autoBlockMode.get().equals("Off", ignoreCase = true)) return
            blockSlot = if(sortByHighestAmount.get()){
                InventoryUtils.findLargestBlockStackInHotbar()!!
            }else{
                InventoryUtils.findBlockInHotbar()!!
            }
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
        if (mc.playerController.onPlayerRightClick(
                mc.thePlayer,
                mc.theWorld,
                itemStack,
                targetPlace!!.blockPos,
                targetPlace!!.enumFacing,
                targetPlace!!.vec3
            )
        ) {
            if (!keepRotation.get()) {
                rotation()
            }
            delayTimer.reset()
            delay = if (!placeableDelay.get()) 0L else TimerUtils.randomDelay(minDelayValue.get(), maxDelayValue.get())
            if (mc.thePlayer.onGround) {
                val modifier = speedModifierValue.get()
                mc.thePlayer.motionX *= modifier.toDouble()
                mc.thePlayer.motionZ *= modifier.toDouble()
            }
            if (sprintModeValue.isMode("Off")) {
                mc.thePlayer.isSprinting = false
            }
            if (sprintModeValue.isMode("PlaceOn")) {
                mc.thePlayer.isSprinting = true
            }
            if (swingValue.get()) mc.thePlayer.swingItem() else mc.netHandler.addToSendQueue(C0APacketAnimation())
        }

        // Reset
        targetPlace = null
        if (!stayAutoBlock.get() && blockSlot >= 0 && !autoBlockMode.get()
                .equals("Switch", ignoreCase = true)
        ) mc.netHandler.addToSendQueue(
            C09PacketHeldItemChange(
                mc.thePlayer.inventory.currentItem
            )
        )
    }

    /**
     * Disable scaffold module
     */
    override fun onDisable() {
        if (mc.thePlayer == null) return
        if (!GameSettings.isKeyDown(mc.gameSettings.keyBindSneak)) {
            mc.gameSettings.keyBindSneak.pressed = false
            if (eagleSneaking) mc.netHandler.addToSendQueue(
                C0BPacketEntityAction(
                    mc.thePlayer,
                    C0BPacketEntityAction.Action.STOP_SNEAKING
                )
            )
        }
        if (!GameSettings.isKeyDown(mc.gameSettings.keyBindRight)) mc.gameSettings.keyBindRight.pressed = false
        if (!GameSettings.isKeyDown(mc.gameSettings.keyBindLeft)) mc.gameSettings.keyBindLeft.pressed = false
        lockRotation = null
        mc.timer.timerSpeed = 1f
        shouldGoDown = false
        faceBlock = false
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

    /**
     * Entity movement event
     *
     * @param event
     */
    @EventTarget
    fun onMove(event: MoveEvent) {
        if (!safeWalkValue.get() || shouldGoDown) return
        if (airSafeValue.get() || mc.thePlayer.onGround) event.isSafeWalk = true
    }

    @EventTarget
    fun onJump(event: JumpEvent) {
        if (towerActivation()) {
            event.cancelEvent()
        }
    }

    /**
     * Scaffold visuals
     *
     * @param event
     */
    @EventTarget
    fun onRender2D(event: Render2DEvent?) {
        progress = (System.currentTimeMillis() - lastMS).toFloat() / 100f
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
            val scaffold = LiquidBounce.moduleManager[Scaffold::class.java]
            val delta = RenderUtils.deltaTime.toFloat()
            if (scaffold!!.state) {
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
                    RenderUtils.reAlpha(getBlockColor(blocksAmount), alpha / 255)
                )
                mc.fontRendererObj.drawString(
                    info3,
                    scaledResolution.scaledWidth / 2 + 10,
                    scaledResolution.scaledHeight / 2,
                    getBlockColor(blocksAmount)
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
        if (modeDisplay.get()) {
            val speed = LiquidBounce.moduleManager.getModule(
                Speed::class.java
            )
            if (autoJumpValue.get() || smartSpeedValue.get() && speed!!.state) {
                Fonts.minecraftFont.drawString(
                    "KeepY",
                    (scaledResolution.scaledWidth / 2 - infoWidth2 / 2 - 1 + 16).toFloat(),
                    (scaledResolution.scaledHeight / 2 + 10).toFloat(),
                    -0x1000000,
                    false
                )
                Fonts.minecraftFont.drawString(
                    "KeepY",
                    (scaledResolution.scaledWidth / 2 - infoWidth2 / 2 + 1 + 16).toFloat(),
                    (scaledResolution.scaledHeight / 2 + 10).toFloat(),
                    -0x1000000,
                    false
                )
                Fonts.minecraftFont.drawString(
                    "KeepY",
                    (scaledResolution.scaledWidth / 2 - infoWidth2 / 2 + 16).toFloat(),
                    (scaledResolution.scaledHeight / 2 + 9).toFloat(),
                    -0x1000000,
                    false
                )
                Fonts.minecraftFont.drawString(
                    "KeepY",
                    (scaledResolution.scaledWidth / 2 - infoWidth2 / 2 + 16).toFloat(),
                    (scaledResolution.scaledHeight / 2 + 11).toFloat(),
                    -0x1000000,
                    false
                )
                Fonts.minecraftFont.drawString(
                    "KeepY",
                    (scaledResolution.scaledWidth / 2 - infoWidth2 / 2 + 16).toFloat(),
                    (scaledResolution.scaledHeight / 2 + 10).toFloat(),
                    Color.WHITE.rgb,
                    false
                )
            } else {
                if (towerActivation()) {
                    Fonts.minecraftFont.drawString(
                        "Tower",
                        (scaledResolution.scaledWidth / 2 - infoWidth2 / 2 - 1 + 16).toFloat(),
                        (scaledResolution.scaledHeight / 2 + 10).toFloat(),
                        -0x1000000,
                        false
                    )
                    Fonts.minecraftFont.drawString(
                        "Tower",
                        (scaledResolution.scaledWidth / 2 - infoWidth2 / 2 + 1 + 16).toFloat(),
                        (scaledResolution.scaledHeight / 2 + 10).toFloat(),
                        -0x1000000,
                        false
                    )
                    Fonts.minecraftFont.drawString(
                        "Tower",
                        (scaledResolution.scaledWidth / 2 - infoWidth2 / 2 + 16).toFloat(),
                        (scaledResolution.scaledHeight / 2 + 9).toFloat(),
                        -0x1000000,
                        false
                    )
                    Fonts.minecraftFont.drawString(
                        "Tower",
                        (scaledResolution.scaledWidth / 2 - infoWidth2 / 2 + 16).toFloat(),
                        (scaledResolution.scaledHeight / 2 + 11).toFloat(),
                        -0x1000000,
                        false
                    )
                    Fonts.minecraftFont.drawString(
                        "Tower",
                        (scaledResolution.scaledWidth / 2 - infoWidth2 / 2 + 16).toFloat(),
                        (scaledResolution.scaledHeight / 2 + 10).toFloat(),
                        Color.WHITE.rgb,
                        false
                    )
                } else {
                    if (placeModeValue.isMode("Pre")) {
                        Fonts.minecraftFont.drawString(
                            "Pre",
                            (scaledResolution.scaledWidth / 2 - infoWidth2 / 2 - 1 + 16).toFloat(),
                            (scaledResolution.scaledHeight / 2 + 10).toFloat(),
                            -0x1000000,
                            false
                        )
                        Fonts.minecraftFont.drawString(
                            "Pre",
                            (scaledResolution.scaledWidth / 2 - infoWidth2 / 2 + 1 + 16).toFloat(),
                            (scaledResolution.scaledHeight / 2 + 10).toFloat(),
                            -0x1000000,
                            false
                        )
                        Fonts.minecraftFont.drawString(
                            "Pre",
                            (scaledResolution.scaledWidth / 2 - infoWidth2 / 2 + 16).toFloat(),
                            (scaledResolution.scaledHeight / 2 + 9).toFloat(),
                            -0x1000000,
                            false
                        )
                        Fonts.minecraftFont.drawString(
                            "Pre",
                            (scaledResolution.scaledWidth / 2 - infoWidth2 / 2 + 16).toFloat(),
                            (scaledResolution.scaledHeight / 2 + 11).toFloat(),
                            -0x1000000,
                            false
                        )
                        Fonts.minecraftFont.drawString(
                            "Pre",
                            (scaledResolution.scaledWidth / 2 - infoWidth2 / 2 + 16).toFloat(),
                            (scaledResolution.scaledHeight / 2 + 10).toFloat(),
                            Color.WHITE.rgb,
                            false
                        )
                    } else if (placeModeValue.isMode("Post")) {
                        Fonts.minecraftFont.drawString(
                            "Post",
                            (scaledResolution.scaledWidth / 2 - infoWidth2 / 2 - 1 + 16).toFloat(),
                            (scaledResolution.scaledHeight / 2 + 10).toFloat(),
                            -0x1000000,
                            false
                        )
                        Fonts.minecraftFont.drawString(
                            "Post",
                            (scaledResolution.scaledWidth / 2 - infoWidth2 / 2 + 1 + 16).toFloat(),
                            (scaledResolution.scaledHeight / 2 + 10).toFloat(),
                            -0x1000000,
                            false
                        )
                        Fonts.minecraftFont.drawString(
                            "Post",
                            (scaledResolution.scaledWidth / 2 - infoWidth2 / 2 + 16).toFloat(),
                            (scaledResolution.scaledHeight / 2 + 9).toFloat(),
                            -0x1000000,
                            false
                        )
                        Fonts.minecraftFont.drawString(
                            "Post",
                            (scaledResolution.scaledWidth / 2 - infoWidth2 / 2 + 16).toFloat(),
                            (scaledResolution.scaledHeight / 2 + 11).toFloat(),
                            -0x1000000,
                            false
                        )
                        Fonts.minecraftFont.drawString(
                            "Post",
                            (scaledResolution.scaledWidth / 2 - infoWidth2 / 2 + 16).toFloat(),
                            (scaledResolution.scaledHeight / 2 + 10).toFloat(),
                            Color.WHITE.rgb,
                            false
                        )
                    }else{
                        Fonts.minecraftFont.drawString(
                            "Legit",
                            (scaledResolution.scaledWidth / 2 - infoWidth2 / 2 - 1 + 16).toFloat(),
                            (scaledResolution.scaledHeight / 2 + 10).toFloat(),
                            -0x1000000,
                            false
                        )
                        Fonts.minecraftFont.drawString(
                            "Legit",
                            (scaledResolution.scaledWidth / 2 - infoWidth2 / 2 + 1 + 16).toFloat(),
                            (scaledResolution.scaledHeight / 2 + 10).toFloat(),
                            -0x1000000,
                            false
                        )
                        Fonts.minecraftFont.drawString(
                            "Legit",
                            (scaledResolution.scaledWidth / 2 - infoWidth2 / 2 + 16).toFloat(),
                            (scaledResolution.scaledHeight / 2 + 9).toFloat(),
                            -0x1000000,
                            false
                        )
                        Fonts.minecraftFont.drawString(
                            "Legit",
                            (scaledResolution.scaledWidth / 2 - infoWidth2 / 2 + 16).toFloat(),
                            (scaledResolution.scaledHeight / 2 + 11).toFloat(),
                            -0x1000000,
                            false
                        )
                        Fonts.minecraftFont.drawString(
                            "Legit",
                            (scaledResolution.scaledWidth / 2 - infoWidth2 / 2 + 16).toFloat(),
                            (scaledResolution.scaledHeight / 2 + 10).toFloat(),
                            Color.WHITE.rgb,
                            false
                        )
                    }
                }
            }
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
        mc.renderItem.renderItemOverlays(mc.fontRendererObj, stack, x, y)
        RenderHelper.disableStandardItemLighting()
        GlStateManager.disableRescaleNormal()
        GlStateManager.disableBlend()
        GlStateManager.popMatrix()
    }

    /**
     * Scaffold visuals
     *
     * @param event
     */
    @EventTarget
    fun onRender3D(event: Render3DEvent?) {
        if (!markValue.get()) return
        val yaw = Math.toRadians(mc.thePlayer.rotationYaw.toDouble())
        val x = if (omniDirectionalExpand.get()) (-sin(yaw)).roundToInt()
             else mc.thePlayer.horizontalFacing.directionVec.x
        val z = if (omniDirectionalExpand.get()) cos(yaw).roundToInt()
             else mc.thePlayer.horizontalFacing.directionVec.z
        for (i in 0 until if (modeValue.get()
                .equals("Expand", ignoreCase = true) && !towerActivation()
        ) expandLengthValue.get() + 1 else 2) {
            val sameY = sameYValue.get()
            val smartSpeed = smartSpeedValue.get() && LiquidBounce.moduleManager.getModule(Speed::class.java)!!.state
            val autojump = autoJumpValue.get() && !GameSettings.isKeyDown(mc.gameSettings.keyBindJump)
            val blockPos = BlockPos(
                mc.thePlayer.posX,
                if ((!towerActivation() || smartSpeed || sameY || autojump) && launchY <= mc.thePlayer.posY
                ) launchY - 1.0 else mc.thePlayer.posY - (if (mc.thePlayer.posY == mc.thePlayer.posY.toInt() + 0.5) 0.0 else 1.0) - if (shouldGoDown) 1.0 else 0.0,
                mc.thePlayer.posZ
            )
            val placeInfo = get(blockPos)
            if (isReplaceable(blockPos) && placeInfo != null) {
                RenderUtils.drawBlockBox(
                    blockPos,
                    Color(redValue.get(), greenValue.get(), blueValue.get(), alphaValue.get()),
                    false
                )
                break
            }
        }
    }

    private fun search(blockPos: BlockPos, raycast: Boolean, area: Boolean): Boolean {
        val player = mc.thePlayer ?: return false

        if (!isReplaceable(blockPos)) {
            return false
        }

        val maxReach = mc.playerController.blockReachDistance

        val eyes = player.eyes
        var placeRotation: PlaceRotation? = null

        var currPlaceRotation: PlaceRotation?

        for (side in EnumFacing.values()) {
            val neighbor = blockPos.offset(side)

            if (!canBeClicked(neighbor)) {
                continue
            }

            if (!area) {
                currPlaceRotation =
                    findTargetPlace(blockPos, neighbor, Vec3(0.5, 0.5, 0.5), side, eyes, maxReach, raycast)
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
                                findTargetPlace(blockPos, neighbor, Vec3(x, y, z), side, eyes, maxReach, raycast)

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

        if (rotationsValue.get() && (rotationModeValue.isMode("Normal") || rotationModeValue.isMode("GrimTest") && offGroundTicks >= 3 || rotationModeValue.isMode("GrimTest2") && !mc.thePlayer.onGround)) {
            lockRotation = RotationUtils.limitAngleChange(
                currRotation, placeRotation.rotation, RandomUtils.nextFloat(minTurnSpeed.get(), maxTurnSpeed.get())
            )
        }
            if(rotationsValue.get() && rotationModeValue.isMode("Intave")){
                lockRotation = Rotation(mc.thePlayer.rotationYaw + 180, placeRotation.rotation.pitch)
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

            if (dist < 0) {
                return null
            }
        }

        var rotation = RotationUtils.toRotation(vec, false)

        rotation = if (stabilizedRotation.get()) {
            Rotation(round(rotation.yaw / 45f) * 45f, rotation.pitch)
        } else {
            rotation
        }

        if (rotationModeValue.isMode("Normal") || (rotationModeValue.isMode("GrimTest") && offGroundTicks >= 3) || (rotationModeValue.isMode("GrimTest2") && !mc.thePlayer.onGround)) {
            lockRotation = rotation
        }

            // If the current rotation already looks at the target block and side, then return right here
            performBlockRaytrace(currRotation, maxReach)?.let { raytrace ->
                if (raytrace.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && raytrace.blockPos == offsetPos && (!raycast || raytrace.sideHit == side.opposite)) {
                    return PlaceRotation(
                        PlaceInfo(
                            raytrace.blockPos,
                            side.opposite,
                            modifyVec(raytrace.hitVec, side, Vec3(offsetPos), !raycast)
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

    private fun overBlock(rotation: Rotation, enumFacing: EnumFacing, pos: BlockPos, strict: Boolean): Boolean {
        val movingObjectPosition = mc.thePlayer.rayTraceCustom(4.5, rotation.pitch, rotation.yaw) ?: return false
        return movingObjectPosition.blockPos == pos && (!strict || movingObjectPosition.sideHit == enumFacing)
    }

    private val blocksAmount: Int
        /**
         * @return hotbar blocks amount
         */
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
    override val tag: String
        get() = placeModeValue.get()
}
