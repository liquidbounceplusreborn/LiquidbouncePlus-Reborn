package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.astolfo

import net.ccbluex.liquidbounce.ui.font.GameFontRenderer
import net.ccbluex.liquidbounce.utils.extensions.darker
import net.minecraft.client.gui.FontRenderer
import java.awt.Color

fun FontRenderer.drawHeightCenteredString(string: String, x: Float, y: Float, color: Int) {
  this.drawStringWithShadow(string, x, y - FONT_HEIGHT / 2, color)
}

fun getHeight(font: GameFontRenderer) = font.height
fun getHeight(font: FontRenderer) = font.FONT_HEIGHT

fun dim(color: Color): Color {
  return color.darker(0.6f)
}