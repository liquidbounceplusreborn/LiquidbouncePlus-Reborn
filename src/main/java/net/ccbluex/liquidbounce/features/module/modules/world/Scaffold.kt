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
import net.ccbluex.liquidbounce.injection.access.StaticStorage
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.NotifyType
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.*
import net.ccbluex.liquidbounce.utils.block.BlockUtils.canBeClicked
import net.ccbluex.liquidbounce.utils.block.BlockUtils.getBlock
import net.ccbluex.liquidbounce.utils.block.BlockUtils.isReplaceable
import net.ccbluex.liquidbounce.utils.block.PlaceInfo
import net.ccbluex.liquidbounce.utils.block.PlaceInfo.Companion.get
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
import net.minecraft.block.BlockAir
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.settings.GameSettings
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
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

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
            "Hypixel"
        ), "Motion"
    ) { towerEnabled.get() }
    private val noMoveOnlyValue = BoolValue("NoMove", true) { towerEnabled.get() }
    private val stopWhenBlockAbove = BoolValue("StopWhenBlockAbove", false) { towerEnabled.get() }
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
    val modeValue = ListValue("Mode", arrayOf("Normal", "Rewinside", "Expand"), "Normal")

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

    //make sprint compatible with tower.add sprint tricks
    val sprintModeValue = ListValue("SprintMode", arrayOf("Same", "Ground", "Air", "PlaceOff", "PlaceOn","FallDownOff", "Off"), "Off")

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
    private val rotationModeValue = ListValue(
        "RotationMode",
        arrayOf("Normal", "AAC", "Static", "Static2", "Static3", "Spin", "Custom"),
        "Normal"
    ) // searching reason
    private val rotationLookupValue = ListValue("RotationLookup", arrayOf("Normal", "AAC", "Same"), "Normal")
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
    private val staticPitchValue = FloatValue("Static-Pitch", 86f, 80f, 90f, "°") {
        rotationModeValue.get().lowercase(Locale.getDefault()).startsWith("static")
    }
    private val customYawValue = FloatValue("Custom-Yaw", 135f, -180f, 180f, "°") {
        rotationModeValue.get().equals("custom", ignoreCase = true)
    }
    private val customPitchValue = FloatValue("Custom-Pitch", 86f, -90f, 90f, "°") {
        rotationModeValue.get().equals("custom", ignoreCase = true)
    }
    private val speenSpeedValue = FloatValue("Spin-Speed", 5f, -90f, 90f, "°") {
        rotationModeValue.get().equals("spin", ignoreCase = true)
    }
    private val speenPitchValue = FloatValue("Spin-Pitch", 90f, -90f, 90f, "°") {
        rotationModeValue.get().equals("spin", ignoreCase = true)
    }
    private val keepRotOnJumpValue = BoolValue("KeepRotOnJump", true) {
        !rotationModeValue.get().equals("normal", ignoreCase = true) && !rotationModeValue.get()
            .equals("aac", ignoreCase = true)
    }
    private val keepRotationValue = BoolValue("KeepRotation", false) { rotationsValue.get() }
    private val keepLengthValue =
        IntegerValue("KeepRotationLength", 0, 0, 20) { rotationsValue.get() && !keepRotationValue.get() }
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
    private val autoJumpValue = BoolValue("AutoJump", false) { !isTowerOnly }
    private val smartSpeedValue = BoolValue("SmartSpeed", false) { !isTowerOnly }
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
    private var towerPlace: PlaceInfo? = null

    // Launch position
    private var launchY = 0
    private var faceBlock = false

    // Rotation lock
    private var lockRotation: Rotation? = null
    private var lookupRotation: Rotation? = null
    private var speenRotation: Rotation? = null

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
     * Update event
     *
     * @param event
     */
    @EventTarget
    fun onUpdate(event: UpdateEvent?) {

        if ((!rotationsValue.get() || noHitCheckValue.get() || faceBlock) && placeModeValue.get() === "Legit") {
            place(false)
        }
        if (towerActivation()) {
            shouldGoDown = false
            mc.gameSettings.keyBindSneak.pressed = false
            mc.thePlayer.isSprinting = false
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
            val mode = modeValue.get()

            // Rewinside scaffold mode
            if (mode.equals("Rewinside", ignoreCase = true)) {
                MovementUtils.strafe(0.2f)
                mc.thePlayer.motionY = 0.0
            }

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
                .equals("air", ignoreCase = true) && mc.thePlayer.onGround || sprintModeValue.get().equals("off", ignoreCase = true) && mc.thePlayer.fallDistance > 0
        ) {
            mc.thePlayer.isSprinting = false
        }

        //Auto Jump thingy
        if (shouldGoDown) {
            launchY = mc.thePlayer.posY.toInt() - 1
        } else if (!sameYValue.get()) {
            if (!autoJumpValue.get() && !(smartSpeedValue.get() && LiquidBounce.moduleManager.getModule(
                    Speed::class.java
                )!!.state) || GameSettings.isKeyDown(
                    mc.gameSettings.keyBindJump
                ) || mc.thePlayer.posY < launchY
            ) launchY = mc.thePlayer.posY.toInt()
            if (autoJumpValue.get() && !LiquidBounce.moduleManager.getModule(
                    Speed::class.java
                )!!.state && MovementUtils.isMoving() && mc.thePlayer.onGround && mc.thePlayer.jumpTicks == 0
            ) {
                mc.thePlayer.jump()
                mc.thePlayer.jumpTicks = 10
            }
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (mc.thePlayer == null) return
        val packet = event.packet

        // AutoBlock
        if (packet is C09PacketHeldItemChange) {
            slot = packet.slotId
        }
    }

    @EventTarget //took it from applyrotationstrafe XD. staticyaw comes from bestnub.
    fun onStrafe(event: StrafeEvent) {
        if (lookupRotation != null && rotationStrafeValue.get()) {
            val dif =
                ((MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw - lookupRotation!!.yaw - 23.5f - 135) + 180) / 45).toInt()
            val yaw = lookupRotation!!.yaw
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

        // Lock Rotation
        if (rotationsValue.get() && keepRotationValue.get() && lockRotation != null) {
            if (rotationModeValue.get().equals("spin", ignoreCase = true)) {
                spinYaw += speenSpeedValue.get()
                spinYaw = MathHelper.wrapAngleTo180_float(spinYaw)
                speenRotation = Rotation(spinYaw, speenPitchValue.get())
                RotationUtils.setTargetRotation(speenRotation)
            } else if (lockRotation != null) RotationUtils.setTargetRotation(
                RotationUtils.limitAngleChange(
                    RotationUtils.serverRotation,
                    lockRotation,
                    RandomUtils.nextFloat(minTurnSpeed.get(), maxTurnSpeed.get())
                )
            )
        }
        val mode = modeValue.get()
        val eventState = event.eventState

        // I think patches should be here instead
        for (i in 0..7) {
            if (mc.thePlayer.inventory.mainInventory[i] != null
                && mc.thePlayer.inventory.mainInventory[i].stackSize <= 0
            ) mc.thePlayer.inventory.mainInventory[i] = null
        }
        if ((!rotationsValue.get() || noHitCheckValue.get() || faceBlock) && placeModeValue.get()
                .equals(eventState.stateName, ignoreCase = true)
        ) {
            place(false)
        }
        if ((!rotationsValue.get() || noHitCheckValue.get() || faceBlock) && placeModeValue.get()
                .equals(eventState.stateName, ignoreCase = true) && towerActivation()
        ) {
            place(false)
        }
        if (eventState === EventState.PRE) {
            if (!shouldPlace() || (if (!autoBlockMode.get()
                        .equals("Off", ignoreCase = true)
                ) InventoryUtils.findAutoBlockBlock() == -1 else mc.thePlayer.heldItem == null ||
                        mc.thePlayer.heldItem.item !is ItemBlock)
            ) return
            findBlock(mode.equals("expand", ignoreCase = true) && !towerActivation())
        }
        if (targetPlace == null) {
            if (placeableDelay.get()) delayTimer.reset()
        }
        if (!towerActivation()) {
            verusState = 0
            towerPlace = null
            return
        }
        mc.timer.timerSpeed = towerTimerValue.get()
        if (placeModeValue.get().equals(eventState.stateName, ignoreCase = true)) place(true)
        if (eventState === EventState.POST) {
            towerPlace = null
            timer.update()
            val isHeldItemBlock = mc.thePlayer.heldItem != null && mc.thePlayer.heldItem.item is ItemBlock
            if (InventoryUtils.findAutoBlockBlock() != -1 || isHeldItemBlock) {
                launchY = mc.thePlayer.posY.toInt()
                if (towerModeValue.get().equals("verus", ignoreCase = true) || !stopWhenBlockAbove.get() || getBlock(
                        BlockPos(
                            mc.thePlayer.posX,
                            mc.thePlayer.posY + 2, mc.thePlayer.posZ
                        )
                    ) is BlockAir
                ) {
                    move(event)
                }
                val blockPos = BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.0, mc.thePlayer.posZ)
                if (mc.theWorld.getBlockState(blockPos).block is BlockAir) {
                    if (search(blockPos, checks = true, towerActive = true) && rotationsValue.get()) {
                        val vecRotation = RotationUtils.faceBlock(blockPos)
                        if (vecRotation != null) {
                            RotationUtils.setTargetRotation(
                                RotationUtils.limitAngleChange(
                                    RotationUtils.serverRotation,
                                    vecRotation.rotation,
                                    RandomUtils.nextFloat(minTurnSpeed.get(), maxTurnSpeed.get())
                                )
                            )
                            towerPlace!!.vec3 = vecRotation.vec
                        }
                    }
                }
            }
        }
    }

    /**
     * Search for new target block
     */
    private fun findBlock(expand: Boolean) {
        val blockPosition = if (shouldGoDown) (if (mc.thePlayer.posY == mc.thePlayer.posY.toInt() + 0.5) BlockPos(
            mc.thePlayer.posX,
            mc.thePlayer.posY - 0.6,
            mc.thePlayer.posZ
        ) else BlockPos(
            mc.thePlayer.posX, mc.thePlayer.posY - 0.6, mc.thePlayer.posZ
        ).down()) else if (!towerActivation() && (sameYValue.get() || (autoJumpValue.get() || smartSpeedValue.get() && LiquidBounce.moduleManager.getModule(
                Speed::class.java
            )!!.state) && !GameSettings.isKeyDown(
                mc.gameSettings.keyBindJump
            )) && launchY <= mc.thePlayer.posY
        ) BlockPos(
            mc.thePlayer.posX,
            (launchY - 1).toDouble(),
            mc.thePlayer.posZ
        ) else if (mc.thePlayer.posY == mc.thePlayer.posY.toInt() + 0.5) BlockPos(
            mc.thePlayer
        ) else BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ).down()
        if (!expand && (!isReplaceable(blockPosition) || search(blockPosition, !shouldGoDown, false))) return
        if (expand) {
            val yaw = Math.toRadians(mc.thePlayer.rotationYaw.toDouble())
            val x = if (omniDirectionalExpand.get()) (-sin(yaw)).roundToInt()
                 else mc.thePlayer.horizontalFacing.directionVec.x
            val z = if (omniDirectionalExpand.get()) cos(yaw).roundToInt()
                 else mc.thePlayer.horizontalFacing.directionVec.z
            for (i in 0 until expandLengthValue.get()) {
                if (search(blockPosition.add(x * i, 0, z * i), checks = false, towerActive = false)) return
            }
        } else {
            for (x in -1..1) for (z in -1..1) if (search(blockPosition.add(x, 0, z), !shouldGoDown, false)) return
        }
    }

    /**
     * Place target block
     */
    private fun place(towerActive: Boolean) {
        if ((if (towerActive) towerPlace else targetPlace) == null) {
            if (placeableDelay.get()) delayTimer.reset()
            return
        }
        if (sprintModeValue.get().equals("PlaceOff", ignoreCase = true)) {
            mc.thePlayer.isSprinting = false
            mc.thePlayer.motionX *= 1.0
            mc.thePlayer.motionZ *= 1.0
        }
        if (sprintModeValue.get().equals("PlaceOff", ignoreCase = true)) {
            mc.thePlayer.isSprinting = true
        }
        if (!towerActivation() && (!delayTimer.hasTimePassed(delay) || smartDelay.get() && mc.rightClickDelayTimer > 0 || (sameYValue.get() || (autoJumpValue.get() || smartSpeedValue.get() && LiquidBounce.moduleManager.getModule(
                Speed::class.java
            )!!.state) && !GameSettings.isKeyDown(
                mc.gameSettings.keyBindJump
            )) && launchY - 1 != (if (towerActive) towerPlace else targetPlace)!!.vec3.yCoord.toInt())
        ) return
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
        if (mc.playerController.onPlayerRightClick(
                mc.thePlayer,
                mc.theWorld,
                itemStack,
                (if (towerActive) towerPlace else targetPlace)!!.blockPos,
                (if (towerActive) towerPlace else targetPlace)!!.enumFacing,
                (if (towerActive) towerPlace else targetPlace)!!.vec3
            )
        ) {
            delayTimer.reset()
            delay = if (!placeableDelay.get()) 0L else TimerUtils.randomDelay(minDelayValue.get(), maxDelayValue.get())
            if (mc.thePlayer.onGround) {
                val modifier = speedModifierValue.get()
                mc.thePlayer.motionX *= modifier.toDouble()
                mc.thePlayer.motionZ *= modifier.toDouble()
            }
            if (sprintModeValue.get().equals("Hypixel", ignoreCase = true) && mc.thePlayer.onGround) {
                mc.thePlayer.motionX *= 0.3
                mc.thePlayer.motionZ *= 0.3
            }
            if (swingValue.get()) mc.thePlayer.swingItem() else mc.netHandler.addToSendQueue(C0APacketAnimation())
        }

        // Reset
        if (towerActive) towerPlace = null else targetPlace = null
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
        lookupRotation = null
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
        if (towerActivation()) event.cancelEvent()
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
                    } else {
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
            val blockPos = BlockPos(
                mc.thePlayer.posX + x * i,
                if (!towerActivation()
                    && (sameYValue.get() ||
                            ((autoJumpValue.get() || smartSpeedValue.get() && LiquidBounce.moduleManager.getModule(
                                Speed::class.java
                            )!!.state)
                                    && !GameSettings.isKeyDown(mc.gameSettings.keyBindJump))) && launchY <= mc.thePlayer.posY
                ) launchY - 1.0 else mc.thePlayer.posY - (if (mc.thePlayer.posY == mc.thePlayer.posY.toInt() + 0.5) 0.0 else 1.0) - if (shouldGoDown) 1.0 else 0.0,
                mc.thePlayer.posZ + z * i
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

    /**
     * Search for placeable block
     *
     * @param blockPosition pos
     * @param checks        visible
     * @return
     */
    private fun search(blockPosition: BlockPos, checks: Boolean, towerActive: Boolean = false): Boolean {
        faceBlock = false
        if (!isReplaceable(blockPosition)) return false
        val staticYawMode = rotationLookupValue.get().equals("AAC", ignoreCase = true) || rotationLookupValue.get()
            .equals("same", ignoreCase = true) && (rotationModeValue.get()
            .equals("AAC", ignoreCase = true) || rotationModeValue.get().contains("Static") && !rotationModeValue.get()
            .equals("static3", ignoreCase = true))
        val eyesPos = Vec3(
            mc.thePlayer.posX,
            mc.thePlayer.entityBoundingBox.minY + mc.thePlayer.getEyeHeight(),
            mc.thePlayer.posZ
        )
        var placeRotation: PlaceRotation? = null
        for (side in StaticStorage.facings()) {
            val neighbor = blockPosition.offset(side)
            if (!canBeClicked(neighbor)) continue
            val dirVec = Vec3(side.directionVec)
            var xSearch = 0.1
            while (xSearch < 0.9) {
                var ySearch = 0.1
                while (ySearch < 0.9) {
                    var zSearch = 0.1
                    while (zSearch < 0.9) {
                        val posVec = Vec3(blockPosition).addVector(xSearch, ySearch, zSearch)
                        val distanceSqPosVec = eyesPos.squareDistanceTo(posVec)
                        val hitVec = posVec.add(Vec3(dirVec.xCoord * 0.5, dirVec.yCoord * 0.5, dirVec.zCoord * 0.5))
                        if (checks && (eyesPos.squareDistanceTo(hitVec) > 18.0 || distanceSqPosVec > eyesPos.squareDistanceTo(
                                posVec.add(dirVec)
                            ) || mc.theWorld.rayTraceBlocks(eyesPos, hitVec, false, true, false) != null)
                        ) {
                            zSearch += 0.1
                            continue
                        }

                        // face block
                        for (i in 0 until if (staticYawMode) 2 else 1) {
                            val diffX: Double = if (staticYawMode && i == 0) 0.0 else hitVec.xCoord - eyesPos.xCoord
                            val diffY = hitVec.yCoord - eyesPos.yCoord
                            val diffZ: Double = if (staticYawMode && i == 1) 0.0 else hitVec.zCoord - eyesPos.zCoord
                            val diffXZ = MathHelper.sqrt_double(diffX * diffX + diffZ * diffZ).toDouble()
                            var rotation = Rotation(
                                MathHelper.wrapAngleTo180_float(
                                    Math.toDegrees(atan2(diffZ, diffX)).toFloat() - 90f
                                ),
                                MathHelper.wrapAngleTo180_float(-Math.toDegrees(atan2(diffY, diffXZ)).toFloat())
                            )
                            lookupRotation = rotation
                            if (rotationModeValue.get().equals(
                                    "static",
                                    ignoreCase = true
                                ) && (keepRotOnJumpValue.get() || !mc.gameSettings.keyBindJump.isKeyDown)
                            ) rotation = Rotation(
                                MovementUtils.getScaffoldRotation(
                                    mc.thePlayer.rotationYaw, mc.thePlayer.moveStrafing
                                ), staticPitchValue.get()
                            )
                            if ((rotationModeValue.get().equals("static2", ignoreCase = true) || rotationModeValue.get()
                                    .equals(
                                        "static3",
                                        ignoreCase = true
                                    )) && (keepRotOnJumpValue.get() || !mc.gameSettings.keyBindJump.isKeyDown)
                            ) rotation = Rotation(rotation.yaw, staticPitchValue.get())
                            if (rotationModeValue.get().equals(
                                    "custom",
                                    ignoreCase = true
                                ) && (keepRotOnJumpValue.get() || !mc.gameSettings.keyBindJump.isKeyDown)
                            ) rotation = Rotation(
                                mc.thePlayer.rotationYaw + customYawValue.get(), customPitchValue.get()
                            )
                            if (rotationModeValue.get().equals(
                                    "spin",
                                    ignoreCase = true
                                ) && speenRotation != null && (keepRotOnJumpValue.get() || !mc.gameSettings.keyBindJump.isKeyDown)
                            ) rotation = speenRotation as Rotation
                            val rotationVector = RotationUtils.getVectorForRotation(
                                if (rotationLookupValue.get()
                                        .equals("same", ignoreCase = true)
                                ) rotation else lookupRotation
                            )
                            val vector = eyesPos.addVector(
                                rotationVector.xCoord * 4,
                                rotationVector.yCoord * 4,
                                rotationVector.zCoord * 4
                            )
                            val obj = mc.theWorld.rayTraceBlocks(eyesPos, vector, false, false, true)
                            if (!(obj.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && obj.blockPos == neighbor)) continue
                            if (placeRotation == null || RotationUtils.getRotationDifference(rotation) < RotationUtils.getRotationDifference(
                                    placeRotation.rotation
                                )
                            ) placeRotation = PlaceRotation(PlaceInfo(neighbor, side.opposite, hitVec), rotation)
                        }
                        zSearch += 0.1
                    }
                    ySearch += 0.1
                }
                xSearch += 0.1
            }
        }
        if (placeRotation == null) return false
        if (rotationsValue.get()) {
            if (minTurnSpeed.get() < 180) {
                val limitedRotation = RotationUtils.limitAngleChange(
                    RotationUtils.serverRotation,
                    placeRotation.rotation,
                    RandomUtils.nextFloat(minTurnSpeed.get(), maxTurnSpeed.get())
                )
                if ((10 * MathHelper.wrapAngleTo180_float(limitedRotation.yaw)).toInt() == (10 * MathHelper.wrapAngleTo180_float(
                        placeRotation.rotation.yaw
                    )).toInt()
                    && (10 * MathHelper.wrapAngleTo180_float(limitedRotation.pitch)).toInt() == (10 * MathHelper.wrapAngleTo180_float(
                        placeRotation.rotation.pitch
                    )).toInt()
                ) {
                    RotationUtils.setTargetRotation(placeRotation.rotation, keepLengthValue.get())
                    lockRotation = placeRotation.rotation
                    faceBlock = true
                } else {
                    RotationUtils.setTargetRotation(limitedRotation, keepLengthValue.get())
                    lockRotation = limitedRotation
                    faceBlock = false
                }
            } else {
                RotationUtils.setTargetRotation(placeRotation.rotation, keepLengthValue.get())
                lockRotation = placeRotation.rotation
                faceBlock = true
            }
            if (rotationLookupValue.get().equals("same", ignoreCase = true)) lookupRotation = lockRotation
        }
        if (towerActive) towerPlace = placeRotation.placeInfo else targetPlace = placeRotation.placeInfo
        return true
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
        get() = if (towerActivation()) "" + rotationModeValue.get() else rotationModeValue.get()
}
