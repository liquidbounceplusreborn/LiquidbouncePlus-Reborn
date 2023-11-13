package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.skeet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import java.math.BigDecimal;
import java.math.MathContext;

public class SkeetUtils {
    public static void drawGradientRect(float left, float top, float right, float bottom, boolean sideways, int startColor, int endColor) {
        float f = (startColor >> 24 & 255) / 255.0F;
        float f1 = (startColor >> 16 & 255) / 255.0F;
        float f2 = (startColor >> 8 & 255) / 255.0F;
        float f3 = (startColor & 255) / 255.0F;

        float f4 = (endColor >> 24 & 255) / 255.0F;
        float f5 = (endColor >> 16 & 255) / 255.0F;
        float f6 = (endColor >> 8 & 255) / 255.0F;
        float f7 = (endColor & 255) / 255.0F;

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(7425);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);

        if (sideways) {
            worldrenderer.pos(right, top, 0).color(f5, f6, f7, f4).endVertex();
            worldrenderer.pos(left, top, 0).color(f1, f2, f3, f).endVertex();
            worldrenderer.pos(left, bottom, 0).color(f1, f2, f3, f).endVertex();
            worldrenderer.pos(right, bottom, 0).color(f5, f6, f7, f4).endVertex();
        } else {
            worldrenderer.pos(right, top, 0).color(f1, f2, f3, f).endVertex();
            worldrenderer.pos(left, top, 0).color(f1, f2, f3, f).endVertex();
            worldrenderer.pos(left, bottom, 0).color(f5, f6, f7, f4).endVertex();
            worldrenderer.pos(right, bottom, 0).color(f5, f6, f7, f4).endVertex();
        }

        tessellator.draw();
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    public static int darker(int color, float factor) {
        int r = (int) ((color >> 16 & 0xFF) * factor);
        int g = (int) ((color >> 8 & 0xFF) * factor);
        int b = (int) ((color & 0xFF) * factor);
        int a = color >> 24 & 0xFF;
        return (r & 0xFF) << 16 | (g & 0xFF) << 8 | b & 0xFF | (a & 0xFF) << 24;
    }

    public static String upperSnakeCaseToPascal(String s) {
        if (s == null) {
            return null;
        }
        if (s.length() == 1) {
            return Character.toString(s.charAt(0));
        }
        return String.valueOf(s.charAt(0)) + s.substring(1).toLowerCase();
    }

    public static void startScissorBox(LockedResolution lr, int x, int y, int width, int height) {
        GL11.glScissor((x * 2), ((lr.getHeight() - (y + height)) * 2), (width * 2), (height * 2));
    }

    public static void drawAndRotateArrow(float x, float y, float size, boolean rotate) {
        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, 0f);
        GL11.glEnable((int)3042);
        GL11.glBlendFunc((int)770, (int)771);
        GL11.glEnable(2848);
        GL11.glHint(3154, 4354);
        GL11.glLineWidth(1.0f);
        GL11.glDisable(3553);
        GL11.glBegin(4);

        if (rotate) {
            GL11.glVertex2f(size, (size / 2.0f));
            GL11.glVertex2f((size / 2.0f), 0.0f);
            GL11.glVertex2f(0.0f, (size / 2.0f));
        } else {
            GL11.glVertex2f(0.0f, 0.0f);
            GL11.glVertex2f((size / 2.0f), (size / 2.0f));
            GL11.glVertex2f(size, 0.0f);
        }

        GL11.glEnd();
        GL11.glDisable(2848);
        GL11.glEnable(3553);
        GL11.glDisable(3042);
        GL11.glPopMatrix();
    }

    public static void drawImage(float x, float y, float width, float height, float r, float g, float b, ResourceLocation image) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(image);
        float f = 1.0f / width;
        float f1 = 1.0f / height;
        GL11.glColor4f((float)r, (float)g, (float)b, (float)1.0f);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(x, y + height, 0.0).tex(0.0, height * f1).endVertex();
        worldrenderer.pos(x + width, y + height, 0.0).tex(width * f, height * f1).endVertex();
        worldrenderer.pos(x + width, y, 0.0).tex(width * f, 0.0).endVertex();
        worldrenderer.pos(x, y, 0.0).tex(0.0, 0.0).endVertex();
        tessellator.draw();
    }

    public static double linearAnimation(double now, double desired, double speed) {
        double dif = Math.abs(now - desired);
        int fps = Minecraft.getDebugFPS();
        if (dif > 0.0) {
            double animationSpeed = roundToDecimalPlace(Math.min(10.0, Math.max(0.005, 144.0 / (double)fps * speed)), 0.005);
            if (dif != 0.0 && dif < animationSpeed) {
                animationSpeed = dif;
            }
            if (now < desired) {
                return now + animationSpeed;
            }
            if (now > desired) {
                return now - animationSpeed;
            }
        }
        return now;
    }

    public static float smoothAnimation(float ani, float finalState, float speed, float scale) {
        return AnimationUtils.getAnimationState(ani, finalState, (float) Math.max(10, (Math.abs(ani - finalState)) * speed) * scale);
    }

    public static double roundToDecimalPlace(double value, double inc) {
        double halfOfInc = inc / 2.0;
        double floored = StrictMath.floor(value / inc) * inc;
        if (value >= floored + halfOfInc) {
            return new BigDecimal(StrictMath.ceil(value / inc) * inc, MathContext.DECIMAL64).stripTrailingZeros().doubleValue();
        }
        return new BigDecimal(floored, MathContext.DECIMAL64).stripTrailingZeros().doubleValue();
    }
}
