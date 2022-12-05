/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.impl

import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Target
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.TargetStyle
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import java.awt.Color

import kotlin.math.abs

class Moon(inst: Target): TargetStyle("Moon", inst, true) {


    private var lastTarget: EntityPlayer? = null

    override fun drawTarget(entity: EntityPlayer) {
        updateAnim(entity.health)
        if (entity != lastTarget || easingHealth < 0 || easingHealth > entity.maxHealth ||
            abs(easingHealth - entity.health) < 0.01) {
            easingHealth = entity.health
        }

        val width = (38 + Fonts.fontSFUI40.getStringWidth(entity.name))
                .coerceAtLeast(118)
                .toFloat()

        // Draw rect box 1
            RenderUtils.drawRect(0F, 0F, width + 40.5f, 51.5F, Color(0,0,0, 100).rgb)
            RenderUtils.drawBorder(0F, 0F, width + 40.5f, 51.5F, 2f, getColor(Color.black.darker().darker()).rgb)

        // Health bar
        RenderUtils.drawBorder(53f, 41f, 155.5f, 48.5f, 2f, getColor(Color.black.darker().darker()).rgb)
        RenderUtils.newDrawRect(53F, 41f, 155.5F, 48.5F, getColor(Color.darkGray.darker().darker()).rgb)
        RenderUtils.newDrawRect(53F, 41f, 17.5F + (easingHealth / entity.maxHealth).coerceIn(0F, 1F) * 138F, 48.5F, targetInstance.barColor.rgb)

        updateAnim(entity.health)

        Fonts.fontSFUI35.drawString("Name: " + entity.name, 53.5f, 3.5f, getColor(-1).rgb)
        Fonts.fontSFUI35.drawString("Distance: ${decimalFormat.format(mc.thePlayer.getDistanceToEntityBox(entity))}", 53.5f, 13f, getColor(-1).rgb)
        Fonts.fontSFUI35.drawString("Health: ${(decimalFormat.format(entity.health))}", 53.5f, 22f, targetInstance.barColor.rgb)

        // Draw info
        val playerInfo = mc.netHandler.getPlayerInfo(entity.uniqueID)
        if (playerInfo != null) {
            Fonts.fontSFUI35.drawString("Ping: ${playerInfo.responseTime.coerceAtLeast(0)}" + "ms",
                53.5f, 31.5f, Color(120,120,120).rgb)

            // Draw head
            val locationSkin = playerInfo.locationSkin
                drawHead(locationSkin,
                    0.5f,
                    0.5f,
                    1.68F,
                    30, 30,
                    1F, 0.4F + 0.6F, 0.4F + 0.6F)
            RenderUtils.drawBorder(0.5f, 0.5f, 51f, 51f, 2f, getColor(Color.black.darker().darker()).rgb)
        }

        lastTarget = entity
    }

    override fun handleBlur(entity: EntityPlayer) {
        val width = (38 + Fonts.fontSFUI40.getStringWidth(entity.name))
                        .coerceAtLeast(118)
                        .toFloat()

        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        RenderUtils.quickDrawRect(0F, 0F, width + 40.5f, 51.5F)
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }

    override fun handleShadowCut(entity: EntityPlayer) = handleBlur(entity)

    override fun handleShadow(entity: EntityPlayer) {
        val width = (38 + Fonts.fontSFUI40.getStringWidth(entity.name))
                        .coerceAtLeast(118)
                        .toFloat()

        RenderUtils.newDrawRect(0F, 0F, width + 40.5f, 51.5F, shadowOpaque.rgb)
    }

    override fun getBorder(entity: EntityPlayer?): Border? {
        entity ?: return Border(0F, 0F, 118F + 40.5f, 51.5F)
        val width = (38 + Fonts.fontSFUI40.getStringWidth(entity.name))
                        .coerceAtLeast(118)
                        .toFloat()
        return Border(0F, 0F, width + 40.5f, 51.5F)
    }

    private fun getHealth2(entity: EntityLivingBase?):Float{
        return if(entity==null || entity.isDead){ 0f }else{ entity.health }
    }

}