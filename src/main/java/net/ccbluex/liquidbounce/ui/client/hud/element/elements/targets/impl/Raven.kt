package net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.impl

import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Target
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.TargetStyle
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.player.EntityPlayer
import java.awt.Color

class Raven(inst: Target): TargetStyle("Raven", inst, false) {
    private var mainStr = ""
    private fun updateMainStr(entity: EntityPlayer) {
        val maxHealth = entity.maxHealth
        val health = entity.health
        val targetName = entity.name
        mainStr = "Target: $targetName ${if(health / maxHealth <= mc.thePlayer.health / mc.thePlayer.maxHealth) "§aW" else "§cL"}"
    }
    override fun drawTarget(entity: EntityPlayer) {
        val maxHealth = entity.maxHealth
        val health = entity.health
        val healthString = "${decimalFormat2.format(entity.health)} "
        val x = 0.0
        val y = 0.0
        updateMainStr(entity)

        RenderUtils.newDrawRect(x, y, Fonts.minecraftFont.getStringWidth(mainStr) + 24.0, 30.0,Color(0, 0, 0, 100).rgb)
        RenderUtils.newDrawRect(x, (y + maxHealth - health) * 1.5,1.0, 30.0, targetInstance.barColor.rgb)

        Fonts.minecraftFont.drawStringWithShadow(mainStr, (x + 6).toFloat(), (y + 5).toFloat(),Color(255, 255, 255).rgb)
        Fonts.minecraftFont.drawStringWithShadow("Health: ", (x + 6).toFloat(), (y + 9 + Fonts.minecraftFont.FONT_HEIGHT).toFloat(), Color(255, 255, 255).rgb)
        Fonts.minecraftFont.drawStringWithShadow(healthString, (x + 6 + Fonts.minecraftFont.getStringWidth("Health: ")).toFloat(), (y + 9 + Fonts.minecraftFont.FONT_HEIGHT).toFloat(), targetInstance.barColor.rgb)

    }
    override fun getBorder(entity: EntityPlayer?): Border {
        entity ?: return Border(0F, 0F, 60f, 30f)
        updateMainStr(entity)
        val width = (Fonts.minecraftFont.getStringWidth(mainStr)).toFloat()
        return Border(0F, 0F, width + 24f, 30f)
    }

    override fun handleBlur(player: EntityPlayer) {
        updateMainStr(player)
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        RenderUtils.fastRoundedRect(0F, 0F, Fonts.minecraftFont.getStringWidth(mainStr) + 24f, 30f, 7F)
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }

    override fun handleShadowCut(player: EntityPlayer) = handleBlur(player)

    override fun handleShadow(player: EntityPlayer) {
        updateMainStr(player)
        RenderUtils.originalRoundedRect(0F, 0F, Fonts.minecraftFont.getStringWidth(mainStr) + 24f, 30f, 8F, shadowOpaque.rgb)
    }
}