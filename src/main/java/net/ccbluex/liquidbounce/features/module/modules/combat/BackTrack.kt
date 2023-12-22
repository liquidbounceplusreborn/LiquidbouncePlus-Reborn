//from kevin client
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Type
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.ccbluex.liquidbounce.utils.PacketUtils.packets
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
import net.minecraft.network.play.server.*
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.Vec3
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import java.util.*
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max


@ModuleInfo(name = "BackTrack", description = "Lets you attack people in their previous locations.", category = ModuleCategory.COMBAT)
class BackTrack : Module() {

    private val mode = ListValue("Mode", arrayOf("Legacy", "Smooth"), "Legacy")
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
    private val setPosOnStop = BoolValue("SetPositionOnStop", false)
    private val rangeCheckMode = ListValue("RangeCheckMode", arrayOf("RayCast", "DirectDistance"), "DirectDistance")

    private val reverse = BoolValue("Reverse", false)
    private val reverseRange : FloatValue = object : FloatValue("ReverseStartRange", 5f, 1f, 6f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            //newValue.coerceAtMost(reverseMaxRange.get())
            if (newValue > reverseMaxRange.get()) set(reverseMaxRange.get())
        }
    }

    private val reverseMaxRange : FloatValue = object : FloatValue("ReverseMaxRange", 6f, 1f, 6f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            //(newValue.coerceAtLeast(reverseRange.get()))
                    if (newValue < reverseRange.get()) set(reverseRange.get())
        }
    }
    private val reverseSelfMaxHurtTime = IntegerValue("ReverseSelfMaxHurtTime", 1, 0, 10)
    private val reverseTargetMaxHurtTime = IntegerValue("ReverseTargetMaxHurtTime", 10, 0, 10)
    private val maxReverseTime = IntegerValue("MaxReverseTime", 100, 1, 500)

    private val espMode = ListValue("ESPMode", arrayOf("FullBox", "OutlineBox", "NormalBox", "OtherOutlineBox", "OtherFullBox", "Model", "None"), "Box")

    private val espRed = IntegerValue("EspRed", 32, 0,255)
    private val espGreen = IntegerValue("EspGreen", 255, 0,255)
    private val espBlue = IntegerValue("EspBlue", 32, 0,255)
    private val espAlpha = IntegerValue("EspAlpha", 35, 0,255)
    private val outlineRed = IntegerValue("OutlineRed", 32, 0,255)
    private val outlineGreen = IntegerValue("OutlineGreen", 200, 0,255)
    private val outlineBlue = IntegerValue("OutlineBlue", 32, 0,255)
    private val outlineAlpha = IntegerValue("OutlineAlpha", 255, 0,255)
    private val alwaysRenderESP = BoolValue("AlwaysRenderESP", false)

    private val storagePackets = ArrayList<ServerPacketStorage>()
    private val storageSendPackets = ArrayList<Packet<INetHandlerPlayServer>>()
    private val storageEntities = ArrayList<Entity>()

    private val storageEntityMove = LinkedList<EntityPacketLoc>()

    private val killAura: KillAura? = LiquidBounce.moduleManager.getModule(KillAura::class.java)
    //    private var currentTarget : EntityLivingBase? = null
    private var timer = MSTimer()
    private val reverseTimer = MSTimer()
    private var hasAttackInReversing = false
    private var lastPosition = Vec3(0.0, 0.0, 0.0)
    private var attacked : Entity? = null

    private var smoothPointer = System.nanoTime()
    var needFreeze = false
    var reversing = false

    //    @EventTarget
    // for safety, see in met.minecraft.network.NetworkManager
    fun onPacket(event: PacketEvent) {
        mc.thePlayer ?: return
        val packet = event.packet
        val theWorld = mc.theWorld!!
        if (packet.javaClass.name.contains("net.minecraft.network.play.server.", true)) {
            val storage = ServerPacketStorage(packet as Packet<INetHandlerPlayClient>)
            if (packet is S14PacketEntity) {
                val entity = packet.getEntity(theWorld)?: return
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
                    if (rangeCheckMode .get() ==  "RayCast") {
                        afterRange = afterBB.getLookingTargetRange(mc.thePlayer!!)
                        beforeRange = mc.thePlayer.getLookDistanceToEntityBox(entity)
                        if (afterRange == Double.MAX_VALUE) {
                            val eyes = mc.thePlayer!!.getPositionEyes(1F)
                            afterRange = getNearestPointBB(eyes, afterBB).distanceTo(eyes) + 0.075
                        }
                        if (beforeRange == Double.MAX_VALUE) beforeRange = mc.thePlayer!!.getDistanceToEntityBox(entity) + 0.075
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
                                smoothPointer = System.nanoTime()
                                stopReverse()
                            }
                            if (!storageEntities.contains(entity)) storageEntities.add(entity)
                            event.cancelEvent()
                            if (mode.get() == "Smooth") {
                                storageEntityMove.add(EntityPacketLoc(entity, x, y, z))
                            }
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
                    if (mode.get() == "Smooth") {
                        storageEntityMove.add(EntityPacketLoc(entity, x, y, z))
                    }
                    event.cancelEvent()
                    return
                }
                if (!event.isCancelled && !needFreeze) {
                    LiquidBounce.eventManager.callEvent(EntityMovementEvent(entity))
                    val f = if (packet.func_149060_h()) (packet.func_149066_f() * 360).toFloat() / 256.0f else entity.rotationYaw
                    val f1 = if (packet.func_149060_h()) (packet.func_149063_g() * 360).toFloat() / 256.0f else entity.rotationPitch
                    entity.setPositionAndRotation2(x, y, z, f, f1, 3, false)
                    entity.onGround = packet.onGround
                }
                event.cancelEvent()
                //                storageEntities.add(entity)
            } else if (packet is S18PacketEntityTeleport) {
                val entity = theWorld.getEntityByID(packet.entityId)
                if (entity !is EntityLivingBase) return
                if (onlyPlayer.get() && entity !is EntityPlayer) return
                entity.serverPosX = packet.x
                entity.serverPosY = packet.y
                entity.serverPosZ = packet.z
                val d0 = entity.serverPosX.toDouble() / 32.0
                val d1 = entity.serverPosY.toDouble() / 32.0
                val d2 = entity.serverPosZ.toDouble() / 32.0
                val f: Float = (packet.yaw * 360).toFloat() / 256.0f
                val f1: Float = (packet.pitch * 360).toFloat() / 256.0f
                if (!needFreeze) {
                    if (!(abs(entity.posX - d0) >= 0.03125) && !(abs(entity.posY - d1) >= 0.015625) && !(abs(entity.posZ - d2) >= 0.03125)) {
                        entity.setPositionAndRotation2(entity.posX, entity.posY, entity.posZ, f, f1, 3, true)
                    } else {
                        entity.setPositionAndRotation2(d0, d1, d2, f, f1, 3, true)
                    }
                    entity.onGround = packet.onGround
                } else storageEntityMove.add(EntityPacketLoc(entity, d0, d1, d2))
                event.cancelEvent()
            } else {
                if ((packet is S12PacketEntityVelocity && resetOnVelocity.get()) || (packet is S08PacketPlayerPosLook && resetOnLagging.get())) {
                    storagePackets.add(storage)
                    event.cancelEvent()
                    releasePackets()
                    return
                }
                if (needFreeze && !event.isCancelled) {
                    if (packet is S19PacketEntityStatus) {
                        if (packet.opCode == 2.toByte()) return
                    }
                    storagePackets.add(storage)
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
                                val rot = (if (it is EntityOtherPlayerMP) Rotation(it.otherPlayerMPYaw.toFloat(), it.otherPlayerMPPitch.toFloat()) else Rotation(it.rotationYaw, it.rotationPitch)).toDirection().multiply(4.0).add(loc)
                                val movingObjectPosition = lastBB.calculateIntercept(loc, rot) ?: return@let
                                val m2 = mc.thePlayer.entityBoundingBox.expand(0.11, 0.1, 0.11).calculateIntercept(loc, rot)
                                if (movingObjectPosition.hitVec != null) {
                                    val d2 = movingObjectPosition.hitVec.distanceTo(loc)
                                    if (d2 <= 3.0 && (m2?.hitVec == null || m2.hitVec.distanceTo(loc) > d2)) stopReverse()
                                }
                            }
                        } else if (distance <= reverseRange.get() &&
                            it.hurtTime <= reverseTargetMaxHurtTime.get() && mc.thePlayer.hurtTime <= reverseSelfMaxHurtTime.get() &&
                            loc.distanceTo(bp) >= distance && (!onlyKillAura.get() || killAura!!.state)) {
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
            if (mode.get() == "Legacy") {
                if (timer.hasTimePassed(maxTime.get().toLong())) {
                    releasePackets()
                    return
                }
            } else {
                doSmoothRelease()
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

        if (espMode .get() ==  "None" || !needFreeze) return

        val entitiesToRender = if (alwaysRenderESP.get()) mc.theWorld.loadedEntityList else storageEntities
        if (espMode .get() ==  "Model") {
            glPushMatrix()
            glEnable(GL_TEXTURE_2D)
            for (entity in entitiesToRender) {
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
            glDisable(GL_TEXTURE_2D)
            GlStateManager.resetColor()
            glMatrixMode(GL_MODELVIEW)
            glPopMatrix()
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
        glPushMatrix()
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glDisable(GL_DEPTH_TEST)

        glDepthMask(false)

        if (outline) {
            glLineWidth(1f)
            glEnable(GL_LINE_SMOOTH)
        }
        // drawing
        val renderManager = mc.renderManager
        for (entity in entitiesToRender) {
            val x = entity.serverPosX.toDouble() / 32.0 - renderManager.renderPosX
            val y = entity.serverPosY.toDouble() / 32.0 - renderManager.renderPosY
            val z = entity.serverPosZ.toDouble() / 32.0 - renderManager.renderPosZ
            if (other) {
                if (outline) {
                    RenderUtils.glColor(outlineRed.get(), outlineGreen.get(), outlineBlue.get(), outlineAlpha.get())
                    RenderUtils.otherDrawOutlinedBoundingBox(entity.rotationYawHead, x, y, z, entity.width / 2.0 + 0.1, entity.height + 0.1)
                }
                if (filled) {
                    RenderUtils.glColor(espRed.get(), espGreen.get(), espBlue.get(), espAlpha.get())
                    RenderUtils.otherDrawBoundingBox(entity.rotationYawHead, x, y, z, entity.width / 2.0 + 0.1, entity.height + 0.1)
                }
            } else {
                val bb = AxisAlignedBB(x - 0.4F, y, z - 0.4F, x + 0.4F, y + 1.9F, z + 0.4F)
                if (outline) {
                    RenderUtils.glColor(outlineRed.get(), outlineGreen.get(), outlineBlue.get(), outlineAlpha.get())
                    RenderUtils.drawSelectionBoundingBox(bb)
                }
                if (filled) {
                    RenderUtils.glColor(espRed.get(), espGreen.get(), espBlue.get(), espAlpha.get())
                    RenderUtils.drawFilledBox(bb)
                }
            }
        }

        // post draw
        glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
        glDepthMask(true)
        if (outline) {
            glDisable(GL_LINE_SMOOTH)
        }
        glDisable(GL_BLEND)
        glEnable(GL_TEXTURE_2D)
        glEnable(GL_DEPTH_TEST)
        glPopMatrix()
    }

    private fun releasePackets() {
        attacked = null
        smoothPointer = System.nanoTime()
        val netHandler: INetHandlerPlayClient = mc.netHandler
        if (storagePackets.isEmpty()) return
        while (storagePackets.isNotEmpty()) {
            storagePackets.removeAt(0).let{
                val packet = it.packet
                try {
                    val packetEvent = PacketEvent(packet)
                    if (!packets.contains(packet)) LiquidBounce.eventManager.callEvent(packetEvent)
                    if (!packetEvent.isCancelled) packet.processPacket(netHandler)
                } catch (_: ThreadQuickExitException) { }
            }
        }
        while (storageEntities.isNotEmpty()) {
            storageEntities.removeAt(0).let { entity ->
                if (!entity.isDead) {
                    val x = entity.serverPosX.toDouble() / 32.0
                    val y = entity.serverPosY.toDouble() / 32.0
                    val z = entity.serverPosZ.toDouble() / 32.0
                    if (setPosOnStop.get()) entity.setPosition(x, y, z)
                }
            }
        }
        storageEntityMove.clear()
        needFreeze = false
    }

    private fun releasePacket(untilNS: Long) {
        val netHandler: INetHandlerPlayClient = mc.netHandler
        if (storagePackets.isEmpty()) return
        smoothPointer = untilNS
        while (storagePackets.isNotEmpty()) {
            val it = storagePackets[0]
            val packet = it.packet
            if (it.time <= untilNS) {
                storagePackets.remove(it)
                try {
                    val packetEvent = PacketEvent(packet)
                    if (!packets.contains(packet)) LiquidBounce.eventManager.callEvent(packetEvent)
                    if (!packetEvent.isCancelled) packet.processPacket(netHandler)
                } catch (_: ThreadQuickExitException) {}
            } else {
                break
            }
        }
        while (storageEntityMove.isNotEmpty()) {
            val first = storageEntityMove.first
            if (first.time <= untilNS) {
                storageEntityMove.remove(first)
                val entity = first.entity
                if (entity is EntityOtherPlayerMP) entity.setPositionAndRotation2(first.x, first.y, first.z, entity.otherPlayerMPYaw.toFloat(), entity.otherPlayerMPPitch.toFloat(), 3, true)
                else entity.setPositionAndUpdate(first.x, first.y, first.z)
            }
            else break
        }
    }

    private fun releaseUntilBefore(ms: Int) = releasePacket(System.nanoTime() - ms * 1000000)

    private fun doSmoothRelease() {
        // what I wrote?
        val target = killAura!!.target
        var found = false
        var bestTimeStamp = max(smoothPointer, System.nanoTime() - maxTime.get() * 1000000)
        for (it in storageEntityMove) {
            if (target == it.entity) {
                found = true
                val width = it.entity.width / 2.0
                val height = it.entity.height
                val bb = AxisAlignedBB(it.x - width, it.y, it.z - width, it.x + width, it.y + height, it.z + width).expands(0.1)
                val range = mc.thePlayer.eyesLoc.distanceTo(bb)
                if (range < minDistance.get() && range < minDistance.get() ||
                    mc.thePlayer.getPositionEyes(3F).distanceTo(bb) < minDistance.get() - 0.1) {
                    bestTimeStamp = max(bestTimeStamp, it.time)
                }
            }
        }
        // simply release them all
        // TODO: Multi targets support
        if (!found) releasePackets()
        else releasePacket(bestTimeStamp)
    }

    fun stopReverse() {
        if (storageSendPackets.isEmpty()) return
        while (storageSendPackets.isNotEmpty()) {
            storageSendPackets.removeAt(0).let {
                try {
                    val packetEvent = PacketEvent(it)
                    if (!packets.contains(it)) LiquidBounce.eventManager.callEvent(packetEvent)
                    if (!packetEvent.isCancelled) PacketUtils.sendPacketNoEvent(it)
                } catch (e: Exception) {
                    LiquidBounce.hud.addNotification(
                        Notification(name,
                            "Something went wrong when sending packet reversing",
                            Type.ERROR
                        )
                    )
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

    private data class ServerPacketStorage(val packet: Packet<INetHandlerPlayClient>) {
        val time = System.nanoTime()
    }

    private data class EntityPacketLoc(val entity: Entity, val x: Double, val y: Double, val z: Double) {
        val time = System.nanoTime()
    }
}