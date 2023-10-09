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
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.network.play.client.*
import net.minecraft.network.play.client.C03PacketPlayer.C05PacketPlayerLook
import net.minecraft.network.play.client.C03PacketPlayer.C06PacketPlayerPosLook
import net.minecraft.world.WorldSettings

@ModuleInfo(
    name = "FreeCam",
    spacedName = "Free Cam",
    description = "Allows you to move out of your body.",
    category = ModuleCategory.RENDER
)
class FreeCam : Module() {

    private val horizontalSpeed = FloatValue("HorizontalSpeed", 0.8F, 0.1F, 2F, "bpt")
    private val verticalSpeed = FloatValue("VerticalSpeed", 0.2F, 0.1F, 2F, "bpt")
    private val flyValue = BoolValue("Fly", true)
    private val noClipValue = BoolValue("NoClip", true)

    private var originalPlayer: EntityOtherPlayerMP? = null
    private var oldX = 0.0
    private var oldY = 0.0
    private var oldZ = 0.0
    private var oldYaw = 0f
    private var oldPitch = 0f
    private var oldGamemode = WorldSettings.GameType.NOT_SET

    private var lastOnGround = false

    override fun onEnable() {
        mc.thePlayer ?: return

        oldX = mc.thePlayer.posX
        oldY = mc.thePlayer.posY
        oldZ = mc.thePlayer.posZ
        oldYaw = mc.thePlayer.rotationYaw
        oldPitch = mc.thePlayer.rotationPitch
        lastOnGround = mc.thePlayer.onGround
        oldGamemode = mc.playerController.currentGameType

        mc.playerController.currentGameType = WorldSettings.GameType.SPECTATOR

        originalPlayer = EntityOtherPlayerMP(mc.theWorld, mc.thePlayer.gameProfile)
        originalPlayer!!.clonePlayer(mc.thePlayer, true)
        originalPlayer!!.rotationYawHead = mc.thePlayer.rotationYawHead
        originalPlayer!!.copyLocationAndAnglesFrom(mc.thePlayer)
        mc.theWorld.addEntityToWorld(-1000, originalPlayer!!)

        if (noClipValue.get())
            mc.thePlayer.noClip = true
    }

    override fun onDisable() {
        mc.thePlayer ?: return
        originalPlayer ?: return

        mc.thePlayer.copyLocationAndAnglesFrom(originalPlayer!!)
        mc.thePlayer.onGround = originalPlayer!!.onGround
        mc.playerController.currentGameType = oldGamemode

        mc.theWorld.removeEntityFromWorld(originalPlayer!!.entityId)
        originalPlayer = null
        mc.thePlayer.motionX = 0.0
        mc.thePlayer.motionY = 0.0
        mc.thePlayer.motionZ = 0.0
        // TODO: fix DuplicateAim and GroudSpoof flag when disabling
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
            if (mc.gameSettings.keyBindJump.isKeyDown)
                mc.thePlayer.motionY += verticalSpeed.get()
            if (mc.gameSettings.keyBindSneak.isKeyDown)
                mc.thePlayer.motionY -= verticalSpeed.get()
            MovementUtils.strafe(horizontalSpeed.get())
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        originalPlayer ?: return

        val packet = event.packet

        // fix grim AimDuplicateLook
        if (packet is C05PacketPlayerLook || packet is C06PacketPlayerPosLook) {
            event.cancelEvent()
            return
        }

        if (packet is C03PacketPlayer) {
            packet.x = oldX
            packet.y = oldY
            packet.z = oldZ
            packet.yaw = oldYaw
            packet.pitch = oldPitch
            packet.isMoving = false
            packet.onGround = lastOnGround
        }

        // don't cancel keepalive because it flag BadPacketE on grim
        if (packet is C08PacketPlayerBlockPlacement || packet is C07PacketPlayerDigging || packet is C0BPacketEntityAction ||
            packet is C0APacketAnimation || packet is C02PacketUseEntity
        )
            event.cancelEvent()
    }
}