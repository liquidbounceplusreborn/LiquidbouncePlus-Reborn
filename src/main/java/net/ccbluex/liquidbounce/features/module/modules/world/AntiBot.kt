/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils.stripColor
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.play.server.*
import net.minecraft.world.WorldSettings
import java.util.*
import java.util.function.Consumer

@ModuleInfo(
    name = "AntiBot",
    spacedName = "Anti Bot",
    description = "Prevents KillAura from attacking AntiCheat bots.",
    category = ModuleCategory.WORLD
)
class AntiBot : Module() {
    private val czechHekValue = BoolValue("CzechMatrix", false)
    private val czechHekPingCheckValue = BoolValue("PingCheck", true) { czechHekValue.get() }
    private val czechHekGMCheckValue = BoolValue("GamemodeCheck", true) { czechHekValue.get() }
    private val matrixIllegalNameValue = BoolValue("MatrixIllegalName", false)
    private val tabValue = BoolValue("Tab", true)
    private val tabModeValue = ListValue("TabMode", arrayOf("Equals", "Contains"), "Contains")
    private val entityIDValue = BoolValue("EntityID", true)
    private val colorValue = BoolValue("Color", false)
    private val livingTimeValue = BoolValue("LivingTime", false)
    private val livingTimeTicksValue = IntegerValue("LivingTimeTicks", 40, 1, 200)
    private val groundValue = BoolValue("Ground", true)
    private val airValue = BoolValue("Air", false)
    private val invalidGroundValue = BoolValue("InvalidGround", true)
    private val swingValue = BoolValue("Swing", false)
    private val healthValue = BoolValue("Health", false)
    private val invalidHealthValue = BoolValue("InvalidHealth", false)
    private val minHealthValue = FloatValue("MinHealth", 0f, 0f, 100f)
    private val maxHealthValue = FloatValue("MaxHealth", 20f, 0f, 100f)
    private val derpValue = BoolValue("Derp", true)
    private val wasInvisibleValue = BoolValue("WasInvisible", false)
    private val validNameValue = BoolValue("ValidName", true)
    private val hiddenNameValue = BoolValue("HiddenName", false)
    private val armorValue = BoolValue("Armor", false)
    private val pingValue = BoolValue("Ping", false)
    private val needHitValue = BoolValue("NeedHit", false)
    private val experimentalNPCDetection = BoolValue("ExperimentalNPCDetection", false)
    private val illegalName = BoolValue("IllegalName", false)
    private val reusedEntityIdValue = BoolValue("ReusedEntityId", false)
    private val spawnInCombatValue = BoolValue("SpawnInCombat", false)
    private val duplicateInWorldValue = BoolValue("DuplicateInWorld", false)
    private val duplicateInTabValue = BoolValue("DuplicateInTab", false)
    private val duplicateCompareModeValue = ListValue("DuplicateCompareMode", arrayOf("OnTime", "WhenSpawn"), "OnTime") { duplicateInTabValue.get() || duplicateInWorldValue.get() }
    private val removeFromWorld = BoolValue("RemoveFromWorld", false)
    private val removeIntervalValue = IntegerValue("Remove-Interval", 20, 1, 100, " tick")
    private val fastDamageValue = BoolValue("FastDamage", false)
    private val fastDamageTicksValue = IntegerValue("FastDamageTicks", 5, 1, 20) { fastDamageValue.get() }
    private val debugValue = BoolValue("Debug", false)
    private val ground: MutableList<Int> = ArrayList()
    private val air: MutableList<Int> = ArrayList()
    private val invalidGround: MutableMap<Int, Int> = HashMap()
    private val swing: MutableList<Int> = ArrayList()
    private val invisible: MutableList<Int> = ArrayList()
    private val hitted: MutableList<Int> = ArrayList()
    private var wasAdded = mc.thePlayer != null
    private val regex = Regex("\\w{3,16}")
    private val hasRemovedEntities = mutableListOf<Int>()
    private val spawnInCombat = mutableListOf<Int>()
    private val lastDamage = mutableMapOf<Int, Int>()
    private val lastDamageVl = mutableMapOf<Int, Float>()
    private val duplicate = mutableListOf<UUID>()

    override fun onDisable() {
        clearAll()
        super.onDisable()
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent?) {
        if (mc.thePlayer == null || mc.theWorld == null) return
        if (removeFromWorld.get() && mc.thePlayer.ticksExisted > 0 && mc.thePlayer.ticksExisted % removeIntervalValue.get() == 0) {
            val ent: MutableList<EntityPlayer> = ArrayList()
            for (entity in mc.theWorld.playerEntities) {
                if (entity !== mc.thePlayer && isBot(entity)) ent.add(entity)
            }
            if (ent.isEmpty()) return
            for (e in ent) {
                mc.theWorld.removeEntity(e)
                if (debugValue.get()) ClientUtils.displayChatMessage("§7[§a§lAnti Bot§7] §fRemoved §r" + e.name + " §fdue to it being a bot.")
            }
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (mc.thePlayer == null || mc.theWorld == null) return
        val packet = event.packet
        if (czechHekValue.get()) {
            if (packet is S41PacketServerDifficulty) wasAdded = false
            if (packet is S38PacketPlayerListItem) {
                val packetListItem = event.packet as S38PacketPlayerListItem
                val data = packetListItem.entries[0]
                if (data.profile != null && data.profile.name != null) {
                    if (!wasAdded) wasAdded =
                        data.profile.name == mc.thePlayer.name else if (!mc.thePlayer.isSpectator && !mc.thePlayer.capabilities.allowFlying && (!czechHekPingCheckValue.get() || data.ping != 0) && (!czechHekGMCheckValue.get() || data.gameMode != WorldSettings.GameType.NOT_SET)) {
                        event.cancelEvent()
                        if (debugValue.get()) ClientUtils.displayChatMessage("§7[§a§lAnti Bot/§6Matrix§7] §fPrevented §r" + data.profile.name + " §ffrom spawning.")
                    }
                }
            }
        }
        if (matrixIllegalNameValue.get()) {
            if (packet is S38PacketPlayerListItem) {
                val entityName = packet.entries[0].profile.name
                mc.theWorld.loadedEntityList.forEach(Consumer { entity: Entity ->
                    if (entity is EntityPlayer) {
                        if (entity.getName() == entityName) {
                            if (debugValue.get()) ClientUtils.displayChatMessage("§7[§a§lAnti Bot/§6Matrix§7] §fPrevented §r$entityName §ffrom spawning.")
                            event.cancelEvent()
                        }
                    }
                })
            }
        }
        if (packet is S14PacketEntity) {
            val packetEntity = event.packet as S14PacketEntity
            val entity = packetEntity.getEntity(mc.theWorld)
            if (entity is EntityPlayer) {
                if (packetEntity.onGround && !ground.contains(entity.getEntityId())) ground.add(entity.getEntityId())
                if (!packetEntity.onGround && !air.contains(entity.getEntityId())) air.add(entity.getEntityId())
                if (packetEntity.onGround) {
                    if (entity.prevPosY != entity.posY) invalidGround[entity.getEntityId()] =
                        invalidGround.getOrDefault(entity.getEntityId(), 0) + 1
                } else {
                    val currentVL = invalidGround.getOrDefault(entity.getEntityId(), 0) / 2
                    if (currentVL <= 0) invalidGround.remove(entity.getEntityId()) else invalidGround[entity.getEntityId()] =
                        currentVL
                }
                if (entity.isInvisible() && !invisible.contains(entity.getEntityId())) invisible.add(entity.getEntityId())
            }
        }

        if (packet is S0BPacketAnimation) {
            val entity = mc.theWorld.getEntityByID(packet.entityID)

            if (entity != null && entity is EntityLivingBase && packet.animationType == 0 &&
                !swing.contains(entity.entityId)) {
                swing.add(entity.entityId)
            }
        } else if (packet is S38PacketPlayerListItem) {
            if (duplicateCompareModeValue.equals("WhenSpawn") && packet.action == S38PacketPlayerListItem.Action.ADD_PLAYER) {
                packet.entries.forEach { entry ->
                    val name = entry.profile.name
                    if (duplicateInWorldValue.get() && mc.theWorld.playerEntities.any { it.name == name } ||
                        duplicateInTabValue.get() && mc.netHandler.playerInfoMap.any { it.gameProfile.name == name }) {
                        duplicate.add(entry.profile.id)
                    }
                }
            }
        } else if (packet is S0CPacketSpawnPlayer) {
            if(LiquidBounce.combatManager.inCombat && !hasRemovedEntities.contains(packet.entityID)) {
                spawnInCombat.add(packet.entityID)
            }
        } else if (packet is S13PacketDestroyEntities) {
            hasRemovedEntities.addAll(packet.entityIDs.toTypedArray())
        }
        if (packet is S19PacketEntityStatus && packet.opCode.toInt() == 2 || packet is S0BPacketAnimation && packet.animationType == 1) {
            val entity = if (packet is S19PacketEntityStatus) { packet.getEntity(mc.theWorld) } else if (packet is S0BPacketAnimation) { mc.theWorld.getEntityByID(packet.entityID) } else { null } ?: return

            if (entity is EntityPlayer) {
                lastDamageVl[entity.entityId] = lastDamageVl.getOrDefault(entity.entityId, 0f) + if (entity.ticksExisted - lastDamage.getOrDefault(entity.entityId, 0) <= fastDamageTicksValue.get()) {
                    1f
                } else {
                    -0.5f
                }
                lastDamage[entity.entityId] = entity.ticksExisted
            }
        }
    }

    @EventTarget
    fun onAttack(e: AttackEvent) {
        val entity = e.targetEntity
        if (entity is EntityLivingBase && !hitted.contains(entity.getEntityId())) hitted.add(entity.getEntityId())
    }

    @EventTarget
    fun onWorld(event: WorldEvent?) {
        clearAll()
    }

    private fun clearAll() {
        hitted.clear()
        swing.clear()
        ground.clear()
        invalidGround.clear()
        invisible.clear()
        hasRemovedEntities.clear()
        spawnInCombat.clear()
        lastDamage.clear()
        lastDamageVl.clear()
        duplicate.clear()
    }

    companion object {
        fun isBot(entity: EntityLivingBase): Boolean {
            if (entity !is EntityPlayer || entity === mc.thePlayer) return false
            val antiBot = LiquidBounce.moduleManager.getModule(AntiBot::class.java)
            if (antiBot == null || !antiBot.state) return false
            if (antiBot.experimentalNPCDetection.get() && (entity.getDisplayName().unformattedText.lowercase(Locale.getDefault())
                    .contains("npc") || entity.getDisplayName().unformattedText.lowercase(
                    Locale.getDefault()
                ).contains("cit-"))
            ) return true
            if (antiBot.illegalName.get() && (entity.getName()
                    .contains(" ") || entity.getDisplayName().unformattedText.contains(" "))
            ) return true
            if (antiBot.colorValue.get() && !entity.getDisplayName().formattedText
                    .replace("§r", "").contains("§")
            ) return true
            if (antiBot.livingTimeValue.get() && entity.ticksExisted < antiBot.livingTimeTicksValue.get()) return true
            if (antiBot.groundValue.get() && !antiBot.ground.contains(entity.getEntityId())) return true
            if (antiBot.airValue.get() && !antiBot.air.contains(entity.getEntityId())) return true
            if (antiBot.swingValue.get() && !antiBot.swing.contains(entity.getEntityId())) return true
            if (antiBot.invalidHealthValue.get() && entity.getHealth().isNaN()) return true
            if (antiBot.healthValue.get() && (entity.getHealth() > antiBot.maxHealthValue.get() || entity.getHealth() < antiBot.minHealthValue.get())) return true
            if (antiBot.entityIDValue.get() && (entity.getEntityId() >= 1000000000 || entity.getEntityId() <= -1)) return true
            if (antiBot.derpValue.get() && (entity.rotationPitch > 90f || entity.rotationPitch < -90f)) return true
            if (antiBot.wasInvisibleValue.get() && antiBot.invisible.contains(entity.getEntityId())) return true
            if (antiBot.armorValue.get()) {
                val player = entity
                if (player.inventory.armorInventory[0] == null && player.inventory.armorInventory[1] == null && player.inventory.armorInventory[2] == null && player.inventory.armorInventory[3] == null) return true
            }
            if (antiBot.pingValue.get()) {
                val player = entity
                if (mc.netHandler.getPlayerInfo(player.uniqueID) != null && mc.netHandler.getPlayerInfo(player.uniqueID).responseTime == 0) return true
            }
            if (antiBot.needHitValue.get() && !antiBot.hitted.contains(entity.getEntityId())) return true
            if (antiBot.invalidGroundValue.get() && antiBot.invalidGround.getOrDefault(
                    entity.getEntityId(),
                    0
                ) >= 10
            ) return true
            if (antiBot.tabValue.get()) {
                val equals = antiBot.tabModeValue.get().equals("Equals", ignoreCase = true)
                val targetName = stripColor(entity.getDisplayName().formattedText)
                if (targetName != null) {
                    for (networkPlayerInfo in mc.netHandler.playerInfoMap) {
                        val networkName = stripColor(EntityUtils.getName(networkPlayerInfo))
                            ?: continue
                        if (if (equals) targetName == networkName else targetName.contains(networkName)) return false
                    }
                    return true
                }
            }
            if (antiBot.validNameValue.get() && !entity.name.matches(antiBot.regex)) {
                return true
            }
            if (antiBot.hiddenNameValue.get() && ( entity.getName().contains("\u00A7") || (entity.hasCustomName() && entity.getCustomNameTag().contains(entity.getName()) ))){
                return true
            }
            if(antiBot.reusedEntityIdValue.get() && antiBot.hasRemovedEntities.contains(entity.entityId)) {
                return false
            }
            if (antiBot.spawnInCombatValue.get() && antiBot.spawnInCombat.contains(entity.entityId)) {
                return true
            }
            if (antiBot.fastDamageValue.get() && antiBot.lastDamageVl.getOrDefault(entity.entityId, 0f) > 0) {
                return true
            }

            if (antiBot.duplicateInWorldValue.get() && antiBot.duplicateCompareModeValue.equals("OnTime") && mc.theWorld.loadedEntityList.count { it is EntityPlayer && it.name == it.name } > 1) {
                return true
            }

            if (antiBot.duplicateInTabValue.get() && antiBot.duplicateCompareModeValue.equals("OnTime") && mc.netHandler.playerInfoMap.count { entity.name == it.gameProfile.name } > 1) {
                return true
            }

            if (antiBot.duplicateCompareModeValue.equals("WhenSpawn") && antiBot.duplicate.contains(entity.gameProfile.id)) {
                return true
            }

            return entity.name.isEmpty() || entity.name == mc.thePlayer.name
        }
    }
}