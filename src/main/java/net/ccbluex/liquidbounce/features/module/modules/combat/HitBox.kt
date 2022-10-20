/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.value.FloatValue

@ModuleInfo(name = "HitBox", spacedName = "Hit Box", description = "Makes hitboxes of targets bigger.", category = ModuleCategory.COMBAT)
class HitBox : Module() {

    val sizeValue = FloatValue("Size", 0.4F, 0F, 1F)

    override val tag: String
        get() = "${sizeValue.get()}"
}
