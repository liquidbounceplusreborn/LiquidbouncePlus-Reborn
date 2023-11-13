package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.skeet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class RenderUtil {
    public static Minecraft mc = Minecraft.getMinecraft();

    public static void drawRoundedRect(float left, float top, float right, float bottom, int color) {
        //Left
        RenderUtil.drawRect(left - 0.5f, top + 0.5f, left, bottom - 0.5f, color);
        //Right
        RenderUtil.drawRect(right, top + 0.5f, right + 0.5f, bottom - 0.5f, color);
        //Top
        RenderUtil.drawRect(left + 0.5f, top - 0.5f, right - 0.5f, top, color);
        //Bottom
        RenderUtil.drawRect(left + 0.5f, bottom, right - 0.5f, bottom + 0.5f, color);
        RenderUtil.drawRect(left, top, right, bottom, color);
    }

    public static void drawBorderedRect(float left, float top, float right, float bottom, float thickness, int color) {
        //Left
        RenderUtil.drawRect(left - thickness, top, left, bottom, color);
        //Right
        RenderUtil.drawRect(right, top, right + thickness, bottom, color);
        //Top
        RenderUtil.drawRect(left, top + thickness, right, top, color);
        //Bottom
        RenderUtil.drawRect(left, bottom, right, bottom + thickness, color);
    }

    public static void drawImage(ResourceLocation image, int x, int y, float width, float height, float alpha) {
        GL11.glPushMatrix();
        GL11.glDisable((int) 2929);
        GL11.glEnable((int) 3042);
        GL11.glDepthMask((boolean) false);
        OpenGlHelper.glBlendFunc((int) 770, (int) 771, (int) 1, (int) 0);
        GL11.glColor4f((float) 1.0f, (float) 0.0f, (float) 0.0f, alpha);
        mc.getTextureManager().bindTexture(image);
        Gui.drawModalRectWithCustomSizedTexture((int) x, (int) y, (float) 0.0f, (float) 0.0f, (int) width, (int) height,
                (float) width, (float) height);
        GL11.glDepthMask((boolean) true);
        GL11.glDisable((int) 3042);
        GL11.glEnable((int) 2929);
        GL11.glPopMatrix();

        GL11.glColor4f((float) 1.0f, (float) 1.0f, (float) 1.0f, 1f);
    }

    public static int reAlpha(int color, float alpha) {
        try {
            Color c = new Color(color);
            float r = ((float) 1 / 255) * c.getRed();
            float g = ((float) 1 / 255) * c.getGreen();
            float b = ((float) 1 / 255) * c.getBlue();
            return new Color(r, g, b, alpha).getRGB();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return color;
    }

    public static boolean isHovering(float mouseX, float mouseY, float xLeft, float yUp, float xRight, float yBottom) {
        return mouseX > xLeft && mouseX < xRight && mouseY > yUp && mouseY < yBottom;
    }

    public static boolean isHoveringBound(float mouseX, float mouseY, float xLeft, float yUp, float width, float height) {
        return mouseX > xLeft && mouseX < xLeft + width && mouseY > yUp && mouseY < yUp + height;
    }

    public static void drawRoundedRect(float x, float y, float right, float bottom, int borderC, int insideC) {
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
        GL11.glHint(GL11.GL_POLYGON_SMOOTH_HINT, GL11.GL_NICEST);
        GL11.glScalef((float) 0.5f, (float) 0.5f, (float) 0.5f);
        drawVLine(x *= 2.0f, (y *= 2.0f) + 1.0f, (bottom *= 2.0f) - 2.0f, borderC);
        drawVLine((right *= 2.0f) - 1.0f, y + 1.0f, bottom - 2.0f, borderC);
        drawHLine(x + 2.0f, right - 3.0f, y, borderC);
        drawHLine(x + 2.0f, right - 3.0f, bottom - 1.0f, borderC);
        drawHLine(x + 1.0f, x + 1.0f, y + 1.0f, borderC);
        drawHLine(right - 2.0f, right - 2.0f, y + 1.0f, borderC);
        drawHLine(right - 2.0f, right - 2.0f, bottom - 2.0f, borderC);
        drawHLine(x + 1.0f, x + 1.0f, bottom - 2.0f, borderC);
        RenderUtil.drawRect(x + 1.0f, y + 1.0f, right - 1.0f, bottom - 1.0f, insideC);
        GL11.glScalef((float) 2.0f, (float) 2.0f, (float) 2.0f);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_DONT_CARE);
        GL11.glHint(GL11.GL_POLYGON_SMOOTH_HINT, GL11.GL_DONT_CARE);
    }

    public static void drawHLine(float x, float y, float right, int bottom) {
        if (y < x) {
            float var5 = x;
            x = y;
            y = var5;
        }
        drawRect(x, right, y + 1, right + 1, bottom);
    }

    public static void drawVLine(float x, float y, float right, int bottom) {
        if (right < y) {
            float var5 = y;
            y = right;
            right = var5;
        }
        drawRect(x, y + 1, x + 1, right, bottom);
    }

    public static void drawRect(float left, float top, float right, float bottom, int color) {

        float e;

        if (left < right) {
            e = left;
            left = right;
            right = e;
        }

        if (top < bottom) {
            e = top;
            top = bottom;
            bottom = e;
        }

        float a = (float) (color >> 24 & 255) / 255.0F;
        float b = (float) (color >> 16 & 255) / 255.0F;
        float c = (float) (color >> 8 & 255) / 255.0F;
        float d = (float) (color & 255) / 255.0F;
        WorldRenderer worldRenderer = Tessellator.getInstance().getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(b, c, d, a);
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(left, bottom, 0.0D).endVertex();
        worldRenderer.pos(right, bottom, 0.0D).endVertex();
        worldRenderer.pos(right, top, 0.0D).endVertex();
        worldRenderer.pos(left, top, 0.0D).endVertex();
        Tessellator.getInstance().draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void drawOutlinedRect(float x, float y, float width, float height, float lineSize, int lineColor) {
        RenderUtil.drawRect(x, y, width, y + lineSize, lineColor);
        RenderUtil.drawRect(x, height - lineSize, width, height, lineColor);
        RenderUtil.drawRect(x, y + lineSize, x + lineSize, height - lineSize, lineColor);
        RenderUtil.drawRect(width - lineSize, y + lineSize, width, height - lineSize, lineColor);
    }

    public static void drawFastRoundedRect(int left, float top, int right, float bottom, float radius, int color) {
        final int semicircle = 18;
        final float f = 90.0f / semicircle;
        GL11.glDisable(2884);
        GL11.glDisable(3553);
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        color(color);
        GL11.glBegin(5);
        GL11.glVertex2f(left + radius, top);
        GL11.glVertex2f(left + radius, bottom);
        GL11.glVertex2f(right - radius, top);
        GL11.glVertex2f(right - radius, bottom);
        GL11.glEnd();
        GL11.glBegin(5);
        GL11.glVertex2f(left, top + radius);
        GL11.glVertex2f(left + radius, top + radius);
        GL11.glVertex2f(left, bottom - radius);
        GL11.glVertex2f(left + radius, bottom - radius);
        GL11.glEnd();
        GL11.glBegin(5);
        GL11.glVertex2f(right, top + radius);
        GL11.glVertex2f(right - radius, top + radius);
        GL11.glVertex2f(right, bottom - radius);
        GL11.glVertex2f(right - radius, bottom - radius);
        GL11.glEnd();
        GL11.glBegin(6);
        float f6 = right - radius;
        float f7 = top + radius;
        GL11.glVertex2f(f6, f7);
        int j = 0;
        for (j = 0; j <= semicircle; ++j) {
            final float f8 = j * f;
            GL11.glVertex2f((float) (f6 + radius * Math.cos(Math.toRadians(f8))), (float) (f7 - radius * Math.sin(Math.toRadians(f8))));
        }
        GL11.glEnd();
        GL11.glBegin(6);
        f6 = left + radius;
        f7 = top + radius;
        GL11.glVertex2f(f6, f7);
        for (j = 0; j <= semicircle; ++j) {
            final float f9 = j * f;
            GL11.glVertex2f((float) (f6 - radius * Math.cos(Math.toRadians(f9))), (float) (f7 - radius * Math.sin(Math.toRadians(f9))));
        }
        GL11.glEnd();
        GL11.glBegin(6);
        f6 = left + radius;
        f7 = bottom - radius;
        GL11.glVertex2f(f6, f7);
        for (j = 0; j <= semicircle; ++j) {
            final float f10 = j * f;
            GL11.glVertex2f((float) (f6 - radius * Math.cos(Math.toRadians(f10))), (float) (f7 + radius * Math.sin(Math.toRadians(f10))));
        }
        GL11.glEnd();
        GL11.glBegin(6);
        f6 = right - radius;
        f7 = bottom - radius;
        GL11.glVertex2f(f6, f7);
        for (j = 0; j <= semicircle; ++j) {
            final float f11 = j * f;
            GL11.glVertex2f((float) (f6 + radius * Math.cos(Math.toRadians(f11))), (float) (f7 + radius * Math.sin(Math.toRadians(f11))));
        }
        GL11.glEnd();
        GL11.glEnable(3553);
        GL11.glEnable(2884);
        GL11.glDisable(3042);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static int width() {
        return new ScaledResolution(mc).getScaledWidth();
    }

    public static int height() {
        return new ScaledResolution(mc).getScaledHeight();
    }

    public static void drawRoundedRect(float x, float y, float x2, float y2, float round, int color) {
        x = (float) ((double) x + ((double) (round / 2.0f) + 0.5));
        y = (float) ((double) y + ((double) (round / 2.0f) + 0.5));
        x2 = (float) ((double) x2 - ((double) (round / 2.0f) + 0.5));
        y2 = (float) ((double) y2 - ((double) (round / 2.0f) + 0.5));
        drawRect((float) x, (float) y, (float) x2, (float) y2, (int) color);
        circle((float) (x2 - round / 2.0f), (float) (y + round / 2.0f), (float) round, (int) color);
        circle((float) (x + round / 2.0f), (float) (y2 - round / 2.0f), (float) round, (int) color);
        circle((float) (x + round / 2.0f), (float) (y + round / 2.0f), (float) round, (int) color);
        circle((float) (x2 - round / 2.0f), (float) (y2 - round / 2.0f), (float) round, (int) color);
        drawRect((float) (x - round / 2.0f - 0.5f), (float) (y + round / 2.0f), (float) x2, (float) (y2 - round / 2.0f), (int) color);
        drawRect((float) x, (float) (y + round / 2.0f), (float) (x2 + round / 2.0f + 0.5f), (float) (y2 - round / 2.0f), (int) color);
        drawRect((float) (x + round / 2.0f), (float) (y - round / 2.0f - 0.5f), (float) (x2 - round / 2.0f), (float) (y2 - round / 2.0f), (int) color);
        drawRect((float) (x + round / 2.0f), (float) y, (float) (x2 - round / 2.0f), (float) (y2 + round / 2.0f + 0.5f), (int) color);
    }


    public static void circle(float x, float y, float radius, int fill) {
        arc(x, y, 0.0f, 360.0f, radius, fill);
    }

    public static void arc(float x, float y, float start, float end, float radius, int color) {
        arcEllipse(x, y, start, end, radius, radius, color);
    }

    public static void color(int color) {
        float f = (float) (color >> 24 & 255) / 255.0f;
        float f1 = (float) (color >> 16 & 255) / 255.0f;
        float f2 = (float) (color >> 8 & 255) / 255.0f;
        float f3 = (float) (color & 255) / 255.0f;
        GL11.glColor4f((float) f1, (float) f2, (float) f3, (float) f);
    }

    public static void arcEllipse(float x, float y, float start, float end, float w, float h, int color) {
        float ldy;
        float ldx;
        float i;
        GlStateManager.color((float) 0.0f, (float) 0.0f, (float) 0.0f);
        GL11.glColor4f((float) 0.0f, (float) 0.0f, (float) 0.0f, (float) 0.0f);
        float temp = 0.0f;
        if (start > end) {
            temp = end;
            end = start;
            start = temp;
        }
        float var11 = (float) (color >> 24 & 255) / 255.0f;
        float var6 = (float) (color >> 16 & 255) / 255.0f;
        float var7 = (float) (color >> 8 & 255) / 255.0f;
        float var8 = (float) (color & 255) / 255.0f;
        Tessellator var9 = Tessellator.getInstance();
        WorldRenderer var10 = var9.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate((int) 770, (int) 771, (int) 1, (int) 0);
        GlStateManager.color((float) var6, (float) var7, (float) var8, (float) var11);
        if (var11 > 0.5f) {
            GL11.glEnable((int) 2848);
            GL11.glLineWidth((float) 2.0f);
            GL11.glBegin((int) 3);
            i = end;
            while (i >= start) {
                ldx = (float) Math.cos((double) ((double) i * 3.141592653589793 / 180.0)) * (w * 1.001f);
                ldy = (float) Math.sin((double) ((double) i * 3.141592653589793 / 180.0)) * (h * 1.001f);
                GL11.glVertex2f((float) (x + ldx), (float) (y + ldy));
                i -= 4.0f;
            }
            GL11.glEnd();
            GL11.glDisable((int) 2848);
        }
        GL11.glBegin((int) 6);
        i = end;
        while (i >= start) {
            ldx = (float) Math.cos((double) ((double) i * 3.141592653589793 / 180.0)) * w;
            ldy = (float) Math.sin((double) ((double) i * 3.141592653589793 / 180.0)) * h;
            GL11.glVertex2f((float) (x + ldx), (float) (y + ldy));
            i -= 4.0f;
        }
        GL11.glEnd();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void drawCircle(float x, float y, int start, int end, float radius, int color) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_POLYGON_SMOOTH);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        color(color);
        GL11.glBegin(GL11.GL_POLYGON);
        for (int i = start; i <= end; i++) {
            double x2 = Math.sin(((i * Math.PI) / 180)) * radius;
            double y2 = Math.cos(((i * Math.PI) / 180)) * radius;
            GL11.glVertex3d(x + x2, y + y2, 0);
        }
        GL11.glEnd();
        GL11.glDisable(GL11.GL_POLYGON_SMOOTH);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
    }

    public static void drawCircleWithTexture(float cX, float cY, int start, int end, float radius, ResourceLocation res, int color) {
        double radian, x, y, tx, ty, xsin, ycos;
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        mc.getTextureManager().bindTexture(res);
        GL11.glEnable(GL11.GL_POLYGON_SMOOTH);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        color(color);
        GL11.glBegin(GL11.GL_POLYGON);
        for (int i = start; i < end; ++i) {
            radian = i * (Math.PI / 180.0f);
            xsin = Math.sin(radian);
            ycos = Math.cos(radian);

            x = xsin * radius;
            y = ycos * radius;

            tx = xsin * 0.5 + 0.5;
            ty = ycos * 0.5 + 0.5;

            GL11.glTexCoord2d(cX + tx, cY + ty);
            GL11.glVertex2d(cX + x, cY + y);
        }
        GL11.glEnd();
        GL11.glDisable(GL11.GL_POLYGON_SMOOTH);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glPopMatrix();
    }

    public static void layeredRect(float right, float bottom, float x2, float y2, int outline, int inline, int background) {
        drawRect(right, bottom, x2, y2, outline);
        drawRect(right + 0.5f, bottom + 0.5f, x2 - 0.5f, y2 - 0.5f, inline);
        drawRect(right + 1f, bottom + 1f, x2 - 1f, y2 - 1f, background);
    }


    public static void drawOutlinedBoundingBox(AxisAlignedBB aa) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.begin(3, DefaultVertexFormats.POSITION);
        worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(3, DefaultVertexFormats.POSITION);
        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(1, DefaultVertexFormats.POSITION);
        worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
        tessellator.draw();
    }

    public static void drawBoundingBox(AxisAlignedBB aa) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
        tessellator.draw();
    }

    public static void drawEntityESP(double x, double y, double z, double width, double height, float red,
                                     float green, float blue, float alpha) {
        GL11.glPushMatrix();
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glDisable(3553);
        GL11.glEnable(2848);
        GL11.glDisable(2929);
        GL11.glDepthMask(false);
        GL11.glLineWidth(1.0F);
        GL11.glColor4f(red, green, blue, 1.0f);
        drawOutlinedBoundingBox(new AxisAlignedBB(x - width, y, z - width, x + width, y + height, z + width));
        GL11.glColor4f(red, green, blue, alpha);
        drawBoundingBox(new AxisAlignedBB(x - width, y, z - width, x + width, y + height, z + width));
        GL11.glDisable(2848);
        GL11.glEnable(3553);
        GL11.glEnable(2929);
        GL11.glDepthMask(true);
        GL11.glDisable(3042);
        GL11.glPopMatrix();
    }

    public static void drawEntityESP(double x, double y, double z, double x1, double y1, double z1, float red,
                                     float green, float blue, float alpha) {
        GL11.glPushMatrix();
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glDisable(3553);
        GL11.glEnable(2848);
        GL11.glDisable(2929);
        GL11.glDepthMask(false);
        GL11.glLineWidth(1.0F);
        GL11.glColor4f(red, green, blue, 1.0f);
        drawOutlinedBoundingBox(new AxisAlignedBB(x, y, z, x1, y1, z1));
        GL11.glColor4f(red, green, blue, alpha);
        drawBoundingBox(new AxisAlignedBB(x, y, z, x1, y1, z1));
        GL11.glDisable(2848);
        GL11.glEnable(3553);
        GL11.glEnable(2929);
        GL11.glDepthMask(true);
        GL11.glDisable(3042);
        GL11.glPopMatrix();
    }

    public static void drawEntityESP(AxisAlignedBB axisAlignedBB, float red,
                                     float green, float blue, float alpha) {
        GL11.glPushMatrix();
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glDisable(3553);
        GL11.glEnable(2848);
        GL11.glDisable(2929);
        GL11.glDepthMask(false);
        GL11.glLineWidth(1.0F);
        GL11.glColor4f(red, green, blue, 1.0f);
        drawOutlinedBoundingBox(axisAlignedBB);
        GL11.glColor4f(red, green, blue, alpha);
        drawBoundingBox(axisAlignedBB);
        GL11.glDisable(2848);
        GL11.glEnable(3553);
        GL11.glEnable(2929);
        GL11.glDepthMask(true);
        GL11.glDisable(3042);
        GL11.glPopMatrix();
    }

    public static void doGlScissor(float x, float y, float windowWidth2, float windowHeight2) {
        int scaleFactor = 1;
        float k = mc.gameSettings.guiScale;
        if (k == 0) {
            k = 1000;
        }
        while (scaleFactor < k && mc.displayWidth / (scaleFactor + 1) >= 320
                && mc.displayHeight / (scaleFactor + 1) >= 240) {
            ++scaleFactor;
        }
        GL11.glScissor((int) (x * scaleFactor), (int) (mc.displayHeight - (y + windowHeight2) * scaleFactor),
                (int) (windowWidth2 * scaleFactor), (int) (windowHeight2 * scaleFactor));
    }


}