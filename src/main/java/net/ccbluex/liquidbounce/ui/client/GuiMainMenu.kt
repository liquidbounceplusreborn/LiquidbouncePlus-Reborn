package net.ccbluex.liquidbounce.ui.client

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.ui.client.altmanager.GuiAltManager
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.minecraft.client.gui.*
import net.minecraft.client.resources.I18n
import org.lwjgl.opengl.GL11
import java.awt.Color

class GuiMainMenu : GuiScreen(), GuiYesNoCallback {
    override fun initGui() {

        val defaultHeight = (this.height / 3.5).toInt()

        this.buttonList.add(GuiButton(1, this.width / 2 - 50, defaultHeight, 100, 20, I18n.format("menu.singleplayer")))
        this.buttonList.add(GuiButton(2, this.width / 2 - 50, defaultHeight + 24, 100, 20, I18n.format("menu.multiplayer")))
        this.buttonList.add(GuiButton(100, this.width / 2 - 50, defaultHeight + 24*2, 100, 20, "AltManager"))
        this.buttonList.add(GuiButton(102, this.width / 2 - 50, defaultHeight + 24*3, 100, 20, "BackGround"))
        this.buttonList.add(GuiButton(0, this.width / 2 - 50, defaultHeight + 24*4, 100, 20, I18n.format("menu.options")))
        this.buttonList.add(GuiButton(4, this.width / 2 - 50, defaultHeight + 24*5, 100, 20, I18n.format("menu.quit")))

        super.initGui()
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawBackground(0)

        val bHeight=(this.height / 3.5).toInt()



        Fonts.fontSFUI40.drawCenteredString("Liquidbounce+",(width / 2).toFloat(), (bHeight - 20).toFloat(),Color.WHITE.rgb,false)
        super.drawScreen(mouseX, mouseY, partialTicks)

        GL11.glPushMatrix()
        GL11.glTranslatef(2f,2f,0f)


        GL11.glPopMatrix()
    }

    override fun actionPerformed(button: GuiButton) {
        when (button.id) {
            0 -> mc.displayGuiScreen(GuiOptions(this, mc.gameSettings))
            1 -> mc.displayGuiScreen(GuiSelectWorld(this))
            2 -> mc.displayGuiScreen(GuiMultiplayer(this))
            4 -> mc.shutdown()
            100 -> mc.displayGuiScreen(GuiAltManager(this))
            102 -> mc.displayGuiScreen(GuiBackground(this))
            103 -> mc.displayGuiScreen(GuiModsMenu(this))
        }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {}
}

 