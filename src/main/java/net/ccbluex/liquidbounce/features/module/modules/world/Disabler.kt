/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 *
 * Some parts of the code are taken and modified from Rilshrink/Minecraft-Disablers, as well as UnlegitMC/FDPClient.
 */
package net.ccbluex.liquidbounce.features.module.modules.world

import io.netty.buffer.Unpooled
import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.ccbluex.liquidbounce.features.module.modules.movement.Fly
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed
import net.ccbluex.liquidbounce.features.module.modules.movement.TargetStrafe
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.NotifyType
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.*
import net.ccbluex.liquidbounce.utils.misc.RandomUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.Stencil
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.init.Items
import net.minecraft.network.Packet
import net.minecraft.network.PacketBuffer
import net.minecraft.network.play.INetHandlerPlayServer
import net.minecraft.network.play.client.*
import net.minecraft.network.play.client.C03PacketPlayer.*
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.Vec3
import java.awt.Color
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.IOException
import java.util.*
import java.util.concurrent.LinkedBlockingQueue
import kotlin.math.round
import kotlin.math.sqrt


@ModuleInfo(name = "Disabler", description = "Disable some anticheats' checks.", category = ModuleCategory.WORLD)
class Disabler : Module() {

	val modeValue = ListValue("Mode",
		arrayOf(
			"Basic", // basic disabler
			"SpartanCombat", // old spartan combat disabler
			"MatrixGeyser", // work with old matrix, around 5.2.x (with badly configured geysermc)
			"OldVerus", // Dort
			"LatestVerus", // FDP
			"PingSpoof", // ping spoof
			"Flag", // flag
			"Matrix", // re
			"Watchdog", // $100k anticheat
			"RotDesync", // Dort, again.
			"Vulcan",// FDP
			"Grim"//made by 吸尘器（人名/name)
		), "SpartanCombat")

	// PingSpoof (idfk what's this purpose but i will keep it here.)
	private val minpsf: IntegerValue = object : IntegerValue("PingSpoof-MinDelay", 0, 0, 10000, "ms", { modeValue.get().equals("pingspoof", true) }) {
		override fun onChanged(oldValue: Int, newValue: Int) {
			val v = maxpsf.get()
			if (v < newValue) set(v)
		}
	}
	private val maxpsf: IntegerValue = object : IntegerValue("PingSpoof-MaxDelay", 0, 0, 10000, "ms", { modeValue.get().equals("pingspoof", true) }) {
		override fun onChanged(oldValue: Int, newValue: Int) {
			val v = minpsf.get()
			if (v > newValue) set(v)
		}
	}
	private val psfStartSendMode = ListValue("PingSpoof-StartSendMode", arrayOf("All", "First"), "All", { modeValue.get().equals("pingspoof", true) })
	private val psfSendMode = ListValue("PingSpoof-SendMode", arrayOf("All", "First"), "All", { modeValue.get().equals("pingspoof", true) })
	private val psfWorldDelay = IntegerValue("PingSpoof-WorldDelay", 15000, 0, 30000, "ms", { modeValue.get().equals("pingspoof", true) })

	// flag
	private val flagMode = ListValue("Flag-Mode", arrayOf("Edit", "Packet"), "Edit", { modeValue.get().equals("flag", true) })
	private val flagTick = IntegerValue("Flag-TickDelay", 25, 1, 200, { modeValue.get().equals("flag", true) })
	private val flagSilent = BoolValue("Flag-SilentMode", true, { modeValue.get().equals("flag", true) })

	// matrix
	private val matrixNoCheck = BoolValue("Matrix-NoModuleCheck", false, { modeValue.get().equals("matrix", true) })
	private val matrixOldMoveFix = BoolValue("Matrix-OldMoveFix", true, { modeValue.get().equals("matrix", true) })
	private val matrixNewMoveFix = BoolValue("Matrix-NewMoveFix", true, { modeValue.get().equals("matrix", true) })
	private val matrixMoveOnly = BoolValue("Matrix-MoveOnly", false, { modeValue.get().equals("matrix", true) })
	private val matrixNoMovePacket = BoolValue("Matrix-NoMovePacket", true, { modeValue.get().equals("matrix", true) })
	private val matrixHotbarChange = BoolValue("Matrix-HotbarChange", true, { modeValue.get().equals("matrix", true) })

	// verus
	private val verusLobbyValue = BoolValue("LobbyCheck", true, { modeValue.get().equals("latestverus", true) || modeValue.get().equals("blocksmc", true) })
	private val verusFlagValue = BoolValue("Verus-Flag", true, { modeValue.get().equals("latestverus", true) })
	private val verusSlientFlagApplyValue = BoolValue("Verus-SlientFlagApply", false, { modeValue.get().equals("latestverus", true) })
	private val verusBufferSizeValue = IntegerValue("Verus-QueueActiveSize", 300, 0, 1000, { modeValue.get().equals("latestverus", true) })
	private val verusPurseDelayValue = IntegerValue("Verus-PurseDelay", 490, 0, 2000, "ms", { modeValue.get().equals("latestverus", true) })
	private val verusFlagDelayValue = IntegerValue("Verus-FlagDelay", 40, 40, 120, " tick", { modeValue.get().equals("latestverus", true) })
	private val verusAntiFlyCheck = BoolValue("Verus-AntiFly", true, { modeValue.get().equals("latestverus", true) })
	private val verusFakeInput = BoolValue("Verus-FakeInput", true, { modeValue.get().equals("latestverus", true) })
	private val verusValidPos = BoolValue("Verus-ValidPosition", true, { modeValue.get().equals("latestverus", true) })

	// watchdog
	private val waitingDisplayMode = ListValue("Waiting-Display", arrayOf("Top", "Middle", "Notification", "Chat", "None"), "Top", { modeValue.get().equals("watchdog", true) })
	val renderServer = BoolValue("Render-ServerSide", false, { modeValue.get().equals("watchdog", true) })
	private val autoAlert = BoolValue("BanAlert", false, { modeValue.get().equals("watchdog", true) })
	private val rotModify = BoolValue("RotationModifier", false, { modeValue.get().equals("watchdog", true) })
	private val tifality90 = BoolValue("Tifality", false, { modeValue.get().equals("watchdog", true) && rotModify.get() })
	private val noMoveKeepRot = BoolValue("NoMoveKeepRot", true, { modeValue.get().equals("watchdog", true) && rotModify.get() })
	private val noC03s = BoolValue("NoC03s", true, { modeValue.get().equals("watchdog", true) })
	private val testFeature = BoolValue("PingSpoof", false, { modeValue.get().equals("watchdog", true) })
	private val testDelay = IntegerValue("Delay", 400, 0, 1000, "ms", { modeValue.get().equals("watchdog", true) && testFeature.get() })
	private val checkValid = BoolValue("InvValidate", false, { modeValue.get().equals("watchdog", true) && testFeature.get() })

	//vulcan
	private val vulcanStrafe = BoolValue ("Strafe", true, { modeValue.get().equals("vulcan", true) })
	private val vulcanStrafe2 = BoolValue ("Test", true, { modeValue.get().equals("vulcan", true) })

	//grim
	private val post = BoolValue("Post",true, { modeValue.get().equals("grim", true) })
	private val c0e = BoolValue("ChestStealer",false, { modeValue.get().equals("grim", true) && post.get() })
	private val c08 = BoolValue("PlaceBlock",false, { modeValue.get().equals("grim", true) && post.get() })
	private val c0b = BoolValue("C0B",false, { modeValue.get().equals("grim", true) && post.get() })

	// debug
	private val debugValue = BoolValue("Debug", false)

	// variables
	private val keepAlives = arrayListOf<C00PacketKeepAlive>()
	private val transactions = arrayListOf<C0FPacketConfirmTransaction>()
	private val packetQueue = LinkedList<C0FPacketConfirmTransaction>()
	private val anotherQueue = LinkedList<C00PacketKeepAlive>()
	private val playerQueue = LinkedList<C03PacketPlayer>()
	private val packets: LinkedBlockingQueue<Any?> = LinkedBlockingQueue<Any?>()

	private val packetsG = LinkedBlockingQueue<Packet<*>>()

	private val packetBus = hashMapOf<Long, Packet<INetHandlerPlayServer>>()
	private val queueBus = LinkedList<Packet<INetHandlerPlayServer>>()
	private val packetBuffer = LinkedBlockingQueue<Packet<INetHandlerPlayServer>>()

	private val posLookInstance = PosLookInstance()

	private val msTimer = MSTimer()
	private val wdTimer = MSTimer()
	private val benTimer = MSTimer()
	private val timerCancelDelay = MSTimer()
	private val timerCancelTimer = MSTimer()
	private var timerShouldCancel = true
	private var canBlink = true

	private var alrSendY = false
	private var alrSprint = false

	private var expectedSetback = false

	private var sendDelay = 0
	private var shouldActive = false
	private var benHittingLean = false

	private var transCount = 0
	private var counter = 0
	private var randDelay = 250

	var shouldModifyRotation = false

	private var verusLastY = 0.0
	private var lastTick = 0

	private var s08count = 0
	private var ticking = 0
	private var lastYaw = 0F

	private var lastUid = 0

	private var initPos: Vec3? = null

	private var lastMotionX = 0.0;
	private var lastMotionY = 0.0;
	private var lastMotionZ = 0.0;
	private var pendingFlagApplyPacket = false;

	private var pre = false

	val speed = LiquidBounce.moduleManager.getModule(Speed::class.java)!!

	val canModifyRotation: Boolean
		get() = (state && modeValue.get().equals("watchdog", true) && shouldModifyRotation)

	val canRenderInto3D: Boolean
		get() = (state && modeValue.get().equals("watchdog", true) && renderServer.get() && shouldModifyRotation)

	fun isMoving(): Boolean = (mc.thePlayer != null && (mc.thePlayer.movementInput.moveForward != 0F || mc.thePlayer.movementInput.moveStrafe != 0F || mc.thePlayer.movementInput.sneak || mc.thePlayer.movementInput.jump))

	fun debug(s: String, force: Boolean = false) {
		if (debugValue.get() || force)
			ClientUtils.displayChatMessage("§7[§3§lDisabler§7]§f $s")
	}

	fun shouldRun(): Boolean = mc.thePlayer != null && mc.thePlayer.inventory != null && (!verusLobbyValue.get() || !mc.thePlayer.inventory.hasItem(Items.compass)) && mc.thePlayer.ticksExisted > 5
	fun isInventory(action: Short): Boolean = action > 0 && action < 100

	override val tag: String
		get() = modeValue.get()

	override fun onEnable() {
		keepAlives.clear()
		transactions.clear()
		packetQueue.clear()
		anotherQueue.clear()
		playerQueue.clear()
		packetBus.clear()
		queueBus.clear()
		packetBuffer.clear()

		s08count = 0

		msTimer.reset()
		wdTimer.reset()
		benTimer.reset()
		expectedSetback = false
		shouldActive = false
		alrSendY = false
		alrSprint = false
		transCount = 0
		lastTick = 0
		ticking = 0

		lastUid = 0
		posLookInstance.reset()

		shouldModifyRotation = false
		benHittingLean = false

	}

	override fun onDisable() {
		keepAlives.forEach {
			PacketUtils.sendPacketNoEvent(it)
		}
		transactions.forEach {
			PacketUtils.sendPacketNoEvent(it)
		}

		keepAlives.clear()
		transactions.clear()
		packetQueue.clear()
		anotherQueue.clear()
		packetBus.clear()

		if (modeValue.get().equals("watchdog", true)) {
			anotherQueue.forEach { PacketUtils.sendPacketNoEvent(it) }
			packetQueue.forEach { PacketUtils.sendPacketNoEvent(it) }
		}

		if (modeValue.get().equals("pingspoof", true)) {
			// make sure not to cause weird flag
			for (p in queueBus)
				PacketUtils.sendPacketNoEvent(p)
		}
		queueBus.clear()

		msTimer.reset()

		mc.thePlayer.motionY = 0.0
		MovementUtils.strafe(0F)
		mc.timer.timerSpeed = 1F

		shouldModifyRotation = false
	}

	@EventTarget
	fun onWorld(event: WorldEvent) {
		transactions.clear()
		keepAlives.clear()
		packetQueue.clear()
		anotherQueue.clear()
		playerQueue.clear()
		packetBus.clear()
		queueBus.clear()
		packetBuffer.clear()

		s08count = 0

		msTimer.reset()
		wdTimer.reset()
		benTimer.reset()
		expectedSetback = false
		shouldActive = false
		alrSendY = false
		alrSprint = false
		benHittingLean = false
		transCount = 0
		counter = 0
		lastTick = 0
		ticking = 0
		lastUid = 0
		posLookInstance.reset()

	}

	@EventTarget
	fun onRender2D(event: Render2DEvent) {
		if (!shouldActive)
		{
			val sc = ScaledResolution(mc)
			val strength = (msTimer.hasTimeLeft(psfWorldDelay.get().toLong()).toFloat() / psfWorldDelay.get().toFloat()).coerceIn(0F, 1F)

			if (modeValue.get().equals("pingspoof", true)) {
				Stencil.write(true)
				RenderUtils.drawRoundedRect(sc.scaledWidth / 2F - 50F, 35F, sc.scaledWidth / 2F + 50F, 55F, 10F, Color(0, 0, 0, 140).rgb)
				Stencil.erase(true)
				RenderUtils.drawRect(sc.scaledWidth / 2F - 50F, 35F, sc.scaledWidth / 2F - 50F + 100F * strength, 55F, Color(0, 111, 255, 70).rgb)
				Stencil.dispose()
				Fonts.font40.drawCenteredString("${(msTimer.hasTimeLeft(psfWorldDelay.get().toLong()).toFloat() / 1000F).toInt()}s left...", sc.scaledWidth / 2F, 41F, -1)
			}
			if ((modeValue.get().equals("watchdog", true) && testFeature.get()) && !ServerUtils.isHypixelLobby() && !mc.isSingleplayer()) {
				when (waitingDisplayMode.get().lowercase()) {
					"top" -> {
						Fonts.minecraftFont.drawString("Please wait...", sc.scaledWidth / 2F - Fonts.minecraftFont.getStringWidth("Please wait...") / 2F, 61.5F, Color(0, 0, 0).rgb, false)
						Fonts.minecraftFont.drawString("Please wait...", sc.scaledWidth / 2F - Fonts.minecraftFont.getStringWidth("Please wait...") / 2F, 62.5F, Color(0, 0, 0).rgb, false)
						Fonts.minecraftFont.drawString("Please wait...", sc.scaledWidth / 2F - Fonts.minecraftFont.getStringWidth("Please wait...") / 2F - 0.5F, 62F, Color(0, 0, 0).rgb, false)
						Fonts.minecraftFont.drawString("Please wait...", sc.scaledWidth / 2F - Fonts.minecraftFont.getStringWidth("Please wait...") / 2F + 0.5F, 62F, Color(0, 0, 0).rgb, false)
						Fonts.minecraftFont.drawString("Please wait...", sc.scaledWidth / 2F - Fonts.minecraftFont.getStringWidth("Please wait...") / 2F, 62F, Color(220, 220, 60).rgb, false)
					}
					"middle" -> {
						Fonts.minecraftFont.drawString("Please wait...", sc.scaledWidth / 2F - Fonts.minecraftFont.getStringWidth("Please wait...") / 2F, sc.scaledHeight / 2F + 14.5F, Color(0, 0, 0).rgb, false)
						Fonts.minecraftFont.drawString("Please wait...", sc.scaledWidth / 2F - Fonts.minecraftFont.getStringWidth("Please wait...") / 2F, sc.scaledHeight / 2F + 15.5F, Color(0, 0, 0).rgb, false)
						Fonts.minecraftFont.drawString("Please wait...", sc.scaledWidth / 2F - Fonts.minecraftFont.getStringWidth("Please wait...") / 2F - 0.5F, sc.scaledHeight / 2F + 15F, Color(0, 0, 0).rgb, false)
						Fonts.minecraftFont.drawString("Please wait...", sc.scaledWidth / 2F - Fonts.minecraftFont.getStringWidth("Please wait...") / 2F + 0.5F, sc.scaledHeight / 2F + 15F, Color(0, 0, 0).rgb, false)
						Fonts.minecraftFont.drawString("Please wait...", sc.scaledWidth / 2F - Fonts.minecraftFont.getStringWidth("Please wait...") / 2F, sc.scaledHeight / 2F + 15F, Color(220, 220, 60).rgb, false)
					}
				}
			}
		}
	}

	@EventTarget
	fun onPacket(event: PacketEvent) {
		val packet = event.packet

		when (modeValue.get().lowercase()) {
			"vulcan" -> {
				if (vulcanStrafe2.get()) {
					if (event.packet is C0FPacketConfirmTransaction) {
						if (mc.thePlayer.ticksExisted % 20 == 0) {
							event.cancelEvent()
						}
					}
					if (event.packet is C17PacketCustomPayload) {
						event.cancelEvent()
					}
					if (event.packet is S08PacketPlayerPosLook) {
						val c03 = event.packet as C03PacketPlayer
						val s08 = event.packet as S08PacketPlayerPosLook
						mc.thePlayer.posX = s08.x
						mc.thePlayer.posY = s08.y - 1
						mc.thePlayer.posZ = s08.z
						c03.isMoving = false
						c03.rotating = false
						c03.onGround = false
					}
					val packet: Packet<*> = event.packet
					if (packet is S08PacketPlayerPosLook) {
						val x = packet.getX() - mc.thePlayer.posX
						val y = packet.getY() - mc.thePlayer.posY
						val z = packet.getZ() - mc.thePlayer.posZ
						val diff = Math.sqrt(x * x + y * y + z * z)
						if (diff <= 4) {
							PacketUtils.sendPacketNoEvent(
								C06PacketPlayerPosLook(
									packet.getX(),
									packet.getY(),
									packet.getZ(),
									packet.getYaw(),
									packet.getPitch(),
									true
								)
							)
						}
					}
				}
			}

			"basic" -> {
				if (packet is C00PacketKeepAlive) {
					debug("C00PacketKeepAlive disabled")
					event.cancelEvent()
				}
				if (packet is C0FPacketConfirmTransaction) {
					debug("C0FPacketConfirmTransaction disabled")
					event.cancelEvent()
				}
			}

			"matrixgeyser" -> if (packet is C03PacketPlayer && mc.thePlayer.ticksExisted % 15 == 0) {
				try {
					val b = ByteArrayOutputStream()
					val _out = DataOutputStream(b)
					_out.writeUTF(mc.thePlayer.gameProfile.getName())
					val buf = PacketBuffer(Unpooled.buffer())
					buf.writeBytes(b.toByteArray())
					mc.netHandler.addToSendQueue(C17PacketCustomPayload("matrix:geyser", buf))

					debug("Sent Matrix Geyser spoof packet.")
				} catch (e: IOException) {
					debug("Error occurred.")
				}
			}

			"spartancombat" -> {
				if (packet is C00PacketKeepAlive && (keepAlives.size <= 0 || packet != keepAlives[keepAlives.size - 1])) {
					debug("c00 added")
					keepAlives.add(packet)
					event.cancelEvent()
				}
				if (packet is C0FPacketConfirmTransaction && (transactions.size <= 0 || packet != transactions[transactions.size - 1])) {
					debug("c0f added")
					transactions.add(packet)
					event.cancelEvent()
				}
			}

			"latestverus" -> { // liulihaocai
				if (!shouldRun()) {
					msTimer.reset()
					packetQueue.clear()
					return
				}

				if (packet is C0FPacketConfirmTransaction && !isInventory(packet.uid)) {
					packetQueue.add(packet)
					event.cancelEvent()
					if (packetQueue.size > verusBufferSizeValue.get()) {
						if (!shouldActive) {
							shouldActive = true
							LiquidBounce.hud.addNotification(
								Notification(
									"Disabler",
									"Successfully put Verus into sleep.",
									NotifyType.SUCCESS
								)
							)
						}
						PacketUtils.sendPacketNoEvent(packetQueue.poll())
					}
					debug("c0f, ${packetQueue.size}")
				}

				if (packet is C0BPacketEntityAction) {
					event.cancelEvent()
					debug("ignored packet action")
				}

				if (packet is C03PacketPlayer) {
					if (verusFlagValue.get() && mc.thePlayer.ticksExisted % verusFlagDelayValue.get() == 0) {
						debug("modified c03")
						packet.y -= 11.015625 // just phase into ground instead (minimum to flag)
						packet.onGround = false
						packet.isMoving = false
					}
					if (verusValidPos.get() && packet is C03PacketPlayer) {
						if (packet.y % 0.015625 == 0.0) {
							packet.onGround = true
							debug("true asf")
						}
					}
				}

				if (packet is S08PacketPlayerPosLook && verusSlientFlagApplyValue.get()) {
					val x = packet.x - mc.thePlayer.posX
					val y = packet.y - mc.thePlayer.posY
					val z = packet.z - mc.thePlayer.posZ
					val diff = sqrt(x * x + y * y + z * z)
					if (diff <= 8) {
						event.cancelEvent()
						// verus, why
						debug("flag silent accept")
						PacketUtils.sendPacketNoEvent(
							C06PacketPlayerPosLook(
								packet.x,
								packet.y,
								packet.z,
								packet.getYaw(),
								packet.getPitch(),
								false
							)
						)
					}
				}
			}

			"oldverus" -> {
				if (packet is C03PacketPlayer) {
					val yPos = round(mc.thePlayer.posY / 0.015625) * 0.015625
					mc.thePlayer.setPosition(mc.thePlayer.posX, yPos, mc.thePlayer.posZ)

					if (mc.thePlayer.ticksExisted % 45 == 0) {
						debug("flag")
						PacketUtils.sendPacketNoEvent(
							C04PacketPlayerPosition(
								mc.thePlayer.posX,
								mc.thePlayer.posY,
								mc.thePlayer.posZ,
								true
							)
						)
						PacketUtils.sendPacketNoEvent(
							C04PacketPlayerPosition(
								mc.thePlayer.posX,
								mc.thePlayer.posY - 11.725,
								mc.thePlayer.posZ,
								false
							)
						)
						PacketUtils.sendPacketNoEvent(
							C04PacketPlayerPosition(
								mc.thePlayer.posX,
								mc.thePlayer.posY,
								mc.thePlayer.posZ,
								true
							)
						)
					}
				}

				if (packet is S08PacketPlayerPosLook) {
					if (mc.thePlayer == null || mc.thePlayer.ticksExisted <= 0) return

					var x = packet.getX() - mc.thePlayer.posX
					var y = packet.getY() - mc.thePlayer.posY
					var z = packet.getZ() - mc.thePlayer.posZ
					var diff = sqrt(x * x + y * y + z * z)
					if (diff <= 8) {
						event.cancelEvent()
						PacketUtils.sendPacketNoEvent(
							C06PacketPlayerPosLook(
								packet.getX(),
								packet.getY(),
								packet.getZ(),
								packet.getYaw(),
								packet.getPitch(),
								true
							)
						)

						debug("silent s08 accept")
					}
				}

				if (packet is C0FPacketConfirmTransaction && !isInventory(packet.uid)) {
					repeat(4) {
						packetQueue.add(packet)
					}
					event.cancelEvent()
					debug("c0f dupe: 4x")
				}
			}

			"blocksmc" -> {
				if (!shouldRun()) {
					queueBus.clear()
					return
				}

				if (packet is C0BPacketEntityAction) {
					event.cancelEvent()
					debug("cancel action")
				}

				if (packet is S08PacketPlayerPosLook) {
					if (mc.thePlayer.getDistance(packet.x, packet.y, packet.z) < 8) {
						PacketUtils.sendPacketNoEvent(
							C06PacketPlayerPosLook(
								packet.x,
								packet.y,
								packet.z,
								packet.yaw,
								packet.pitch,
								false
							)
						)
						event.cancelEvent()
						debug("silent flag")
					}
				}

				if (packet is C00PacketKeepAlive || (packet is C0FPacketConfirmTransaction && !isInventory(packet.uid))) {
					queueBus.add(packet as Packet<INetHandlerPlayServer>)
					event.cancelEvent()

					debug("c0f or c00, ${queueBus.size}")

					if (queueBus.size > 300) {
						PacketUtils.sendPacketNoEvent(queueBus.poll())
						debug("poll")
					}
				}

				if (packet is C03PacketPlayer) {
					if (mc.thePlayer.ticksExisted % 20 == 0) {
						PacketUtils.sendPacketNoEvent(C0CPacketInput(0.98f, 0.98f, false, false))
						debug("c18 and c0c")
					}

					if (mc.thePlayer.ticksExisted % 45 == 0) {
						packet.y = -0.015625
						packet.onGround = false
						packet.isMoving = false
						debug("flag packet")
					}
				}
			}

			"flag" -> {
				if (packet is C03PacketPlayer && flagMode.get().equals(
						"edit",
						true
					) && mc.thePlayer.ticksExisted > 0 && mc.thePlayer.ticksExisted % flagTick.get() == 0
				) {
					packet.isMoving = false
					packet.onGround = false
					packet.y = -0.08

					debug("flagged")
				}
				if (packet is S08PacketPlayerPosLook && flagSilent.get()) {
					if (mc.thePlayer == null || mc.thePlayer.ticksExisted <= 0) return

					var x = packet.getX() - mc.thePlayer.posX
					var y = packet.getY() - mc.thePlayer.posY
					var z = packet.getZ() - mc.thePlayer.posZ
					var diff = sqrt(x * x + y * y + z * z)
					if (diff <= 8) {
						event.cancelEvent()
						PacketUtils.sendPacketNoEvent(
							C06PacketPlayerPosLook(
								packet.getX(),
								packet.getY(),
								packet.getZ(),
								packet.getYaw(),
								packet.getPitch(),
								true
							)
						)

						debug("silent s08 accept")
					}
				}
			}

			"pingspoof" -> {
				if (packet is C0FPacketConfirmTransaction && !isInventory(packet.uid)) {
					queueBus.add(packet)
					event.cancelEvent()

					debug("c0f added, action id ${packet.uid}, target id ${packet.windowId}")
				}
				if (packet is C00PacketKeepAlive) {
					queueBus.add(packet)
					event.cancelEvent()

					debug("c00 added, key ${packet.key}")
				}
			}

			"matrix" -> {
				if (matrixNoCheck.get() || LiquidBounce.moduleManager.getModule(Speed::class.java)!!.state || LiquidBounce.moduleManager.getModule(
						Fly::class.java
					)!!.state
				) {
					if (packet is C03PacketPlayer) {
						if (matrixNoMovePacket.get() && !packet.isMoving) {
							event.cancelEvent()
							debug("no move, cancelled")
							return
						}
						if (matrixOldMoveFix.get()) {
							// almost completely disable strafe check, nofall
							packet.onGround = true
							if (!packet.rotating) { // fix fly sometimes doesn't land properly since most mc servers all refer to C04, C05, C06 as C03s aka. PacketPlayerInFlying.
								packet.rotating = true
								packet.yaw = mc.thePlayer.rotationYaw
								packet.pitch = mc.thePlayer.rotationPitch
							}
						}
						if (matrixNewMoveFix.get()) {
							if (packet is C06PacketPlayerPosLook && pendingFlagApplyPacket) {
								pendingFlagApplyPacket = false;
								mc.thePlayer.motionX = lastMotionX;
								mc.thePlayer.motionY = lastMotionY;
								mc.thePlayer.motionZ = lastMotionZ;
							} else if (packet is S08PacketPlayerPosLook) {
								pendingFlagApplyPacket = true
								lastMotionX = mc.thePlayer.motionX;
								lastMotionY = mc.thePlayer.motionY;
								lastMotionZ = mc.thePlayer.motionZ;
							}
						}
					}
				}
			}

			"watchdog" -> {
				if (mc.isSingleplayer()) return

				if (autoAlert.get() && packet is S02PacketChat && packet.getChatComponent().getUnformattedText()
						.contains("Cages opened!", true)
				)
					LiquidBounce.hud.addNotification(
						Notification(
							"Disabler",
							"Speed is bannable until this notification disappears.",
							NotifyType.SUCCESS,
							20000
						)
					)

				if (testFeature.get() && !ServerUtils.isHypixelLobby()) {
					if (packet is C0FPacketConfirmTransaction && (!checkValid.get() || !isInventory(packet.uid))) {
						event.cancelEvent()
						packetQueue.add(packet)

						debug("c0f, ${packet.uid} ID, ${packet.windowId} wID")

						if (!shouldActive) {
							shouldActive = true
							debug("activated")
							when (waitingDisplayMode.get().lowercase()) {
								"notification" -> LiquidBounce.hud.addNotification(
									Notification(
										"Disabler",
										"Activated Disabler.",
										NotifyType.SUCCESS,
										2000
									)
								)

								"chat" -> debug("Activated Disabler.", true)
							}
						}
					}
					if (packet is C00PacketKeepAlive) {
						event.cancelEvent()
						anotherQueue.add(packet)

						wdTimer.reset()
						debug("c00, ${packet.key}")
					}
					if (packet is C03PacketPlayer || packet is C0BPacketEntityAction || packet is C08PacketPlayerBlockPlacement || packet is C0APacketAnimation) {
						if (!shouldActive)
							event.cancelEvent()
					}
					if (packet is S08PacketPlayerPosLook && !shouldActive) {
						if (alrSendY) {
							//mc.netHandler.addToSendQueue(C06PacketPlayerPosLook(packet.getX(), packet.getY(), packet.getZ(), packet.getYaw(), packet.getPitch(), false))
							event.cancelEvent()
							debug("no s08")
						} else {
							alrSendY = true
							debug("first s08, ignore")
						}
					}
				}

				if (noC03s.get() && packet is C03PacketPlayer) {
					if (packet !is C04PacketPlayerPosition && packet !is C05PacketPlayerLook && packet !is C06PacketPlayerPosLook)
						event.cancelEvent()
				}
			}

			"rotdesync" -> {
				if (packet is S08PacketPlayerPosLook) {
					if (!mc.netHandler.doneLoadingTerrain) {
						debug("not loaded terrain yet")
						return
					}
					event.cancelEvent()
					PacketUtils.sendPacketNoEvent(C04PacketPlayerPosition(packet.x, packet.y, packet.z, false))
					mc.thePlayer.setPosition(packet.x, packet.y, packet.z)
					debug("silent setback")
				}
			}

			"grim" -> {
				if (post.get()) {
					if (packet is C0EPacketClickWindow && c0e.get()) {
						if (!pre) {
							event.cancelEvent()
							packetsG.add(packet)
						}
					}
					if (packet is C0BPacketEntityAction && c0b.get()) {
						if (!pre) {
							event.cancelEvent()
							packetsG.add(packet)
						}
					}
					if (packet is C08PacketPlayerBlockPlacement && c08.get()) {
						if (!pre) {
							event.cancelEvent()
							packetsG.add(packet)
						}
					}
				}
			}
		}
	}

	fun flush(check: Boolean) {
		if ((if (check) psfSendMode.get() else psfStartSendMode.get()).equals("all", true))
			while (queueBus.size > 0) {
				PacketUtils.sendPacketNoEvent(queueBus.poll())
			}
		else
			PacketUtils.sendPacketNoEvent(queueBus.poll())
	}

	@EventTarget(priority = 2)
	fun onMotion(event: MotionEvent) {
		val killAura = LiquidBounce.moduleManager.getModule(KillAura::class.java)!! as KillAura
		val fly = LiquidBounce.moduleManager.getModule(Fly::class.java)!! as Fly
		val targetStrafe = LiquidBounce.moduleManager.getModule(TargetStrafe::class.java)!! as TargetStrafe

		if (event.eventState == EventState.PRE)
			shouldModifyRotation = false

		if (modeValue.get().equals("watchdog", true)) {
			if (event.eventState == EventState.PRE) {
				if ((speed.state || fly.state) && rotModify.get()) {
					shouldModifyRotation = true
					if (MovementUtils.isMoving()) {
						val cYaw = if (targetStrafe.canStrafe) MovementUtils.getPredictionYaw(event.x, event.z) - 90F
						else MovementUtils.getRawDirection(event.yaw)

						lastYaw = cYaw
						event.yaw = cYaw
						if (tifality90.get()) event.pitch = 90F
						RotationUtils.setTargetRotation(Rotation(cYaw, if (tifality90.get()) 90F else event.pitch))
					} else if (noMoveKeepRot.get()) {
						event.yaw = lastYaw
						if (tifality90.get()) event.pitch = 90F
						RotationUtils.setTargetRotation(Rotation(lastYaw, if (tifality90.get()) 90F else event.pitch))
					}
				}
				if (mc.isSingleplayer()) return
				if (testFeature.get() && !ServerUtils.isHypixelLobby()) {
					if (shouldActive && wdTimer.hasTimePassed(testDelay.get().toLong())) {
						while (!anotherQueue.isEmpty()) {
							PacketUtils.sendPacketNoEvent(anotherQueue.poll())
							debug("c00, ${anotherQueue.size}")
						}
						while (!packetQueue.isEmpty()) {
							PacketUtils.sendPacketNoEvent(packetQueue.poll())
							debug("c0f, ${packetQueue.size}")
						}
					}
				}
			}
		}

		if (event.eventState == EventState.POST && (!matrixMoveOnly.get() || isMoving())) // check post event
			if (modeValue.get().equals("matrix", true)) {
				if (matrixNoCheck.get() || LiquidBounce.moduleManager.getModule(Fly::class.java)!!.state || LiquidBounce.moduleManager.getModule(
						Speed::class.java
					)!!.state
				) {
					var changed = false
					if (matrixHotbarChange.get()) for (i in 0..8) {
						// find a empty inventory slot
						if (mc.thePlayer.inventory.mainInventory[i] == null && i != mc.thePlayer.inventory.currentItem) {
							PacketUtils.sendPacketNoEvent(C09PacketHeldItemChange(i))
							changed = true
							debug("found empty slot $i, switching")
							break
						}
					}

					PacketUtils.sendPacketNoEvent(
						C06PacketPlayerPosLook(
							mc.thePlayer.posX,
							mc.thePlayer.posY,
							mc.thePlayer.posZ,
							RotationUtils.serverRotation.yaw,
							RotationUtils.serverRotation.pitch,
							mc.thePlayer.onGround
						)
					)
					mc.netHandler.addToSendQueue(
						C08PacketPlayerBlockPlacement(
							BlockPos(-1, -1, -1),
							-1,
							null,
							0f,
							0f,
							0f
						)
					)
					debug("sent placement")

					if (changed) {
						PacketUtils.sendPacketNoEvent(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
						debug("switched back")
					}
				}
			}
		if (modeValue.get().equals("vulcan", true)) {
			if (event.eventState == EventState.PRE) {
				if (vulcanStrafe.get()) {
					if (mc.thePlayer.ticksExisted % 5 == 0) {
						mc.netHandler.addToSendQueue(
							C07PacketPlayerDigging(
								C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK,
								BlockPos(-1, -1, -1),
								EnumFacing.UP
							)
						)
					}
				}
			}
		}
		if (modeValue.get().equals("vulcan", ignoreCase = true)) {
			if (event.eventState == EventState.PRE) {
				if (vulcanStrafe2.get()) {
					mc.netHandler.addToSendQueue(
						C0BPacketEntityAction(
							mc.thePlayer,
							C0BPacketEntityAction.Action.START_SPRINTING
						)
					)
					mc.netHandler.addToSendQueue(
						C0BPacketEntityAction(
							mc.thePlayer,
							C0BPacketEntityAction.Action.STOP_SPRINTING
						)
					)
					if (mc.thePlayer.ticksExisted % 5 == 0) {
						mc.netHandler.addToSendQueue(
							C07PacketPlayerDigging(
								C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK,
								BlockPos(-1, -1, -1),
								EnumFacing.UP
							)
						)
					}
				}
			}
		}
		if(modeValue.isMode("Grim")) {
			if (post.get()) {
				pre = event.eventState == EventState.PRE
				if (event.eventState == EventState.PRE) {
					try {
						while (!packetsG.isEmpty()) {
							mc.netHandler!!.addToSendQueue(packetsG.take())
						}
					} catch (e: Exception) {
						e.printStackTrace()
					}
				}
			}
		}
	}

	@EventTarget
	fun onUpdate(event: UpdateEvent) {
		when (modeValue.get().toLowerCase()) {
			"spartancombat" -> {
				if (msTimer.hasTimePassed(3000L) && keepAlives.size > 0 && transactions.size > 0) {
					PacketUtils.sendPacketNoEvent(keepAlives[keepAlives.size - 1])
					PacketUtils.sendPacketNoEvent(transactions[transactions.size - 1])

					debug("c00 no.${keepAlives.size - 1} sent.")
					debug("c0f no.${transactions.size - 1} sent.")
					keepAlives.clear()
					transactions.clear()
					msTimer.reset()
				}
			}
			"oldverus" -> {
				if (mc.thePlayer.ticksExisted % 180 == 0) {
					while (packetQueue.size > 22) {
						PacketUtils.sendPacketNoEvent(packetQueue.poll())
					}
					debug("pushed queue until size < 22.")
				}
			}
			"latestverus" -> {
				if (verusAntiFlyCheck.get() && !shouldActive) {
					val flyMod = LiquidBounce.moduleManager[Fly::class.java]!!
					if (flyMod.state) {
						flyMod.state = false
						LiquidBounce.hud.addNotification(Notification("Disabler","You can't fly before successful activation.", NotifyType.ERROR))
						debug("no fly allowed")
					}
				}
				if (mc.thePlayer.ticksExisted % 15 == 0 && shouldRun()) {
					if (verusFakeInput.get()) {
						mc.netHandler.addToSendQueue(C0CPacketInput(mc.thePlayer.moveStrafing.coerceAtMost(0.98F), mc.thePlayer.moveForward.coerceAtMost(0.98F), mc.thePlayer.movementInput.jump, mc.thePlayer.movementInput.sneak))
						debug("c0c")
					}
				}
			}
			"pingspoof" -> {
				if (msTimer.hasTimePassed(psfWorldDelay.get().toLong()) && !shouldActive) {
					shouldActive = true
					sendDelay = RandomUtils.nextInt(minpsf.get(), maxpsf.get())
					if (queueBus.size > 0) flush(false)
					msTimer.reset()
					debug("activated. expected next delay: ${sendDelay}ms")
				}

				if (shouldActive) {
					if (msTimer.hasTimePassed(sendDelay.toLong()) && !queueBus.isEmpty()) {
						flush(true)
						sendDelay = RandomUtils.nextInt(minpsf.get(), maxpsf.get())
						msTimer.reset()
						debug("expected next delay: ${sendDelay}ms")
					}
				}
			}
			"flag" -> {
				if (flagMode.get().equals("packet", true) && mc.thePlayer.ticksExisted > 0 && mc.thePlayer.ticksExisted % flagTick.get() == 0) {
					PacketUtils.sendPacketNoEvent(C04PacketPlayerPosition(mc.thePlayer.posX, -0.08, mc.thePlayer.posZ, mc.thePlayer.onGround))
					debug("flagged")
				}
			}
		}
	}
}
