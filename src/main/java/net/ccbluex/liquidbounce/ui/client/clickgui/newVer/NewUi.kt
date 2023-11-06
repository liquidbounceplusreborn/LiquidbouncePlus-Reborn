package net.ccbluex.liquidbounce.ui.client.clickgui.newVer

import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.modules.client.NewGUI
import net.ccbluex.liquidbounce.ui.client.clickgui.newVer.element.CategoryElement
import net.ccbluex.liquidbounce.ui.client.clickgui.newVer.element.SearchElement
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.AnimationUtils
import net.ccbluex.liquidbounce.utils.MouseUtils.mouseWithinBounds
import net.ccbluex.liquidbounce.utils.Stencil
import net.ccbluex.liquidbounce.utils.extensions.setAlpha
import net.ccbluex.liquidbounce.utils.geom.Rectangle
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.util.MathHelper
import net.minecraft.util.ResourceLocation
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.io.IOException
import java.util.function.Consumer

/**
 * @author inf (original java code)
 * @author pie (refactored)
 */
class NewUi private constructor() : GuiScreen() {
    private val categoryElements: MutableList<CategoryElement> = ArrayList()
    private var startYAnim = height / 2f
    private var endYAnim = height / 2f
    private var searchElement: SearchElement? = null
    private var fading = 0f

    private val backgroundColor = Color(16, 16, 16, 255)
    private val backgroundColor2 = Color(40, 40, 40, 255)
    // 30
    private var marginLeft
        get() = NewGUI.left.get()
        set(value) = NewGUI.left.set(value)
    private var marginRight
        get() = NewGUI.right.get()
        set(value) = NewGUI.right.set(value)
    private var marginTop
        get() = NewGUI.top.get()
        set(value) = NewGUI.top.set(value)
    private var marginBotton
        get() = NewGUI.bottom.get()
        set(value) = NewGUI.bottom.set(value)

    private val categoryXOffset
        get() = NewGUI.sideWidth.get()

    private val searchXOffset = 10f
    private val searchYOffset = 85f

    private val searchWidth
        get() = NewGUI.sideWidth.get() - 10f
    private val searchHeight = 20f

    private val elementHeight = 24f

    private val elementsStartY = 110

    private val categoriesTopMargin = 20f
    private val categoriesBottommargin = 20f

    private val xButtonColor = Color(0.2f, 0f, 0f, 1f)

    val window
        get() = Rectangle(marginLeft, marginTop, this.width - (marginLeft + marginRight), this.height - (marginTop + marginBotton))

    private var dragging = false
    private val moveAera
        get() = Rectangle(window.x, window.y, window.width - 20f, 20f)
    private var x2 = 0f
    private var y2 = 0f


    init {
        ModuleCategory.values().forEach { categoryElements.add(CategoryElement(it)) }
        searchElement = SearchElement(window.x + searchXOffset, window.y + searchYOffset, searchWidth, searchHeight)
        categoryElements[0].focused = true
    }

    override fun initGui() {
        Keyboard.enableRepeatEvents(true)
        categoryElements.forEach { cat ->
            cat.moduleElements.filter { it.listeningKeybind() }.forEach { mod ->
                mod.resetState()
            }
        }

        super.initGui()
    }

    override fun onGuiClosed() {
        categoryElements.filter { it.focused }.map { it.handleMouseRelease(-1, -1, 0, 0f, 0f, 0f, 0f) }
        dragging = false
        Keyboard.enableRepeatEvents(false)
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (dragging) {
            val ml = marginLeft
            val mr = marginRight
            val mt = marginTop
            val mb = marginBotton

            marginLeft = x2 + mouseX
            marginTop = y2 + mouseY

            marginRight -= marginLeft - ml
            marginBotton -= marginTop - mt
        }
        
        val resizeAmount = 1.5f * if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) -1 else 1
        if (Keyboard.isKeyDown(Keyboard.KEY_LEFT))
            marginLeft -= resizeAmount
        if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT))
            marginRight -= resizeAmount
        if (Keyboard.isKeyDown(Keyboard.KEY_UP))
            marginTop -= resizeAmount
        if (Keyboard.isKeyDown(Keyboard.KEY_DOWN))
            marginBotton -= resizeAmount

        Fonts.fontSmall.drawString("Use the arrow keys to expand and hold shift to shrink", 15f, 15f, -1)

        drawFullSized(mouseX, mouseY, partialTicks, NewGUI.accentColor)
    }

    private fun drawFullSized(mouseX: Int, mouseY: Int, partialTicks: Float, accentColor: Color) {
        RenderUtils.originalRoundedRect(window.x, window.y, window.x + window.width, window.y + window.height, 5f, backgroundColor.rgb)
        RenderUtils.customRounded(window.x, window.y, window.x + window.width, window.y + 20f, 5f, 5f, 0f, 0f, backgroundColor2.rgb)


        // something to make it look more like windoze - inf, 2022
        if (window.contains(mouseX, mouseY))
            fading += 0.2f * RenderUtils.deltaTime * 0.045f
        else
            fading -= 0.2f * RenderUtils.deltaTime * 0.045f
        fading = MathHelper.clamp_float(fading, 0f, 1f)
        xButtonColor.setAlpha(fading)
        RenderUtils.customRounded(window.x2 - 20f, window.y, window.x2, window.y + 20f, 0f, 5f, 0f, 0f, xButtonColor.rgb)
        GlStateManager.disableAlpha()
        RenderUtils.drawImage(IconManager.removeIcon, window.x2 - 17.0, window.y + 5.0, 10.0, 10.0)
        GlStateManager.enableAlpha()

        val skinAndNameSpace = NewGUI.sideWidth.get() - 15
        val font = Fonts.fontLarge

        if (skinAndNameSpace > 50) {
            // skin mask
            Stencil.write(true)
            RenderUtils.drawRoundedRect(window.x + 10f, window.y + 25f, window.x + 10f + 50f, window.y + 25f + 50f, 4f, -13816531)
            Stencil.erase(true)

            if (mc.netHandler.getPlayerInfo(mc.thePlayer.uniqueID) != null) {
                val skin = mc.netHandler.getPlayerInfo(mc.thePlayer.uniqueID).locationSkin
                GL11.glPushMatrix()
                GL11.glTranslatef(window.x + 10f, window.y + 25f, 0f)
                GL11.glDisable(GL11.GL_DEPTH_TEST)
                GL11.glEnable(GL11.GL_BLEND)
                GL11.glDepthMask(false)
                OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO)
                GL11.glColor4f(1f, 1f, 1f, 1f)
                mc.textureManager.bindTexture(skin)
                drawScaledCustomSizeModalRect(
                    0, 0, 8f, 8f, 8, 8, 50, 50,
                    64f, 64f
                )
                GL11.glDepthMask(true)
                GL11.glDisable(GL11.GL_BLEND)
                GL11.glEnable(GL11.GL_DEPTH_TEST)
                GL11.glPopMatrix()
            }
            Stencil.dispose()
        }

        if (skinAndNameSpace > 110) {
            if (font.getStringWidth(mc.thePlayer.gameProfile.name) > 70)
                font.drawString(font.trimStringToWidth(mc.thePlayer.gameProfile.name, 50) + "...", window.x + 70, window.y + 48 - font.FONT_HEIGHT + 15, -1)
            else
                font.drawString(mc.thePlayer.gameProfile.name, window.x + 70, window.y + 48 - font.FONT_HEIGHT + 15, -1)
        }

        // reset search pos
        searchElement!!.xPos = window.x + searchXOffset
        searchElement!!.yPos = window.y + searchYOffset
        searchElement!!.width = searchWidth

        // taken from searchBox's constructor
        searchElement!!.searchBox.width = searchWidth.toInt() - 4
        searchElement!!.searchBox.xPosition = (window.x + searchXOffset + 2).toInt()
        searchElement!!.searchBox.yPosition = (window.y + searchYOffset + 2).toInt()

        if (searchElement!!.drawBox(mouseX, mouseY, accentColor)) {
            searchElement!!.drawPanel(mouseX, mouseY, window.x + categoryXOffset, window.y + categoriesTopMargin, window.width - categoryXOffset, window.height - categoriesBottommargin, Mouse.getDWheel(), categoryElements, accentColor)
            return
        }

        var startY = window.y + elementsStartY

        for (ce in categoryElements) {
            ce.drawLabel(mouseX, mouseY, window.x, startY, categoryXOffset, elementHeight)
            if (ce.focused) {
                startYAnim = if (NewGUI.fastRenderValue.get())
                    startY + 6f
                             else
                                 AnimationUtils.animate(startY + 6f,
                                    startYAnim,
                                    (if (startYAnim - (startY + 5f) > 0) 0.65f else 0.55f) * RenderUtils.deltaTime * 0.025f
                                )
                endYAnim =  if (NewGUI.fastRenderValue.get())
                                startY + elementHeight - 6f
                            else
                                AnimationUtils.animate(
                                    startY + elementHeight - 6f,
                                    endYAnim,
                                    (if (endYAnim - (startY + elementHeight - 5f) < 0) 0.65f else 0.55f) * RenderUtils.deltaTime * 0.025f
                                )
                ce.drawPanel(mouseX, mouseY, window.x + categoryXOffset, window.y + categoriesTopMargin, window.width - categoryXOffset, window.height - categoriesBottommargin, Mouse.getDWheel(), accentColor)
                Fonts.font40.drawString(ce.name, window.x + 7, window.y + 7, -1)
            }
            startY += elementHeight
        }
        val offset = 8f
        RenderUtils.originalRoundedRect(window.x + 2f + offset, startYAnim, window.x + 4f + offset, endYAnim, 1f, accentColor.rgb)
        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    @Throws(IOException::class)
    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        // search back button
        if (searchElement!!.isTyping() && Rectangle(window.x, window.y, 60f, 24f).contains(mouseX, mouseY)) {
            searchElement!!.searchBox.text = ""
            return
        }

        if (moveAera.contains(mouseX, mouseY) && !dragging) {
            dragging = true
            x2 = window.x - mouseX
            y2 = window.y - mouseY
            return
        }

        // close button
        if (Rectangle(window.x2 - 24, window.y, 24f, 24f).contains(mouseX, mouseY)) {
            mc.displayGuiScreen(null)
            return
        }


        var startY = window.y + elementsStartY

        searchElement!!.handleMouseClick(mouseX, mouseY, mouseButton, window.x + categoryXOffset, window.y + categoriesTopMargin, window.width - categoryXOffset, window.height - categoriesBottommargin, categoryElements)
        if (!searchElement!!.isTyping()) {
            categoryElements.forEach { cat ->
                if (cat.focused)
                    cat.handleMouseClick(mouseX, mouseY, mouseButton, window.x + categoryXOffset, window.y + categoriesTopMargin, window.width - categoryXOffset, window.height - categoriesBottommargin.toFloat())
                if (mouseWithinBounds(mouseX, mouseY, window.x, startY, window.x + categoryXOffset, startY + elementHeight) && !searchElement!!.isTyping()) {
                    categoryElements.forEach(Consumer { e: CategoryElement -> e.focused = false })
                    cat.focused = true
                    return
                }
                startY += elementHeight
            }
        }
    }

    @Throws(IOException::class)
    override fun keyTyped(typedChar: Char, keyCode: Int) {
        categoryElements.filter { it.focused }.forEach { cat ->
            if (cat.handleKeyTyped(typedChar, keyCode)) return
        }

        if (searchElement!!.handleTyping(typedChar, keyCode, window.x + categoryXOffset, window.y + categoriesTopMargin, window.width - categoryXOffset, window.height - categoriesBottommargin, categoryElements))
            return
        super.keyTyped(typedChar, keyCode)
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        if (dragging) {
            dragging = false
            return
        }

        searchElement!!.handleMouseRelease(mouseX, mouseY, state, window.x + categoryXOffset, window.y + categoriesTopMargin, window.width - categoryXOffset, window.height - categoriesBottommargin, categoryElements)
        if (!searchElement!!.isTyping()) {
            categoryElements.filter { it.focused }.forEach { cat ->
                cat.handleMouseRelease(mouseX, mouseY, state, window.x + categoryXOffset, window.y + categoriesTopMargin, window.width - categoryXOffset, window.height - categoriesBottommargin.toFloat())
            }
        }
        super.mouseReleased(mouseX, mouseY, state)
    }

    override fun doesGuiPauseGame(): Boolean {
        return false
    }

    companion object {
        private var instance: NewUi? = null
        fun getInstance(): NewUi {
            return if (instance == null) NewUi().also { instance = it } else instance!!
        }

        fun resetInstance() {
            instance = NewUi()
        }
    }
}
