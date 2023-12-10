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
import net.minecraft.util.MathHelper
import kotlin.math.abs

@ModuleInfo(name = "MovementCorrection", spacedName = "Movement Correction", description = "Allows you to follow rotation movement.", category = ModuleCategory.MOVEMENT)
class MovementCorrection : Module() {

    private val strafeMode = ListValue("StrafeFixMode", arrayOf("Vanilla","LiquidBounce", "FDP","Kevin","None"), "LiquidBounce")
    val attackSilent = BoolValue("AttackSilentStrafe",true)
    val jump = BoolValue("JumpFix",true)
    val always = BoolValue("Always",true)
    val killAuraValue = BoolValue("KillAura",true){ !always.get() }
    val scaffoldValue = BoolValue("Scaffold",true) { !always.get() }
    val breakerValue = BoolValue("Breaker",true) { !always.get() }
    val antiFireBallValue = BoolValue("AntiFireBall",true) { !always.get() }

    private var fixed = false
    private var fixedYaw = 0f

    @EventTarget
    fun onStrafe(event: StrafeEvent) {
        val killAura = LiquidBounce.moduleManager[KillAura::class.java]!!
        val scaffold = LiquidBounce.moduleManager[Scaffold::class.java]!!
        val breaker = LiquidBounce.moduleManager[Breaker::class.java]!!
        val antiFireBall = LiquidBounce.moduleManager[AntiFireBall::class.java]!!
        val (yaw) = RotationUtils.targetRotation ?: return
        var strafe = event.strafe
        var forward = event.forward
        var friction = event.friction
        if((always.get() || killAuraValue.get() && killAura.state && killAura.target != null || scaffoldValue.get() && scaffold.state ||breakerValue.get() &&  breaker.state ||antiFireBallValue.get() && antiFireBall.state && antiFireBall.target != null) && RotationUtils.targetRotation != null) {
            when (strafeMode.get()) {
                "Vanilla" -> {
                    val yaw = if (scaffoldValue.get() && scaffold.state) MovementUtils.getRawDirectionRotation(yaw,event.forward,event.strafe) + 270 else yaw
                    var f: Float = strafe * strafe + forward * forward
                    if (f >= 1.0E-4f) {
                        f = MathHelper.sqrt_float(f)
                        if (f < 1.0f) {
                            f = 1.0f
                        }
                        f = friction / f
                        strafe *= f
                        forward *= f
                        val f1 = MathHelper.sin(yaw * 3.1415927f / 180.0f)
                        val f2 = MathHelper.cos(yaw * 3.1415927f / 180.0f)
                        mc.thePlayer.motionX += (strafe * f2 - forward * f1).toDouble()
                        mc.thePlayer.motionZ += (forward * f2 + strafe * f1).toDouble()
                    }
                    event.cancelEvent()
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

                    var angleDiff =
                        ((MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw - yaw - 22.5f - 135.0f) + 180.0).toDouble() / (45.0).toDouble()).toInt()
                    //alert("Diff: " + angleDiff + " friction: " + friction + " factor: " + factor);
                    var calcYaw = if (scaffoldValue.get() && scaffold.state || attackSilent.get() && (killAuraValue.get() && killAura.state && killAura.target != null || antiFireBallValue.get() && antiFireBall.state && antiFireBall.target != null)) {
                        yaw + 45.0f * angleDiff.toFloat()
                    } else yaw

                    var calcMoveDir = Math.max(Math.abs(strafe), Math.abs(forward)).toFloat()
                    calcMoveDir = calcMoveDir * calcMoveDir
                    var calcMultiplier = MathHelper.sqrt_float(calcMoveDir / Math.min(1.0f, calcMoveDir * 2.0f))

                    if (scaffoldValue.get() && scaffold.state || attackSilent.get() && (killAuraValue.get() && killAura.state && killAura.target != null || antiFireBallValue.get() && antiFireBall.state && antiFireBall.target != null)) {
                        when (angleDiff) {
                            1, 3, 5, 7, 9 -> {
                                if ((Math.abs(forward) > 0.005 || Math.abs(strafe) > 0.005) && !(Math.abs(forward) > 0.005 && Math.abs(
                                        strafe
                                    ) > 0.005)
                                ) {
                                    friction = friction / calcMultiplier
                                } else if (Math.abs(forward) > 0.005 && Math.abs(strafe) > 0.005) {
                                    friction = friction * calcMultiplier
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
                        event.yaw = fixedYaw;
                    }
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
        if (strafeMode.get() == "Kevin") {
            val killAura = LiquidBounce.moduleManager[KillAura::class.java]!!
            val scaffold = LiquidBounce.moduleManager[Scaffold::class.java]!!
            val breaker = LiquidBounce.moduleManager[Breaker::class.java]!!
            val antiFireBall = LiquidBounce.moduleManager[AntiFireBall::class.java]!!
            val (yaw) = RotationUtils.targetRotation ?: return
            if ((always.get() || killAuraValue.get() && killAura.state && killAura.target != null || scaffoldValue.get() && scaffold.state || breakerValue.get() && breaker.state || antiFireBallValue.get() && antiFireBall.state && antiFireBall.target != null) && RotationUtils.targetRotation != null) {
                val forward: Float = event.forward
                val strafe: Float = event.strafe
                val yaw = if (scaffoldValue.get() && scaffold.state) MovementUtils.getRawDirectionRotation(yaw,event.forward,event.strafe) + 270 else yaw
                fixedYaw = yaw
                fixed = true

                val angle = MathHelper.wrapAngleTo180_double(
                    Math.toDegrees(
                        direction(
                            mc.thePlayer.rotationYaw,
                            forward.toDouble(),
                            strafe.toDouble()
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
                                        predictedForward.toDouble(),
                                        predictedStrafe.toDouble()
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
        }
    }

    fun direction(rotationYaw: Float, moveForward: Double, moveStrafing: Double): Double {
        var yaw = rotationYaw
        if (moveForward < 0f) yaw += 180f
        var forward = 1f
        if (moveForward < 0f) forward = -0.5f else if (moveForward > 0f) forward = 0.5f
        if (moveStrafing > 0f) yaw -= 90f * forward
        if (moveStrafing < 0f) yaw += 90f * forward
        return Math.toRadians(yaw.toDouble())
    }
}