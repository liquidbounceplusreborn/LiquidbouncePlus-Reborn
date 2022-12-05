package net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.impl

import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Target
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.TargetStyle
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.ccbluex.liquidbounce.utils.extensions.skin
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.player.EntityPlayer
import java.awt.Color
import kotlin.math.pow

class LiquidBounceTwo(inst: Target): TargetStyle("LiquidBounce2", inst, true) {
    override fun drawTarget(entity: EntityPlayer) {
        updateAnim(entity.health)
        val width = (38 + Fonts.font40.getStringWidth(entity.name))
            .coerceAtLeast(118)
            .toFloat()
        // Draw rect box
        RenderUtils.drawBorderedRect(0F, 0F, width, 36F, 3F, Color.BLACK.rgb, Color.BLACK.rgb)
        // Damage animation
        if (easingHealth > entity.health)
            RenderUtils.drawRect(
                0F, 34F, (easingHealth / entity.maxHealth) * width,
                36F, Color(252, 185, 65).rgb
            )
        // Health bar
        RenderUtils.drawRect(
            0F, 34F, (entity.health / entity.maxHealth) * width,
            36F, Color(252, 96, 66).rgb
        )
        // Heal animation
        if (easingHealth < entity.health)
            RenderUtils.drawRect(
                (easingHealth / entity.maxHealth) * width, 34F,
                (entity.health / entity.maxHealth) * width, 36F, Color(44, 201, 144).rgb
            )

        easingHealth += ((entity.health - easingHealth) / 2.0F.pow(10.0F - targetInstance.fadeSpeed.get())) * RenderUtils.deltaTime
        Fonts.font40.drawString(entity.name, 36, 3, 0xffffff)
        Fonts.font35.drawString(
            "Distance: ${decimalFormat.format(mc.thePlayer.getDistanceToEntityBox(entity))}",
            36,
            15,
            0xffffff
        )
        // Draw info
        val playerInfo = mc.netHandler.getPlayerInfo(entity.uniqueID)
        if (playerInfo != null) {
            Fonts.font35.drawString(
                "Ping: ${playerInfo.responseTime.coerceAtLeast(0)}",
                36, 24, 0xffffff
            )
            // Draw head
            RenderUtils.drawHead(entity.skin, 30, 30)
        }
    }

    override fun handleBlur(entity: EntityPlayer) {
        val width = (38 + Fonts.fontSFUI40.getStringWidth(entity.name))
            .coerceAtLeast(118)
            .toFloat()

        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        RenderUtils.quickDrawRect(0F,
            0F,
            (36 + mc.thePlayer.name.let(Fonts.font40::getStringWidth)).coerceAtLeast(118).toFloat(),
            36F)
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }

    override fun handleShadowCut(entity: EntityPlayer) = handleBlur(entity)

    override fun handleShadow(entity: EntityPlayer) {
        RenderUtils.newDrawRect(0F,
            0F,
            (36 + mc.thePlayer.name.let(Fonts.font40::getStringWidth)).coerceAtLeast(118).toFloat(),
            36F, shadowOpaque.rgb)
    }

    override fun getBorder(entity: EntityPlayer?): Border? {
        return Border(
            0F,
            0F,
            (36 + mc.thePlayer.name.let(Fonts.font40::getStringWidth)).coerceAtLeast(118).toFloat(),
            36F
        )
    }
}