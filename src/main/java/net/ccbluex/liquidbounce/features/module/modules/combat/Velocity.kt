/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.client.settings.GameSettings
import net.minecraft.network.play.client.C02PacketUseEntity
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C0APacketAnimation
import net.minecraft.network.play.client.C0FPacketConfirmTransaction
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minecraft.network.play.server.S27PacketExplosion
import net.minecraft.network.play.server.S32PacketConfirmTransaction
import net.minecraft.util.BlockPos
import net.minecraft.util.MathHelper
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@ModuleInfo(
    name = "Velocity",
    description = "Allows you to modify the amount of knockback you take.",
    category = ModuleCategory.COMBAT
)
class Velocity : Module() {

    /**
     * OPTIONS
     */
    private val horizontalValue = FloatValue("Horizontal", 0F, -1F, 1F, "x")
    private val verticalValue = FloatValue("Vertical", 0F, -1F, 1F, "x")
    private val horizontalExplosionValue = FloatValue("HorizontalExplosion", 0F, 0F, 1F, "x")
    private val verticalExplosionValue = FloatValue("VerticalExplosion", 0F, 0F, 1F, "x")
    private val modeValue = ListValue(
        "Mode", arrayOf(
            "Cancel",
            "Simple",
            "Hypixel",
            "AACv4",
            "AAC4Reduce",
            "AAC5Reduce",
            "AAC5.2.0",
            "AAC",
            "AACPush",
            "AACZero",
            "Reverse",
            "SmoothReverse",
            "Jump",
            "Glitch",
            "Phase",
            "Matrix",
            "Legit",
            "AEMine",
            "GrimAC",
            "GrimCombat",
            "AllAC",
            "Intave",
            "JumpReset",
            "Smart"
        ), "Cancel"
    ) // later

    private val aac5KillAuraValue = BoolValue("AAC5.2.0-Attack-Only", true) { modeValue.get().equals("aac5.2.0", true) }

    // Affect chance
    private val reduceChance = FloatValue("Reduce-Chance", 100F, 0F, 100F, "%")
    private var shouldAffect: Boolean = true

    // Reverse
    private val reverseStrengthValue =
        FloatValue("ReverseStrength", 1F, 0.1F, 1F, "x") { modeValue.get().equals("reverse", true) }
    private val reverse2StrengthValue =
        FloatValue("SmoothReverseStrength", 0.05F, 0.02F, 0.1F, "x") { modeValue.get().equals("smoothreverse", true) }

    // AAC Push
    private val aacPushXZReducerValue =
        FloatValue("AACPushXZReducer", 2F, 1F, 3F, "x") { modeValue.get().equals("aacpush", true) }
    private val aacPushYReducerValue = BoolValue("AACPushYReducer", true) { modeValue.get().equals("aacpush", true) }

    // legit
    private val legitStrafeValue = BoolValue("LegitStrafe", false) { modeValue.get().equals("legit", true) }
    private val legitFaceValue = BoolValue("LegitFace", true) { modeValue.get().equals("legit", true) }

    //add strafe in aac
    private val aacStrafeValue = BoolValue("AACStrafeValue", false) { modeValue.get().equals("aac", true) }

    //epic
    private val phaseOffsetValue =
        FloatValue("Phase-Offset", 0.05F, -10F, 10F, "m") { modeValue.get().equals("phase", true) }

    // Jump Reset
    private val jumpResetMode =
        ListValue(
            "JumpReset-Mode",
            arrayOf("Normal", "Reset", "Reduce", "Combo"),
            "Normal"
        ) {
            modeValue.get().equals("jumpreset", true)
        }

    private val tagModeValue = ListValue("TagMode", arrayOf("Off", "Mode", "Percentage", "Both"), "Both")

    /**
     * VALUES
     */
    private var velocityTimer = MSTimer()
    private var velocityInput = false

    // Legit
    private var pos: BlockPos? = null

    // SmoothReverse
    private var reverseHurt = false

    // AACPush
    private var jump = false

    // Grim
    private val transactionQueue: Queue<Short> = ConcurrentLinkedQueue()
    private var grimPacket = false
    private var attack = false;

    private var jumped = 0

    override val tag: String?
        get() = when (tagModeValue.get()) {
            "Mode" -> modeValue.get()
            "Percentage" -> "${(horizontalValue.get() * 100).roundToInt()}% ${(verticalValue.get() * 100).roundToInt()}%"
            "Both" -> "${modeValue.get()} ${(horizontalValue.get() * 100).roundToInt()}% ${(verticalValue.get() * 100).roundToInt()}%"
            else -> null
        }

    override fun onDisable() {
        mc.thePlayer?.speedInAir = 0.02F
        grimPacket = false
        transactionQueue.clear()
        attack = false
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {

        if (mc.thePlayer.hurtTime <= 0) shouldAffect = (Math.random().toFloat() < reduceChance.get() / 100F)
        if (mc.thePlayer.isInWater || mc.thePlayer.isInLava || mc.thePlayer.isInWeb || !shouldAffect)
            return

        when (modeValue.get().lowercase(Locale.getDefault())) {
            "jump" -> if (mc.thePlayer.hurtTime > 0 && mc.thePlayer.onGround) {
                mc.thePlayer.motionY = 0.42

                val yaw = mc.thePlayer.rotationYaw * 0.017453292F
                mc.thePlayer.motionX -= MathHelper.sin(yaw) * 0.2
                mc.thePlayer.motionZ += MathHelper.cos(yaw) * 0.2
            }

            "glitch" -> {
                mc.thePlayer.noClip = velocityInput
                if (mc.thePlayer.hurtTime == 7)
                    mc.thePlayer.motionY = 0.4

                velocityInput = false
            }

            "reverse" -> {
                if (!velocityInput)
                    return

                if (!mc.thePlayer.onGround) {
                    MovementUtils.strafe(MovementUtils.getSpeed() * reverseStrengthValue.get())
                } else if (velocityTimer.hasTimePassed(80L))
                    velocityInput = false
            }

            "aacv4" -> {
                if (!mc.thePlayer.onGround) {
                    if (velocityInput) {
                        mc.thePlayer.speedInAir = 0.02f
                        mc.thePlayer.motionX *= 0.6
                        mc.thePlayer.motionZ *= 0.6
                    }
                } else if (velocityTimer.hasTimePassed(80L)) {
                    velocityInput = false
                    mc.thePlayer.speedInAir = 0.02f
                }
            }

            "aac4reduce" -> {
                if (mc.thePlayer.hurtTime > 0 && !mc.thePlayer.onGround && velocityInput && velocityTimer.hasTimePassed(
                        80L
                    )
                ) {
                    mc.thePlayer.motionX *= 0.62
                    mc.thePlayer.motionZ *= 0.62
                }
                if (velocityInput && (mc.thePlayer.hurtTime < 4 || mc.thePlayer.onGround) && velocityTimer.hasTimePassed(
                        120L
                    )
                ) {
                    velocityInput = false
                }
            }

            "aac5reduce" -> {
                if (mc.thePlayer.hurtTime > 1 && velocityInput) {
                    mc.thePlayer.motionX *= 0.81
                    mc.thePlayer.motionZ *= 0.81
                }
                if (velocityInput && (mc.thePlayer.hurtTime < 5 || mc.thePlayer.onGround) && velocityTimer.hasTimePassed(
                        120L
                    )
                ) {
                    velocityInput = false
                }
            }

            "smoothreverse" -> {
                if (!velocityInput) {
                    mc.thePlayer.speedInAir = 0.02F
                    return
                }

                if (mc.thePlayer.hurtTime > 0)
                    reverseHurt = true

                if (!mc.thePlayer.onGround) {
                    if (reverseHurt)
                        mc.thePlayer.speedInAir = reverse2StrengthValue.get()
                } else if (velocityTimer.hasTimePassed(80)) {
                    velocityInput = false
                    reverseHurt = false
                }
            }

            "aac" -> if (velocityInput && velocityTimer.hasTimePassed(50)) {
                mc.thePlayer.motionX *= horizontalValue.get()
                mc.thePlayer.motionZ *= horizontalValue.get()
                mc.thePlayer.motionY *= verticalValue.get()
                if (aacStrafeValue.get()) {
                    MovementUtils.strafe()
                }
                velocityInput = false
            }

            "aacpush" -> {
                if (jump) {
                    if (mc.thePlayer.onGround)
                        jump = false
                } else {
                    // Strafe
                    if (mc.thePlayer.hurtTime > 0 && mc.thePlayer.motionX != 0.0 && mc.thePlayer.motionZ != 0.0)
                        mc.thePlayer.onGround = true

                    // Reduce Y
                    if (mc.thePlayer.hurtResistantTime > 0 && aacPushYReducerValue.get() && !LiquidBounce.moduleManager[Speed::class.java]!!.state)
                        mc.thePlayer.motionY -= 0.014999993
                }

                // Reduce XZ
                if (mc.thePlayer.hurtResistantTime >= 19) {
                    val reduce = aacPushXZReducerValue.get()

                    mc.thePlayer.motionX /= reduce
                    mc.thePlayer.motionZ /= reduce
                }
            }

            "aaczero" -> if (mc.thePlayer.hurtTime > 0) {
                if (!velocityInput || mc.thePlayer.onGround || mc.thePlayer.fallDistance > 2F)
                    return

                mc.thePlayer.addVelocity(0.0, -1.0, 0.0)
                mc.thePlayer.onGround = true
            } else
                velocityInput = false

            "matrix" -> {
                if (mc.thePlayer.hurtTime <= 0) {
                    return
                }
                if (mc.thePlayer.onGround) {
                    if (mc.thePlayer.hurtTime <= 6) {
                        mc.thePlayer.motionX *= 0.700054132
                        mc.thePlayer.motionZ *= 0.700054132
                    }
                    if (mc.thePlayer.hurtTime <= 5) {
                        mc.thePlayer.motionX *= 0.803150645
                        mc.thePlayer.motionZ *= 0.803150645
                    }
                } else if (mc.thePlayer.hurtTime <= 10) {
                    mc.thePlayer.motionX *= 0.605001
                    mc.thePlayer.motionZ *= 0.605001
                }
            }

            "aemine" -> {
                if (mc.thePlayer.hurtTime <= 0) {
                    return
                }
                if (mc.thePlayer.hurtTime >= 6) {
                    mc.thePlayer.motionX *= 0.605001
                    mc.thePlayer.motionZ *= 0.605001
                    mc.thePlayer.motionY *= 0.727
                } else if (!mc.thePlayer.onGround) {
                    mc.thePlayer.motionX *= 0.305001
                    mc.thePlayer.motionZ *= 0.305001
                    mc.thePlayer.motionY -= 0.095
                }
            }

            "allac" -> {
                if (mc.thePlayer.hurtTime > 0) {//0.00000012 reduce lmao
                    mc.thePlayer.motionX += -1.0E-7
                    mc.thePlayer.motionY += -1.0E-7
                    mc.thePlayer.motionZ += -1.0E-7
                    mc.thePlayer.isAirBorne = true
                }
            }

            "grimac" ->
                if (transactionQueue.isEmpty() && grimPacket) grimPacket = false

            "intave" -> if (velocityInput) {
                if (mc.thePlayer.hurtTime == 9) {
                    if (++jumped % 2 == 0 && mc.thePlayer.onGround && mc.thePlayer.isSprinting && mc.currentScreen == null) {
                        mc.gameSettings.keyBindJump.pressed = true
                        jumped = 0 // reset
                    }
                } else {
                    mc.gameSettings.keyBindJump.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindJump)
                    velocityInput = false
                }
            }

            "smart" -> if (velocityInput) {
                if (mc.thePlayer.onGround && mc.thePlayer.hurtTime == 9 && mc.thePlayer.isSprinting && mc.currentScreen == null) {
                    if (jumped > 2) {
                        jumped = 0
                    } else {
                        ++jumped
                        if (mc.thePlayer.ticksExisted % 5 != 0) mc.gameSettings.keyBindJump.pressed = true
                    }
                } else if (mc.thePlayer.hurtTime == 8) {
                    mc.gameSettings.keyBindJump.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindJump)
                    velocityInput = false
                }
            }

            "jumpreset" ->
                if (mc.thePlayer.hurtTime > 0 && mc.thePlayer.onGround) {
                    if (jumpResetMode.get() == "Reset") {
                        mc.thePlayer.motionY = 0.0
                        mc.thePlayer.motionX = 0.0
                        mc.thePlayer.motionZ = 0.0

                        mc.thePlayer.jump()
                    } else if (jumpResetMode.get() == "Reduce") {
                        mc.thePlayer.motionY = 0.0

                        val yaw = mc.thePlayer.rotationYaw * 0.017453292F
                        mc.thePlayer.motionX -= MathHelper.sin(yaw) * 0.2
                        mc.thePlayer.motionZ += MathHelper.cos(yaw) * 0.2

                        mc.thePlayer.jump()

                        ClientUtils.displayChatMessage("Modified X:" + mc.thePlayer.motionX)
                        ClientUtils.displayChatMessage("Modified Z:" + mc.thePlayer.motionZ)
                    } else if (jumpResetMode.get() == "Normal") {
                        mc.thePlayer.jump()
                    }
                } else if (
                    jumpResetMode.get() == "Combo" &&
                    mc.thePlayer.hurtTime == 9 &&
                    mc.thePlayer.onGround
                ) {
                    mc.thePlayer.jump()
                }
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        val killAura = LiquidBounce.moduleManager[KillAura::class.java] as KillAura

        if (packet is S12PacketEntityVelocity) {

            if (mc.thePlayer == null || (mc.theWorld?.getEntityByID(packet.entityID)
                    ?: return) != mc.thePlayer || !shouldAffect
            )
                return

            velocityTimer.reset()

            when (modeValue.get().lowercase(Locale.getDefault())) {
                "cancel" -> event.cancelEvent()
                "simple" -> {
                    val horizontal = horizontalValue.get()
                    val vertical = verticalValue.get()

                    packet.motionX = (packet.getMotionX() * horizontal).toInt()
                    packet.motionY = (packet.getMotionY() * vertical).toInt()
                    packet.motionZ = (packet.getMotionZ() * horizontal).toInt()
                }

                "hypixel" -> {
                    if (mc.thePlayer.onGround || mc.thePlayer.fallDistance > 0.0) {
                        mc.thePlayer.motionY = packet.getMotionY().toDouble() / 8000.0
                    }
                    event.cancelEvent()
                }

                "aac4reduce" -> {
                    velocityInput = true
                    packet.motionX = (packet.getMotionX() * 0.6).toInt()
                    packet.motionZ = (packet.getMotionZ() * 0.6).toInt()
                }

                "aac", "aac5reduce", "reverse", "aacv4", "smoothreverse", "aaczero", "intave" -> velocityInput = true

                "aac5.2.0" -> {
                    event.cancelEvent()
                    if (!mc.isIntegratedServerRunning && (!aac5KillAuraValue.get() || killAura.target != null)) mc.netHandler.addToSendQueue(
                        C03PacketPlayer.C04PacketPlayerPosition(
                            mc.thePlayer.posX,
                            1.7976931348623157E+308,
                            mc.thePlayer.posZ,
                            true
                        )
                    )
                }

                "smart" -> {
                    if (packet.motionX * packet.motionX + packet.motionZ * packet.motionZ + packet.motionY * packet.motionY > 640000) velocityInput =
                        true
                }

                "glitch" -> {
                    if (!mc.thePlayer.onGround)
                        return

                    velocityInput = true
                    event.cancelEvent()
                }

                "phase" -> mc.thePlayer.setPositionAndUpdate(
                    mc.thePlayer.posX,
                    mc.thePlayer.posY + phaseOffsetValue.get().toDouble(),
                    mc.thePlayer.posZ
                )

                "legit" -> {
                    pos = BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ)
                }

                "grimac" -> {
                    event.cancelEvent()
                    grimPacket = true
                }

                "grimcombat" -> {
                    val target = LiquidBounce.combatManager.getNearByEntity(3f)
                    repeat(12) {
                        mc.thePlayer.sendQueue.addToSendQueue(
                            C02PacketUseEntity(
                                target,
                                C02PacketUseEntity.Action.ATTACK
                            )
                        )
                        mc.thePlayer.sendQueue.addToSendQueue(C0APacketAnimation())
                    }
                    event.cancelEvent()
                    mc.thePlayer.motionY = packet.getMotionY().toDouble() / 8000.0
                    mc.thePlayer.motionX *= 0
                    mc.thePlayer.motionZ *= 0
                }
            }

            if (packet is S27PacketExplosion) {
                mc.thePlayer.motionX = mc.thePlayer.motionX + packet.func_149149_c() * (horizontalExplosionValue.get())
                mc.thePlayer.motionY = mc.thePlayer.motionY + packet.func_149144_d() * (verticalExplosionValue.get())
                mc.thePlayer.motionZ = mc.thePlayer.motionZ + packet.func_149147_e() * (horizontalExplosionValue.get())
                event.cancelEvent()
            }
        }
        if (modeValue.get() == "GrimAC") {
            if (packet is S32PacketConfirmTransaction) {
                if (!grimPacket) return
                event.cancelEvent()
                transactionQueue.add(packet.actionNumber)
            }
            if (packet is C0FPacketConfirmTransaction) {
                if (!grimPacket || transactionQueue.isEmpty()) return
                if (transactionQueue.remove(packet.getUid())) event.cancelEvent()
            }
        }
    }

    @EventTarget
    fun onStrafe(event: StrafeEvent) {
        when (modeValue.get().lowercase(Locale.getDefault())) {
            "legit" -> {
                if (pos == null || mc.thePlayer.hurtTime <= 0)
                    return

                val rot = RotationUtils.getRotations(pos!!.x.toDouble(), pos!!.y.toDouble(), pos!!.z.toDouble())
                if (legitFaceValue.get()) {
                    RotationUtils.setTargetRotation(rot)
                }
                val yaw = rot.yaw
                if (legitStrafeValue.get()) {
                    val speed = MovementUtils.getSpeed()
                    val yaw1 = Math.toRadians(yaw.toDouble())
                    mc.thePlayer.motionX = -sin(yaw1) * speed
                    mc.thePlayer.motionZ = cos(yaw1) * speed
                } else {
                    var strafe = event.strafe
                    var forward = event.forward
                    val friction = event.friction

                    var f = strafe * strafe + forward * forward

                    if (f >= 1.0E-4F) {
                        f = MathHelper.sqrt_float(f)

                        if (f < 1.0F)
                            f = 1.0F

                        f = friction / f
                        strafe *= f
                        forward *= f

                        val yawSin = MathHelper.sin((yaw * Math.PI / 180F).toFloat())
                        val yawCos = MathHelper.cos((yaw * Math.PI / 180F).toFloat())

                        mc.thePlayer.motionX += strafe * yawCos - forward * yawSin
                        mc.thePlayer.motionZ += forward * yawCos + strafe * yawSin
                    }
                }
            }
        }
    }

    @EventTarget
    fun onJump(event: JumpEvent) {
        if (mc.thePlayer == null || mc.thePlayer.isInWater || mc.thePlayer.isInLava || mc.thePlayer.isInWeb || !shouldAffect)
            return

        when (modeValue.get().lowercase(Locale.getDefault())) {
            "aacpush" -> {
                jump = true

                if (!mc.thePlayer.isCollidedVertically)
                    event.cancelEvent()
            }

            "aacv4" -> {
                if (mc.thePlayer.hurtTime > 0) {
                    event.cancelEvent()
                }
            }

            "aaczero" -> if (mc.thePlayer.hurtTime > 0)
                event.cancelEvent()
        }
    }
}
