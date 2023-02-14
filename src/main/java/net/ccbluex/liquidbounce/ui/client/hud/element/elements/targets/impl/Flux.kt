package net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.impl

import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Target
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.TargetStyle
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.extensions.skin
import net.ccbluex.liquidbounce.utils.render.BlendUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.player.EntityPlayer
import java.awt.Color
import kotlin.math.pow

class Flux(inst: Target): TargetStyle("Flux", inst, true) {
    override fun drawTarget(entity: EntityPlayer) {
        updateAnim(entity.health)
        val hp = decimalFormat.format(easingHealth)
        val additionalWidth = Fonts.minecraftFont.getStringWidth("${entity.name}  ${hp} hp").coerceAtLeast(75)
        RenderUtils.drawCircleRect(
            0f,
            0f,
            45f + additionalWidth,
            34f,
            5f,
            Color(0, 0, 0, targetInstance.bgAlphaValue.get()).rgb
        )
        drawHead(entity.skin, 5, 3, 29, 28)
        RenderUtils.drawOutlinedRect(5f, 2f, 35f, 32f, 1f, targetInstance.bordercolor.rgb)
        // info text
        Fonts.minecraftFont.drawString(entity.name, 40, 5, Color.WHITE.rgb)
        "$hp hp".also {
            Fonts.minecraftFont.drawString(
                it,
                40 + additionalWidth - Fonts.minecraftFont.getStringWidth(it),
                5,
                Color.LIGHT_GRAY.rgb
            )
        }
        // hp bar
        val yPos = 5 + Fonts.minecraftFont.FONT_HEIGHT + 2f
        if (easingHealth > entity.health) {
            if (targetInstance.colorModeValue.get().equals("Custom")) {
                RenderUtils.drawRect(
                    40f,
                    yPos,
                    40 + (easingHealth / entity.maxHealth) * additionalWidth,
                    yPos + 3.5f,
                    Color(targetInstance.redValue.get(), targetInstance.greenValue.get(), targetInstance.blueValue.get(), 150)
                )
            } else if (targetInstance.colorModeValue.get().equals("Health")) {
                RenderUtils.drawRect(
                    40f,
                    yPos,
                    40 + (easingHealth / entity.maxHealth) * additionalWidth,
                    yPos + 3.5f,
                    BlendUtils.getHealthColor(entity.health, entity.maxHealth)
                )
            }
        }
        RenderUtils.drawRect(
            40f,
            yPos,
            40 + (entity.health / entity.maxHealth) * additionalWidth,
            yPos + 3.5f,
            targetInstance.barColor
        )
        RenderUtils.drawRect(
            40f,
            yPos + 9,
            40 + (entity.totalArmorValue / 20F) * additionalWidth,
            yPos + 12.5f,
            Color(77, 128, 255).rgb
        )
        easingHealth += ((entity.health - easingHealth) / 2.0F.pow(10.0F - targetInstance.fadeSpeed.get())) * RenderUtils.deltaTime
    }

    override fun handleBlur(entity: EntityPlayer) {
        val additionalWidth = Fonts.minecraftFont.getStringWidth(entity?.name).coerceAtLeast(75)
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        RenderUtils.fastRoundedRect(0F, 0F, additionalWidth.toFloat() + 54, 34F, 7F)
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }

    override fun handleShadowCut(entity: EntityPlayer) = handleBlur(entity)

    override fun handleShadow(entity: EntityPlayer) {
        val additionalWidth = Fonts.minecraftFont.getStringWidth(entity?.name).coerceAtLeast(75)

        RenderUtils.originalRoundedRect(0F, 0F, additionalWidth.toFloat() + 54, 34F, 8F, shadowOpaque.rgb)
    }

    override fun getBorder(entity: EntityPlayer?): Border? {
        val additionalWidth = Fonts.minecraftFont.getStringWidth(entity?.name).coerceAtLeast(75)
        return Border(0F, 0F, additionalWidth.toFloat() + 54, 34F)
    }
}