package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.ccbluex.liquidbounce.features.module.modules.render.ColorMixer
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.ServerUtils
import net.ccbluex.liquidbounce.utils.SessionUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.entity.player.EntityPlayer
import java.awt.Color

@ElementInfo(name = "SessionInfo")
class SessionInfo(x: Double = 10.0, y: Double = 10.0, scale: Float = 1F) : Element(x, y, scale) {
    private val colorModeValue = ListValue("Color", arrayOf("Custom","Sky", "CRainbow", "LiquidSlowly", "Fade", "Mixer"), "Custom")
    private val modeValue = ListValue("Mode", arrayOf("1", "2"), "1")
    val colorRedValue = IntegerValue("Red", 255,255, 255)
    val colorGreenValue = IntegerValue("Green", 255, 0, 255)
    val colorBlueValue = IntegerValue("Blue", 255, 0, 255)
    private val saturationValue = FloatValue("Saturation", 0.9f, 0f, 1f)
    private val brightnessValue = FloatValue("Brightness", 1f, 0f, 1f)
    private val skyDistanceValue = IntegerValue("Sky-Distance", 2, -4, 4)
    private val cRainbowSecValue = IntegerValue("CRainbow-Seconds", 2, 1, 10)
    private val cRainbowDistValue = IntegerValue("CRainbow-Distance", 2, 1, 6)
    private val mixerSecValue = IntegerValue("Mixer-Seconds", 2, 1, 10)
    private val mixerDistValue = IntegerValue("Mixer-Distance", 2, 0, 10)
    private val liquidSlowlyDistanceValue = IntegerValue("LiquidSlowly-Distance", 90, 1, 90)
    private val fadeDistanceValue = IntegerValue("Fade-Distance", 50, 1, 100)
    
    val counter = intArrayOf(0)

    val colorMode = colorModeValue.get()
    var Sky = RenderUtils.SkyRainbow(counter[0] * (skyDistanceValue.get() * 50), saturationValue.get(), brightnessValue.get())
    var CRainbow = RenderUtils.getRainbowOpaque(cRainbowSecValue.get(), saturationValue.get(), brightnessValue.get(), counter[0] * (50 * cRainbowDistValue.get()))
    var FadeColor = ColorUtils.fade(Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get(), 255), fadeDistanceValue.get(), 100).rgb
    val test = ColorUtils.LiquidSlowly(System.nanoTime(), liquidSlowlyDistanceValue.get(), saturationValue.get(), brightnessValue.get())?.rgb
    var LiquidSlowly : Int = test!!
    val mixerColor = ColorMixer.getMixedColor(mixerDistValue.get() * 10, mixerSecValue.get()).rgb
    val customColor = Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get(),255).rgb


    override fun drawElement(): Border {
        val target: EntityPlayer? = mc.thePlayer
        val convertedTarget = target!!
        if (modeValue.get().equals("1")) {
            RenderUtils.drawCircleRect(0f, 0f, 165f, 63f ,5f ,Color(0, 0, 0, 120).rgb)
            RenderUtils.drawRect(0f, 12f, 165f, 13f,
                when {
                colorMode.equals("Sky", ignoreCase = true) -> Sky
                colorMode.equals("CRainbow", ignoreCase = true) -> CRainbow
                colorMode.equals("LiquidSlowly", ignoreCase = true) -> LiquidSlowly
                colorMode.equals("Fade", ignoreCase = true) -> FadeColor
                colorMode.equals("Mixer", ignoreCase = true) -> mixerColor
                else -> customColor
                }
            )
            Fonts.fontTahoma.drawString("SessionInfo" , 165 / 2 - Fonts.fontTahoma.getStringWidth("SessionInfo") / 2, 3, Color(0xFFFFFF).rgb)
        }
        if (modeValue.get().equals("2")) {
            RenderUtils.drawRect(0f, 0f, 165f, 63f ,Color(0, 0, 0, 120).rgb)
            RenderUtils.drawRect(0f, 0f, 165f, 1f,                 when {
                colorMode.equals("Sky", ignoreCase = true) -> Sky
                colorMode.equals("CRainbow", ignoreCase = true) -> CRainbow
                colorMode.equals("LiquidSlowly", ignoreCase = true) -> LiquidSlowly
                colorMode.equals("Fade", ignoreCase = true) -> FadeColor
                colorMode.equals("Mixer", ignoreCase = true) -> mixerColor
                else -> customColor
            })
            Fonts.fontTahoma.drawString("SessionInfo" , 2, 4, Color(0xFFFFFF).rgb)
        }
        Fonts.fontSFUI35.drawString("Playtime"  , 2 , 15 , Color(0xFFFFFF).rgb)
        Fonts.fontSFUI35.drawString(SessionUtils.getFormatSessionTime() , 165 - Fonts.fontSFUI35.getStringWidth(SessionUtils.getFormatSessionTime()) - 3 , 15 , Color(0xFFFFFF).rgb)
        Fonts.fontSFUI35.drawString("Kills" , 2 , 27 , Color(0xFFFFFF).rgb)
        Fonts.fontSFUI35.drawString("${KillAura.CombatListener.killCounts}" , 165 - Fonts.fontSFUI35.getStringWidth("${KillAura.CombatListener.killCounts}") - 3 , 27 , Color(0xFFFFFF).rgb)
        Fonts.fontSFUI35.drawString("Name", 2 , 39 , Color(0xFFFFFF).rgb)
        Fonts.fontSFUI35.drawString(convertedTarget.name , 165 - Fonts.fontSFUI35.getStringWidth(convertedTarget.name) - 3 , 39 , Color(0xFFFFFF).rgb)
        Fonts.fontSFUI35.drawString("Server"  , 2 , 51 , Color(0xFFFFFF).rgb)
        Fonts.fontSFUI35.drawString(ServerUtils.getRemoteIp()  , 165 - Fonts.fontSFUI35.getStringWidth(ServerUtils.getRemoteIp()) - 3 , 51 , Color(0xFFFFFF).rgb)
        return Border(0f, 0f, 165f, 63f)
    }
}
