package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.skeet.component.impl.sub.color;

import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.skeet.LockedResolution;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.skeet.MathUtils;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.skeet.RenderUtil;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.skeet.SkeetClickGUI;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.skeet.component.ButtonComponent;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.skeet.component.Component;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.skeet.component.ExpandableComponent;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.skeet.component.PredicateComponent;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public abstract class ColorPickerComponent extends ButtonComponent implements PredicateComponent, ExpandableComponent {
    private boolean expanded;
    private float hue;
    private float saturation;
    private float brightness;
    private float alpha;
    private boolean colorSelectorDragging;
    private boolean hueSelectorDragging;
    private boolean alphaSelectorDragging;

    public ColorPickerComponent(Component parent, float x2, float y2, float width, float height) {
        super(parent, x2, y2, width, height);
    }

    private static void drawCheckeredBackground(float x, float y, float x2, float y2) {
        RenderUtils.drawRect(x, y, x2, y2, SkeetClickGUI.getColor(16777215));
        for(boolean offset = false; y < y2; ++y) {
            for(float x1 = x + (float)((offset = !offset) ? 1 : 0); x1 < x2; x1 += 2.0F) {
                if (x1 <= x2 - 1.0F) {
                    RenderUtils.drawRect(x1, y, x1 + 1.0F, y + 1.0F, SkeetClickGUI.getColor(8421504));
                }
            }
        }
    }

    @Override
    public void drawComponent(LockedResolution resolution, int mouseX, int mouseY) {
        float x2 = this.getX();
        float y2 = this.getY();
        float width = this.getWidth();
        float height = this.getHeight();
        int black = SkeetClickGUI.getColor(0);
        RenderUtils.drawRect(x2 - 0.5f, y2 - 0.5f, (x2 + width) + 0.5f, (y2 + height) + 0.5f, black);
        int guiAlpha = (int) SkeetClickGUI.getAlpha();
        int color = this.getColor();
        int colorAlpha = color >> 24 & 0xFF;
        int minAlpha = Math.min(guiAlpha, colorAlpha);

        if (colorAlpha < 255) {
            drawCheckeredBackground(x2, y2, x2 + width, y2 + height);
        }

        int newColor = new Color(color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF, minAlpha).getRGB();
        drawGradientRect(x2, y2, x2 + width, y2 + height, newColor, darker(newColor, 0.6f));
        if (this.isExpanded()) {
            float hueSelectorY;
            float hueSliderYDif;
            float alphaSliderBottom;
            float hueSliderRight;
            GL11.glTranslated(0.0, 0.0, 3.0);
            float expandedX = this.getExpandedX();
            float expandedY = this.getExpandedY();
            float expandedWidth = this.getExpandedWidth();
            float expandedHeight = this.getExpandedHeight();
            RenderUtils.drawRect(expandedX, expandedY, expandedX + expandedWidth, expandedY + expandedHeight, black);
            RenderUtils.drawRect(expandedX + 0.5f, expandedY + 0.5f, (expandedX + expandedWidth) - 0.5f, (expandedY + expandedHeight) - 0.5f, SkeetClickGUI.getColor(0x39393B));
            RenderUtils.drawRect(expandedX + 1.0f, expandedY + 1.0f, expandedX + expandedWidth - 1.0f, expandedY + expandedHeight - 1.0f, SkeetClickGUI.getColor(0x232323));
            float colorPickerSize = expandedWidth - 9.0f - 8.0f;
            float colorPickerLeft = expandedX + 3.0f;
            float colorPickerTop = expandedY + 3.0f;
            float colorPickerRight = colorPickerLeft + colorPickerSize;
            float colorPickerBottom = colorPickerTop + colorPickerSize;
            int selectorWhiteOverlayColor = new Color(255, 255, 255, Math.min(guiAlpha, 180)).getRGB();
            if ((float)mouseX <= colorPickerLeft || (float)mouseY <= colorPickerTop || (float)mouseX >= colorPickerRight || (float)mouseY >= colorPickerBottom) {
                this.colorSelectorDragging = false;
            }
            RenderUtils.drawRect(colorPickerLeft - 0.5f, colorPickerTop - 0.5f, colorPickerRight + 0.5f, colorPickerBottom + 0.5f, SkeetClickGUI.getColor(0));
            this.drawColorPickerRect(colorPickerLeft, colorPickerTop, colorPickerRight, colorPickerBottom);
            float hueSliderLeft = this.saturation * (colorPickerRight - colorPickerLeft);
            float alphaSliderTop = (1.0f - this.brightness) * (colorPickerBottom - colorPickerTop);
            if (this.colorSelectorDragging) {
                hueSliderRight = colorPickerRight - colorPickerLeft;
                alphaSliderBottom = (float)mouseX - colorPickerLeft;
                this.saturation = alphaSliderBottom / hueSliderRight;
                hueSliderLeft = alphaSliderBottom;
                hueSliderYDif = colorPickerBottom - colorPickerTop;
                hueSelectorY = (float)mouseY - colorPickerTop;
                this.brightness = 1.0f - hueSelectorY / hueSliderYDif;
                alphaSliderTop = hueSelectorY;
                this.updateColor(Color.HSBtoRGB(this.hue, this.saturation, this.brightness), false);
            }
            hueSliderRight = colorPickerLeft + hueSliderLeft - 0.5f;
            alphaSliderBottom = colorPickerTop + alphaSliderTop - 0.5f;
            hueSliderYDif = colorPickerLeft + hueSliderLeft + 0.5f;
            hueSelectorY = colorPickerTop + alphaSliderTop + 0.5f;
            RenderUtils.drawRect(hueSliderRight - 0.5f, alphaSliderBottom - 0.5f, hueSliderRight, hueSelectorY + 0.5f, black);
            RenderUtils.drawRect(hueSliderYDif, alphaSliderBottom - 0.5f, hueSliderYDif + 0.5f, hueSelectorY + 0.5f, black);
            RenderUtils.drawRect(hueSliderRight, alphaSliderBottom - 0.5f, hueSliderYDif, alphaSliderBottom, black);
            RenderUtils.drawRect(hueSliderRight, hueSelectorY, hueSliderYDif, hueSelectorY + 0.5f, black);
            RenderUtils.drawRect(hueSliderRight, alphaSliderBottom, hueSliderYDif, hueSelectorY, selectorWhiteOverlayColor);
            hueSliderLeft = colorPickerRight + 3.0f;
            hueSliderRight = hueSliderLeft + 8.0f;
            if ((float)mouseX <= hueSliderLeft || (float)mouseY <= colorPickerTop || (float)mouseX >= hueSliderRight || (float)mouseY >= colorPickerBottom) {
                this.hueSelectorDragging = false;
            }
            hueSliderYDif = colorPickerBottom - colorPickerTop;
            hueSelectorY = (1.0f - this.hue) * hueSliderYDif;
            if (this.hueSelectorDragging) {
                float inc = (float)mouseY - colorPickerTop;
                this.hue = 1.0f - inc / hueSliderYDif;
                hueSelectorY = inc;
                this.updateColor(Color.HSBtoRGB(this.hue, this.saturation, this.brightness), false);
            }
            RenderUtils.drawRect(hueSliderLeft - 0.5f, colorPickerTop - 0.5f, hueSliderRight + 0.5f, colorPickerBottom + 0.5f, black);
            float hsHeight = colorPickerBottom - colorPickerTop;
            float alphaSelectorX = hsHeight / 5.0f;
            float asLeft = colorPickerTop;
            int i2 = 0;
            while ((float)i2 < 5.0f) {
                boolean last = (float)i2 == 4.0f;
                drawGradientRect(hueSliderLeft, asLeft, hueSliderRight, asLeft + alphaSelectorX, SkeetClickGUI.getColor(Color.HSBtoRGB(1.0f - 0.2f * (float)i2, 1.0f, 1.0f)), SkeetClickGUI.getColor(Color.HSBtoRGB(1.0f - 0.2f * (float)(i2 + 1), 1.0f, 1.0f)));
                if (!last) {
                    asLeft += alphaSelectorX;
                }
                ++i2;
            }
            float hsTop = colorPickerTop + hueSelectorY - 0.5f;
            float asRight = colorPickerTop + hueSelectorY + 0.5f;
            RenderUtils.drawRect(hueSliderLeft - 0.5f, hsTop - 0.5f, hueSliderLeft, asRight + 0.5f, black);
            RenderUtils.drawRect(hueSliderRight, hsTop - 0.5f, hueSliderRight + 0.5f, asRight + 0.5f, black);
            RenderUtils.drawRect(hueSliderLeft, hsTop - 0.5f, hueSliderRight, hsTop, black);
            RenderUtils.drawRect(hueSliderLeft, asRight, hueSliderRight, asRight + 0.5f, black);
            RenderUtils.drawRect(hueSliderLeft, hsTop, hueSliderRight, asRight, selectorWhiteOverlayColor);
            alphaSliderTop = colorPickerBottom + 3.0f;
            alphaSliderBottom = alphaSliderTop + 8.0f;
            if ((float)mouseX <= colorPickerLeft || (float)mouseY <= alphaSliderTop || (float)mouseX >= colorPickerRight || (float)mouseY >= alphaSliderBottom) {
                this.alphaSelectorDragging = false;
            }
            int z2 = Color.HSBtoRGB(this.hue, this.saturation, this.brightness);
            int r2 = z2 >> 16 & 0xFF;
            int g2 = z2 >> 8 & 0xFF;
            int b2 = z2 & 0xFF;
            hsHeight = colorPickerRight - colorPickerLeft;
            alphaSelectorX = this.alpha * hsHeight;
            if (this.alphaSelectorDragging) {
                asLeft = (float)mouseX - colorPickerLeft;
                this.alpha = asLeft / hsHeight;
                alphaSelectorX = asLeft;
                this.updateColor(new Color(r2, g2, b2, (int)(this.alpha * 255.0f)).getRGB(), true);
            }
            RenderUtils.drawRect(colorPickerLeft - 0.5f, alphaSliderTop - 0.5f, colorPickerRight + 0.5f, alphaSliderBottom + 0.5f, black);
            drawCheckeredBackground(colorPickerLeft, alphaSliderTop, colorPickerRight, alphaSliderBottom);
            drawGradientRect(colorPickerLeft, alphaSliderTop, colorPickerRight, alphaSliderBottom, true, new Color(r2, g2, b2, 0).getRGB(), new Color(r2, g2, b2, Math.min(guiAlpha, 255)).getRGB());
            asLeft = colorPickerLeft + alphaSelectorX - 0.5f;
            asRight = colorPickerLeft + alphaSelectorX + 0.5f;
            RenderUtils.drawRect(asLeft - 0.5f, alphaSliderTop, asRight + 0.5f, alphaSliderBottom, black);
            RenderUtils.drawRect(asLeft, alphaSliderTop, asRight, alphaSliderBottom, selectorWhiteOverlayColor);
            GL11.glTranslated(0.0, 0.0, -3.0);
        }
    }

    @Override
    public void onMouseClick(int mouseX, int mouseY, int button) {
        if (this.isHovered(mouseX, mouseY)) {
            this.onPress(button);
        }

        if (this.isExpanded() && button == 0) {
            float expandedX = this.getExpandedX();
            float expandedY = this.getExpandedY();
            float expandedWidth = this.getExpandedWidth();
            float expandedHeight = this.getExpandedHeight();
            float colorPickerSize = expandedWidth - 9.0f - 8.0f;
            float colorPickerLeft = expandedX + 3.0f;
            float colorPickerTop = expandedY + 3.0f;
            float colorPickerRight = colorPickerLeft + colorPickerSize;
            float colorPickerBottom = colorPickerTop + colorPickerSize;
            float alphaSliderTop = colorPickerBottom + 3.0f;
            float alphaSliderBottom = alphaSliderTop + 8.0f;
            float hueSliderLeft = colorPickerRight + 3.0f;
            float hueSliderRight = hueSliderLeft + 8.0f;
            this.colorSelectorDragging = !this.colorSelectorDragging && (float)mouseX > colorPickerLeft && (float)mouseY > colorPickerTop && (float)mouseX < colorPickerRight && (float)mouseY < colorPickerBottom;
            this.alphaSelectorDragging = !this.alphaSelectorDragging && (float)mouseX > colorPickerLeft && (float)mouseY > alphaSliderTop && (float)mouseX < colorPickerRight && (float)mouseY < alphaSliderBottom;
            this.hueSelectorDragging = !this.hueSelectorDragging && (float)mouseX > hueSliderLeft && (float)mouseY > colorPickerTop && (float)mouseX < hueSliderRight && (float)mouseY < colorPickerBottom;
        }
    }

    @Override
    public void onMouseRelease(int button) {
        if (this.hueSelectorDragging) {
            this.hueSelectorDragging = false;
        } else if (this.colorSelectorDragging) {
            this.colorSelectorDragging = false;
        } else if (this.alphaSelectorDragging) {
            this.alphaSelectorDragging = false;
        }
    }

    public void updateColor(int hex, boolean hasAlpha) {
        if (hasAlpha) {
            this.setColor(hex);
        } else {
            this.setColor(new Color(hex >> 16 & 0xFF, hex >> 8 & 0xFF, hex & 0xFF, (int)(this.alpha * 255.0f)).getRGB());
        }
    }

    public abstract int getColor();

    public abstract void setColor(int color);

    @Override
    public void updateValue(String valGroup, String valKey, Object oldVal, Object val) {
        if(((ColorPickerTextComponent) this.getParent()).valID.equals(valGroup) && val instanceof Float) {
            switch(valKey) {
                case "Hue":
                    this.updateH((Float) val);
                    break;
                case "Saturation":
                    this.updateS((Float) val);
                    break;
                case "Brightness":
                    this.updateB((Float) val);
                    break;
                case "Alpha":
                    this.updateA((Float) val);
            }
        }
    }

    public void updateH(float h) {
        this.updateValue(h, this.saturation, this.brightness);
    }

    public void updateS(float s) {
        this.updateValue(this.hue, s, this.brightness);
    }

    public void updateB(float b) {
        this.updateValue(this.hue, this.saturation, b);
    }

    public void updateA(float a) {
        this.alpha = MathUtils.clampValue(a, 0, 1);
    }

    public void updateValue(float h, float s, float b) {
        this.hue = h;
        this.saturation = s;
        this.brightness = b;
    }

    public void updateValue(int value) {
        float[] hsb = this.getHSBFromColor(value);
        this.hue = hsb[0];
        this.saturation = hsb[1];
        this.brightness = hsb[2];
        this.alpha = (float)(value >> 24 & 255) / 255.0f;
    }

    private float[] getHSBFromColor(int hex) {
        int r2 = hex >> 16 & 0xFF;
        int g2 = hex >> 8 & 0xFF;
        int b2 = hex & 0xFF;
        return Color.RGBtoHSB(r2, g2, b2, null);
    }

    private void drawColorPickerRect(float left, float top, float right, float bottom) {
        int hueBasedColor = SkeetClickGUI.getColor(Color.HSBtoRGB(this.hue, 1.0F, 1.0F));
        drawGradientRect(left, top, right, bottom, true, SkeetClickGUI.getColor(16777215), hueBasedColor);
        drawGradientRect(left, top, right, bottom, 0, SkeetClickGUI.getColor(0));
    }

    @Override
    public float getExpandedX() {
        return this.getX() + this.getWidth() - 80.333336f;
    }

    @Override
    public float getExpandedY() {
        return this.getY() + this.getHeight();
    }

    @Override
    public boolean isExpanded() {
        return this.expanded;
    }

    @Override
    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    @Override
    public void onPress(int mouseButton) {
        if (mouseButton == 1) {
            this.setExpanded(!this.isExpanded());
        }
    }

    @Override
    public float getExpandedWidth() {
        float right = this.getX() + this.getWidth();
        return right - this.getExpandedX();
    }

    @Override
    public float getExpandedHeight() {
        return this.getExpandedWidth();
    }

    public static int darker(int color, float factor) {
        int r = (int)((color >> 16 & 0xFF) * factor);
        int g = (int)((color >> 8 & 0xFF) * factor);
        int b = (int)((color & 0xFF) * factor);
        int a = color >> 24 & 0xFF;
        return (r & 0xFF) << 16 | (g & 0xFF) << 8 | b & 0xFF | (a & 0xFF) << 24;
    }

    public void drawGradientRect(float left, float top, float right, float bottom, int startColor, int endColor) {
        float f = (float) (startColor >> 24 & 255) / 255.0F;
        float f1 = (float) (startColor >> 16 & 255) / 255.0F;
        float f2 = (float) (startColor >> 8 & 255) / 255.0F;
        float f3 = (float) (startColor & 255) / 255.0F;
        float f4 = (float) (endColor >> 24 & 255) / 255.0F;
        float f5 = (float) (endColor >> 16 & 255) / 255.0F;
        float f6 = (float) (endColor >> 8 & 255) / 255.0F;
        float f7 = (float) (endColor & 255) / 255.0F;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(7425);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos(right, top, 0).color(f1, f2, f3, f).endVertex();
        worldrenderer.pos(left, top, 0).color(f1, f2, f3, f).endVertex();
        worldrenderer.pos(left, bottom, 0).color(f5, f6, f7, f4).endVertex();
        worldrenderer.pos(right, bottom, 0).color(f5, f6, f7, f4).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    public void drawGradientRect(double left, double top, double right, double bottom, boolean sideways, int startColor, int endColor) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glBegin(GL11.GL_QUADS);
        RenderUtil.color(startColor);
        if (sideways) {
            GL11.glVertex2d(left, top);
            GL11.glVertex2d(left, bottom);
            RenderUtil.color(endColor);
            GL11.glVertex2d(right, bottom);
            GL11.glVertex2d(right, top);
        } else {
            GL11.glVertex2d(left, top);
            RenderUtil.color(endColor);
            GL11.glVertex2d(left, bottom);
            GL11.glVertex2d(right, bottom);
            RenderUtil.color(startColor);
            GL11.glVertex2d(right, top);
        }
        GL11.glEnd();
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }
}

