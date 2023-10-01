package net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.impl

import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Target
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.TargetStyle
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.entity.player.EntityPlayer
import java.awt.Color

class Raven(inst: Target): TargetStyle("Raven", inst, false) {
    override fun drawTarget(entity: EntityPlayer) {
        val maxHealth = entity.maxHealth
        val health = entity.health
        val healthString = "${decimalFormat2.format(entity.health)} "
        val targetName = entity.name
        val x = 0.0
        val y = 0.0
        RenderUtils.newDrawRect(x, y, Fonts.minecraftFont.getStringWidth(entity.name) + 60.0, 30.0,Color(0, 0, 0, 100).rgb)

        RenderUtils.newDrawRect(x, (y + maxHealth - health) * 1.5,1.0, 30.0, targetInstance.barColor.rgb);

        Fonts.minecraftFont.drawStringWithShadow("Target: $targetName", (x + 6).toFloat(), (y + 5).toFloat(),Color(255, 255, 255).rgb)
        Fonts.minecraftFont.drawStringWithShadow("Health: ", (x + 6).toFloat(), (y + 9 + Fonts.minecraftFont.FONT_HEIGHT).toFloat(), Color(255, 255, 255).rgb)
        Fonts.minecraftFont.drawStringWithShadow(healthString, (x + 6 + Fonts.minecraftFont.getStringWidth("Health: ")).toFloat(), (y + 9 + Fonts.minecraftFont.FONT_HEIGHT).toFloat(), targetInstance.barColor.rgb)

        val winorlose = if(health / maxHealth <= mc.thePlayer.health / mc.thePlayer.maxHealth) "W" else "L"
        Fonts.minecraftFont.drawStringWithShadow(winorlose, (x + 6 + Fonts.minecraftFont.getStringWidth("Target: $targetName ")).toFloat(), (y + 5).toFloat(), (if (winorlose == "W")  Color(0, 255, 0).rgb else Color(139, 0, 0).rgb))

    }
    override fun getBorder(entity: EntityPlayer?): Border? {
        entity ?: return Border(0F, 0F, 60f, 30f)
        val width = (Fonts.minecraftFont.getStringWidth(entity.name))
                .coerceAtLeast(60)
                .toFloat()
        return Border(0F, 0F, width + 60f, 30f)
    }
}