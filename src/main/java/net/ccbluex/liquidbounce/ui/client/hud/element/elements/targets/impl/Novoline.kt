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

class Novoline(inst: Target): TargetStyle("Novoline", inst, true) {
    override fun drawTarget(entity: EntityPlayer) {
        updateAnim(entity.health)
        val mainColor = targetInstance.barColor
        val percent = entity.health.toInt()
        val nameLength = (Fonts.minecraftFont.getStringWidth(entity.name)).coerceAtLeast(
            Fonts.minecraftFont.getStringWidth(
                "${
                    decimalFormat.format(percent)
                }"
            )
        ).toFloat() + 20F
        val barWidth = (entity.health / entity.maxHealth).coerceIn(0F, entity.maxHealth) * (nameLength - 2F)
        RenderUtils.drawRect(-2F, -2F, 3F + nameLength + 36F, 2F + 36F, Color(50, 50, 50, 150).rgb)
        RenderUtils.drawRect(-1F, -1F, 2F + nameLength + 36F, 1F + 36F, Color(0, 0, 0, 100).rgb)
        drawHead(entity.skin, 0, 0, 36, 36)
        Fonts.minecraftFont.drawStringWithShadow(entity.name, 2F + 36F, 2F, -1)
        RenderUtils.drawRect(37F, 14F, 37F + nameLength, 24F, Color(0, 0, 0, 200).rgb)
        easingHealth += ((entity.health - easingHealth) / 2.0F.pow(10.0F - targetInstance.fadeSpeed.get())) * RenderUtils.deltaTime
        val animateThingy =
            (easingHealth.coerceIn(entity.health, entity.maxHealth) / entity.maxHealth) * (nameLength - 2F)
        if (easingHealth > entity.health)
            RenderUtils.drawRect(38F, 15F, 38F + animateThingy, 23F, mainColor.darker().rgb)
        RenderUtils.drawRect(38F, 15F, 38F + barWidth, 23F, mainColor.rgb)
        Fonts.minecraftFont.drawStringWithShadow("${decimalFormat.format(percent)}", 38F, 26F, Color.WHITE.rgb)
        Fonts.fontSFUI35.drawStringWithShadow(
            "❤",
            Fonts.minecraftFont.getStringWidth("${decimalFormat.format(percent)}") + 40F,
            27F,
            Color(targetInstance.redValue.get(), targetInstance.greenValue.get(), targetInstance.blueValue.get()).rgb
        )
    }

    override fun handleBlur(entity: EntityPlayer) {
        val percent = entity.health.toInt()
        val nameLength = (Fonts.minecraftFont.getStringWidth(entity.name)).coerceAtLeast(
            Fonts.minecraftFont.getStringWidth(
                "${
                    decimalFormat.format(percent)
                }"
            )
        ).toFloat() + 20F
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        RenderUtils.quickDrawRect(-1F, -2F, nameLength + 40, 38F)
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }

    override fun handleShadowCut(entity: EntityPlayer) = handleBlur(entity)

    override fun handleShadow(entity: EntityPlayer) {
        val percent = entity.health.toInt()
        val nameLength = (Fonts.minecraftFont.getStringWidth(entity.name)).coerceAtLeast(
            Fonts.minecraftFont.getStringWidth(
                "${
                    decimalFormat.format(percent)
                }"
            )
        ).toFloat() + 20F
        RenderUtils.newDrawRect(-1F, -2F, nameLength + 40, 38F, shadowOpaque.rgb)
    }

    override fun getBorder(entity: EntityPlayer?): Border? {
        val percent = entity?.health?.toInt()
        val nameLength = (Fonts.minecraftFont.getStringWidth(entity?.name)).coerceAtLeast(
            Fonts.minecraftFont.getStringWidth(
                "${
                    decimalFormat.format(percent)
                }"
            )
        ).toFloat() + 20F
        return Border(-1F, -2F, nameLength + 40, 38F)
    }
}