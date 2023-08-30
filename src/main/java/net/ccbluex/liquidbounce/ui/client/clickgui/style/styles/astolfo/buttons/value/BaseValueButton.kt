package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.astolfo.buttons.value

import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.astolfo.AstolfoConstants.FONT
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.astolfo.buttons.AstolfoButton
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.astolfo.getHeight
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.astolfo.geom.Rectangle
import net.ccbluex.liquidbounce.value.Value

abstract class BaseValueButton(x: Float, y: Float, width: Float, height: Float, val value: Value<*>) : AstolfoButton(x, y, width, height) {
  val baseRect: Rectangle
    get() = Rectangle(x, y, width, height)
  val hOffset: Float
    get() = (height - getHeight(FONT)) / 2 + 4

  fun canDisplay() = value.canDisplay.invoke()
}