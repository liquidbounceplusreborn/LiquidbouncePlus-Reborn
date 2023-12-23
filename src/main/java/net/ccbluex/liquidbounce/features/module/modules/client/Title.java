package net.ccbluex.liquidbounce.features.module.modules.client;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.event.EventTarget;
import net.ccbluex.liquidbounce.event.Render2DEvent;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;
import net.ccbluex.liquidbounce.features.module.modules.render.ColorMixer;
import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.ccbluex.liquidbounce.ui.font.GameFontRenderer;
import net.ccbluex.liquidbounce.utils.EntityUtils;
import net.ccbluex.liquidbounce.utils.math.MathUtils;
import net.ccbluex.liquidbounce.utils.render.*;
import net.ccbluex.liquidbounce.value.FloatValue;
import net.ccbluex.liquidbounce.value.IntegerValue;
import net.ccbluex.liquidbounce.value.ListValue;
import net.ccbluex.liquidbounce.value.TextValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumChatFormatting;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import static net.minecraft.util.EnumChatFormatting.DARK_GRAY;
import static net.minecraft.util.EnumChatFormatting.WHITE;

@ModuleInfo(name = "Title", description = "Client Title", category = ModuleCategory.CLIENT,array = false)
public class Title extends Module {

    private static final TextValue title = new TextValue("Title", ".title title <your title here>");

    private static final ListValue colorModeValue = new ListValue("Color", new String[] {"Custom", "Health", "Rainbow", "Sky", "LiquidSlowly", "Fade", "Mixer"}, "Custom");
    public static final IntegerValue red = new IntegerValue("Red",255,0,255);
    public static final IntegerValue green = new IntegerValue("Green",255,0,255);
    public static final IntegerValue blue = new IntegerValue("Blue",255,0,255);
    private static final FloatValue saturationValue = new FloatValue("Saturation", 1F, 0F, 1F);
    private static final FloatValue brightnessValue = new FloatValue("Brightness", 1F, 0F, 1F);
    private static final IntegerValue mixerSecondsValue = new IntegerValue("Seconds", 2, 1, 10);

    public final ListValue display = new ListValue("ClientName", new String[]{"LiquidBounce", "Custom"}, "LiquidBounce");

    public final ListValue modeValue = new ListValue("Mode", new String[]{"Test1","Test2","Test3","Test4","Test5","Test6","Test7","Test8","Test9"}, "Test2");
    @EventTarget
    private void onRender2d(Render2DEvent event){

        final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");

        String name = Objects.equals(display.get(), "LiquidBounce") ? "LiquidBounce" : title.get();

        switch (modeValue.get()){
            case "Test1": {
                final String title = String.format("| %s | %s | %sfps", LiquidBounce.CLIENT_VERSION, mc.session.getUsername(), Minecraft.getDebugFPS());
                final String mark = name;
                final float width = (float)(Fonts.font35.getStringWidth(title) + Fonts.font40.getStringWidth(mark) + 6);
                RenderUtils.drawRect(4.0, 4.0, width + 10.0f, Fonts.font40.getHeight() + 8, new Color(0, 0, 0, 100).getRGB());
                Fonts.font40.drawString(mark, 8.0f, 9f, getColor().getRGB(), true);
                Fonts.font35.drawString(title, (float)(12 + Fonts.font40.getStringWidth(mark)), 9.0f, -1);
                break;
            }
            case "Test2": {
                final String str = EnumChatFormatting.DARK_GRAY + " | " + EnumChatFormatting.WHITE + mc.session.getUsername() + EnumChatFormatting.DARK_GRAY + " | " + EnumChatFormatting.WHITE + Minecraft.getDebugFPS() + "fps" + EnumChatFormatting.DARK_GRAY + " | " + EnumChatFormatting.WHITE + (HUD.mc.isSingleplayer() ? "SinglePlayer" : HUD.mc.getCurrentServerData().serverIP);
                RoundedUtil.drawRound(6.0f, 6.0f, (float)(Fonts.font35.getStringWidth(str) + 8 + Fonts.font40.getStringWidth(name.toUpperCase())), 15.0f, 0.0f, new Color(19, 19, 19, 230));
                RoundedUtil.drawRound(6.0f, 6.0f, (float)(Fonts.font35.getStringWidth(str) + 8 + Fonts.font40.getStringWidth(name.toUpperCase())), 1.0f, 1.0f, color(8));
                Fonts.font35.drawString(str, (float)(11 + Fonts.font40.getStringWidth(name.toUpperCase())), 11.5f, Color.WHITE.getRGB());
                Fonts.font40.drawString(EnumChatFormatting.BOLD +name.toUpperCase(), 9.5f, 11.5f, color(8).getRGB());
                Fonts.font40.drawString(EnumChatFormatting.BOLD + name.toUpperCase(), 10.0f, 12f, Color.WHITE.getRGB());
                break;
            }
            case "Test3": {
                final String title = String.format("| %s | %s | %sfps", LiquidBounce.CLIENT_VERSION, mc.session.getUsername(), Minecraft.getDebugFPS());
                final String mark = name;
                final float width = (float)(Fonts.font35.getStringWidth(title) + Fonts.font35.getStringWidth(mark) + 6);
                RenderUtils.drawExhiRect(8.0f, 8.0f, width + 10.0f, Fonts.font40.getHeight() + 8, 1);
                Fonts.font35.drawString(mark, 8.5f, 10f, getColor().getRGB(), true);
                Fonts.font35.drawString(title, (float)(13.5 + Fonts.font35.getStringWidth(mark)), 9.0f, -1);
                break;
            }
            case "Test4": {
                String text = DARK_GRAY + "   |  " + WHITE + mc.thePlayer.getName() + DARK_GRAY + "  |  " + WHITE + EntityUtils.getPing(mc.thePlayer) + "ms" + DARK_GRAY + "  |  " + WHITE + dateFormat.format(new Date()) + DARK_GRAY + "  |  " + WHITE;

                ShadowUtils.shadow(15, () -> {
                    GlStateManager.enableBlend();
                    GlStateManager.disableTexture2D();
                    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                    RenderUtils.originalRoundedRect(6f, 7f, 5 + Fonts.fontSFUI35.getStringWidth(text) +  Fonts.fontSFUI35.getStringWidth(name), 27f, 5.4f,getColor().getRGB());
                    GlStateManager.enableTexture2D();
                    GlStateManager.disableBlend();
                    return null;
                }, () -> {
                    GlStateManager.enableBlend();
                    GlStateManager.disableTexture2D();
                    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                    RenderUtils.originalRoundedRect(6f, 7f, 5 + Fonts.fontSFUI35.getStringWidth(text) +  Fonts.fontSFUI35.getStringWidth(name), 27f, 5.4f,getColor().getRGB());
                    GlStateManager.enableTexture2D();
                    GlStateManager.disableBlend();
                    return null;
                });
                BlurUtils.blurAreaRounded(6f, 7f, 5 + Fonts.fontSFUI35.getStringWidth(text) +  Fonts.fontSFUI35.getStringWidth(name), 27f,5.4f, 10f);
                RenderUtils.drawRoundedRect(6f, 7f, 5 + Fonts.fontSFUI35.getStringWidth(text) +  Fonts.fontSFUI35.getStringWidth(name), 27f, 5.4f, new Color(0, 0, 0, 100).getRGB());

                Fonts.fontSFUI35.drawStringWithShadow(" " + name, 8, 13, -1);
                Fonts.fontSFUI35.drawString(text, 7 + Fonts.fontSFUI35.getStringWidth(name), 13, Color.WHITE.getRGB());
                break;
            }
            case "Test5":{
                String text = DARK_GRAY + "   |  " + WHITE + mc.thePlayer.getName() + DARK_GRAY + "  |  " + WHITE + EntityUtils.getPing(mc.thePlayer) + "ms" + DARK_GRAY + "  |  " + WHITE + dateFormat.format(new Date()) + DARK_GRAY + "  |  " + WHITE;
                ShadowUtils.shadow(15, () -> {
                    GlStateManager.enableBlend();
                    GlStateManager.disableTexture2D();
                    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                    RenderUtils.originalRoundedRect(6f, 7f, 5 + Fonts.fontSFUI35.getStringWidth(text) + Fonts.fontTahoma.getStringWidth(name), 27f,5.4f,getColor().getRGB());
                    GlStateManager.enableTexture2D();
                    GlStateManager.disableBlend();
                    return null;
                }, () -> {
                    GlStateManager.enableBlend();
                    GlStateManager.disableTexture2D();
                    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                    RenderUtils.originalRoundedRect(6f, 7f, 5 + Fonts.fontSFUI35.getStringWidth(text) + Fonts.fontTahoma.getStringWidth(name), 27f,5.4f,getColor().getRGB());
                    GlStateManager.enableTexture2D();
                    GlStateManager.disableBlend();
                    return null;
                });
                BlurUtils.blurAreaRounded(6f, 7f, 5 + Fonts.fontSFUI35.getStringWidth(text) + Fonts.fontTahoma.getStringWidth(name), 27f,5.4f, 10f);
                RenderUtils.drawRoundedRect(6f, 7f, 5 + Fonts.fontSFUI35.getStringWidth(text) + Fonts.fontTahoma.getStringWidth(name), 27f, 5.4f, new Color(0, 0, 0, 100).getRGB());
                Color c1 = interpolateColorsBackAndForth(6, 90, getColor(), getColor(), false);
                    Color c2 = interpolateColorsBackAndForth(6, 180, getColor(), getColor(), false);
                    Fonts.fontTahoma.drawString(" " + name, 10, 12, new Color(0, 0, 0).getRGB());
                    RoundedUtil.applyGradientHorizontal(10, 12, Fonts.fontTahoma.getStringWidth("  " + name), Fonts.fontTahoma.getHeight(), 1, c1, c2, () -> {
                        RenderUtils.setAlphaLimit(0);
                        Fonts.fontTahoma.drawString(" " + name, 10, 12, new Color(255, 255, 255).getRGB());
                        GlowUtils.drawGlow(10, 12, Fonts.fontTahoma.getStringWidth(" " + name), Fonts.fontTahoma.getHeight(), 6, applyOpacity(new Color(255, 255, 255), 0.5f));
                    });

                Fonts.fontSFUI35.drawString(text, 9  + Fonts.fontTahoma.getStringWidth(name), 13, Color.WHITE.getRGB());
                break;
            }
            case "Test6": {
                String username = mc.thePlayer.getName();
                String servername = mc.isSingleplayer() ? "Singleplayer" : mc.getCurrentServerData().serverIP;
                String fps = Minecraft.getDebugFPS() + "fps";
                String times = dateFormat.format(new Date());
                RoundedUtil.drawRound(6, 5, Fonts.fontTahoma.getStringWidth(name) + Fonts.fontTahoma30.getStringWidth(" | " + username + " | " + servername + " | " + times + " | " + fps) + 3 +5, 12, 1, new Color(0, 0, 0, 100));
                Fonts.fontTahoma.drawString(name, 9, 7, new Color(24,114,165).getRGB());
                Fonts.fontTahoma.drawString(name, 8, 7, -1);
                Fonts.fontTahoma30.drawString(" | " + username + " | " + servername + " | " + times + " | " + fps, Fonts.fontTahoma.getStringWidth(name) + 11, 8, -1);
                break;
            }
            case "Test7":{
                String server2 = mc.isSingleplayer() ? "local" : mc.getCurrentServerData().serverIP.toLowerCase();
                String time3 = (new SimpleDateFormat("HH:mm:ss")).format(Calendar.getInstance().getTime());
                String text2 = name + " | " + mc.thePlayer.getName() + " | " + server2 + " | " + mc.getDebugFPS() + " fps" + " | " + Objects.requireNonNull(mc.getNetHandler()).getPlayerInfo(mc.thePlayer.getUniqueID()).getResponseTime() + "ms" + " | " + time3;
                RenderUtils.drawRect(5, 6, Fonts.fontSFUI35.getStringWidth(text2) + 9, 18, new Color(31, 31, 31, 255).getRGB());
                for (float l = 0; l < Fonts.fontSFUI35.getStringWidth(text2) + 4; l += 1f) {
                    RenderUtils.drawRect(5 + l, 5, l + 6, 6, getColor());
                }
                RenderUtils.drawRect(5, 6f, Fonts.fontSFUI35.getStringWidth(text2) + 9, 6.5f, new Color(20, 20, 20, 100).getRGB());
                Fonts.fontSFUI35.drawString(text2, 7F, 10.5F, -1, true);
                break;
            }
            case "Test8" :{
                drawNewRect(5, 6, Fonts.fontSFUI40.getStringWidth(name) + 7, 19, new Color(25, 125, 255).getRGB());
                drawNewRect(7, 6, Fonts.fontSFUI40.getStringWidth(name) + 10, 19, new Color(31, 31, 31).getRGB());
                Fonts.fontSFUI40.drawString(name, 8, 9, -1, true);
                break;
            }
            case "Test9":{
                String append5 = name.substring(0, 1);
                String append6 = name.substring(1);

                String clientname = append5 + WHITE + append6;

                String username2 = mc.thePlayer.getName();
                String servername2 = mc.isSingleplayer() ? "Singleplayer" : mc.getCurrentServerData().serverIP;
                String fps2 = Minecraft.getDebugFPS() + "fps";
                String time2 = dateFormat.format(new Date());
                int y = -1;
                int x = -4;

                GameFontRenderer watermarkfont = Fonts.fontSFUI35;

                GameFontRenderer watermarkfont2 = Fonts.fontSFUI40;

                RoundedUtil.drawRound(10 + x,5 + y,watermarkfont.getStringWidth(username2) +
                        watermarkfont.getStringWidth(clientname) +
                        watermarkfont.getStringWidth(servername2) +
                        watermarkfont.getStringWidth(fps2) +
                        watermarkfont.getStringWidth(time2) +
                        (watermarkfont2.getStringWidth(" | ") * 4)
                        + 2.5f,11.5f,0,new Color(0,0,0,90));

                RoundedUtil.drawRound(10+ x,4.3f+ y,watermarkfont.getStringWidth(username2) +
                        watermarkfont.getStringWidth(clientname) +
                        watermarkfont.getStringWidth(servername2) +
                        watermarkfont.getStringWidth(fps2) +
                        watermarkfont.getStringWidth(time2) +
                        (watermarkfont2.getStringWidth(" | ") * 4)
                        + 2.5f,0.7f,0,new Color(getColor().getRGB()));

                watermarkfont.drawString(clientname,11f+ x,9f+ y, getColor().getRGB());

                watermarkfont2.drawString(" | ",11+ x + watermarkfont.getStringWidth(clientname),8f+ y,getColor().getRGB(),false);

                watermarkfont.drawString(username2,11f + x+watermarkfont.getStringWidth(clientname)
                                + watermarkfont2.getStringWidth(" | ")
                        ,9f+ y, -1);

                watermarkfont2.drawString(" | ",11 + x+ watermarkfont.getStringWidth(username2)+
                        watermarkfont.getStringWidth(clientname)+
                        watermarkfont2.getStringWidth(" | "),8f+ y,getColor().getRGB(),false);

                watermarkfont.drawString(servername2,11f+ x +watermarkfont.getStringWidth(clientname)
                                + watermarkfont.getStringWidth(username2)
                                + watermarkfont2.getStringWidth(" | ") * 2
                        ,9f+ y, -1);

                watermarkfont2.drawString(" | ",11+ x + watermarkfont.getStringWidth(username2)+
                        watermarkfont.getStringWidth(clientname)+
                        watermarkfont.getStringWidth(servername2) +
                        watermarkfont2.getStringWidth(" | ") * 2,8f+ y,getColor().getRGB(),false);

                watermarkfont.drawString(fps2,11f+ x +watermarkfont.getStringWidth(clientname)
                                + watermarkfont.getStringWidth(servername2)
                                + watermarkfont.getStringWidth(username2)
                                + watermarkfont2.getStringWidth(" | ") * 3
                        ,9f+ y, -1);

                watermarkfont2.drawString(" | ",11+ x +  watermarkfont.getStringWidth(username2)+
                        watermarkfont.getStringWidth(fps2) +
                        watermarkfont.getStringWidth(clientname)+
                        watermarkfont.getStringWidth(servername2) +
                        watermarkfont2.getStringWidth(" | ") * 3,8f+ y,getColor().getRGB(),false);

                watermarkfont.drawString(time2,11f+ x +watermarkfont.getStringWidth(clientname)
                                + watermarkfont.getStringWidth(fps2)
                                + watermarkfont.getStringWidth(servername2)
                                + watermarkfont.getStringWidth(username2)
                                + watermarkfont2.getStringWidth(" | ") * 4
                        ,9f+ y, -1);

                break;
            }
        }
    }

    public static final Color getColor() {
        switch (colorModeValue.get()) {
            case "Custom":
                return new Color(red.get(), green.get(), blue.get());
            case "Rainbow":
                return new Color(RenderUtils.getRainbowOpaque(mixerSecondsValue.get(), saturationValue.get(), brightnessValue.get(), 0));
            case "Sky":
                return RenderUtils.skyRainbow(0, saturationValue.get(), brightnessValue.get());
            case "LiquidSlowly":
                return ColorUtils.LiquidSlowly(System.nanoTime(), 0, saturationValue.get(), brightnessValue.get());
            case "Mixer":
                return ColorMixer.getMixedColor(0, mixerSecondsValue.get());
            case "Fade":
                return ColorUtils.fade(new Color(red.get(), green.get(), blue.get()), 0, 100);
            default:
                return Color.white;
        }
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

    public static Color color(final int tick) {
        Color textColor = new Color(-1);
        textColor = ColorUtils.fade(5, tick * 20, getColor(), 1.0f);
        return textColor;
    }

    public static Color interpolateColorC(Color color1, Color color2, float amount) {
        amount = Math.min(1, Math.max(0, amount));
        return new Color(MathUtils.interpolateInt(color1.getRed(), color2.getRed(), amount),
                MathUtils.interpolateInt(color1.getGreen(), color2.getGreen(), amount),
                MathUtils.interpolateInt(color1.getBlue(), color2.getBlue(), amount),
                MathUtils.interpolateInt(color1.getAlpha(), color2.getAlpha(), amount));
    }

    public static Color interpolateColorsBackAndForth(int speed, int index, Color start, Color end, boolean trueColor) {
        int angle = (int) (((System.currentTimeMillis()) / speed + index) % 360);
        angle = (angle >= 180 ? 360 - angle : angle) * 2;
        return trueColor ? interpolateColorHue(start, end, angle / 360f) : interpolateColorC(start, end, angle / 360f);
    }

    public static Color interpolateColorHue(Color color1, Color color2, float amount) {
        amount = Math.min(1, Math.max(0, amount));

        float[] color1HSB = Color.RGBtoHSB(color1.getRed(), color1.getGreen(), color1.getBlue(), null);
        float[] color2HSB = Color.RGBtoHSB(color2.getRed(), color2.getGreen(), color2.getBlue(), null);

        Color resultColor = Color.getHSBColor(MathUtils.interpolateFloat(color1HSB[0], color2HSB[0], amount),
                MathUtils.interpolateFloat(color1HSB[1], color2HSB[1], amount), MathUtils.interpolateFloat(color1HSB[2], color2HSB[2], amount));

        return new Color(resultColor.getRed(), resultColor.getGreen(), resultColor.getBlue(),
                MathUtils.interpolateInt(color1.getAlpha(), color2.getAlpha(), amount));
    }

    public static Color applyOpacity(Color color, float opacity) {
        opacity = Math.min(1, Math.max(0, opacity));
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (color.getAlpha() * opacity));
    }
}
