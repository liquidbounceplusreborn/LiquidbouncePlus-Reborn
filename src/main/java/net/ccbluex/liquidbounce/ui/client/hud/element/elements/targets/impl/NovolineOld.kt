package net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.impl

import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Target
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.TargetStyle
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.MathHelper
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.pow

class NovolineOld(inst: Target): TargetStyle("NovolineOld", inst, false) {

    override fun drawTarget(entity: EntityPlayer) {
        easingHealth += ((entity.health - easingHealth) / 2.0F.pow(10.0F - targetInstance.animProgress)) * RenderUtils.deltaTime

        val nameLength = (35F + Fonts.minecraftFont.getStringWidth(entity.name)
            .toFloat()).coerceAtLeast(35F + getArmorLength(entity))
        val barWidth = (easingHealth / entity.maxHealth) * nameLength

        RenderUtils.drawRect(0F, 0F, nameLength, 40F, Color(0,0,0,150).rgb)

        GlStateManager.resetColor()
        GL11.glColor4f(1F, 1F, 1F, 1F)
        RenderUtils.drawEntityOnScreen(15, 35, 15, entity)

        Fonts.minecraftFont.drawStringWithShadow(entity.name, 30F, 5F, -1);
        drawArmor(30, 15, entity)

        RenderUtils.drawRect(0F, 39F, barWidth, 40F,  targetInstance.barColor.rgb)
    }

    override fun getBorder(entity: EntityPlayer?): Border? {
        return Border(0F, 0F, 80F, 40F)
    }

    private fun drawArmor(x: Int, y: Int, ent: EntityPlayer) {
        GL11.glPushMatrix()
        RenderHelper.enableGUIStandardItemLighting()

        val renderItem = MinecraftInstance.mc.renderItem

        var drawX: Int = x
        var drawY: Int = y

        for (index in 3 downTo 0) {
            val stack = ent.inventory.armorInventory[index] ?: continue

            renderItem.renderItemIntoGUI(stack, drawX, drawY)
            renderItem.renderItemOverlays(MinecraftInstance.mc.fontRendererObj, stack, drawX, drawY)

            drawX += 18
        }

        if (ent.getHeldItem() != null && ent.getHeldItem().getItem() != null) {
            renderItem.renderItemIntoGUI(ent.getHeldItem(), drawX, drawY)
            renderItem.renderItemOverlays(MinecraftInstance.mc.fontRendererObj, ent.getHeldItem(), drawX, drawY)
        }

        RenderHelper.disableStandardItemLighting()
        GlStateManager.enableAlpha()
        GlStateManager.disableBlend()
        GlStateManager.disableLighting()
        GlStateManager.disableCull()
        GL11.glPopMatrix()
    }

    private fun getArmorLength(ent: EntityPlayer): Float {
        var x : Float = 0F
        for (i in 3 downTo 0) {
            val stack = ent.inventory.armorInventory[i] ?: continue
            x += 18F
        }
        if (ent.getHeldItem() != null && ent.getHeldItem().getItem() != null)
            x += 18F

        return x
    }
}
