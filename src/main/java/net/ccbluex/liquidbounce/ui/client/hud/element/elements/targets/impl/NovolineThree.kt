package net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.impl

import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Target
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.TargetStyle
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.Palette
import net.ccbluex.liquidbounce.utils.extensions.skin
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.IntegerValue
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.player.EntityPlayer
import java.awt.Color
import java.math.BigDecimal
import kotlin.math.pow

class NovolineThree(inst: Target): TargetStyle("Novoline3", inst, true) {
    private val gredValue = IntegerValue("GradientRed", 255, 0, 255, { targetInstance.styleValue.get().equals("Novoline3", true) })
    private val ggreenValue = IntegerValue("GradientGreen", 255, 0, 255, { targetInstance.styleValue.get().equals("Novoline3", true) })
    private val gblueValue = IntegerValue("GradientBlue", 255, 0, 255, { targetInstance.styleValue.get().equals("Novoline3", true) })
    override fun drawTarget(entity: EntityPlayer) {
        updateAnim(entity.health)
        val width = (38 + Fonts.minecraftFont.getStringWidth(entity.name))
            .coerceAtLeast(118)
            .toFloat()
        targetInstance.counter1[0] += 1
        targetInstance.counter2[0] += 1
        targetInstance.counter1[0] = targetInstance.counter1[0].coerceIn(0, 50)
        targetInstance.counter2[0] = targetInstance.counter2[0].coerceIn(0, 80)
        RenderUtils.drawRect(0F, 0F, width, 34.5F, Color(0, 0, 0, targetInstance.bgAlphaValue.get()))
        val customColor = Color(targetInstance.redValue.get(), targetInstance.greenValue.get(), targetInstance.blueValue.get(), 255)
        val customColor1 = Color(gredValue.get(), ggreenValue.get(), gblueValue.get(), 255)
        RenderUtils.drawGradientSideways(
            34.0, 16.0, width.toDouble() - 2,
            24.0, Color(40, 40, 40, 220).rgb, Color(60, 60, 60, 255).rgb
        )
        RenderUtils.drawGradientSideways(
            34.0, 16.0, (36.0F + (easingHealth / entity.maxHealth) * (width - 36.0F)).toDouble() - 2,
            24.0, Palette.fade2(customColor, targetInstance.counter1[0], Fonts.fontSFUI35.FONT_HEIGHT).rgb,
            Palette.fade2(customColor1, targetInstance.counter2[0], Fonts.fontSFUI35.FONT_HEIGHT).rgb
        )
        easingHealth += ((entity.health - easingHealth) / 2.0F.pow(10.0F - targetInstance.fadeSpeed.get())) * RenderUtils.deltaTime
        Fonts.minecraftFont.drawString(entity.name, 34, 4, Color(255, 255, 255, 255).rgb)
        drawHead(entity.skin, 2, 2, 30, 30)
        Fonts.minecraftFont.drawStringWithShadow(
            BigDecimal((entity.health / entity.maxHealth * 100).toDouble()).setScale(
                1,
                BigDecimal.ROUND_HALF_UP
            ).toString() + "%", width / 2F + 5.5F, 16F, Color.white.rgb
        )
    }

    override fun handleBlur(entity: EntityPlayer) {
        val width = (38 + Fonts.fontSFUI40.getStringWidth(entity.name))
            .coerceAtLeast(118)
            .toFloat()

        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        RenderUtils.quickDrawRect(0F, 0F, 118F, 34F)
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()

    }

    override fun handleShadowCut(entity: EntityPlayer) = handleBlur(entity)

    override fun handleShadow(entity: EntityPlayer) {
        val width = (38 + Fonts.fontSFUI40.getStringWidth(entity.name))
            .coerceAtLeast(118)
            .toFloat()

        RenderUtils.newDrawRect(0F, 0F, 118F, 34F, shadowOpaque.rgb)
    }

    override fun getBorder(entity: EntityPlayer?): Border? {
        return Border(0F, 0F, 118F, 34F)
    }
}