package net.ccbluex.liquidbounce.ui.client.clickgui.newVer.element

import net.ccbluex.liquidbounce.ui.client.clickgui.newVer.element.module.ModuleElement
import net.ccbluex.liquidbounce.ui.client.clickgui.newVer.ColorManager
import net.ccbluex.liquidbounce.ui.client.clickgui.newVer.IconManager
import net.ccbluex.liquidbounce.ui.client.clickgui.newVer.NewUi
import net.ccbluex.liquidbounce.ui.client.clickgui.newVer.extensions.animSmooth
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.MouseUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.Stencil
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.abs

class SearchElement(var xPos: Float, var yPos: Float, var width: Float, val height: Float) {

    private var scrollHeight = 0F
    private var animScrollHeight = 0F
    private var lastHeight = 0F
    private val startYY = 5f

    val searchBox = SearchBox(0, xPos.toInt() + 2, yPos.toInt() + 2, width.toInt() - 4, height.toInt() - 2)

    fun drawBox(mouseX: Int, mouseY: Int, accentColor: Color): Boolean {
        RenderUtils.originalRoundedRect(xPos - 0.5F, yPos - 0.5F, xPos + width + 0.5F, yPos + height + 0.5F, 4F, ColorManager.buttonOutline.rgb)
        Stencil.write(true)
        RenderUtils.originalRoundedRect(xPos, yPos, xPos + width, yPos + height, 4F, ColorManager.textBox.rgb)
        Stencil.erase(true)
        if (searchBox.isFocused) {
            RenderUtils.newDrawRect(xPos, yPos + height - 1F, xPos + width, yPos + height, accentColor.rgb)
            searchBox.drawTextBox()
        } else if (searchBox.text.isEmpty()) {
            searchBox.text = "Search"
            searchBox.drawTextBox()
            searchBox.text = ""
        } else
            searchBox.drawTextBox()

        Stencil.dispose()
        GlStateManager.disableAlpha()
        RenderUtils.drawImage2(IconManager.search, xPos + width - 15F, yPos + 5F, 10, 10)
        GlStateManager.enableAlpha()
        return searchBox.text.isNotEmpty()
    }

    private fun searchMatch(module: ModuleElement): Boolean {
        return module.module.name.contains(searchBox.text, true)
    }

    private fun getSearchModules(ces: List<CategoryElement>): List<ModuleElement> {
        val modules = mutableListOf<ModuleElement>()
        ces.forEach { cat ->
            modules.addAll(cat.moduleElements.filter { searchMatch(it) })
        }
        return modules
    }

    fun drawPanel(mX: Int, mY: Int, x: Float, y: Float, w: Float, h: Float, wheel: Int, ces: List<CategoryElement>, accentColor: Color) {
        var mouseX = mX
        var mouseY = mY
        lastHeight = 0F

        getSearchModules(ces).forEach { mod ->
            if (searchMatch(mod)) {
                lastHeight += mod.animHeight + 40F
            }
        }

        if (lastHeight >= 10F) lastHeight -= 10F
        handleScrolling(wheel, h)
        drawScroll(x, y + startYY, w, h)


//        Fonts.fontLarge.drawString("Search", x + 10F, y + 10F, -1)
        Fonts.fontSmall.drawString("Search", NewUi.getInstance().windowXStart + 20f, y - 12F, -1)
        RenderUtils.drawImage2(IconManager.back, NewUi.getInstance().windowXStart + 4f, y - 15F, 10, 10)

        var startY = y + startYY
        if (mouseY < y + startYY || mouseY >= y + h)
            mouseY = -1
        RenderUtils.makeScissorBox(x, y + startYY, x + w, y + h)
        GL11.glEnable(GL11.GL_SCISSOR_TEST)
        ces.forEach { cat ->
            cat.moduleElements.forEach { mod ->
                if (searchMatch(mod)) {
                    startY += if (startY + animScrollHeight > y + h || startY + animScrollHeight + 40F + mod.animHeight < y + startYY)
                        40F + mod.animHeight
                    else
                        mod.drawElement(mouseX, mouseY, x, startY + animScrollHeight, w, 40F, accentColor)
                }
            }
        }

        GL11.glDisable(GL11.GL_SCISSOR_TEST)
    }

    private fun handleScrolling(wheel: Int, height: Float) {
        if (wheel != 0) {
            if (wheel > 0)
                scrollHeight += 50F
            else
                scrollHeight -= 50F
        }
        if (lastHeight > height - (startYY + 10f))
            scrollHeight = scrollHeight.coerceIn(-lastHeight + height - (startYY + 10f), 0F)
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
                y + 5F + multiply,
                x + width - 4F,
                y + 5F + (height - (startYY + 10F)) * ((height - (startYY + 10F)) / lastHeight) + multiply,
                1F, 0x50FFFFFF)
        }
    }

    fun handleMouseClick(mX: Int, mY: Int, mouseButton: Int, x: Float, y: Float, w: Float, h: Float, ces: List<CategoryElement>) {
        if (MouseUtils.mouseWithinBounds(mX, mY, x - 200F, y - 20F, x - 170F, y)) {
//            searchBox.text = ""
            return
        }
        var mouseY = mY
        searchBox.mouseClicked(mX, mouseY, mouseButton)
        if (searchBox.text.isEmpty()) return
        if (mouseY < y + startYY || mouseY >= y + h)
            mouseY = -1
        var startY = y + startYY

        getSearchModules(ces).forEach { mod ->
            mod.handleClick(mX, mouseY, x, startY + animScrollHeight, w, 40F)
            startY += 40F + mod.animHeight
        }
    }

    fun handleMouseRelease(mX: Int, mY: Int, mouseButton: Int, x: Float, y: Float, w: Float, h: Float, ces: List<CategoryElement>) {
        var mouseX = mX
        var mouseY = mY
        if (searchBox.text.isEmpty()) return
        if (mouseY < y + startYY || mouseY >= y + h)
            mouseY = -1
        var startY = y + startYY
        getSearchModules(ces).forEach { mod ->
            mod.handleRelease(mouseX, mouseY, x, startY + animScrollHeight, w, 40F)
            startY += 40F + mod.animHeight
        }
    }

    fun handleTyping(typedChar: Char, keyCode: Int, x: Float, y: Float, w: Float, h: Float, ces: List<CategoryElement>): Boolean {
        searchBox.textboxKeyTyped(typedChar, keyCode)
        if (searchBox.text.isEmpty()) return false
        getSearchModules(ces).forEach { mod ->
            if (mod.handleKeyTyped(typedChar, keyCode))
                return true
        }
        return false
    }

    fun isTyping(): Boolean = searchBox.text.isNotEmpty()

}