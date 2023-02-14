package net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.impl

import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Target
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.TargetStyle
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.extensions.skin
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.player.EntityPlayer
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.min
import kotlin.math.pow

class AsuidBounce(inst: Target): TargetStyle("AsuidBounce", inst, true) {
    override fun drawTarget(entity: EntityPlayer) {
        updateAnim(entity.health)
        GL11.glPushMatrix()
        RenderUtils.drawBorderedRect(0F, 0F, 120F, 38f, 2f, Color(5, 5, 5, 255).rgb, Color(25, 25, 25, 255).rgb)
        RenderUtils.drawRect(1f, 34f, 119f, 37f, Color(50, 50, 50, 255).rgb)
        RenderUtils.drawRect(75F, 25.5f, 115F, 29.5f, Color(35, 35, 35, 255).rgb)
        RenderUtils.drawRect(75F, 15.5f, 115F, 19.5f, Color(35, 35, 35, 255).rgb)
        drawHead(entity.skin, 2, 2, 30, 30)
        RenderUtils.drawOutlinedRect(1f, 1f, 33f, 33f, 0.5f, Color(65, 65, 65, 255).rgb)
        Fonts.fontSFUI35.drawString(entity.getName(), 36F, 3f, Color.WHITE.rgb)
        Fonts.fontSFUI35.drawString("Distance", 36F, 14f, Color.WHITE.rgb)
        Fonts.fontSFUI35.drawString(
            "Armor " + decimalFormat2.format(entity.getTotalArmorValue() / 2f),
            36F,
            24f,
            Color.WHITE.rgb
        )
        RenderUtils.drawRect(
            1f,
            34f,
            1f + min((Math.round(easingHealth / entity.getMaxHealth() * 10000) / 80f), 118f),
            37f,
            targetInstance.barColor
        )
        RenderUtils.drawRect(
            75F,
            25.5f,
            75F + min((Math.round(entity.getTotalArmorValue() / 20F * 10000) / 250f), 40f),
            29.5f,
            Color(170, 145, 100).rgb
        )
        RenderUtils.drawRect(
            75F,
            15.5f,
            75F + min((Math.round(mc.thePlayer.getDistanceToEntity(entity) / 10F * 10000) / 250f), 40f),
            19.5f,
            Color(175, 100, 80).rgb
        )
        RenderUtils.drawOutlinedRect(74.5f, 25f, 115.5f, 30f, 0.5f, Color(0, 0, 0, 255).rgb)
        RenderUtils.drawOutlinedRect(74.5f, 15f, 115.5f, 20f, 0.5f, Color(0, 0, 0, 255).rgb)
        easingHealth += ((entity.health - easingHealth) / 2.0F.pow(10.0F - targetInstance.fadeSpeed.get())) * RenderUtils.deltaTime
        GL11.glPopMatrix()
    }

    override fun handleBlur(entity: EntityPlayer) {
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        RenderUtils.fastRoundedRect(0F, 0F, 120F, 38F, 7F)
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }

    override fun handleShadowCut(entity: EntityPlayer) = handleBlur(entity)

    override fun handleShadow(entity: EntityPlayer) {
        RenderUtils.newDrawRect(0F, 0F, 120F, 38F, shadowOpaque.rgb)
    }

    override fun getBorder(entity: EntityPlayer?): Border? {
        return Border(0F, 0F, 120F, 38F)
    }
}