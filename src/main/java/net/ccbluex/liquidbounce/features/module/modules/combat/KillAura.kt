/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import cc.paimonmc.viamcp.ViaMCP
import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.exploit.Disabler
import net.ccbluex.liquidbounce.features.module.modules.misc.AntiBot
import net.ccbluex.liquidbounce.features.module.modules.movement.TargetStrafe
import net.ccbluex.liquidbounce.features.module.modules.player.Blink
import net.ccbluex.liquidbounce.features.module.modules.render.FreeCam
import net.ccbluex.liquidbounce.features.module.modules.world.Scaffold
import net.ccbluex.liquidbounce.features.module.modules.world.Teams
import net.ccbluex.liquidbounce.utils.*
import net.ccbluex.liquidbounce.utils.extensions.*
import net.ccbluex.liquidbounce.utils.misc.RandomUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.utils.timer.TimerUtils
import net.ccbluex.liquidbounce.value.*
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemAxe
import net.minecraft.item.ItemSword
import net.minecraft.network.handshake.client.C00Handshake
import net.minecraft.network.play.client.*
import net.minecraft.network.play.server.S45PacketTitle
import net.minecraft.potion.Potion
import net.minecraft.util.*
import net.minecraft.util.MathHelper.cos
import net.minecraft.util.MathHelper.sin
import net.minecraft.world.WorldSettings
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11
import java.util.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@ModuleInfo(name = "KillAura", spacedName = "Kill Aura", description = "Automatically attacks targets around you.",
    category = ModuleCategory.COMBAT, keyBind = Keyboard.KEY_R
)
class KillAura : Module() {
    private val attackNote = NoteValue("Attack") //region attack
    private val maxCPSValue = object : IntegerValue("MaxCPS", 8, 1, 20) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val i = minCPS
            if (i > newValue) set(i)

            attackDelay = TimerUtils.randomClickDelay(minCPS, this.get())
        }
    }

    private val maxCPS: Int by maxCPSValue

    private val minCPS by object : IntegerValue("MinCPS", 5, 1, 20) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val i = maxCPS
            if (i < newValue) set(i)

            attackDelay = TimerUtils.randomClickDelay(this.get(), maxCPS)
        }
    }

    private val hurtTime by IntegerValue("HurtTime", 10, 0, 10)

    private val priority by ListValue(
        "Priority",
        arrayOf(
            "Health",
            "Distance",
            "Direction",
            "LivingTime",
            "Armor",
            "HurtResistance",
            "HurtTime",
            "HealthAbsorption",
            "RegenAmplifier"
        ),
        "Distance"
    )
    val targetMode by ListValue("TargetMode", arrayOf("Single", "Switch", "Multi"), "Switch")

    private val switchDelay by IntegerValue("SwitchDelay", 1000, 1, 2000, "ms") { targetMode == "Switch" }
    private val multiMaxTargets by IntegerValue("LimitedMultiTargets", 0, 0, 50) { targetMode == "Multi" }

    private val noBlink by BoolValue("NoBlink", true)
    private val noScaff by BoolValue("NoScaffold", true)

    private val predict by BoolValue("Predict", true)

    private val maxPredictSizeValue: FloatValue = object : FloatValue("MaxPredictSize", 1f, 0.1f, 5f, { predict }) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val v = minPredictSize
            if (v > newValue) set(v)
        }
    }

    private val maxPredictSize by maxPredictSizeValue

    private val minPredictSize by object : FloatValue("MinPredictSize", 1f, 0.1f, 5f, { predict }) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val v = maxPredictSize
            if (v < newValue) set(v)
        }
    }
    //endregion

    private val rotationNote = NoteValue("Rotation") //region rotation

    private val searchRange by FloatValue("SearchRange", 6f, 1f, 10f, "m")
    val attackRange by object : FloatValue("AttackRange", 5f, 1f, 10f, "m") {
        override fun onChanged(oldValue: Float, newValue: Float) {
            if (newValue > searchRange) set(searchRange)
        }
    }
    private val rotationRange by object : FloatValue("RotationRange", 5f, 1f, 10f, "m") {
        override fun onChanged(oldValue: Float, newValue: Float) {
            if (newValue > searchRange) set(searchRange)
        }
    }

    private val thoughWallsRotationRange by object : FloatValue("RotationWallsRange", 5f, 0f, 10f, "m") {
        override fun onChanged(oldValue: Float, newValue: Float) {
            if (newValue > searchRange) set(searchRange)
        }
    }

    private val throughWallsAttackRange by object : FloatValue("AttackWallsRange", 4f, 0f, 10f, "m") {
        override fun onChanged(oldValue: Float, newValue: Float) {
            if (newValue > searchRange) set(searchRange)
        }
    }
    private val rangeSprintReducement by FloatValue("RangeSprintReducement", 0.4f, 0f, 2f, "m")

    // Modes
    private val rotations by ListValue("RotationMode", arrayOf("Vanilla", "Grim", "Novoline", "None"), "Vanilla")

    private val shakeAmout by FloatValue("NovolineShakeAmoutTest", 4f, 0f, 10f) { rotations == "Novoline" }

    // Turn Speed
    private val yawMaxTurnSpeedValue: FloatValue = object : FloatValue("YawMaxTurnSpeed", 180f, 0f, 180f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val v = yawMinTurnSpeed
            if (v > newValue) set(v)
        }
    }

    private val yawMaxTurnSpeed by yawMaxTurnSpeedValue

    private val yawMinTurnSpeed by object : FloatValue("YawMinTurnSpeed", 180f, 0f, 180f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val v = yawMaxTurnSpeed
            if (v < newValue) set(v)
        }
    }

    private val pitchMaxTurnSpeedValue: FloatValue = object : FloatValue("PitchMaxTurnSpeed", 180f, 0f, 180f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val v = pitchMinTurnSpeed
            if (v > newValue) set(v)
        }
    }

    private val pitchMaxTurnSpeed by pitchMaxTurnSpeedValue

    private val pitchMinTurnSpeed by object : FloatValue("PitchMinTurnSpeed", 180f, 0f, 180f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val v = pitchMaxTurnSpeed
            if (v < newValue) set(v)
        }
    }

    private val roundTurnAngle by BoolValue("RoundAngle", false) { rotations != "None" }
    private val roundAngleDirs by IntegerValue("RoundAngle-Directions", 4, 2, 90) { rotations != "None" && roundTurnAngle }

    private val shake by BoolValue("Shake", false)
    private val randomCenterNew by BoolValue("NewCalc", true) { rotations == "Vanilla" && shake }
    private val minRandValue: FloatValue = object : FloatValue("MinMultiply", 0.8f, 0f, 2f, "x", { rotations == "Vanilla" && shake }) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val v = maxRand
            if (v < newValue) set(v)
        }
    }
    private val minRand by minRandValue
    private val maxRand by object : FloatValue("MaxMultiply", 0.8f, 0f, 2f, "x", { rotations == "Vanilla" && shake }) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val v = minRand
            if (v > newValue) set(v)
        }
    }


    private val noHitCheck by BoolValue("NoHitCheck", false) { rotations != "None" }
    private val silentRotation by BoolValue("SilentRotation", true) { rotations != "None" }
    private val fov by FloatValue("FOV", 180f, 0f, 360f)
    //endregion

    private val autoblockNote = NoteValue("Autoblock")
    private val autoBlockMode by ListValue(
        "AutoBlock",
        arrayOf("None", "AfterTick", "NCP", "OldHypixel", "Vanilla", "Hypixel", "Legacy"),
        "Vanilla"
    )

    private val displayAutoBlockSettings by BoolValue("Open-AutoBlock-Settings", true) { autoBlockMode != "None" }
    private val interactAB by BoolValue("InteractAutoBlock", true) { autoBlockMode != "None" && displayAutoBlockSettings }
    private val verusAB by BoolValue("VerusAutoBlock", false) { autoBlockMode != "None" && displayAutoBlockSettings }
    private val autoblockRange by object: FloatValue("AutoBlockRange", 4.5f, 0f, 10f,  { autoBlockMode != "None" && displayAutoBlockSettings }) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            if (newValue > searchRange) set(searchRange)
        }
    }

    private val throughWallsAutoblockRange by object: FloatValue("AutoBlockWallsRange", 4.5f, 0f, 10f,  { autoBlockMode != "None" && displayAutoBlockSettings }) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            if (newValue > searchRange) set(searchRange)
        }
    }

    // smart autoblock stuff
    private val smartAutoBlockValue by BoolValue("SmartAutoBlock", false) { autoBlockMode != "None" && displayAutoBlockSettings } // thanks czech
    private val smartABItemValue by BoolValue("SmartAutoBlock-ItemCheck", true) { autoBlockMode != "None" && smartAutoBlockValue && displayAutoBlockSettings }
    private val smartABFacingValue by BoolValue("SmartAutoBlock-FacingCheck", true) { autoBlockMode != "None" && smartAutoBlockValue && displayAutoBlockSettings }
    private val smartABRangeValue by FloatValue("SmartAB-Range", 3.5F, 3F, 8F, "m") { autoBlockMode != "None" && smartAutoBlockValue && displayAutoBlockSettings }
    private val smartABTolerationValue by FloatValue("SmartAB-Toleration", 0F, 0F, 2F) { autoBlockMode != "None" && smartAutoBlockValue && displayAutoBlockSettings }

    private val afterTickPatch = BoolValue("AfterTickPatch", true) { autoBlockMode == "AfterTick" && displayAutoBlockSettings }
    private val blockRate by IntegerValue("BlockRate", 100, 1, 100, "%") { autoBlockMode != "None" && displayAutoBlockSettings }


    //endregion

    private val bypassNote by NoteValue("Bypass") //region bypass
    private val raycast by BoolValue("RayCast", true)
    private val raycastIgnored by BoolValue("RayCastIgnored", false) { raycast }
    private val livingRaycast by BoolValue("LivingRayCast", true) { raycast }
    private val aac by BoolValue("AAC", false)
    private val rotationStrafe by ListValue("Strafe", arrayOf("Off", "Strict", "Silent", "Fdp"), "Off")
    private val testSilent by BoolValue("Silent", true) { rotationStrafe == "Fdp" && rotations != "None" }
    private val failRate by FloatValue("FailRate", 0f, 0f, 100f)
    private val fakeSwing by BoolValue("FakeSwing", true)
    private val noInvAttack by BoolValue("NoInvAttack", false)
    private val noInvAttackDelay by IntegerValue("NoInvDelay", 200, 0, 500, "ms") { noInvAttack }
    private val swing by BoolValue("Swing", true)
    private val swingOrder by BoolValue("1.9OrderCheck", true) { swing }
    private val keepSprint by BoolValue("KeepSprint", true)
    //endregion

    private val visualNote by NoteValue("Visual") //region visual
    private val circle by BoolValue("Circle", true)
    private val circleAccuracy by IntegerValue("Accuracy", 59, 0, 59) { circle }
    private val circleThickness by FloatValue("Thickness", 2f, 0f, 20f) { circle }
    private val circleRed by IntegerValue("Red", 255, 0, 255) { circle }
    private val circleGreen by IntegerValue("Green", 255, 0, 255) { circle }
    private val circleBlue by IntegerValue("Blue", 255, 0, 255) { circle }
    private val circleAlpha by IntegerValue("Alpha", 255, 0, 255) { circle }
    private val fakeSharp by BoolValue("FakeSharp", true)
    private val fakeSharpSword by BoolValue("FakeSharp-SwordOnly", true) { fakeSharp }
    //endregion

    /**
     * MODULE
     */

    // Target
    var target: EntityLivingBase? = null
    var currentTarget: EntityLivingBase? = null
    var hitable = false
    private val prevTargetEntities = mutableListOf<Int>()

    private var markEntity: EntityLivingBase? = null

    // Attack delay
    private val attackTimer = MSTimer()
    private var attackDelay = 0L
    var clicks = 0

    // Container Delay
    private var containerOpen = -1L

    // Fake block status
    private var verusBlocking = false

    private var smartBlocking = false

    // Block status
    var blockingStatus = false

    private val canSmartBlock: Boolean
        get() = !smartAutoBlockValue || smartBlocking

    /**
     * Enable kill aura module
     */
    override fun onEnable() {
        mc.thePlayer ?: return
        mc.theWorld ?: return

//        updateTarget()
        verusBlocking = false
        smartBlocking = false
    }

    /**
     * Disable kill aura module
     */
    override fun onDisable() {
        target = null
        currentTarget = null
        hitable = false
        prevTargetEntities.clear()
        attackTimer.reset()
        clicks = 0

        stopBlocking()
        if (verusBlocking && !blockingStatus && !mc.thePlayer.isBlocking) {
            verusBlocking = false
            if (verusAB)
                PacketUtils.sendPacketNoEvent(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN))
        }
    }

    /**
     * Motion event
     */
    @EventTarget
    fun onMotion(event: MotionEvent) {
        if (event.eventState == EventState.POST) {
            target ?: return
            currentTarget ?: return

            // Update hitable
            updateHitable()

            // AutoBlock
            if (autoBlockMode == "AfterTick" && canBlock)
                startBlocking(currentTarget!!, hitable)
        }

        if (rotationStrafe == "Off")
            update()
    }


    /**
     * Strafe event
     */
    @EventTarget
    fun onStrafe(event: StrafeEvent) {
        val targetStrafe = LiquidBounce.moduleManager.getModule(TargetStrafe::class.java)!!
        if (rotationStrafe == "Off" && !targetStrafe.state)
            return

        update()

        if (currentTarget != null && RotationUtils.targetRotation != null) {
            when (rotationStrafe.lowercase(Locale.getDefault())) {
                "strict" -> {
                    val (yaw) = RotationUtils.targetRotation ?: return
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
                    event.cancelEvent()
                }

                "silent" -> {
                    update()

                    RotationUtils.targetRotation.applyStrafeToPlayer(event)
                    event.cancelEvent()
                }

                "fdp" -> {
                    val (yaw) = RotationUtils.targetRotation ?: return
                    var strafe = event.strafe
                    var forward = event.forward
                    var friction = event.friction
                    var factor = strafe * strafe + forward * forward

                    var angleDiff =
                        ((MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw - yaw - 22.5f - 135.0f) + 180.0).toDouble() / (45.0).toDouble()).toInt()
                    //alert("Diff: " + angleDiff + " friction: " + friction + " factor: " + factor);
                    var calcYaw = if (testSilent) {
                        yaw + 45.0f * angleDiff.toFloat()
                    } else yaw

                    var calcMoveDir = max(abs(strafe), abs(forward)).toFloat()
                    calcMoveDir *= calcMoveDir
                    var calcMultiplier = MathHelper.sqrt_float(calcMoveDir / min(1.0f, calcMoveDir * 2.0f))

                    if (testSilent) {
                        when (angleDiff) {
                            1, 3, 5, 7, 9 -> {
                                if ((abs(forward) > 0.005 || abs(strafe) > 0.005) && !(abs(forward) > 0.005 && abs(
                                        strafe
                                    ) > 0.005)
                                ) {
                                    friction /= calcMultiplier
                                } else if (abs(forward) > 0.005 && abs(strafe) > 0.005) {
                                    friction *= calcMultiplier
                                }
                            }
                        }
                    }
                    if (factor >= 1.0E-4F) {
                        factor = MathHelper.sqrt_float(factor)

                        if (factor < 1.0F) {
                            factor = 1.0F
                        }

                        factor = friction / factor
                        strafe *= factor
                        forward *= factor

                        val yawSin = MathHelper.sin((calcYaw * Math.PI / 180F).toFloat())
                        val yawCos = MathHelper.cos((calcYaw * Math.PI / 180F).toFloat())

                        mc.thePlayer.motionX += strafe * yawCos - forward * yawSin
                        mc.thePlayer.motionZ += forward * yawCos + strafe * yawSin
                    }
                    event.cancelEvent()
                }
            }
        }
    }


    @EventTarget
    fun onJump(event: JumpEvent) {
        if (rotationStrafe == "Strict" || rotationStrafe == "Fdp" && !testSilent) {
            if (currentTarget != null && RotationUtils.targetRotation != null) {
                event.yaw = RotationUtils.targetRotation.yaw
            }
        }
    }


    fun update() {
        if (cancelRun || (noInvAttack && (mc.currentScreen is GuiContainer || System.currentTimeMillis() - containerOpen < noInvAttackDelay)))
            return

        // Update target
        updateTarget()

        if (target == null) {
            stopBlocking()
            return
        }

        // Target
        currentTarget = target

        if (targetMode != "Switch" && isEnemy(currentTarget))
            target = currentTarget
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (verusBlocking && ((packet is C07PacketPlayerDigging && packet.status == C07PacketPlayerDigging.Action.RELEASE_USE_ITEM) || packet is C08PacketPlayerBlockPlacement) && verusAB)
            event.cancelEvent()

        if (packet is C09PacketHeldItemChange)
            verusBlocking = false
    }

    /**
     * Update event
     */
    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        updateKA()

        smartBlocking = false
        if (smartAutoBlockValue && target != null) {
            val smTarget = target!!
            if (!smartABItemValue || (smTarget.heldItem != null && smTarget.heldItem.item != null && (smTarget.heldItem.item is ItemSword || smTarget.heldItem.item is ItemAxe))) {
                if (mc.thePlayer.getDistanceToEntityBox(smTarget) < smartABRangeValue) {
                    if (smartABFacingValue) {
                        if (smTarget.rayTrace(smartABRangeValue.toDouble(), 1F).typeOfHit == MovingObjectPosition.MovingObjectType.MISS) {
                            val eyesVec = smTarget.getPositionEyes(1F)
                            val lookVec = smTarget.getLook(1F)
                            val pointingVec = eyesVec.addVector(lookVec.xCoord * smartABRangeValue, lookVec.yCoord * smartABRangeValue, lookVec.zCoord * smartABRangeValue)
                            val border = mc.thePlayer.collisionBorderSize + smartABTolerationValue
                            val bb = mc.thePlayer.entityBoundingBox.expand(border.toDouble(), border.toDouble(), border.toDouble())
                            smartBlocking = bb.calculateIntercept(eyesVec, pointingVec) != null || bb.intersectsWith(smTarget.entityBoundingBox)
                        }
                    } else
                        smartBlocking = true
                }
            }
        }

        if (blockingStatus || mc.thePlayer.isBlocking)
            verusBlocking = true
        else if (verusBlocking) {
            verusBlocking = false
            if (verusAB)
                PacketUtils.sendPacketNoEvent(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN))
        }
    }

    private fun updateKA() {
        if (cancelRun) {
            target = null
            currentTarget = null
            hitable = false
            stopBlocking()
            return
        }

        if (noInvAttack && (mc.currentScreen is GuiContainer || System.currentTimeMillis() - containerOpen < noInvAttackDelay)) {
            target = null
            currentTarget = null
            hitable = false
            if (mc.currentScreen is GuiContainer) containerOpen = System.currentTimeMillis()
            return
        }

        if (target != null && currentTarget != null) {
            while (clicks > 0) {
                runAttack()
                clicks--
            }
        }
    }

    /**
     * Render event
     */
    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        if (circle) {
            GL11.glPushMatrix()
            GL11.glTranslated(
                mc.thePlayer.lastTickPosX + (mc.thePlayer.posX - mc.thePlayer.lastTickPosX) * mc.timer.renderPartialTicks - mc.renderManager.renderPosX,
                mc.thePlayer.lastTickPosY + (mc.thePlayer.posY - mc.thePlayer.lastTickPosY) * mc.timer.renderPartialTicks - mc.renderManager.renderPosY,
                mc.thePlayer.lastTickPosZ + (mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ) * mc.timer.renderPartialTicks - mc.renderManager.renderPosZ
            )
            GL11.glEnable(GL11.GL_BLEND)
            GL11.glEnable(GL11.GL_LINE_SMOOTH)
            GL11.glDisable(GL11.GL_TEXTURE_2D)
            GL11.glDisable(GL11.GL_DEPTH_TEST)
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

            GL11.glLineWidth(circleThickness)
            GL11.glColor4f(
                circleRed.toFloat() / 255.0F,
                circleGreen.toFloat() / 255.0F,
                circleBlue.toFloat() / 255.0F,
                circleAlpha.toFloat() / 255.0F
            )
            GL11.glRotatef(90F, 1F, 0F, 0F)
            GL11.glBegin(GL11.GL_LINE_STRIP)

            for (i in 0..360 step 60 - circleAccuracy) { // You can change circle accuracy  (60 - accuracy)
                GL11.glVertex2f(
                    kotlin.math.cos(i * Math.PI / 180.0).toFloat() * attackRange,
                    (kotlin.math.sin(i * Math.PI / 180.0).toFloat() * attackRange)
                )
            }

            GL11.glEnd()

            GL11.glDisable(GL11.GL_BLEND)
            GL11.glEnable(GL11.GL_TEXTURE_2D)
            GL11.glEnable(GL11.GL_DEPTH_TEST)
            GL11.glDisable(GL11.GL_LINE_SMOOTH)

            GL11.glPopMatrix()
        }

        if (cancelRun) {
            target = null
            currentTarget = null
            hitable = false
            stopBlocking()
            return
        }

        if (noInvAttack && (mc.currentScreen is GuiContainer || System.currentTimeMillis() - containerOpen < noInvAttackDelay)) {
            target = null
            currentTarget = null
            hitable = false
            if (mc.currentScreen is GuiContainer) containerOpen = System.currentTimeMillis()
            return
        }

        target ?: return

        if (currentTarget != null && attackTimer.hasTimePassed(attackDelay) &&
            currentTarget!!.hurtTime <= hurtTime
        ) {
            clicks++
            attackTimer.reset()
            attackDelay = TimerUtils.randomClickDelay(minCPS, maxCPS)
        }
    }

    /**
     * Handle entity move
     */
    @EventTarget
    fun onEntityMove(event: EntityMovementEvent) {
        val movedEntity = event.movedEntity

        if (target == null || movedEntity != currentTarget)
            return

        updateHitable()
    }

    /**
     * Attack enemy
     */
    private fun runAttack() {
        target ?: return
        currentTarget ?: return

        // Settings
        val multi = targetMode == "Multi"
        val openInventory = aac && mc.currentScreen is GuiInventory
        val failHit = failRate > 0 && Random().nextInt(100) <= failRate

        // Close inventory when open
        if (openInventory)
            mc.netHandler.addToSendQueue(C0DPacketCloseWindow())

        // Check is not hitable or check failrate
        if (!hitable || failHit) {
            if (swing && (fakeSwing || failHit))
                mc.thePlayer.swingItem()
        } else {
            // Attack
            if (multi) {
                var targets = 0

                for (entity in mc.theWorld.loadedEntityList) {
                    val distance = mc.thePlayer.getDistanceToEntityBox(entity)

//                    if (entity is EntityLivingBase && isEnemy(entity) && distance <= getRange(entity)) {
                    if (entity is EntityLivingBase && isEnemy(entity) && distance <= searchRange) {
                        attackEntity(entity)

                        targets += 1

                        if (multiMaxTargets != 0 && multiMaxTargets <= targets)
                            break
                    }
                }
            } else {
                attackEntity(currentTarget!!)
            }

            prevTargetEntities.add(if (aac) target!!.entityId else currentTarget!!.entityId)

            if (target == currentTarget)
                target = null
        }

        if(targetMode == "Switch" && attackTimer.hasTimePassed((switchDelay).toLong())) {
            if(switchDelay != 0) {
                prevTargetEntities.add(if (aac) target!!.entityId else currentTarget!!.entityId)
                attackTimer.reset()
            }
        }

        // Open inventory
        if (openInventory)
            mc.netHandler.addToSendQueue(C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT))
    }

    /**
     * Update current target
     */
    private fun updateTarget() {

        // Settings
        val switchMode = targetMode == "Switch"

        // Find possible targets
        val targets = mutableListOf<EntityLivingBase>()

        for (entity in mc.theWorld.loadedEntityList) {
            if (entity !is EntityLivingBase || !isEnemy(entity) || (switchMode && prevTargetEntities.contains(entity.entityId))/* || (!focusEntityName.isEmpty() && !focusEntityName.contains(entity.name.toLowerCase()))*/)
                continue

            val distance = mc.thePlayer.getDistanceToEntityBox(entity)
            val entityFov = RotationUtils.getRotationDifference(entity)

            if (distance <= searchRange && (fov == 180F || entityFov <= fov) && entity.hurtTime <= hurtTime)
                targets.add(entity)
        }

        // Sort targets by priority
        when (priority.lowercase(Locale.getDefault())) {
            "distance" -> targets.sortBy { mc.thePlayer.getDistanceToEntityBox(it) } // Sort by distance
            "health" -> targets.sortBy { it.health } // Sort by health
            "direction" -> targets.sortBy { RotationUtils.getRotationDifference(it) } // Sort by FOV
            "livingtime" -> targets.sortBy { -it.ticksExisted } // Sort by existence
            "hurtresistance" -> targets.sortBy { it.hurtResistantTime } // Sort by armor hurt time
            "hurttime" -> targets.sortBy { it.hurtTime } // Sort by hurt time
            "healthabsorption" -> targets.sortBy { it.health + it.absorptionAmount } // Sort by full health with absorption effect
            "regenamplifier" -> targets.sortBy { if (it.isPotionActive(Potion.regeneration)) it.getActivePotionEffect(Potion.regeneration).amplifier else -1 }
        }

        var found = false

        // Find best target
        for (entity in targets) {
            // Update rotations to current target
            if (!updateRotations(entity)) // when failed then try another target
                continue

            // Set target to current entity
            target = entity
            found = true
            break
        }


        if (found) {
//            if (rotations.get().equals("spin", true)) {
//                spinYaw += RandomUtils.nextFloat(minSpinSpeed.get(), maxSpinSpeed.get())
//                spinYaw = MathHelper.wrapAngleTo180_float(spinYaw)
//                val rot = Rotation(spinYaw, 90F)
//                RotationUtils.setTargetRotation(rot, 0)
//            }
            return
        } else {
            target = null
        }

//        if (searchTarget != null) {
//            if (target != searchTarget) target = searchTarget
//            return
//        } else {
//            target = null
//        }


        // Cleanup last targets when no target found and try again
        if (prevTargetEntities.isNotEmpty()) {
            prevTargetEntities.clear()
            updateTarget()
        }
    }

    /**
     * Check if [entity] is selected as enemy with current target options and other modules
     */
    private fun isEnemy(entity: Entity?): Boolean {
        if (entity is EntityLivingBase && (EntityUtils.targetDead || isAlive(entity)) && entity != mc.thePlayer) {
            if (!EntityUtils.targetInvisible && entity.isInvisible())
                return false

            if (EntityUtils.targetPlayer && entity is EntityPlayer) {
                if (entity.isSpectator || AntiBot.isBot(entity))
                    return false

                if (EntityUtils.isFriend(entity) && !LiquidBounce.moduleManager[NoFriends::class.java]!!.state)
                    return false

                val teams = LiquidBounce.moduleManager[Teams::class.java] as Teams

                return !teams.state || !teams.isInYourTeam(entity)
            }

            return EntityUtils.targetMobs && EntityUtils.isMob(entity) || EntityUtils.targetAnimals &&
                    EntityUtils.isAnimal(entity)
        }

        return false
    }

    /**
     * Attack [entity]
     */
    private fun attackEntity(entity: EntityLivingBase) {
        if (mc.thePlayer.getDistanceToEntity(entity) <= getAttackRange(entity)) {

            // Stop blocking
            if (mc.thePlayer.isBlocking || blockingStatus)
                stopBlocking()

            // Call attack event
            LiquidBounce.eventManager.callEvent(AttackEvent(entity))

            markEntity = entity

            // Attack target
            if (swing && (!swingOrder || ViaMCP.getInstance().version <= 47)) // version fix
                mc.thePlayer.swingItem()

            mc.netHandler.addToSendQueue(C02PacketUseEntity(entity, C02PacketUseEntity.Action.ATTACK))

            if (swing && swingOrder && ViaMCP.getInstance().version > 47)
                mc.thePlayer.swingItem()

            if (keepSprint) {
                // Critical Effect
                if (mc.thePlayer.fallDistance > 0F && !mc.thePlayer.onGround && !mc.thePlayer.isOnLadder &&
                    !mc.thePlayer.isInWater && !mc.thePlayer.isPotionActive(Potion.blindness) && !mc.thePlayer.isRiding
                )
                    mc.thePlayer.onCriticalHit(entity)

                // Enchant Effect
                if (EnchantmentHelper.getModifierForCreature(mc.thePlayer.heldItem, entity.creatureAttribute) > 0F)
                    mc.thePlayer.onEnchantmentCritical(entity)
            } else {
                if (mc.playerController.currentGameType != WorldSettings.GameType.SPECTATOR)
                    mc.thePlayer.attackTargetEntityWithCurrentItem(entity)
            }

            // Extra critical effects
            val criticals = LiquidBounce.moduleManager[Criticals::class.java] as Criticals

            for (i in 0..2) {
                // Critical Effect
                if (mc.thePlayer.fallDistance > 0F && !mc.thePlayer.onGround && !mc.thePlayer.isOnLadder && !mc.thePlayer.isInWater && !mc.thePlayer.isPotionActive(Potion.blindness) && mc.thePlayer.ridingEntity == null || criticals.state && criticals.msTimer.hasTimePassed(criticals.delayValue.get().toLong()) && !mc.thePlayer.isInWater && !mc.thePlayer.isInLava && !mc.thePlayer.isInWeb)
                    mc.thePlayer.onCriticalHit(target)

                // Enchant Effect
                if (EnchantmentHelper.getModifierForCreature(mc.thePlayer.heldItem, target!!.creatureAttribute) > 0.0f || (fakeSharp && (!fakeSharpSword || canBlock)))
                    mc.thePlayer.onEnchantmentCritical(target)
            }
        }

        // Start blocking after attack
        if ((!afterTickPatch.get() || autoBlockMode != "AfterTick") && (mc.thePlayer.isBlocking || canBlock))
            startBlocking(entity, interactAB)
    }

    /**
     * Update killaura rotations to enemy
     *
     * TODO: seperate this from update()
     */
    private fun updateRotations(entity: Entity): Boolean {
        if (rotations == "None") return true
        if (mc.thePlayer.getDistanceToEntity(entity) > getRotationRange(entity)) {
            return false
        }

        val disabler = LiquidBounce.moduleManager.getModule(Disabler::class.java)!!
        val modify = disabler.canModifyRotation

        if (modify) return true // just ignore then

        val defRotation = getTargetRotation(entity) ?: return false

        if (defRotation != RotationUtils.serverRotation && roundTurnAngle)
            defRotation.yaw = RotationUtils.roundRotation(defRotation.yaw, roundAngleDirs)

        if (silentRotation) {
            RotationUtils.setTargetRotation(defRotation, if (aac) 15 else 0)
        } else {
            defRotation.toPlayer(mc.thePlayer!!)
        }

        return true
    }

    private fun getTargetRotation(entity: Entity): Rotation? {

        var boundingBox = entity.hitBox
        val amount = shakeAmout
        val range = getRotationRange(entity)

        if (predict)
            boundingBox = boundingBox.offset(
                (entity.posX - entity.prevPosX - (mc.thePlayer!!.posX - mc.thePlayer!!.prevPosX)) * RandomUtils.nextFloat(
                    minPredictSize,
                    maxPredictSize
                ),
                (entity.posY - entity.prevPosY - (mc.thePlayer!!.posY - mc.thePlayer!!.prevPosY)) * RandomUtils.nextFloat(
                    minPredictSize,
                    maxPredictSize
                ),
                (entity.posZ - entity.prevPosZ - (mc.thePlayer!!.posZ - mc.thePlayer!!.prevPosZ)) * RandomUtils.nextFloat(
                    minPredictSize,
                    maxPredictSize
                )
            )

        if (rotations == "Vanilla"){
            val (_, rotation) = RotationUtils.searchCenter(
                boundingBox,
                false,
                shake,
                predict,
                !mc.thePlayer.canEntityBeSeen(entity),
                range,
                RandomUtils.nextFloat(minRand, maxRand),
                randomCenterNew
            )
            return RotationUtils.limitAngleChange(RotationUtils.serverRotation, rotation,
                (Math.random() * (yawMaxTurnSpeed - yawMinTurnSpeed) + yawMinTurnSpeed).toFloat(),
                (Math.random() * (pitchMaxTurnSpeed - pitchMinTurnSpeed) + pitchMinTurnSpeed).toFloat()
            )

        }
        if (rotations == "Grim") {
            val bb : AxisAlignedBB = entity.entityBoundingBox
            val thePlayer = mc.thePlayer
            val random = Random()
            var lastHitVec = Vec3(0.0, 0.0, 0.0)
            return RotationUtils.limitAngleChange(
                RotationUtils.serverRotation,
                RotationUtils.OtherRotation(
                    boundingBox,
                    if (shake) {
                        if (RotationUtils.targetRotation == null || (random.nextBoolean() && !attackTimer.hasTimePassed(
                                attackDelay / 2
                            ))
                        ) {
                            lastHitVec = Vec3(
                                MathHelper.clamp_double(thePlayer.posX, bb.minX, bb.maxX) + RandomUtils.nextDouble(
                                    -0.2,
                                    0.2
                                ),
                                MathHelper.clamp_double(
                                    thePlayer.posY + 1.62F,
                                    bb.minY,
                                    bb.maxY
                                ) + RandomUtils.nextDouble(-0.2, 0.2),
                                MathHelper.clamp_double(thePlayer.posZ, bb.minZ, bb.maxZ) + RandomUtils.nextDouble(
                                    -0.2,
                                    0.2
                                )
                            )
                        }
                        lastHitVec
                    } else getNearestPointBB(mc.thePlayer.getPositionEyes(1f), entity.entityBoundingBox),
                    predict,
                    !mc.thePlayer.canEntityBeSeen(entity),
                    range
                ),
                (Math.random() * (yawMaxTurnSpeed - yawMinTurnSpeed) + yawMinTurnSpeed).toFloat(),
                (Math.random() * (pitchMaxTurnSpeed - pitchMinTurnSpeed) + pitchMinTurnSpeed).toFloat()
            )
        }
        if (rotations == "Novoline") {
            return Rotation(
                (RotationUtils.getAngles(entity).yaw + Math.random() * amount - amount / 2).toFloat(),
                (RotationUtils.getAngles(entity).pitch + Math.random() * amount - amount / 2).toFloat()
            )
        }
        return RotationUtils.serverRotation
    }

    /**
     * Check if enemy is hitable with current rotations
     */
    private fun updateHitable() {
        if (rotations == "None") {
            hitable = true
            return
        }

        val disabler = LiquidBounce.moduleManager.getModule(Disabler::class.java)!!

        // Completely disable rotation check if turn speed equals to 0 or NoHitCheck is enabled
        if(yawMaxTurnSpeed <= 0F || noHitCheck || disabler.canModifyRotation) {
            hitable = true
            return
        }

        val reach = min(attackRange.toDouble(), mc.thePlayer.getDistanceToEntityBox(target!!)) + 1

        if (raycast) {
            val raycastedEntity = RaycastUtils.raycastEntity(reach) {
                (!livingRaycast || it is EntityLivingBase && it !is EntityArmorStand) &&
                        (isEnemy(it) || raycastIgnored || aac && mc.theWorld.getEntitiesWithinAABBExcludingEntity(it, it.entityBoundingBox).isNotEmpty())
            }

            if (raycast && raycastedEntity is EntityLivingBase && (LiquidBounce.moduleManager[NoFriends::class.java]!!.state || !EntityUtils.isFriend(raycastedEntity)))
                currentTarget = raycastedEntity

            hitable = if(yawMaxTurnSpeed > 0F) currentTarget == raycastedEntity else true
        } else
            hitable = RotationUtils.isFaced(currentTarget, reach)
    }

    /**
     * Start blocking
     */

    private fun startBlocking(interactEntity: Entity, interact: Boolean) {
        if (mc.thePlayer.getDistanceToEntity(interactEntity) > getBlockRange(interactEntity)) {
            stopBlocking()
            blockingStatus = false
            return
        }

        if (!canSmartBlock || autoBlockMode == "None" || !(blockRate > 0 && Random().nextInt(100) <= blockRate))
            return

        if (autoBlockMode == "NCP") {
            mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(BlockPos(-1, -1, -1), 255, null, 0.0f, 0.0f, 0.0f))
            blockingStatus = true
            return
        }

        if (autoBlockMode == "OldHypixel") {
            mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(BlockPos(-1, -1, -1), 255, mc.thePlayer.inventory.getCurrentItem(), 0.0f, 0.0f, 0.0f))
            blockingStatus = true
            return
        }

        if (interact) {
            //mc.netHandler.addToSendQueue(C02PacketUseEntity(interactEntity, interactEntity.positionVector))
            val positionEye = mc.renderViewEntity?.getPositionEyes(1F)

            val expandSize = interactEntity.collisionBorderSize.toDouble()
            val boundingBox = interactEntity.entityBoundingBox.expand(expandSize, expandSize, expandSize)

            val (yaw, pitch) = RotationUtils.targetRotation ?: Rotation(mc.thePlayer!!.rotationYaw, mc.thePlayer!!.rotationPitch)
            val yawCos = cos(-yaw * 0.017453292F - Math.PI.toFloat())
            val yawSin = sin(-yaw * 0.017453292F - Math.PI.toFloat())
            val pitchCos = -cos(-pitch * 0.017453292F)
            val pitchSin = sin(-pitch * 0.017453292F)
            val range = autoblockRange.toDouble()
            val lookAt = positionEye!!.addVector(yawSin * pitchCos * range, pitchSin * range, yawCos * pitchCos * range)

            val movingObject = boundingBox.calculateIntercept(positionEye, lookAt) ?: return
            val hitVec = movingObject.hitVec

            mc.netHandler.addToSendQueue(C02PacketUseEntity(interactEntity, Vec3(
                hitVec.xCoord - interactEntity.posX,
                hitVec.yCoord - interactEntity.posY,
                hitVec.zCoord - interactEntity.posZ)
            ))
            mc.netHandler.addToSendQueue(C02PacketUseEntity(interactEntity, C02PacketUseEntity.Action.INTERACT))
        }

        mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem()))
        blockingStatus = true
    }


    fun nextInt(startInclusive: Int = 0, endExclusive: Int = Int.MAX_VALUE) =
        if (endExclusive - startInclusive <= 0) startInclusive else startInclusive + kotlin.random.Random.nextInt(
            endExclusive - startInclusive
        )


    /**
     * Stop blocking
     */
    private fun stopBlocking() {
        if (blockingStatus) {
            if (autoBlockMode == "OldHypixel")
                mc.netHandler.addToSendQueue(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos(1.0, 1.0, 1.0), EnumFacing.DOWN))
            else
                mc.netHandler.addToSendQueue(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN))
            blockingStatus = false
        }
    }

    /**
     * Check if run should be cancelled
     */
    private val cancelRun: Boolean
        get() = mc.thePlayer.isSpectator || !isAlive(mc.thePlayer)
                || (noBlink && LiquidBounce.moduleManager[Blink::class.java]!!.state) || LiquidBounce.moduleManager[FreeCam::class.java]!!.state ||
                (noScaff && (LiquidBounce.moduleManager[Scaffold::class.java]!!.state))

    /**
     * Check if [entity] is alive
     */
    private fun isAlive(entity: EntityLivingBase) = entity.isEntityAlive && entity.health > 0 || aac && entity.hurtTime > 5


    /**
     * Check if player is able to block
     */
    private val canBlock: Boolean
        get() = mc.thePlayer.heldItem != null && mc.thePlayer.heldItem.item is ItemSword

    private fun getAttackRange(entity: Entity) =
        (if (mc.thePlayer.canEntityBeSeen(entity)) attackRange else throughWallsAttackRange) - (if (mc.thePlayer.isSprinting) rangeSprintReducement else 0f)
    private fun getRotationRange(entity: Entity) =
        if (mc.thePlayer.canEntityBeSeen(entity)) rotationRange else thoughWallsRotationRange
    private fun getBlockRange(entity: Entity) =
        if (mc.thePlayer.canEntityBeSeen(entity)) autoblockRange else throughWallsAutoblockRange

    /**
     * HUD Tag
     */
    override val tag: String
        get() = targetMode

    object CombatListener : Listenable {
        private var syncEntity: EntityLivingBase? = null
        private var totalPlayed = 0
        private var startTime = System.currentTimeMillis()
        var win = 0
        var killCounts = 0

        @EventTarget
        private fun onAttack(event: AttackEvent) {
            syncEntity = event.targetEntity as EntityLivingBase?
        }

        @EventTarget
        private fun onUpdate(event: UpdateEvent) {
            if (syncEntity != null && syncEntity!!.isDead) {
                ++killCounts
                syncEntity = null
            }
        }

        @EventTarget(ignoreCondition = true)
        private fun onPacket(event: PacketEvent) {
            val packet = event.packet
            if (event.packet is C00Handshake) startTime = System.currentTimeMillis()

            if (packet is S45PacketTitle) {
                val title = packet.message.formattedText
                if (title.contains("Winner")) {
                    win++
                }
                if (title.contains("BedWar")) {
                    totalPlayed++
                }
                if (title.contains("SkyWar")) {
                    totalPlayed++
                }
            }
        }

        override fun handleEvents() = true

        init {
            LiquidBounce.eventManager.registerListener(this)
        }
    }
}