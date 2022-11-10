package net.ccbluex.liquidbounce.features.module.modules.render;


import net.ccbluex.liquidbounce.event.EventTarget;
import net.ccbluex.liquidbounce.event.Render2DEvent;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL11;

import java.awt.*;
@ModuleInfo(name = "FPSHurtCam", description = "qwq", category = ModuleCategory.RENDER)
public class FPSHurtCam extends Module {
    int alpha2 = 0;
    @EventTarget
    private void renderHud(Render2DEvent event) {
        {
            ScaledResolution sr = new ScaledResolution(this.mc);
            if(mc.thePlayer.hurtTime >= 1) {
                if(alpha2 < 100) {
                    alpha2 += 5;
                }
            }else {
                if(alpha2 > 0) {
                    alpha2 -= 5;
                }
            }
            this.drawGradientSidewaysV(0, 0, sr.getScaledWidth(), 25,  new Color(255,0,0,0).getRGB(),new Color(255,0,0,alpha2).getRGB());
            this.drawGradientSidewaysV(0, sr.getScaledHeight() - 25, sr.getScaledWidth(),sr.getScaledHeight(),  new Color(255,0,0,alpha2).getRGB(),new Color(255,0,0,0).getRGB());
        }

    }
    public static void drawGradientSidewaysV(double left, double top, double right, double bottom, int col1, int col2) {
        float f = (float) (col1 >> 24 & 255) / 255.0f;
        float f1 = (float) (col1 >> 16 & 255) / 255.0f;
        float f2 = (float) (col1 >> 8 & 255) / 255.0f;
        float f3 = (float) (col1 & 255) / 255.0f;
        float f4 = (float) (col2 >> 24 & 255) / 255.0f;
        float f5 = (float) (col2 >> 16 & 255) / 255.0f;
        float f6 = (float) (col2 >> 8 & 255) / 255.0f;
        float f7 = (float) (col2 & 255) / 255.0f;
        GL11.glEnable((int) 3042);
        GL11.glDisable((int) 3553);
        GL11.glBlendFunc((int) 770, (int) 771);
        GL11.glEnable((int) 2848);
        GL11.glShadeModel((int) 7425);
        GL11.glPushMatrix();
        GL11.glBegin((int) 7);
        GL11.glColor4f((float) f1, (float) f2, (float) f3, (float) f);
        GL11.glVertex2d((double) left, (double) bottom);
        GL11.glVertex2d((double) right, (double) bottom);
        GL11.glColor4f((float) f5, (float) f6, (float) f7, (float) f4);
        GL11.glVertex2d((double) right, (double) top);
        GL11.glVertex2d((double) left, (double) top);
        GL11.glEnd();
        GL11.glPopMatrix();
        GL11.glEnable((int) 3553);
        GL11.glDisable((int) 3042);
        GL11.glDisable((int) 2848);
        GL11.glShadeModel((int) 7424);
        Gui.drawRect(0, 0, 0, 0, 0);
    }
}
