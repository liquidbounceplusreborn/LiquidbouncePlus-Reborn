package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.astolfo.buttons

import net.ccbluex.liquidbounce.LiquidBounce.commandManager
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.astolfo.AstolfoConstants.BACKGROUND_MODULE
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.astolfo.AstolfoConstants.BACKGROUND_VALUE
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.astolfo.AstolfoConstants.FONT
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.astolfo.AstolfoConstants.MODULE_HEIGHT
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.astolfo.AstolfoConstants.VALUE_HEIGHT
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.astolfo.buttons.value.*
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.astolfo.drawHeightCenteredString
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.astolfo.getHeight
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.astolfo.geom.Rectangle
import net.ccbluex.liquidbounce.utils.MouseButtons
import net.ccbluex.liquidbounce.utils.extensions.setAlpha
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRect
import net.ccbluex.liquidbounce.value.*
import net.minecraft.util.EnumChatFormatting
import org.lwjgl.input.Keyboard
import java.awt.Color

class AstolfoModuleButton(x: Float, y: Float, width: Float, height: Float, var module: Module, var color: Color) : AstolfoButton(x, y, width, height) {
  var open = false
  var valueButtons = ArrayList<BaseValueButton>()
  private val shifting: Boolean
    get() = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)

  init {
    val startY = y + height
    for ((count, v) in module.values.withIndex()) { // has to come before integer value
      when (v) {
        is BlockValue -> valueButtons.add(BlockValueButton(x, startY + MODULE_HEIGHT * count, width, VALUE_HEIGHT, v, color))
        is BoolValue -> valueButtons.add(BoolValueButton(x, startY + MODULE_HEIGHT * count, width, VALUE_HEIGHT, v, color))
        is ListValue -> valueButtons.add(ListValueButton(x, startY + MODULE_HEIGHT * count, width, VALUE_HEIGHT, v, color))
        is IntegerValue -> valueButtons.add(IntegerValueButton(x, startY + MODULE_HEIGHT * count, width, VALUE_HEIGHT, v, color))
        is FloatValue -> valueButtons.add(FloatValueButton(x, startY + MODULE_HEIGHT * count, width, VALUE_HEIGHT, v, color))
        is FontValue -> valueButtons.add(FontValueButton(x, startY + MODULE_HEIGHT * count, width, VALUE_HEIGHT, v, color))
      }
    }
  }

  override fun drawPanel(mouseX: Int, mouseY: Int): Rectangle {
    val background = Rectangle(x, y, width, height)
    drawRect(background, BACKGROUND_MODULE)

    val foreground = Rectangle(x + 2, y, width - 2 * 2, height)
    if (open) drawRect(foreground, -0xdcd9dd)
    else if (module.state) drawRect(foreground, color.rgb)
    else drawRect(foreground, BACKGROUND_MODULE)

    FONT.drawHeightCenteredString(module.name.lowercase(), x + (height - getHeight(FONT)) / 2 + 3, y + height / 2, if (open && module.state) color.rgb else -0x1)

    if (valueButtons.size > 0) {
      val char = if (open) "-" else "+"
      FONT.drawHeightCenteredString(char, x + width - FONT.getStringWidth(char) - 4, y + height / 2, Int.MAX_VALUE)
    }

    if (module.keyBind != Keyboard.KEY_NONE) {
      val name = Keyboard.getKeyName(module.keyBind)
      val format = "${EnumChatFormatting.GRAY}[$name]"
      FONT.drawHeightCenteredString(format, x + width - FONT.getStringWidth(format) - FONT.getStringWidth("+") - 10, y + height / 2, Int.MAX_VALUE)
    }

    var used = 0f
    var count = 0

    var should = true

    if (open) {
      val startY = y + height
      for (valueButton in valueButtons) {
        if (!valueButton.canDisplay()) continue
        if (!should) continue

        valueButton.x = x
        valueButton.y = startY + used
        val box = valueButton.drawPanel(mouseX, mouseY)
        used += box.height
        count++
      }
    }

    //		if (extended)
    //			ClientUtils.logger.info("ModuleButton(${module.name}): used $used px for $count/${module.values.size} values, height=$height")
    return Rectangle(x, y, width, used + height)
  }

  override fun mouseAction(mouseX: Int, mouseY: Int, click: Boolean, button: Int) {
    if (isHovered(mouseX, mouseY) && click) {
      when (button) {
        MouseButtons.LEFT.ordinal -> if (shifting) commandManager.executeCommands("${commandManager.prefix}hide ${module.name}") else module.toggle()
        MouseButtons.RIGHT.ordinal -> if (shifting) return else if (module.values.isNotEmpty()) open = !open
      }
    }

    if (isHovered(mouseX, mouseY)) {
      val desc = module.description
      if (desc.isEmpty()) return
      val offset = 3
      val box = Rectangle(mouseX.toFloat(), mouseY.toFloat(), FONT.getStringWidth(desc) + 2f * offset, FONT.FONT_HEIGHT + 2f * offset)
      val boxColor = Color(BACKGROUND_VALUE)
      boxColor.setAlpha(190)
      val textColor = Color(255, 255, 255, 190)

      drawRect(box, boxColor.rgb)
      FONT.drawString(desc, (box.x + offset).toInt(), (box.y + offset).toInt(), textColor.rgb)
    }
  }
}
