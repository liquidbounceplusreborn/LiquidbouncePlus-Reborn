//from kevin client
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventState
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.utils.RaycastUtils
import net.ccbluex.liquidbounce.utils.extensions.expands
import net.ccbluex.liquidbounce.utils.extensions.getNearestPointBB
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import kotlin.math.min

@ModuleInfo(
    name = "TimerRange",
    description = "Automatically speeds up/down when ??????",
    category = ModuleCategory.COMBAT
)
object TimerRange : Module() {
    private val mode = ListValue("Mode", arrayOf("RayCast", "Radius"), "RayCast")
    private val minDistance: FloatValue = object : FloatValue("MinDistance", 3F, 0F, 4F) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            if (newValue > maxDistance.get()) set(maxDistance.get())
        }
    }
    private val maxDistance: FloatValue = object : FloatValue("MaxDistance", 4F, 3F, 7F) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            if (newValue < minDistance.get()) set(minDistance.get())
        }
    }
    private val rangeMode = ListValue("RangeMode", arrayOf("Setting", "Smart"), "Smart")
    private val maxTimeValue = IntegerValue("MaxTime", 3, 0, 20)
    private val delayValue = IntegerValue("Delay", 5, 0, 20)
    private val maxHurtTimeValue = IntegerValue("TargetMaxHurtTime", 2, 0, 10)
    private val onlyKillAura = BoolValue("OnlyKillAura", true)
   //private val auraClick = BoolValue("AuraClick", true)
    private val onlyPlayer = BoolValue("OnlyPlayer", true)
    private val debug = BoolValue("Debug", false)
    private val betterAnimation = BoolValue("BetterAnimation", true)
    private val reverseValue = BoolValue("Reverse", false)
    private val maxReverseRange : FloatValue = object : FloatValue("MaxReverseRange", 2.8f, 1f, 4f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            if (newValue > minDistance.get() && minDistance.get() > minReverseRange.get()) set(minDistance.get())
            else if (newValue < minReverseRange.get()) set(minReverseRange.get())
        }
    }

    private val minReverseRange : FloatValue = object : FloatValue("MinReverseRange", 2.5f, 1f, 4f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            if (newValue > maxReverseRange.get()) set(maxReverseRange.get())
        }
    }
    private val reverseTime : IntegerValue = object : IntegerValue("ReverseStopTime", 3, 1, 10) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            if (newValue < reverseTickTime.get()) set(reverseTickTime.get())
        }
    }
    private val reverseTickTime : IntegerValue = object : IntegerValue("ReverseTickTime", 3, 0, 10) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            if (newValue > reverseTime.get()) set(reverseTime.get())
        }
    }
    private val reverseDelay = IntegerValue("ReverseDelay", 5, 0, 20)
    private val reverseTargetMaxHurtTime = IntegerValue("ReverseTargetMaxHurtTime", 3, 0, 10)
    //private val reverseAuraClick = ListValue("ReverseAuraClick", arrayOf("None", "BeforeTimer", "AfterTimer"), "None")

    private val killAura: KillAura? = LiquidBounce.moduleManager.getModule(KillAura::class.java)

    @JvmStatic
    private var working = false
    private var stopWorking = false
    private var lastNearest = 10.0
    private var cooldown = 0
    private var freezeTicks = 0
    private var reverseFreeze = true
    private var firstAnimation = true

    @EventTarget fun onMotion(event: MotionEvent) {
        if (event.eventState == EventState.PRE) return // post event mean player's tick is done
        val thePlayer = mc.thePlayer ?: return
        if (onlyKillAura.get() && !killAura?.state!!) return
        if (mode  .get() == "RayCast") {
            val entity = RaycastUtils.raycastEntity(maxDistance.get() + 1.0, object : RaycastUtils.IEntityFilter {
                override fun canRaycast(entity: Entity?): Boolean {
                    return entity != null && entity is EntityLivingBase && (!onlyPlayer.get() || entity is EntityPlayer)
                }
            })
            if (entity == null || entity !is EntityLivingBase) {
                lastNearest = 10.0
                return
            }
            if (!EntityUtils.isSelected(entity, true)) return
            val vecEyes = thePlayer.getPositionEyes(1f)
            val predictEyes = if (rangeMode  .get() == "Smart") {
                thePlayer.getPositionEyes(maxTimeValue.get() + 1f)
            } else thePlayer.getPositionEyes(3f)
            val entityBox = entity.entityBoundingBox.expands(entity.collisionBorderSize.toDouble())
            val box = getNearestPointBB(
                vecEyes,
                entityBox
            )
            val box2 = getNearestPointBB(
                predictEyes,
                if (entity is EntityOtherPlayerMP) {
                    entityBox.offset(entity.otherPlayerMPX - entity.posX, entity.otherPlayerMPY - entity.posY, entity.otherPlayerMPZ - entity.posZ)
                } else entityBox
            )
            val range = box.distanceTo(vecEyes)
            val afterRange = box2.distanceTo(predictEyes)
            if (!working && reverseValue.get()) {
                if (range <= maxReverseRange.get() && range >= minReverseRange.get() && cooldown <= 0 && entity.hurtTime <= reverseTargetMaxHurtTime.get()) {
                    freezeTicks = reverseTime.get()
                    firstAnimation = false
                    reverseFreeze = true
                    return
                }
            }
            if (range < minDistance.get()) {
                stopWorking = true
            } else if (((rangeMode  .get() == "Smart" && range > minDistance.get() && afterRange < minDistance.get() && afterRange < range) || (rangeMode  .get() == "Setting" && range <= maxDistance.get() && range < lastNearest && afterRange < range)) && entity.hurtTime <= maxHurtTimeValue.get()) {
                stopWorking = false
                foundTarget()
            }
            lastNearest = range
        } else {
            val entityList = mc.theWorld.getEntitiesWithinAABBExcludingEntity(
                thePlayer,
                thePlayer.entityBoundingBox.expands(maxDistance.get() + 1.0)
            )
            if (entityList.isNotEmpty()) {
                val vecEyes = thePlayer.getPositionEyes(1f)
                val afterEyes = if (rangeMode  .get() == "Smart") {
                    thePlayer.getPositionEyes(maxTimeValue.get() + 1f)
                } else thePlayer.getPositionEyes(3f)
                var targetFound = false
                var targetInRange = false
                var nearest = 10.0
                for (entity in entityList) {
                    if (entity !is EntityLivingBase) continue
                    if (onlyPlayer.get() && entity !is EntityPlayer) continue
                    if (!EntityUtils.isSelected(entity, true)) continue
                    val entityBox = entity.entityBoundingBox.expands(entity.collisionBorderSize.toDouble())
                    val box = getNearestPointBB(
                        vecEyes,
                        entityBox
                    )
                    val box2 = getNearestPointBB(
                        afterEyes,
                        if (entity is EntityOtherPlayerMP) {
                            entityBox.offset(entity.otherPlayerMPX - entity.posX, entity.otherPlayerMPY - entity.posY, entity.otherPlayerMPZ - entity.posZ)
                        } else entityBox
                    )
                    val range = box.distanceTo(vecEyes)
                    val afterRange = box2.distanceTo(afterEyes)
                    if (!working && reverseValue.get()) {
                        if (range <= maxReverseRange.get() && range >= minReverseRange.get() && cooldown <= 0 && entity.hurtTime <= reverseTargetMaxHurtTime.get()) {
                            freezeTicks = reverseTime.get()
                            firstAnimation = false
                            reverseFreeze = true
                            return
                        }
                    }
                    if (range < minDistance.get()) {
                        targetInRange = true
                        break
                    } else if (range <= maxDistance.get() && afterRange < range && entity.hurtTime <= maxHurtTimeValue.get()) {
                        targetFound = true
                    }
                    nearest = min(nearest, range)
                }
                if (targetInRange) {
                    stopWorking = true
                } else if (targetFound && nearest < lastNearest) {
                    stopWorking = false
                    foundTarget()
                }
                lastNearest = nearest
            } else {
                lastNearest = 10.0
            }
        }
    }

    fun foundTarget() {
        if (cooldown > 0 || freezeTicks != 0 || maxTimeValue.get() == 0) return
        cooldown = delayValue.get()
        working = true
        freezeTicks = 0
        if (betterAnimation.get()) firstAnimation = false
        while (freezeTicks <= maxTimeValue.get()){
            //(if (auraClick.get()) 1 else 0) && !stopWorking) {
            ++freezeTicks
            mc.runTick()
        }
        if (debug.get()) ClientUtils.displayChatMessage("Timer-ed")
        /*if (auraClick.get()) {
            killAura?.clicks = killAura?.clicks!! + 1
            ++freezeTicks
            mc.runTick()
            if (debug.get()) ClientUtils.displayChatMessage("Clicked")
        }*/
        stopWorking = false
        working = false
    }

    @JvmStatic
    fun handleTick(): Boolean {
        if (working || freezeTicks < 0) return true
        if (state && freezeTicks > 0) {
            --freezeTicks
            return true
        }
        if (reverseFreeze) {
            reverseFreeze = false
            var time = reverseTickTime.get()
            working = true
            //if (reverseAuraClick .get() === "BeforeTimer") killAura?.clicks = killAura?.clicks!! + 1
            while (time > 0) {
                --time
                mc.runTick()
            }
            working = false
            cooldown = reverseDelay.get()
            //if (reverseAuraClick .get() == "AfterTimer") killAura?.clicks = killAura?.clicks!! + 1
        }
        if (cooldown > 0) --cooldown
        return false
    }

    @JvmStatic
    fun freezeAnimation(): Boolean {
        if (freezeTicks != 0) {
            if (!firstAnimation) {
                firstAnimation = true
                return false
            }
            return true
        }
        return false
    }
}