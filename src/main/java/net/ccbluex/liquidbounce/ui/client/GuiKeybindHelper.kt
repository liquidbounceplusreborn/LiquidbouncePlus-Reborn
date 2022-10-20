/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.ui.client

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.misc.MiscUtils
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.util.ResourceLocation
import org.lwjgl.input.Keyboard
import java.awt.Color
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Files
import javax.imageio.ImageIO

class GuiKeybindHelper(val prevGui: GuiScreen) : GuiScreen() {

    private var pressedKey = 0

    override fun initGui() {
        buttonList.add(GuiButton(0, width / 2 - 100, height - 30, "Back"))
    }

    override fun actionPerformed(button: GuiButton) {
        mc.displayGuiScreen(prevGui)
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawDefaultBackground()
        Fonts.fontSFUI40.drawCenteredString("Pressed key:", width / 2F, height / 2F - 12F, Color.LIGHT_GRAY.rgb)
        Fonts.font72.drawCenteredString(if (pressedKey == 0) "Listening..." else Keyboard.getKeyName(pressedKey), width / 2F, height / 2F, -1)
        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (Keyboard.KEY_ESCAPE == keyCode) {
            mc.displayGuiScreen(prevGui)
            return
        }

        pressedKey = keyCode
        super.keyTyped(typedChar, keyCode)
    }

}