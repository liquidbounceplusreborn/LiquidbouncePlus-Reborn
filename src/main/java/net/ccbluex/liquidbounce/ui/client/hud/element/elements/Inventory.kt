package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.Palette
import net.ccbluex.liquidbounce.utils.render.GLUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.*
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11
import java.awt.Color

/**
 * CustomHUD Armor element
 *
 * Shows a horizontal display of current armor
 */
@ElementInfo(name = "Inventory")
class Inventory(x: Double = 10.0, y: Double = 10.0, scale: Float = 1F) : Element(x, y, scale) {
    private val brightnessValue = FloatValue("Brightness", 1f, 0f, 1f)
    private val redValue = IntegerValue("Text-R", 255, 0, 255)
    private val greenValue = IntegerValue("Text-G", 255, 0, 255)
    private val blueValue = IntegerValue("Text-B", 255, 0, 255)
    private val colorModeValue = ListValue("Text-Color", arrayOf("Custom", "Fade"), "Custom")
    private val distanceValue = IntegerValue("Line-Distance", 0, 0, 400)
    private val gradientAmountValue = IntegerValue("Gradient-Amount", 25, 1, 50)

    /**
     * Draw element
     */
    override fun drawElement(): Border? {
        val colorMode = colorModeValue.get()
        val color = Color(redValue.get(), greenValue.get(), blueValue.get()).rgb
        RenderUtils.drawRect(8f, 28f, 8f + 163f, 30f + 65f, Color(20, 19, 18, 170).rgb)
        val barLength1 = (163f).toDouble()
        for (i in 0..(gradientAmountValue.get()-1)) {
            val barStart = i.toDouble() / gradientAmountValue.get().toDouble() * barLength1
            val barEnd = (i + 1).toDouble() / gradientAmountValue.get().toDouble() * barLength1
            RenderUtils.drawGradientSideways(8+ barStart, 28.0, 8 + barEnd, 29.0,
                when {
                    colorMode.equals("Fade", ignoreCase = true) -> Palette.fade2(Color(redValue.get(), greenValue.get(), blueValue.get()), i * distanceValue.get(), 1000).rgb
                    else -> color
                },
                when {
                    colorMode.equals("Fade", ignoreCase = true) -> Palette.fade2(Color(redValue.get(), greenValue.get(), blueValue.get()), i * distanceValue.get(), 1000).rgb
                    else -> color
                })
        }
        Fonts.fontSFUI35.drawString("Inventory List", 10F, 31F, Color(0xFFFFFF).rgb)
        var itemX: Int = 10
        var itemY: Int = 42
        var airs = 0
        for (i in mc.thePlayer.inventory.mainInventory.indices) {
            if (i < 9) continue
            val stack = mc.thePlayer.inventory.mainInventory[i]
            if (stack == null) {
                airs++
            }
            val res = ScaledResolution(mc)
            GL11.glPushMatrix()
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
            if (mc.theWorld != null) GLUtils.enableGUIStandardItemLighting()
            GlStateManager.pushMatrix()
            GlStateManager.disableAlpha()
            GlStateManager.clear(256)
            mc.renderItem.zLevel = -150.0f
            mc.renderItem.renderItemAndEffectIntoGUI(stack, itemX, itemY)
            mc.renderItem.renderItemOverlays(Fonts.font40, stack, itemX, itemY)
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
            if (itemX < 152) {
                itemX += 18
            } else {
                itemX = 10
                itemY += 18
            }
        }

        if (airs == 27) {
            Fonts.fontSFUI40.drawString("Your inventory is empty...", 28, 56, Color(255, 255, 255).rgb)
        }
        return Border(8f, 28f, 8f + 163f, 30f + 65f)
    }

}
