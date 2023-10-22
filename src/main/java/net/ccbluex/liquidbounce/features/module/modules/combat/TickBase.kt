package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue

@ModuleInfo(
    name = "TickBase",
    description = "Automatically speeds up/down when ??????",
    category = ModuleCategory.COMBAT
)
class TickBase : Module() {
    private val mode = ListValue("Mode", arrayOf("Range1","TickTest1","HurtTimeTest1","PredictTest"),"Range1")
    private val distance = FloatValue("ClosestDistance", 1.75f, 0f, 8f, "") {mode.get() == "Range1"}
    private val distanceToTimer = FloatValue("ClosestDistanceTimer", 0.4f, 0.1f, 10f) {mode.get() == "Range1"}
    private val distance2: FloatValue = object : FloatValue("NormalDistance", 4f, 0f, 8f, {mode.get() == "Range1"}) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val a = distance.get()
            if (a > newValue) set(a)
        }
    }
    private val normalTimer = FloatValue("NormalTimer", 1.2f, 0.1f, 10f) {mode.get() == "Range1"}

    private val igroneDistance = FloatValue("IgroneDistance", 0.8f, 0.1f, 2f) {mode.get() == "Range1"}

    private val tick = IntegerValue("Tick", 1, 1, 10) {mode.get() == "TickTest1"}
    private val tickTimer = FloatValue("TickTimer", 0.4f, 0.1f, 10f) {mode.get() == "TickTest1"}

    private val hurtTime = IntegerValue("HurtTime", 3, 0, 10) {mode.get() == "HurtTimeTest1"}
    private val hurtTimer = FloatValue("HurtTimeTimer", 0.2f, 0.1f, 10f) {mode.get() == "HurtTimeTest1"}

    private val predictDistance = FloatValue("PredictDistance", 0.3f, 0.1f, 2f) {mode.get() == "PredictTest"}
    private val predictTimer = FloatValue("PredictTimer", 0.4f, 0.1f, 10f) {mode.get() == "PredictTest"}
    private val predictTimer2 = FloatValue("PredictTimer2", 2.4f, 0.1f, 10f) {mode.get() == "PredictTest"}

    private var ticks = 0
    private val killAura = LiquidBounce.moduleManager.getModule(KillAura::class.java)

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (mc.thePlayer != null) {
            mc.timer.timerSpeed = 1f
            val entity = killAura?.target
            when (mode.get()) {
                "Range1" -> {
                    if (mc.thePlayer.getDistanceToEntity(entity) <= distance.get()) {
                        mc.timer.timerSpeed = distanceToTimer.get()
                    } else if (mc.thePlayer.getDistanceToEntity(entity) <= distance2.get()) {
                        mc.timer.timerSpeed = normalTimer.get()
                    }
                    if(mc.thePlayer.getDistanceToEntity(entity) <= igroneDistance.get()){
                        mc.timer.timerSpeed = 1f
                    }
                }

                "TickTest1" -> {
                    if (entity != null) {
                        ticks++
                        if (ticks == (tick.get() * 100)) {
                            mc.timer.timerSpeed = tickTimer.get()
                            ticks = 0
                        }
                    }
                }

                "HurtTimeTest1" -> {
                    if (entity != null) {
                        if (mc.thePlayer.hurtTime > hurtTime.get()) {
                            mc.timer.timerSpeed = hurtTimer.get()
                        }
                    }
                }

                "PredictTest" -> {
                    if (predictDistance.get() + killAura?.attackRangeValue!!.get() <= mc.thePlayer.getDistanceToEntityBox(entity!!).toFloat()) {
                        mc.timer.timerSpeed = predictTimer.get()
                    }else if (killAura.attackRangeValue.get() <= mc.thePlayer.getDistanceToEntityBox(entity).toFloat()) {
                        mc.timer.timerSpeed = predictTimer2.get()
                    }
                }
            }
        }
    }

    override fun onDisable() {
        mc.timer.timerSpeed = 1f
        super.onDisable()
    }
}
