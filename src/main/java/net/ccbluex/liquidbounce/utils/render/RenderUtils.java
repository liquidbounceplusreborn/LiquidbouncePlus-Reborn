/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 *
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.utils.render;

import com.mojang.authlib.GameProfile;
import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.features.module.modules.render.TargetMark;
import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.ccbluex.liquidbounce.utils.MinecraftInstance;
import net.ccbluex.liquidbounce.utils.block.BlockUtils;
import net.ccbluex.liquidbounce.utils.render.shader.Shader;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.util.*;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

import javax.vecmath.Vector3d;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static java.lang.Math.*;
import static net.ccbluex.liquidbounce.utils.render.GLUtils.glDisable;
import static net.ccbluex.liquidbounce.utils.render.GLUtils.glEnable;
import static net.minecraft.client.renderer.GlStateManager.disableBlend;
import static net.minecraft.client.renderer.GlStateManager.enableTexture2D;
import static net.minecraft.client.renderer.RenderGlobal.drawSelectionBoundingBox;
import static org.lwjgl.opengl.GL11.*;

public final class RenderUtils extends MinecraftInstance {
    private static final Map<Integer, Boolean> glCapMap = new HashMap<>();

    public static int deltaTime;
    public static Minecraft mc = Minecraft.getMinecraft();
    public static float delta;
    private static float
            scissorX,
            scissorY,
            scissorWidth,
            scissorHeight,
            scissorSF;
    private static boolean isScissoring;


    private static final int[] DISPLAY_LISTS_2D = new int[4];

    static {
        for (int i = 0; i < DISPLAY_LISTS_2D.length; i++) {
            DISPLAY_LISTS_2D[i] = glGenLists(1);
        }

        glNewList(DISPLAY_LISTS_2D[0], GL_COMPILE);

        quickDrawRect(-7F, 2F, -4F, 3F);
        quickDrawRect(4F, 2F, 7F, 3F);
        quickDrawRect(-7F, 0.5F, -6F, 3F);
        quickDrawRect(6F, 0.5F, 7F, 3F);

        glEndList();

        glNewList(DISPLAY_LISTS_2D[1], GL_COMPILE);

        quickDrawRect(-7F, 3F, -4F, 3.3F);
        quickDrawRect(4F, 3F, 7F, 3.3F);
        quickDrawRect(-7.3F, 0.5F, -7F, 3.3F);
        quickDrawRect(7F, 0.5F, 7.3F, 3.3F);

        glEndList();

        glNewList(DISPLAY_LISTS_2D[2], GL_COMPILE);

        quickDrawRect(4F, -20F, 7F, -19F);
        quickDrawRect(-7F, -20F, -4F, -19F);
        quickDrawRect(6F, -20F, 7F, -17.5F);
        quickDrawRect(-7F, -20F, -6F, -17.5F);

        glEndList();

        glNewList(DISPLAY_LISTS_2D[3], GL_COMPILE);

        quickDrawRect(7F, -20F, 7.3F, -17.5F);
        quickDrawRect(-7.3F, -20F, -7F, -17.5F);
        quickDrawRect(4F, -20.3F, 7.3F, -20F);
        quickDrawRect(-7.3F, -20.3F, -4F, -20F);

        glEndList();
    }

    public static void drawRectWH(float x, float y, float w, float h, int color) {
        drawRect(x, y, w + x, h + y, color);
    }

    public static void drawFastRoundedRect(float x0, float y0, float x1, float y1, float radius, int color) {
        boolean Semicircle = true;
        float f = 5.0F;
        float f2 = (float) (color >> 24 & 255) / 255.0F;
        float f3 = (float) (color >> 16 & 255) / 255.0F;
        float f4 = (float) (color >> 8 & 255) / 255.0F;
        float f5 = (float) (color & 255) / 255.0F;

        GL11.glDisable(2884);
        GL11.glDisable(3553);
        GL11.glEnable(3042);
        GlStateManager.enableBlend();
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GlStateManager.disableTexture2D();
        GL11.glColor4f(f3, f4, f5, f2);
        GL11.glBegin(5);
        GL11.glVertex2f(x0 + radius, y0);
        GL11.glVertex2f(x0 + radius, y1);
        GL11.glVertex2f(x1 - radius, y0);
        GL11.glVertex2f(x1 - radius, y1);
        GL11.glEnd();
        GL11.glBegin(5);
        GL11.glVertex2f(x0, y0 + radius);
        GL11.glVertex2f(x0 + radius, y0 + radius);
        GL11.glVertex2f(x0, y1 - radius);
        GL11.glVertex2f(x0 + radius, y1 - radius);
        GL11.glEnd();
        GL11.glBegin(5);
        GL11.glVertex2f(x1, y0 + radius);
        GL11.glVertex2f(x1 - radius, y0 + radius);
        GL11.glVertex2f(x1, y1 - radius);
        GL11.glVertex2f(x1 - radius, y1 - radius);
        GL11.glEnd();
        GL11.glBegin(6);
        float f6 = x1 - radius;
        float f7 = y0 + radius;

        GL11.glVertex2f(f6, f7);
        boolean j = false;

        float f11;
        int i;

        for (i = 0; i <= 18; ++i) {
            f11 = (float) i * 5.0F;
            GL11.glVertex2f((float) ((double) f6 + (double) radius * Math.cos(Math.toRadians((double) f11))), (float) ((double) f7 - (double) radius * Math.sin(Math.toRadians((double) f11))));
        }

        GL11.glEnd();
        GL11.glBegin(6);
        f6 = x0 + radius;
        f7 = y0 + radius;
        GL11.glVertex2f(f6, f7);

        for (i = 0; i <= 18; ++i) {
            f11 = (float) i * 5.0F;
            GL11.glVertex2f((float) ((double) f6 - (double) radius * Math.cos(Math.toRadians((double) f11))), (float) ((double) f7 - (double) radius * Math.sin(Math.toRadians((double) f11))));
        }

        GL11.glEnd();
        GL11.glBegin(6);
        f6 = x0 + radius;
        f7 = y1 - radius;
        GL11.glVertex2f(f6, f7);

        for (i = 0; i <= 18; ++i) {
            f11 = (float) i * 5.0F;
            GL11.glVertex2f((float) ((double) f6 - (double) radius * Math.cos(Math.toRadians((double) f11))), (float) ((double) f7 + (double) radius * Math.sin(Math.toRadians((double) f11))));
        }

        GL11.glEnd();
        GL11.glBegin(6);
        f6 = x1 - radius;
        f7 = y1 - radius;
        GL11.glVertex2f(f6, f7);

        for (i = 0; i <= 18; ++i) {
            f11 = (float) i * 5.0F;
            GL11.glVertex2f((float) ((double) f6 + (double) radius * Math.cos(Math.toRadians((double) f11))), (float) ((double) f7 + (double) radius * Math.sin(Math.toRadians((double) f11))));
        }

        GL11.glEnd();
        GL11.glEnable(3553);
        GL11.glEnable(2884);
        GL11.glDisable(3042);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void setupRender(boolean start) {
        if (start) {
            GlStateManager.enableBlend();
            GL11.glEnable(2848);
            GlStateManager.disableDepth();
            GlStateManager.disableTexture2D();
            GlStateManager.blendFunc(770, 771);
            GL11.glHint(3154, 4354);
        } else {
            GlStateManager.disableBlend();
            GlStateManager.enableTexture2D();
            GL11.glDisable(2848);
            GlStateManager.enableDepth();
        }

        GlStateManager.depthMask(!start);
    }

    public static void startDrawing() {
        GL11.glEnable(3042);
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(2848);
        GL11.glDisable(3553);
        GL11.glDisable(2929);
        RenderUtils.mc.entityRenderer.setupCameraTransform(RenderUtils.mc.timer.renderPartialTicks, 0);
    }

    public static void stopDrawing() {
        GL11.glDisable(3042);
        GL11.glEnable(3553);
        GL11.glDisable(2848);
        GL11.glDisable(3042);
        GL11.glEnable(2929);
    }

    public static Vec3 interpolateRender(EntityPlayer player) {
        float part = Minecraft.getMinecraft().timer.renderPartialTicks;
        double interpX = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double) part;
        double interpY = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double) part;
        double interpZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double) part;

        return new Vec3(interpX, interpY, interpZ);
    }

    public static void drawHead(ResourceLocation skin, int x, int y, int width, int height) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderUtils.mc.getTextureManager().bindTexture(skin);
        RenderUtils.drawScaledCustomSizeModalRect(x, y, 8.0F, 8.0F, 8, 8, width, height, 64.0F, 64.0F);
    }

    public static Color fastrainbow() {
        byte rainbowTick = 0;

        return new Color(Color.HSBtoRGB((float) ((double) RenderUtils.mc.thePlayer.ticksExisted / 50.0D + Math.sin((double) rainbowTick / 50.0D * 1.6D)) % 1.0F, 0.5F, 1.0F));
    }

    public static Color fastRainbow2() {
        byte rainbowTick = 0;

        return new Color(Color.HSBtoRGB((float) ((double) System.currentTimeMillis() / 15000.0D + Math.sin((double) rainbowTick / 50.0D * 1.6D)) % 1.0F, 0.5F, 1.0F));
    }

    public static void drawImage(ResourceLocation image, double x, double y, double width, double height) {
        new ScaledResolution(Minecraft.getMinecraft());
        GL11.glDisable(2929);
        GL11.glEnable(3042);
        GL11.glDepthMask(false);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(image);
        drawModalRectWithCustomSizedTexture((float) x, (float) y, 0.0F, 0.0F, (float) width, (float) height, (float) width, (float) height);
        GL11.glDepthMask(true);
        GL11.glDisable(3042);
        GL11.glEnable(2929);
    }

    public static void drawIcon(float x, float y, int sizex, int sizey, ResourceLocation resourceLocation) {
        GL11.glPushMatrix();
        Minecraft.getMinecraft().getTextureManager().bindTexture(resourceLocation);
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(2848);
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 771);
        GL11.glTranslatef(x, y, 0.0F);
        drawScaledRect(0, 0, 0.0F, 0.0F, sizex, sizey, sizex, sizey, (float) sizex, (float) sizey);
        GlStateManager.disableAlpha();
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableLighting();
        GlStateManager.disableRescaleNormal();
        GL11.glDisable(2848);
        GlStateManager.disableBlend();
        GL11.glPopMatrix();
    }

    public static void drawScaledRect(int x, int y, float u, float v, int uWidth, int vHeight, int width, int height, float tileWidth, float tileHeight) {
        Gui.drawScaledCustomSizeModalRect(x, y, u, v, uWidth, vHeight, width, height, tileWidth, tileHeight);
    }

    public static int reAlpha(int color, float alpha) {
        Color c = new Color(color);
        float r = 0.003921569F * (float) c.getRed();
        float g = 0.003921569F * (float) c.getGreen();
        float b = 0.003921569F * (float) c.getBlue();
        return (new Color(r, g, b, alpha)).getRGB();
    }

    public static void drawGradientRect2(double left, double top, double right, double bottom, int startColor, int endColor) {
        float sa = (float) (startColor >> 24 & 255) / 255.0F;
        float sr = (float) (startColor >> 16 & 255) / 255.0F;
        float sg = (float) (startColor >> 8 & 255) / 255.0F;
        float sb = (float) (startColor & 255) / 255.0F;
        float ea = (float) (endColor >> 24 & 255) / 255.0F;
        float er = (float) (endColor >> 16 & 255) / 255.0F;
        float eg = (float) (endColor >> 8 & 255) / 255.0F;
        float eb = (float) (endColor & 255) / 255.0F;

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(7425);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();

        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos(left, bottom, 0.0D).color(sr, sg, sb, sa).endVertex();
        worldrenderer.pos(right, bottom, 0.0D).color(er, eg, eb, ea).endVertex();
        worldrenderer.pos(right, top, 0.0D).color(er, eg, eb, ea).endVertex();
        worldrenderer.pos(left, top, 0.0D).color(sr, sg, sb, sa).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    public static void drawDimRect(double x, double y, double x2, double y2, int col1) {
        drawRect(x, y, x2, y2, col1);
        float f2 = (float) (col1 >> 16 & 255) / 255.0F;
        float f3 = (float) (col1 >> 8 & 255) / 255.0F;
        float f4 = (float) (col1 & 255) / 255.0F;

        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(2848);
        GL11.glPushMatrix();
        GL11.glColor4f(f2, f3, f4, 0.2F);
        GL11.glLineWidth(2.0F);
        GL11.glBegin(1);
        GL11.glVertex2d(x, y);
        GL11.glVertex2d(x, y2);
        GL11.glVertex2d(x2, y2);
        GL11.glVertex2d(x2, y);
        GL11.glVertex2d(x, y);
        GL11.glVertex2d(x2, y);
        GL11.glVertex2d(x, y2);
        GL11.glVertex2d(x2, y2);
        GL11.glEnd();
        GL11.glPopMatrix();
        GL11.glEnable(3553);
        GL11.glDisable(3042);
        GL11.glDisable(2848);
    }

    public static void drawColorRect(double left, double top, double right, double bottom, Color color1, Color color2, Color color3, Color color4) {
        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(2848);
        GL11.glShadeModel(7425);
        GL11.glPushMatrix();
        GL11.glBegin(7);
        glColor(color2);
        GL11.glVertex2d(left, bottom);
        glColor(color3);
        GL11.glVertex2d(right, bottom);
        glColor(color4);
        GL11.glVertex2d(right, top);
        glColor(color1);
        GL11.glVertex2d(left, top);
        GL11.glEnd();
        GL11.glPopMatrix();
        GL11.glEnable(3553);
        GL11.glDisable(3042);
        GL11.glDisable(2848);
        GL11.glShadeModel(7424);
        Gui.drawRect(0, 0, 0, 0, 0);
    }

    public static boolean isHovering(int n, int n2, float n3, float n4, float n5, float n6) {
        boolean b = (float) n > n3 && (float) n < n5 && (float) n2 > n4 && (float) n2 < n6;

        return b;
    }

    public static void drawTexture(int x, int y, int width, int height, int angle, ResourceLocation texture) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        GL11.glColor4f(255.0F, 255.0F, 255.0F, 255.0F);
        if (angle <= 0) {
            Gui.drawModalRectWithCustomSizedTexture(x, y, 0.0F, 0.0F, width, height, (float) width, (float) height);
        } else {
            GlStateManager.pushMatrix();
            int x1 = x + 5;
            byte y1 = -10;

            GlStateManager.translate((float) x1, (float) (y1 + y) + 15.0F, 0.0F);
            GlStateManager.rotate((float) angle, 0.0F, 0.0F, 1.0F);
            GlStateManager.translate((float) (-x1), -((float) (y1 + y) + 15.0F), 0.0F);
            Gui.drawModalRectWithCustomSizedTexture(x, y, 0.0F, 0.0F, 10, 10, 10.0F, 10.0F);
            GlStateManager.popMatrix();
        }

    }

    public static void drawYBrect2(float x, float y, float width, float height, float r, int color) {
        drawSector(x + r + 0.5F, y + r + 0.5F, (double) r, 90, 180, 1.0F, color, true, false);
        drawRect(0.0F, 0.0F, 0.0F, 0.0F, 0);
        drawSector(x + width - r - 0.5F, y + r + 0.5F, (double) r, 90, 90, 1.0F, color, true, false);
        drawRect(0.0F, 0.0F, 0.0F, 0.0F, 0);
        drawSector(x + r + 0.5F, y + height - r - 0.5F, (double) r, 90, 270, 1.0F, color, true, false);
        drawRect(0.0F, 0.0F, 0.0F, 0.0F, 0);
        drawSector(x + width - r - 0.5F, y + height - r - 0.5F, (double) r, 90, 360, 1.0F, color, true, false);
        drawRect(0.0F, 0.0F, 0.0F, 0.0F, 0);
        drawRect(x + r, y, x + r + width - 2.0F * r, y + height, color);
        drawRect(x, y + r, x + r, y + height - r, color);
        drawRect(x + width - r, y + r, x + width, y + height - r, color);
    }

    public static void drawSector(float x, float y, double r, int angle, int startAngle, float lineWidth, int c, boolean isFull, boolean isClose) {
        if (angle > 0) {
            GL11.glScalef(0.5F, 0.5F, 0.5F);
            r *= 2.0D;
            x *= 2.0F;
            y *= 2.0F;
            float f2 = (float) (c >> 24 & 255) / 255.0F;
            float f3 = (float) (c >> 16 & 255) / 255.0F;
            float f4 = (float) (c >> 8 & 255) / 255.0F;
            float f5 = (float) (c & 255) / 255.0F;

            GL11.glEnable(3042);
            GlStateManager.enableAlpha();
            GL11.glLineWidth(lineWidth);
            GL11.glDisable(3553);
            GL11.glEnable(2848);
            GL11.glBlendFunc(770, 771);
            GL11.glColor4f(f3, f4, f5, f2);
            GL11.glBegin(3);

            for (int startX = startAngle; startX <= angle + startAngle; ++startX) {
                double x2 = Math.sin((double) startX * 3.141592653589793D / 180.0D) * r;
                double y2 = Math.cos((double) startX * 3.141592653589793D / 180.0D) * r;

                GL11.glVertex2d((double) x + x2, (double) y + y2);
                if (isFull) {
                    GL11.glVertex2d((double) x, (double) y);
                }
            }

            if (isClose && !isFull && angle < 360) {
                double d0 = Math.sin((double) startAngle * 3.141592653589793D / 180.0D) * r;
                double startY = Math.cos((double) startAngle * 3.141592653589793D / 180.0D) * r;

                GL11.glVertex2d((double) x, (double) y);
                GL11.glVertex2d((double) x + d0, (double) y + startY);
            }

            GL11.glEnd();
            GL11.glDisable(2848);
            GL11.glEnable(3553);
            GL11.glDisable(3042);
            GL11.glScalef(2.0F, 2.0F, 2.0F);
        }
    }

    public static int width() {
        return (new ScaledResolution(Minecraft.getMinecraft())).getScaledWidth();
    }

    public static int height() {
        return (new ScaledResolution(Minecraft.getMinecraft())).getScaledHeight();
    }

    public static void enableGL3D(float lineWidth) {
        GL11.glDisable(3008);
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glDisable(3553);
        GL11.glDisable(2929);
        GL11.glDepthMask(false);
        GL11.glEnable(2884);
        GL11.glEnable(2848);
        GL11.glHint(3154, 4354);
        GL11.glHint(3155, 4354);
        GL11.glLineWidth(lineWidth);
    }

    public static void drawArc(float n, float n2, double n3, int n4, int n5, double n6, int n7) {
        n3 *= 2.0D;
        n *= 2.0F;
        n2 *= 2.0F;
        float n8 = (float) (n4 >> 24 & 255) / 255.0F;
        float n9 = (float) (n4 >> 16 & 255) / 255.0F;
        float n10 = (float) (n4 >> 8 & 255) / 255.0F;
        float n11 = (float) (n4 & 255) / 255.0F;

        GL11.glDisable(2929);
        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glBlendFunc(770, 771);
        GL11.glDepthMask(true);
        GL11.glEnable(2848);
        GL11.glHint(3154, 4354);
        GL11.glHint(3155, 4354);
        GL11.glScalef(0.5F, 0.5F, 0.5F);
        GL11.glLineWidth((float) n7);
        GL11.glEnable(2848);
        GL11.glColor4f(n9, n10, n11, n8);
        GL11.glBegin(3);

        for (int n12 = n5; (double) n12 <= n6; ++n12) {
            GL11.glVertex2d((double) n + Math.sin((double) n12 * 3.141592653589793D / 180.0D) * n3, (double) n2 + Math.cos((double) n12 * 3.141592653589793D / 180.0D) * n3);
        }

        GL11.glEnd();
        GL11.glDisable(2848);
        GL11.glScalef(2.0F, 2.0F, 2.0F);
        GL11.glEnable(3553);
        GL11.glDisable(3042);
        GL11.glEnable(2929);
        GL11.glDisable(2848);
        GL11.glHint(3154, 4352);
        GL11.glHint(3155, 4352);
    }

    public static int rainbow(int delay) {
        double rainbow = Math.ceil((double) (System.currentTimeMillis() + (long) delay) / 10.0D);

        return Color.getHSBColor((float) ((rainbow %= 360.0D) / 360.0D), 0.5F, 1.0F).getRGB();
    }

    public static int rainbow(int delay, float slowspeed) {
        double rainbow = Math.ceil((double) (System.currentTimeMillis() + (long) delay) / (double) slowspeed);

        return Color.getHSBColor((float) ((rainbow %= 360.0D) / 360.0D), 0.5F, 1.0F).getRGB();
    }

    public static void disableGL3D() {
        GL11.glEnable(3553);
        GL11.glEnable(2929);
        GL11.glDisable(3042);
        GL11.glEnable(3008);
        GL11.glDepthMask(true);
        GL11.glCullFace(1029);
        GL11.glDisable(2848);
        GL11.glHint(3154, 4352);
        GL11.glHint(3155, 4352);
    }

    public static void drawBorderedRect(float x, float y, float x1, float y1, float width, int borderColor) {
        RenderUtils.R2DUtils.enableGL2D();
        glColor(borderColor);
        RenderUtils.R2DUtils.drawRect(x + width, y, x1 - width, y + width);
        RenderUtils.R2DUtils.drawRect(x, y, x + width, y1);
        RenderUtils.R2DUtils.drawRect(x1 - width, y, x1, y1);
        RenderUtils.R2DUtils.drawRect(x + width, y1 - width, x1 - width, y1);
        RenderUtils.R2DUtils.disableGL2D();
    }

    public static void drawCircle(double x, double y, double radius, int c) {
        float alpha = (float) (c >> 24 & 255) / 255.0F;
        float red = (float) (c >> 16 & 255) / 255.0F;
        float green = (float) (c >> 8 & 255) / 255.0F;
        float blue = (float) (c & 255) / 255.0F;
        boolean blend = GL11.glIsEnabled(3042);
        boolean line = GL11.glIsEnabled(2848);
        boolean texture = GL11.glIsEnabled(3553);

        if (!blend) {
            GL11.glEnable(3042);
        }

        if (!line) {
            GL11.glEnable(2848);
        }

        if (texture) {
            GL11.glDisable(3553);
        }

        GL11.glBlendFunc(770, 771);
        GL11.glColor4f(red, green, blue, alpha);
        GL11.glBegin(9);

        for (int i = 0; i <= 360; ++i) {
            GL11.glVertex2d(x + Math.sin((double) i * 3.141526D / 180.0D) * radius, y + Math.cos((double) i * 3.141526D / 180.0D) * radius);
        }

        GL11.glEnd();
        if (texture) {
            GL11.glEnable(3553);
        }

        if (!line) {
            GL11.glDisable(2848);
        }

        if (!blend) {
            GL11.glDisable(3042);
        }

    }

    public static void drawCircle2(double x, double y, double radius, int c) {
        float f2 = (float) (c >> 24 & 255) / 255.0F;
        float f22 = (float) (c >> 16 & 255) / 255.0F;
        float f3 = (float) (c >> 8 & 255) / 255.0F;
        float f4 = (float) (c & 255) / 255.0F;

        GlStateManager.alphaFunc(516, 0.001F);
        GlStateManager.color(f22, f3, f4, f2);
        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        Tessellator tes = Tessellator.getInstance();

        for (double i = 0.0D; i < 360.0D; ++i) {
            double f5 = Math.sin(i * 3.141592653589793D / 180.0D) * radius;
            double f6 = Math.cos(i * 3.141592653589793D / 180.0D) * radius;

            GL11.glVertex2d((double) f3 + x, (double) f4 + y);
        }

        GlStateManager.disableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.enableTexture2D();
        GlStateManager.alphaFunc(516, 0.1F);
    }

    public static void drawFullCircle(int cx, int cy, double r, int segments, float lineWidth, int part, int c) {
        GL11.glScalef(0.5F, 0.5F, 0.5F);
        r *= 2.0D;
        cx *= 2;
        cy *= 2;
        float f2 = (float) (c >> 24 & 255) / 255.0F;
        float f22 = (float) (c >> 16 & 255) / 255.0F;
        float f3 = (float) (c >> 8 & 255) / 255.0F;
        float f4 = (float) (c & 255) / 255.0F;

        GL11.glEnable(3042);
        GL11.glLineWidth(lineWidth);
        GL11.glDisable(3553);
        GL11.glEnable(2848);
        GL11.glBlendFunc(770, 771);
        GL11.glColor4f(f22, f3, f4, f2);
        GL11.glBegin(3);

        for (int i = segments - part; i <= segments; ++i) {
            double x = Math.sin((double) i * 3.141592653589793D / 180.0D) * r;
            double y = Math.cos((double) i * 3.141592653589793D / 180.0D) * r;

            GL11.glVertex2d((double) cx + x, (double) cy + y);
        }

        GL11.glEnd();
        GL11.glDisable(2848);
        GL11.glEnable(3553);
        GL11.glDisable(3042);
        GL11.glScalef(2.0F, 2.0F, 2.0F);
    }

    public static Color rainbowEffect(int delay) {
        float hue = (float) (System.nanoTime() + (long) delay) / 2.0E10F % 1.0F;
        Color color = new Color((int) Long.parseLong(Integer.toHexString(Color.HSBtoRGB(hue, 1.0F, 1.0F)), 16));

        return new Color((float) color.getRed() / 255.0F, (float) color.getGreen() / 255.0F, (float) color.getBlue() / 255.0F, (float) color.getAlpha() / 255.0F);
    }

    public static void drawFullscreenImage(ResourceLocation image) {
        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());

        GL11.glDisable(2929);
        GL11.glDepthMask(false);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glDisable(3008);
        Minecraft.getMinecraft().getTextureManager().bindTexture(image);
        Gui.drawModalRectWithCustomSizedTexture(0, 0, 0.0F, 0.0F, scaledResolution.getScaledWidth(), scaledResolution.getScaledHeight(), (float) scaledResolution.getScaledWidth(), (float) scaledResolution.getScaledHeight());
        GL11.glDepthMask(true);
        GL11.glEnable(2929);
        GL11.glEnable(3008);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void drawPlayerHead(ResourceLocation skin, int x, int y, int width, int height) {
        GL11.glColor4f(1F, 1F, 1F, 1F);
        mc.getTextureManager().bindTexture(skin);
        Gui.drawScaledCustomSizeModalRect(
                x, y, 8F, 8F, 8, 8, width, height,
                64F, 64F
        );
    }

    public static void drawHead(ResourceLocation skin, int width, int height) {
        GL11.glColor4f(1F, 1F, 1F, 1F);
        mc.getTextureManager().bindTexture(skin);
        Gui.drawScaledCustomSizeModalRect(
                2, 2, 8F, 8F, 8, 8, width, height,
                64F, 64F
        );
    }

    public static void drawPlayerHead(String playerName, int x, int y, int width, int height2) {
        Minecraft.getMinecraft();
        Iterator iterator = RenderUtils.mc.theWorld.getLoadedEntityList().iterator();

        while (iterator.hasNext()) {
            Entity player = (Entity) iterator.next();
            EntityPlayer ply;

            if (player instanceof EntityPlayer && playerName.equalsIgnoreCase((ply = (EntityPlayer) player).getName())) {
                GameProfile profile = new GameProfile(ply.getUniqueID(), ply.getName());
                NetworkPlayerInfo networkplayerinfo1 = new NetworkPlayerInfo(profile);

                new ScaledResolution(Minecraft.getMinecraft());
                GL11.glDisable(2929);
                GL11.glEnable(3042);
                GL11.glDepthMask(false);
                OpenGlHelper.glBlendFunc(770, 771, 1, 0);
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                Minecraft.getMinecraft().getTextureManager().bindTexture(networkplayerinfo1.getLocationSkin());
                Gui.drawModalRectWithCustomSizedTexture(x, y, 0.0F, 0.0F, width, height2, (float) width, (float) height2);
                GL11.glDepthMask(true);
                GL11.glDisable(3042);
                GL11.glEnable(2929);
            }
        }

    }

    public static double getAnimationState(double animation, double finalState, double speed) {
        float add = (float) (0.01D * speed);

        animation = animation < finalState ? (animation + (double) add < finalState ? animation + (double) add : finalState) : (animation - (double) add > finalState ? animation - (double) add : finalState);
        return animation;
    }

    public static String getShaderCode(InputStreamReader file) {
        String shaderSource = "";

        try {
            String e;
            BufferedReader reader;

            for (reader = new BufferedReader(file); (e = reader.readLine()) != null; shaderSource = shaderSource + e + "\n") {
                ;
            }

            reader.close();
        } catch (IOException ioexception) {
            ioexception.printStackTrace();
            System.exit(-1);
        }

        return shaderSource.toString();
    }

    public static void drawOutlinedRect(int x, int y, int width, int height2, int lineSize, Color lineColor, Color backgroundColor) {
        drawRect((float) x, (float) y, (float) width, (float) height2, backgroundColor.getRGB());
        drawRect((float) x, (float) y, (float) width, (float) (y + lineSize), lineColor.getRGB());
        drawRect((float) x, (float) (height2 - lineSize), (float) width, (float) height2, lineColor.getRGB());
        drawRect((float) x, (float) (y + lineSize), (float) (x + lineSize), (float) (height2 - lineSize), lineColor.getRGB());
        drawRect((float) (width - lineSize), (float) (y + lineSize), (float) width, (float) (height2 - lineSize), lineColor.getRGB());
    }

    public static void drawImage(ResourceLocation image, int x, int y, int width, int height2, Color color) {
        new ScaledResolution(Minecraft.getMinecraft());
        GL11.glDisable(2929);
        GL11.glEnable(3042);
        GL11.glDepthMask(false);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GL11.glColor4f((float) color.getRed() / 255.0F, (float) color.getBlue() / 255.0F, (float) color.getRed() / 255.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(image);
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0.0F, 0.0F, width, height2, (float) width, (float) height2);
        GL11.glDepthMask(true);
        GL11.glDisable(3042);
        GL11.glEnable(2929);
    }

    public static void doGlScissor(int x, int y, int width, int height2) {
        Minecraft mc = Minecraft.getMinecraft();
        int scaleFactor = 1;
        int k = mc.gameSettings.guiScale;

        if (k == 0) {
            k = 1000;
        }

        while (scaleFactor < k && mc.displayWidth / (scaleFactor + 1) >= 320 && mc.displayHeight / (scaleFactor + 1) >= 240) {
            ++scaleFactor;
        }

        GL11.glScissor(x * scaleFactor, mc.displayHeight - (y + height2) * scaleFactor, width * scaleFactor, height2 * scaleFactor);
    }

    public static void drawblock(double a, double a2, double a3, int a4, int a5, float a6) {
        float a7 = (float) (a4 >> 24 & 255) / 255.0F;
        float a8 = (float) (a4 >> 16 & 255) / 255.0F;
        float a9 = (float) (a4 >> 8 & 255) / 255.0F;
        float a10 = (float) (a4 & 255) / 255.0F;
        float a11 = (float) (a5 >> 24 & 255) / 255.0F;
        float a12 = (float) (a5 >> 16 & 255) / 255.0F;
        float a13 = (float) (a5 >> 8 & 255) / 255.0F;
        float a14 = (float) (a5 & 255) / 255.0F;

        GL11.glPushMatrix();
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glDisable(3553);
        GL11.glEnable(2848);
        GL11.glDisable(2929);
        GL11.glDepthMask(false);
        GL11.glColor4f(a8, a9, a10, a7);
        drawOutlinedBoundingBox(new AxisAlignedBB(a, a2, a3, a + 1.0D, a2 + 1.0D, a3 + 1.0D));
        GL11.glLineWidth(a6);
        GL11.glColor4f(a12, a13, a14, a11);
        drawOutlinedBoundingBox(new AxisAlignedBB(a, a2, a3, a + 1.0D, a2 + 1.0D, a3 + 1.0D));
        GL11.glDisable(2848);
        GL11.glEnable(3553);
        GL11.glEnable(2929);
        GL11.glDepthMask(true);
        GL11.glDisable(3042);
        GL11.glPopMatrix();
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

    public static int createShader(String shaderCode, int shaderType) throws Exception {
        byte shader = 0;

        int shader1;

        try {
            shader1 = ARBShaderObjects.glCreateShaderObjectARB(shaderType);
            if (shader1 == 0) {
                return 0;
            }
        } catch (Exception exception) {
            ARBShaderObjects.glDeleteObjectARB(shader);
            throw exception;
        }

        ARBShaderObjects.glShaderSourceARB(shader1, shaderCode);
        ARBShaderObjects.glCompileShaderARB(shader1);
        if (ARBShaderObjects.glGetObjectParameteriARB(shader1, '1') == 0) {
            throw new RuntimeException("Error creating shader:");
        } else {
            return shader1;
        }
    }

    public static void drawBorderRect(double x, double y, double x1, double y1, int color, double lwidth) {
        drawHLine(x, y, x1, y, (float) lwidth, color);
        drawHLine(x1, y, x1, y1, (float) lwidth, color);
        drawHLine(x, y1, x1, y1, (float) lwidth, color);
        drawHLine(x, y1, x, y, (float) lwidth, color);
    }

    public static void drawHLine(double x, double y, double x1, double y1, float width, int color) {
        float f = (float) (color >> 24 & 255) / 255.0F;
        float f1 = (float) (color >> 16 & 255) / 255.0F;
        float f2 = (float) (color >> 8 & 255) / 255.0F;
        float f3 = (float) (color & 255) / 255.0F;

        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(f1, f2, f3, f);
        GL11.glPushMatrix();
        GL11.glLineWidth(width);
        GL11.glBegin(3);
        GL11.glVertex2d(x, y);
        GL11.glVertex2d(x1, y1);
        GL11.glEnd();
        GL11.glLineWidth(1.0F);
        GL11.glPopMatrix();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public void drawCircle(int x, int y, float radius, int color) {
        float alpha = (float) (color >> 24 & 255) / 255.0F;
        float red = (float) (color >> 16 & 255) / 255.0F;
        float green = (float) (color >> 8 & 255) / 255.0F;
        float blue = (float) (color & 255) / 255.0F;
        boolean blend = GL11.glIsEnabled(3042);
        boolean line = GL11.glIsEnabled(2848);
        boolean texture = GL11.glIsEnabled(3553);

        if (!blend) {
            GL11.glEnable(3042);
        }

        if (!line) {
            GL11.glEnable(2848);
        }

        if (texture) {
            GL11.glDisable(3553);
        }

        GL11.glBlendFunc(770, 771);
        GL11.glColor4f(red, green, blue, alpha);
        GL11.glBegin(9);

        for (int i = 0; i <= 360; ++i) {
            GL11.glVertex2d((double) x + Math.sin((double) i * 3.141526D / 180.0D) * (double) radius, (double) y + Math.cos((double) i * 3.141526D / 180.0D) * (double) radius);
        }

        GL11.glEnd();
        if (texture) {
            GL11.glEnable(3553);
        }

        if (!line) {
            GL11.glDisable(2848);
        }

        if (!blend) {
            GL11.glDisable(3042);
        }

    }

    public static void renderOne(float width) {
        checkSetupFBO();
        GL11.glPushAttrib(1048575);
        GL11.glDisable(3008);
        GL11.glDisable(3553);
        GL11.glDisable(2896);
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glLineWidth(width);
        GL11.glEnable(2848);
        GL11.glEnable(2960);
        GL11.glClear(1024);
        GL11.glClearStencil(15);
        GL11.glStencilFunc(512, 1, 15);
        GL11.glStencilOp(7681, 7681, 7681);
        GL11.glPolygonMode(1032, 6913);
    }

    public static void checkSetupFBO() {
        Framebuffer fbo = Minecraft.getMinecraft().getFramebuffer();

        if (fbo != null && fbo.depthBuffer > -1) {
            setupFBO(fbo);
            fbo.depthBuffer = -1;
        }

    }

    public static void setupFBO(Framebuffer fbo) {
        EXTFramebufferObject.glDeleteRenderbuffersEXT(fbo.depthBuffer);
        int stencil_depth_buffer_ID = EXTFramebufferObject.glGenRenderbuffersEXT();

        EXTFramebufferObject.glBindRenderbufferEXT('1', stencil_depth_buffer_ID);
        EXTFramebufferObject.glRenderbufferStorageEXT('1', '1', Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);
        EXTFramebufferObject.glFramebufferRenderbufferEXT('1', '1', '1', stencil_depth_buffer_ID);
        EXTFramebufferObject.glFramebufferRenderbufferEXT('1', '1', '1', stencil_depth_buffer_ID);
    }

    public static void renderTwo() {
        GL11.glStencilFunc(512, 0, 15);
        GL11.glStencilOp(7681, 7681, 7681);
        GL11.glPolygonMode(1032, 6914);
    }

    public static void renderThree() {
        GL11.glStencilFunc(514, 1, 15);
        GL11.glStencilOp(7680, 7680, 7680);
        GL11.glPolygonMode(1032, 6913);
    }

    public static void renderFour() {
        setColor(new Color(255, 255, 255));
        GL11.glDepthMask(false);
        GL11.glDisable(2929);
        GL11.glEnable(10754);
        GL11.glPolygonOffset(1.0F, -2000000.0F);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
    }

    public static void renderFive() {
        GL11.glPolygonOffset(1.0F, 2000000.0F);
        GL11.glDisable(10754);
        GL11.glEnable(2929);
        GL11.glDepthMask(true);
        GL11.glDisable(2960);
        GL11.glDisable(2848);
        GL11.glHint(3154, 4352);
        GL11.glEnable(3042);
        GL11.glEnable(2896);
        GL11.glEnable(3553);
        GL11.glEnable(3008);
        GL11.glPopAttrib();
    }

    public static void drawBox(AxisAlignedBB box) {
        if (box != null) {
            GL11.glBegin(7);
            GL11.glVertex3d(box.minX, box.minY, box.maxZ);
            GL11.glVertex3d(box.maxX, box.minY, box.maxZ);
            GL11.glVertex3d(box.maxX, box.maxY, box.maxZ);
            GL11.glVertex3d(box.minX, box.maxY, box.maxZ);
            GL11.glEnd();
            GL11.glBegin(7);
            GL11.glVertex3d(box.maxX, box.minY, box.maxZ);
            GL11.glVertex3d(box.minX, box.minY, box.maxZ);
            GL11.glVertex3d(box.minX, box.maxY, box.maxZ);
            GL11.glVertex3d(box.maxX, box.maxY, box.maxZ);
            GL11.glEnd();
            GL11.glBegin(7);
            GL11.glVertex3d(box.minX, box.minY, box.minZ);
            GL11.glVertex3d(box.minX, box.minY, box.maxZ);
            GL11.glVertex3d(box.minX, box.maxY, box.maxZ);
            GL11.glVertex3d(box.minX, box.maxY, box.minZ);
            GL11.glEnd();
            GL11.glBegin(7);
            GL11.glVertex3d(box.minX, box.minY, box.maxZ);
            GL11.glVertex3d(box.minX, box.minY, box.minZ);
            GL11.glVertex3d(box.minX, box.maxY, box.minZ);
            GL11.glVertex3d(box.minX, box.maxY, box.maxZ);
            GL11.glEnd();
            GL11.glBegin(7);
            GL11.glVertex3d(box.maxX, box.minY, box.maxZ);
            GL11.glVertex3d(box.maxX, box.minY, box.minZ);
            GL11.glVertex3d(box.maxX, box.maxY, box.minZ);
            GL11.glVertex3d(box.maxX, box.maxY, box.maxZ);
            GL11.glEnd();
            GL11.glBegin(7);
            GL11.glVertex3d(box.maxX, box.minY, box.minZ);
            GL11.glVertex3d(box.maxX, box.minY, box.maxZ);
            GL11.glVertex3d(box.maxX, box.maxY, box.maxZ);
            GL11.glVertex3d(box.maxX, box.maxY, box.minZ);
            GL11.glEnd();
            GL11.glBegin(7);
            GL11.glVertex3d(box.minX, box.minY, box.minZ);
            GL11.glVertex3d(box.maxX, box.minY, box.minZ);
            GL11.glVertex3d(box.maxX, box.maxY, box.minZ);
            GL11.glVertex3d(box.minX, box.maxY, box.minZ);
            GL11.glEnd();
            GL11.glBegin(7);
            GL11.glVertex3d(box.maxX, box.minY, box.minZ);
            GL11.glVertex3d(box.minX, box.minY, box.minZ);
            GL11.glVertex3d(box.minX, box.maxY, box.minZ);
            GL11.glVertex3d(box.maxX, box.maxY, box.minZ);
            GL11.glEnd();
            GL11.glBegin(7);
            GL11.glVertex3d(box.minX, box.maxY, box.minZ);
            GL11.glVertex3d(box.maxX, box.maxY, box.minZ);
            GL11.glVertex3d(box.maxX, box.maxY, box.maxZ);
            GL11.glVertex3d(box.minX, box.maxY, box.maxZ);
            GL11.glEnd();
            GL11.glBegin(7);
            GL11.glVertex3d(box.maxX, box.maxY, box.minZ);
            GL11.glVertex3d(box.minX, box.maxY, box.minZ);
            GL11.glVertex3d(box.minX, box.maxY, box.maxZ);
            GL11.glVertex3d(box.maxX, box.maxY, box.maxZ);
            GL11.glEnd();
            GL11.glBegin(7);
            GL11.glVertex3d(box.minX, box.minY, box.minZ);
            GL11.glVertex3d(box.maxX, box.minY, box.minZ);
            GL11.glVertex3d(box.maxX, box.minY, box.maxZ);
            GL11.glVertex3d(box.minX, box.minY, box.maxZ);
            GL11.glEnd();
            GL11.glBegin(7);
            GL11.glVertex3d(box.maxX, box.minY, box.minZ);
            GL11.glVertex3d(box.minX, box.minY, box.minZ);
            GL11.glVertex3d(box.minX, box.minY, box.maxZ);
            GL11.glVertex3d(box.maxX, box.minY, box.maxZ);
            GL11.glEnd();
        }
    }

    public static void drawCompleteBox(AxisAlignedBB axisalignedbb, float width, int insideColor, int borderColor) {
        GL11.glLineWidth(width);
        GL11.glEnable(2848);
        GL11.glEnable(2881);
        GL11.glHint(3154, 4354);
        GL11.glHint(3155, 4354);
        glColor(insideColor);
        drawBox(axisalignedbb);
        glColor(borderColor);
        drawOutlinedBox(axisalignedbb);
        drawCrosses(axisalignedbb);
        GL11.glDisable(2848);
        GL11.glDisable(2881);
    }

    public static void drawCrosses(AxisAlignedBB axisalignedbb, float width, int color) {
        GL11.glLineWidth(width);
        GL11.glEnable(2848);
        GL11.glEnable(2881);
        GL11.glHint(3154, 4354);
        GL11.glHint(3155, 4354);
        glColor(color);
        drawCrosses(axisalignedbb);
        GL11.glDisable(2848);
        GL11.glDisable(2881);
    }

    public static void drawOutlineBox(AxisAlignedBB axisalignedbb, float width, int color) {
        GL11.glLineWidth(width);
        GL11.glEnable(2848);
        GL11.glEnable(2881);
        GL11.glHint(3154, 4354);
        GL11.glHint(3155, 4354);
        glColor(color);
        drawOutlinedBox(axisalignedbb);
        GL11.glDisable(2848);
        GL11.glDisable(2881);
    }

    public static void drawOutlinedBox(AxisAlignedBB box) {
        if (box != null) {
            GL11.glBegin(3);
            GL11.glVertex3d(box.minX, box.minY, box.minZ);
            GL11.glVertex3d(box.maxX, box.minY, box.minZ);
            GL11.glVertex3d(box.maxX, box.minY, box.maxZ);
            GL11.glVertex3d(box.minX, box.minY, box.maxZ);
            GL11.glVertex3d(box.minX, box.minY, box.minZ);
            GL11.glEnd();
            GL11.glBegin(3);
            GL11.glVertex3d(box.minX, box.maxY, box.minZ);
            GL11.glVertex3d(box.maxX, box.maxY, box.minZ);
            GL11.glVertex3d(box.maxX, box.maxY, box.maxZ);
            GL11.glVertex3d(box.minX, box.maxY, box.maxZ);
            GL11.glVertex3d(box.minX, box.maxY, box.minZ);
            GL11.glEnd();
            GL11.glBegin(1);
            GL11.glVertex3d(box.minX, box.minY, box.minZ);
            GL11.glVertex3d(box.minX, box.maxY, box.minZ);
            GL11.glVertex3d(box.maxX, box.minY, box.minZ);
            GL11.glVertex3d(box.maxX, box.maxY, box.minZ);
            GL11.glVertex3d(box.maxX, box.minY, box.maxZ);
            GL11.glVertex3d(box.maxX, box.maxY, box.maxZ);
            GL11.glVertex3d(box.minX, box.minY, box.maxZ);
            GL11.glVertex3d(box.minX, box.maxY, box.maxZ);
            GL11.glEnd();
        }
    }

    public static void drawCrosses(AxisAlignedBB box) {
        if (box != null) {
            GL11.glBegin(1);
            GL11.glVertex3d(box.maxX, box.maxY, box.maxZ);
            GL11.glVertex3d(box.maxX, box.minY, box.minZ);
            GL11.glVertex3d(box.maxX, box.maxY, box.minZ);
            GL11.glVertex3d(box.minX, box.maxY, box.maxZ);
            GL11.glVertex3d(box.minX, box.maxY, box.minZ);
            GL11.glVertex3d(box.maxX, box.minY, box.minZ);
            GL11.glVertex3d(box.minX, box.minY, box.maxZ);
            GL11.glVertex3d(box.maxX, box.maxY, box.maxZ);
            GL11.glVertex3d(box.minX, box.minY, box.maxZ);
            GL11.glVertex3d(box.minX, box.maxY, box.minZ);
            GL11.glVertex3d(box.minX, box.minY, box.minZ);
            GL11.glVertex3d(box.maxX, box.minY, box.maxZ);
            GL11.glEnd();
        }
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

    public static void drawOutlinedBlockESP(double x, double y, double z, float red, float green, float blue, float alpha, float lineWidth) {
        GL11.glPushMatrix();
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glDisable(3553);
        GL11.glEnable(2848);
        GL11.glDisable(2929);
        GL11.glDepthMask(false);
        GL11.glLineWidth(lineWidth);
        GL11.glColor4f(red, green, blue, alpha);
        drawOutlinedBoundingBox(new AxisAlignedBB(x, y, z, x + 1.0D, y + 1.0D, z + 1.0D));
        GL11.glDisable(2848);
        GL11.glEnable(3553);
        GL11.glEnable(2929);
        GL11.glDepthMask(true);
        GL11.glDisable(3042);
        GL11.glPopMatrix();
    }

    public static void drawBlockESP(double x, double y, double z, float red, float green, float blue, float alpha, float lineRed, float lineGreen, float lineBlue, float lineAlpha, float lineWidth) {
        GL11.glPushMatrix();
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glDisable(2896);
        GL11.glDisable(3553);
        GL11.glEnable(2848);
        GL11.glDisable(2929);
        GL11.glDepthMask(false);
        GL11.glColor4f(red, green, blue, alpha);
        drawBoundingBox(new AxisAlignedBB(x, y, z, x + 1.0D, y + 1.0D, z + 1.0D));
        GL11.glLineWidth(lineWidth);
        GL11.glColor4f(lineRed, lineGreen, lineBlue, lineAlpha);
        drawOutlinedBoundingBox(new AxisAlignedBB(x, y, z, x + 1.0D, y + 1.0D, z + 1.0D));
        GL11.glDisable(2848);
        GL11.glEnable(3553);
        GL11.glEnable(2896);
        GL11.glEnable(2929);
        GL11.glDepthMask(true);
        GL11.glDisable(3042);
        GL11.glPopMatrix();
    }

    public static void drawSolidBlockESP(double x, double y, double z, float red, float green, float blue, float alpha) {
        GL11.glPushMatrix();
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glDisable(3553);
        GL11.glEnable(2848);
        GL11.glDisable(2929);
        GL11.glDepthMask(false);
        GL11.glColor4f(red, green, blue, alpha);
        drawBoundingBox(new AxisAlignedBB(x, y, z, x + 1.0D, y + 1.0D, z + 1.0D));
        GL11.glColor3f(1.0F, 1.0F, 1.0F);
        GL11.glDisable(2848);
        GL11.glEnable(3553);
        GL11.glEnable(2929);
        GL11.glDepthMask(true);
        GL11.glDisable(3042);
        GL11.glPopMatrix();
    }

    public static void drawOutlinedEntityESP(double x, double y, double z, double width, double height2, float red, float green, float blue, float alpha) {
        GL11.glPushMatrix();
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glDisable(3553);
        GL11.glEnable(2848);
        GL11.glDisable(2929);
        GL11.glDepthMask(false);
        GL11.glColor4f(red, green, blue, alpha);
        drawOutlinedBoundingBox(new AxisAlignedBB(x - width, y, z - width, x + width, y + height2, z + width));
        GL11.glDisable(2848);
        GL11.glEnable(3553);
        GL11.glEnable(2929);
        GL11.glDepthMask(true);
        GL11.glDisable(3042);
        GL11.glPopMatrix();
    }

    public static void drawSolidEntityESP(double x, double y, double z, double width, double height2, float red, float green, float blue, float alpha) {
        GL11.glPushMatrix();
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glDisable(3553);
        GL11.glEnable(2848);
        GL11.glDisable(2929);
        GL11.glDepthMask(false);
        GL11.glColor4f(red, green, blue, alpha);
        drawBoundingBox(new AxisAlignedBB(x - width, y, z - width, x + width, y + height2, z + width));
        GL11.glDisable(2848);
        GL11.glEnable(3553);
        GL11.glEnable(2929);
        GL11.glDepthMask(true);
        GL11.glDisable(3042);
        GL11.glPopMatrix();
    }

    public static void drawEntityESP(double x, double y, double z, double width, double height2, float red, float green, float blue, float alpha, float lineRed, float lineGreen, float lineBlue, float lineAlpha, float lineWdith) {
        GL11.glPushMatrix();
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glDisable(3553);
        GL11.glEnable(2848);
        GL11.glDisable(2929);
        GL11.glDepthMask(false);
        GL11.glColor4f(red, green, blue, alpha);
        drawBoundingBox(new AxisAlignedBB(x - width, y, z - width, x + width, y + height2, z + width));
        GL11.glLineWidth(lineWdith);
        GL11.glColor4f(lineRed, lineGreen, lineBlue, lineAlpha);
        drawOutlinedBoundingBox(new AxisAlignedBB(x - width, y, z - width, x + width, y + height2, z + width));
        GL11.glDisable(2848);
        GL11.glEnable(3553);
        GL11.glEnable(2929);
        GL11.glDepthMask(true);
        GL11.glDisable(3042);
        GL11.glPopMatrix();
    }

    public static void drawEntityESP(double x, double y, double z, double width, double height2, float red, float green, float blue, float alpha) {
        GL11.glPushMatrix();
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glDisable(3553);
        GL11.glEnable(2848);
        GL11.glDisable(2929);
        GL11.glDepthMask(false);
        GL11.glColor4f(red, green, blue, alpha);
        drawBoundingBox(new AxisAlignedBB(x - width, y, z - width, x + width, y + height2, z + width));
        GL11.glDisable(2848);
        GL11.glEnable(3553);
        GL11.glEnable(2929);
        GL11.glDepthMask(true);
        GL11.glDisable(3042);
        GL11.glPopMatrix();
    }

    public static void drawRoundedRect(float x, float y, float x1, float y1, int borderC, int insideC) {
        drawRect(x + 0.5F, y, x1 - 0.5F, y + 0.5F, insideC);
        drawRect(x + 0.5F, y1 - 0.5F, x1 - 0.5F, y1, insideC);
        drawRect(x, y + 0.5F, x1, y1 - 0.5F, insideC);
    }

    public static void circle(float x, float y, float radius, int fill) {
        arc(x, y, 0.0F, 360.0F, radius, fill);
    }

    public static void circle(float x, float y, float radius, Color fill) {
        arc(x, y, 0.0F, 360.0F, radius, fill);
    }

    public static void arc(float x, float y, float start, float end, float radius, int color) {
        arcEllipse(x, y, start, end, radius, radius, color);
    }

    public static void arc(float x, float y, float start, float end, float radius, Color color) {
        arcEllipse(x, y, start, end, radius, radius, color);
    }

    public static void arcEllipse(float x, float y, float start, float end, float w, float h, int color) {
        GlStateManager.color(0.0F, 0.0F, 0.0F);
        GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.0F);
        float temp = 0.0F;

        if (start > end) {
            temp = end;
            end = start;
            start = temp;
        }

        float f = (float) (color >> 24 & 255) / 255.0F;
        float f1 = (float) (color >> 16 & 255) / 255.0F;
        float f2 = (float) (color >> 8 & 255) / 255.0F;
        float f3 = (float) (color & 255) / 255.0F;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();

        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(f1, f2, f3, f);
        float ldy;
        float ldx;
        float i;

        if (f > 0.5F) {
            GL11.glEnable(2848);
            GL11.glLineWidth(2.0F);
            GL11.glBegin(3);

            for (i = end; i >= start; i -= 4.0F) {
                ldx = (float) Math.cos((double) i * 3.141592653589793D / 180.0D) * w * 1.001F;
                ldy = (float) Math.sin((double) i * 3.141592653589793D / 180.0D) * h * 1.001F;
                GL11.glVertex2f(x + ldx, y + ldy);
            }

            GL11.glEnd();
            GL11.glDisable(2848);
        }

        GL11.glBegin(6);

        for (i = end; i >= start; i -= 4.0F) {
            ldx = (float) Math.cos((double) i * 3.141592653589793D / 180.0D) * w;
            ldy = (float) Math.sin((double) i * 3.141592653589793D / 180.0D) * h;
            GL11.glVertex2f(x + ldx, y + ldy);
        }

        GL11.glEnd();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void drawBorderedRect(double x2, double d2, double x22, double e2, float l1, int col1, int col2) {
        drawRect(x2, d2, x22, e2, col2);
        float f2 = (float) (col1 >> 24 & 255) / 255.0F;
        float f22 = (float) (col1 >> 16 & 255) / 255.0F;
        float f3 = (float) (col1 >> 8 & 255) / 255.0F;
        float f4 = (float) (col1 & 255) / 255.0F;

        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(2848);
        GL11.glPushMatrix();
        GL11.glColor4f(f22, f3, f4, f2);
        GL11.glLineWidth(l1);
        GL11.glBegin(1);
        GL11.glVertex2d(x2, d2);
        GL11.glVertex2d(x2, e2);
        GL11.glVertex2d(x22, e2);
        GL11.glVertex2d(x22, d2);
        GL11.glVertex2d(x2, d2);
        GL11.glVertex2d(x22, d2);
        GL11.glVertex2d(x2, e2);
        GL11.glVertex2d(x22, e2);
        GL11.glEnd();
        GL11.glPopMatrix();
        GL11.glEnable(3553);
        GL11.glDisable(3042);
        GL11.glDisable(2848);
    }

    public static Color blend(Color color1, Color color2, double ratio) {
        float r = (float) ratio;
        float ir = 1.0F - r;
        float[] rgb1 = new float[3];
        float[] rgb2 = new float[3];

        color1.getColorComponents(rgb1);
        color2.getColorComponents(rgb2);
        Color color3 = new Color(rgb1[0] * r + rgb2[0] * ir, rgb1[1] * r + rgb2[1] * ir, rgb1[2] * r + rgb2[2] * ir);

        return color3;
    }

    public static void drawEntityOnScreen(int posX, int posY, int scale, float mouseX, float mouseY, EntityLivingBase ent) {
        GlStateManager.enableColorMaterial();
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) posX, (float) posY, 40.0F);
        GlStateManager.scale((float) (-scale), (float) scale, (float) scale);
        GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
        float f = ent.renderYawOffset;
        float f1 = ent.rotationYaw;
        float f2 = ent.rotationPitch;
        float f3 = ent.prevRotationYawHead;
        float f4 = ent.rotationYawHead;

        GlStateManager.rotate(135.0F, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-((float) Math.atan((double) (mouseY / 40.0F))) * 20.0F, 1.0F, 0.0F, 0.0F);
        ent.renderYawOffset = (float) Math.atan((double) (mouseX / 40.0F)) * -14.0F;
        ent.rotationYaw = (float) Math.atan((double) (mouseX / 40.0F)) * -14.0F;
        ent.rotationPitch = -((float) Math.atan((double) (mouseY / 40.0F))) * 15.0F;
        ent.rotationYawHead = ent.rotationYaw;
        ent.prevRotationYawHead = ent.rotationYaw;
        GlStateManager.translate(0.0F, 0.0F, 0.0F);
        RenderManager rendermanager = Minecraft.getMinecraft().getRenderManager();

        rendermanager.setPlayerViewY(180.0F);
        rendermanager.setRenderShadow(false);
        rendermanager.renderEntityWithPosYaw(ent, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F);
        rendermanager.setRenderShadow(true);
        ent.renderYawOffset = f;
        ent.rotationYaw = f1;
        ent.rotationPitch = f2;
        ent.prevRotationYawHead = f3;
        ent.rotationYawHead = f4;
        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }

    public static void drawRoundRect(double d, double e, double g, double h, int color) {
        drawRect(d + 1.0D, e, g - 1.0D, h, color);
        drawRect(d, e + 1.0D, d + 1.0D, h - 1.0D, color);
        drawRect(d + 1.0D, e + 1.0D, d + 0.5D, e + 0.5D, color);
        drawRect(d + 1.0D, e + 1.0D, d + 0.5D, e + 0.5D, color);
        drawRect(g - 1.0D, e + 1.0D, g - 0.5D, e + 0.5D, color);
        drawRect(g - 1.0D, e + 1.0D, g, h - 1.0D, color);
        drawRect(d + 1.0D, h - 1.0D, d + 0.5D, h - 0.5D, color);
        drawRect(g - 1.0D, h - 1.0D, g - 0.5D, h - 0.5D, color);
    }

    public static void pre() {
        GL11.glDisable(2929);
        GL11.glDisable(3553);
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
    }

    public static void post() {
        GL11.glDisable(3042);
        GL11.glEnable(3553);
        GL11.glEnable(2929);
        GL11.glColor3d(1.0D, 1.0D, 1.0D);
    }

    public static class R2DUtils {

        public static void enableGL2D() {
            GL11.glDisable(2929);
            GL11.glEnable(3042);
            GL11.glDisable(3553);
            GL11.glBlendFunc(770, 771);
            GL11.glDepthMask(true);
            GL11.glEnable(2848);
            GL11.glHint(3154, 4354);
            GL11.glHint(3155, 4354);
        }

        public static void disableGL2D() {
            GL11.glEnable(3553);
            GL11.glDisable(3042);
            GL11.glEnable(2929);
            GL11.glDisable(2848);
            GL11.glHint(3154, 4352);
            GL11.glHint(3155, 4352);
        }

        public static void drawRoundedRect(float x, float y, float x1, float y1, int borderC, int insideC) {
            enableGL2D();
            GL11.glScalef(0.5F, 0.5F, 0.5F);
            drawVLine(x *= 2.0F, (y *= 2.0F) + 1.0F, (y1 *= 2.0F) - 2.0F, borderC);
            drawVLine((x1 *= 2.0F) - 1.0F, y + 1.0F, y1 - 2.0F, borderC);
            drawHLine(x + 2.0F, x1 - 3.0F, y, borderC);
            drawHLine(x + 2.0F, x1 - 3.0F, y1 - 1.0F, borderC);
            drawHLine(x + 1.0F, x + 1.0F, y + 1.0F, borderC);
            drawHLine(x1 - 2.0F, x1 - 2.0F, y + 1.0F, borderC);
            drawHLine(x1 - 2.0F, x1 - 2.0F, y1 - 2.0F, borderC);
            drawHLine(x + 1.0F, x + 1.0F, y1 - 2.0F, borderC);
            drawRect(x + 1.0F, y + 1.0F, x1 - 1.0F, y1 - 1.0F, insideC);
            GL11.glScalef(2.0F, 2.0F, 2.0F);
            disableGL2D();
            Gui.drawRect(0, 0, 0, 0, 0);
        }

        public static void drawRect(double x2, double y2, double x1, double y1, int color) {
            enableGL2D();
            glColor(color);
            drawRect(x2, y2, x1, y1);
            disableGL2D();
        }

        private static void drawRect(double x2, double y2, double x1, double y1) {
            GL11.glBegin(7);
            GL11.glVertex2d(x2, y1);
            GL11.glVertex2d(x1, y1);
            GL11.glVertex2d(x1, y2);
            GL11.glVertex2d(x2, y2);
            GL11.glEnd();
        }

        public static void glColor(int hex) {
            float alpha = (float) (hex >> 24 & 255) / 255.0F;
            float red = (float) (hex >> 16 & 255) / 255.0F;
            float green = (float) (hex >> 8 & 255) / 255.0F;
            float blue = (float) (hex & 255) / 255.0F;

            GL11.glColor4f(red, green, blue, alpha);
        }

        public static void drawRect(float x, float y, float x1, float y1, int color) {
            enableGL2D();
            glColor(color);
            drawRect(x, y, x1, y1);
            disableGL2D();
        }

        public static void drawBorderedRect(float x, float y, float x1, float y1, float width, int borderColor) {
            enableGL2D();
            glColor(borderColor);
            drawRect(x + width, y, x1 - width, y + width);
            drawRect(x, y, x + width, y1);
            drawRect(x1 - width, y, x1, y1);
            drawRect(x + width, y1 - width, x1 - width, y1);
            disableGL2D();
        }

        public static void drawBorderedRect(float x, float y, float x1, float y1, int insideC, int borderC) {
            enableGL2D();
            GL11.glScalef(0.5F, 0.5F, 0.5F);
            drawVLine(x *= 2.0F, y *= 2.0F, y1 *= 2.0F, borderC);
            drawVLine((x1 *= 2.0F) - 1.0F, y, y1, borderC);
            drawHLine(x, x1 - 1.0F, y, borderC);
            drawHLine(x, x1 - 2.0F, y1 - 1.0F, borderC);
            drawRect(x + 1.0F, y + 1.0F, x1 - 1.0F, y1 - 1.0F, insideC);
            GL11.glScalef(2.0F, 2.0F, 2.0F);
            disableGL2D();
        }

        public static void drawGradientRect(float x, float y, float x1, float y1, int topColor, int bottomColor) {
            enableGL2D();
            GL11.glShadeModel(7425);
            GL11.glBegin(7);
            glColor(topColor);
            GL11.glVertex2f(x, y1);
            GL11.glVertex2f(x1, y1);
            glColor(bottomColor);
            GL11.glVertex2f(x1, y);
            GL11.glVertex2f(x, y);
            GL11.glEnd();
            GL11.glShadeModel(7424);
            disableGL2D();
        }

        public static void drawHLine(float x, float y, float x1, int y1) {
            if (y < x) {
                float f = x;

                x = y;
                y = f;
            }

            drawRect(x, x1, y + 1.0F, x1 + 1.0F, y1);
        }

        public static void drawVLine(float x, float y, float x1, int y1) {
            if (x1 < y) {
                float f = y;

                y = x1;
                x1 = f;
            }

            drawRect(x, y + 1.0F, x + 1.0F, x1, y1);
        }

        public static void drawHLine(float x, float y, float x1, int y1, int y2) {
            if (y < x) {
                float f = x;

                x = y;
                y = f;
            }

            drawGradientRect(x, x1, y + 1.0F, x1 + 1.0F, y1, y2);
        }

        public static void drawRect(float x, float y, float x1, float y1) {
            GL11.glBegin(7);
            GL11.glVertex2f(x, y1);
            GL11.glVertex2f(x1, y1);
            GL11.glVertex2f(x1, y);
            GL11.glVertex2f(x, y);
            GL11.glEnd();
        }

    }

    public static void disableSmoothLine() {
        GL11.glEnable(3553);
        GL11.glEnable(2929);
        GL11.glDisable(3042);
        GL11.glEnable(3008);
        GL11.glDepthMask(true);
        GL11.glCullFace(1029);
        GL11.glDisable(2848);
        GL11.glHint(3154, 4352);
        GL11.glHint(3155, 4352);
    }

    public static void drawShadow(int x, int y, int width, int height) {
        ScaledResolution sr = new ScaledResolution(mc);
        drawTexturedRect(x - 9, y - 9, 9, 9, "paneltopleft", sr);
        drawTexturedRect(x - 9, y + height, 9, 9, "panelbottomleft", sr);
        drawTexturedRect(x + width, y + height, 9, 9, "panelbottomright", sr);
        drawTexturedRect(x + width, y - 9, 9, 9, "paneltopright", sr);
        drawTexturedRect(x - 9, y, 9, height, "panelleft", sr);
        drawTexturedRect(x + width, y, 9, height, "panelright", sr);
        drawTexturedRect(x, y - 9, width, 9, "paneltop", sr);
        drawTexturedRect(x, y + height, width, 9, "panelbottom", sr);
    }

    public static float[] getScissor() {
        if (isScissoring) {
            return new float[] {scissorX,scissorY,scissorWidth,scissorHeight,scissorSF};
        }
        return new float[] {-1};
    }

    public static void beginCrop(float x, float y, float width, float height) {
        float scaleFactor = getScaleFactor();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor((int) (x * scaleFactor), (int) ((float) Display.getHeight() - y * scaleFactor), (int) (width * scaleFactor), (int) (height * scaleFactor));
        isScissoring = true;
        scissorX = x;
        scissorY = y;
        scissorWidth = width;
        scissorHeight = height;
        scissorSF = scaleFactor;
    }

    public static void beginCropFixed(float x, float y, float width, float height) {
        float scaleFactor = getScaleFactor();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor((int) (x * scaleFactor), (int) ((float) Display.getHeight() - y * scaleFactor), (int) (width * scaleFactor), (int) (height * scaleFactor));
        isScissoring = true;
        scissorX = x;
        scissorY = y;
        scissorWidth = width;
        scissorHeight = height;
        scissorSF = scaleFactor;
    }

    public static void beginCrop(float x, float y, float width, float height, float scaleFactor) {
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor((int) (x * scaleFactor), (int) ((float) Display.getHeight() - y * scaleFactor), (int) (width * scaleFactor), (int) (height * scaleFactor));
        isScissoring = true;
        scissorX = x;
        scissorY = y;
        scissorWidth = width;
        scissorHeight = height;
        scissorSF = scaleFactor;
    }

    public static void endCrop() {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        isScissoring = false;
    }

    public static void doGlScissor(int x, int y, float width, float height, float scale) {
        int scaleFactor = 1;

        while (scaleFactor < scale && mc.displayWidth / (scaleFactor + 1) >= 320 && mc.displayHeight / (scaleFactor + 1) >= 240) {
            ++scaleFactor;
        }

        GL11.glScissor(x * scaleFactor, (int) (mc.displayHeight - (y + height) * scaleFactor), (int) (width * scaleFactor), (int) (height * scaleFactor));
    }

    public static void drawLine3D(double x1, double y1, double z1, double x2, double y2, double z2, int color) {
        drawLine3D(x1, y1, z1, x2, y2, z2, color, true);
    }

    public static void drawLine3D(double x1, double y1, double z1, double x2, double y2, double z2, int color, boolean disableDepth) {
        enableRender3D(disableDepth);
        setColor(color);
        GL11.glBegin(1);
        GL11.glVertex3d(x1, y1, z1);
        GL11.glVertex3d(x2, y2, z2);
        GL11.glEnd();
        disableRender3D(disableDepth);
    }

    public static void drawLine2D(double x1, double y1, double x2, double y2, float width, int color) {
        enableRender2D();
        setColor(color);
        GL11.glLineWidth(width);
        GL11.glBegin(1);
        GL11.glVertex2d(x1, y1);
        GL11.glVertex2d(x2, y2);
        GL11.glEnd();
        disableRender2D();
    }

    public static void drawPoint(int x, int y, float size, int color) {
        enableRender2D();
        setColor(color);
        GL11.glPointSize(size);
        GL11.glEnable(2832);
        GL11.glBegin(0);
        GL11.glVertex2d(x, y);
        GL11.glEnd();
        GL11.glDisable(2832);
        disableRender2D();
    }

    public static float getScaleFactor() {
        ScaledResolution scaledResolution = new ScaledResolution(mc);
        return scaledResolution.getScaleFactor();
    }

    public static void drawTexturedRect(int x, int y, int width, int height, String image, ScaledResolution sr) {
        GL11.glPushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        mc.getTextureManager().bindTexture(new ResourceLocation("liquidbounce/cool/potionrender/" + image + ".png"));
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, width, height, width, height);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GL11.glPopMatrix();
    }

    public static void drawOutlinedBox(AxisAlignedBB boundingBox, int color) {
        drawOutlinedBox(boundingBox, color, true);
    }

    public static void drawOutlinedBox(AxisAlignedBB boundingBox, int color, boolean disableDepth) {
        if (boundingBox != null) {
            enableRender3D(disableDepth);
            setColor(color);
            GL11.glBegin(3);
            GL11.glVertex3d(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
            GL11.glVertex3d(boundingBox.maxX, boundingBox.minY, boundingBox.minZ);
            GL11.glVertex3d(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ);
            GL11.glVertex3d(boundingBox.minX, boundingBox.minY, boundingBox.maxZ);
            GL11.glVertex3d(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
            GL11.glEnd();
            GL11.glBegin(3);
            GL11.glVertex3d(boundingBox.minX, boundingBox.maxY, boundingBox.minZ);
            GL11.glVertex3d(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ);
            GL11.glVertex3d(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
            GL11.glVertex3d(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ);
            GL11.glVertex3d(boundingBox.minX, boundingBox.maxY, boundingBox.minZ);
            GL11.glEnd();
            GL11.glBegin(1);
            GL11.glVertex3d(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
            GL11.glVertex3d(boundingBox.minX, boundingBox.maxY, boundingBox.minZ);
            GL11.glVertex3d(boundingBox.maxX, boundingBox.minY, boundingBox.minZ);
            GL11.glVertex3d(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ);
            GL11.glVertex3d(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ);
            GL11.glVertex3d(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
            GL11.glVertex3d(boundingBox.minX, boundingBox.minY, boundingBox.maxZ);
            GL11.glVertex3d(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ);
            GL11.glEnd();
            disableRender3D(disableDepth);
        }
    }

    public static void drawBox(AxisAlignedBB boundingBox, int color, boolean disableDepth) {
        if (boundingBox != null) {
            enableRender3D(disableDepth);
            setColor(color);
            GL11.glBegin(7);
            GL11.glVertex3d(boundingBox.minX, boundingBox.minY, boundingBox.maxZ);
            GL11.glVertex3d(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ);
            GL11.glVertex3d(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
            GL11.glVertex3d(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ);
            GL11.glEnd();
            GL11.glBegin(7);
            GL11.glVertex3d(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ);
            GL11.glVertex3d(boundingBox.minX, boundingBox.minY, boundingBox.maxZ);
            GL11.glVertex3d(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ);
            GL11.glVertex3d(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
            GL11.glEnd();
            GL11.glBegin(7);
            GL11.glVertex3d(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
            GL11.glVertex3d(boundingBox.minX, boundingBox.minY, boundingBox.maxZ);
            GL11.glVertex3d(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ);
            GL11.glVertex3d(boundingBox.minX, boundingBox.maxY, boundingBox.minZ);
            GL11.glEnd();
            GL11.glBegin(7);
            GL11.glVertex3d(boundingBox.minX, boundingBox.minY, boundingBox.maxZ);
            GL11.glVertex3d(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
            GL11.glVertex3d(boundingBox.minX, boundingBox.maxY, boundingBox.minZ);
            GL11.glVertex3d(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ);
            GL11.glEnd();
            GL11.glBegin(7);
            GL11.glVertex3d(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ);
            GL11.glVertex3d(boundingBox.maxX, boundingBox.minY, boundingBox.minZ);
            GL11.glVertex3d(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ);
            GL11.glVertex3d(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
            GL11.glEnd();
            GL11.glBegin(7);
            GL11.glVertex3d(boundingBox.maxX, boundingBox.minY, boundingBox.minZ);
            GL11.glVertex3d(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ);
            GL11.glVertex3d(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
            GL11.glVertex3d(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ);
            GL11.glEnd();
            GL11.glBegin(7);
            GL11.glVertex3d(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
            GL11.glVertex3d(boundingBox.maxX, boundingBox.minY, boundingBox.minZ);
            GL11.glVertex3d(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ);
            GL11.glVertex3d(boundingBox.minX, boundingBox.maxY, boundingBox.minZ);
            GL11.glEnd();
            GL11.glBegin(7);
            GL11.glVertex3d(boundingBox.maxX, boundingBox.minY, boundingBox.minZ);
            GL11.glVertex3d(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
            GL11.glVertex3d(boundingBox.minX, boundingBox.maxY, boundingBox.minZ);
            GL11.glVertex3d(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ);
            GL11.glEnd();
            GL11.glBegin(7);
            GL11.glVertex3d(boundingBox.minX, boundingBox.maxY, boundingBox.minZ);
            GL11.glVertex3d(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ);
            GL11.glVertex3d(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
            GL11.glVertex3d(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ);
            GL11.glEnd();
            GL11.glBegin(7);
            GL11.glVertex3d(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ);
            GL11.glVertex3d(boundingBox.minX, boundingBox.maxY, boundingBox.minZ);
            GL11.glVertex3d(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ);
            GL11.glVertex3d(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
            GL11.glEnd();
            GL11.glBegin(7);
            GL11.glVertex3d(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
            GL11.glVertex3d(boundingBox.maxX, boundingBox.minY, boundingBox.minZ);
            GL11.glVertex3d(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ);
            GL11.glVertex3d(boundingBox.minX, boundingBox.minY, boundingBox.maxZ);
            GL11.glEnd();
            GL11.glBegin(7);
            GL11.glVertex3d(boundingBox.maxX, boundingBox.minY, boundingBox.minZ);
            GL11.glVertex3d(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
            GL11.glVertex3d(boundingBox.minX, boundingBox.minY, boundingBox.maxZ);
            GL11.glVertex3d(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ);
            GL11.glEnd();
            disableRender3D(disableDepth);
        }
    }

    public static void enableRender3D(boolean disableDepth) {
        if (disableDepth) {
            GL11.glDepthMask(false);
            GL11.glDisable(2929);
        }

        GL11.glDisable(3008);
        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(2848);
        GL11.glHint(3154, 4354);
        GL11.glLineWidth(1.0F);
    }

    public static void disableRender3D(boolean enableDepth) {
        if (enableDepth) {
            GL11.glDepthMask(true);
            GL11.glEnable(2929);
        }

        GL11.glEnable(3553);
        GL11.glDisable(3042);
        GL11.glEnable(3008);
        GL11.glDisable(2848);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void setColor(int colorHex) {
        float alpha = (float) (colorHex >> 24 & 255) / 255.0F;
        float red = (float) (colorHex >> 16 & 255) / 255.0F;
        float green = (float) (colorHex >> 8 & 255) / 255.0F;
        float blue = (float) (colorHex & 255) / 255.0F;
        GL11.glColor4f(red, green, blue, alpha);
    }

    public static int getDisplayWidth() {
        ScaledResolution scaledResolution = new ScaledResolution(mc);
        int displayWidth = scaledResolution.getScaledWidth();
        return displayWidth;
    }

    public static int getDisplayHeight() {
        ScaledResolution scaledResolution = new ScaledResolution(mc);
        int displayHeight = scaledResolution.getScaledHeight();
        return displayHeight;
    }

    public static void drawCircle(float x, float y, float radius, float lineWidth, int color) {
        enableRender2D();
        setColor(color);
        GL11.glLineWidth(lineWidth);
        int vertices = (int) Math.min(Math.max(radius, 45.0F), 360.0F);
        GL11.glBegin(2);

        for (int i = 0; i < vertices; ++i) {
            double angleRadians = 6.283185307179586D * (double) i / (double) vertices;
            GL11.glVertex2d((double) x + Math.sin(angleRadians) * (double) radius, (double) y + Math.cos(angleRadians) * (double) radius);
        }

        GL11.glEnd();
        disableRender2D();
    }

    public static void drawFilledCircleNoBorder(float x, float y, float radius, int color) {
        enableRender2D();
        setColor(color);
        int vertices = (int) Math.min(Math.max(radius, 45.0F), 360.0F);
        GL11.glBegin(9);

        for (int i = 0; i < vertices; ++i) {
            double angleRadians = 6.283185307179586D * (double) i / (double) vertices;
            GL11.glVertex2d((double) x + Math.sin(angleRadians) * (double) radius, (double) y + Math.cos(angleRadians) * (double) radius);
        }

        GL11.glEnd();
        disableRender2D();
    }

    public static int opacity(int hexColor, int factor) {
        float alpha = Math.max((float) (hexColor >> 24 & 255) - (float) (hexColor >> 24 & 255) / (100.0F / (float) factor), 0.0F);
        float red = (float) (hexColor >> 16 & 255);
        float green = (float) (hexColor >> 8 & 255);
        float blue = (float) (hexColor & 255);
        return (int) ((float) (((int) alpha << 24) + ((int) red << 16) + ((int) green << 8)) + blue);
    }

    public static int darker(int hexColor, int factor) {
        float alpha = (float) (hexColor >> 24 & 255);
        float red = Math.max((float) (hexColor >> 16 & 255) - (float) (hexColor >> 16 & 255) / (100.0F / (float) factor), 0.0F);
        float green = Math.max((float) (hexColor >> 8 & 255) - (float) (hexColor >> 8 & 255) / (100.0F / (float) factor), 0.0F);
        float blue = Math.max((float) (hexColor & 255) - (float) (hexColor & 255) / (100.0F / (float) factor), 0.0F);
        return (int) ((float) (((int) alpha << 24) + ((int) red << 16) + ((int) green << 8)) + blue);
    }

    public static void drawBorderedRect(float x, float y, float width, float height, float borderWidth, Color rectColor, Color borderColor) {
        drawBorderedRect(x, y, width, height, borderWidth, rectColor.getRGB(), borderColor.getRGB());
    }

    public static void drawBox(AxisAlignedBB boundingBox, int color) {
        drawBox(boundingBox, color, true);
    }


    public static void drawRoundedRect3(float x, float y, float x2, float y2, final float round, final int color,final int mode) {
        final float rectX = x,rectY = y, rectX2 = x2, rectY2 = y2;
        x += (float) (round / 2.0f + 0.5);
        y += (float) (round / 2.0f + 0.5);
        x2 -= (float) (round / 2.0f + 0.5);
        y2 -= (float) (round / 2.0f + 0.5);
        if(mode == 1)
            drawRect(x , rectY, rectX2, rectY2, color);
        else
            drawRect(rectX, rectY, x2, rectY2, color);
        circle(x2 - round / 2.0f, y + round / 2.0f, round, color);
        circle(x + round / 2.0f, y2 - round / 2.0f, round, color);
        circle(x + round / 2.0f, y + round / 2.0f, round, color);
        circle(x2 - round / 2.0f, y2 - round / 2.0f, round, color);
        drawRect((int) (x - round / 2.0f - 0.5f), (int) (y + round / 2.0f), (int) x2, (int) (y2 - round / 2.0f),
                color);
        drawRect((int) x, (int) (y + round / 2.0f), (int) (x2 + round / 2.0f + 0.5f), (int) (y2 - round / 2.0f),
                color);
        drawRect((int) (x + round / 2.0f), (int) (y - round / 2.0f - 0.5f), (int) (x2 - round / 2.0f),
                (int) (y2 - round / 2.0f), color);
        drawRect((int) (x + round / 2.0f), (int) y, (int) (x2 - round / 2.0f), (int) (y2 + round / 2.0f + 0.5f),
                color);
    }

    public static void drawFullCircle(final float x, final float y,final float radius,
                                      final int color,final int outSideColor) {
        final float alpha = (color >> 24 & 0xFF) / 255.0f;
        final float red = (color >> 16 & 0xFF) / 255.0f;
        final float green = (color >> 8 & 0xFF) / 255.0f;
        final float blue = (color & 0xFF) / 255.0f;
        final float outSideAlpha = (outSideColor >> 24 & 0xFF) / 255.0f;
        final float outSideRed = (outSideColor >> 16 & 0xFF) / 255.0f;
        final float outSideGreen = (outSideColor >> 8 & 0xFF) / 255.0f;
        final float outSideBlue = (outSideColor & 0xFF) / 255.0f;
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(outSideRed, outSideGreen, outSideBlue, outSideAlpha);
        if (alpha > 0.5f) {
            GL11.glEnable(2881);
            GL11.glEnable(2848);
//            GL11.glLineWidth(2.5F);
            enableSmoothLine(2F);

            GL11.glBlendFunc(770,  771);
            //GL11.glLineWidth(2.0f);
            GL11.glBegin(3);
            for(int i = 0; i <= 360; ++i) {
                GL11.glVertex2d(
                        ( x + Math.sin( ((double) i * 3.141526 / 180.0)) * radius),
                        ( y + Math.cos( ((double) i * 3.141526 / 180.0)) * radius));
            }
            GL11.glEnd();
            GL11.glDisable(2848);
            GL11.glDisable(2881);
            GlStateManager.resetColor();
        }
        GlStateManager.color(red, green, blue, alpha);
        GL11.glBegin(6);
        for(int i = 0; i <= 360; ++i) {
            GL11.glVertex2d(
                    (x + Math.sin( ((double) i * 3.141526 / 180.0)) * radius),
                    (y + Math.cos( ((double) i * 3.141526 / 180.0)) * radius));
        }
        GL11.glEnd();
        disableSmoothLine();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void enableSmoothLine(float width) {
        GL11.glDisable(3008);
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glDisable(3553);
        GL11.glDisable(2929);
        GL11.glDepthMask(false);
        GL11.glEnable(2884);
        GL11.glEnable(2848);
        GL11.glHint(3154, 4354);
        GL11.glHint(3155, 4354);
        GL11.glLineWidth(width);
    }

    public static void startGlScissor(int x, int y, int width, int height) {
        int scaleFactor = new ScaledResolution(mc).getScaleFactor();
        GL11.glPushMatrix();
        GL11.glEnable(3089);
        GL11.glScissor((x * scaleFactor), (mc.displayHeight - (y + height) * scaleFactor),
                (width * scaleFactor), ((height += 14) * scaleFactor));
    }

    public static void arcIiiilllIIiii(final float x, final float y, final float start, final float end, final float radius, final int color, final float lineWidth) {
        illlIIIIiii(x, y, start, end, radius, radius, color, lineWidth);
    }

    public static void drawOutFullCircle(final float x, final float y, final float radius, final int fill, final float lineWidth) {
        arcIiiilllIIiii(x, y, 0.0f, 360.0f, radius, fill, lineWidth);
    }

    public static void drawOutFullCircle(final float x, final float y, final float radius, final int fill, final float lineWidth, final float start, final float end) {
        arcIiiilllIIiii(x, y, start, end, radius, fill, lineWidth);
    }

    public static void illlIIIIiii(final float x, final float y, float start, float end, final float w, final float h, final int color, final float lineWidth) {
        if (start > end) {
            final float temp = end;
            end = start;
            start = temp;
        }
        final float alpha = (color >> 24 & 0xFF) / 255.0f;
        final float red = (color >> 16 & 0xFF) / 255.0f;
        final float green = (color >> 8 & 0xFF) / 255.0f;
        final float blue = (color & 0xFF) / 255.0f;
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
        GlStateManager.color(red, green, blue, alpha);
        GL11.glEnable(2881);
        GL11.glEnable(2848);
        GL11.glLineWidth(lineWidth);
        GL11.glBegin(3);
        for (float i = end; i >= start; i -= 4.0f) {
            GL11.glVertex2d(x + Math.cos(i * 3.141592653589793 / 180.0) * w * 1.001, y + Math.sin(i * 3.141592653589793 / 180.0) * h * 1.001);
        }
        GL11.glEnd();
        GL11.glDisable(2848);
        GL11.glDisable(2881);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static double getAnimationState2(double animation, double finalState, double speed) {
        float add = (float) (0.01 * speed);
        if (animation < finalState) {
            if (animation + add < finalState)
                animation += add;
            else
                animation = finalState;
        } else {
            if (animation - add > finalState)
                animation -= add;
            else
                animation = finalState;
        }
        return animation;
    }

    public static void drawScaledCustomSizeModalCircle(int x, int y, float u, float v, int uWidth, int vHeight, int width, int height, float tileWidth, float tileHeight) {
        float f = 1.0F / tileWidth;
        float f1 = 1.0F / tileHeight;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(GL_POLYGON, DefaultVertexFormats.POSITION_TEX);
        float xRadius = width / 2f;
        float yRadius = height / 2f;
        float uRadius = (((u + (float) uWidth) * f) - (u * f)) / 2f;
        float vRadius = (((v + (float) vHeight) * f1) - (v * f1)) / 2f;
        for(int i = 0; i <= 360; i+=10) {
            double xPosOffset = Math.sin(i * Math.PI / 180.0D);
            double yPosOffset = Math.cos(i * Math.PI / 180.0D);
            worldrenderer.pos(x + xRadius + xPosOffset * xRadius, y + yRadius + yPosOffset * yRadius, 0)
                    .tex(u * f + uRadius + xPosOffset * uRadius, v * f1 + vRadius + yPosOffset * vRadius).endVertex();
        }
        tessellator.draw();
    }

    public static void drawLimitedCircle(final float lx, final float ly, final float x2, final float y2,final int xx, final int yy, final float radius, final Color color) {
        int sections = 50;
        double dAngle = 2 * Math.PI / sections;
        float x, y;

        glPushAttrib(GL_ENABLE_BIT);

        glEnable(GL_BLEND);
        glDisable(GL_TEXTURE_2D);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_LINE_SMOOTH);
        glBegin(GL_TRIANGLE_FAN);

        for (int i = 0; i < sections; i++) {
            x = (float) (radius * Math.sin((i * dAngle)));
            y = (float) (radius * Math.cos((i * dAngle)));

            glColor4f(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, color.getAlpha() / 255F);
            glVertex2f(Math.min(x2,Math.max(xx + x,lx)), Math.min(y2,Math.max(yy + y,ly)));
        }

        GlStateManager.color(0, 0, 0);

        glEnd();

        glPopAttrib();
    }

    public static void drawBorderRect(float x, float y, float x2, float y2, float round, int color) {
        drawRect((x += ((round / 2.0F) + 0.5F)),
                (y += ((round / 2.0f) + 0.5F)),
                (x2 -= ((round / 2.0f) + 0.5F)),
                (y2 -=  ((round / 2.0f) + 0.5F)), color);
        circle(x2 - round / 2.0f, y + round / 2.0f, round, color);
        circle(x + round / 2.0f, y2 - round / 2.0f - 0.2f, round, color);
        circle(x + round / 2.0f, y + round / 2.0f, round, color);
        circle(x2 - round / 2.0f, y2 - round / 2.0f - 0.2f, round, color);
        drawRect((x - round / 2.0f - 0.5f), (y + round / 2.0f), x2, (y2 - round / 2.0f),
                color);
        drawRect( x,  (y + round / 2.0f), (x2 + round / 2.0f + 0.5f), (y2 - round / 2.0f),
                color);
        drawRect((x + round / 2.0f), (y - round / 2.0f - 0.5f), (x2 - round / 2.0f),
                (y2 - round / 2.0f), color);
        drawRect((x + round / 2.0f), y, (x2 - round / 2.0f),(y2 + round / 2.0f + 0.5f),
                color);

    }

    public static void drawRoundedRect2(float x, float y, float x2, float y2, final float round, final int color) {
        x += (float) (round / 2.0f + 0.5);
        y += (float) (round / 2.0f + 0.5);
        x2 -= (float) (round / 2.0f + 0.5);
        y2 -= (float) (round / 2.0f + 0.5);
        drawRect((int) x, (int) y, (int) x2, (int) y2, color);
        circle(x2 - round / 2.0f, y + round / 2.0f, round, color);
        circle(x + round / 2.0f, y2 - round / 2.0f, round, color);
        circle(x + round / 2.0f, y + round / 2.0f, round, color);
        circle(x2 - round / 2.0f, y2 - round / 2.0f, round, color);
        drawRect((int) (x - round / 2.0f - 0.5f), (int) (y + round / 2.0f), (int) x2, (int) (y2 - round / 2.0f),
                color);
        drawRect((int) x, (int) (y + round / 2.0f), (int) (x2 + round / 2.0f + 0.5f), (int) (y2 - round / 2.0f),
                color);
        drawRect((int) (x + round / 2.0f), (int) (y - round / 2.0f - 0.5f), (int) (x2 - round / 2.0f),
                (int) (y2 - round / 2.0f), color);
        drawRect((int) (x + round / 2.0f), (int) y, (int) (x2 - round / 2.0f), (int) (y2 + round / 2.0f + 0.5f),
                color);
    }

    public static void arc3(final float x, final float y, final float start, final float end, final float radius,
                            final Color color) {
        arcEllipse(x, y, start, end, radius, radius, color);
    }

    public static void stopGlScissor() {
        GL11.glDisable(3089);
        GL11.glPopMatrix();
    }

    public static void arcEllipse(final float x, final float y, float start, float end, final float w, final float h,
                                  final Color color) {
        GlStateManager.color(0.0f, 0.0f, 0.0f);
        GL11.glColor4f(0.0f, 0.0f, 0.0f, 0.0f);
        float temp = 0.0f;
        if (start > end) {
            temp = end;
            end = start;
            start = temp;
        }
        final Tessellator var9 = Tessellator.getInstance();
        var9.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f,
                color.getAlpha() / 255.0f);
        if (color.getAlpha() > 0.5f) {
            GL11.glEnable(2848);
            GL11.glLineWidth(2.0f);
            GL11.glBegin(3);
            for (float i = end; i >= start; i -= 4.0f) {
                final float ldx = (float) Math.cos(i * Math.PI / 180.0) * w * 1.001f;
                final float ldy = (float) Math.sin(i * Math.PI / 180.0) * h * 1.001f;
                GL11.glVertex2f(x + ldx, y + ldy);
            }
            GL11.glEnd();
            GL11.glDisable(2848);
        }
        GL11.glBegin(6);
        for (float i = end; i >= start; i -= 4.0f) {
            final float ldx = (float) Math.cos(i * Math.PI / 180.0) * w;
            final float ldy = (float) Math.sin(i * Math.PI / 180.0) * h;
            GL11.glVertex2f(x + ldx, y + ldy);
        }
        GL11.glEnd();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void circle2(final float x, final float y, final float radius, final int color) {
        arc(x,y,180.0F,360.0F,radius,color);
    }

    public static void arc2(final float x, final float y, final float start, final float end, final float radius,
                            final int color) {
        arcEllipse(x, y, start, end, radius, radius, color);
    }

    public static void drawNoFullCircle(final float x, final float y, final float radius, final int fill) {
        arc233(x, y, 0.0f, 360.0f, radius, fill);
    }

    public static void arc233(final float x, final float y, final float start, final float end, final float radius,
                              final int color) {
        arcEllipse233(x, y, start, end, radius, radius, color);
    }

    public static void arcEllipse233(final float x, final float y, float start, float end, final float w, final float h,
                                     final int color) {
        GlStateManager.color(0.0f, 0.0f, 0.0f);
        GL11.glColor4f(0.0f, 0.0f, 0.0f, 0.0f);
        float temp;
        if (start > end) {
            temp = end;
            end = start;
            start = temp;
        }
        final float alpha = (color >> 24 & 0xFF) / 255.0f;
        final float red = (color >> 16 & 0xFF) / 255.0f;
        final float green = (color >> 8 & 0xFF) / 255.0f;
        final float blue = (color & 0xFF) / 255.0f;
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(red, green, blue, alpha);
        if (alpha > 0.5f) {
            GL11.glEnable(GL_POLYGON_SMOOTH);
            GL11.glEnable(2848);
            GL11.glLineWidth(1.5F);
            GL11.glBegin(3);
            for (float i = end; i >= start; i -= 4.0f) {
                final float ldx = (float) Math.cos(i * Math.PI / 180.0) * w * 1.001f;
                final float ldy = (float) Math.sin(i * Math.PI / 180.0) * h * 1.001f;
                GL11.glVertex2f(x + ldx, y + ldy);
            }
            GL11.glEnd();
            GL11.glDisable(2848);
            GL11.glDisable(GL_POLYGON_SMOOTH);
        }
//        GL11.glBegin(6);
//        for (float i = end; i >= start; i -= 4.0F) {
//            final float ldx = (float) Math.cos(i * Math.PI / 180.0) * w;
//            final float ldy = (float) Math.sin(i * Math.PI / 180.0) * h;
//            GL11.glVertex2f(x + ldx, y + ldy);
//        }
//        GL11.glEnd();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void drawOutlinedString(String str, int x, int y, int color,int color2) {
        Minecraft mc = Minecraft.getMinecraft();
        mc.fontRendererObj.drawString(str, (int) (x - 1.0F), y, color2);
        mc.fontRendererObj.drawString(str, (int) (x + 1.0F), y,color2);
        mc.fontRendererObj.drawString(str, x, (int) (y + 1.0F), color2);
        mc.fontRendererObj.drawString(str, x, (int) (y -1.0F),color2);
        mc.fontRendererObj.drawString(str, x, y, color);
    }

    public static void HanaBidrawRect(float x1, float y1, float x2, float y2, int color) {
        GL11.glPushMatrix();
        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(2848);
        GL11.glPushMatrix();
        color(color);
        GL11.glBegin(7);
        GL11.glVertex2d(x2, y1);
        GL11.glVertex2d(x1, y1);
        GL11.glVertex2d(x1, y2);
        GL11.glVertex2d(x2, y2);
        GL11.glEnd();
        GL11.glPopMatrix();
        GL11.glEnable(3553);
        GL11.glDisable(3042);
        GL11.glDisable(2848);
        GL11.glPopMatrix();
        Gui.drawRect(0,0,0,0,0);
    }

    public static void quickDrawHead(ResourceLocation skin, int x, int y, int width, int height) {
        mc.getTextureManager().bindTexture(skin);
        drawScaledCustomSizeModalRect(x, y, 8F, 8F, 8, 8, width, height,
                64F, 64F);
        drawScaledCustomSizeModalRect(x, y, 40F, 8F, 8, 8, width, height,
                64F, 64F);
    }

    public static void drawBlockBox2(final BlockPos blockPos, final Color color, final boolean outline, final boolean box, final float outlineWidth) {
        final RenderManager renderManager = mc.getRenderManager();
        final Timer timer = mc.timer;

        final double x = blockPos.getX() - renderManager.renderPosX;
        final double y = blockPos.getY() - renderManager.renderPosY;
        final double z = blockPos.getZ() - renderManager.renderPosZ;

        AxisAlignedBB axisAlignedBB = new AxisAlignedBB(x, y, z, x + 1.0, y + 1.0, z + 1.0);
        final Block block = BlockUtils.getBlock(blockPos);

        if (block != null) {
            final EntityPlayer player = mc.thePlayer;

            final double posX = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double) timer.renderPartialTicks;
            final double posY = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double) timer.renderPartialTicks;
            final double posZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double) timer.renderPartialTicks;
            axisAlignedBB = block.getSelectedBoundingBox(mc.theWorld, blockPos)
                    .expand(0.0020000000949949026D, 0.0020000000949949026D, 0.0020000000949949026D)
                    .offset(-posX, -posY, -posZ);
        }

        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        enableGlCap(GL_BLEND);
        disableGlCap(GL_TEXTURE_2D, GL_DEPTH_TEST);
        glDepthMask(false);

        if(box) {
            glColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha() != 255 ? color.getAlpha() : outline ? 26 : 35);
            drawFilledBox(axisAlignedBB);
        }

        if (outline) {
            glLineWidth(outlineWidth);
            enableGlCap(GL_LINE_SMOOTH);
            glColor(color);

            drawSelectionBoundingBox(axisAlignedBB);
        }

        GlStateManager.resetColor();
        glDepthMask(true);
        resetCaps();
    }

    public static void drawRect2(final double x, final double y, final double x2, final double y2, final int color) {
        glEnable(GL_BLEND);
        glDisable(GL_TEXTURE_2D);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_LINE_SMOOTH);

        glColor(color);
        glBegin(GL_QUADS);

        glVertex2d(x2, y);
        glVertex2d(x, y);
        glVertex2d(x, y2);
        glVertex2d(x2, y2);
        glEnd();

        glEnable(GL_TEXTURE_2D);
        glDisable(GL_BLEND);
        glDisable(GL_LINE_SMOOTH);
    }

    public static void quickDrawGradientSideways(double left, double top, double right, double bottom, int col1, int col2) {
        glBegin(7);
        glColor(col1);
        glVertex2d(left, top);
        glVertex2d(left, bottom);

        glColor(col2);
        glVertex2d(right, bottom);
        glVertex2d(right, top);
        glEnd();
    }

    public static void drawEntityBox2(final Entity entity, final Color color, final boolean outline, final boolean box, final float outlineWidth) {
        final RenderManager renderManager = mc.getRenderManager();
        final Timer timer = mc.timer;

        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        enableGlCap(GL_BLEND);
        disableGlCap(GL_TEXTURE_2D, GL_DEPTH_TEST);
        glDepthMask(false);

        final double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * timer.renderPartialTicks
                - renderManager.renderPosX;
        final double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * timer.renderPartialTicks
                - renderManager.renderPosY;
        final double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * timer.renderPartialTicks
                - renderManager.renderPosZ;

        final AxisAlignedBB entityBox = entity.getEntityBoundingBox();
        final AxisAlignedBB axisAlignedBB = new AxisAlignedBB(
                entityBox.minX - entity.posX + x - 0.05D,
                entityBox.minY - entity.posY + y,
                entityBox.minZ - entity.posZ + z - 0.05D,
                entityBox.maxX - entity.posX + x + 0.05D,
                entityBox.maxY - entity.posY + y + 0.15D,
                entityBox.maxZ - entity.posZ + z + 0.05D
        );

        if (outline) {
            glLineWidth(outlineWidth);
            enableGlCap(GL_LINE_SMOOTH);
            glColor(color.getRed(), color.getGreen(), color.getBlue(), box?170:255);
            drawSelectionBoundingBox(axisAlignedBB);
        }

        if(box) {
            glColor(color.getRed(), color.getGreen(), color.getBlue(), outline ? 26 : 35);
            drawFilledBox(axisAlignedBB);
        }

        GlStateManager.resetColor();
        glDepthMask(true);
        resetCaps();
    }

    public static void drawAxisAlignedBB(final AxisAlignedBB axisAlignedBB, final Color color, final boolean outline, final boolean box, final float outlineWidth) {
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_BLEND);
        glLineWidth(outlineWidth);
        glDisable(GL_TEXTURE_2D);
        glDisable(GL_DEPTH_TEST);
        glDepthMask(false);
        glColor(color);

        if (outline) {
            glLineWidth(outlineWidth);
            enableGlCap(GL_LINE_SMOOTH);
            glColor(color.getRed(), color.getGreen(), color.getBlue(), 95);
            drawSelectionBoundingBox(axisAlignedBB);
        }

        if(box) {
            glColor(color.getRed(), color.getGreen(), color.getBlue(), outline ? 26 : 35);
            drawFilledBox(axisAlignedBB);
        }

        GlStateManager.resetColor();
        glEnable(GL_TEXTURE_2D);
        glEnable(GL_DEPTH_TEST);
        glDepthMask(true);
        glDisable(GL_BLEND);
    }

    public static void drawRectBlur(final float x, final float y, final float x2, final float y2, final int color) {
        glEnable(GL_BLEND);
        glDisable(GL_TEXTURE_2D);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_LINE_SMOOTH);

        glColor(color);
        quickDrawRect(x, y, x2, y2);

        glEnable(GL_TEXTURE_2D);
        glDisable(GL_BLEND);
        glDisable(GL_LINE_SMOOTH);
    }

    public static void drawBorderedRect2(final float x, final float y, final float x2, final float y2, final float width, final int color1, final int color2) {
        drawRectBlur(x, y, x2, y2, color2);
        glEnable(GL_BLEND);
        glDisable(GL_TEXTURE_2D);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_LINE_SMOOTH);

        glColor(color1);
        glLineWidth(width);
        glBegin(1);
        glVertex2d(x, y);
        glVertex2d(x, y2);
        glVertex2d(x2, y2);
        glVertex2d(x2, y);
        glVertex2d(x, y);
        glVertex2d(x2, y);
        glVertex2d(x, y2);
        glVertex2d(x2, y2);
        glEnd();

        glEnable(GL_TEXTURE_2D);
        glDisable(GL_BLEND);
        glDisable(GL_LINE_SMOOTH);
    }

    public static void drawFilledCircle(int xx, int yy, float radius, int col) {
        float f = (float) (col >> 24 & 255) / 255.0f;
        float f1 = (float) (col >> 16 & 255) / 255.0f;
        float f2 = (float) (col >> 8 & 255) / 255.0f;
        float f3 = (float) (col & 255) / 255.0f;
        int sections = 50;
        double dAngle = 6.283185307179586 / (double) sections;
        GL11.glPushMatrix();
        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glEnable(2848);
        GL11.glBlendFunc(770, 771);
        GL11.glBegin(6);
        int i = 0;
        while (i < sections) {
            float x = (float) ((double) radius * Math.sin((double) i * dAngle));
            float y = (float) ((double) radius * Math.cos((double) i * dAngle));
            GL11.glColor4f(f1, f2, f3, f);
            GL11.glVertex2f((float) xx + x, (float) yy + y);
            ++i;
        }
        GlStateManager.color(0.0f, 0.0f, 0.0f);
        GL11.glEnd();
        GL11.glEnable(3553);
        GL11.glDisable(3042);
        GL11.glDisable(2848);
        GL11.glPopMatrix();
    }

    public static void drawFilledCircle(float xx, float yy, float radius, int col) {
        float f = (float) (col >> 24 & 255) / 255.0f;
        float f1 = (float) (col >> 16 & 255) / 255.0f;
        float f2 = (float) (col >> 8 & 255) / 255.0f;
        float f3 = (float) (col & 255) / 255.0f;
        int sections = 50;
        double dAngle = 6.283185307179586 / (double) sections;
        GL11.glPushMatrix();
        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(2848);
        GL11.glBegin(6);
        int i = 0;
        while (i < sections) {
            float x = (float) ((double) radius * Math.sin((double) i * dAngle));
            float y = (float) ((double) radius * Math.cos((double) i * dAngle));
            GL11.glColor4f(f1, f2, f3, f);
            GL11.glVertex2f(xx + x, yy + y);
            ++i;
        }
        GlStateManager.color(0.0f, 0.0f, 0.0f);
        GL11.glEnd();
        GL11.glEnable(3553);
        GL11.glDisable(3042);
        GL11.glDisable(2848);
        GL11.glPopMatrix();
    }

    public static void glColor(final int hex, final int alpha) {
        final float red = (hex >> 16 & 0xFF) / 255F;
        final float green = (hex >> 8 & 0xFF) / 255F;
        final float blue = (hex & 0xFF) / 255F;

        GlStateManager.color(red, green, blue, alpha / 255F);
    }

    public static void glColor(final int hex, final float alpha) {
        final float red = (hex >> 16 & 0xFF) / 255F;
        final float green = (hex >> 8 & 0xFF) / 255F;
        final float blue = (hex & 0xFF) / 255F;

        GlStateManager.color(red, green, blue, alpha);
    }

    public static void glColor(final Color color, final float alpha) {
        final float red = color.getRed() / 255F;
        final float green = color.getGreen() / 255F;
        final float blue = color.getBlue() / 255F;

        GlStateManager.color(red, green, blue, alpha);
    }

    private static final Frustum frustrum = new Frustum();
    protected static float zLevel = 0F;

    /**
     * Draws a textured rectangle at the stored z-value. Args: x, y, u, v, width, height
     */
    public static void drawTexturedModalRect(int x, int y, int textureX, int textureY, int width, int height, float zLevel)
    {
        float f = 0.00390625F;
        float f1 = 0.00390625F;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos((double)(x + 0), (double)(y + height), (double)zLevel).tex((double)((float)(textureX + 0) * f), (double)((float)(textureY + height) * f1)).endVertex();
        worldrenderer.pos((double)(x + width), (double)(y + height), (double)zLevel).tex((double)((float)(textureX + width) * f), (double)((float)(textureY + height) * f1)).endVertex();
        worldrenderer.pos((double)(x + width), (double)(y + 0), (double)zLevel).tex((double)((float)(textureX + width) * f), (double)((float)(textureY + 0) * f1)).endVertex();
        worldrenderer.pos((double)(x + 0), (double)(y + 0), (double)zLevel).tex((double)((float)(textureX + 0) * f), (double)((float)(textureY + 0) * f1)).endVertex();
        tessellator.draw();
    }
    public static void drawShadow(float x, float y, float width, float height) {
        drawTexturedRect(x - 9, y - 9, 9, 9, "paneltopleft");
        drawTexturedRect(x - 9, y + height, 9, 9, "panelbottomleft");
        drawTexturedRect(x + width, y + height, 9, 9, "panelbottomright");
        drawTexturedRect(x + width, y - 9, 9, 9, "paneltopright");
        drawTexturedRect(x - 9, y, 9, height, "panelleft");
        drawTexturedRect(x + width, y, 9, height, "panelright");
        drawTexturedRect(x, y - 9, width, 9, "paneltop");
        drawTexturedRect(x, y + height, width, 9, "panelbottom");
    }

    public static void drawModel(final float yaw, final float pitch, final EntityLivingBase entityLivingBase) {
        GlStateManager.resetColor();
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.enableColorMaterial();
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0f, 0.0f, 50.0f);
        GlStateManager.scale(-50.0f, 50.0f, 50.0f);
        GlStateManager.rotate(180.0f, 0.0f, 0.0f, 1.0f);
        final float renderYawOffset = entityLivingBase.renderYawOffset;
        final float rotationYaw = entityLivingBase.rotationYaw;
        final float rotationPitch = entityLivingBase.rotationPitch;
        final float prevRotationYawHead = entityLivingBase.prevRotationYawHead;
        final float rotationYawHead = entityLivingBase.rotationYawHead;
        GlStateManager.rotate(135.0f, 0.0f, 1.0f, 0.0f);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.rotate(-135.0f, 0.0f, 1.0f, 0.0f);
        GlStateManager.rotate((float) (-Math.atan(pitch / 40.0f) * 20.0), 1.0f, 0.0f, 0.0f);
        entityLivingBase.renderYawOffset = yaw - yaw / yaw * 0.4f;
        entityLivingBase.rotationYaw = yaw - yaw / yaw * 0.2f;
        entityLivingBase.rotationPitch = pitch;
        entityLivingBase.rotationYawHead = entityLivingBase.rotationYaw;
        entityLivingBase.prevRotationYawHead = entityLivingBase.rotationYaw;
        GlStateManager.translate(0.0f, 0.0f, 0.0f);
        final RenderManager renderManager = mc.getRenderManager();
        renderManager.setPlayerViewY(180.0f);
        renderManager.setRenderShadow(false);
        entityLivingBase.getAlwaysRenderNameTag();
        renderManager.renderEntityWithPosYaw(entityLivingBase, 0.0, 0.0, 0.0, 0.0f, 1.0f);
        renderManager.setRenderShadow(true);
        entityLivingBase.renderYawOffset = renderYawOffset;
        entityLivingBase.rotationYaw = rotationYaw;
        entityLivingBase.rotationPitch = rotationPitch;
        entityLivingBase.prevRotationYawHead = prevRotationYawHead;
        entityLivingBase.rotationYawHead = rotationYawHead;
        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.resetColor();
    }

    public static void drawNewRect(double left, double top, double right, double bottom, int color) {
        if (left < right) {
            double i = left;
            left = right;
            right = i;
        }
        if (top < bottom) {
            double j = top;
            top = bottom;
            bottom = j;
        }
        float f3 = (float)(color >> 24 & 0xFF) / 255.0f;
        float f = (float)(color >> 16 & 0xFF) / 255.0f;
        float f1 = (float)(color >> 8 & 0xFF) / 255.0f;
        float f2 = (float)(color & 0xFF) / 255.0f;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer vertexbuffer = tessellator.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
        GlStateManager.color(f, f1, f2, f3);
        vertexbuffer.begin(7, DefaultVertexFormats.POSITION);
        vertexbuffer.pos(left, bottom, 0.0).endVertex();
        vertexbuffer.pos(right, bottom, 0.0).endVertex();
        vertexbuffer.pos(right, top, 0.0).endVertex();
        vertexbuffer.pos(left, top, 0.0).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void drawCheckeredBackground(final float x, float y, final float x2, final float y2) {
        drawRect(x, y, x2, y2, getColor(16777215));
        boolean offset = false;
        while (y < y2) {
            for (float x3 = x + ((offset = !offset) ? 1 : 0); x3 < x2; x3 += 2.0f) {
                if (x3 <= x2 - 1.0f) {
                    drawRect(x3, y, x3 + 1.0f, y + 1.0f, getColor(8421504));
                }
            }
            ++y;
        }
    }

    public static void skeetRect(final double x, final double y, final double x1, final double y1, final double size) {
        RenderUtils.rectangleBordered(x, y + -4.0, x1 + size, y1 + size, 0.5, new Color(60, 60, 60).getRGB(), new Color(10, 10, 10).getRGB());
        RenderUtils.rectangleBordered(x + 1.0, y + -3.0, x1 + size - 1.0, y1 + size - 1.0, 1.0, new Color(40, 40, 40).getRGB(), new Color(40, 40, 40).getRGB());
        RenderUtils.rectangleBordered(x + 2.5, y + -1.5, x1 + size - 2.5, y1 + size - 2.5, 0.5, new Color(40, 40, 40).getRGB(), new Color(60, 60, 60).getRGB());
        RenderUtils.rectangleBordered(x + 2.5, y + -1.5, x1 + size - 2.5, y1 + size - 2.5, 0.5, new Color(22, 22, 22).getRGB(), new Color(255, 255, 255, 0).getRGB());
    }

    public static int getColor(final int color) {
        final int r = color >> 16 & 0xFF;
        final int g = color >> 8 & 0xFF;
        final int b = color & 0xFF;
        final int a = 255;
        return (r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF) | (a & 0xFF) << 24;
    }

    public static int darker(final int color, final float factor) {
        final int r = (int)((color >> 16 & 0xFF) * factor);
        final int g = (int)((color >> 8 & 0xFF) * factor);
        final int b = (int)((color & 0xFF) * factor);
        final int a = color >> 24 & 0xFF;
        return (r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF) | (a & 0xFF) << 24;
    }

    public static void drawGradientRect(final double left, final double top, final double right, final double bottom, final boolean sideways, final int startColor, final int endColor) {
        GL11.glDisable(3553);
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glShadeModel(7425);
        GL11.glBegin(7);
        color(startColor);
        if (sideways) {
            GL11.glVertex2d(left, top);
            GL11.glVertex2d(left, bottom);
            color(endColor);
            GL11.glVertex2d(right, bottom);
            GL11.glVertex2d(right, top);
        }
        else {
            GL11.glVertex2d(left, top);
            color(endColor);
            GL11.glVertex2d(left, bottom);
            GL11.glVertex2d(right, bottom);
            color(startColor);
            GL11.glVertex2d(right, top);
        }
        GL11.glEnd();
        GL11.glDisable(3042);
        GL11.glShadeModel(7424);
        GL11.glEnable(3553);
    }

    public static void drawGradientRect(final float left, final float top, final float right, final float bottom, final int startColor, final int endColor) {
        final float f = (startColor >> 24 & 0xFF) / 255.0f;
        final float f2 = (startColor >> 16 & 0xFF) / 255.0f;
        final float f3 = (startColor >> 8 & 0xFF) / 255.0f;
        final float f4 = (startColor & 0xFF) / 255.0f;
        final float f5 = (endColor >> 24 & 0xFF) / 255.0f;
        final float f6 = (endColor >> 16 & 0xFF) / 255.0f;
        final float f7 = (endColor >> 8 & 0xFF) / 255.0f;
        final float f8 = (endColor & 0xFF) / 255.0f;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(7425);
        final Tessellator tessellator = Tessellator.getInstance();
        final WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos((double)right, (double)top, 0.0).color(f2, f3, f4, f).endVertex();
        worldrenderer.pos((double)left, (double)top, 0.0).color(f2, f3, f4, f).endVertex();
        worldrenderer.pos((double)left, (double)bottom, 0.0).color(f6, f7, f8, f5).endVertex();
        worldrenderer.pos((double)right, (double)bottom, 0.0).color(f6, f7, f8, f5).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }
    public static void drawGradientSidewaysV(double left, double top, double right, double bottom, int col1, int col2) {
        glEnable(GL_BLEND);
        glDisable(GL_TEXTURE_2D);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_LINE_SMOOTH);
        glShadeModel(GL_SMOOTH);

        quickDrawGradientSidewaysV(left, top, right, bottom, col1, col2);

        glEnable(GL_TEXTURE_2D);
        glDisable(GL_BLEND);
        glDisable(GL_LINE_SMOOTH);
        glShadeModel(GL_FLAT);
    }
    public static void quickDrawGradientSidewaysV(double left, double top, double right, double bottom, int col1, int col2) {
        glBegin(GL_QUADS);

        glColor(col1);
        glVertex2d(left, top);
        glVertex2d(right, top);
        glColor(col2);
        glVertex2d(right, bottom);
        glVertex2d(left, bottom);

        glEnd();
    }



    public static void scissor(final double x, final double y, final double width, final double height) {
        int scaleFactor;
        for (scaleFactor = new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor(); scaleFactor < 2 && Minecraft.getMinecraft().displayWidth / (scaleFactor + 1) >= 320 && Minecraft.getMinecraft().displayHeight / (scaleFactor + 1) >= 240; ++scaleFactor) {}
        GL11.glScissor((int)(x * scaleFactor), (int)(Minecraft.getMinecraft().displayHeight - (y + height) * scaleFactor), (int)(width * scaleFactor), (int)(height * scaleFactor));
    }

    public static void drawClickGuiArrow(final float x, final float y, final float size, final Animation animation, final int color) {
        GL11.glTranslatef(x, y, 0.0f);
        final double[] interpolation = new double[1];
        setup2DRendering(() -> render(5, () -> {
            color(color);
            interpolation[0] = interpolate(0.0, size / 2.0, animation.getOutput());
            if (animation.getOutput() >= 0.48) {
                GL11.glVertex2d((double)(size / 2.0f), (double)interpolate(size / 2.0, 0.0, animation.getOutput()));
            }
            GL11.glVertex2d(0.0, interpolation[0]);
            if (animation.getOutput() < 0.48) {
                GL11.glVertex2d((double)(size / 2.0f), (double)interpolate(size / 2.0, 0.0, animation.getOutput()));
            }
            GL11.glVertex2d((double)size, interpolation[0]);
        }));
        GL11.glTranslatef(-x, -y, 0.0f);
    }

    public static void render(final int mode, final Runnable render) {
        GL11.glBegin(mode);
        render.run();
        GL11.glEnd();
    }

    public static void setup2DRendering(final Runnable f) {
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glDisable(3553);
        f.run();
        GL11.glEnable(3553);
        GlStateManager.disableBlend();
    }

    public static void drawTexturedRect(float x, float y, float width, float height, String image) {
        glPushMatrix();
        final boolean enableBlend = glIsEnabled(GL_BLEND);
        final boolean disableAlpha = !glIsEnabled(GL_ALPHA_TEST);
        if (!enableBlend) glEnable(GL_BLEND);
        if (!disableAlpha) glDisable(GL_ALPHA_TEST);
        mc.getTextureManager().bindTexture(new ResourceLocation("liquidbounce+/ui/" + image + ".png"));
        GlStateManager.color(1F, 1F, 1F, 1F);
        RenderUtils.drawModalRectWithCustomSizedTexture(x, y, 0, 0, width, height, width, height);
        if (!enableBlend) glDisable(GL_BLEND);
        if (!disableAlpha) glEnable(GL_ALPHA_TEST);
        glPopMatrix();
    }

    public static void drawModalRectWithCustomSizedTexture(float x, float y, float u, float v, float width, float height, float textureWidth, float textureHeight)
    {
        float f = 1.0F / textureWidth;
        float f1 = 1.0F / textureHeight;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(x, (y + height), 0.0D).tex((u * f), ((v + height) * f1)).endVertex();
        worldrenderer.pos((x + width), (y + height), 0.0D).tex(((u + width) * f), ((v + height) * f1)).endVertex();
        worldrenderer.pos((x + width), y, 0.0D).tex(((u + width) * f), (v * f1)).endVertex();
        worldrenderer.pos(x, y, 0.0D).tex((u * f), (v * f1)).endVertex();
        tessellator.draw();
    }

    public static boolean isInViewFrustrum(Entity entity) {
        return isInViewFrustrum(entity.getEntityBoundingBox()) || entity.ignoreFrustumCheck;
    }

    private static boolean isInViewFrustrum(AxisAlignedBB bb) {
        Entity current = mc.getRenderViewEntity();
        frustrum.setPosition(current.posX, current.posY, current.posZ);
        return frustrum.isBoundingBoxInFrustum(bb);
    }

    public static float interpolate(float current, float old, float scale) {
        return old + (current - old) * scale;
    }

    public static double interpolate(double current, double old, double scale) {
        return old + (current - old) * scale;
    }

    public static int SkyRainbow(int var2, float st, float bright) {
        double v1 = Math.ceil(System.currentTimeMillis() + (long) (var2 * 109)) / 5;
        return Color.getHSBColor((double) ((float) ((v1 %= 360.0) / 360.0)) < 0.5 ? -((float) (v1 / 360.0)) : (float) (v1 / 360.0), st, bright).getRGB();
    }

    public static Color skyRainbow(int var2, float st, float bright) {
        double v1 = Math.ceil(System.currentTimeMillis() + (long) (var2 * 109)) / 5;
        return Color.getHSBColor((double) ((float) ((v1 %= 360.0) / 360.0)) < 0.5 ? -((float) (v1 / 360.0)) : (float) (v1 / 360.0), st, bright);
    }

    public static int getRainbowOpaque(int seconds, float saturation, float brightness, int index) {
        float hue = ((System.currentTimeMillis() + index) % (int) (seconds * 1000)) / (float) (seconds * 1000);
        int color = Color.HSBtoRGB(hue, saturation, brightness);
        return color;
    }

    public static int getNormalRainbow(int delay, float sat, float brg) {
        double rainbowState = Math.ceil((System.currentTimeMillis() + delay) / 20.0);
        rainbowState %= 360;
        return Color.getHSBColor((float) (rainbowState / 360.0f), sat, brg).getRGB();
    }

    public static void startSmooth() {
        glEnable(2848);
        glEnable(2881);
        glEnable(2832);
        glEnable(3042);
        glBlendFunc(770, 771);
        glHint(3154, 4354);
        glHint(3155, 4354);
        glHint(3153, 4354);
    }

    public static void endSmooth() {
        glDisable(2848);
        glDisable(2881);
        glEnable(2832);
    }
    public static void MdrawRect(double d, double e, double g, double h, int color)
    {
        if (d < g)
        {
            int i = (int) d;
            d = g;
            g = i;
        }

        if (e < h)
        {
            int j = (int) e;
            e = h;
            h = j;
        }

        float f3 = (float)(color >> 24 & 255) / 255.0F;
        float f = (float)(color >> 16 & 255) / 255.0F;
        float f1 = (float)(color >> 8 & 255) / 255.0F;
        float f2 = (float)(color & 255) / 255.0F;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(f, f1, f2, f3);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION);
        worldrenderer.pos((double)d, (double)h, 0.0D).endVertex();
        worldrenderer.pos((double)g, (double)h, 0.0D).endVertex();
        worldrenderer.pos((double)g, (double)e, 0.0D).endVertex();
        worldrenderer.pos((double)d, (double)e, 0.0D).endVertex();
        tessellator.draw();
        enableTexture2D();
        disableBlend();
    }



    public static void drawExhiRect(float x, float y, float x2, float y2) {
        drawRect(x - 3.5F, y - 3.5F, x2 + 3.5F, y2 + 3.5F, Color.black.getRGB());
        drawRect(x - 3F, y - 3F, x2 + 3F, y2 + 3F, new Color(50, 50, 50).getRGB());
        //drawBorder(x - 1.5F, y - 1.5F, x2 + 1.5F, y2 + 1.5F, 2.5F, new Color(26, 26, 26).getRGB());
        drawRect(x - 2.5F, y - 2.5F, x2 + 2.5F, y2 + 2.5F, new Color(26, 26, 26).getRGB());
        drawRect(x - 0.5F, y - 0.5F, x2 + 0.5F, y2 + 0.5F, new Color(50, 50, 50).getRGB());
        drawRect(x, y, x2, y2, new Color(18, 18, 18).getRGB());
    }

    public static void drawOutlinedRect(float x, float y, float width, float height, float lineSize, int lineColor) {
        RenderUtils.drawRect(x, y, width, y + lineSize, lineColor);
        RenderUtils.drawRect(x, height - lineSize, width, height, lineColor);
        RenderUtils.drawRect(x, y + lineSize, x + lineSize, height - lineSize, lineColor);
        RenderUtils.drawRect(width - lineSize, y + lineSize, width, height - lineSize, lineColor);
    }

    public static void drawCircle(float x, float y ,float radius, final Color color) {
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
        glColor(Color.WHITE);

        glEnable(GL_LINE_SMOOTH);
        glLineWidth(1);
        glBegin(GL_LINE_STRIP);
        for (float i = 180; i >= -180; i -= (360 / 90)) {
            glColor4f(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, color.getAlpha() / 255F);
            glVertex2f((float) (x + (cos(i * PI / 180) * (radius * 1.001F))), (float) (y + (sin(i * PI / 180) * (radius * 1.001F))));
        }
        glEnd();
        glDisable(GL_LINE_SMOOTH);

        enableTexture2D();
        disableBlend();
    }


    public static void drawExhiRect(float x, float y, float x2, float y2, float alpha) {
        drawRect(x - 3.5F, y - 3.5F, x2 + 3.5F, y2 + 3.5F, new Color(0, 0, 0, alpha).getRGB());
        drawRect(x - 3F, y - 3F, x2 + 3F, y2 + 3F, new Color(50F / 255F, 50F / 255F, 50F / 255F, alpha).getRGB());
        drawRect(x - 2.5F, y - 2.5F, x2 + 2.5F, y2 + 2.5F, new Color(26F / 255F, 26F / 255F, 26F / 255F, alpha).getRGB());
        drawRect(x - 0.5F, y - 0.5F, x2 + 0.5F, y2 + 0.5F, new Color(50F / 255F, 50F / 255F, 50F / 255F, alpha).getRGB());
        drawRect(x, y, x2, y2, new Color(18F / 255F, 18 / 255F, 18F / 255F, alpha).getRGB());
    }

    public static void drawRoundedRect(float paramXStart, float paramYStart, float paramXEnd, float paramYEnd, float radius, int color) {
        drawRoundedRect(paramXStart, paramYStart, paramXEnd, paramYEnd, radius, color, true);
    }

    public static void drawRDRect(final float left, final float top, final float width, final float height, final int color) {
        final float f3 = (color >> 24 & 0xFF) / 255.0f;
        final float f4 = (color >> 16 & 0xFF) / 255.0f;
        final float f5 = (color >> 8 & 0xFF) / 255.0f;
        final float f6 = (color & 0xFF) / 255.0f;
        final Tessellator tessellator = Tessellator.getInstance();
        final WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(f4, f5, f6, f3);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION);
        worldrenderer.pos((double)left, (double)(top + height), 0.0).endVertex();
        worldrenderer.pos((double)(left + width), (double)(top + height), 0.0).endVertex();
        worldrenderer.pos((double)(left + width), (double)top, 0.0).endVertex();
        worldrenderer.pos((double)left, (double)top, 0.0).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void enableRender2D() {
        GL11.glEnable(3042);
        GL11.glDisable(2884);
        GL11.glDisable(3553);
        GL11.glEnable(2848);
        GL11.glBlendFunc(770, 771);
        GL11.glLineWidth(1.0f);
    }

    public static void color(final int color, final float alpha) {
        final float r = (color >> 16 & 0xFF) / 255.0f;
        final float g = (color >> 8 & 0xFF) / 255.0f;
        final float b = (color & 0xFF) / 255.0f;
        GlStateManager.color(r, g, b, alpha);
    }

    public static void color(final int color) {
        color(color, (color >> 24 & 0xFF) / 255.0f);
    }

    public static void drawRoundedRect(final float x, final float y, final float width, final float height, float edgeRadius, int color, final float borderWidth, int borderColor) {
        if (color == 16777215) {
            color = -65794;
        }
        if (borderColor == 16777215) {
            borderColor = -65794;
        }
        if (edgeRadius < 0.0f) {
            edgeRadius = 0.0f;
        }
        if (edgeRadius > width / 2.0f) {
            edgeRadius = width / 2.0f;
        }
        if (edgeRadius > height / 2.0f) {
            edgeRadius = height / 2.0f;
        }
        drawRDRect(x + edgeRadius, y + edgeRadius, width - edgeRadius * 2.0f, height - edgeRadius * 2.0f, color);
        drawRDRect(x + edgeRadius, y, width - edgeRadius * 2.0f, edgeRadius, color);
        drawRDRect(x + edgeRadius, y + height - edgeRadius, width - edgeRadius * 2.0f, edgeRadius, color);
        drawRDRect(x, y + edgeRadius, edgeRadius, height - edgeRadius * 2.0f, color);
        drawRDRect(x + width - edgeRadius, y + edgeRadius, edgeRadius, height - edgeRadius * 2.0f, color);
        enableRender2D();
        color(color);
        GL11.glBegin(6);
        float centerX = x + edgeRadius;
        float centerY = y + edgeRadius;
        GL11.glVertex2d((double)centerX, (double)centerY);
        for (int vertices = (int)Math.min(Math.max(edgeRadius, 10.0f), 90.0f), i = 0; i < vertices + 1; ++i) {
            final double angleRadians = 6.283185307179586 * (i + 180) / (vertices * 4);
            GL11.glVertex2d(centerX + Math.sin(angleRadians) * edgeRadius, centerY + Math.cos(angleRadians) * edgeRadius);
        }
        GL11.glEnd();
        GL11.glBegin(6);
        centerX = x + width - edgeRadius;
        centerY = y + edgeRadius;
        GL11.glVertex2d((double)centerX, (double)centerY);
        for (int vertices = (int)Math.min(Math.max(edgeRadius, 10.0f), 90.0f), i = 0; i < vertices + 1; ++i) {
            final double angleRadians = 6.283185307179586 * (i + 90) / (vertices * 4);
            GL11.glVertex2d(centerX + Math.sin(angleRadians) * edgeRadius, centerY + Math.cos(angleRadians) * edgeRadius);
        }
        GL11.glEnd();
        GL11.glBegin(6);
        centerX = x + edgeRadius;
        centerY = y + height - edgeRadius;
        GL11.glVertex2d((double)centerX, (double)centerY);
        for (int vertices = (int)Math.min(Math.max(edgeRadius, 10.0f), 90.0f), i = 0; i < vertices + 1; ++i) {
            final double angleRadians = 6.283185307179586 * (i + 270) / (vertices * 4);
            GL11.glVertex2d(centerX + Math.sin(angleRadians) * edgeRadius, centerY + Math.cos(angleRadians) * edgeRadius);
        }
        GL11.glEnd();
        GL11.glBegin(6);
        centerX = x + width - edgeRadius;
        centerY = y + height - edgeRadius;
        GL11.glVertex2d((double)centerX, (double)centerY);
        for (int vertices = (int)Math.min(Math.max(edgeRadius, 10.0f), 90.0f), i = 0; i < vertices + 1; ++i) {
            final double angleRadians = 6.283185307179586 * i / (vertices * 4);
            GL11.glVertex2d(centerX + Math.sin(angleRadians) * edgeRadius, centerY + Math.cos(angleRadians) * edgeRadius);
        }
        GL11.glEnd();
        color(borderColor);
        GL11.glLineWidth(borderWidth);
        GL11.glBegin(3);
        centerX = x + edgeRadius;
        centerY = y + edgeRadius;
        int vertices;
        int i;
        for (vertices = (i = (int)Math.min(Math.max(edgeRadius, 10.0f), 90.0f)); i >= 0; --i) {
            final double angleRadians = 6.283185307179586 * (i + 180) / (vertices * 4);
            GL11.glVertex2d(centerX + Math.sin(angleRadians) * edgeRadius, centerY + Math.cos(angleRadians) * edgeRadius);
        }
        GL11.glVertex2d((double)(x + edgeRadius), (double)y);
        GL11.glVertex2d((double)(x + width - edgeRadius), (double)y);
        centerX = x + width - edgeRadius;
        centerY = y + edgeRadius;
        for (i = vertices; i >= 0; --i) {
            final double angleRadians = 6.283185307179586 * (i + 90) / (vertices * 4);
            GL11.glVertex2d(centerX + Math.sin(angleRadians) * edgeRadius, centerY + Math.cos(angleRadians) * edgeRadius);
        }
        GL11.glVertex2d((double)(x + width), (double)(y + edgeRadius));
        GL11.glVertex2d((double)(x + width), (double)(y + height - edgeRadius));
        centerX = x + width - edgeRadius;
        centerY = y + height - edgeRadius;
        for (i = vertices; i >= 0; --i) {
            final double angleRadians = 6.283185307179586 * i / (vertices * 4);
            GL11.glVertex2d(centerX + Math.sin(angleRadians) * edgeRadius, centerY + Math.cos(angleRadians) * edgeRadius);
        }
        GL11.glVertex2d((double)(x + width - edgeRadius), (double)(y + height));
        GL11.glVertex2d((double)(x + edgeRadius), (double)(y + height));
        centerX = x + edgeRadius;
        centerY = y + height - edgeRadius;
        for (i = vertices; i >= 0; --i) {
            final double angleRadians = 6.283185307179586 * (i + 270) / (vertices * 4);
            GL11.glVertex2d(centerX + Math.sin(angleRadians) * edgeRadius, centerY + Math.cos(angleRadians) * edgeRadius);
        }
        GL11.glVertex2d((double)x, (double)(y + height - edgeRadius));
        GL11.glVertex2d((double)x, (double)(y + edgeRadius));
        GL11.glEnd();
        disableRender2D();
    }

    public static void disableRender2D() {
        GL11.glDisable(3042);
        GL11.glEnable(2884);
        GL11.glEnable(3553);
        GL11.glDisable(2848);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
    }

    public static void resetColor() {
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
    }

    public static void originalRoundedRect(float paramXStart, float paramYStart, float paramXEnd, float paramYEnd, float radius, int color) {
        float alpha = (color >> 24 & 0xFF) / 255.0F;
        float red = (color >> 16 & 0xFF) / 255.0F;
        float green = (color >> 8 & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;

        float z = 0;
        if (paramXStart > paramXEnd) {
            z = paramXStart;
            paramXStart = paramXEnd;
            paramXEnd = z;
        }

        if (paramYStart > paramYEnd) {
            z = paramYStart;
            paramYStart = paramYEnd;
            paramYEnd = z;
        }

    	double x1 = (double)(paramXStart + radius);
    	double y1 = (double)(paramYStart + radius);
    	double x2 = (double)(paramXEnd - radius);
    	double y2 = (double)(paramYEnd - radius);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();

        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(red, green, blue, alpha);
        worldrenderer.begin(GL_POLYGON, DefaultVertexFormats.POSITION);

        double degree = Math.PI / 180;
        for (double i = 0; i <= 90; i += 1)
            worldrenderer.pos(x2 + Math.sin(i * degree) * radius, y2 + Math.cos(i * degree) * radius, 0.0D).endVertex();
        for (double i = 90; i <= 180; i += 1)
            worldrenderer.pos(x2 + Math.sin(i * degree) * radius, y1 + Math.cos(i * degree) * radius, 0.0D).endVertex();
        for (double i = 180; i <= 270; i += 1)
            worldrenderer.pos(x1 + Math.sin(i * degree) * radius, y1 + Math.cos(i * degree) * radius, 0.0D).endVertex();
        for (double i = 270; i <= 360; i += 1)
            worldrenderer.pos(x1 + Math.sin(i * degree) * radius, y2 + Math.cos(i * degree) * radius, 0.0D).endVertex();

        tessellator.draw();
        enableTexture2D();
        disableBlend();
    }

    public static void newDrawRect(float left, float top, float right, float bottom, int color)
    {
        if (left < right)
        {
            float i = left;
            left = right;
            right = i;
        }

        if (top < bottom)
        {
            float j = top;
            top = bottom;
            bottom = j;
        }

        float f3 = (float)(color >> 24 & 255) / 255.0F;
        float f = (float)(color >> 16 & 255) / 255.0F;
        float f1 = (float)(color >> 8 & 255) / 255.0F;
        float f2 = (float)(color & 255) / 255.0F;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(f, f1, f2, f3);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION);
        worldrenderer.pos((double)left, (double)bottom, 0.0D).endVertex();
        worldrenderer.pos((double)right, (double)bottom, 0.0D).endVertex();
        worldrenderer.pos((double)right, (double)top, 0.0D).endVertex();
        worldrenderer.pos((double)left, (double)top, 0.0D).endVertex();
        tessellator.draw();
        enableTexture2D();
        disableBlend();
    }

    public static void newDrawRect(double left, double top, double right, double bottom, int color)
    {
        if (left < right)
        {
            double i = left;
            left = right;
            right = i;
        }

        if (top < bottom)
        {
            double j = top;
            top = bottom;
            bottom = j;
        }

        float f3 = (float)(color >> 24 & 255) / 255.0F;
        float f = (float)(color >> 16 & 255) / 255.0F;
        float f1 = (float)(color >> 8 & 255) / 255.0F;
        float f2 = (float)(color & 255) / 255.0F;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(f, f1, f2, f3);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION);
        worldrenderer.pos(left, bottom, 0.0D).endVertex();
        worldrenderer.pos(right, bottom, 0.0D).endVertex();
        worldrenderer.pos(right, top, 0.0D).endVertex();
        worldrenderer.pos(left, top, 0.0D).endVertex();
        tessellator.draw();
        enableTexture2D();
        disableBlend();
    }

    public static void drawRoundedRect(float paramXStart, float paramYStart, float paramXEnd, float paramYEnd, float radius, int color, boolean popPush) {
        float alpha = (color >> 24 & 0xFF) / 255.0F;
        float red = (color >> 16 & 0xFF) / 255.0F;
        float green = (color >> 8 & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;

        float z = 0;
        if (paramXStart > paramXEnd) {
            z = paramXStart;
            paramXStart = paramXEnd;
            paramXEnd = z;
        }

        if (paramYStart > paramYEnd) {
            z = paramYStart;
            paramYStart = paramYEnd;
            paramYEnd = z;
        }

    	double x1 = (double)(paramXStart + radius);
    	double y1 = (double)(paramYStart + radius);
    	double x2 = (double)(paramXEnd - radius);
    	double y2 = (double)(paramYEnd - radius);

        if (popPush) glPushMatrix();
        glEnable(GL_BLEND);
        glDisable(GL_TEXTURE_2D);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_LINE_SMOOTH);
        glLineWidth(1);

    	glColor4f(red, green, blue, alpha);
        glBegin(GL_POLYGON);

        double degree = Math.PI / 180;
        for (double i = 0; i <= 90; i += 1)
            glVertex2d(x2 + Math.sin(i * degree) * radius, y2 + Math.cos(i * degree) * radius);
        for (double i = 90; i <= 180; i += 1)
            glVertex2d(x2 + Math.sin(i * degree) * radius, y1 + Math.cos(i * degree) * radius);
        for (double i = 180; i <= 270; i += 1)
            glVertex2d(x1 + Math.sin(i * degree) * radius, y1 + Math.cos(i * degree) * radius);
        for (double i = 270; i <= 360; i += 1)
            glVertex2d(x1 + Math.sin(i * degree) * radius, y2 + Math.cos(i * degree) * radius);
        glEnd();

        glEnable(GL_TEXTURE_2D);
        glDisable(GL_BLEND);
        glDisable(GL_LINE_SMOOTH);
        if (popPush) glPopMatrix();
    }

    public static void drawGradientHorizontal(final float x, final float y, final float width, final float height, final float radius, final Color left, final Color right) {
        drawGradientRound(x, y, width, height, radius, left, left, right, right);
    }

    public static void drawGradientRound(final float x, final float y, final float width, final float height, final float radius, final Color bottomLeft, final Color topLeft, final Color bottomRight, final Color topRight) {
        RenderUtils.resetColor();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 771);
        Shader.drawQuads(x - 1.0f, y - 1.0f, width + 2.0f, height + 2.0f);
        GlStateManager.disableBlend();
    }

    // rTL = radius top left, rTR = radius top right, rBR = radius bottom right, rBL = radius bottom left
    public static void customRounded(float paramXStart, float paramYStart, float paramXEnd, float paramYEnd, float rTL, float rTR, float rBR, float rBL, int color) {
        float alpha = (color >> 24 & 0xFF) / 255.0F;
        float red = (color >> 16 & 0xFF) / 255.0F;
        float green = (color >> 8 & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;

        float z = 0;
        if (paramXStart > paramXEnd) {
            z = paramXStart;
            paramXStart = paramXEnd;
            paramXEnd = z;
        }

        if (paramYStart > paramYEnd) {
            z = paramYStart;
            paramYStart = paramYEnd;
            paramYEnd = z;
        }

        double xTL = paramXStart + rTL;
        double yTL = paramYStart + rTL;

        double xTR = paramXEnd - rTR;
        double yTR = paramYStart + rTR;

        double xBR = paramXEnd - rBR;
        double yBR = paramYEnd - rBR;

        double xBL = paramXStart + rBL;
        double yBL = paramYEnd - rBL;

        glPushMatrix();
        glEnable(GL_BLEND);
        glDisable(GL_TEXTURE_2D);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_LINE_SMOOTH);
        glLineWidth(1);

    	glColor4f(red, green, blue, alpha);
        glBegin(GL_POLYGON);
    
        double degree = Math.PI / 180;
        if (rBR <= 0)
            glVertex2d(xBR, yBR);
        else for (double i = 0; i <= 90; i += 1)
            glVertex2d(xBR + Math.sin(i * degree) * rBR, yBR + Math.cos(i * degree) * rBR);

        if (rTR <= 0)
            glVertex2d(xTR, yTR);
        else for (double i = 90; i <= 180; i += 1)
            glVertex2d(xTR + Math.sin(i * degree) * rTR, yTR + Math.cos(i * degree) * rTR);

        if (rTL <= 0)
            glVertex2d(xTL, yTL);
        else for (double i = 180; i <= 270; i += 1)
            glVertex2d(xTL + Math.sin(i * degree) * rTL, yTL + Math.cos(i * degree) * rTL);

        if (rBL <= 0)
            glVertex2d(xBL, yBL);
        else for (double i = 270; i <= 360; i += 1)
            glVertex2d(xBL + Math.sin(i * degree) * rBL, yBL + Math.cos(i * degree) * rBL);
        glEnd();

        glEnable(GL_TEXTURE_2D);
        glDisable(GL_BLEND);
        glDisable(GL_LINE_SMOOTH);
        glPopMatrix();
    }

    public static void fastRoundedRect(float paramXStart, float paramYStart, float paramXEnd, float paramYEnd, float radius) {
        float z = 0;
        if (paramXStart > paramXEnd) {
            z = paramXStart;
            paramXStart = paramXEnd;
            paramXEnd = z;
        }

        if (paramYStart > paramYEnd) {
            z = paramYStart;
            paramYStart = paramYEnd;
            paramYEnd = z;
        }

    	double x1 = (double)(paramXStart + radius);
    	double y1 = (double)(paramYStart + radius);
    	double x2 = (double)(paramXEnd - radius);
    	double y2 = (double)(paramYEnd - radius);

        glEnable(GL_LINE_SMOOTH);
        glLineWidth(1);

        glBegin(GL_POLYGON);
    
        double degree = Math.PI / 180;
        for (double i = 0; i <= 90; i += 1)
            glVertex2d(x2 + Math.sin(i * degree) * radius, y2 + Math.cos(i * degree) * radius);
        for (double i = 90; i <= 180; i += 1)
            glVertex2d(x2 + Math.sin(i * degree) * radius, y1 + Math.cos(i * degree) * radius);
        for (double i = 180; i <= 270; i += 1)
            glVertex2d(x1 + Math.sin(i * degree) * radius, y1 + Math.cos(i * degree) * radius);
        for (double i = 270; i <= 360; i += 1)
            glVertex2d(x1 + Math.sin(i * degree) * radius, y2 + Math.cos(i * degree) * radius);
        glEnd();
        glDisable(GL_LINE_SMOOTH);
    }

    public static void drawTriAngle(float cx, float cy, float r, float n, Color color, boolean polygon) {
        cx *= 2.0;
        cy *= 2.0;
        double b = 6.2831852 / n;
        double p = Math.cos(b);
        double s = Math.sin(b);
        r *= 2.0;
        double x = r;
        double y = 0.0;

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        glLineWidth(1F);
        enableGlCap(GL_LINE_SMOOTH);
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.resetColor();
        glColor(color);
        GlStateManager.scale(0.5f, 0.5f, 0.5f);
        worldrenderer.begin(polygon ? GL_POLYGON : 2, DefaultVertexFormats.POSITION);
        int ii = 0;
        while (ii < n) {
            worldrenderer.pos((double)x + cx, (double)y + cy, 0.0D).endVertex();
            double t = x;
            x = p * x - s * y;
            y = s * t + p * y;
            ii++;
        }
        tessellator.draw();
        enableTexture2D();
        disableBlend();
        GlStateManager.scale(2f, 2f, 2f);
        GlStateManager.color(1, 1, 1, 1);
    }

    public static void drawGradientSideways(final double left, final double top, final double right, final double bottom, final int col1, final int col2) {
        final float f = (col1 >> 24 & 0xFF) / 255.0f;
        final float f2 = (col1 >> 16 & 0xFF) / 255.0f;
        final float f3 = (col1 >> 8 & 0xFF) / 255.0f;
        final float f4 = (col1 & 0xFF) / 255.0f;
        final float f5 = (col2 >> 24 & 0xFF) / 255.0f;
        final float f6 = (col2 >> 16 & 0xFF) / 255.0f;
        final float f7 = (col2 >> 8 & 0xFF) / 255.0f;
        final float f8 = (col2 & 0xFF) / 255.0f;
        glEnable(3042);
        glDisable(3553);
        glBlendFunc(770, 771);
        glEnable(2848);
        glShadeModel(7425);
        glPushMatrix();
        glBegin(7);
        glColor4f(f2, f3, f4, f);
        glVertex2d(left, top);
        glVertex2d(left, bottom);
        glColor4f(f6, f7, f8, f5);
        glVertex2d(right, bottom);
        glVertex2d(right, top);
        glEnd();
        glPopMatrix();
        glEnable(3553);
        glDisable(3042);
        glDisable(2848);
        glShadeModel(7424);
    }
    
    public static void drawGradientRect(int left, int top, int right, int bottom, int startColor, int endColor)
    {
        float f = (float)(startColor >> 24 & 255) / 255.0F;
        float f1 = (float)(startColor >> 16 & 255) / 255.0F;
        float f2 = (float)(startColor >> 8 & 255) / 255.0F;
        float f3 = (float)(startColor & 255) / 255.0F;
        float f4 = (float)(endColor >> 24 & 255) / 255.0F;
        float f5 = (float)(endColor >> 16 & 255) / 255.0F;
        float f6 = (float)(endColor >> 8 & 255) / 255.0F;
        float f7 = (float)(endColor & 255) / 255.0F;
        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(7425);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos((double)right, (double)top, (double)zLevel).color(f1, f2, f3, f).endVertex();
        worldrenderer.pos((double)left, (double)top, (double)zLevel).color(f1, f2, f3, f).endVertex();
        worldrenderer.pos((double)left, (double)bottom, (double)zLevel).color(f5, f6, f7, f4).endVertex();
        worldrenderer.pos((double)right, (double)bottom, (double)zLevel).color(f5, f6, f7, f4).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(7424);
        disableBlend();
        GlStateManager.enableAlpha();
        enableTexture2D();
        GlStateManager.popMatrix();
    }

    public static void drawGradientSideways(final float left, final float top, final float right, final float bottom, final int col1, final int col2) {
        final float f = (col1 >> 24 & 0xFF) / 255.0f;
        final float f2 = (col1 >> 16 & 0xFF) / 255.0f;
        final float f3 = (col1 >> 8 & 0xFF) / 255.0f;
        final float f4 = (col1 & 0xFF) / 255.0f;
        final float f5 = (col2 >> 24 & 0xFF) / 255.0f;
        final float f6 = (col2 >> 16 & 0xFF) / 255.0f;
        final float f7 = (col2 >> 8 & 0xFF) / 255.0f;
        final float f8 = (col2 & 0xFF) / 255.0f;
        glEnable(3042);
        glDisable(3553);
        glBlendFunc(770, 771);
        glEnable(2848);
        glShadeModel(7425);
        glPushMatrix();
        glBegin(7);
        glColor4f(f2, f3, f4, f);
        glVertex2f(left, top);
        glVertex2f(left, bottom);
        glColor4f(f6, f7, f8, f5);
        glVertex2f(right, bottom);
        glVertex2f(right, top);
        glEnd();
        glPopMatrix();
        glEnable(3553);
        glDisable(3042);
        glDisable(2848);
        glShadeModel(7424);
    }

    public static void drawBlockBox(final BlockPos blockPos, final Color color, final boolean outline) {
        final RenderManager renderManager = mc.getRenderManager();
        final Timer timer = mc.timer;

        final double x = blockPos.getX() - renderManager.renderPosX;
        final double y = blockPos.getY() - renderManager.renderPosY;
        final double z = blockPos.getZ() - renderManager.renderPosZ;

        AxisAlignedBB axisAlignedBB = new AxisAlignedBB(x, y, z, x + 1.0, y + 1, z + 1.0);
        final Block block = BlockUtils.getBlock(blockPos);

        if (block != null) {
            final EntityPlayer player = mc.thePlayer;

            final double posX = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double) timer.renderPartialTicks;
            final double posY = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double) timer.renderPartialTicks;
            final double posZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double) timer.renderPartialTicks;
            axisAlignedBB = block.getSelectedBoundingBox(mc.theWorld, blockPos)
                    .expand(0.0020000000949949026D, 0.0020000000949949026D, 0.0020000000949949026D)
                    .offset(-posX, -posY, -posZ);
        }

        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        enableGlCap(GL_BLEND);
        disableGlCap(GL_TEXTURE_2D, GL_DEPTH_TEST);
        glDepthMask(false);

        glColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha() != 255 ? color.getAlpha() : outline ? 26 : 35);
        drawFilledBox(axisAlignedBB);

        if (outline) {
            glLineWidth(1F);
            enableGlCap(GL_LINE_SMOOTH);
            glColor(color);

            drawSelectionBoundingBox(axisAlignedBB);
        }

        GlStateManager.resetColor();
        glDepthMask(true);
        resetCaps();
    }

    public static void drawSelectionBoundingBox(AxisAlignedBB boundingBox) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();

        worldrenderer.begin(GL_LINE_STRIP, DefaultVertexFormats.POSITION);

        // Lower Rectangle
        worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex();
        worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex();
        worldrenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex();
        worldrenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex();
        worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex();

        // Upper Rectangle
        worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex();
        worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex();
        worldrenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex();
        worldrenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex();
        worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex();

        // Upper Rectangle
        worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex();
        worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex();

        worldrenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex();
        worldrenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex();

        worldrenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex();
        worldrenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex();

        tessellator.draw();
    }

    public static void drawEntityBox(final Entity entity, final Color color, final boolean outline) {
        final RenderManager renderManager = mc.getRenderManager();
        final Timer timer = mc.timer;

        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        enableGlCap(GL_BLEND);
        disableGlCap(GL_TEXTURE_2D, GL_DEPTH_TEST);
        glDepthMask(false);

        final double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * timer.renderPartialTicks
                - renderManager.renderPosX;
        final double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * timer.renderPartialTicks
                - renderManager.renderPosY;
        final double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * timer.renderPartialTicks
                - renderManager.renderPosZ;

        final AxisAlignedBB entityBox = entity.getEntityBoundingBox();
        final AxisAlignedBB axisAlignedBB = new AxisAlignedBB(
                entityBox.minX - entity.posX + x - 0.05D,
                entityBox.minY - entity.posY + y,
                entityBox.minZ - entity.posZ + z - 0.05D,
                entityBox.maxX - entity.posX + x + 0.05D,
                entityBox.maxY - entity.posY + y + 0.15D,
                entityBox.maxZ - entity.posZ + z + 0.05D
        );

        if (outline) {
            glLineWidth(1F);
            enableGlCap(GL_LINE_SMOOTH);
            glColor(color.getRed(), color.getGreen(), color.getBlue(), 95);
            drawSelectionBoundingBox(axisAlignedBB);
        }

        glColor(color.getRed(), color.getGreen(), color.getBlue(), outline ? 26 : 35);
        drawFilledBox(axisAlignedBB);
        GlStateManager.resetColor();
        glDepthMask(true);
        resetCaps();
    }

    public static void drawAxisAlignedBB(final AxisAlignedBB axisAlignedBB, final Color color) {
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_BLEND);
        glLineWidth(2F);
        glDisable(GL_TEXTURE_2D);
        glDisable(GL_DEPTH_TEST);
        glDepthMask(false);
        glColor(color);
        drawFilledBox(axisAlignedBB);
        GlStateManager.resetColor();
        glEnable(GL_TEXTURE_2D);
        glEnable(GL_DEPTH_TEST);
        glDepthMask(true);
        glDisable(GL_BLEND);
    }

    public static void drawPlatform(final double y, final Color color, final double size) {
        final RenderManager renderManager = mc.getRenderManager();
        final double renderY = y - renderManager.renderPosY;

        drawAxisAlignedBB(new AxisAlignedBB(size, renderY + 0.02D, size, -size, renderY, -size), color);
    }

    public static void drawPlatform(final Entity entity, final Color color) {
        final RenderManager renderManager = mc.getRenderManager();
        final Timer timer = mc.timer;

        TargetMark targetMark = LiquidBounce.moduleManager.getModule(TargetMark.class);
        if (targetMark == null)
            return;

        final double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * timer.renderPartialTicks
                - renderManager.renderPosX;
        final double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * timer.renderPartialTicks
                - renderManager.renderPosY;
        final double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * timer.renderPartialTicks
                - renderManager.renderPosZ;

        final AxisAlignedBB axisAlignedBB = entity.getEntityBoundingBox()
                .offset(-entity.posX, -entity.posY, -entity.posZ)
                .offset(x, y - targetMark.moveMarkValue.get(), z);

        drawAxisAlignedBB(
                new AxisAlignedBB(axisAlignedBB.minX, axisAlignedBB.maxY + 0.2, axisAlignedBB.minZ, axisAlignedBB.maxX, axisAlignedBB.maxY + 0.26, axisAlignedBB.maxZ),
                color
        );
    }

    public static void drawFilledBox(final AxisAlignedBB axisAlignedBB) {
        final Tessellator tessellator = Tessellator.getInstance();
        final WorldRenderer worldRenderer = tessellator.getWorldRenderer();

        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();

        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();

        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();

        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();

        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();

        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        tessellator.draw();
    }
    private static final IntBuffer viewport = BufferUtils.createIntBuffer(16);
    private final static FloatBuffer modelview = GLAllocation.createDirectFloatBuffer(16);
    private final static FloatBuffer projections = GLAllocation.createDirectFloatBuffer(16);

    public static ScaledResolution getResolution() {
        return new ScaledResolution(mc);
    }
    public static javax.vecmath.Vector3d project(double x, double y, double z) {
        FloatBuffer vector = GLAllocation.createDirectFloatBuffer(4);
        GL11.glGetFloat(2982, modelview);
        GL11.glGetFloat(2983, projections);
        GL11.glGetInteger(2978, viewport);
        if (GLU.gluProject((float) x, (float) y, (float) z, modelview, projections, viewport, vector)) {
            return new Vector3d(vector.get(0) / getResolution().getScaleFactor(), (Display.getHeight() - vector.get(1)) / getResolution().getScaleFactor(), vector.get(2));
        }
        return null;
    }    public static void Nametags(double x, double y, double width, double height, int color) {
        float f = (color >> 24 & 0xFF) / 255.0F;
        float f1 = (color >> 16 & 0xFF) / 255.0F;
        float f2 = (color >> 8 & 0xFF) / 255.0F;
        float f3 = (color & 0xFF) / 255.0F;
        GL11.glColor4f(f1, f2, f3, f);
        Nametags1(x, y, x + width, y + height, color);
    }
    public static void Nametags1(double left, double top, double right, double bottom, int color) {
        if (left < right)
        {
            int i = (int)left;
            left = right;
            right = i;
        }

        if (top < bottom)
        {
            int j = (int)top;
            top = bottom;
            bottom = j;
        }

        float f3 = (float)(color >> 24 & 255) / 255.0F;
        float f = (float)(color >> 16 & 255) / 255.0F;
        float f1 = (float)(color >> 8 & 255) / 255.0F;
        float f2 = (float)(color & 255) / 255.0F;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(f, f1, f2, f3);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION);
        worldrenderer.pos(left, bottom, 0.0D).endVertex();
        worldrenderer.pos(right, bottom, 0.0D).endVertex();
        worldrenderer.pos(right, top, 0.0D).endVertex();
        worldrenderer.pos(left, top, 0.0D).endVertex();
        tessellator.draw();
        enableTexture2D();
        disableBlend();
    }
    public static void drawArrow(double x, double y, int lineWidth, int color, double length) {
        start2D();
        GL11.glPushMatrix();
        GL11.glLineWidth(lineWidth);
        setColor(new Color(color));
        GL11.glBegin(GL_LINE_STRIP);
        GL11.glVertex2d(x, y);
        GL11.glVertex2d(x + 3, y + length);
        GL11.glVertex2d(x + 3 * 2, y);
        GL11.glEnd();
        GL11.glPopMatrix();
        stop2D();
    }
    public static void drawEntityOnScreen(final double posX, final double posY, final float scale, final EntityLivingBase entity) {
        GlStateManager.pushMatrix();
        GlStateManager.enableColorMaterial();

        GlStateManager.translate(posX, posY, 50.0);
        GlStateManager.scale((-scale), scale, scale);
        GlStateManager.rotate(180F, 0F, 0F, 1F);
        GlStateManager.rotate(135F, 0F, 1F, 0F);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.rotate(-135F, 0F, 1F, 0F);
        GlStateManager.translate(0.0, 0.0, 0.0);

        RenderManager rendermanager = mc.getRenderManager();
        rendermanager.setPlayerViewY(180F);
        rendermanager.setRenderShadow(false);
        rendermanager.renderEntityWithPosYaw(entity, 0.0, 0.0, 0.0, 0F, 1F);
        rendermanager.setRenderShadow(true);

        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }

    public static void drawEntityOnScreen(final int posX, final int posY, final int scale, final EntityLivingBase entity) {
        drawEntityOnScreen(posX, posY, (float) scale, entity);
    }

    public static void quickDrawRect(final float x, final float y, final float x2, final float y2) {
        glBegin(GL_QUADS);

        glVertex2d(x2, y);
        glVertex2d(x, y);
        glVertex2d(x, y2);
        glVertex2d(x2, y2);

        glEnd();
    }

    public static void drawRect(final float x, final float y, final float x2, final float y2, final int color) {
        glPushMatrix();
        glEnable(GL_BLEND);
        glDisable(GL_TEXTURE_2D);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_LINE_SMOOTH);

        glColor(color);
        glBegin(GL_QUADS);

        glVertex2d(x2, y);
        glVertex2d(x, y);
        glVertex2d(x, y2);
        glVertex2d(x2, y2);
        glEnd();

        glEnable(GL_TEXTURE_2D);
        glDisable(GL_BLEND);
        glDisable(GL_LINE_SMOOTH);
        glPopMatrix();
    }

    public static void drawRect(double left, double top, double right, double bottom, int color)
    {
        if (left < right)
        {
            double i = left;
            left = right;
            right = i;
        }

        if (top < bottom)
        {
            double j = top;
            top = bottom;
            bottom = j;
        }

        float f3 = (float)(color >> 24 & 255) / 255.0F;
        float f = (float)(color >> 16 & 255) / 255.0F;
        float f1 = (float)(color >> 8 & 255) / 255.0F;
        float f2 = (float)(color & 255) / 255.0F;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(f, f1, f2, f3);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION);
        worldrenderer.pos(left, bottom, 0.0D).endVertex();
        worldrenderer.pos(right, bottom, 0.0D).endVertex();
        worldrenderer.pos(right, top, 0.0D).endVertex();
        worldrenderer.pos(left, top, 0.0D).endVertex();
        tessellator.draw();
        enableTexture2D();
        disableBlend();
    }

    /**
     * Like {@link #drawRect(float, float, float, float, int)}, but without setup
     */
    public static void quickDrawRect(final float x, final float y, final float x2, final float y2, final int color) {
        glColor(color);
        glBegin(GL_QUADS);

        glVertex2d(x2, y);
        glVertex2d(x, y);
        glVertex2d(x, y2);
        glVertex2d(x2, y2);

        glEnd();
    }

    public static void drawRect(final float x, final float y, final float x2, final float y2, final Color color) {
        drawRect(x, y, x2, y2, color.getRGB());
    }

    public static void drawBorderedRect(final float x, final float y, final float x2, final float y2, final float width,
                                        final int color1, final int color2) {
        drawRect(x, y, x2, y2, color2);
        drawBorder(x, y, x2, y2, width, color1);
    }

    public static void drawBorder(float x, float y, float x2, float y2, float width, int color1) {
        glEnable(GL_BLEND);
        glDisable(GL_TEXTURE_2D);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_LINE_SMOOTH);

        glColor(color1);
        glLineWidth(width);

        glBegin(GL_LINE_LOOP);

        glVertex2d(x2, y);
        glVertex2d(x, y);
        glVertex2d(x, y2);
        glVertex2d(x2, y2);

        glEnd();

        glEnable(GL_TEXTURE_2D);
        glDisable(GL_BLEND);
        glDisable(GL_LINE_SMOOTH);
    }

    public static void drawRectBasedBorder(float x, float y, float x2, float y2, float width, int color1) {
        drawRect(x - width / 2F, y - width / 2F, x2 + width / 2F, y + width / 2F, color1);
        drawRect(x - width / 2F, y + width / 2F, x + width / 2F, y2 + width / 2F, color1);
        drawRect(x2 - width / 2F, y + width / 2F, x2 + width / 2F, y2 + width / 2F, color1);
        drawRect(x + width / 2F, y2 - width / 2F, x2 - width / 2F, y2 + width / 2F, color1);
    }

    public static void drawRectBasedBorder(double x, double y, double x2, double y2, double width, int color1) {
        newDrawRect(x - width / 2F, y - width / 2F, x2 + width / 2F, y + width / 2F, color1);
        newDrawRect(x - width / 2F, y + width / 2F, x + width / 2F, y2 + width / 2F, color1);
        newDrawRect(x2 - width / 2F, y + width / 2F, x2 + width / 2F, y2 + width / 2F, color1);
        newDrawRect(x + width / 2F, y2 - width / 2F, x2 - width / 2F, y2 + width / 2F, color1);
    }

    public static void quickDrawBorderedRect(final float x, final float y, final float x2, final float y2, final float width, final int color1, final int color2) {
        quickDrawRect(x, y, x2, y2, color2);

        glColor(color1);
        glLineWidth(width);

        glBegin(GL_LINE_LOOP);

        glVertex2d(x2, y);
        glVertex2d(x, y);
        glVertex2d(x, y2);
        glVertex2d(x2, y2);

        glEnd();
    }

    public static void drawLoadingCircle(float x, float y) {
        for (int i = 0; i < 4; i++) {
            int rot = (int) ((System.nanoTime() / 5000000 * i) % 360);
            drawCircle(x, y, i * 10, rot - 180, rot);
        }
    }

    public static void drawCheck(double x, double y, int lineWidth, int color) {
        start2D();
        GL11.glPushMatrix();
        GL11.glLineWidth(lineWidth);
        setColor(new Color(color));
        GL11.glBegin(GL_LINE_STRIP);
        GL11.glVertex2d(x, y);
        GL11.glVertex2d(x + 2, y + 3);
        GL11.glVertex2d(x + 6, y - 2);
        GL11.glEnd();
        GL11.glPopMatrix();
        stop2D();
    }
    public static void start2D() {
        GL11.glEnable(3042);
        GL11.glDisable(3553);
        glBlendFunc(770, 771);
        GL11.glEnable(2848);
    }

    public static void stop2D() {
        GL11.glEnable(3553);
        GL11.glDisable(3042);
        GL11.glDisable(2848);
        enableTexture2D();
        disableBlend();
        glColor4f(1, 1, 1, 1);
    }

    public static void drawCircle(float x, float y, float radius, float lineWidth, int start, int end, Color color) {
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
        glColor(color);

        glEnable(GL_LINE_SMOOTH);
        glLineWidth(lineWidth);
        glBegin(GL_LINE_STRIP);
        for (float i = end; i >= start; i -= (360 / 90.0f)) {
            glVertex2f((float) (x + (cos(i * PI / 180) * (radius * 1.001F))), (float) (y + (sin(i * PI / 180) * (radius * 1.001F))));
        }
        glEnd();
        glDisable(GL_LINE_SMOOTH);

        enableTexture2D();
        disableBlend();
    }

    public static void drawOutlinedStringCock(FontRenderer fr, String s, float x, float y, int color, int outlineColor) {
        fr.drawString(ColorUtils.stripColor(s), (int) (x - 1.0f), (int) y, outlineColor);
        fr.drawString(ColorUtils.stripColor(s), (int) x, (int) (y - 1.0f), outlineColor);
        fr.drawString(ColorUtils.stripColor(s), (int) (x + 1.0f), (int) y, outlineColor);
        fr.drawString(ColorUtils.stripColor(s), (int) x, (int) (y + 1.0f), outlineColor);
        fr.drawString(s, (int) x, (int) y, color);
    }

    private static void drawEnchantTag(String text, int x, float y) {
        GlStateManager.pushMatrix();
        GlStateManager.disableDepth();
        GL11.glScalef(0.5f, 0.5f, 0.5f);
        drawOutlinedStringCock(Minecraft.getMinecraft().fontRendererObj, text, x, y, -1, new Color(0, 0, 0, 220).darker().getRGB());
        GL11.glScalef(1.0f, 1.0f, 1.0f);
        GlStateManager.enableDepth();
        GlStateManager.popMatrix();
    }

    public static void renderEnchantText(ItemStack stack, int x, float y) {
        RenderHelper.disableStandardItemLighting();
        float enchantmentY = y + 24f;
        if (stack.getItem() instanceof ItemArmor) {
            int protectionLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.protection.effectId, stack);
            int unbreakingLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack);
            int thornLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.thorns.effectId, stack);
            if (protectionLevel > 0) {
                RenderUtils.drawEnchantTag("P" + ColorUtils.getColor(protectionLevel) + protectionLevel, x * 2, enchantmentY);
                enchantmentY += 8;
            }
            if (unbreakingLevel > 0) {
                RenderUtils.drawEnchantTag("U" + ColorUtils.getColor(unbreakingLevel) + unbreakingLevel, x * 2, enchantmentY);
                enchantmentY += 8;
            }
            if (thornLevel > 0) {
                RenderUtils.drawEnchantTag("T" + ColorUtils.getColor(thornLevel) + thornLevel, x * 2, enchantmentY);
                enchantmentY += 8;
            }
        }
        if (stack.getItem() instanceof ItemBow) {
            int powerLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, stack);
            int punchLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.punch.effectId, stack);
            int flameLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, stack);
            int unbreakingLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack);
            if (powerLevel > 0) {
                RenderUtils.drawEnchantTag("Pow" + ColorUtils.getColor(powerLevel) + powerLevel, x * 2, enchantmentY);
                enchantmentY += 8;
            }
            if (punchLevel > 0) {
                RenderUtils.drawEnchantTag("Pun" + ColorUtils.getColor(punchLevel) + punchLevel, x * 2, enchantmentY);
                enchantmentY += 8;
            }
            if (flameLevel > 0) {
                RenderUtils.drawEnchantTag("F" + ColorUtils.getColor(flameLevel) + flameLevel, x * 2, enchantmentY);
                enchantmentY += 8;
            }
            if (unbreakingLevel > 0) {
                RenderUtils.drawEnchantTag("U" + ColorUtils.getColor(unbreakingLevel) + unbreakingLevel, x * 2, enchantmentY);
                enchantmentY += 8;
            }
        }
        if (stack.getItem() instanceof ItemSword) {
            int sharpnessLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, stack);
            int knockbackLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.knockback.effectId, stack);
            int fireAspectLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.fireAspect.effectId, stack);
            int unbreakingLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack);
            if (sharpnessLevel > 0) {
                RenderUtils.drawEnchantTag("S" +  ColorUtils.getColor(sharpnessLevel) + sharpnessLevel, x * 2, enchantmentY);
                enchantmentY += 8;
            }
            if (knockbackLevel > 0) {
                RenderUtils.drawEnchantTag("K" + ColorUtils.getColor(knockbackLevel) + knockbackLevel, x * 2, enchantmentY);
                enchantmentY += 8;
            }
            if (fireAspectLevel > 0) {
                RenderUtils.drawEnchantTag("F" + ColorUtils.getColor(fireAspectLevel) + fireAspectLevel, x * 2, enchantmentY);
                enchantmentY += 8;
            }
            if (unbreakingLevel > 0) {
                RenderUtils.drawEnchantTag("U" + ColorUtils.getColor(unbreakingLevel) + unbreakingLevel, x * 2, enchantmentY);
                enchantmentY += 8;
            }
        }
        if (stack.getRarity() == EnumRarity.EPIC) {
            GlStateManager.pushMatrix();
            GlStateManager.disableDepth();
            GL11.glScalef(0.5f, 0.5f, 0.5f);
            drawOutlinedStringCock(Minecraft.getMinecraft().fontRendererObj, "God", x * 2, enchantmentY, new Color(255, 255, 0).getRGB(), new Color(100, 100, 0, 200).getRGB());
            GL11.glScalef(1.0f, 1.0f, 1.0f);
            GlStateManager.enableDepth();
            GlStateManager.popMatrix();
        }
    }

    public static void skeetRectSmall(final double x, final double y, final double x1, final double y1, final double size) {
        RenderUtils.rectangleBordered(x + 4.35, y + 0.5, x1 + size - 84.5, y1 + size - 4.35, 0.5, new Color(48, 48, 48).getRGB(), new Color(10, 10, 10).getRGB());
        RenderUtils.rectangleBordered(x + 5.0, y + 1.0, x1 + size - 85.0, y1 + size - 5.0, 0.5, new Color(17, 17, 17).getRGB(), new Color(255, 255, 255, 0).getRGB());
    }
    public static void rectangleBordered(double x, double y, double x1, double y1, double width, int internalColor,
                                         int borderColor) {
        rectangle(x + width, y + width, x1 - width, y1 - width, internalColor);
        GlStateManager.color((float) 1.0f, (float) 1.0f, (float) 1.0f, (float) 1.0f);
        rectangle(x + width, y, x1 - width, y + width, borderColor);
        GlStateManager.color((float) 1.0f, (float) 1.0f, (float) 1.0f, (float) 1.0f);
        rectangle(x, y, x + width, y1, borderColor);
        GlStateManager.color((float) 1.0f, (float) 1.0f, (float) 1.0f, (float) 1.0f);
        rectangle(x1 - width, y, x1, y1, borderColor);
        GlStateManager.color((float) 1.0f, (float) 1.0f, (float) 1.0f, (float) 1.0f);
        rectangle(x + width, y1 - width, x1 - width, y1, borderColor);
        GlStateManager.color((float) 1.0f, (float) 1.0f, (float) 1.0f, (float) 1.0f);
    }
    public static void drawScaledCustomSizeModalRect(int x, int y, float u, float v, int uWidth, int vHeight, int width, int height, float tileWidth, float tileHeight) {
        float f = 1.0F / tileWidth;
        float f1 = 1.0F / tileHeight;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(x, y + height, 0.0D).tex(u * f, (v + (float) vHeight) * f1).endVertex();
        worldrenderer.pos(x + width, y + height, 0.0D).tex((u + (float) uWidth) * f, (v + (float) vHeight) * f1).endVertex();
        worldrenderer.pos(x + width, y, 0.0D).tex((u + (float) uWidth) * f, v * f1).endVertex();
        worldrenderer.pos(x, y, 0.0D).tex(u * f, v * f1).endVertex();
        tessellator.draw();
    }    public static double getAnimationStateSmooth(double target, double current, double speed) {
        boolean larger;
        boolean bl = larger = target > current;
        if (speed < 0.0) {
            speed = 0.0;
        } else if (speed > 1.0) {
            speed = 1.0;
        }
        if (target == current){
            return target;
        }
        double dif = Math.max(target, current) - Math.min(target, current);
        double factor = Math.max((dif * speed),1);
        if (factor < 0.1) {
            factor = 0.1;
        }
        if (larger){
            if (current + factor>target){
                current = target;
            }else {
                current += factor;
            }
        }else {
            if (current - factor<target) {
                current = target;
            }else {
                current -= factor;
            }
        }
        return current;
    }

    public static void drawCircleFull(float x, float y ,float radius, float Bord , final Color color) {
        drawCircle(x , y , radius + 0.15f, color);
        drawCircleD(x , y , radius, color.getRGB());
    }

    public static void prepareScissorBox(float x, float y, float x2, float y2) {
        ScaledResolution scale = new ScaledResolution(mc);
        int factor = scale.getScaleFactor();
        GL11.glScissor((int) (x * (float) factor),
                (int) (((float) scale.getScaledHeight() - y2) * (float) factor),
                (int) ((x2 - x) * (float) factor), (int) ((y2 - y) * (float) factor));
    }

    public static int Astolfo(int var2) {
        double v1 = Math.ceil(System.currentTimeMillis() + (long) (var2 * 109)) / 5;
        return Color.getHSBColor((double) ((float) ((v1 %= 360.0) / 360.0)) < 0.5 ? -((float) (v1 / 360.0)) : (float) (v1 / 360.0), 0.5F, 1.0F).getRGB();
    }

    public static void drawCircleD(float x, float y, float radius, int color) {
        float alpha = (float) (color >> 24 & 255) / 255.0f;
        float red = (float) (color >> 16 & 255) / 255.0f;
        float green = (float) (color >> 8 & 255) / 255.0f;
        float blue = (float) (color & 255) / 255.0f;
        boolean blend = GL11.glIsEnabled((int) 3042);
        boolean line = GL11.glIsEnabled((int) 2848);
        boolean texture = GL11.glIsEnabled((int) 3553);
        if (!blend) {
            GL11.glEnable((int) 3042);
        }
        if (!line) {
            GL11.glEnable((int) 2848);
        }
        if (texture) {
            GL11.glDisable((int) 3553);
        }
        GL11.glBlendFunc((int) 770, (int) 771);
        GL11.glColor4f((float) red, (float) green, (float) blue, (float) alpha);
        GL11.glBegin((int) 9);
        int i = 0;
        while (i <= 360) {
            GL11.glVertex2d((double) ((double) x + Math.sin((double) i * 3.141526 / 180.0) * (double) radius), (double) ((double) y + Math.cos((double) i * 3.141526 / 180.0) * (double) radius));
            ++i;
        }
        GL11.glEnd();
        if (texture) {
            GL11.glEnable((int) 3553);
        }
        if (!line) {
            GL11.glDisable((int) 2848);
        }
        if (!blend) {
            GL11.glDisable((int) 3042);
        }
    }

    public static void rectangle(double x, double y, double x2, double y2, int color) {
        glEnable(GL_BLEND);
        glDisable(GL_TEXTURE_2D);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_LINE_SMOOTH);

        glColor(color);
        glBegin(GL_QUADS);

        glVertex2d(x2, y);
        glVertex2d(x, y);
        glVertex2d(x, y2);
        glVertex2d(x2, y2);
        glEnd();

        glEnable(GL_TEXTURE_2D);
        glDisable(GL_BLEND);
        glDisable(GL_LINE_SMOOTH);
    }
    public static void drawCircleRect(float x, float y, float x1, float y1, float radius, int color){
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        glColor(color);

        quickRenderCircle(x1-radius,y1-radius,0,90,radius,radius);
        quickRenderCircle(x+radius,y1-radius,90,180,radius,radius);
        quickRenderCircle(x+radius,y+radius,180,270,radius,radius);
        quickRenderCircle(x1-radius,y+radius,270,360,radius,radius);

        quickDrawRect(x+radius,y+radius,x1-radius,y1-radius);
        quickDrawRect(x,y+radius,x+radius,y1-radius);
        quickDrawRect(x1-radius,y+radius,x1,y1-radius);
        quickDrawRect(x+radius,y,x1-radius,y+radius);
        quickDrawRect(x+radius,y1-radius,x1-radius,y1);

        enableTexture2D();
        disableBlend();
    }

    public static void quickRenderCircle(double x, double y, double start, double end, double w, double h) {
        if (start > end) {
            double temp = end;
            end = start;
            start = temp;
        }

        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glVertex2d(x, y);
        for(double i = end; i >= start; i-=4) {
            double ldx = Math.cos(i * Math.PI / 180.0) * w;
            double ldy = Math.sin(i * Math.PI / 180.0) * h;
            GL11.glVertex2d(x + ldx, y + ldy);
        }
        GL11.glVertex2d(x, y);
        GL11.glEnd();
    }

    public static void drawCircle(float x, float y, float radius, float lineWidth, int start, int end) {
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
        glColor(Color.WHITE);

        glEnable(GL_LINE_SMOOTH);
        glLineWidth(lineWidth);
        glBegin(GL_LINE_STRIP);
        for (float i = end; i >= start; i -= (360 / 90.0f)) {
            glVertex2f((float) (x + (cos(i * PI / 180) * (radius * 1.001F))), (float) (y + (sin(i * PI / 180) * (radius * 1.001F))));
        }
        glEnd();
        glDisable(GL_LINE_SMOOTH);

        enableTexture2D();
        disableBlend();
    }

    public static void drawCircle(float x, float y, float radius, int start, int end) {
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
        glColor(Color.WHITE);

        glEnable(GL_LINE_SMOOTH);
        glLineWidth(2F);
        glBegin(GL_LINE_STRIP);
        for (float i = end; i >= start; i -= (360 / 90.0f)) {
            glVertex2f((float) (x + (cos(i * PI / 180) * (radius * 1.001F))), (float) (y + (sin(i * PI / 180) * (radius * 1.001F))));
        }
        glEnd();
        glDisable(GL_LINE_SMOOTH);

        enableTexture2D();
        disableBlend();
    }

    public static void drawFilledCircle(final int xx, final int yy, final float radius, final Color color) {
        int sections = 50;
        double dAngle = 2 * Math.PI / sections;
        float x, y;

        glPushAttrib(GL_ENABLE_BIT);

        glEnable(GL_BLEND);
        glDisable(GL_TEXTURE_2D);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_LINE_SMOOTH);
        glBegin(GL_TRIANGLE_FAN);

        for (int i = 0; i < sections; i++) {
            x = (float) (radius * Math.sin((i * dAngle)));
            y = (float) (radius * Math.cos((i * dAngle)));

            glColor4f(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, color.getAlpha() / 255F);
            glVertex2f(xx + x, yy + y);
        }

        GlStateManager.color(0, 0, 0);

        glEnd();

        glPopAttrib();
    }

    public static void drawFilledCircle(double x, double y, double r, int c, int id) {
        float f = (float) (c >> 24 & 0xff) / 255F;
        float f1 = (float) (c >> 16 & 0xff) / 255F;
        float f2 = (float) (c >> 8 & 0xff) / 255F;
        float f3 = (float) (c & 0xff) / 255F;
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(f1, f2, f3, f);
        GL11.glBegin(GL11.GL_POLYGON);
        if (id == 1) {
            GL11.glVertex2d(x, y);
            for (int i = 0; i <= 90; i++) {
                double x2 = Math.sin((i * 3.141526D / 180)) * r;
                double y2 = Math.cos((i * 3.141526D / 180)) * r;
                GL11.glVertex2d(x - x2, y - y2);
            }
        } else if (id == 2) {
            GL11.glVertex2d(x, y);
            for (int i = 90; i <= 180; i++) {
                double x2 = Math.sin((i * 3.141526D / 180)) * r;
                double y2 = Math.cos((i * 3.141526D / 180)) * r;
                GL11.glVertex2d(x - x2, y - y2);
            }
        } else if (id == 3) {
            GL11.glVertex2d(x, y);
            for (int i = 270; i <= 360; i++) {
                double x2 = Math.sin((i * 3.141526D / 180)) * r;
                double y2 = Math.cos((i * 3.141526D / 180)) * r;
                GL11.glVertex2d(x - x2, y - y2);
            }
        } else if (id == 4) {
            GL11.glVertex2d(x, y);
            for (int i = 180; i <= 270; i++) {
                double x2 = Math.sin((i * 3.141526D / 180)) * r;
                double y2 = Math.cos((i * 3.141526D / 180)) * r;
                GL11.glVertex2d(x - x2, y - y2);
            }
        } else {
            for (int i = 0; i <= 360; i++) {
                double x2 = Math.sin((i * 3.141526D / 180)) * r;
                double y2 = Math.cos((i * 3.141526D / 180)) * r;
                GL11.glVertex2f((float) (x - x2), (float) (y - y2));
            }
        }
        GL11.glEnd();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
    }

    public static void drawFilledCircle(final float xx, final float yy, final float radius, final Color color) {
        int sections = 50;
        double dAngle = 2 * Math.PI / sections;
        float x, y;

        glPushAttrib(GL_ENABLE_BIT);

        glEnable(GL_BLEND);
        glDisable(GL_TEXTURE_2D);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_LINE_SMOOTH);
        glBegin(GL_TRIANGLE_FAN);

        for (int i = 0; i < sections; i++) {
            x = (float) (radius * Math.sin((i * dAngle)));
            y = (float) (radius * Math.cos((i * dAngle)));

            glColor4f(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, color.getAlpha() / 255F);
            glVertex2f(xx + x, yy + y);
        }

        GlStateManager.color(0, 0, 0);

        glEnd();

        glPopAttrib();
    }

    public static void drawImage(ResourceLocation image, int x, int y, int width, int height) {
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glDepthMask(false);
        OpenGlHelper.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
        glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(image);
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, width, height, width, height);
        glDepthMask(true);
        glDisable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);
    }

    public static void drawImage(ResourceLocation image, int x, int y, int width, int height, float alpha) {
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glDepthMask(false);
        OpenGlHelper.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
        glColor4f(1.0F, 1.0F, 1.0F, alpha);
        mc.getTextureManager().bindTexture(image);
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, width, height, width, height);
        glDepthMask(true);
        glDisable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);
    }

    public static void drawImage2(ResourceLocation image, float x, float y, int width, int height) {
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glDepthMask(false);
        OpenGlHelper.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
        glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        glTranslatef(x, y, x);
        mc.getTextureManager().bindTexture(image);
        Gui.drawModalRectWithCustomSizedTexture(0, 0, 0, 0, width, height, width, height);
        glTranslatef(-x, -y, -x);
        glDepthMask(true);
        glDisable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);
    }

    public static void drawImage3(ResourceLocation image, float x, float y, int width, int height, float r, float g, float b, float al) {
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glDepthMask(false);
        OpenGlHelper.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
        glColor4f(r, g, b, al);
        glTranslatef(x, y, x);
        mc.getTextureManager().bindTexture(image);
        Gui.drawModalRectWithCustomSizedTexture(0, 0, 0, 0, width, height, width, height);
        glTranslatef(-x, -y, -x);
        glDepthMask(true);
        glDisable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);
    }

    public static void drawExhiEnchants(ItemStack stack, float x, float y) {
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableDepth();
        disableBlend();
        GlStateManager.resetColor();
        final int darkBorder = 0xFF000000;
        if (stack.getItem() instanceof ItemArmor) {
            int prot = EnchantmentHelper.getEnchantmentLevel(Enchantment.protection.effectId, stack);
            int unb = EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack);
            int thorn = EnchantmentHelper.getEnchantmentLevel(Enchantment.thorns.effectId, stack);
            if (prot > 0) {
                drawExhiOutlined(prot + "", drawExhiOutlined("P", x, y, 0.35F, darkBorder, -1, true), y, 0.35F, getBorderColor(prot), getMainColor(prot), true);
                y += 4;
            }
            if (unb > 0) {
                drawExhiOutlined(unb + "", drawExhiOutlined("U", x, y, 0.35F, darkBorder, -1, true), y, 0.35F, getBorderColor(unb),getMainColor(unb), true);
                y += 4;
            }
            if (thorn > 0) {
                drawExhiOutlined(thorn + "", drawExhiOutlined("T", x, y, 0.35F, darkBorder, -1, true), y, 0.35F, getBorderColor(thorn), getMainColor(thorn), true);
                y += 4;
            }
        }
        if (stack.getItem() instanceof ItemBow) {
            int power = EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, stack);
            int punch = EnchantmentHelper.getEnchantmentLevel(Enchantment.punch.effectId, stack);
            int flame = EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, stack);
            int unb = EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack);
            if (power > 0) {
                drawExhiOutlined(power + "", drawExhiOutlined("Pow", x, y, 0.35F, darkBorder, -1, true), y, 0.35F, getBorderColor(power), getMainColor(power), true);
                y += 4;
            }
            if (punch > 0) {
                drawExhiOutlined(punch + "", drawExhiOutlined("Pun", x, y, 0.35F, darkBorder, -1, true), y, 0.35F, getBorderColor(punch), getMainColor(punch), true);
                y += 4;
            }
            if (flame > 0) {
                drawExhiOutlined(flame + "", drawExhiOutlined("F", x, y, 0.35F, darkBorder, -1, true), y, 0.35F, getBorderColor(flame), getMainColor(flame), true);
                y += 4;
            }
            if (unb > 0) {
                drawExhiOutlined(unb + "", drawExhiOutlined("U", x, y, 0.35F, darkBorder, -1, true), y, 0.35F, getBorderColor(unb), getMainColor(unb), true);
                y += 4;
            }
        }
        if (stack.getItem() instanceof ItemSword) {
            int sharp = EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, stack);
            int kb = EnchantmentHelper.getEnchantmentLevel(Enchantment.knockback.effectId, stack);
            int fire = EnchantmentHelper.getEnchantmentLevel(Enchantment.fireAspect.effectId, stack);
            int unb = EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack);
            if (sharp > 0) {
                drawExhiOutlined(sharp + "", drawExhiOutlined("S", x, y, 0.35F, darkBorder, -1, true), y, 0.35F, getBorderColor(sharp), getMainColor(sharp), true);
                y += 4;
            }
            if (kb > 0) {
                drawExhiOutlined(kb + "", drawExhiOutlined("K", x, y, 0.35F, darkBorder, -1, true), y, 0.35F, getBorderColor(kb), getMainColor(kb), true);
                y += 4;
            }
            if (fire > 0) {
                drawExhiOutlined(fire + "", drawExhiOutlined("F", x, y, 0.35F, darkBorder, -1, true), y, 0.35F, getBorderColor(fire), getMainColor(fire), true);
                y += 4;
            }
            if (unb > 0) {
                drawExhiOutlined(unb + "", drawExhiOutlined("U", x, y, 0.35F, darkBorder, -1, true), y, 0.35F, getBorderColor(unb), getMainColor(unb), true);
                y += 4;
            }
        }
        GlStateManager.enableDepth();
        RenderHelper.enableGUIStandardItemLighting();
    }

    private static float drawExhiOutlined(String text, float x, float y, float borderWidth, int borderColor, int mainColor, boolean drawText) {
        Fonts.fontTahomaSmall.drawString(text, x, y - borderWidth, borderColor);
        Fonts.fontTahomaSmall.drawString(text, x, y + borderWidth, borderColor);
        Fonts.fontTahomaSmall.drawString(text, x - borderWidth, y, borderColor);
        Fonts.fontTahomaSmall.drawString(text, x + borderWidth, y, borderColor);
        if (drawText)
            Fonts.fontTahomaSmall.drawString(text, x, y, mainColor);
        return x + Fonts.fontTahomaSmall.getWidth(text) - 2F;
    }

    private static int getMainColor(int level) {
        if (level == 4)
            return 0xFFAA0000;
        return -1;
    }

    private static int getBorderColor(int level) {
        if (level == 2)
            return 0x7055FF55;
        if (level == 3)
            return 0x7000AAAA;
        if (level == 4)
            return 0x70AA0000;
        if (level >= 5)
            return 0x70FFAA00;
        return 0x70FFFFFF;
    }

    public static void glColor(final int red, final int green, final int blue, final int alpha) {
        GlStateManager.color(red / 255F, green / 255F, blue / 255F, alpha / 255F);
    }

    public static void glColor(final Color color) {
        final float red = color.getRed() / 255F;
        final float green = color.getGreen() / 255F;
        final float blue = color.getBlue() / 255F;
        final float alpha = color.getAlpha() / 255F;

        GlStateManager.color(red, green, blue, alpha);
    }

    public static void glColor(final int hex) {
        final float alpha = (hex >> 24 & 0xFF) / 255F;
        final float red = (hex >> 16 & 0xFF) / 255F;
        final float green = (hex >> 8 & 0xFF) / 255F;
        final float blue = (hex & 0xFF) / 255F;

        GlStateManager.color(red, green, blue, alpha);
    }

    public static void draw2D(final EntityLivingBase entity, final double posX, final double posY, final double posZ, final int color, final int backgroundColor) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(posX, posY, posZ);
        GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0F, 1F, 0F);
        GlStateManager.scale(-0.1D, -0.1D, 0.1D);

        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glDisable(GL_TEXTURE_2D);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        GlStateManager.depthMask(true);

        glColor(color);

        glCallList(DISPLAY_LISTS_2D[0]);

        glColor(backgroundColor);

        glCallList(DISPLAY_LISTS_2D[1]);

        GlStateManager.translate(0, 21 + -(entity.getEntityBoundingBox().maxY - entity.getEntityBoundingBox().minY) * 12, 0);

        glColor(color);
        glCallList(DISPLAY_LISTS_2D[2]);

        glColor(backgroundColor);
        glCallList(DISPLAY_LISTS_2D[3]);

        // Stop render
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_TEXTURE_2D);
        glDisable(GL_BLEND);

        GlStateManager.popMatrix();
    }

    public static void setColor(Color color) {
        float alpha = (color.getRGB() >> 24 & 0xFF) / 255.0F;
        float red = (color.getRGB() >> 16 & 0xFF) / 255.0F;
        float green = (color.getRGB() >> 8 & 0xFF) / 255.0F;
        float blue = (color.getRGB() & 0xFF) / 255.0F;
        GL11.glColor4f(red, green, blue, alpha);
    }

    public static void draw2D(final BlockPos blockPos, final int color, final int backgroundColor) {
        final RenderManager renderManager = mc.getRenderManager();

        final double posX = (blockPos.getX() + 0.5) - renderManager.renderPosX;
        final double posY = blockPos.getY() - renderManager.renderPosY;
        final double posZ = (blockPos.getZ() + 0.5) - renderManager.renderPosZ;

        GlStateManager.pushMatrix();
        GlStateManager.translate(posX, posY, posZ);
        GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0F, 1F, 0F);
        GlStateManager.scale(-0.1D, -0.1D, 0.1D);

        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glDisable(GL_TEXTURE_2D);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        GlStateManager.depthMask(true);

        glColor(color);

        glCallList(DISPLAY_LISTS_2D[0]);

        glColor(backgroundColor);

        glCallList(DISPLAY_LISTS_2D[1]);

        GlStateManager.translate(0, 9, 0);

        glColor(color);

        glCallList(DISPLAY_LISTS_2D[2]);

        glColor(backgroundColor);

        glCallList(DISPLAY_LISTS_2D[3]);

        // Stop render
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_TEXTURE_2D);
        glDisable(GL_BLEND);

        GlStateManager.popMatrix();
    }

    public static void renderNameTag(final String string, final double x, final double y, final double z) {
        final RenderManager renderManager = mc.getRenderManager();

        glPushMatrix();
        glTranslated(x - renderManager.renderPosX, y - renderManager.renderPosY, z - renderManager.renderPosZ);
        glNormal3f(0F, 1F, 0F);
        glRotatef(-mc.getRenderManager().playerViewY, 0F, 1F, 0F);
        glRotatef(mc.getRenderManager().playerViewX, 1F, 0F, 0F);
        glScalef(-0.05F, -0.05F, 0.05F);
        setGlCap(GL_LIGHTING, false);
        setGlCap(GL_DEPTH_TEST, false);
        setGlCap(GL_BLEND, true);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        final int width = Fonts.font35.getStringWidth(string) / 2;

        Gui.drawRect(-width - 1, -1, width + 1, Fonts.font35.FONT_HEIGHT, Integer.MIN_VALUE);
        Fonts.font35.drawString(string, -width, 1.5F, Color.WHITE.getRGB(), true);

        resetCaps();
        glColor4f(1F, 1F, 1F, 1F);
        glPopMatrix();
    }

    public static void drawLine(final float x, final float y, final float x1, final float y1, final float width) {
        glDisable(GL_TEXTURE_2D);
        glLineWidth(width);
        glBegin(GL_LINES);
        glVertex2f(x, y);
        glVertex2f(x1, y1);
        glEnd();
        glEnable(GL_TEXTURE_2D);
    }

    public static void drawLine(final double x, final double y, final double x1, final double y1, final float width) {
        glDisable(GL_TEXTURE_2D);
        glLineWidth(width);
        glBegin(GL_LINES);
        glVertex2d(x, y);
        glVertex2d(x1, y1);
        glEnd();
        glEnable(GL_TEXTURE_2D);
    }

    public static void makeScissorBox(final float x, final float y, final float x2, final float y2) {
        final ScaledResolution scaledResolution = new ScaledResolution(mc);
        final int factor = scaledResolution.getScaleFactor();
        glScissor((int) (x * factor), (int) ((scaledResolution.getScaledHeight() - y2) * factor), (int) ((x2 - x) * factor), (int) ((y2 - y) * factor));
    }

    /**
     * GL CAP MANAGER
     *
     * TODO: Remove gl cap manager and replace by something better
     */

    public static void resetCaps() {
        glCapMap.forEach(RenderUtils::setGlState);
    }

    public static void enableGlCap(final int cap) {
        setGlCap(cap, true);
    }

    public static void enableGlCap(final int... caps) {
        for (final int cap : caps)
            setGlCap(cap, true);
    }

    public static void disableGlCap(final int cap) {
        setGlCap(cap, true);
    }

    public static void disableGlCap(final int... caps) {
        for (final int cap : caps)
            setGlCap(cap, false);
    }

    public static void setGlCap(final int cap, final boolean state) {
        glCapMap.put(cap, glGetBoolean(cap));
        setGlState(cap, state);
    }

    public static void setGlState(final int cap, final boolean state) {
        if (state)
            glEnable(cap);
        else
            glDisable(cap);
    }
}