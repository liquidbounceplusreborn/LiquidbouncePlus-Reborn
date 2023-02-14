package net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.impl

import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Target
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.TargetStyle
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.extensions.skin
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.player.EntityPlayer
import java.awt.Color
import kotlin.math.pow

class NovolineTwo(inst: Target): TargetStyle("Novoline2", inst, true) {
    override fun drawTarget(entity: EntityPlayer) {
        updateAnim(entity.health)
        val width = (38 + Fonts.fontSFUI40.getStringWidth(entity.name)).coerceAtLeast(118).toFloat()
        RenderUtils.drawRect(0f, 0f, width + 14f, 44f, Color(0, 0, 0, targetInstance.bgAlphaValue.get()).rgb)
        drawHead(entity.skin, 3, 3, 30, 30)
        Fonts.fontSFUI35.drawString(entity.name, 34.5f, 4f, Color.WHITE.rgb)
        Fonts.fontSFUI35.drawString("Health: ${decimalFormat.format(entity.health)}", 34.5f, 14f, Color.WHITE.rgb)
        Fonts.fontSFUI35.drawString(
            "Distance: ${decimalFormat.format(mc.thePlayer.getDistanceToEntity(entity))}m",
            34.5f,
            24f,
            Color.WHITE.rgb
        )
        RenderUtils.drawRect(2.5f, 35.5f, width + 11.5f, 37.5f, Color(0, 0, 0, 200).rgb)
        RenderUtils.drawRect(3f, 36f, 3f + (easingHealth / entity.maxHealth) * (width + 8f), 37f, targetInstance.barColor.rgb)
        RenderUtils.drawRect(2.5f, 39.5f, width + 11.5f, 41.5f, Color(0, 0, 0, 200).rgb)
        RenderUtils.drawRect(
            3f,
            40f,
            3f + (entity.totalArmorValue / 20F) * (width + 8f),
            41f,
            Color(77, 128, 255).rgb
        )
        easingHealth += ((entity.health - easingHealth) / 2.0F.pow(10.0F - targetInstance.fadeSpeed.get())) * RenderUtils.deltaTime
    }

    override fun handleBlur(entity: EntityPlayer) {
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        RenderUtils.quickDrawRect(0F, 0F, 132F, 43F)
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()

    }

    override fun handleShadowCut(entity: EntityPlayer) = handleBlur(entity)

    override fun handleShadow(entity: EntityPlayer) {
        RenderUtils.newDrawRect(0F, 0F, 132F, 43F, shadowOpaque.rgb)
    }

    override fun getBorder(entity: EntityPlayer?): Border? {
        return Border(0F, 0F, 132F, 43F)
    }
}