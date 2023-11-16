/*
 * LiquidBounce++ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/PlusPlusMC/LiquidBouncePlusPlus/
 */
package net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.impl

import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Target
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.TargetStyle
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.extensions.getHealth1D
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawEntityOnScreen
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import org.lwjgl.opengl.GL11

class Astolfo2(inst: Target) : TargetStyle("Astolfo2", inst, true) {

    override fun drawTarget(entity: EntityPlayer) {
        val font = Fonts.minecraftFont

        updateAnim(entity.health)

        RenderUtils.drawRect(0F, 0F, 190F, 74F, targetInstance.bgColor.rgb)
        GlStateManager.resetColor()
        GL11.glColor4f(1F, 1F, 1F, 1F)

        GL11.glPushMatrix()
        drawEntityOnScreen(23, 57, 25, entity)
        GL11.glPopMatrix()

        GL11.glPushMatrix()
        drawArmor(entity)
        GL11.glPopMatrix()

        GL11.glPushMatrix()
        GL11.glScalef(2f, 2f, 2f)
        font.drawStringWithShadow(entity.name, 22f, 2f, getColor(-1).rgb)
        font.drawStringWithShadow("${entity.getHealth1D()}", 22f, 10f, targetInstance.barColor.rgb)
        GL11.glPopMatrix()

        GL11.glPushMatrix()
        GL11.glScalef(1.5f, 1.5f, 1.5f)
        font.drawStringWithShadow("❤", 50f, 15f, targetInstance.barColor.rgb)
        GL11.glPopMatrix()

        RenderUtils.newDrawRect(3F, 66F, 2F + (easingHealth / entity.maxHealth) * 185F, 71F, targetInstance.barColor.rgb)
    }

    private fun drawArmor(entity: EntityPlayer) {
        val renderItem = mc.renderItem
        val x = 172F
        var y = 1F
        val step = 16F

        RenderHelper.enableGUIStandardItemLighting()
        for (index in 4 downTo 1) {
            val stack = entity.getEquipmentInSlot(index)
            if (stack == null) {
                y += step
                continue
            }
            renderItem.renderItemAndEffectIntoGUI(stack, x.toInt(), y.toInt())
//            renderItem.renderItemOverlays(mc.fontRendererObj, stack, x.toInt(), y.toInt())
            y += step
        }
        RenderHelper.disableStandardItemLighting()
    }

    override fun handleBlur(entity: EntityPlayer) {
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        RenderUtils.fastRoundedRect(0F, 0F, 190F, 68F, 7F)
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }

    override fun handleShadowCut(entity: EntityPlayer) = handleBlur(entity)

    override fun handleShadow(entity: EntityPlayer) {
        RenderUtils.originalRoundedRect(0F, 0F, 190F, 68F, 8F, shadowOpaque.rgb)
    }

    override fun getBorder(entity: EntityPlayer?): Border {
        return Border(0F, 0F, 190F, 74F)
    }

    private fun getHealth1Places(entity: EntityLivingBase?): Float {
        return if (entity == null || entity.isDead) {
            0f
        } else {
            entity.getHealth1D()
        }
    }
}