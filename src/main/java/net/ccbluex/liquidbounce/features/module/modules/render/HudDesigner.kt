package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.ui.client.hud.designer.GuiHudDesigner

@ModuleInfo(name = "HudDesigner", description = "Desinger ur HUD.", category = ModuleCategory.RENDER, canEnable = false)
class HudDesigner : Module() {
    override fun onEnable() {
        mc.displayGuiScreen(GuiHudDesigner())
        fun onDisable() {
            mc.displayGuiScreen(GuiHudDesigner())
        }
    }
}