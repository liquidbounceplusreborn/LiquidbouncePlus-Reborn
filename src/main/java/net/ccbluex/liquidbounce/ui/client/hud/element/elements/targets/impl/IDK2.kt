package net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.impl

import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Target
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.TargetStyle
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.MathHelper
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.abs
import kotlin.math.pow

class IDK2(inst: Target): TargetStyle("IDK2", inst, false) {

    override fun drawTarget(entity: EntityPlayer) {
        if (easingHealth < 0 || easingHealth > entity.maxHealth ||
            abs(easingHealth - entity.health) < 0.01) {
            easingHealth = entity.health
        }

        var c = targetInstance.barColor

        RenderUtils.rectangleBordered(0.0, 0.0, 100.0, 44f.toDouble(), 1.0, Color(95,95,95,255).rgb, Color(0,0,0,255).rgb)
        RenderUtils.rectangleBordered(1.5, 1.5, 100.toDouble() - 1.5, 44f.toDouble() - 1.5, 1.0, Color(45,45,45,255).rgb, Color(65,65,65,255).rgb)

        val startPos = 6.0
        val barWidth = 100f.toDouble() - startPos * 2.0
        RenderUtils.rectangle(startPos - 0.5, 15.5, (startPos + (barWidth))+0.5,
            26.5, Color(25,25,25,255).rgb)

        RenderUtils.drawGradientSideways(startPos, 16.0, startPos + ((easingHealth / entity.maxHealth) * barWidth),
            26.0, c.rgb, c.rgb)

        mc.fontRendererObj.drawStringWithShadow(((entity.health * 10).toInt() / 10.0F).toString() + " HP", 100f / 2.3F, 18.0F, -1)

        easingHealth += ((entity.health - easingHealth) / 2.0F.pow(10.0F - targetInstance.animProgress)) * RenderUtils.deltaTime

        entity.name?.let { mc.fontRendererObj.drawStringWithShadow(it, (startPos.toInt() + 2).toFloat(), 6f, 0xffffff) }

        mc.fontRendererObj.drawStringWithShadow("Distance: " + mc.thePlayer!!.getDistanceToEntity(entity).toInt() + "m",
            (startPos.toInt() + 2).toFloat(), 30f, 0xffffff)
    }

    override fun getBorder(entity: EntityPlayer?): Border? {
        return Border(0F, 0F, 100f, 44f)
    }
}
