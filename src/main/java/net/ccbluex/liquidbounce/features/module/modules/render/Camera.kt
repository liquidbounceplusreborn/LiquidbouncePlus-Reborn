/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.ListValue

@ModuleInfo(name = "Camera", description = "Allows you to see through walls in third person view.", category = ModuleCategory.RENDER)
class Camera : Module(){
    val cameraClipValue = BoolValue("CameraClip", true)
    val antiBlindValue = BoolValue("AntiBlind", true)

    //AntiBlind
    val confusionEffect = BoolValue("Confusion", true) { antiBlindValue.get() }
    val pumpkinEffect = BoolValue("Pumpkin", true) { antiBlindValue.get() }
    val fireEffect = BoolValue("Fire", false) { antiBlindValue.get() }
    val scoreBoard = BoolValue("Scoreboard", false) { antiBlindValue.get() }
    val bossHealth = BoolValue("Boss-Health", true) { antiBlindValue.get() }
}