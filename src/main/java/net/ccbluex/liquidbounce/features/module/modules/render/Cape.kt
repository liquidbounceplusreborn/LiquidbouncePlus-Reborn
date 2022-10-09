/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.value.ListValue
import net.ccbluex.liquidbounce.utils.ClientUtils

import net.minecraft.util.ResourceLocation
import java.io.File

@ModuleInfo(name = "Cape", description = "LiquidBounce+ capes.", category = ModuleCategory.RENDER)
class Cape : Module() {

    val styleValue = ListValue("Style", arrayOf("Dark", "Darker", "Light", "Special1", "Special2","Dark2"), "Dark")

    private val capeCache = hashMapOf<String, CapeStyle>()

    fun getCapeLocation(value: String): ResourceLocation {
        if (capeCache[value.toUpperCase()] == null) {
            try {
                capeCache[value.toUpperCase()] = CapeStyle.valueOf(value.toUpperCase())
            } catch (e: Exception) {
                capeCache[value.toUpperCase()] = CapeStyle.DARK
            }
        }
        return capeCache[value.toUpperCase()]!!.location
    }

    enum class CapeStyle(val location: ResourceLocation) {
        DARK(ResourceLocation("liquidbounce+/cape/dark.png")),
        DARKER(ResourceLocation("liquidbounce+/cape/darker.png")),
        DARK2(ResourceLocation("liquidbounce+/cape/dark2.png")),
        LIGHT(ResourceLocation("liquidbounce+/cape/light.png")),
        SPECIAL1(ResourceLocation("liquidbounce+/cape/special1.png")),
        SPECIAL2(ResourceLocation("liquidbounce+/cape/special2.png"));
    }

    override val tag: String
        get() = styleValue.get()

}