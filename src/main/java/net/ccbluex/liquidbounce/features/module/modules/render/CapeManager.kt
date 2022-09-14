/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.ui.cape.GuiCape

@ModuleInfo(name = "Cape", description = "LiquidBounce+ capes.", category = ModuleCategory.RENDER, onlyEnable = true)
class CapeManager : Module() {

    override fun onEnable() {
        mc.displayGuiScreen(GuiCape)
    }

}