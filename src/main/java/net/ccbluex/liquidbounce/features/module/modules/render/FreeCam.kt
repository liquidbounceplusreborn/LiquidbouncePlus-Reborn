/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.PosLookInstance
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.network.Packet
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.*
import net.minecraft.network.play.client.C0BPacketEntityAction
import net.minecraft.network.play.server.S08PacketPlayerPosLook

@ModuleInfo(name = "FreeCam", spacedName = "Free Cam", description = "Allows you to move out of your body.", category = ModuleCategory.RENDER)
class FreeCam : Module() {

    private val speedValue = FloatValue("Speed", 0.8F, 0.1F, 2F, "m")
    private val flyValue = BoolValue("Fly", true)
    private val noClipValue = BoolValue("NoClip", true)
    val undetectableValue = BoolValue("Undetectable", true)

    private var fakePlayer: EntityOtherPlayerMP? = null
    private var oldX = 0.0
    private var oldY = 0.0
    private var oldZ = 0.0

    private var lastOnGround = false

    private val posLook = PosLookInstance()

    override fun onEnable() {
        mc.thePlayer ?: return

        oldX = mc.thePlayer.posX
        oldY = mc.thePlayer.posY
        oldZ = mc.thePlayer.posZ
        lastOnGround = mc.thePlayer.onGround

        fakePlayer = EntityOtherPlayerMP(mc.theWorld, mc.thePlayer.gameProfile)
        fakePlayer!!.clonePlayer(mc.thePlayer, true)
        fakePlayer!!.rotationYawHead = mc.thePlayer.rotationYawHead
        fakePlayer!!.copyLocationAndAnglesFrom(mc.thePlayer)
        mc.theWorld.addEntityToWorld(-1000, fakePlayer!!)

        if (noClipValue.get())
            mc.thePlayer.noClip = true
    }

    override fun onDisable() {
        mc.thePlayer ?: return
        fakePlayer ?: return

        mc.thePlayer.posX = fakePlayer!!.posX
        mc.thePlayer.posY = fakePlayer!!.posY
        mc.thePlayer.posZ = fakePlayer!!.posZ

        mc.theWorld.removeEntityFromWorld(fakePlayer!!.entityId)
        fakePlayer = null
        mc.thePlayer.motionX = 0.0
        mc.thePlayer.motionY = 0.0
        mc.thePlayer.motionZ = 0.0
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (noClipValue.get())
            mc.thePlayer.noClip = true
        mc.thePlayer.fallDistance = 0F

        if (flyValue.get()) {
            mc.thePlayer.motionY = 0.0
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionZ = 0.0
            if (mc.gameSettings.keyBindJump.isKeyDown())
                mc.thePlayer.motionY += speedValue.get()
            if (mc.gameSettings.keyBindSneak.isKeyDown())
                mc.thePlayer.motionY -= speedValue.get()
            MovementUtils.strafe(speedValue.get())
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        fakePlayer ?: return

        val packet = event.packet
        if (undetectableValue.get()) {
            if (packet is C04PacketPlayerPosition || packet is C05PacketPlayerLook) {
                event.cancelEvent()
                mc.netHandler.addToSendQueue(C03PacketPlayer(lastOnGround))
            } else if (packet is C06PacketPlayerPosLook) {
                if (posLook.equalFlag(packet)) {
                    fakePlayer!!.setPosition(packet.x, packet.y, packet.z)
                    fakePlayer!!.onGround = packet.onGround
                    lastOnGround = packet.onGround
                    fakePlayer!!.rotationYaw = packet.yaw
                    fakePlayer!!.rotationPitch = packet.pitch
                    fakePlayer!!.rotationYawHead = packet.yaw
                    posLook.reset()
                } else if (mc.thePlayer.positionUpdateTicks >= 20) {
                    packet.x = fakePlayer!!.posX
                    packet.y = fakePlayer!!.posY
                    packet.z = fakePlayer!!.posZ
                    packet.onGround = lastOnGround
                    packet.yaw = fakePlayer!!.rotationYaw
                    packet.pitch = fakePlayer!!.rotationPitch
                } else {
                    event.cancelEvent()
                    mc.netHandler.addToSendQueue(C03PacketPlayer(lastOnGround))
                }
            }
        } else if (packet is C03PacketPlayer)
            event.cancelEvent()
        if (packet is C0BPacketEntityAction)
            event.cancelEvent()
        if (packet is S08PacketPlayerPosLook) {
            event.cancelEvent()
            posLook.set(packet)
        }
    }

}