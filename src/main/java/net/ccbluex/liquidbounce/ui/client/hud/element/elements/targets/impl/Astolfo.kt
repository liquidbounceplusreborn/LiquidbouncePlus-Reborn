package net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.impl

import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Target
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.TargetStyle
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.extensions.darker
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.player.EntityPlayer
import java.awt.Color
import kotlin.math.pow

class Astolfo(inst: Target): TargetStyle("Astolfo", inst, true) {
    override fun drawTarget(entity: EntityPlayer) {
        updateAnim(entity.health)
        val colors = targetInstance.barColor.rgb
        val colors1 = targetInstance.barColor.darker(1.0f).rgb
        val colors2 = targetInstance.barColor.darker(0.5f).rgb
        val additionalWidth = Fonts.minecraftFont.getStringWidth("${entity.name}").coerceAtLeast(125)
        GlStateManager.pushMatrix()
        GlStateManager.translate((15).toFloat(), 55.toFloat(), 0.0f)
        GlStateManager.color(1f, 1f, 1f)
        GuiInventory.drawEntityOnScreen(-18, 47, 30, -180f, 0f, entity)
        RenderUtils.drawRect(
            -38.0,
            -14.0,
            133.0,
            52.0,
            net.ccbluex.liquidbounce.utils.render.Colors.getColor(0, 0, 0, 180)
        )
        mc.fontRendererObj.drawStringWithShadow(entity.getName(), 0.0f, -8.0f, Color(255, 255, 255).rgb)
        RenderUtils.drawRect(0.0, (8.0f + Math.round(40.0f)).toDouble(), 130.0, 40.0, colors2)
        if (entity.getHealth() / 2.0f + entity.getAbsorptionAmount() / 2.0f > 1.0) {
            RenderUtils.drawRect(
                0.0,
                (8.0f + Math.round(40.0f)).toDouble(),
                ((entity.health / entity.maxHealth) * additionalWidth).toDouble() + 5f,
                40.0,
                colors1
            )
        }
        RenderUtils.drawRect(
            0.0,
            (8.0f + Math.round(40.0f)).toDouble(),
            ((easingHealth / entity.maxHealth) * additionalWidth).toDouble(),
            40.0,
            colors
        )
        easingHealth += ((entity.health - easingHealth) / 2.0F.pow(10.0F - targetInstance.fadeSpeed.get())) * RenderUtils.deltaTime
        GlStateManager.scale(3f, 3f, 3f)
        mc.fontRendererObj.drawStringWithShadow(
            "${entity.health.toInt()}\u2764",
            0.0f,
            2.5f,
            colors
        )
        GlStateManager.popMatrix()
    }

    override fun handleBlur(entity: EntityPlayer) {
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        RenderUtils.fastRoundedRect(-23F, 41F, 148F, 107F, 7F)
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }

    override fun handleShadowCut(entity: EntityPlayer) = handleBlur(entity)

    override fun handleShadow(entity: EntityPlayer) {

        RenderUtils.originalRoundedRect(-23F, 41F, 148F, 107F, 8F, shadowOpaque.rgb)
    }

    override fun getBorder(entity: EntityPlayer?): Border? {
        return Border(-23F, 41F, 148F, 107F)
    }
}
