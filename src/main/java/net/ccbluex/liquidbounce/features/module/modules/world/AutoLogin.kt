/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Type
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.TextValue
import net.minecraft.network.play.client.C01PacketChatMessage
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.network.play.server.S45PacketTitle

@ModuleInfo(name = "AutoLogin", spacedName = "Auto Login", description = "Automatically login into some servers for you.", category = ModuleCategory.WORLD)
class AutoLogin : Module() {

	private val password = TextValue("Password", "example@01")
	private val regRegex = TextValue("Register-Regex", "/register")
	private val loginRegex = TextValue("Login-Regex", "/login")
	private val regCmd = TextValue("Register-Cmd", "/register %p %p")
	private val loginCmd = TextValue("Login-Cmd", "/login %p")
    private val unique = BoolValue("Unique", false)
    private val uniqueFormat = TextValue("UniqueFormat", "%name%_%p%_xx") { unique.get() }

    private val uniqueHelp: BoolValue = object : BoolValue("ClickForFormatHelp", false, { unique.get() }) {
        override fun onChange(oldValue: Boolean, newValue: Boolean) {
            ClientUtils.displayChatMessage("UniqueFormat placeholders:")
            ClientUtils.displayChatMessage("  - %name%: username")
            ClientUtils.displayChatMessage("  - %pass%: original password")
            ClientUtils.displayChatMessage("  - More coming soon...")
        }
    }

	private val delayValue = IntegerValue("Delay", 5000, 0, 5000, "ms")

	private val loginPackets = arrayListOf<C01PacketChatMessage>()
	private val registerPackets = arrayListOf<C01PacketChatMessage>()
	private val regTimer = MSTimer()
	private val logTimer = MSTimer()

	override fun onEnable() = resetEverything()

    private fun getPassword(): String {
        var pw = password.get()
        if (unique.get())
            pw = uniqueFormat.get().replace("%name%", mc.session.username).replace("%pass%", password.get())

        return pw
    }

	@EventTarget
	fun onWorld(event: WorldEvent) = resetEverything()

	@EventTarget
	fun onUpdate(event: UpdateEvent) {
		if (registerPackets.isEmpty())
			regTimer.reset()
		else if (regTimer.hasTimePassed(delayValue.get().toLong())) {
			for (packet in registerPackets)
				PacketUtils.sendPacketNoEvent(packet)
			LiquidBounce.hud.addNotification(Notification("Successfully registered.", Type.SUCCESS))
			registerPackets.clear()
			regTimer.reset()
		}

		if (loginPackets.isEmpty())
			logTimer.reset()
		else if (logTimer.hasTimePassed(delayValue.get().toLong())) {
			for (packet in loginPackets)
				PacketUtils.sendPacketNoEvent(packet)
            LiquidBounce.hud.addNotification(Notification("Successfully logged in.", Type.SUCCESS))
			loginPackets.clear()
			logTimer.reset()
		}
	}

    @EventTarget
    fun onPacket(event: PacketEvent) {
		if (mc.thePlayer == null)
			return

		val packet = event.packet

    	if (packet is S45PacketTitle) {
            val messageOrigin = packet.message ?: return
            val message: String = messageOrigin.unformattedText

    		if (message.contains(loginRegex.get(), true))
                sendLogin(loginCmd.get().replace("%p", getPassword(), true))

    		if (message.contains(regRegex.get(), true))
                sendRegister(regCmd.get().replace("%p", getPassword(), true))
    	}

    	if (packet is S02PacketChat) {
            val message: String = packet.chatComponent.unformattedText

    		if (message.contains(loginRegex.get(), true))
                sendLogin(loginCmd.get().replace("%p", getPassword(), true))

    		if (message.contains(regRegex.get(), true))
                sendRegister(regCmd.get().replace("%p", getPassword(), true))
    	}
    }

	private fun sendLogin(str: String) = loginPackets.add(C01PacketChatMessage(str))
	private fun sendRegister(str: String) = registerPackets.add(C01PacketChatMessage(str))

	private fun resetEverything() {
		registerPackets.clear()
		loginPackets.clear()
		regTimer.reset()
		logTimer.reset()
    }
}