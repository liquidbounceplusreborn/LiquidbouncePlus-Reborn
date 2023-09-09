package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.value.FloatValue
import net.minecraft.entity.EntityLivingBase

@ModuleInfo(
    name = "TimerRange",
    description = "Automatically speeds up/down when you are near an enemy.",
    category = ModuleCategory.COMBAT
)
class TimerRange : Module() {
    private val distance = FloatValue("ClosestDistance", 1.75f, 0f, 8f, "")
    private val distanceToTimer = FloatValue("ClosestDistanceTimer", 0.4f, 0.1f, 10f, "")
    private val distance2: FloatValue = object : FloatValue("NormalDistance", 4f, 0f, 8f, ""){
        override fun onChanged(oldValue: Float, newValue: Float) {
            val a = distance.get()
            if (a > newValue) set(a)
        }
    }
    private val normalTimer = FloatValue("NormalTimer", 1.2f, 0.1f, 10f, "")

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (mc.thePlayer != null) {
            mc.timer.timerSpeed = 1f
            for (entity in mc.theWorld.loadedEntityList)
                if (entity is EntityLivingBase && EntityUtils.isSelected(entity, true)) {
                    if (mc.thePlayer.getDistanceToEntity(entity) <= distance.get()) {
                        mc.timer.timerSpeed = distanceToTimer.get()
                    } else if (mc.thePlayer.getDistanceToEntity(entity) <= distance2.get()) {
                        mc.timer.timerSpeed = normalTimer.get()
                    }
                }
        }
    }

    override fun onDisable() {
        mc.timer.timerSpeed = 1f
        super.onDisable()
    }
}