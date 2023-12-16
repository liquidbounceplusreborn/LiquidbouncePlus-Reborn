package net.ccbluex.liquidbounce.features.module.modules.combat

import cc.paimonmc.viamcp.utils.AttackOrder
import com.viaversion.viaversion.api.Via
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper
import com.viaversion.viaversion.api.type.Type
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.Protocol1_8TO1_9
import de.gerrygames.viarewind.utils.PacketUtil
import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.misc.AntiBot
import net.ccbluex.liquidbounce.features.module.modules.misc.ViaVersionFix
import net.ccbluex.liquidbounce.features.module.modules.player.Blink
import net.ccbluex.liquidbounce.features.module.modules.render.FreeCam
import net.ccbluex.liquidbounce.features.module.modules.world.Scaffold
import net.ccbluex.liquidbounce.features.module.modules.world.Teams
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.ccbluex.liquidbounce.utils.Rotation
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.ccbluex.liquidbounce.utils.extensions.getNearestPointBB
import net.ccbluex.liquidbounce.utils.extensions.hitBox
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemSword
import net.minecraft.network.handshake.client.C00Handshake
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.client.C09PacketHeldItemChange
import net.minecraft.network.play.server.S45PacketTitle
import net.minecraft.potion.Potion
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import java.util.*

@ModuleInfo(
    name = "KillAura",
    category = ModuleCategory.COMBAT,
    description = "Auto-attacks entities"
)
class KillAura : Module() {
    val range = FloatValue("Range", 4.0f, 2.0f, 10.0f)
    val apsValue = IntegerValue("APS", 8, 1, 20)
    val rotate = BoolValue("Rotate",true)
    val rotationMode = ListValue("RotateMode", arrayOf("Normal", "LiquidBounce","NearestPoint"), "LiquidBounce"){rotate.get()}

    private val priority by ListValue(
        "Priority",
        arrayOf(
            "Health",
            "Distance",
            "Direction",
            "LivingTime",
            "Armor",
            "HurtResistance",
            "HurtTime",
            "HealthAbsorption",
            "RegenAmplifier"
        ),
        "Distance"
    )
    val autoBlockMode by ListValue("AutoBlock", arrayOf("None", "Vanilla"), "None")
    val verusAutoBlockValue by BoolValue("VerusAutoBlock", false) { autoBlockMode == "Vanilla" }
    val jitter = BoolValue("Jitter", true)
    val jitterStrengthYaw = FloatValue("JitterStrengthYaw", 10.0f, 0.0f, 20.0f) { jitter.get() }
    val jitterStrengthPitch = FloatValue("JitterStrengthPitch", 10.0f, 0.0f, 20.0f) { jitter.get() }
    private val noInvAttack by BoolValue("NoInvAttack", false)
    private val noBlink by BoolValue("NoBlink", true)
    private val noScaff by BoolValue("NoScaffold", true)

    var rotations: Rotation? = null
    var target: EntityLivingBase? = null
    var blockingStatus = false
    var verusBlocking = false

    private val timerAttack: MSTimer = MSTimer()

    override fun onDisable() {
        target = null
        timerAttack.reset()
        stopBlocking()
        if (verusBlocking && !blockingStatus && !mc.thePlayer.isBlocking) {
            verusBlocking = false
            if (verusAutoBlockValue)
                PacketUtils.sendPacketNoEvent(
                    C07PacketPlayerDigging(
                        C07PacketPlayerDigging.Action.RELEASE_USE_ITEM,
                        BlockPos.ORIGIN,
                        EnumFacing.DOWN
                    )
                )
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (cancelRun)
            return
        val packet = event.packet
        if (verusBlocking
            && ((packet is C07PacketPlayerDigging
                    && packet.status == C07PacketPlayerDigging.Action.RELEASE_USE_ITEM)
                    || packet is C08PacketPlayerBlockPlacement)
            && verusAutoBlockValue
        )
            event.cancelEvent()

        if (packet is C09PacketHeldItemChange)
            verusBlocking = false
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (cancelRun)
            return
        if (blockingStatus || mc.thePlayer.isBlocking)
            verusBlocking = true
        else if (verusBlocking) {
            verusBlocking = false
            if (verusAutoBlockValue)
                PacketUtils.sendPacketNoEvent(
                    C07PacketPlayerDigging(
                        C07PacketPlayerDigging.Action.RELEASE_USE_ITEM,
                        BlockPos.ORIGIN,
                        EnumFacing.DOWN
                    )
                )
        }
        updateTarget()
        if (target != null) {

            if(rotate.get())
            rotate(target!!)

            if (mc.thePlayer.isBlocking || blockingStatus)
                stopBlocking()
            if (timerAttack.hasTimePassed(1000L / (apsValue.get() + 2))) {
                AttackOrder.sendFixedAttack(mc.thePlayer, target!!)
                timerAttack.reset()
            }
            if (autoBlockMode == "Vanilla" && canBlock) {
                startBlocking()
            }
        }
    }

    private fun rotate(entity:Entity) {
        var boundingBox = entity.hitBox

        when (rotationMode.get()) {
            "Normal" -> rotations = RotationUtils.getAngles(entity)!!
            "LiquidBounce" -> {
                rotations = RotationUtils.searchCenter(
                    boundingBox,
                    false,
                    false,
                    false,
                    true,
                    range.get(),
                    0f,
                    false
                )
            }
            "NearestPoint" -> {
                rotations = RotationUtils.OtherRotation(
                    boundingBox,
                    getNearestPointBB(mc.thePlayer.getPositionEyes(1f), entity.entityBoundingBox),
                    false,
                    true,
                    range.get()
                )
            }
        }

        val random = Random()
        val jitterYaw: Float = (random.nextFloat() * 2 - 1) * jitterStrengthYaw.get()
        val jitterPitch: Float = (random.nextFloat() * 2 - 1) * jitterStrengthPitch.get()
        rotations?.yaw = rotations?.yaw?.plus(if (jitter.get()) jitterYaw else 0f)!!
        rotations?.pitch = rotations?.pitch?.plus(if (jitter.get()) jitterPitch else 0f)!!

        RotationUtils.setTargetRotation(rotations!!)
    }

    private fun updateTarget() {

        // Find possible targets
        val targets = mutableListOf<EntityLivingBase>()

        for (entity in mc.theWorld.loadedEntityList) {
            if (entity !is EntityLivingBase || !isEnemy(entity))
                continue

            val distance = mc.thePlayer.getDistanceToEntityBox(entity)

            if (distance <= range.get())
                targets.add(entity)
        }

        // Sort targets by priority
        when (priority.lowercase(Locale.getDefault())) {
            "distance" -> targets.sortBy { mc.thePlayer.getDistanceToEntityBox(it) } // Sort by distance
            "health" -> targets.sortBy { it.health } // Sort by health
            "direction" -> targets.sortBy { RotationUtils.getRotationDifference(it) } // Sort by FOV
            "livingtime" -> targets.sortBy { -it.ticksExisted } // Sort by existence
            "hurtresistance" -> targets.sortBy { it.hurtResistantTime } // Sort by armor hurt time
            "hurttime" -> targets.sortBy { it.hurtTime } // Sort by hurt time
            "healthabsorption" -> targets.sortBy { it.health + it.absorptionAmount } // Sort by full health with absorption effect
            "regenamplifier" -> targets.sortBy { if (it.isPotionActive(Potion.regeneration)) it.getActivePotionEffect(
                Potion.regeneration).amplifier else -1 }
        }

        var found = false

        // Find best target
        for (entity in targets) {
            // Set target to current entity
            target = entity
            found = true
            break
        }


        if (!found) {
            target = null
        }
    }

    private fun isEnemy(entity: Entity?): Boolean {
        if (entity is EntityLivingBase && (EntityUtils.targetDead || isAlive(entity)) && entity != mc.thePlayer) {
            if (!EntityUtils.targetInvisible && entity.isInvisible())
                return false

            if (EntityUtils.targetPlayer && entity is EntityPlayer) {
                if (entity.isSpectator || AntiBot.isBot(entity))
                    return false

                if (EntityUtils.isFriend(entity) && !LiquidBounce.moduleManager[NoFriends::class.java]!!.state)
                    return false

                val teams = LiquidBounce.moduleManager[Teams::class.java] as Teams

                return !teams.state || !teams.isInYourTeam(entity)
            }

            return EntityUtils.targetMobs && EntityUtils.isMob(entity) || EntityUtils.targetAnimals &&
                    EntityUtils.isAnimal(entity)
        }

        return false
    }
    private fun isAlive(entity: EntityLivingBase) = entity.isEntityAlive && entity.health > 0

    private val canBlock: Boolean
        get() = mc.thePlayer.heldItem != null && mc.thePlayer.heldItem.item is ItemSword

    private fun startBlocking() {
        if (LiquidBounce.moduleManager.getModule(ViaVersionFix::class.java)?.state == true) {
            val useItem = PacketWrapper.create(29, null, Via.getManager().connectionManager.connections.iterator().next())
            useItem.write(Type.VAR_INT, 1)
            PacketUtil.sendToServer(useItem, Protocol1_8TO1_9::class.java, true, true)
        }

        PacketUtils.sendPacketNoEvent(C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem()))
        blockingStatus = true
    }

    /**
     * Stop blocking
     */
    private fun stopBlocking() {
        if (blockingStatus) {
            PacketUtils.sendPacketNoEvent(
                C07PacketPlayerDigging(
                    C07PacketPlayerDigging.Action.RELEASE_USE_ITEM,
                    BlockPos.ORIGIN,
                    EnumFacing.DOWN
                )
            )
            blockingStatus = false
        }
    }
    private val cancelRun: Boolean
        get() = mc.thePlayer.isSpectator || !isAlive(mc.thePlayer)
                || (noBlink && LiquidBounce.moduleManager[Blink::class.java]!!.state) || LiquidBounce.moduleManager[FreeCam::class.java]!!.state ||
                (noScaff && (LiquidBounce.moduleManager[Scaffold::class.java]!!.state)) || noInvAttack && mc.currentScreen is GuiContainer

    object CombatListener : Listenable {
        private var syncEntity: EntityLivingBase? = null
        private var totalPlayed = 0
        private var startTime = System.currentTimeMillis()
        var win = 0
        var killCounts = 0

        @EventTarget
        private fun onAttack(event: AttackEvent) {
            syncEntity = event.targetEntity as EntityLivingBase?
        }

        @EventTarget
        private fun onUpdate(event: UpdateEvent) {
            if (syncEntity != null && syncEntity!!.isDead) {
                ++killCounts
                syncEntity = null
            }
        }

        @EventTarget(ignoreCondition = true)
        private fun onPacket(event: PacketEvent) {
            val packet = event.packet
            if (event.packet is C00Handshake) startTime = System.currentTimeMillis()

            if (packet is S45PacketTitle) {
                val title = packet.message.formattedText
                if (title.contains("Winner")) {
                    win++
                }
                if (title.contains("BedWar")) {
                    totalPlayed++
                }
                if (title.contains("SkyWar")) {
                    totalPlayed++
                }
            }
        }

        override fun handleEvents() = true

        init {
            LiquidBounce.eventManager.registerListener(this)
        }
    }
    override val tag: String
        get() = rotationMode.get()
}

