package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.Side
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.FontValue
import net.ccbluex.liquidbounce.value.IntegerValue
import java.awt.Color

@ElementInfo(name = "SessionInfo") class SessionInfo(x: Double = 15.0, y: Double = 10.0, scale: Float = 1F, side: Side = Side(Side.Horizontal.LEFT, Side.Vertical.UP)) : Element(x, y, scale, side) {

    private val radiusValue = FloatValue("Radius", 4.25f, 0f, 10f)
    private val bgredValue = IntegerValue("Bg-R", 255, 0, 255)
    private val bggreenValue = IntegerValue("Bg-G", 255, 0, 255)
    private val bgblueValue = IntegerValue("Bg-B", 255, 0, 255)
    private val bgalphaValue = IntegerValue("Bg-Alpha", 150, 0, 255)

    val lineValue = BoolValue("Line", true)
    private val redValue = IntegerValue("Line-R", 255, 0, 255)
    private val greenValue = IntegerValue("Line-G", 255, 0, 255)
    private val blueValue = IntegerValue("Line-B", 255, 0, 255)
    private val colorRedValue2 = IntegerValue("Line-R2", 0, 0, 255)
    private val colorGreenValue2 = IntegerValue("Line-G2", 111, 0, 255)
    private val colorBlueValue2 = IntegerValue("Line-B2", 255, 0, 255)

    val fontValue = FontValue("Font", Fonts.font35)

    override fun drawElement(): Border? {
        val fontRenderer = fontValue.get()

        val y2 = fontRenderer.FONT_HEIGHT * 3 + 11.0
        val x2 = 140.0

        var durationInMillis: Long = System.currentTimeMillis() - LiquidBounce.playTimeStart
        var second = durationInMillis / 1000 % 60
        var minute = durationInMillis / (1000 * 60) % 60
        var hour = durationInMillis / (1000 * 60 * 60) % 24
        var time: String
        time = String.format("%02dh %02dm %02ds", hour, minute, second)

        RenderUtils.drawRoundedRect(-2f, -2f, x2.toFloat(), y2.toFloat(), radiusValue.get(), Color(bgredValue.get(), bggreenValue.get(), bgblueValue.get(), bgalphaValue.get()).rgb)
        if(lineValue.get()) {
            RenderUtils.drawGradientSideways(
                2.44,
                fontRenderer.FONT_HEIGHT + 2.5 + 0.0,
                138.0 + -2.44,
                fontRenderer.FONT_HEIGHT + 2.5 + 1.16,
                Color(redValue.get(), greenValue.get(), blueValue.get()).rgb,
                Color(colorRedValue2.get(), colorGreenValue2.get(), colorBlueValue2.get()).rgb
            )
        }
        fontRenderer.drawStringWithShadow("Session Info", x2.toFloat() / 4f, 3f, Color.WHITE.rgb)
        fontRenderer.drawStringWithShadow("Play Time: $time", 2f, fontRenderer.FONT_HEIGHT + 8f, Color.WHITE.rgb)
        fontRenderer.drawStringWithShadow("Players Killed: " + KillAura.CombatListener.killCounts,2f , fontRenderer.FONT_HEIGHT * 2 + 8f, Color.WHITE.rgb)
        return Border(-2f, -2f, x2.toFloat(), y2.toFloat())
    }
}