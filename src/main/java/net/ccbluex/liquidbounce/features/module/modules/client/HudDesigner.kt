package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.ui.client.hud.designer.GuiHudDesigner

@ModuleInfo(name = "HudDesigner", description = "Desinger ur HUD.", category = ModuleCategory.CLIENT, onlyEnable = true)
class HudDesigner : Module() {
    private var gui: GuiHudDesigner? = null

    private fun getGUI(): GuiHudDesigner {
        if (gui == null)
            gui = GuiHudDesigner()
        return gui!!
    }
    override fun onEnable() {
        mc.displayGuiScreen(getGUI())
    }
}