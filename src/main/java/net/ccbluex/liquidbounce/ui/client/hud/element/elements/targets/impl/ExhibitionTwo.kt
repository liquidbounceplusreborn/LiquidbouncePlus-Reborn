package net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.impl

import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Target
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.TargetStyle
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.ccbluex.liquidbounce.utils.render.BlendUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.entity.player.EntityPlayer
import org.lwjgl.opengl.GL11
import java.awt.Color

class ExhibitionTwo(inst: Target): TargetStyle("Exhibition2", inst, true) {
    override fun drawTarget(entity: EntityPlayer) {
        updateAnim(entity.health)
        val minWidth = 140F.coerceAtLeast(45F + Fonts.fontTahoma.getStringWidth(entity.name))
        RenderUtils.drawExhiRect(0F, 0F, minWidth, 45F)
        RenderUtils.drawRect(2.5F, 2.5F, 42.5F, 42.5F, Color(59, 59, 59).rgb)
        RenderUtils.drawRect(3F, 3F, 42F, 42F, Color(19, 19, 19).rgb)
        GL11.glColor4f(1f, 1f, 1f, 1f)
        RenderUtils.drawEntityOnScreen(22, 40, 15, entity)
        Fonts.fontTahoma.drawString(entity.name, 46, 4, -1)
        val barLength = 75F * (entity.health / entity.maxHealth).coerceIn(0F, 1F)
        RenderUtils.drawRect(
            45F,
            15F,
            45F + 60F,
            18F,
            BlendUtils.getHealthColor(entity.health, entity.maxHealth).darker().darker().darker().rgb
        )
        RenderUtils.drawRect(
            45F,
            15F,
            45F + barLength,
            18F,
            BlendUtils.getHealthColor(entity.health, entity.maxHealth).rgb
        )
        for (i in 0..9) {
            RenderUtils.drawBorder(45F + i * 6F, 15F, 45F + (i + 1F) * 6F, 18F, 0.25F, Color.black.rgb)
        }
        GL11.glPushMatrix()
        GL11.glTranslatef(46F, 20F, 0F)
        GL11.glScalef(0.5f, 0.5f, 0.5f)
        Fonts.minecraftFont.drawString(
            "HP: ${entity.health.toInt()} | Dist: ${
                mc.thePlayer.getDistanceToEntityBox(
                    entity
                ).toInt()
            }", 0, 0, -1
        )
        GL11.glPopMatrix()
        GlStateManager.resetColor()
        GL11.glPushMatrix()
        GL11.glColor4f(1f, 1f, 1f, 1f)
        RenderHelper.enableGUIStandardItemLighting()
        val renderItem = mc.renderItem
        var x = 45
        var y = 26
        for (index in 3 downTo 0) {
            val stack = entity.inventory.armorInventory[index] ?: continue
            if (stack.getItem() == null)
                continue
            renderItem.renderItemIntoGUI(stack, x, y)
            renderItem.renderItemOverlays(mc.fontRendererObj, stack, x, y)
            x += 18
        }
        val mainStack = entity.heldItem
        if (mainStack != null && mainStack.getItem() != null) {
            renderItem.renderItemIntoGUI(mainStack, x, y)
            renderItem.renderItemOverlays(mc.fontRendererObj, mainStack, x, y)
        }
        RenderHelper.disableStandardItemLighting()
        GlStateManager.enableAlpha()
        GlStateManager.disableBlend()
        GlStateManager.disableLighting()
        GlStateManager.disableCull()
        GL11.glPopMatrix()
    }

    override fun handleBlur(entity: EntityPlayer) {
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        RenderUtils.fastRoundedRect(-3F, -3F, 143F, 48F, 7F)
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }

    override fun handleShadowCut(entity: EntityPlayer) = handleBlur(entity)

    override fun handleShadow(entity: EntityPlayer) {
        RenderUtils.newDrawRect(-3F, -3F, 143F, 48F, shadowOpaque.rgb)
    }

    override fun getBorder(entity: EntityPlayer?): Border? {
        return Border(-3F, -3F, 143F, 48F)
    }
}