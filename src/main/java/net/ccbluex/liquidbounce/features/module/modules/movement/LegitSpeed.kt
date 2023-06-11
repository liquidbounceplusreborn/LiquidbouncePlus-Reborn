package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.JumpEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.minecraft.potion.Potion

@ModuleInfo(name = "LegitSpeed", spacedName = "Legit Speed", description = "Allows you to move faster with speed effect.", category = ModuleCategory.MOVEMENT)
class LegitSpeed : Module() {
    @EventTarget
    fun onJump(event: JumpEvent?) {
        if (mc.thePlayer != null || MovementUtils.isMoving()) {
            var boost: Float
            if (mc.thePlayer.isPotionActive(Potion.moveSpeed) && mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).amplifier != 2) {
                mc.thePlayer.motionX *= (0.7 * (mc.thePlayer.getActivePotionEffect(
                    Potion.moveSpeed).amplifier + 1))
                mc.thePlayer.motionZ *= (0.7 * (mc.thePlayer.getActivePotionEffect(
                    Potion.moveSpeed).amplifier + 1))
            }
        }
    }
}