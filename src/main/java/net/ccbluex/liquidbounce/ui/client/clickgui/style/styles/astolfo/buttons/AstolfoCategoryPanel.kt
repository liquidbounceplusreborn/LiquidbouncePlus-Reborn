package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.astolfo.buttons

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.astolfo.AstolfoConstants.BACKGROUND_CATEGORY
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.astolfo.AstolfoConstants.FONT
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.astolfo.AstolfoConstants.PANEL_HEIGHT
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.astolfo.AstolfoConstants.PANEL_WIDTH
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.astolfo.drawHeightCenteredString
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.astolfo.geom.Rectangle
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawBorderedRect
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRect
import java.awt.Color

class AstolfoCategoryPanel(x: Float, y: Float, var category: ModuleCategory, var color: Color) : AstolfoButton(x, y, PANEL_WIDTH, PANEL_HEIGHT) {
  var open = false
  var moduleButtons = ArrayList<AstolfoModuleButton>()
  private var dragged = false
  private var mouseX2 = 0
  private var mouseY2 = 0

  init {
    val startY = y + height
    for ((count, mod) in LiquidBounce.moduleManager.modules.filter { it.category.displayName.equals(this.category.displayName, true)}.withIndex()) {
      ClientUtils.getLogger().info("Added ${mod.name} to ${this.category.displayName}")
      moduleButtons.add(AstolfoModuleButton(x, startY + height * count, width, height, mod, color))
    }
  }

  override fun drawPanel(mouseX: Int, mouseY: Int): Rectangle {
    if (dragged) {
      x = (mouseX2 + mouseX).toFloat()
      y = (mouseY2 + mouseY).toFloat()
    }
    drawRect(x, y, x + width, y + height, BACKGROUND_CATEGORY)
    FONT.drawHeightCenteredString(category.displayName.lowercase(), x + 4, y + height / 2, -0x1)

    var used = 0f
    if (open) {
      val startY = y + height
      for (moduleButton in moduleButtons) {
        moduleButton.x = x
        moduleButton.y = startY + used
        val box = moduleButton.drawPanel(mouseX, mouseY)
        used += box.height
      }
    } //		drawRect(x, y + count + height, x + width, y + used + height, -0xe7e5e9)
    //		drawBorder(x, y, x + width, y + used + height + 2, 2f, color.rgb)
    drawBorderedRect(x, y, x + width, y + height + used, 2f, color.rgb, Color(0, 0, 0, 0).rgb)

    return Rectangle() // unused since panel is the biggest unit there is
  }

  override fun mouseAction(mouseX: Int, mouseY: Int, click: Boolean, button: Int) {
    if (isHovered(mouseX, mouseY)) {
      if (click) {
        if (button == 0) {
          dragged = true
          mouseX2 = (x - mouseX).toInt()
          mouseY2 = (y - mouseY).toInt()
        } else {
          open = !open
        }
      }
    }
    if (!click) dragged = false
  }
}
