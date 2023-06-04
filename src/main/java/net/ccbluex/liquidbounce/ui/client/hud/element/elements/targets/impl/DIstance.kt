package net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.impl

import com.mojang.realmsclient.gui.ChatFormatting
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Target
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.TargetStyle
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.extensions.skin
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.RoundedUtil
import net.ccbluex.liquidbounce.utils.render.Stencil
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.player.EntityPlayer
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.pow

class Distance(inst: Target): TargetStyle("Distance", inst, false) {
    override fun drawTarget(entity: EntityPlayer) {
        RenderUtils.drawShadow(0f,
            0f,
            150F,
            30F)
        RoundedUtil.drawRound(
            0f,
            0f,
            150F,
            30F,
            4f,
            Color(25, 25, 25, 255)
        )

        Fonts.fontSFUI35.drawString(entity.name!!, 36, 6, -1)
        Fonts.fontSFUI35.drawString("Distance:   " + ChatFormatting.WHITE + Math.round(entity.getDistance(mc.thePlayer!!.posX,mc.thePlayer!!.posY,mc.thePlayer!!.posZ)) + "m", 36, 18, Color(41,132,163).rgb)
        RenderUtils.drawCircle(123f, 15f,10f, -90, (270f * (easingHealth / 20f)).toInt(), Color(41,132,163))
        Fonts.fontSFUI35.drawCenteredString(Math.round(easingHealth).toString(), 123.1f, 12f, -1)

        var playerInfo = mc.netHandler.getPlayerInfo(mc.thePlayer!!.uniqueID)
        if (entity is EntityOtherPlayerMP) {
            playerInfo = mc.netHandler.getPlayerInfo(entity.uniqueID)
        }
        if (playerInfo != null) {
            val locationSkin = playerInfo.locationSkin

            val renderHurtTime = entity.hurtTime - if (entity.hurtTime != 0) {
                Minecraft.getMinecraft().timer.renderPartialTicks
            } else {
                0f
            }

            val hurtPercent = renderHurtTime / 10.0F
            GL11.glColor4f(1f, 1 - hurtPercent, 1 - hurtPercent, 1f)
            val size = 24

            GL11.glPushMatrix()

            mc.textureManager.bindTexture(locationSkin)
            GL11.glPopMatrix()
        }
    }
    override fun getBorder(entity: EntityPlayer?): Border? {
        return Border(0F,0F,150F,30F)
    }
}
