/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.utils.extensions

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.ccbluex.liquidbounce.utils.Rotation
import net.ccbluex.liquidbounce.utils.render.RenderUtils.stripColor
import net.minecraft.entity.Entity
import net.minecraft.entity.boss.EntityDragon
import net.minecraft.entity.monster.EntityGhast
import net.minecraft.entity.monster.EntityGolem
import net.minecraft.entity.monster.EntityMob
import net.minecraft.entity.monster.EntitySlime
import net.minecraft.entity.passive.EntityAnimal
import net.minecraft.entity.passive.EntityBat
import net.minecraft.entity.passive.EntitySquid
import net.minecraft.entity.passive.EntityVillager
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.MovingObjectPosition
import net.minecraft.util.Vec3
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt


/**
 * Allows to get the distance between the current entity and [entity] from the nearest corner of the bounding box
 */
fun Entity.getDistanceToEntityBox(entity: Entity): Double {
    val eyes = this.getPositionEyes(1F)
    val pos = getNearestPointBB(eyes, entity.entityBoundingBox)
    val xDist = abs(pos.xCoord - eyes.xCoord)
    val yDist = abs(pos.yCoord - eyes.yCoord)
    val zDist = abs(pos.zCoord - eyes.zCoord)
    return sqrt(xDist.pow(2) + yDist.pow(2) + zDist.pow(2))
}

fun getNearestPointBB(eye: Vec3, box: AxisAlignedBB): Vec3 {
    val origin = doubleArrayOf(eye.xCoord, eye.yCoord, eye.zCoord)
    val destMins = doubleArrayOf(box.minX, box.minY, box.minZ)
    val destMaxs = doubleArrayOf(box.maxX, box.maxY, box.maxZ)
    for (i in 0..2) {
        if (origin[i] > destMaxs[i]) origin[i] = destMaxs[i] else if (origin[i] < destMins[i]) origin[i] = destMins[i]
    }
    return Vec3(origin[0], origin[1], origin[2])
}

fun EntityPlayer.getPing(): Int {
    val playerInfo = MinecraftInstance.mc.netHandler.getPlayerInfo(uniqueID)
    return playerInfo?.responseTime ?: 0
}

fun Entity.isAnimal(): Boolean {
    return this is EntityAnimal ||
            this is EntitySquid ||
            this is EntityGolem ||
            this is EntityBat
}

fun Entity.isMob(): Boolean {
    return this is EntityMob ||
            this is EntityVillager ||
            this is EntitySlime
            || this is EntityGhast ||
            this is EntityDragon
}

fun EntityPlayer.isClientFriend(): Boolean {
    val entityName = name ?: return false

    return LiquidBounce.fileManager.friendsConfig.isFriend(stripColor(entityName))
}
fun EntityPlayer.customRayTrace(blockReachDistance: Double, partialTicks: Float, yaw: Float, pitch: Float): MovingObjectPosition? {
    val vec3: Vec3 = this.getPositionEyes(partialTicks)
    val vec4: Vec3? = this.customGetLook(partialTicks, yaw, pitch)
    val vec5 = vec3.addVector(
        vec4!!.xCoord * blockReachDistance,
        vec4.yCoord * blockReachDistance,
        vec4.zCoord * blockReachDistance
    )
    return this.worldObj.rayTraceBlocks(vec3, vec5, false, false, true)
}
private fun EntityPlayer.customGetLook(partialTicks: Float, yaw: Float, pitch: Float): Vec3? {
    if (partialTicks == 1.0f || partialTicks == 2.0f) {
        return this.getVectorForRotation(pitch, yaw)
    }
    val f: Float = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * partialTicks
    val f2: Float = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * partialTicks
    return this.getVectorForRotation(f, f2)
}

val Entity.hitBox: AxisAlignedBB
    get() {
        val borderSize = collisionBorderSize.toDouble()
        return entityBoundingBox.expand(borderSize, borderSize, borderSize)
    }

val Entity.rotation: Rotation
    get() = Rotation(rotationYaw, rotationPitch)
