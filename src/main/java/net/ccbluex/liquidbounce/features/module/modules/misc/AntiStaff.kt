package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.NotifyType
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.utils.ServerUtils
import net.ccbluex.liquidbounce.utils.misc.HttpUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.minecraft.entity.Entity
import net.minecraft.network.play.server.*
import kotlin.concurrent.thread

@ModuleInfo(
    name = "AntiStaff",
    spacedName = "Anti Staff",
    description = "Anti staff on BlocksMC. Automatically leaves a map if detected known staffs.",
    category = ModuleCategory.MISC
)
class AntiStaff : Module() {
    private var staffs = mutableListOf<String>()
    private var mushmcstaffs = mutableListOf<String>()
    private var hypixelstaffs = mutableListOf<String>()
    private var gommehdstaffs = mutableListOf<String>()
    private var gamsterstaffs = mutableListOf<String>()


    private var staffsInWorld = mutableListOf<String>()

    private var bmcStaffList: String = "${LiquidBounce.CLIENT_CLOUD}/staffs.txt"
    private var mushStaffList: String = "${LiquidBounce.CLIENT_CLOUD}/mushstaffs.txt"
    private var hypixelStaffList: String = "${LiquidBounce.CLIENT_CLOUD}/hypixelstaffs.txt"
    private var gommeHDStaffList: String = "${LiquidBounce.CLIENT_CLOUD}/gommehdstaffs.txt"
    private var gamsterStaffList: String = "${LiquidBounce.CLIENT_CLOUD}/gamsterstaffs.txt"



    private val notify = BoolValue("Notify", true)
    private val chat = BoolValue("Chat", true)
    private val leave = BoolValue("Leave", true)

    private val onBMC: Boolean
        get() = ServerUtils.serverData.serverIP.contains("blocksmc.com")

    private val onMushMC: Boolean
        get() = !mc.isSingleplayer && ServerUtils.serverData != null && ServerUtils.serverData.serverIP.contains("jogar.mush.com.br") || ServerUtils.serverData.serverIP.contains("mush.com.br")
    private val onHypixel: Boolean
        get() = !mc.isSingleplayer && ServerUtils.serverData != null && ServerUtils.serverData.serverIP.contains("hypixel.net")
    private val onGommeHD: Boolean
        get() = !mc.isSingleplayer && ServerUtils.serverData != null && ServerUtils.serverData.serverIP.contains("GoomeHD.net")
    private val onGamster: Boolean
        get() = !mc.isSingleplayer && ServerUtils.serverData != null && ServerUtils.serverData.serverIP.contains("mc.gamster.org")



    override fun onInitialize() {
        thread {
            staffs.addAll(HttpUtils.get(bmcStaffList).split(","))
            mushmcstaffs.addAll(HttpUtils.get(mushStaffList).split(","))
            gamsterstaffs.addAll(HttpUtils.get(gamsterStaffList).split(","))
            hypixelstaffs.addAll(HttpUtils.get(hypixelStaffList).split(","))
            gommehdstaffs.addAll(HttpUtils.get(gommeHDStaffList).split(","))

            ClientUtils.getLogger().info("[Staff/main] $staffs")
        }
    }

    override fun onEnable() {
        staffsInWorld.clear()
    }

    @EventTarget
    fun onWorld(e: WorldEvent) {
        staffsInWorld.clear()
    }

    private fun warn(name: String) {
        if (name in staffsInWorld)
            return

        val msg = if (leave.get()) ", leaving" else ""
        if (chat.get())
            chat("[AntiStaff] Detected staff: $name$msg")
        if (notify.get())
            LiquidBounce.hud.addNotification(
                Notification(
                    "AntiStaff",
                    "Detected staff: $name$msg",
                    NotifyType.ERROR,
                    4000
                )
            )
        if (leave.get())
            mc.thePlayer.sendChatMessage("/leave")

        staffsInWorld.add(name)
    }

    private fun isStaff(entity: Entity): Boolean {
        if (onBMC) {
            return entity.name in staffs || entity.displayName.unformattedText in staffs
        } else if (onMushMC) {
            return entity.name in mushmcstaffs || entity.displayName.unformattedText in mushmcstaffs
        } else if (onHypixel) {
            return entity.name in hypixelstaffs || entity.displayName.unformattedText in hypixelstaffs
        } else if (onGamster) {
            return entity.name in gamsterstaffs || entity.displayName.unformattedText in gamsterstaffs
        } else if (onGommeHD) {
            return entity.name in gommehdstaffs || entity.displayName.unformattedText in gommehdstaffs

        }

        // Handle the default case if neither onBMC nor onMushMC is true
         return false
    }


    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (mc.theWorld == null || mc.thePlayer == null) return
        if (onBMC || onMushMC || onHypixel || onGommeHD || onGamster) {
            when (val packet = event.packet) {
                is S0CPacketSpawnPlayer -> {
                    val entity = mc.theWorld.getEntityByID(packet.entityID) ?: return
                    if (isStaff(entity))
                        warn(entity.name)
                }

                is S1EPacketRemoveEntityEffect -> {
                    val entity = mc.theWorld.getEntityByID(packet.entityId) ?: return
                    if (isStaff(entity))
                        warn(entity.name)
                }

                is S01PacketJoinGame -> {
                    val entity = mc.theWorld.getEntityByID(packet.entityId) ?: return
                    if (isStaff(entity))
                        warn(entity.name)
                }

                is S04PacketEntityEquipment -> {
                    val entity = mc.theWorld.getEntityByID(packet.entityID) ?: return
                    if (isStaff(entity))
                        warn(entity.name)
                }

                is S1CPacketEntityMetadata -> {
                    val entity = mc.theWorld.getEntityByID(packet.entityId) ?: return
                    if (isStaff(entity))
                        warn(entity.name)
                }

                is S1DPacketEntityEffect -> {
                    val entity = mc.theWorld.getEntityByID(packet.entityId) ?: return
                    if (isStaff(entity))
                        warn(entity.name)
                }

                is S18PacketEntityTeleport -> {
                    val entity = mc.theWorld.getEntityByID(packet.entityId) ?: return
                    if (isStaff(entity))
                        warn(entity.name)
                }

                is S20PacketEntityProperties -> {
                    val entity = mc.theWorld.getEntityByID(packet.entityId) ?: return
                    if (isStaff(entity))
                        warn(entity.name)
                }

                is S0BPacketAnimation -> {
                    val entity = mc.theWorld.getEntityByID(packet.entityID) ?: return
                    if (isStaff(entity))
                        warn(entity.name)
                }

                is S14PacketEntity -> {
                    val entity = packet.getEntity(mc.theWorld) ?: return
                    if (isStaff(entity))
                        warn(entity.name)
                }

                is S19PacketEntityStatus -> {
                    val entity = packet.getEntity(mc.theWorld) ?: return
                    if (isStaff(entity))
                        warn(entity.name)
                }

                is S19PacketEntityHeadLook -> {
                    val entity = packet.getEntity(mc.theWorld) ?: return
                    if (isStaff(entity))
                        warn(entity.name)
                }

                is S49PacketUpdateEntityNBT -> {
                    val entity = packet.getEntity(mc.theWorld) ?: return
                    if (isStaff(entity))
                        warn(entity.name)
                }
            }
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (mc.theWorld == null || mc.thePlayer == null || !onBMC) return

        mc.netHandler.playerInfoMap.forEach {
            val networkName = ColorUtils.stripColor(EntityUtils.getName(it))!!.split(" ")[0]
            if (networkName in staffs)
                warn(networkName)
        }

        mc.theWorld.loadedEntityList.forEach {
            if (it.name in staffs)
                warn(it.name)
        }
    }
}
