package net.ccbluex.liquidbounce.features.module.modules.client;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.event.EventTarget;
import net.ccbluex.liquidbounce.event.Render2DEvent;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;
import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.ccbluex.liquidbounce.utils.render.ColorUtils;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.ccbluex.liquidbounce.utils.render.RoundedUtil;
import net.ccbluex.liquidbounce.value.IntegerValue;
import net.ccbluex.liquidbounce.value.ListValue;
import net.ccbluex.liquidbounce.value.TextValue;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumChatFormatting;

import java.awt.*;
import java.util.Objects;

@ModuleInfo(name = "Title", description = "Client Title", category = ModuleCategory.CLIENT,array = false)
public class Title extends Module {

    private static final TextValue title = new TextValue("Title", ".title title <your title here>");
    public static final IntegerValue red = new IntegerValue("Red",255,0,255);
    public static final IntegerValue green = new IntegerValue("Green",255,0,255);
    public static final IntegerValue blue = new IntegerValue("Blue",255,0,255);

    public final ListValue display = new ListValue("ClientName", new String[]{"LiquidBounce", "Custom"}, "LiquidBounce");

    public final ListValue modeValue = new ListValue("Mode", new String[]{"Test1","Test2","Test3"}, "Test2");
    @EventTarget
    private void onRender2d(Render2DEvent event){

        String name = Objects.equals(display.get(), "LiquidBounce") ? "LiquidBounce" : title.get();

        switch (modeValue.get()){
            case "Test1": {
                final String title = String.format("| %s | %s | %sfps", LiquidBounce.CLIENT_VERSION, mc.session.getUsername(), Minecraft.getDebugFPS());
                final String mark = name;
                final float width = (float)(Fonts.font35.getStringWidth(title) + Fonts.font40.getStringWidth(mark) + 6);
                RenderUtils.drawRect(4.0, 4.0, width + 10.0f, Fonts.font40.getHeight() + 8, new Color(0, 0, 0, 100).getRGB());
                Fonts.font40.drawString(mark, 8.0f, 9f, new Color(red.get(),green.get(),blue.get()).getRGB(), true);
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
                Fonts.font35.drawString(mark, 8.5f, 10f, new Color(red.get(),green.get(),blue.get()).getRGB(), true);
                Fonts.font35.drawString(title, (float)(13.5 + Fonts.font35.getStringWidth(mark)), 9.0f, -1);
                break;
            }
        }
    }
    public static Color color(final int tick) {
        Color textColor = new Color(-1);
        textColor = ColorUtils.fade(5, tick * 20, new Color(red.get(),green.get(),blue.get()), 1.0f);
        return textColor;
    }
}
