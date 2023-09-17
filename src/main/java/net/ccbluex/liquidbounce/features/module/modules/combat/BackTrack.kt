//from kevin client
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.NotifyType
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.ccbluex.liquidbounce.utils.Rotation
import net.ccbluex.liquidbounce.utils.extensions.*
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.Packet
import net.minecraft.network.ThreadQuickExitException
import net.minecraft.network.play.INetHandlerPlayClient
import net.minecraft.network.play.INetHandlerPlayServer
import net.minecraft.network.play.client.C02PacketUseEntity
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minecraft.network.play.server.S14PacketEntity
import net.minecraft.network.play.server.S19PacketEntityStatus
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.Vec3
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.ceil


@ModuleInfo(name = "BackTrack", description = "Lets you attack people in their previous locations.", category = ModuleCategory.COMBAT)
class BackTrack : Module() {

    private val minDistance: FloatValue = object : FloatValue("MinDistance", 2.9f, 1f, 4f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            if (newValue > maxStartDistance.get()) set(maxStartDistance.get())
        }
    }
    private val maxStartDistance : FloatValue = object : FloatValue("MaxStartDistance", 3.2f, 2f, 4f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            if (newValue < minDistance.get()) set(minDistance.get())
            else if (newValue > maxDistance.get()) set(maxDistance.get())
        }
    }
    private val maxDistance: FloatValue = object : FloatValue("MaxActiveDistance", 5f, 2f, 6f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            if (newValue < maxStartDistance.get()) set(maxStartDistance.get())
        }
    }
    private val minTime : IntegerValue = object : IntegerValue("MinTime", 100, 0, 500) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            if (newValue > maxTime.get()) set(maxTime.get())
        }
    }
    private val maxTime : IntegerValue = object : IntegerValue("MaxTime", 200, 0, 1000) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            if (newValue < minTime.get()) set(minTime.get())
        }
    }
    private val smartPacket = BoolValue("Smart", true)
    private val maxHurtTime = IntegerValue("MaxHurtTime", 6, 0, 10)
    private val hurtTimeWithPing = BoolValue("CalculateHurtTimeWithPing", true)
    private val minAttackReleaseRange = FloatValue("MinAttackReleaseRange", 3.2F, 2f, 6f)

    private val onlyKillAura = BoolValue("OnlyKillAura", true)
    private val onlyPlayer = BoolValue("OnlyPlayer", true)
    private val resetOnVelocity = BoolValue("ResetOnVelocity", true)
    private val resetOnLagging = BoolValue("ResetOnLagging", true)
    private val rangeCheckMode = ListValue("RangeCheckMode", arrayOf("RayCast", "DirectDistance"), "DirectDistance")

    private val reverse = BoolValue("Reverse", false)
    private val reverseRange = FloatValue("ReverseStartRange", 2.9f, 1f, 6f)

    private val reverseMaxRange = FloatValue("ReverseMaxRange", 3.0f, 1f, 6f)
    private val reverseSelfMaxHurtTime = IntegerValue("ReverseSelfMaxHurtTime", 1, 0, 10)
    private val reverseTargetMaxHurtTime = IntegerValue("ReverseTargetMaxHurtTime", 10, 0, 10)
    private val maxReverseTime = IntegerValue("MaxReverseTime", 100, 1, 500)

    private val espMode = ListValue("ESPMode", arrayOf("FullBox", "OutlineBox", "NormalBox", "OtherOutlineBox", "OtherFullBox", "Model", "None"), "Box")

    private val storageSendPackets = ArrayList<Packet<INetHandlerPlayServer>>()
    private val storagePackets = ArrayList<Packet<INetHandlerPlayClient>>()
    private val storageEntities = ArrayList<Entity>()

    private val killAura = LiquidBounce.moduleManager.getModule(KillAura::class.java)
    private var timer = MSTimer()
    private val reverseTimer = MSTimer()
    private var hasAttackInReversing = false
    private var lastPosition = Vec3(0.0, 0.0, 0.0)
    private var attacked : Entity? = null

    var needFreeze = false
    var reversing = false

    //    @EventTarget
    // for safety, see in met.minecraft.network.NetworkManager
    fun onPacket(event: PacketEvent) {
        mc.thePlayer ?: return
        val packet = event.packet
        val theWorld = mc.theWorld!!
        if (packet.javaClass.name.contains("net.minecraft.network.play.server.", true)) {
            if (packet is S14PacketEntity) {
                val entity = packet.getEntity(theWorld) ?: return
                if (entity !is EntityLivingBase) return
                if (onlyPlayer.get() && entity !is EntityPlayer) return
                entity.serverPosX += packet.func_149062_c().toInt()
                entity.serverPosY += packet.func_149061_d().toInt()
                entity.serverPosZ += packet.func_149064_e().toInt()
                val x = entity.serverPosX.toDouble() / 32.0
                val y = entity.serverPosY.toDouble() / 32.0
                val z = entity.serverPosZ.toDouble() / 32.0
                if ((!onlyKillAura.get() || killAura!!.state || needFreeze) && EntityUtils.isSelected(entity, true)) {
                    val afterBB = AxisAlignedBB(x - 0.4F, y - 0.1F, z - 0.4F, x + 0.4F, y + 1.9F, z + 0.4F)
                    var afterRange: Double
                    var beforeRange: Double
                    if (rangeCheckMode.get() == "RayCast") {
                        afterRange = afterBB.getLookingTargetRange(mc.thePlayer!!)
                        beforeRange = entity.getLookDistanceToEntityBox()
                        if (afterRange == Double.MAX_VALUE) {
                            val eyes = mc.thePlayer!!.getPositionEyes(1F)
                            afterRange = getNearestPointBB(eyes, afterBB).distanceTo(eyes) + 0.075
                        }
                        if (beforeRange == Double.MAX_VALUE) beforeRange =
                            mc.thePlayer!!.getDistanceToEntityBox(entity) + 0.075
                    } else {
                        val eyes = mc.thePlayer!!.getPositionEyes(1F)
                        afterRange = getNearestPointBB(eyes, afterBB).distanceTo(eyes)
                        beforeRange = mc.thePlayer!!.getDistanceToEntityBox(entity)
                    }

                    if (beforeRange <= maxStartDistance.get()) {
                        if (afterRange in minDistance.get()..maxDistance.get() && (!smartPacket.get() || afterRange > beforeRange + 0.02) && entity.hurtTime <= calculatedMaxHurtTime) {
                            if (!needFreeze) {
                                timer.reset()
                                needFreeze = true
                                stopReverse()
                            }
                            if (!storageEntities.contains(entity)) storageEntities.add(entity)
                            event.cancelEvent()
                            return
                        }
                    } else {
                        if (smartPacket.get()) {
                            if (afterRange < beforeRange) {
                                if (needFreeze) releasePackets()
                            }
                        }
                    }
                }
                if (needFreeze) {
                    if (!storageEntities.contains(entity)) storageEntities.add(entity)
                    event.cancelEvent()
                    return
                }
                if (!event.isCancelled && !needFreeze) {
                    LiquidBounce.eventManager.callEvent(EntityMovementEvent(entity))
                    val f =
                        if (packet.func_149060_h()) (packet.func_149066_f() * 360).toFloat() / 256.0f else entity.rotationYaw
                    val f1 =
                        if (packet.func_149060_h()) (packet.func_149063_g() * 360).toFloat() / 256.0f else entity.rotationPitch
                    entity.setPositionAndRotation2(x, y, z, f, f1, 3, false)
                    entity.onGround = packet.onGround
                }
                event.cancelEvent()
                //                storageEntities.add(entity)
            } else {
                if ((packet is S12PacketEntityVelocity && resetOnVelocity.get()) || (packet is S08PacketPlayerPosLook && resetOnLagging.get())) {
                    storagePackets.add(packet as Packet<INetHandlerPlayClient>)
                    event.cancelEvent()
                    releasePackets()
                    return
                }
                if (needFreeze && !event.isCancelled) {
                    if (packet is S19PacketEntityStatus) {
                        if (packet.opCode == 2.toByte()) return
                    }
                    storagePackets.add(packet as Packet<INetHandlerPlayClient>)
                    event.cancelEvent()
                }
            }
        } else {
            if (reversing) {
                event.cancelEvent()
                storageSendPackets.add(packet as Packet<INetHandlerPlayServer>)
                if (reverseTimer.hasTimePassed(maxReverseTime.get().toLong())) {
                    stopReverse()
                }
            }
            if (packet is C02PacketUseEntity) {
                if (packet.action == C02PacketUseEntity.Action.ATTACK) {
                    if (needFreeze) attacked = packet.getEntityFromWorld(theWorld)
                    if (reversing) hasAttackInReversing = true
                }
            } else if (packet is C03PacketPlayer) {
                if (!needFreeze && reverse.get()) {
                    val vec = Vec3(packet.x, packet.y, packet.z)
                    LiquidBounce.combatManager.target?.let {
                        val loc = Vec3(it.posX, it.posY + it.eyeHeight, it.posZ)
                        val bp = getNearestPointBB(loc, mc.thePlayer.entityBoundingBox.expand(0.1, 0.1, 0.1))
                        val distance = loc.distanceTo(bp)
                        if (reversing) {
                            val lastBB = AxisAlignedBB(
                                lastPosition.xCoord - 0.4f,
                                lastPosition.yCoord - 0.1f,
                                lastPosition.zCoord - 0.4f,
                                lastPosition.xCoord + 0.4f,
                                lastPosition.yCoord + 1.9f,
                                lastPosition.zCoord + 0.4f
                            )
                            val d = getNearestPointBB(loc, lastBB).distanceTo(loc)
                            if (distance > d || distance > reverseMaxRange.get() || (it.hurtTime <= (1 + (mc.thePlayer.getPing() / 50.0).toInt()) && hasAttackInReversing)) {
                                stopReverse()
                            } else {
                                val rot =
                                    Rotation(it.rotationYaw, it.rotationPitch).toDirection().multiply(4.0).add(loc)
                                val movingObjectPosition = lastBB.calculateIntercept(loc, rot) ?: return@let
                                val m2 =
                                    mc.thePlayer.entityBoundingBox.expand(0.1, 0.1, 0.1).calculateIntercept(loc, rot)
                                if (movingObjectPosition.hitVec != null) {
                                    val d2 = movingObjectPosition.hitVec.distanceTo(loc)
                                    if (d2 <= 3.0 && (m2?.hitVec == null || m2.hitVec.distanceTo(loc) > d2)) stopReverse()
                                }
                            }
                        } else if (distance <= reverseRange.get() &&
                            it.hurtTime <= reverseTargetMaxHurtTime.get() && mc.thePlayer.hurtTime <= reverseSelfMaxHurtTime.get() &&
                            loc.distanceTo(bp) >= distance && (!onlyKillAura.get() || killAura!!.state)
                        ) {
                            reversing = true
                            lastPosition = vec
                            hasAttackInReversing = false
                            reverseTimer.reset()
                        }
                    }
                }
            }
        }
    }

    @EventTarget fun onMotion(event: MotionEvent) {
        if (event.eventState == EventState.PRE) return
        if (needFreeze) {
            if (timer.hasTimePassed(maxTime.get().toLong())) {
                releasePackets()
                return
            }
            if (storageEntities.isNotEmpty()) {
                var release = false // for-each
                for (entity in storageEntities) {
                    val x = entity.serverPosX.toDouble() / 32.0
                    val y = entity.serverPosY.toDouble() / 32.0
                    val z = entity.serverPosZ.toDouble() / 32.0
                    val entityBB = AxisAlignedBB(x - 0.4F, y -0.1F, z - 0.4F, x + 0.4F, y + 1.9F, z + 0.4F)
                    var range = entityBB.getLookingTargetRange(mc.thePlayer!!)
                    if (range == Double.MAX_VALUE) {
                        val eyes = mc.thePlayer!!.getPositionEyes(1F)
                        range = getNearestPointBB(eyes, entityBB).distanceTo(eyes) + 0.075
                    }
                    if (range <= minDistance.get()) {
                        release = true
                        break
                    }
                    val entity1 = attacked
                    if (entity1 != entity) continue
                    if (timer.hasTimePassed(minTime.get().toLong())) {
                        if (range >= minAttackReleaseRange.get()) {
                            release = true
                            break
                        }
                    }
                }
                if (release) releasePackets()
            }
        }
    }

    @EventTarget fun onWorld(event: WorldEvent) {
        attacked = null
        storageEntities.clear()
        if (event.worldClient == null) storagePackets.clear()
    }

    @EventTarget fun onRender3D(event: Render3DEvent) {

        if (reversing) {
            val renderManager = mc.renderManager

            val vec = lastPosition.addVector(-renderManager.renderPosX, -renderManager.renderPosY, -renderManager.renderPosZ)
            RenderUtils.drawAxisAlignedBB(AxisAlignedBB(vec.xCoord - 0.4, vec.yCoord + 0.2, vec.zCoord - 0.4, vec.xCoord + 0.4, vec.yCoord, vec.zCoord + 0.4), Color(37, 126, 255, 70))
        }

        if (espMode.get() == "None" || !needFreeze) return

        if (espMode.get() ==  "Model") {
            GL11.glPushMatrix()
            GL11.glDisable(GL11.GL_TEXTURE_2D)
            GL11.glDisable(GL11.GL_DEPTH_TEST)
            GlStateManager.disableAlpha()
            for (entity in storageEntities) {
                if (entity !is EntityOtherPlayerMP) return
                val mp = EntityOtherPlayerMP(mc.theWorld, entity.gameProfile)
                mp.posX = entity.serverPosX / 32.0
                mp.posY = entity.serverPosY / 32.0
                mp.posZ = entity.serverPosZ / 32.0
                mp.prevPosX = mp.posX
                mp.prevPosY = mp.posY
                mp.prevPosZ = mp.posZ
                mp.lastTickPosX = mp.posX
                mp.lastTickPosY = mp.posY
                mp.lastTickPosZ = mp.posZ
                mp.rotationYaw = entity.rotationYaw
                mp.rotationPitch = entity.rotationPitch
                mp.rotationYawHead = entity.rotationYawHead
                mp.prevRotationYaw = mp.rotationYaw
                mp.prevRotationPitch = mp.rotationPitch
                mp.prevRotationYawHead = mp.rotationYawHead
                mp.isInvisible = false
                mp.swingProgress = entity.swingProgress
                mp.swingProgressInt = entity.swingProgressInt
                mp.hurtTime = entity.hurtTime
                mp.hurtResistantTime = entity.hurtResistantTime
                mc.renderManager.renderEntitySimple(mp, event.partialTicks)
            }
            GlStateManager.enableAlpha()
            GL11.glEnable(GL11.GL_TEXTURE_2D)
            GL11.glEnable(GL11.GL_DEPTH_TEST)
            GlStateManager.resetColor()
            GL11.glPopMatrix()
            return
        }

        var outline = false
        var filled = false
        var other = false
        when (espMode.get()) {
            "NormalBox" -> {
                outline = true
                filled = true
            }
            "FullBox" -> {
                filled = true
            }
            "OtherOutlineBox" -> {
                other = true
                outline = true
            }
            "OtherFullBox" -> {
                other = true
                filled = true
            }
            else -> {
                outline = true
            }
        }

        // pre draw
        GL11.glPushMatrix()
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glDisable(GL11.GL_DEPTH_TEST)

        GL11.glDepthMask(false)

        if (outline) {
            GL11.glLineWidth(1f)
            GL11.glEnable(GL11.GL_LINE_SMOOTH)
        }
        // drawing
        val renderManager = mc.renderManager
        for (entity in storageEntities) {
            val x = entity.serverPosX.toDouble() / 32.0 - renderManager.renderPosX
            val y = entity.serverPosY.toDouble() / 32.0 - renderManager.renderPosY
            val z = entity.serverPosZ.toDouble() / 32.0 - renderManager.renderPosZ
            if (other) {
                if (outline) {
                    RenderUtils.glColor(32, 200, 32, 255)
                    RenderUtils.otherDrawOutlinedBoundingBox(entity.rotationYawHead, x, y, z, entity.width / 2.0 + 0.1, entity.height + 0.1)
                }
                if (filled) {
                    RenderUtils.glColor(32, 255, 32, 35)
                    RenderUtils.otherDrawBoundingBox(entity.rotationYawHead, x, y, z, entity.width / 2.0 + 0.1, entity.height + 0.1)
                }
            } else {
                val bb = AxisAlignedBB(x - 0.4F, y, z - 0.4F, x + 0.4F, y + 1.9F, z + 0.4F)
                if (outline) {
                    RenderUtils.glColor(32, 200, 32, 255)
                    RenderUtils.drawSelectionBoundingBox(bb)
                }
                if (filled) {
                    RenderUtils.glColor(32, 255, 32, if (outline) 26 else 35)
                    RenderUtils.drawFilledBox(bb)
                }
            }
        }

        // post draw
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
        GL11.glDepthMask(true)
        if (outline) {
            GL11.glDisable(GL11.GL_LINE_SMOOTH)
        }
        GL11.glDisable(GL11.GL_BLEND)
        GL11.glEnable(GL11.GL_TEXTURE_2D)
        GL11.glEnable(GL11.GL_DEPTH_TEST)
        GL11.glPopMatrix()
    }

    fun releasePackets() {
        attacked = null
        val netHandler: INetHandlerPlayClient = mc.netHandler
        if (storagePackets.isEmpty()) return
        while (storagePackets.isNotEmpty()) {
            storagePackets.removeAt(0).let{
                try {
                    val packetEvent = PacketEvent(it)
                    if (!PacketUtils.packets.contains(it)) LiquidBounce.eventManager.callEvent(packetEvent)
                    if (!packetEvent.isCancelled) it.processPacket(netHandler)
                } catch (_: ThreadQuickExitException) { }
            }
        }
        while (storageEntities.isNotEmpty()) {
            storageEntities.removeAt(0).let { entity ->
                if (!entity.isDead) {
                    val x = entity.serverPosX.toDouble() / 32.0
                    val y = entity.serverPosY.toDouble() / 32.0
                    val z = entity.serverPosZ.toDouble() / 32.0
                    entity.setPosition(x, y, z)
                }
            }
        }
        needFreeze = false
    }

    fun stopReverse() {
        if (storageSendPackets.isEmpty()) return
        while (storageSendPackets.isNotEmpty()) {
            storageSendPackets.removeAt(0).let {
                try {
                    val packetEvent = PacketEvent(it)
                    if (!PacketUtils.packets.contains(it)) LiquidBounce.eventManager.callEvent(packetEvent)
                    if (!packetEvent.isCancelled) PacketUtils.sendPacketNoEvent(it)
                } catch (e: Exception) {
                    LiquidBounce.hud.addNotification(Notification( "BackTrack","Something went wrong when sending packet reversing",NotifyType.ERROR))
                }
                // why kotlin
                return@let
            }
        }
        if (storageSendPackets.isEmpty()) {
            reversing = false
            hasAttackInReversing = false
        }
    }

    private val calculatedMaxHurtTime : Int
        get() = maxHurtTime.get() + if (hurtTimeWithPing.get()) ceil(mc.thePlayer.getPing() / 50.0).toInt() else 0

    fun update() {}

    //init {
      //  INetworkManager.backTrack = this
    //}
//    val target: EntityLivingBase?
//    get() = if (onlyKillAura.get()) {
//        if (killAura.target is EntityLivingBase) killAura.target as EntityLivingBase?
//        else null
//    } else currentTarget
}

//data class DataEntityPosStorage(val entity: EntityLivingBase, var modifiedTick: Int = 0)