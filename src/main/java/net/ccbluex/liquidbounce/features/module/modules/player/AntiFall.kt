/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 *
 * This code belongs to WYSI-Foundation. Please give credits when using this in your repository.
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.movement.Fly
import net.ccbluex.liquidbounce.features.module.modules.render.FreeCam
import net.ccbluex.liquidbounce.features.module.modules.world.Scaffold
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.ccbluex.liquidbounce.utils.block.BlockUtils.getBlock
import net.ccbluex.liquidbounce.utils.misc.NewFallingPlayer
import net.ccbluex.liquidbounce.utils.misc.RandomUtils
import net.ccbluex.liquidbounce.utils.timer.TimerUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.block.BlockAir
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.network.play.client.C03PacketPlayer.C06PacketPlayerPosLook
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.util.BlockPos
import org.lwjgl.opengl.GL11
import java.util.*
import kotlin.math.abs

@ModuleInfo(
    name = "AntiFall",
    spacedName = "Anti Fall",
    description = "Prevents you from falling into the void.",
    category = ModuleCategory.PLAYER
)
class AntiFall : Module() {
    private val voidDetectionAlgorithm = ListValue("Detect-Method", arrayOf("Collision", "Predict"), "Collision")
    private val setBackModeValue = ListValue(
        "SetBack-Mode",
        arrayOf(
            "Teleport",
            "FlyFlag",
            "IllegalPacket",
            "IllegalTeleport",
            "StopMotion",
            "Position",
            "Edit",
            "SpoofBack",
            "Blink",
            "HypixelTest"
        ),
        "Teleport"
    )
    private val resetMotionValue = BoolValue("ResetMotion", false) { setBackModeValue.get().lowercase(Locale.getDefault()).contains("blink") }
    private val startFallDistValue = FloatValue("BlinkStartFallDistance", 2f, 0f, 5f) { setBackModeValue.get() == "Blink" }
    private val maxFallDistSimulateValue = IntegerValue("Predict-CheckFallDistance", 255, 0, 255, "m") { voidDetectionAlgorithm.get() == "Predict" }
    private val maxFindRangeValue = IntegerValue("Predict-MaxFindRange", 60, 0, 255, "m") { voidDetectionAlgorithm.get() == "Predict" }
    private val illegalDupeValue = IntegerValue("Illegal-Dupe", 1, 1, 5, "x") { setBackModeValue.get().contains("Illegal") }
    private val setBackFallDistValue = FloatValue("Max-FallDistance", 5f, 5f, 20f, "m")
    private val resetFallDistanceValue = BoolValue("Reset-FallDistance", true)
    private val renderTraceValue = BoolValue("Render-Trace", true)
    private val scaffoldValue = BoolValue("AutoScaffold", true)
    private val noFlyValue = BoolValue("NoFly", true)
    private val noFreeCamValue = BoolValue("NoFreecam", true)

    private var detectedLocation = BlockPos.ORIGIN
    private var blink = false
    private var posX = 0.0
    private var posY = 0.0
    private var posZ = 0.0
    private var motionX = 0.0
    private var motionY = 0.0
    private var motionZ = 0.0
    private var canBlink = false
    private var lastX = 0.0
    private var lastY = 0.0
    private var lastZ = 0.0
    private var lastGroundPos = DoubleArray(3)
    private var lastFound = 0.0
    private var shouldRender = false
    private var shouldStopMotion = false
    private var shouldEdit = false
    private val positions = LinkedList<DoubleArray>()
    private val packetCache = ArrayList<C03PacketPlayer>()
    private val shouldSkip: Boolean
        get() = (noFlyValue.get() && LiquidBounce.moduleManager.getModule(Fly::class.java)!!.state) ||
                (noFreeCamValue.get() && LiquidBounce.moduleManager.getModule(FreeCam::class.java)!!.state)
    private val scaffold: Scaffold
        get() = LiquidBounce.moduleManager.getModule(Scaffold::class.java)!!

    private fun pullBack() {
        shouldStopMotion = true
        when (setBackModeValue.get().lowercase()) {
            "illegalteleport" -> {
                mc.thePlayer.setPositionAndUpdate(lastX, lastY, lastZ)
                var i = 0
                while (i < illegalDupeValue.get()) {
                    PacketUtils.sendPacketNoEvent(
                        C04PacketPlayerPosition(
                            mc.thePlayer.posX,
                            mc.thePlayer.posY - 1E+159,
                            mc.thePlayer.posZ,
                            false
                        )
                    )
                    i++
                }
            }

            "illegalpacket" -> {
                var i = 0
                while (i < illegalDupeValue.get()) {
                    PacketUtils.sendPacketNoEvent(
                        C04PacketPlayerPosition(
                            mc.thePlayer.posX,
                            mc.thePlayer.posY - 1E+159,
                            mc.thePlayer.posZ,
                            false
                        )
                    )
                    i++
                }
            }

            "teleport" -> mc.thePlayer.setPositionAndUpdate(lastX, lastY, lastZ)
            "flyflag" -> mc.thePlayer.motionY = 0.0
            "stopmotion" -> {
                val oldFallDist = mc.thePlayer.fallDistance
                mc.thePlayer.motionY = 0.0
                mc.thePlayer.fallDistance = oldFallDist
            }

            "position" -> PacketUtils.sendPacketNoEvent(
                C06PacketPlayerPosLook(
                    mc.thePlayer.posX,
                    mc.thePlayer.posY + RandomUtils.nextDouble(6.0, 10.0),
                    mc.thePlayer.posZ,
                    mc.thePlayer.rotationYaw,
                    mc.thePlayer.rotationPitch,
                    false
                )
            )

            "edit", "spoofback" -> shouldEdit = true
        }
        if (resetFallDistanceValue.get() && setBackModeValue.get() != "stopmotion")
            mc.thePlayer.fallDistance = 0f
        if (scaffoldValue.get() && !scaffold.state)
            scaffold.state = true
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent?) {
        if (shouldSkip) return

        detectedLocation = null
        if (setBackModeValue.get().equals("blink", true)) {
            if (!blink) {
                val collide = NewFallingPlayer(
                    mc.thePlayer.posX,
                    mc.thePlayer.posY,
                    mc.thePlayer.posZ,
                    0.0,
                    0.0,
                    0.0,
                    0f,
                    0f,
                    0f,
                    0f
                ).findCollision(60)
                if (canBlink && (collide == null || mc.thePlayer.posY - collide.y > startFallDistValue.get())) {
                    posX = mc.thePlayer.posX
                    posY = mc.thePlayer.posY
                    posZ = mc.thePlayer.posZ
                    motionX = mc.thePlayer.motionX
                    motionY = mc.thePlayer.motionY
                    motionZ = mc.thePlayer.motionZ
                    packetCache.clear()
                    blink = true
                }
                if (mc.thePlayer.onGround) {
                    canBlink = true
                }
            } else {
                if (mc.thePlayer.fallDistance > setBackFallDistValue.get()) {
                    mc.thePlayer.setPositionAndUpdate(posX, posY, posZ)
                    if (resetMotionValue.get()) {
                        mc.thePlayer.motionX = 0.0
                        mc.thePlayer.motionY = 0.0
                        mc.thePlayer.motionZ = 0.0
                        mc.thePlayer.jumpMovementFactor = 0.00f
                    } else {
                        mc.thePlayer.motionX = motionX
                        mc.thePlayer.motionY = motionY
                        mc.thePlayer.motionZ = motionZ
                        mc.thePlayer.jumpMovementFactor = 0.00f
                    }
                    if (scaffoldValue.get()) {
                        LiquidBounce.moduleManager.getModule(Scaffold::class.java)!!.state = true
                    }
                    packetCache.clear()
                    blink = false
                    canBlink = false
                } else if (mc.thePlayer.onGround) {
                    blink = false
                    for (packet in packetCache) {
                        mc.netHandler.addToSendQueue(packet)
                    }
                }
            }
        }
        if (voidDetectionAlgorithm.get().equals("collision", true)) {
            if (mc.thePlayer.onGround && getBlock(
                    BlockPos(
                        mc.thePlayer.posX,
                        mc.thePlayer.posY - 1.0,
                        mc.thePlayer.posZ
                    )
                ) !is BlockAir
            ) {
                lastX = mc.thePlayer.prevPosX
                lastY = mc.thePlayer.prevPosY
                lastZ = mc.thePlayer.prevPosZ
            }
            shouldRender = renderTraceValue.get() && !MovementUtils.isBlockUnder()
            shouldStopMotion = false
            shouldEdit = false
            if (!MovementUtils.isBlockUnder()) {
                if (mc.thePlayer.fallDistance >= setBackFallDistValue.get())
                    pullBack()
            }
        } else {
            if (mc.thePlayer.onGround && getBlock(
                    BlockPos(
                        mc.thePlayer.posX,
                        mc.thePlayer.posY - 1.0,
                        mc.thePlayer.posZ
                    )
                ) !is BlockAir
            ) {
                lastX = mc.thePlayer.prevPosX
                lastY = mc.thePlayer.prevPosY
                lastZ = mc.thePlayer.prevPosZ
            }
            shouldStopMotion = false
            shouldEdit = false
            shouldRender = false
            if (!mc.thePlayer.onGround && !mc.thePlayer.isOnLadder && !mc.thePlayer.isInWater) {
                val newFallingPlayer = NewFallingPlayer(mc.thePlayer)
                try {
                    detectedLocation = newFallingPlayer.findCollision(maxFindRangeValue.get())
                } catch (e: Exception) {
                    // do nothing. i hate errors
                }
                if (detectedLocation != null && abs(mc.thePlayer.posY - detectedLocation.y) +
                    mc.thePlayer.fallDistance <= maxFallDistSimulateValue.get()
                ) {
                    lastFound = mc.thePlayer.fallDistance.toDouble()
                }
                shouldRender = renderTraceValue.get() && detectedLocation == null
                if (mc.thePlayer.fallDistance - lastFound > setBackFallDistValue.get()) {
                    pullBack()
                }
            }
        }
        if (shouldRender) synchronized(positions) {
            positions.add(
                doubleArrayOf(
                    mc.thePlayer.posX,
                    mc.thePlayer.entityBoundingBox.minY,
                    mc.thePlayer.posZ
                )
            )
        } else synchronized(positions) { positions.clear() }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (shouldSkip) return

        if (setBackModeValue.get().equals("blink", true) && blink && event.packet is C03PacketPlayer) {
            packetCache.add(event.packet)
            event.cancelEvent()
        }
        if (setBackModeValue.get().equals("stopmotion", true) && event.packet is S08PacketPlayerPosLook)
            mc.thePlayer.fallDistance = 0f
        if (setBackModeValue.get().equals("edit", true) && shouldEdit && event.packet is C03PacketPlayer) {
            event.packet.y += 100.0
            shouldEdit = false
        }
        if (setBackModeValue.get().equals("spoofback", true) && shouldEdit && event.packet is C03PacketPlayer) {
            val packetPlayer = event.packet
            packetPlayer.x = lastX
            packetPlayer.y = lastY
            packetPlayer.z = lastZ
            packetPlayer.isMoving = false
            shouldEdit = false
        }
        if (setBackModeValue.get().equals("hypixeltest", true)) {
            if (event.packet is C03PacketPlayer) {
                if (isInVoid) {
                    event.cancelEvent()
                    packets.add(event.packet)
                    if (mc.thePlayer.fallDistance >= setBackFallDistValue.get()) {
                        PacketUtils.sendPacketNoEvent(
                            C04PacketPlayerPosition(
                                lastGroundPos[0],
                                lastGroundPos[1] - 1,
                                lastGroundPos[2],
                                true
                            )
                        )
                    }
                } else {
                    lastGroundPos[0] = mc.thePlayer.posX
                    lastGroundPos[1] = mc.thePlayer.posY
                    lastGroundPos[2] = mc.thePlayer.posZ
                    if (packets.isNotEmpty()) {
                        ClientUtils.displayChatMessage("Release Packets - " + packets.size)
                        for (p in packets) PacketUtils.sendPacketNoEvent(p)
                        packets.clear()
                    }
                    timer.reset()
                }
            }
        }
    }

    @EventTarget
    fun onMove(event: MoveEvent) {
        if (shouldSkip) return

        if (setBackModeValue.get().equals("stopmotion", true) && shouldStopMotion) {
            event.zero()
        }
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent?) {
        if (shouldSkip) return

        if (shouldRender) synchronized(positions) {
            GL11.glPushMatrix()
            GL11.glDisable(GL11.GL_TEXTURE_2D)
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
            GL11.glEnable(GL11.GL_LINE_SMOOTH)
            GL11.glEnable(GL11.GL_BLEND)
            GL11.glDisable(GL11.GL_DEPTH_TEST)
            mc.entityRenderer.disableLightmap()
            GL11.glLineWidth(1f)
            GL11.glBegin(GL11.GL_LINE_STRIP)
            GL11.glColor4f(1f, 1f, 0.1f, 1f)
            val renderPosX = mc.renderManager.viewerPosX
            val renderPosY = mc.renderManager.viewerPosY
            val renderPosZ = mc.renderManager.viewerPosZ
            for (pos in positions) GL11.glVertex3d(pos[0] - renderPosX, pos[1] - renderPosY, pos[2] - renderPosZ)
            GL11.glColor4d(1.0, 1.0, 1.0, 1.0)
            GL11.glEnd()
            GL11.glEnable(GL11.GL_DEPTH_TEST)
            GL11.glDisable(GL11.GL_LINE_SMOOTH)
            GL11.glDisable(GL11.GL_BLEND)
            GL11.glEnable(GL11.GL_TEXTURE_2D)
            GL11.glPopMatrix()
        }
    }

    override fun onDisable() {
        reset()
        super.onDisable()
    }

    override fun onEnable() {
        reset()
        super.onEnable()
    }

    private fun reset() {
        canBlink = false
        blink = false
        detectedLocation = null
        lastFound = 0.0
        lastZ = lastFound
        lastY = lastZ
        lastX = lastY
        shouldRender = false
        shouldStopMotion = shouldRender
        synchronized(positions) { positions.clear() }
    }

    companion object {
        var timer = TimerUtils()
        var packets = ArrayList<C03PacketPlayer>()
        val isInVoid: Boolean
            get() {
                for (i in 0..128) {
                    if (MovementUtils.isOnGround(i.toDouble())) {
                        return false
                    }
                }
                return true
            }
    }
}
