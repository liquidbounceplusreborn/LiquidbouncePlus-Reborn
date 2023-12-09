/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.utils

import net.ccbluex.liquidbounce.event.StrafeEvent
import net.ccbluex.liquidbounce.utils.block.PlaceInfo
import net.ccbluex.liquidbounce.utils.math.toRadians
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.MathHelper
import net.minecraft.util.Vec3
import kotlin.math.*

/**
 * Rotations
 */
data class Rotation(var yaw: Float, var pitch: Float) {

    /**
     * Set rotations to [player]
     */
    fun toPlayer(player: EntityPlayer) {
        if (yaw.isNaN() || pitch.isNaN())
            return

        fixedSensitivity(MinecraftInstance.mc.gameSettings.mouseSensitivity)

        player.rotationYaw = yaw
        player.rotationPitch = pitch
    }

    /**
     * Patch gcd exploit in aim
     *
     * @see net.minecraft.client.renderer.EntityRenderer.updateCameraAndRender
     */
    fun fixedSensitivity(sensitivity: Float = MinecraftInstance.mc.gameSettings.mouseSensitivity): Rotation {
        val f = sensitivity * 0.6F + 0.2F
        val gcd = f * f * f * 1.2F

        // get previous rotation
        val rotation = RotationUtils.serverRotation

        // fix yaw
        var deltaYaw = yaw - rotation.yaw
        deltaYaw -= deltaYaw % gcd
        yaw = rotation.yaw + deltaYaw

        // fix pitch
        var deltaPitch = pitch - rotation.pitch
        deltaPitch -= deltaPitch % gcd
        pitch = rotation.pitch + deltaPitch
        return this
    }

    /**
     * Apply strafe to player
     *
     * @author bestnub
     */
    fun applyStrafeToPlayer(event: StrafeEvent, strict: Boolean = false) {
        val player = MinecraftInstance.mc.thePlayer

        val diff = (player.rotationYaw - yaw).toRadians()

        val friction = event.friction

        var calcForward: Float
        var calcStrafe: Float

        if (!strict) {
            // Remove modifier, replay the updatePlayerMoveState() logic
            val (strafe, forward) = Pair(event.strafe / 0.98f, event.forward / 0.98f)

            // Filter out previous sneak / block input modifications by rounding inputs up
            val modifiedForward = ceil(abs(forward)) * forward.sign
            val modifiedStrafe = ceil(abs(strafe)) * strafe.sign

            // Remake the rotation-based input using the modified inputs
            calcForward = round(modifiedForward * cos(diff) + modifiedStrafe * sin(diff))
            calcStrafe = round(modifiedStrafe * cos(diff) - modifiedForward * sin(diff))

            // Was the user sneaking? Blocking? Both? Neither?
            val f = if (event.forward != 0f) event.forward else event.strafe

            // Apply original modifications back
            calcForward *= abs(f)
            calcStrafe *= abs(f)
        } else {
            calcForward = event.forward
            calcStrafe = event.strafe
        }

        var d = calcStrafe * calcStrafe + calcForward * calcForward

        if (d >= 1.0E-4f) {
            d = friction / sqrt(d).coerceAtLeast(1f)

            calcStrafe *= d
            calcForward *= d

            val yawRad = yaw.toRadians()
            val yawSin = sin(yawRad)
            val yawCos = cos(yawRad)

            player.motionX += calcStrafe * yawCos - calcForward * yawSin
            player.motionZ += calcForward * yawCos + calcStrafe * yawSin
        }
    }


    fun toDirection(): Vec3 {
        val f: Float = MathHelper.cos(-yaw * 0.017453292f - Math.PI.toFloat())
        val f1: Float = MathHelper.sin(-yaw * 0.017453292f - Math.PI.toFloat())
        val f2: Float = -MathHelper.cos(-pitch * 0.017453292f)
        val f3: Float = MathHelper.sin(-pitch * 0.017453292f)
        return Vec3((f1 * f2).toDouble(), f3.toDouble(), (f * f2).toDouble())
    }
}

/**
 * Rotation with vector
 */
data class VecRotation(val vec: Vec3, val rotation: Rotation)

/**
 * Rotation with place info
 */
data class PlaceRotation(val placeInfo: PlaceInfo, val rotation: Rotation)