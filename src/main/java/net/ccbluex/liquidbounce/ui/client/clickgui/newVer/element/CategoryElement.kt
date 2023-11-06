package net.ccbluex.liquidbounce.ui.client.clickgui.newVer.element

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.ui.client.clickgui.newVer.ColorManager
import net.ccbluex.liquidbounce.ui.client.clickgui.newVer.element.module.ModuleElement
import net.ccbluex.liquidbounce.ui.client.clickgui.newVer.extensions.animSmooth
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.ccbluex.liquidbounce.utils.MouseUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.abs

class CategoryElement(val category: ModuleCategory): MinecraftInstance() {
    val name = category.displayName
    var focused = false

    private var scrollHeight = 0F
    private var animScrollHeight = 0F
    private var lastHeight = 0F

    private val startYY = 10F

    val moduleElements = mutableListOf<ModuleElement>()

    init {
        LiquidBounce.moduleManager.modules.filter { it.category == category }.forEach { moduleElements.add(ModuleElement(it)) }
    }

    fun drawLabel(mouseX: Int, mouseY: Int, x: Float, y: Float, width: Float, height: Float) {
        if (focused)
            RenderUtils.originalRoundedRect(x + 11F, y + 3F, x + width - 3F, y + height - 3F, 3F, ColorManager.dropDown.rgb)
        else if (MouseUtils.mouseWithinBounds(mouseX, mouseY, x, y, x + width, y + height))
            RenderUtils.originalRoundedRect(x + 11F, y + 3F, x + width - 3F, y + height - 3F, 3F, ColorManager.border.rgb)
        Fonts.font40.drawString(name, x + 16F, y + height / 2F - Fonts.font40.FONT_HEIGHT / 2F + 2F, -1)
    }

    fun drawPanel(mX: Int, mY: Int, x: Float, y: Float, width: Float, height: Float, wheel: Int, accentColor: Color) {
        var mouseX = mX
        var mouseY = mY
        lastHeight = 0F
        for (me in moduleElements)
            lastHeight += 40F + me.animHeight
        if (lastHeight >= 10F)
            lastHeight -= 10F
        handleScrolling(wheel, height)
        drawScroll(x, y + 50F, width, height)
        if (mouseY < y + 50F || mouseY >= y + height)
            mouseY = -1
        RenderUtils.makeScissorBox(x, y + startYY, x + width, y + height)
        GL11.glEnable(3089)
        var startY = y + startYY
        for (moduleElement in moduleElements) {
            if (startY + animScrollHeight > y + height || startY + animScrollHeight + 40F + moduleElement.animHeight < y + startYY)
                startY += 40F + moduleElement.animHeight
            else
                startY += moduleElement.drawElement(mouseX, mouseY, x, startY + animScrollHeight, width, 40F, accentColor)
        }
        GL11.glDisable(3089)
    }

    private fun handleScrolling(wheel: Int, height: Float) {
        if (wheel != 0) {
            if (wheel > 0)
                scrollHeight += 50F
            else
                scrollHeight -= 50F
        }
        if (lastHeight > height - (startYY + 10F))
            scrollHeight = scrollHeight.coerceIn(-lastHeight + height - (startYY + 10F), 0F)
        else
            scrollHeight = 0F
        animScrollHeight = animScrollHeight.animSmooth(scrollHeight, 0.5F)
    }

    private fun drawScroll(x: Float, y: Float, width: Float, height: Float) {
        if (lastHeight > height - (startYY + 10F)) {
            val last = (height - (startYY + 10F)) - (height - (startYY + 10F)) * ((height - (startYY + 10F)) / lastHeight)
            val multiply = last * abs(animScrollHeight / (-lastHeight + height - (startYY + 10F))).coerceIn(0F, 1F)
            RenderUtils.originalRoundedRect(
                x + width - 6F,
                y + 5F + multiply - 40f,
                x + width - 4F,
                y + 5F + (height - (startYY + 10F)) * ((height - (startYY + 10F)) / lastHeight) + multiply - 40f,
                1F, 0x50FFFFFF)
        }
    }

    fun handleMouseClick(mX: Int, mY: Int, mouseButton: Int, x: Float, y: Float, width: Float, height: Float) {
        var mouseX = mX
        var mouseY = mY
        if (mouseY < y + startYY || mouseY >= y + height)
            mouseY = -1
        var startY = y + startYY
        if (mouseButton == 0)
            for (moduleElement in moduleElements) {
                moduleElement.handleClick(mouseX, mouseY, x, startY + animScrollHeight, width, 40F)
                startY += 40F + moduleElement.animHeight
            }
    }

    fun handleMouseRelease(mX: Int, mY: Int, mouseButton: Int, x: Float, y: Float, width: Float, height: Float) {
        var mouseX = mX
        var mouseY = mY
        if (mouseY < y + startYY || mouseY >= y + height)
            mouseY = -1
        var startY = y + startYY
        if (mouseButton == 0)
            for (moduleElement in moduleElements) {
                moduleElement.handleRelease(mouseX, mouseY, x, startY + animScrollHeight, width, 40F)
                startY += 40F + moduleElement.animHeight
            }
    }

    fun handleKeyTyped(keyTyped: Char, keyCode: Int): Boolean {
        for (moduleElement in moduleElements)
            if (moduleElement.handleKeyTyped(keyTyped, keyCode))
                return true
        return false
    }
}