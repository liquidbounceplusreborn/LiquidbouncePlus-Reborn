package net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.impl

import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Target
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.TargetStyle
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.extensions.skin
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.Stencil
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.player.EntityPlayer
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.pow

class Moon4(inst: Target): TargetStyle("Moon4.0", inst, true) {
    override fun drawTarget(entity: EntityPlayer) {
        updateAnim(entity.health)
        val mainColor = targetInstance.barColor
        val percent = entity.health.toInt()
        // val nameLength = (Fonts.fontTahoma.getStringWidth(entity.name)).coerceAtLeast(
        val nameLength = (Fonts.fontSFUI35.getStringWidth(entity.name)).coerceAtLeast(
        //   Fonts.fontTahoma.getStringWidth(
            Fonts.fontSFUI35.getStringWidth(
                "${
                    decimalFormat2.format(percent)
                }"
            )
        ).toFloat() + 20F
        val barWidth = (entity.health / entity.maxHealth).coerceIn(0F, entity.maxHealth) * (nameLength - 2F)
        RenderUtils.drawRoundedRect(-2F, -2F, 3F + nameLength + 36F, 2F + 36F, 3f,Color(50, 50, 50, targetInstance.bgAlphaValue.get()).rgb)
        RenderUtils.drawRoundedRect(-1F, -1F, 2F + nameLength + 36F, 1F + 36F,3f, Color(0, 0, 0, 50).rgb)
        if (entity != null) {
            Stencil.write(false)
            GL11.glDisable(GL11.GL_TEXTURE_2D)
            GL11.glEnable(GL11.GL_BLEND)
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
            RenderUtils.fastRoundedRect(1f, 0.5f, 36F, 35.5F, 5F)
            GL11.glDisable(GL11.GL_BLEND)
            GL11.glEnable(GL11.GL_TEXTURE_2D)
            Stencil.erase(true)
            drawHead(entity.skin, 1, 0.5.toInt(), 35, 35, 1F - targetInstance.getFadeProgress())
            Stencil.dispose()
        }
        // Fonts.fontTahoma.drawStringWithShadow(entity.name, 2F + 36F, 2F, -1)
        Fonts.fontSFUI35.drawStringWithShadow(entity.name, 2F + 36F, 2F, -1)
        RenderUtils.drawRoundedRect(37F, 23F, 37F + nameLength, 33f, 3f,Color(0, 0, 0, 100).rgb)
        easingHealth += ((entity.health - easingHealth) / 2.0F.pow(10.0F - targetInstance.fadeSpeed.get())) * RenderUtils.deltaTime
        val animateThingy =
            (easingHealth.coerceIn(entity.health, entity.maxHealth) / entity.maxHealth) * (nameLength - 2F)
        if (easingHealth > entity.health)
            RenderUtils.drawRoundedRect(38F, 24F, 38F + animateThingy, 32f,3f, mainColor.darker().rgb)
        RenderUtils.drawRoundedRect(38F, 24f, 38F + barWidth, 32f,3f, mainColor.rgb)
        Fonts.fontSFUI35.drawStringWithShadow("${decimalFormat2.format(percent)}HP", 38F, 15F, Color.WHITE.rgb)
    }

    override fun handleBlur(entity: EntityPlayer) {
        val percent = entity.health.toInt()
        // val nameLength = (Fonts.fontTahoma.getStringWidth(entity.name)).coerceAtLeast(
        val nameLength = (Fonts.fontSFUI35.getStringWidth(entity.name)).coerceAtLeast(
            // Fonts.fontTahoma.getStringWidth(
            Fonts.fontSFUI35.getStringWidth(
                "${
                    decimalFormat2.format(percent)
                }"
            )
        ).toFloat() + 18f
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        RenderUtils.fastRoundedRect(-1F, -1F, nameLength + 40, 37F,5f)
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }

    override fun handleShadowCut(entity: EntityPlayer) = handleBlur(entity)

    override fun handleShadow(entity: EntityPlayer) {
        val percent = entity.health.toInt()
        // val nameLength = (Fonts.fontTahoma.getStringWidth(entity.name)).coerceAtLeast(
        val nameLength = (Fonts.fontSFUI35.getStringWidth(entity.name)).coerceAtLeast(
        // Fonts.fontTahoma.getStringWidth(
            Fonts.fontSFUI35.getStringWidth(
                "${
                    decimalFormat2.format(percent)
                }"
            )
        ).toFloat() + 18f
        RenderUtils.originalRoundedRect(-1F, -1F, nameLength + 40, 37F,5f, shadowOpaque.rgb)
    }

    override fun getBorder(entity: EntityPlayer?): Border? {
        val percent = entity?.health?.toInt()
        // val nameLength = (Fonts.fontTahoma.getStringWidth(entity?.name.toString())).coerceAtLeast(
        val nameLength = (Fonts.fontSFUI35.getStringWidth(entity?.name.toString())).coerceAtLeast(
            // Fonts.fontTahoma.getStringWidth(
            Fonts.fontSFUI35.getStringWidth(
                "${
                    decimalFormat2.format(percent)
                }"
            )
        ).toFloat() + 18F
        return Border(-1F, -2F, nameLength + 40, 38F)
    }
}