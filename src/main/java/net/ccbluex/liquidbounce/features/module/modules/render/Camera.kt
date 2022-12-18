/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.value.*
import java.awt.Color

@ModuleInfo(name = "Camera", description = "Allows you to see through walls in third person view.", category = ModuleCategory.RENDER)
class Camera : Module(){
    val cameraClipValue = BoolValue("CameraClip", true)
    val antiBlindValue = BoolValue("AntiBlind", true)
    val wordlColorValue = BoolValue("WorldColor", true)
    val fogColorValue = BoolValue("FogColor", true)
    //WorldColor
    val wordlColorrValue = IntegerValue("Red", 255, 0, 255) { wordlColorValue.get() }
    val wordlColorgValue = IntegerValue("Green", 255, 0, 255) { wordlColorValue.get() }
    val wordlColorbValue = IntegerValue("Blue", 255, 0, 255) { wordlColorValue.get() }
    //FogColor
    val fogColorrValue = IntegerValue("Red", 255, 0, 255) { fogColorValue.get() }
    val fogColorgValue = IntegerValue("Green", 255, 0, 255) { fogColorValue.get() }
    val fogColorbValue = IntegerValue("Blue", 255, 0, 255) { fogColorValue.get() }
    val fogDistance = FloatValue("DistanceFog", 0.7f, 0.1f, 2f) { fogColorValue.get() }

    //AntiBlind
    val confusionEffect = BoolValue("Confusion", true) { antiBlindValue.get() }
    val pumpkinEffect = BoolValue("Pumpkin", true) { antiBlindValue.get() }
    val fireEffect = BoolValue("Fire", false) { antiBlindValue.get() }
    val scoreBoard = BoolValue("Scoreboard", false) { antiBlindValue.get() }
    val bossHealth = BoolValue("Boss-Health", true) { antiBlindValue.get() }
}
