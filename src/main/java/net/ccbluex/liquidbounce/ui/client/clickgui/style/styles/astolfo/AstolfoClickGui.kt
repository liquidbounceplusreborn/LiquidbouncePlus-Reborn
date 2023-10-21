package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.astolfo

import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.modules.client.ClickGUI
import net.ccbluex.liquidbounce.features.module.modules.client.ClickGUI.generateColor
import net.ccbluex.liquidbounce.file.FileManager
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.astolfo.buttons.AstolfoCategoryPanel
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.minecraft.client.gui.GuiScreen
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.io.PrintWriter
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

    private fun init() {
    var xPos = 4f
    for (cat in ModuleCategory.values()) {
      panels.add(AstolfoCategoryPanel(xPos, 4f, cat,generateColor()))
      xPos += AstolfoConstants.PANEL_WIDTH.toInt() + 10
    }
        loadConfig()
    }

    private fun loadConfig() {
        val jsonElement = JsonParser().parse(FileReader(LiquidBounce.fileManager.clickGuiConfig.file))
        if (jsonElement is JsonNull) return

        val jsonObject = jsonElement as JsonObject
        for (panel in panels) {
            if (!jsonObject.has(panel.category.displayName)) continue
            try {
                val panelObject = jsonObject.getAsJsonObject(panel.name)
                panel.open = panelObject["open"].asBoolean
                panel.x = panelObject["posX"].asFloat
                panel.y = panelObject["posY"].asFloat
                for (moduleElement in panel.moduleButtons) {
                    if (!panelObject.has(moduleElement.module.name)) continue
                    try {
                        val elementObject = panelObject.getAsJsonObject(moduleElement.module.name)
                        moduleElement.open = elementObject["Settings"].asBoolean
                    } catch (e: Exception) {
                        ClientUtils.getLogger().error(
                            "Error while loading clickgui module element with the name '" + moduleElement.module.name + "' (Panel Name: " + panel.name + ").",
                            e
                        )
                    }
                }
            } catch (e: Exception) {
                ClientUtils.getLogger()
                    .error("Error while loading clickgui panel with the name '" + panel.name + "'.", e)
            }
        }
    }

    private fun saveConfig() {
        val jsonObject = JsonObject()

        for (panel in panels) {
            val panelObject = JsonObject()
            panelObject.addProperty("open", panel.open)
            panelObject.addProperty("visible", true)
            panelObject.addProperty("posX", panel.x)
            panelObject.addProperty("posY", panel.y)
            for (moduleElement in panel.moduleButtons) {
                val elementObject = JsonObject()
                elementObject.addProperty("Settings", moduleElement.open)
                panelObject.add(moduleElement.module.name, elementObject)
            }
            jsonObject.add(panel.name, panelObject)
        }

        val file = LiquidBounce.fileManager.clickGuiConfig.file
        val printWriter = PrintWriter(FileWriter(file))
        printWriter.println(FileManager.PRETTY_GSON.toJson(jsonObject))
        printWriter.close()
    }

    override fun onGuiClosed() {
        saveConfig()
  }

  override fun drawScreen(mouseXIn: Int, mouseYIn: Int, partialTicks: Float) {
      if (panels.isEmpty())
          init()
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
