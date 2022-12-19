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
    val worldColorValue = BoolValue("WorldColor", true)
    val fogColorValue = BoolValue("FogColor", true)
    val hitColorValue = BoolValue("HitColor", true)
    val cameraPositionValue = BoolValue("CameraPosition", true)
    //WorldColor
    val worldColorRValue = IntegerValue("Red", 255, 0, 255) { worldColorValue.get() }
    val worldColorGValue = IntegerValue("Green", 255, 0, 255) { worldColorValue.get() }
    val worldColorBValue = IntegerValue("Blue", 255, 0, 255) { worldColorValue.get() }
    //FogColor
    val fogColorRValue = IntegerValue("Red", 255, 0, 255) { fogColorValue.get() }
    val fogColorGValue = IntegerValue("Green", 255, 0, 255) { fogColorValue.get() }
    val fogColorBValue = IntegerValue("Blue", 255, 0, 255) { fogColorValue.get() }
    val fogDistance = FloatValue("DistanceFog", 0.7f, 0.1f, 2f) { fogColorValue.get() }
    //HitColor
    val hitColorRValue = IntegerValue("Red", 255, 0, 255) { hitColorValue.get() }
    val hitColorGValue = IntegerValue("Green", 255, 0, 255) { hitColorValue.get() }
    val hitColorBValue = IntegerValue("Blue", 255, 0, 255) { hitColorValue.get() }
    val hitColorAlphaValue = IntegerValue("Aplha", 255, 0, 255) { hitColorValue.get() }
    //CameraPosition
    val cameraPositionYawValue = IntegerValue("Yaw", 10, -50, 50) { cameraPositionValue.get() }
    val cameraPositionPitchValue = IntegerValue("Pitch", 10, -50, 50) { cameraPositionValue.get() }
    val cameraPositionFovValue = IntegerValue("DistanceFov", 4, 1, 50) { cameraPositionValue.get() }
    //AntiBlind
    val confusionEffect = BoolValue("Confusion", true) { antiBlindValue.get() }
    val pumpkinEffect = BoolValue("Pumpkin", true) { antiBlindValue.get() }
    val fireEffect = BoolValue("Fire", false) { antiBlindValue.get() }
    val scoreBoard = BoolValue("Scoreboard", false) { antiBlindValue.get() }
    val bossHealth = BoolValue("Boss-Health", true) { antiBlindValue.get() }
}
