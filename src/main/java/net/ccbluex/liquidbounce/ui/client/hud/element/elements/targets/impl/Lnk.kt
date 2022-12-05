package net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.impl

import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Target
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.TargetStyle
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.Colors
import net.ccbluex.liquidbounce.utils.render.GLUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.entity.player.EntityPlayer
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.pow

class Lnk(inst: Target): TargetStyle("Lnk", inst, true){
    override fun drawTarget(entity: EntityPlayer) {
        updateAnim(entity.health)
        val width = (38 + Fonts.minecraftFont.getStringWidth(entity.name))
            .coerceAtLeast(118)
            .toFloat()
        val fractions = floatArrayOf(0.0f, 0.5f, 1.0f)
        val colors = arrayOf(Color.RED, Color.YELLOW, Color(10, 255, 40))
        val progress = easingHealth / entity.maxHealth
        val customColor = if (easingHealth >= entity.maxHealth) Color.GREEN else Colors.blendColors(
            fractions,
            colors,
            progress
        ).brighter()
        RenderUtils.drawRect(0F, 0F, width + 5F, 45F, Color(35, 35, 35, 190))
        GL11.glPushMatrix()
        GL11.glColor4f(1f, 1f, 1f, 1f)
        RenderUtils.drawEntityOnScreen(10.0, 33.0, 16f, entity)
        GL11.glPopMatrix()
        mc.fontRendererObj.drawStringWithShadow(entity.name, 23f, 4f, Color.white.rgb)
        RenderUtils.drawRect(3f, 37f, width + 2.5f, 42f, Color(30, 30, 30, 120))
        RenderUtils.drawRect(
            3f,
            37f,
            easingHealth / entity.maxHealth * (width + 2.5f),
            42f,
            Color(customColor.red, customColor.green, customColor.blue, 160)
        )
        easingHealth += ((entity.health - easingHealth) / 2.0F.pow(10.0F - targetInstance.fadeSpeed.get())) * RenderUtils.deltaTime
        var x = 22
        var y = 14
        for (index in 3 downTo 0) {
            RenderUtils.drawRect(
                x.toFloat(),
                y.toFloat(),
                x.toFloat() + 18f,
                y + 15f,
                Color(30, 30, 30, 120).rgb
            )
            if (entity.inventory.armorInventory[index] != null) {
                GlStateManager.pushMatrix()
                GlStateManager.scale(0.65, 0.65, 0.65)
                mc.fontRendererObj.drawStringWithShadow(
                    ((entity.inventory.armorInventory[index].maxDamage - entity.inventory.armorInventory[index].itemDamage)).toString(),
                    (x.toFloat() + 4f) * 1 / 0.65f,
                    47f,
                    Color.white.rgb
                )
                GlStateManager.scale(1 / 0.65, 1 / 0.65, 1 / 0.65)
                GlStateManager.popMatrix()
                GL11.glPushMatrix()
                GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
                if (mc.theWorld != null) {
                    GLUtils.enableGUIStandardItemLighting()
                }
                GlStateManager.pushMatrix()
                GlStateManager.disableAlpha()
                GlStateManager.clear(256)
                mc.renderItem.renderItemIntoGUI(entity.inventory.armorInventory[index], x + 1, y - 1)
                mc.renderItem.zLevel = 0.0f
                GlStateManager.disableBlend()
                GlStateManager.scale(0.5, 0.5, 0.5)
                GlStateManager.disableDepth()
                GlStateManager.disableLighting()
                GlStateManager.enableDepth()
                GlStateManager.scale(2.0f, 2.0f, 25.0f)
                GlStateManager.enableAlpha()
                GlStateManager.popMatrix()
                GL11.glPopMatrix()
            }
            x += 20
        }
        RenderUtils.drawRect(x.toFloat(), y.toFloat(), x.toFloat() + 18f, y + 15f, Color(30, 30, 30, 120).rgb)
        if (entity.inventory.mainInventory[entity.inventory.currentItem] != null) {
            if (entity.inventory.mainInventory[entity.inventory.currentItem].isItemStackDamageable) {
                GlStateManager.pushMatrix()
                GlStateManager.scale(0.65, 0.65, 0.65)
                mc.fontRendererObj.drawStringWithShadow(
                    ((entity.inventory.mainInventory[entity.inventory.currentItem].maxDamage - entity.inventory.mainInventory[entity.inventory.currentItem].itemDamage)).toString(),
                    (x.toFloat() + 4f) * 1 / 0.65f,
                    47f,
                    Color.white.rgb
                )
                GlStateManager.scale(1 / 0.65, 1 / 0.65, 1 / 0.65)
                GlStateManager.popMatrix()
            }
            GL11.glPushMatrix()
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
            if (mc.theWorld != null) {
                RenderHelper.enableGUIStandardItemLighting()
            }
            GlStateManager.pushMatrix()
            GlStateManager.disableAlpha()
            GlStateManager.clear(256)
            mc.renderItem.renderItemIntoGUI(
                entity.inventory.mainInventory[entity.inventory.currentItem],
                x + 1,
                y - 1
            )
            mc.renderItem.zLevel = 0.0f
            GlStateManager.disableBlend()
            GlStateManager.scale(0.5, 0.5, 0.5)
            GlStateManager.disableDepth()
            GlStateManager.disableLighting()
            GlStateManager.enableDepth()
            GlStateManager.scale(2.0f, 2.0f, 2.0f)
            GlStateManager.enableAlpha()
            GlStateManager.popMatrix()
            GL11.glPopMatrix()
        }
    }

    override fun handleBlur(entity: EntityPlayer) {
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        RenderUtils.fastRoundedRect(0F, 0F, 124F, 44F, 7F)
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }

    override fun handleShadowCut(entity: EntityPlayer) = handleBlur(entity)

    override fun handleShadow(entity: EntityPlayer) {

        RenderUtils.originalRoundedRect(0F, 0F, 124F, 44F, 8F, shadowOpaque.rgb)
    }

    override fun getBorder(entity: EntityPlayer?): Border? {
        return Border(0F, 0F, 124F, 44F)
    }
}