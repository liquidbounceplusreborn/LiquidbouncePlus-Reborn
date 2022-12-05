package net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.impl

import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Target
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.TargetStyle
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.Colors
import net.ccbluex.liquidbounce.utils.PlayerUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.player.EntityPlayer
import kotlin.math.pow

class Simplicity(inst: Target): TargetStyle("Simplicity", inst, true) {
    override fun drawTarget(entity: EntityPlayer) {
        updateAnim(entity.health)
        GlStateManager.pushMatrix()
        var width = 100.0
        width = PlayerUtils.getIncremental(width, -50.0)
        Fonts.font35.drawStringWithShadow("\u00a7l" + entity.getName(), (38).toFloat(), 2.0f, -1)
        if (width < 80.0) {
            width = 80.0
        }
        if (width > 80.0) {
            width = 80.0
        }
        RenderUtils.drawGradientSideways(
            37.5,
            11.toDouble(),
            37.5 + (easingHealth / entity.maxHealth) * width,
            (19).toDouble(),
            ColorUtils.rainbow(5000000000L).rgb,
            ColorUtils.rainbow(500L).rgb
        )
        RenderUtils.rectangleBordered(
            37.0,
            10.5,
            38.0 + (easingHealth / entity.maxHealth) * width,
            19.5,
            0.5,
            Colors.getColor(0, 0),
            Colors.getColor(0)
        )
        easingHealth += ((entity.health - easingHealth) / 2.0F.pow(10.0F - targetInstance.fadeSpeed.get())) * RenderUtils.deltaTime
        GlStateManager.resetColor()
        GlStateManager.popMatrix()
    }

    override fun handleBlur(entity: EntityPlayer) {
        val width = (38 + Fonts.fontSFUI40.getStringWidth(entity.name))
            .coerceAtLeast(118)
            .toFloat()

        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        RenderUtils.quickDrawRect(0F, 0F, width, 32F)
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()

    }

    override fun getBorder(entity: EntityPlayer?): Border? {
        return Border(37F, 0F, 119F, 20F)
    }
}