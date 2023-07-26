package net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.impl

import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Target
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.TargetStyle
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.extensions.skin
import net.ccbluex.liquidbounce.utils.render.AnimationUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.AbstractClientPlayer
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.MathHelper
import java.awt.Color
import kotlin.math.pow

class Hanabi(inst: Target): TargetStyle("Hanabi", inst, true) {
    override fun drawTarget(entity: EntityPlayer) {
        updateAnim(entity.health)
        val blackcolor = Color(0, 0, 0, 180).rgb
        val blackcolor2 = Color(200, 200, 200).rgb
        val health: Float
        var hpPercentage: Double
        val hurt: Color
        val healthStr: String
        val width = (38 + Fonts.fontSFUI40.getStringWidth(entity.name))
            .coerceAtLeast(140)
            .toFloat()
        health = entity.getHealth()
        hpPercentage = (health / entity.getMaxHealth()).toDouble()
        hurt = Color.getHSBColor(310f / 360f, entity.hurtTime.toFloat() / 10f, 1f)
        healthStr = (entity.getHealth().toInt().toFloat() / 2.0f).toString()
        hpPercentage = MathHelper.clamp_double(hpPercentage, 0.0, 1.0)
        val hpWidth = 140.0 * hpPercentage
        targetInstance.healthBarWidth2 = AnimationUtils.animate(hpWidth, targetInstance.healthBarWidth2, 0.20000000298023224)
        targetInstance.healthBarWidth = RenderUtils.getAnimationStateSmooth(
            hpWidth,
            targetInstance.healthBarWidth,
            (14f / Minecraft.getDebugFPS()).toDouble()
        ).toFloat().toDouble()
        targetInstance.hudHeight =
            RenderUtils.getAnimationStateSmooth(40.0, targetInstance.hudHeight, (8f / Minecraft.getDebugFPS()).toDouble())
        if (targetInstance.hudHeight == 0.0) {
            targetInstance.healthBarWidth2 = 140.0
            targetInstance.healthBarWidth = 140.0
        }
        RenderUtils.prepareScissorBox(
            0f,
            (40 - targetInstance.hudHeight).toFloat(),
            (targetInstance.x + 140.0f).toFloat(),
            (targetInstance.y + 40).toFloat()
        )
        RenderUtils.drawRect(0f, 0f, 140.0f, 40.0f, blackcolor)
        RenderUtils.drawRect(0f, 37.0f, 140f, 40f, Color(0, 0, 0, 48).rgb)
        drawHead(entity.skin, 2, 2, 33, 33)
        if (easingHealth > entity.health)
            RenderUtils.drawRect(
                0F,
                37.0f,
                (easingHealth / entity.maxHealth) * width,
                40.0f,
                Color(255, 0, 213, 220).rgb
            )
        // Health bar
        RenderUtils.drawGradientSideways(
            0.0, 37.0, ((entity.health / entity.maxHealth) * width).toDouble(),
            40.0, targetInstance.barColor.rgb, targetInstance.barColor.rgb
        )
        easingHealth += ((entity.health - easingHealth) / 2.0F.pow(10.0F - targetInstance.fadeSpeed.get())) * RenderUtils.deltaTime
        Fonts.fontSFUI35.drawStringWithShadow("❤", 112F, 28F, hurt.rgb)
        Fonts.fontSFUI35.drawStringWithShadow(healthStr, 120F, 28F, Color.WHITE.rgb)
        Fonts.fontSFUI35.drawString(
            "XYZ:" + entity.posX.toInt() + " " + entity.posY.toInt() + " " + entity.posZ.toInt() + " | " + "Hurt:" + (entity.hurtTime > 0),
            38F,
            15f,
            blackcolor2
        )
        Fonts.fontSFUI40.drawString(entity.getName(), 38.0f, 4.0f, blackcolor2)
        mc.textureManager.bindTexture((entity as AbstractClientPlayer).locationSkin)
        Gui.drawScaledCustomSizeModalRect(3, 3, 8.0f, 8.0f, 8, 8, 32, 32, 64f, 64f)
    }

    override fun handleBlur(entity: EntityPlayer) {
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        RenderUtils.fastRoundedRect(0F, 0F, 140F, 40F, 7F)
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }

    override fun handleShadowCut(entity: EntityPlayer) = handleBlur(entity)

    override fun handleShadow(entity: EntityPlayer) {

        RenderUtils.originalRoundedRect(0F, 0F, 140F, 40F, 8F, shadowOpaque.rgb)
    }

    override fun getBorder(entity: EntityPlayer?): Border? {
        return Border(0F, 0F, 140F, 40F)
    }
}