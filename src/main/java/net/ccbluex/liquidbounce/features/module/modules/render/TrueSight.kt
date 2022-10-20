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

@ModuleInfo(name = "TrueSight", spacedName = "True Sight", description = "Allows you to see invisible entities and barriers.", category = ModuleCategory.RENDER)
class TrueSight : Module() {
    val barriersValue = BoolValue("Barriers", true)
    val entitiesValue = BoolValue("Entities", true)
}