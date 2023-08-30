package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.astolfo.buttons.value

import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.astolfo.AstolfoConstants.BACKGROUND_VALUE
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.astolfo.AstolfoConstants.FONT
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.astolfo.AstolfoConstants.SLIDER_OFFSET
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.astolfo.dim
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.astolfo.drawHeightCenteredString
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.astolfo.geom.Rectangle
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRect
import net.ccbluex.liquidbounce.value.FloatValue
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import java.awt.Color
import java.math.BigDecimal
import java.math.RoundingMode

class FloatValueButton(x: Float, y: Float, width: Float, height: Float, var setting: FloatValue, var color: Color) : BaseValueButton(x, y, width, height, setting) {
  private var dragged = false
  override fun drawPanel(mouseX: Int, mouseY: Int): Rectangle {
    val background = Rectangle(x, y, width, height)
    drawRect(background, BACKGROUND_VALUE)
    val diff = (setting.maximum - setting.minimum).toDouble()
    val percentWidth = (setting.get() - setting.minimum) / (setting.maximum - setting.minimum)

    val foreground = Rectangle(x + 3, y, width - 3 * 2, height)
    foreground.width *= percentWidth

    val foreground2 = Rectangle(foreground)
    foreground2.width = (foreground2.width + SLIDER_OFFSET * (width - 3 * 2)).coerceAtMost(width - 3 * 2)

    if (setting.get() > setting.minimum) drawRect(foreground2, dim(color).rgb)
    drawRect(foreground, color.rgb)

    if (dragged) {
      val innerWidth = width - 3 * 2
      val position = (mouseX - x - 3).coerceAtMost(innerWidth).coerceAtLeast(0f)
      val value = setting.minimum + position / innerWidth * diff
      setting.set(value)
    }
    FONT.drawHeightCenteredString(setting.name, x + hOffset, y + height / 2, -0x1)

    val format = round(setting.get()).toString()
    val formatWidth = FONT.getStringWidth(format)
    FONT.drawHeightCenteredString(format, x + width - formatWidth - hOffset / 2, y + height / 2, -0x1)

    if (background.contains(mouseX, mouseY) && Mouse.hasWheel()) {
      val wheel = Mouse.getDWheel()
      if (wheel != 0) {
        val amount = if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) 0.1f else 0.01f
        setting.set(setting.get() + amount * if (wheel > 0) 1 else -1)
      }
    }

    return background
  }

  override fun mouseAction(mouseX: Int, mouseY: Int, click: Boolean, button: Int) {
    if (isHovered(mouseX, mouseY)) {
      dragged = true
    }
    if (!click) dragged = false
  }

  private fun round(f: Float): BigDecimal {
    var bd = BigDecimal(f.toString())
    bd = bd.setScale(4, RoundingMode.HALF_UP)
    return bd
  }
}
