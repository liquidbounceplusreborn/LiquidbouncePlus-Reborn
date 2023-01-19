/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.modules.player.Blink
import net.ccbluex.liquidbounce.features.module.modules.render.FreeCam
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.ccbluex.liquidbounce.value.*
import net.minecraft.network.play.client.*
import net.minecraft.network.play.client.C03PacketPlayer.*

@ModuleInfo(name = "PacketFixer", spacedName = "Packet Fixer", description = "Fix some weird packet issues. May bypass some Verus checks.", category = ModuleCategory.WORLD)
class PacketFixer : Module() {

	// settings
	private val fixBlinkAndFreecam = BoolValue("BlinkFreeCam3Y", true)
    private val fixPacketPlayer = BoolValue("Timer3A", true)
    private val fixItemSwap = BoolValue("Scaffold14D", true)
	private val fixInvalidPlace = BoolValue("Scaffold14E", true)
    private val fixGround = BoolValue("Fly4I", false)
    private val fixIdleFly = BoolValue("Fly4C", false)

	// local variables
	private var x = 0.0
	private var y = 0.0
	private var z = 0.0
	private var yaw = 0.0F
	private var pitch = 0.0F
	private var jam = 0
	private var packetCount = 0
	private var prevSlot = -1

	// events
	override fun onEnable() {
		jam = 0
		packetCount = 0
		prevSlot = -1

		if (mc.thePlayer == null) return
		x = mc.thePlayer.posX
		y = mc.thePlayer.posY
		z = mc.thePlayer.posZ
		yaw = mc.thePlayer.rotationYaw
		pitch = mc.thePlayer.rotationPitch
	}

	@EventTarget(priority = 1)
	private fun onPacket(event: PacketEvent) {
		if (mc.thePlayer == null || mc.theWorld == null || event.isCancelled) return

		val packet = event.packet

		// fix ground check (4I)
		if (fixGround.get() && packet is C03PacketPlayer && packet !is C04PacketPlayerPosition && packet !is C06PacketPlayerPosLook) {
			if ((mc.thePlayer.motionY == 0.0 || (mc.thePlayer.onGround && mc.thePlayer.isCollidedVertically)) && !packet.onGround)
				packet.onGround = true
		}

		if (packet is C04PacketPlayerPosition) {
			x = packet.x
			y = packet.y
			z = packet.z
			jam = 0
		}

		if (packet is C05PacketPlayerLook) {
			yaw = packet.yaw
			pitch = packet.pitch
		}

		if (packet is C06PacketPlayerPosLook) {
			x = packet.x
			y = packet.y
			z = packet.z
			jam = 0

			yaw = packet.yaw
			pitch = packet.pitch
		}

		// fix bad packets, caused by timer or fast use
		if (fixPacketPlayer.get() && packet is C03PacketPlayer && packet !is C04PacketPlayerPosition && packet !is C06PacketPlayerPosLook) {
			jam++
			if (jam > 20) {
				jam = 0
				event.cancelEvent()
				PacketUtils.sendPacketNoEvent(C06PacketPlayerPosLook(x, y, z, yaw, pitch, packet.onGround))
			}
		}

		// fix scaffold duplicated hotbar switch
		if (!mc.isSingleplayer() && fixItemSwap.get() && packet is C09PacketHeldItemChange) {
			if (packet.getSlotId() == prevSlot) {
				event.cancelEvent()
			} else {
				prevSlot = packet.getSlotId()
			}
		}

		if (fixInvalidPlace.get() && packet is C08PacketPlayerBlockPlacement) {
			packet.facingX = packet.facingX.coerceIn(-1.00000F, 1.00000F)
			packet.facingY = packet.facingY.coerceIn(-1.00000F, 1.00000F)
			packet.facingZ = packet.facingZ.coerceIn(-1.00000F, 1.00000F)
		}

		// fix blink and freecam cancelling c03s while sending c00
		val blink = LiquidBounce.moduleManager.getModule(Blink::class.java)!! as Blink
		val freeCam = LiquidBounce.moduleManager.getModule(FreeCam::class.java)!! as FreeCam
		if (fixBlinkAndFreecam.get() && ((blink.state && !blink.pulseValue.get()) || (freeCam.state && !freeCam.undetectableValue.get())) && packet is C00PacketKeepAlive)
			event.cancelEvent()

		// fix fly while not moving, reduce some checks (4C)
		if (fixIdleFly.get() && packet is C03PacketPlayer && !packet.onGround) {
			if (packet !is C04PacketPlayerPosition && packet !is C05PacketPlayerLook && packet !is C06PacketPlayerPosLook) {
				packetCount++
				if (packetCount >= 2) 
					event.cancelEvent()
			} else {
				packetCount = 0
			}
		}
	}

}
