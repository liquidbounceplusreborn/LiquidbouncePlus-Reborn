package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.astolfo

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.modules.render.ClickGUI
import net.ccbluex.liquidbounce.features.module.modules.render.ClickGUI.generateColor
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.astolfo.buttons.AstolfoCategoryPanel
import net.minecraft.client.gui.GuiScreen
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.io.IOException
import kotlin.math.roundToInt

/**
 * Astolfo Click GUI
 *
 * Designed in a way that only this class know about scale and scrolling.
 * @author pii4
 * @property panels  the categories
 * @property scale scale factor
 * @property scroll scroll amount
 */
class AstolfoClickGui : GuiScreen() {
  private var panels = ArrayList<AstolfoCategoryPanel>()
  private val scale: Float
    get() = LiquidBounce.moduleManager.getModule(ClickGUI::class.java)?.scale?.get()!!
  private val scroll: Float
    get() = LiquidBounce.moduleManager.getModule(ClickGUI::class.java)?.scroll?.get()!!

  private var pressed = mutableMapOf("UP" to false, "DOWN" to false, "LEFT" to false, "RIGHT" to false)

  private fun updatePressed() {
    pressed["UP"] = Keyboard.isKeyDown(Keyboard.KEY_UP)
    pressed["DOWN"] = Keyboard.isKeyDown(Keyboard.KEY_DOWN)
    pressed["LEFT"] = Keyboard.isKeyDown(Keyboard.KEY_LEFT)
    pressed["RIGHT"] = Keyboard.isKeyDown(Keyboard.KEY_RIGHT)
  }

  init {
    var xPos = 4f
    for (cat in ModuleCategory.values()) {
      panels.add(AstolfoCategoryPanel(xPos, 4f, cat,generateColor()))
      xPos += AstolfoConstants.PANEL_WIDTH.toInt() + 10
    }
  }

  override fun drawScreen(mouseXIn: Int, mouseYIn: Int, partialTicks: Float) {
    val mouseX = (mouseXIn / scale).roundToInt()
    val mouseY = (mouseYIn / scale).roundToInt()

    GL11.glPushMatrix()
    GL11.glScalef(scale, scale, scale)

    drawRect(0, 0, mc.currentScreen.width, mc.currentScreen.height, Color(0, 0, 0, 50).rgb)

    // vertical scrolling
    if (Mouse.hasWheel() && Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
      val wheel = Mouse.getDWheel()
      if (wheel != 0) {
        val scrollAmount = scroll * if (wheel > 0) 1 else -1
        panels.map { it.y += scrollAmount }
      }
    }
    if (Keyboard.isKeyDown(Keyboard.KEY_UP) && !pressed["UP"]!!) panels.map { it.y -= scroll }
    if (Keyboard.isKeyDown(Keyboard.KEY_DOWN) && !pressed["DOWN"]!!) panels.map { it.y += scroll }
    if (Keyboard.isKeyDown(Keyboard.KEY_LEFT) && !pressed["LEFT"]!!) panels.map { it.x -= scroll }
    if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT) && !pressed["RIGHT"]!!) panels.map { it.x += scroll }

    if (Keyboard.isKeyDown(Keyboard.KEY_F10)) panels.map { it.y = 10f }

    for (catPanel in panels) {
      //ClientUtils.getLogger().info("${catPanel.category.displayName}: ${catPanel.moduleButtons.size}")
      catPanel.drawPanel(mouseX, mouseY)
    }

    GL11.glPopMatrix()
    updatePressed()
  }

  private fun mouseAction(mouseXIn: Int, mouseYIn: Int, mouseButton: Int, state: Boolean) {
    val mouseX = (mouseXIn / scale).roundToInt()
    val mouseY = (mouseYIn / scale).roundToInt()

    for (panel in panels) {
      panel.mouseAction(mouseX, mouseY, state, mouseButton)
      if (panel.open) {
        for (moduleButton in panel.moduleButtons) {
          moduleButton.mouseAction(mouseX, mouseY, state, mouseButton)
          if (moduleButton.open) {
            for (pan in moduleButton.valueButtons) {
              pan.mouseAction(mouseX, mouseY, state, mouseButton)
            }
          }
        }
      }
    }
  }

  @Throws(IOException::class)
  override fun mouseClicked(mouseXIn: Int, mouseYIn: Int, mouseButton: Int) {
    mouseAction(mouseXIn, mouseYIn, mouseButton, true)
  }

  override fun mouseReleased(mouseXIn: Int, mouseYIn: Int, mouseButton: Int) {
    mouseAction(mouseXIn, mouseYIn, mouseButton, false)
  }
}
