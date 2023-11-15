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
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue

@ModuleInfo(name = "Chams", description = "Allows you to see targets through blocks.", category = ModuleCategory.RENDER)
class Chams : Module() {
    val targetsValue = BoolValue("Targets", true)
    val chestsValue = BoolValue("Chests", true)
    val itemsValue = BoolValue("Items", true)
    val localPlayerValue = BoolValue("LocalPlayer", true)
    val legacyMode = BoolValue("Legacy-Mode", false)
    val texturedValue = BoolValue("Textured", true) { !legacyMode.get() }
    val colorModeValue = ListValue("Color", arrayOf("Custom", "Rainbow", "Sky", "LiquidSlowly", "Fade", "Mixer"), "Custom") { !legacyMode.get() }
    val behindColorModeValue = ListValue("Behind-Color", arrayOf("Same", "Opposite", "Custom"), "Same") { !legacyMode.get() }
    val redValue = IntegerValue("Red", 255, 0, 255) { !legacyMode.get() }
    val greenValue = IntegerValue("Green", 119, 0, 255) { !legacyMode.get() }
    val blueValue = IntegerValue("Blue", 119, 0, 255) { !legacyMode.get() }
    val alphaValue = IntegerValue("Alpha", 255, 0, 255) { !legacyMode.get() }
    val behindRedValue = IntegerValue("BehindRed", 0, 0, 255) { !legacyMode.get() && behindColorModeValue.isMode("Custom")}
    val behindGreenValue = IntegerValue("BehindGreen", 223, 0, 255) { !legacyMode.get() && behindColorModeValue.isMode("Custom")}
    val behindBlueValue = IntegerValue("BehindBlue", 255, 0, 255) { !legacyMode.get() && behindColorModeValue.isMode("Custom")}
    val behindAlphaValue = IntegerValue("BehindAlpha", 255, 0, 255) { !legacyMode.get() && behindColorModeValue.isMode("Custom")}
    val saturationValue = FloatValue("Saturation", 1F, 0F, 1F) { !legacyMode.get() }
    val brightnessValue = FloatValue("Brightness", 1F, 0F, 1F) { !legacyMode.get() }
    val mixerSecondsValue = IntegerValue("Seconds", 2, 1, 10) { !legacyMode.get() }
}
