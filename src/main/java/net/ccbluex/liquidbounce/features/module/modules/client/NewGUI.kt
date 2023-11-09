/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.render.ColorMixer
import net.ccbluex.liquidbounce.ui.client.clickgui.newVer.NewUi.Companion.getInstance
import net.ccbluex.liquidbounce.utils.render.ColorUtils.LiquidSlowly
import net.ccbluex.liquidbounce.utils.render.ColorUtils.fade
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import org.lwjgl.input.Keyboard
import java.awt.Color
import java.util.*

@ModuleInfo(name = "NewGUI", description = "next generation clickgui.", category = ModuleCategory.CLIENT, forceNoSound = true, onlyEnable = true, keyBind = Keyboard.KEY_RSHIFT)
object NewGUI : Module() {
    val fastRenderValue = BoolValue("FastRender", false)
    private val colorModeValue = ListValue("Color", arrayOf("Custom", "Sky", "Rainbow", "LiquidSlowly", "Fade", "Mixer"), "Custom")
    private val colorRedValue = IntegerValue("Red", 0, 0, 255)
    private val colorGreenValue = IntegerValue("Green", 140, 0, 255)
    private val colorBlueValue = IntegerValue("Blue", 255, 0, 255)
    val left = FloatValue("Left", 30f, 0f, 1000f) { false }
    val right = FloatValue("Right", 30f, 0f, 1000f) { false }
    val top = FloatValue("Top", 30f, 0f, 1000f) { false }
    val bottom = FloatValue("Bottom", 30f, 0f, 1000f) { false }

    private val saturationValue = FloatValue("Saturation", 1f, 0f, 1f)
    private val brightnessValue = FloatValue("Brightness", 1f, 0f, 1f)
    private val mixerSecondsValue = IntegerValue("Seconds", 2, 1, 10)

    val accentColor: Color
        get() {
            var c = Color(255, 255, 255, 255)
            when (colorModeValue.get().lowercase(Locale.getDefault())) {
                "custom" -> c = Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get())
                "rainbow" -> c = Color(RenderUtils.getRainbowOpaque(mixerSecondsValue.get(), saturationValue.get(), brightnessValue.get(), 0))
                "sky" -> c = RenderUtils.skyRainbow(0, saturationValue.get(), brightnessValue.get())
                "liquidslowly" -> c = LiquidSlowly(System.nanoTime(), 0, saturationValue.get(), brightnessValue.get())
                "fade" -> c = fade(Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get()), 0, 100)
                "mixer" -> c = ColorMixer.getMixedColor(0, mixerSecondsValue.get())
            }
            return c
        }

    override fun onEnable() {
        mc.displayGuiScreen(getInstance())
    }
}
