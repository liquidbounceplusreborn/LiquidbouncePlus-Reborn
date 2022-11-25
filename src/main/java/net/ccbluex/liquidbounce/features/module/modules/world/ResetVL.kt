package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.FloatValue

@ModuleInfo(name = "ResetVL", spacedName = "Reset VL", description = "Unflags you on Hypixel. (may not work as intended)", category = ModuleCategory.WORLD)
class ResetVL : Module() {

    private var y = 0.0
    private var jumped = 0

    private val yMotion = FloatValue("YMotion", 0.08f, 0.05f, 0.15f)
    private val jumpAmount = IntegerValue ("Amount", 25, 5, 30)
    private val timer = FloatValue("Timer", 2.25f, 1f, 4f)

    override fun onEnable() {
        mc.thePlayer ?: return
        y = mc.thePlayer.posY
        jumped = 0
    }

    override fun onDisable() {
        mc.timer.timerSpeed = 1F
    }

    @EventTarget
    fun onMotion(event: MotionEvent) {
        if (jumped <= jumpAmount.get()) {
            if (mc.thePlayer.onGround) {
                mc.thePlayer.motionY = yMotion.get().toDouble()
                jumped++
            }
            mc.thePlayer.posY = y
            mc.timer.timerSpeed = timer.get()
        } else
            state = false
    }

}