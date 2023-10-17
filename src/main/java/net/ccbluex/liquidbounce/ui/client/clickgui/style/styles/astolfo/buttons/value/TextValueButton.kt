package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.astolfo.buttons.value

import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.astolfo.AstolfoConstants.BACKGROUND_VALUE
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.astolfo.AstolfoConstants.FONT
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.astolfo.drawHeightCenteredString
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.astolfo.geom.Rectangle
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRect
import net.ccbluex.liquidbounce.value.TextValue
import java.awt.Color

class TextValueButton(x: Float, y: Float, width: Float, height: Float, var setting: TextValue, var color: Color) : BaseValueButton(x, y, width, height, setting) {
  override fun drawPanel(mouseX: Int, mouseY: Int): Rectangle {
    val background = Rectangle(x, y, width, height)
    drawRect(background, BACKGROUND_VALUE)
    FONT.drawHeightCenteredString(setting.name, x + hOffset, y + height / 2, -0x1)

    return background
  }

  // I don't have any good idea on how to put TextValue on astolfo cgui
  override fun mouseAction(mouseX: Int, mouseY: Int, click: Boolean, button: Int) {

  }
}
