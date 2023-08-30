package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.potion.Potion

@ModuleInfo(name = "LegitSpeed", spacedName = "Legit Speed", description = "Allows you to move faster with speed effect.", category = ModuleCategory.MOVEMENT)
class LegitSpeed : Module() {
    private val mode = ListValue("Mode", arrayOf("Bingus", "Phantom"), "Bingus")
    private val speed = FloatValue("Ph", 0.08f, 0.0f, 0.2f)
    private val boost = FloatValue("Boost 1", 0.1f, 0.0f, 0.2f)
    private val boost1 = FloatValue("Boost 2", 0.15f, 0.0f, 0.2f)
    private val boost2 = FloatValue("Boost 3", 0.15f, 0.0f, 0.2f)
    private var fastFall = BoolValue("FastFall", true)

    var ph = false
    
    @EventTarget
    fun onJump(event: JumpEvent?) {
        if (mc.thePlayer != null || MovementUtils.isMoving()) {
            if (mode.get() == "Phantom") {
                ph = true
            }
            if (mode.get() == "Bingus") {
                if (mc.thePlayer.isPotionActive(Potion.moveSpeed)
                    && mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).amplifier !== 1
                ) {
                    val boost: Float = boost.get()
                    mc.thePlayer.motionX *= 1.0f + getBaseSpeed() * boost
                    mc.thePlayer.motionZ *= 1.0f + getBaseSpeed() * boost
                }
                if (mc.thePlayer.isPotionActive(Potion.moveSpeed)
                    && mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).amplifier !== 2
                ) {
                    val boost: Float = boost1.get()
                    mc.thePlayer.motionX *= 1.0f + getBaseSpeed() * boost
                    mc.thePlayer.motionZ *= 1.0f + getBaseSpeed() * boost
                }
                if (mc.thePlayer.isPotionActive(Potion.moveSpeed)
                    && mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).amplifier !== 3
                ) {
                    val boost: Float = boost2.get()
                    mc.thePlayer.motionX *= 1.0f + getBaseSpeed() * boost
                    mc.thePlayer.motionZ *= 1.0f + getBaseSpeed() * boost
                }
            }
        }
    }
    @EventTarget
    fun onMove(event: MoveEvent){
        if (mode.get() == "Phantom") {
            if (mc.thePlayer != null || MovementUtils.isMoving()) {
                if (ph) {
                    event.setX(event.x + event.x * speed.get())
                    event.setZ(event.z + event.z * speed.get())
                }
            }
        }
    }
    @EventTarget
    fun onMotion(event:MotionEvent){
        if(event.eventState == EventState.PRE){
            if (MovementUtils.isMoving() && fastFall.get()) {
                if (mc.thePlayer.motionY < 0.1 && mc.thePlayer.motionY > -0.25 && mc.thePlayer.fallDistance < 0.1) {
                    mc.thePlayer.motionY -= 0.15
                }
            }
        }
    }

    private fun getBaseSpeed(): Double {
        val player = mc.thePlayer
        var base = 0.2895
        val moveSpeed = player.getActivePotionEffect(Potion.moveSpeed)
        val moveSlowness = player.getActivePotionEffect(Potion.moveSlowdown)
        if (moveSpeed != null && moveSpeed.duration > 20) {
            base *= 1.0 + 0.19 * (moveSpeed.amplifier + 1)
        }
        if (moveSlowness != null && moveSlowness.duration > 20) {
            base *= 1.0 - 0.13 * (moveSlowness.amplifier + 1)
        }
        if (player.isInWater) {
            base *= 0.5203619984250619
            val depthStriderLevel = EnchantmentHelper.getDepthStriderModifier(mc.thePlayer)
            if (depthStriderLevel > 0) {
                val DEPTH_STRIDER_VALUES = doubleArrayOf(
                    1.0, 1.4304347400741908, 1.7347825295420374,
                    1.9217391028296074
                )
                base *= DEPTH_STRIDER_VALUES[depthStriderLevel]
            }
        } else if (player.isInLava) {
            base *= 0.5203619984250619
        }
        return base
    }
    override fun onEnable() {
        this.ph = false
    }
    override val tag: String
        get() = mode.get()
}