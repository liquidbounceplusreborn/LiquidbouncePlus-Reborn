package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.JumpEvent
import net.ccbluex.liquidbounce.event.MovementInputUpdateEvent
import net.ccbluex.liquidbounce.event.StrafeEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.combat.AntiFireBall
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.ccbluex.liquidbounce.features.module.modules.world.Breaker
import net.ccbluex.liquidbounce.features.module.modules.world.Scaffold
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.util.BlockPos
import net.minecraft.util.MathHelper
import kotlin.math.abs


@ModuleInfo(name = "MovementCorrection", spacedName = "Movement Correction", description = "Allows you to follow rotation movement.", category = ModuleCategory.MOVEMENT)
class MovementCorrection : Module() {

    val strafeMode = ListValue("StrafeFixMode", arrayOf("Vanilla","LiquidBounce", "FDP","Kevin","Rise","InputOverride","None"), "LiquidBounce")
    val attackSilent = BoolValue("AttackSilentStrafe",true){ strafeMode.get() == "LiquidBounce" || strafeMode.get() == "FDP" }
    val jump = BoolValue("JumpFix",true)
    val always = BoolValue("Always",true)
    val killAuraValue = BoolValue("KillAura",true){ !always.get() }
    val scaffoldValue = BoolValue("Scaffold",true) { !always.get() }
    val breakerValue = BoolValue("Breaker",true) { !always.get() }
    val antiFireBallValue = BoolValue("AntiFireBall",true) { !always.get() }

    private var fixed = false
    private var fixedYaw = 0f

    fun canFix() : Boolean{
        val killAura = LiquidBounce.moduleManager[KillAura::class.java]!!
        val scaffold = LiquidBounce.moduleManager[Scaffold::class.java]!!
        val breaker = LiquidBounce.moduleManager[Breaker::class.java]!!
        val antiFireBall = LiquidBounce.moduleManager[AntiFireBall::class.java]!!
        return (always.get() || killAuraValue.get() && killAura.state && killAura.target != null || scaffoldValue.get() && scaffold.state ||breakerValue.get() &&  breaker.state ||antiFireBallValue.get() && antiFireBall.state && antiFireBall.target != null) && RotationUtils.targetRotation != null
    }

    @EventTarget
    fun onStrafe(event: StrafeEvent) {
        val (yaw) = RotationUtils.targetRotation ?: return
        var strafe = event.strafe
        var forward = event.forward
        var friction = event.friction
        val scaffold = LiquidBounce.moduleManager[Scaffold::class.java]!!
        val killAura = LiquidBounce.moduleManager[KillAura::class.java]!!
        val antiFireBall = LiquidBounce.moduleManager[AntiFireBall::class.java]!!
        if(canFix()) {
            when (strafeMode.get()) {
                "Vanilla" -> {
                    val yaw = if (scaffoldValue.get() && scaffold.state) MovementUtils.getRawDirectionRotation(yaw,event.forward,event.strafe) + 270 else yaw
                    event.yaw = yaw
                }
                "LiquidBounce" -> {
                    RotationUtils.targetRotation!!.applyStrafeToPlayer(event,!scaffoldValue.get() && !scaffold.state || !attackSilent.get() && !(killAuraValue.get() && killAura.state && killAura.target != null || antiFireBallValue.get() && antiFireBall.state && antiFireBall.target != null))
                    event.cancelEvent()
                }

                "FDP" -> {
                    if (event.isCancelled) {
                        return
                    }
                    var factor = strafe * strafe + forward * forward

                    val angleDiff =
                        ((MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw - yaw - 22.5f - 135.0f) + 180.0) / (45.0).toDouble()).toInt()
                    val calcYaw = if (scaffoldValue.get() && scaffold.state || attackSilent.get() && (killAuraValue.get() && killAura.state && killAura.target != null || antiFireBallValue.get() && antiFireBall.state && antiFireBall.target != null)) {
                        yaw + 45.0f * angleDiff.toFloat()
                    } else yaw

                    var calcMoveDir = Math.abs(strafe).coerceAtLeast(abs(forward))
                    calcMoveDir *= calcMoveDir
                    val calcMultiplier = MathHelper.sqrt_float(calcMoveDir / 1.0f.coerceAtMost(calcMoveDir * 2.0f))

                    if (scaffoldValue.get() && scaffold.state || attackSilent.get() && (killAuraValue.get() && killAura.state && killAura.target != null || antiFireBallValue.get() && antiFireBall.state && antiFireBall.target != null)) {
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
                "Kevin" -> {
                    if (fixed) {
                        fixed = false
                        event.yaw = fixedYaw
                    }
                }
                "Rise" -> {
                    event.yaw = yaw
                }
                "InputOverride" -> {
                    event.yaw = yaw
                }
            }
        }
    }

    @EventTarget
    fun onJump(event:JumpEvent){
        val (yaw) = RotationUtils.targetRotation ?: return
        if(jump.get() && RotationUtils.targetRotation != null){
            event.yaw = yaw
        }
    }

    @EventTarget
    fun onMovementInput(event: MovementInputUpdateEvent) {
        val (yaw) = RotationUtils.targetRotation ?: return
        if (canFix()) {
            when (strafeMode.get()) {
                "Kevin" -> {
                    val scaffold = LiquidBounce.moduleManager[Scaffold::class.java]!!
                    val forward: Float = event.forward
                    val strafe: Float = event.strafe
                    val yaw = if (scaffoldValue.get() && scaffold.state) MovementUtils.getRawDirectionRotation(
                        yaw,
                        event.forward,
                        event.strafe
                    ) + 270 else yaw
                    fixedYaw = yaw
                    fixed = true

                    val angle = MathHelper.wrapAngleTo180_double(
                        Math.toDegrees(
                            direction(
                                mc.thePlayer.rotationYaw,
                                forward,
                                strafe
                            )
                        )
                    )

                    if (forward == 0f && strafe == 0f) {
                        return
                    }

                    var closestForward = 0f
                    var closestStrafe = 0f
                    var closestDifference = Float.MAX_VALUE

                    run {
                        var predictedForward = -1f
                        while (predictedForward <= 1f) {
                            var predictedStrafe = -1f
                            while (predictedStrafe <= 1f) {
                                if (predictedStrafe == 0f && predictedForward == 0f) {
                                    predictedStrafe += 1f
                                    continue
                                }
                                val predictedAngle = MathHelper.wrapAngleTo180_double(
                                    Math.toDegrees(
                                        direction(
                                            yaw,
                                            predictedForward,
                                            predictedStrafe
                                        )
                                    )
                                )
                                val difference = abs(angle - predictedAngle)
                                if (difference < closestDifference) {
                                    closestDifference = difference.toFloat()
                                    closestForward = predictedForward
                                    closestStrafe = predictedStrafe
                                }
                                predictedStrafe += 1f
                            }
                            predictedForward += 1f
                        }
                    }

                    event.forward = closestForward
                    event.strafe = closestStrafe
                }

                "Rise" -> {
                    fixMovement(event, yaw)
                }

                "InputOverride" -> {
                    if (event.forward != 0f || event.strafe != 0f) {
                        val fixed = getMovementCorrection(event.forward, event.strafe)
                        event.forward = fixed[0]
                        event.strafe = fixed[1]
                    }
                }
            }
        }
    }

    fun getMovementCorrection(forward: Float, strafe: Float): FloatArray {
        // Set the player's current mouse rotation yaw
        val y = mc.thePlayer.rotationYaw

        // If the player's mouse yaw is the same as the client yaw, return the current movement inputs
        if (RotationUtils.targetRotation?.yaw == y) {
            return floatArrayOf(forward, strafe)
        }

        // Determine the slipperiness factor based on the player's position
        var slipperiness = 0.91f
        if (mc.thePlayer.onGround) {
            slipperiness = mc.theWorld.getBlockState(
                BlockPos(
                    MathHelper.floor_double(mc.thePlayer.posX),
                    MathHelper.floor_double(mc.thePlayer.entityBoundingBox.minY) - 1,
                    MathHelper.floor_double(mc.thePlayer.posZ)
                )
            ).block.slipperiness * 0.91f
        }

        // Calculate player's movement factor based on slipperiness and on-ground status
        val moveFactor: Float
        moveFactor = if (mc.thePlayer.onGround) {
            mc.thePlayer.aiMoveSpeed * (0.16277136f / (slipperiness * slipperiness * slipperiness))
        } else {
            mc.thePlayer.jumpMovementFactor
        }

        // Calculate normalized movement inputs
        var magnitude = strafe * strafe + forward * forward
        magnitude = moveFactor / magnitude
        val normalizedStrafe = strafe * magnitude
        val normalizedForward = forward * magnitude

        // Calculate motion values based on current mouse yaw
        val realYawSin = MathHelper.sin(y * Math.PI.toFloat() / 180.0f)
        val realYawCos = MathHelper.cos(y * Math.PI.toFloat() / 180.0f)
        val realYawMotionX = normalizedStrafe * realYawCos - normalizedForward * realYawSin
        val realYawMotionZ = normalizedForward * realYawCos + normalizedStrafe * realYawSin

        // Calculate motion values based on client yaw
        val rotationYawSin = MathHelper.sin(RotationUtils.targetRotation?.yaw!! * Math.PI.toFloat() / 180.0f)
        val rotationYawCos = MathHelper.cos(RotationUtils.targetRotation?.yaw!!  * Math.PI.toFloat() / 180.0f)

        // Store the closest movement direction found through testing different combinations
        val closest = floatArrayOf(Float.NaN, 0f, 0f)

        // bruteforce all possible strafe and forward combinations
        for (possibleStrafe in -1..1) {
            for (possibleForward in -1..1) {
                val testFStrafe = possibleStrafe * magnitude
                val testFForward = possibleForward * magnitude
                val testYawMotionX = testFStrafe * rotationYawCos - testFForward * rotationYawSin
                val testYawMotionZ = testFForward * rotationYawCos + testFStrafe * rotationYawSin

                // Calculate the distance between the real and tested motions
                val diffX = realYawMotionX - testYawMotionX
                val diffZ = realYawMotionZ - testYawMotionZ
                val distance = MathHelper.sqrt_float(diffX * diffX + diffZ * diffZ)

                // Update closest if the current combination is closer
                if (java.lang.Float.isNaN(closest[0]) || distance < closest[0]) {
                    closest[0] = distance
                    closest[1] = possibleForward.toFloat()
                    closest[2] = possibleStrafe.toFloat()
                }
            }
        }

        // Return the movement inputs corresponding to the closest direction found
        return floatArrayOf(closest[1], closest[2])
    }

    fun fixMovement(event: MovementInputUpdateEvent, yaw: Float) {
        val forward: Float = event.forward
        val strafe: Float = event.strafe
        val angle = MathHelper.wrapAngleTo180_double(
            Math.toDegrees(
                direction(
                    mc.thePlayer.rotationYaw,
                    forward,
                    strafe
                )
            )
        )
        if (forward == 0f && strafe == 0f) {
            return
        }
        var closestForward = 0f
        var closestStrafe = 0f
        var closestDifference = Float.MAX_VALUE
        var predictedForward = -1f
        while (predictedForward <= 1f) {
            var predictedStrafe = -1f
            while (predictedStrafe <= 1f) {
                if (predictedStrafe == 0f && predictedForward == 0f) {
                    predictedStrafe += 1f
                    continue
                }
                val predictedAngle = MathHelper.wrapAngleTo180_double(
                    Math.toDegrees(
                        direction(
                            yaw,
                            predictedForward,
                            predictedStrafe
                        )
                    )
                )
                val difference = abs(angle - predictedAngle)
                if (difference < closestDifference) {
                    closestDifference = difference.toFloat()
                    closestForward = predictedForward
                    closestStrafe = predictedStrafe
                }
                predictedStrafe += 1f
            }
            predictedForward += 1f
        }
        event.forward = closestForward
        event.strafe = closestStrafe
    }

    fun direction(rotationYaw: Float, moveForward: Float, moveStrafing: Float): Double {
        var yaw = rotationYaw
        if (moveForward < 0f) yaw += 180f
        var forward = 1f
        if (moveForward < 0f) forward = -0.5f else if (moveForward > 0f) forward = 0.5f
        if (moveStrafing > 0f) yaw -= 90f * forward
        if (moveStrafing < 0f) yaw += 90f * forward
        return Math.toRadians(yaw.toDouble())
    }
}