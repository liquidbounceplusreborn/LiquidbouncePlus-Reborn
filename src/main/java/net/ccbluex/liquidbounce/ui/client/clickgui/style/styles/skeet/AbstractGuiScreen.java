package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.skeet;

import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.IOException;

public class AbstractGuiScreen extends GuiScreen {
    public float scale;
    public float curWidth = 0;
    public float curHeight = 0;

    public AbstractGuiScreen() {
        this(2);
    }

    public AbstractGuiScreen(int scale) {
        this.scale = scale;
    }

    public void doInit() {};
    public void drawScr(int mouseX, int mouseY, float partialTicks) {}
    public void mouseClick(int mouseX, int mouseY, int mouseButton) {}
    public void mouseRelease(int mouseX, int mouseY, int state) {}

    @Override
    public void initGui() {
        this.doInit();

        super.initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        GLUtils.INSTANCE.rescale(this.scale);
        curWidth = mc.displayWidth / scale;
        curHeight = mc.displayHeight / scale;

        this.drawScr(this.getRealMouseX(), this.getRealMouseY(), partialTicks);

        //Debug
        //RenderUtil.drawRect(this.getRealMouseX(), this.getRealMouseY(), this.getRealMouseX() + 5, this.getRealMouseY() + 5, Colors.WHITE.c);
        //mc.fontRendererObj.drawString("Cursor", this.getRealMouseX() + 8, this.getRealMouseY(), -1);

        //mc.fontRendererObj.drawString(this.getRealMouseX() + " - CurScreen MouseX", 2, 2, -1);
        //mc.fontRendererObj.drawString(this.getRealMouseY() + " - CurScreen MouseY", 2, 12, -1);

        //mc.fontRendererObj.drawString(mouseX + " - Minecraft Scaled MouseX", 2, 32, -1);
        //mc.fontRendererObj.drawString(mouseY + " - Minecraft Scaled MouseY", 2, 42, -1);

        //mc.fontRendererObj.drawString(mc.gameSettings.guiScale + " - Minecraft scale factor", 2, 62, -1);

        GLUtils.INSTANCE.rescaleMC();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        this.mouseClick(this.getRealMouseX(), this.getRealMouseY(), mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        this.mouseRelease(this.getRealMouseX(), this.getRealMouseY(), state);
        super.mouseReleased(mouseX, mouseY, state);
    }

    public int getRealMouseX() {
        return (int) ((Mouse.getX() * (mc.displayWidth / scale)) / mc.displayWidth);
    }

    public int getRealMouseY() {
        float scaleHeight = (mc.displayHeight / scale);
        return (int) (scaleHeight - (Mouse.getY() * scaleHeight) / mc.displayHeight);
    }

    public void doGlScissor(int x, int y, float width, float height) {
        int scaleFactor = 1;
        float sc = scale;

        while (scaleFactor < sc && mc.displayWidth / (scaleFactor + 1) >= 320  && mc.displayHeight / (scaleFactor + 1) >= 240) {
            ++scaleFactor;
        }

        GL11.glScissor((int) (x * scaleFactor), (int) (mc.displayHeight - (y + height) * scaleFactor), (int) (width * scaleFactor), (int) (height * scaleFactor));
    }
}
