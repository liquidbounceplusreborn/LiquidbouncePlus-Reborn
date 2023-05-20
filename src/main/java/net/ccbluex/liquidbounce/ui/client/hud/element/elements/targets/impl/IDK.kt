package net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.impl

import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Target
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.TargetStyle
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawExhiRect
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.MathHelper
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.pow

class IDK(inst: Target): TargetStyle("IDK", inst, false) {

    override fun drawTarget(entity: EntityPlayer) {
        easingHealth += ((entity.health - easingHealth) / 2.0F.pow(10.0F - targetInstance.animProgress)) * RenderUtils.deltaTime

        drawExhiRect(0F, 0F, 160F, 60F)

        GlStateManager.resetColor()
        GL11.glColor4f(1F, 1F, 1F, 1F)

        for (i in 0..4) {
            val percent = easingHealth / entity.maxHealth
            val huh =
                (MathHelper.clamp_float(percent, i.toFloat() / 5F, (i.toFloat() + 1F) / 5F) - (i.toFloat() / 5F)) / 0.2F
            val w = huh * 29F

            RenderUtils.drawRect(
                5F + (i.toFloat() * 30F),
                54F,
                5F + (i.toFloat() * 30F) + w,
                55F,
                targetInstance.barColor.rgb
            )
        }

        GlStateManager.resetColor()
        GL11.glColor4f(1F, 1F, 1F, 1F)

        Fonts.minecraftFont.drawStringWithShadow(
            "${decimalFormat2.format(mc.thePlayer.getDistanceToEntityBox(entity))} m",
            6F,
            30F,
            -1
        )
        Fonts.minecraftFont.drawStringWithShadow(entity.name, 6F, 40F, -1)

        GL11.glPushMatrix()
        GL11.glTranslatef(155F, 20F, 0F)
        GL11.glScalef(2F, 2F, 0F)
        Fonts.minecraftFont.drawStringWithShadow(
            "${decimalFormat3.format(entity.health)} ❤",
            -Fonts.minecraftFont.getStringWidth("${decimalFormat3.format(entity.health)} ❤").toFloat(),
            0F,
            -1
        )
        GL11.glPopMatrix()
    }

    override fun getBorder(entity: EntityPlayer?): Border? {
        return Border(0F, 0F, 160F, 60F)
    }
}
