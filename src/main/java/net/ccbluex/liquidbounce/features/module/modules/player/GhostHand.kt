/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.value.BlockValue

@ModuleInfo(name = "GhostHand", spacedName = "Ghost Hand", description = "Allows you to interact with selected blocks through walls.", category = ModuleCategory.WORLD)
class GhostHand : Module() {

    val blockValue = BlockValue("Block", 54)

}
