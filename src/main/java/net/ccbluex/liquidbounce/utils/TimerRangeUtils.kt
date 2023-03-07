package net.ccbluex.liquidbounce.utils

import net.ccbluex.liquidbounce.features.module.modules.combat.NoFriends
import net.ccbluex.liquidbounce.features.module.modules.world.AntiBot
import net.ccbluex.liquidbounce.features.module.modules.world.Teams
import net.ccbluex.liquidbounce.utils.EntityUtils.*
import net.ccbluex.liquidbounce.utils.MinecraftInstance.mc
import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.utils.extensions.isAnimal
import net.ccbluex.liquidbounce.utils.extensions.isClientFriend
import net.ccbluex.liquidbounce.utils.extensions.isMob
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.boss.EntityDragon
import net.minecraft.entity.monster.EntityGhast
import net.minecraft.entity.monster.EntityGolem
import net.minecraft.entity.monster.EntityMob
import net.minecraft.entity.monster.EntitySlime
import net.minecraft.entity.passive.EntityAnimal
import net.minecraft.entity.passive.EntityBat
import net.minecraft.entity.passive.EntitySquid
import net.minecraft.entity.passive.EntityVillager
import net.minecraft.entity.player.EntityPlayer

class TimerRangeUtils {

    @JvmField
    var targetInvisible: Boolean = false

    @JvmField
    var targetPlayer: Boolean = true

    @JvmField
    var targetMobs: Boolean = true

    @JvmField
    var targetAnimals: Boolean = false

    @JvmField
    var targetDead: Boolean = false

    @JvmField
    var lastAttackedPerson: Entity? = null

    @JvmField
    var lastAttackedPersonTime: Long? = null

    fun isSelected(entity: Entity?, canAttackCheck: Boolean): Boolean {
        if (entity is EntityLivingBase && (targetDead || entity.isEntityAlive) && entity != mc.thePlayer) {
            if (targetInvisible || !entity.isInvisible) {
                if (targetPlayer && entity is EntityPlayer) {
                    if (canAttackCheck) {
                        if (entity.isClientFriend() && !LiquidBounce.moduleManager.getModule(NoFriends::class.java)!!.state)
                            return false

                        if (entity.isSpectator) return false
                        val teams = LiquidBounce.moduleManager.getModule(Teams::class.java) as Teams
                        return !teams.state || !teams.isInYourTeam(entity)
                    }
                    return true
                }

                return targetMobs && entity.isMob() || targetAnimals && entity.isAnimal()
            }
        }
        return false
    }

    fun isAnimal(entity: Entity?): Boolean {
        return entity is EntityAnimal || entity is EntitySquid || entity is EntityGolem ||
                entity is EntityBat
    }

    fun closestPerson(): EntityLivingBase? {
        val targets = mc.theWorld.loadedEntityList.filter {
            it is EntityLivingBase && it != mc.thePlayer && isSelected(it, true) &&
                    mc.thePlayer.canEntityBeSeen(it) && isEnemy(it)
        }
        val entity = targets.minByOrNull { mc.thePlayer.getDistanceToEntity(it) }
        return entity as EntityLivingBase?
    }

    fun closestPersonsDistance(): Float {
        val targets = mc.theWorld.loadedEntityList.filter {
            it is EntityLivingBase && it != mc.thePlayer && isSelected(it, true) &&
                    mc.thePlayer.canEntityBeSeen(it) && isEnemy(it)
        }
        val entity = targets.minByOrNull { mc.thePlayer.getDistanceToEntity(it) }
        return if (entity == null) {
            100000f
        } else {
            mc.thePlayer.getDistanceToEntity(entity)
        }
    }

    fun isEnemy(entity: Entity?): Boolean {
        if (entity is EntityLivingBase && (targetDead || isAlive(entity)) && entity != mc.thePlayer) {
            if (!targetInvisible && entity.isInvisible)
                return false

            if (targetPlayer && entity is EntityPlayer) {
                if (entity.isSpectator)
                    return false

                if (entity.isClientFriend() && !LiquidBounce.moduleManager[NoFriends::class.java]!!.state)
                    return false

                val teams = LiquidBounce.moduleManager[Teams::class.java] as Teams

                return !teams.state || !teams.isInYourTeam(entity)
            }

            return targetMobs && entity.isMob() || targetAnimals && entity.isAnimal()
        }

        return false
    }

    private fun isAlive(entity: EntityLivingBase) = entity.isEntityAlive && entity.health > 0

    fun isMob(entity: Entity?): Boolean {
        return entity is EntityMob || entity is EntityVillager || entity is EntitySlime ||
                entity is EntityGhast || entity is EntityDragon
    }

    fun isRendered(entityToCheck: Entity): Boolean {
        return mc.theWorld != null && mc.theWorld!!.getLoadedEntityList()!!.contains(entityToCheck)
    }
}