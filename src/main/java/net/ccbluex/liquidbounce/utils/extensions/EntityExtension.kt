package net.ccbluex.liquidbounce.utils.extensions

import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.ccbluex.liquidbounce.utils.Rotation
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.resources.DefaultPlayerSkin
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.MathHelper
import net.minecraft.util.MovingObjectPosition
import net.minecraft.util.ResourceLocation
import net.minecraft.util.Vec3


fun EntityPlayer.getEyeVec3(): Vec3 {
    return Vec3(this.posX, this.entityBoundingBox.minY + this.getEyeHeight(), this.posZ)
}

val EntityLivingBase.renderHurtTime: Float
    get() = this.hurtTime - if(this.hurtTime!=0) { Minecraft.getMinecraft().timer.renderPartialTicks } else { 0f }

val EntityLivingBase.hurtPercent: Float
    get() = (this.renderHurtTime)/10

val EntityLivingBase.ping: Int
    get() = if (this is EntityPlayer) { Minecraft.getMinecraft().netHandler.getPlayerInfo(this.uniqueID)?.responseTime?.coerceAtLeast(0) } else { null } ?: -1

val EntityLivingBase.skin: ResourceLocation // TODO: add special skin for mobs
    get() = if (this is EntityPlayer) { Minecraft.getMinecraft().netHandler.getPlayerInfo(this.uniqueID)?.locationSkin } else { null } ?: DefaultPlayerSkin.getDefaultSkinLegacy()

val Entity.eyes: Vec3
    get() = getPositionEyes(1f)

fun Entity.getVectorForRotation(pitch: Float, yaw: Float): Vec3 {
    val f = MathHelper.cos(-yaw * 0.017453292f - 3.1415927f)
    val f2 = MathHelper.sin(-yaw * 0.017453292f - 3.1415927f)
    val f3 = -MathHelper.cos(-pitch * 0.017453292f)
    val f4 = MathHelper.sin(-pitch * 0.017453292f)
    return Vec3((f2 * f3).toDouble(), f4.toDouble(), (f * f3).toDouble())
}
fun Entity.rayTraceCustom(blockReachDistance: Double, yaw: Float, pitch: Float): MovingObjectPosition? {
    val vec3 = MinecraftInstance.mc.thePlayer.getPositionEyes(1.0f)
    val vec31 = MinecraftInstance.mc.thePlayer.getVectorForRotation(yaw, pitch)
    val vec32 = vec3.addVector(
        vec31.xCoord * blockReachDistance,
        vec31.yCoord * blockReachDistance,
        vec31.zCoord * blockReachDistance
    )
    return MinecraftInstance.mc.theWorld.rayTraceBlocks(vec3, vec32, false, false, true)
}
fun Entity.getLookCustom(yaw: Float, pitch: Float): Vec3 {
    return this.getVectorForRotation(pitch, yaw)
}
fun Entity.getLookDistanceToEntityBox(entity: Entity=this, rotation: Rotation? = null, range: Double=10.0): Double {
    val eyes = this.getPositionEyes(1F)
    val end = (rotation?: RotationUtils.targetRotation).toDirection().multiply(range).add(eyes)
    return entity.entityBoundingBox.calculateIntercept(eyes, end)?.hitVec?.distanceTo(eyes) ?: Double.MAX_VALUE
}