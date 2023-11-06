package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.astolfo.buttons.value

import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.astolfo.AstolfoConstants.BACKGROUND_VALUE
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.astolfo.AstolfoConstants.FONT
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.astolfo.drawHeightCenteredString
import net.ccbluex.liquidbounce.utils.geom.Rectangle
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRect
import net.ccbluex.liquidbounce.value.NoteValue
import net.minecraft.util.EnumChatFormatting
import java.awt.Color

class NoteValueButton(x: Float, y: Float, width: Float, height: Float, var setting: NoteValue, var color: Color) : BaseValueButton(x, y, width, height, setting) {
    override fun drawPanel(mouseX: Int, mouseY: Int): Rectangle {
        val background = Rectangle(x, y, width, height)
        drawRect(background, BACKGROUND_VALUE)
        val format = "-- ${EnumChatFormatting.BOLD}${setting.name} --"
        FONT.drawHeightCenteredString(format, x + (width - FONT.getStringWidth(format)) / 2, y + height / 2, if (setting.open) Color(0, 255, 0).rgb else Color(255, 0, 0).rgb)
        return background
    }

    override fun mouseAction(mouseX: Int, mouseY: Int, click: Boolean, button: Int) {
        if (click && baseRect.contains(mouseX, mouseY)) setting.open = !setting.open
    }
}