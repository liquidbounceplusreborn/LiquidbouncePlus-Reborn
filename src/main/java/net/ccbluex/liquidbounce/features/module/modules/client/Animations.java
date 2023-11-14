/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.client;

import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;
import net.ccbluex.liquidbounce.value.BoolValue;
import net.ccbluex.liquidbounce.value.FloatValue;
import net.ccbluex.liquidbounce.value.IntegerValue;
import net.ccbluex.liquidbounce.value.ListValue;

@ModuleInfo(name = "Animations", description = "Render items Animations", category = ModuleCategory.CLIENT)
public class Animations extends Module {

    // some ListValue
    public static final ListValue Sword = new ListValue("Style", new String[]{
            "Normal", "SlideDown1", "SlideDown2" , "Slide", "Slide1", "Minecraft", "Remix", "Exhibition",
            "Avatar", "Swang", "Tap1", "Tap2", "Poke", "Push1", "Push2" , "Up" , "Shield", "Akrien", "VisionFX", "Swong","Swank",
            "SigmaOld", "ETB", "Rotate360", "SmoothFloat", "Strange" , "Reverse", "Zoom", "Move", "Stab", "Jello", "1.7", "Flux", "Stella", "Tifality","OldExhibition","Smooth"
        }, "Minecraft");

    // item general scale
    public static final FloatValue Scale = new FloatValue("Scale", 0.4f, 0f, 4f);

    // normal item position
    public static final FloatValue itemPosX = new FloatValue("ItemX", 0f, -1f, 1f);
    public static final FloatValue itemPosY = new FloatValue("ItemY", 0f, -1f, 1f);
    public static final FloatValue itemPosZ = new FloatValue("ItemZ", 0f, -1f, 1f);
    public static final FloatValue itemDistance = new FloatValue("ItemDistance", 1, 1, 5f);

    // change Position Blocking Sword
    public static final FloatValue blockPosX = new FloatValue("BlockingX", 0f, -1f, 1f);
    public static final FloatValue blockPosY = new FloatValue("BlockingY", 0f, -1f, 1f);
    public static final FloatValue blockPosZ = new FloatValue("BlockingZ", 0f, -1f, 1f);

    // modify item swing and rotate
    public static final IntegerValue SpeedSwing = new IntegerValue("Swing-Speed", 4, 0, 20);

    // custom animation sword
    public static final FloatValue mcSwordPos =  new FloatValue("MCPosOffset", 0.45f, 0, 0.5f, () -> Sword.get().equalsIgnoreCase("minecraft"));

    // fake blocking bruh
    public static final BoolValue fakeBlock = new BoolValue("Fake-Block", false);

    // block not everything
    public static final BoolValue blockEverything = new BoolValue("Block-Everything", false);

    public static final BoolValue swing = new BoolValue("FluxSwing", false);

    // gui animations
    public static final ListValue guiAnimations = new ListValue("Container-Animation", new String[]{"None", "Zoom", "Slide", "Smooth"}, "None");
    public static final ListValue vSlideValue = new ListValue("Slide-Vertical", new String[]{"None", "Upward", "Downward"}, "Downward", () -> guiAnimations.get().equalsIgnoreCase("slide"));
    public static final ListValue hSlideValue = new ListValue("Slide-Horizontal", new String[]{"None", "Right", "Left"}, "Right", () -> guiAnimations.get().equalsIgnoreCase("slide"));
    public static final IntegerValue animTimeValue = new IntegerValue("Container-AnimTime", 750, 0, 3000, () -> !guiAnimations.get().equalsIgnoreCase("none"));
    public static final ListValue tabAnimations = new ListValue("Tab-Animation", new String[]{"None", "Zoom", "Slide"}, "Zoom");

    // block crack
    public static final BoolValue noBlockParticles = new BoolValue("NoBlockParticles", false);

    //1.7

    public static final BoolValue oldBow = new BoolValue("1.7Bow", false);

    public static final BoolValue oldRod = new BoolValue("1.7Rod", false);

    public static final BoolValue oldSwing = new BoolValue("1.7Swing", false);

}
