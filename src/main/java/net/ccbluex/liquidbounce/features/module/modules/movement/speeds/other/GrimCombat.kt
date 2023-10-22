package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.other

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils.getSpeed
import net.ccbluex.liquidbounce.utils.MovementUtils.strafe
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.minecraft.entity.EntityLivingBase


class GrimCombat : SpeedMode("GrimCombat") {
    override fun onUpdate() {
        val speed = LiquidBounce.moduleManager.getModule(Speed::class.java) as Speed
        for (entity in mc.theWorld.loadedEntityList) {
            if (entity is EntityLivingBase && entity.entityId != mc.thePlayer.entityId && mc.thePlayer.getDistanceToEntityBox(
                            entity
                    ) <= speed.distance.get() && ( !speed.onlyAir.get() || !mc.thePlayer.onGround)
            ) {
                if(speed.speedUp.get()) {
                    mc.thePlayer.motionX *= (1 + (speed.speed.get() * 0.01))
                    mc.thePlayer.motionZ *= (1 + (speed.speed.get() * 0.01))
                }
                if(speed.okstrafe.get()){
                    strafe(getSpeed())
                }
                return
            }
        }
    }
    override fun onMotion() {}
    override fun onMove(event: MoveEvent) {}

    override fun onMotion(event: MotionEvent) {}
}