/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.render;

import net.ccbluex.liquidbounce.event.EventTarget;
import net.ccbluex.liquidbounce.event.Render3DEvent;
import net.ccbluex.liquidbounce.event.UpdateEvent;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;
import net.ccbluex.liquidbounce.utils.render.ColorUtils;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.ccbluex.liquidbounce.value.BoolValue;
import net.ccbluex.liquidbounce.value.FloatValue;
import net.ccbluex.liquidbounce.value.IntegerValue;
import net.ccbluex.liquidbounce.value.ListValue;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

@ModuleInfo(name = "Breadcrumbs", description = "Leaves a trail behind you.", category = ModuleCategory.RENDER)
public class Breadcrumbs extends Module {
    public final BoolValue unlimitedValue = new BoolValue("Unlimited", false);
    public final FloatValue lineWidth = new FloatValue("LineWidth", 1F, 1F, 10F);
    private final ListValue colorModeValue = new ListValue("Color", new String[] {"Custom", "Rainbow", "Sky", "LiquidSlowly", "Fade", "Mixer"}, "Custom");
    public final IntegerValue colorRedValue = new IntegerValue("R", 255, 0, 255, () -> colorModeValue.get().contains("Custom") || colorModeValue.get().contains("Fade"));
    public final IntegerValue colorGreenValue = new IntegerValue("G", 179, 0, 255, () -> colorModeValue.get().contains("Custom") || colorModeValue.get().contains("Fade"));
    public final IntegerValue colorBlueValue = new IntegerValue("B", 72, 0, 255, () -> colorModeValue.get().contains("Custom") || colorModeValue.get().contains("Fade"));
    private final FloatValue saturationValue = new FloatValue("Saturation", 1F, 0F, 1F, () -> !(colorModeValue.get().contains("Custom") || colorModeValue.get().contains("Fade")));
    private final FloatValue brightnessValue = new FloatValue("Brightness", 1F, 0F, 1F, () -> !(colorModeValue.get().contains("Custom") || colorModeValue.get().contains("Fade")));
    private final IntegerValue mixerSecondsValue = new IntegerValue("Seconds", 2, 1, 10, () -> !(colorModeValue.get().contains("Custom") || colorModeValue.get().contains("Fade")));
    public final IntegerValue fadeSpeedValue = new IntegerValue("Fade-Speed", 25, 0, 255);
    public final BoolValue colorRainbow = new BoolValue("Rainbow", false);
    private final LinkedList<Dot> positions = new LinkedList<>();

    private double lastX, lastY, lastZ = 0;

    @EventTarget
    public void onRender3D(Render3DEvent event) {
        final Color color = getColor();

        synchronized (positions) {
            glPushMatrix();

            glDisable(GL_TEXTURE_2D);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            glEnable(GL_LINE_SMOOTH);
            glEnable(GL_BLEND);
            glDisable(GL_DEPTH_TEST);
            mc.entityRenderer.disableLightmap();
            glLineWidth(lineWidth.get());
            glBegin(GL_LINE_STRIP);
            
            final double renderPosX = mc.getRenderManager().viewerPosX;
            final double renderPosY = mc.getRenderManager().viewerPosY;
            final double renderPosZ = mc.getRenderManager().viewerPosZ;

            List<Dot> removeQueue = new ArrayList<>();

            for (final Dot dot : positions) {
                if (dot.alpha > 0) dot.render(color, renderPosX, renderPosY, renderPosZ, unlimitedValue.get() ? 0 : fadeSpeedValue.get());
                else removeQueue.add(dot);
            }

            for (Dot removeDot : removeQueue) 
                positions.remove(removeDot);

            glColor4d(1, 1, 1, 1);
            glEnd();
            glEnable(GL_DEPTH_TEST);
            glDisable(GL_LINE_SMOOTH);
            glDisable(GL_BLEND);
            glEnable(GL_TEXTURE_2D);
            glPopMatrix();
        }
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        synchronized (positions) {
            if (mc.thePlayer.posX != lastX || mc.thePlayer.getEntityBoundingBox().minY != lastY || mc.thePlayer.posZ != lastZ) {
                positions.add(new Dot(new double[]{mc.thePlayer.posX, mc.thePlayer.getEntityBoundingBox().minY, mc.thePlayer.posZ}));
                lastX = mc.thePlayer.posX;
                lastY = mc.thePlayer.getEntityBoundingBox().minY;
                lastZ = mc.thePlayer.posZ;
            }
        }
    }

    @Override
    public void onEnable() {
        if (mc.thePlayer == null)
            return;

        synchronized (positions) {
            positions.add(new Dot(new double[]{mc.thePlayer.posX,
                    mc.thePlayer.getEntityBoundingBox().minY + (mc.thePlayer.getEyeHeight() * 0.5f),
                    mc.thePlayer.posZ}));
            positions.add(new Dot(new double[]{mc.thePlayer.posX, mc.thePlayer.getEntityBoundingBox().minY, mc.thePlayer.posZ}));
        }
        super.onEnable();
    }

    @Override
    public void onDisable() {
        synchronized (positions) {
            positions.clear();
        }
        super.onDisable();
    }

    class Dot {
        public int alpha = 255;
        private final double[] pos;

        public Dot(double[] position) {
            this.pos = position;
        }

        public void render(Color color, double renderPosX, double renderPosY, double renderPosZ, int decreaseBy) {
            Color reColor = ColorUtils.reAlpha(color, alpha);
            RenderUtils.glColor(reColor);
            glVertex3d(pos[0] - renderPosX, pos[1] - renderPosY, pos[2] - renderPosZ);
            alpha -= decreaseBy;
            if (alpha < 0) alpha = 0;
        }
    }

    public final Color getColor() {
        switch (colorModeValue.get()) {
            case "Custom":
                return new Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get());
            case "Rainbow":
                return new Color(RenderUtils.getRainbowOpaque(mixerSecondsValue.get(), saturationValue.get(), brightnessValue.get(), 0));
            case "Sky":
                return RenderUtils.skyRainbow(0, saturationValue.get(), brightnessValue.get());
            case "LiquidSlowly":
                return ColorUtils.LiquidSlowly(System.nanoTime(), 0, saturationValue.get(), brightnessValue.get());
            case "Mixer":
                return ColorMixer.getMixedColor(0, mixerSecondsValue.get());
            default:
                return ColorUtils.fade(new Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get()), 0, 100);
        }
    }
}
