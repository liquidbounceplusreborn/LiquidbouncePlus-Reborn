package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.skeet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;

public enum GLUtils {
    INSTANCE;

    public Minecraft mc = Minecraft.getMinecraft();

    public void rescale(double factor) {
        rescale(mc.displayWidth / factor, mc.displayHeight / factor);
    }

    public void rescaleMC() {
        ScaledResolution resolution = new ScaledResolution(mc);
        rescale(mc.displayWidth / resolution.getScaleFactor(),mc.displayHeight / resolution.getScaleFactor());
    }

    public void rescale(double width, double height) {
        GlStateManager.clear(256);
        GlStateManager.matrixMode(5889);
        GlStateManager.loadIdentity();
        GlStateManager.ortho(0.0D, width, height, 0.0D, 1000.0D, 3000.0D);
        GlStateManager.matrixMode(5888);
        GlStateManager.loadIdentity();
        GlStateManager.translate(0.0F, 0.0F, -2000.0F);
    }
}
