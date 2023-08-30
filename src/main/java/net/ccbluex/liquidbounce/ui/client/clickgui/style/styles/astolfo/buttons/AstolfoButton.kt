package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.astolfo.buttons

import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.astolfo.geom.Rectangle

abstract class AstolfoButton(var x: Float, var y: Float, var width: Float, var height: Float) {
  abstract fun drawPanel(mouseX: Int, mouseY: Int): Rectangle // return used height
  abstract fun mouseAction(mouseX: Int, mouseY: Int, click: Boolean, button: Int)
  fun isHovered(mouseX: Int, mouseY: Int): Boolean {
    return mouseX >= x && mouseX <= x + width && mouseY > y && mouseY < y + height
  }
}
