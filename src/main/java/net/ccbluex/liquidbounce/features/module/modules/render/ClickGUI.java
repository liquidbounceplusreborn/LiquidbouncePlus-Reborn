/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.render;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.astolfo.AstolfoClickGui;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.dropdown.DropdownGUI;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.flux.FluxClassic;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.*;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.novoline.ClickyUI;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.otcV2.OtcClickGUi;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.zeroday.ClickUI;
import net.ccbluex.liquidbounce.utils.render.ColorUtils;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.ccbluex.liquidbounce.value.*;
import org.lwjgl.input.Keyboard;

import java.awt.*;

@ModuleInfo(name = "ClickGUI", description = "Opens the ClickGUI.", category = ModuleCategory.RENDER, keyBind = Keyboard.KEY_RSHIFT, forceNoSound = true, onlyEnable = true)
public class ClickGUI extends Module {
    private final ListValue styleValue = new ListValue("Style",
            new String[]{
                    "LiquidBounce",
                    "Null",
                    "Slowly",
                    "Black",
                    "White",
                    "Test",
                    "Novoline",
                    "Flux",
                    "Zeroday",
                    "Chocolate",
                    "OneTap",
                    "Astolfo",
                    "DropDown"},
            "LiquidBounce") {
        @Override
        protected void onChanged(final String oldValue, final String newValue) {
            updateStyle();
        }
    };

    public final FloatValue scaleValue = new FloatValue("Scale", 1F, 0.4F, 2F);
    public final IntegerValue maxElementsValue = new IntegerValue("MaxElements", 15, 1, 20);

    public static final ListValue colorModeValue = new ListValue("Color", new String[]{"Custom", "Sky", "Rainbow", "LiquidSlowly", "Fade", "Mixer"}, "Custom");
    public static final IntegerValue colorRedValue = new IntegerValue("Red", 0, 0, 255, () -> colorModeValue.get().equalsIgnoreCase("custom"));
    public static final IntegerValue colorGreenValue = new IntegerValue("Green", 160, 0, 255, () -> colorModeValue.get().equalsIgnoreCase("custom"));
    public static final IntegerValue colorBlueValue = new IntegerValue("Blue", 255, 0, 255, () -> colorModeValue.get().equalsIgnoreCase("custom"));
    public static final FloatValue saturationValue = new FloatValue("Saturation", 1F, 0F, 1F);
    public static final FloatValue brightnessValue = new FloatValue("Brightness", 1F, 0F, 1F);
    public static final IntegerValue mixerSecondsValue = new IntegerValue("Seconds", 2, 1, 10);
    public final ListValue backgroundValue = new ListValue("Background", new String[]{"Default", "Gradient", "None"}, "Default");

    public final IntegerValue gradStartValue = new IntegerValue("GradientStartAlpha", 255, 0, 255, () -> backgroundValue.get().equalsIgnoreCase("gradient"));
    public final IntegerValue gradEndValue = new IntegerValue("GradientEndAlpha", 0, 0, 255, () -> backgroundValue.get().equalsIgnoreCase("gradient"));

    public final ListValue animationValue = new ListValue("Animation", new String[]{"Azura", "Slide", "SlideBounce", "Zoom", "ZoomBounce", "None"}, "Azura");
    public final FloatValue animSpeedValue = new FloatValue("AnimSpeed", 1F, 0.01F, 5F, "x");

    public final FloatValue scale = new FloatValue("AstolfoScale", 1f, 0f, 10f, () -> styleValue.get().equalsIgnoreCase("astolfo"));;
    public final FloatValue scroll = new FloatValue("Scroll", 20f, 0f, 200f, () -> styleValue.get().equalsIgnoreCase("astolfo"));;
    public final BoolValue getGetClosePrevious = new BoolValue("ClosePrevious",false, () -> styleValue.get().equalsIgnoreCase("dropdown"));
    public final BoolValue disp = new BoolValue("DisplayValue", false, () -> styleValue.get().equalsIgnoreCase("onetap"));

    DropdownGUI dropdownGUI = null;
    AstolfoClickGui astolfoClickGui = null;

    public static Color generateColor() {
        Color c = new Color(255, 255, 255, 255);
        switch (colorModeValue.get().toLowerCase()) {
            case "custom":
                c = new Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get());
                break;
            case "rainbow":
                c = new Color(RenderUtils.getRainbowOpaque(mixerSecondsValue.get(), saturationValue.get(), brightnessValue.get(), 0));
                break;
            case "sky":
                c = RenderUtils.skyRainbow(0, saturationValue.get(), brightnessValue.get());
                break;
            case "liquidslowly":
                c = ColorUtils.LiquidSlowly(System.nanoTime(), 0, saturationValue.get(), brightnessValue.get());
                break;
            case "fade":
                c = ColorUtils.fade(new Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get()), 0, 100);
                break;
            case "mixer":
                c = ColorMixer.getMixedColor(0, mixerSecondsValue.get());
                break;
        }
        return c;
    }

    private DropdownGUI getDropdownGUI() {
        if (dropdownGUI == null)
            dropdownGUI = new DropdownGUI();
        return dropdownGUI;
    }

    private AstolfoClickGui getAstolfoClickGui() {
        if (astolfoClickGui == null)
            astolfoClickGui = new AstolfoClickGui();
        return astolfoClickGui;
    }


    @Override
    public void onEnable() {
        switch (styleValue.get().toLowerCase()) {
            case "novoline":
                mc.displayGuiScreen(new ClickyUI());
                this.setState(false);
                break;
            case "flux":
                mc.displayGuiScreen(new FluxClassic());
                this.setState(false);
                break;
            case "zeroday":
                mc.displayGuiScreen(new ClickUI());
                this.setState(false);
                break;
            case "onetap":
                mc.displayGuiScreen(new OtcClickGUi());
                this.setState(false);
                break;
            case "chocolate":
                mc.displayGuiScreen(new SkeetStyle());
                break;
            case "dropdown":
                mc.displayGuiScreen(getDropdownGUI());
                break;
            case "astolfo":
                mc.displayGuiScreen(getAstolfoClickGui());
                break;
            default:
                updateStyle();
                mc.displayGuiScreen(LiquidBounce.clickGui);
                LiquidBounce.clickGui.progress = 0;
                LiquidBounce.clickGui.slide = 0;
                LiquidBounce.clickGui.lastMS = System.currentTimeMillis();
                mc.displayGuiScreen(LiquidBounce.clickGui);
        }
    }

    private void updateStyle() {
        switch(styleValue.get().toLowerCase()) {
            case "liquidbounce":
                LiquidBounce.clickGui.style = new LiquidBounceStyle();
                break;
            case "null":
                LiquidBounce.clickGui.style = new NullStyle();
                break;
            case "slowly":
                LiquidBounce.clickGui.style = new SlowlyStyle();
                break;
            case "black":
                LiquidBounce.clickGui.style = new BlackStyle();
                break;
            case "white":
                LiquidBounce.clickGui.style = new WhiteStyle();
                break;
            case "test":
                LiquidBounce.clickGui.style = new TestStyle();
                break;
        }
    }
}