package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.LiquidBounce
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
import net.ccbluex.liquidbounce.value.*
import net.minecraft.entity.player.EntityPlayer
import java.awt.Color
import net.ccbluex.liquidbounce.features.module.modules.world.BanChecker
import net.ccbluex.liquidbounce.utils.render.BlurUtils

@ElementInfo(name = "SessionInfo")
class SessionInfo(x: Double = 10.0, y: Double = 10.0, scale: Float = 1F) : Element(x, y, scale) {
    private val colorModeValue =
        ListValue("Color", arrayOf("Custom", "Sky", "CRainbow", "LiquidSlowly", "Fade", "Mixer"), "Custom")
    private val modeValue = ListValue("Mode", arrayOf("1", "2", "3"), "1")
    val colorRedValue = IntegerValue("Red", 255, 255, 255)
    val colorGreenValue = IntegerValue("Green", 255, 0, 255)
    val colorBlueValue = IntegerValue("Blue", 255, 0, 255)
    private val radiusValue = FloatValue("Radius", 4.25f, 0f, 10f)

    val lineValue = BoolValue("Line", true)
    private val gradientAmountValue = IntegerValue("Gradient-Amount", 25, 1, 50)
    private val saturationValue = FloatValue("Saturation", 0.9f, 0f, 1f)
    private val brightnessValue = FloatValue("Brightness", 1f, 0f, 1f)
    private val skyDistanceValue = IntegerValue("Sky-Distance", 2, -4, 4)
    private val cRainbowSecValue = IntegerValue("CRainbow-Seconds", 2, 1, 10)
    private val cRainbowDistValue = IntegerValue("CRainbow-Distance", 2, 1, 6)
    private val mixerSecValue = IntegerValue("Mixer-Seconds", 2, 1, 10)
    private val mixerDistValue = IntegerValue("Mixer-Distance", 2, 0, 10)
    private val liquidSlowlyDistanceValue = IntegerValue("LiquidSlowly-Distance", 90, 1, 90)
    private val fadeDistanceValue = IntegerValue("Fade-Distance", 50, 1, 100)
    private val blurValue = BoolValue("Blur", false)
    private val blurStrength = IntegerValue("BlurStrength", 10, 1, 60, { blurValue.get() })

    val counter = intArrayOf(0)
    val rainbowType = colorModeValue.get()
    val color = Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get(),255).rgb

    override fun drawElement(): Border {
        val target: EntityPlayer? = mc.thePlayer
        val convertedTarget = target!!
        if (modeValue.get().equals("1")) {
            for (i in 0..(gradientAmountValue.get() - 1)) {
                RenderUtils.drawCircleRect(0f, 0f, 165f, 63f, 5f, Color(0, 0, 0, 120).rgb)
                RenderUtils.drawRect(
                    0f, 12f, 165f, 13f,
                    when (rainbowType) {
                        "CRainbow" -> RenderUtils.getRainbowOpaque(
                            cRainbowSecValue.get(),
                            saturationValue.get(),
                            brightnessValue.get(),
                            i * cRainbowDistValue.get()
                        )

                        "Sky" -> RenderUtils.SkyRainbow(
                            i * skyDistanceValue.get(),
                            saturationValue.get(),
                            brightnessValue.get()
                        )

                        "Mixer" -> ColorMixer.getMixedColor(i * mixerDistValue.get(), mixerSecValue.get()).rgb
                        "Fade" -> ColorUtils.fade(
                            Color(
                                colorRedValue.get(),
                                colorGreenValue.get(),
                                colorBlueValue.get(),
                                255
                            ), i * fadeDistanceValue.get(), 100
                        ).rgb
                        "LiquidSlowly" -> ColorUtils.LiquidSlowly(System.nanoTime(), i * liquidSlowlyDistanceValue.get(), saturationValue.get(), brightnessValue.get())!!.rgb
                        else -> color
                    }
                )
                Fonts.fontTahoma.drawString(
                    "SessionInfo",
                    165 / 2 - Fonts.fontTahoma.getStringWidth("SessionInfo") / 2F,
                    3F,
                    Color(0xFFFFFF).rgb
                )
                Fonts.fontSFUI35.drawString("Playtime", 2F, 15F, Color(0xFFFFFF).rgb)
                Fonts.fontSFUI35.drawString(
                    SessionUtils.getFormatSessionTime(),
                    165 - Fonts.fontSFUI35.getStringWidth(SessionUtils.getFormatSessionTime()) - 3F,
                    15F,
                    Color(0xFFFFFF).rgb
                )
                Fonts.fontSFUI35.drawString("Kills", 2F, 27F, Color(0xFFFFFF).rgb)
                Fonts.fontSFUI35.drawString(
                    "${KillAura.CombatListener.killCounts}",
                    165 - Fonts.fontSFUI35.getStringWidth("${KillAura.CombatListener.killCounts}") - 3F,
                    27F,
                    Color(0xFFFFFF).rgb
                )
                Fonts.fontSFUI35.drawString("Name", 2F, 39F, Color(0xFFFFFF).rgb)
                Fonts.fontSFUI35.drawString(
                    convertedTarget.name,
                    165 - Fonts.fontSFUI35.getStringWidth(convertedTarget.name) - 3F,
                    39F,
                    Color(0xFFFFFF).rgb
                )
                Fonts.fontSFUI35.drawString("Server", 2F, 51F, Color(0xFFFFFF).rgb)
                Fonts.fontSFUI35.drawString(
                    ServerUtils.getRemoteIp(),
                    165 - Fonts.fontSFUI35.getStringWidth(ServerUtils.getRemoteIp()) - 3F,
                    51F,
                    Color(0xFFFFFF).rgb
                )
            }
        }
        if (modeValue.get().equals("2")) {
            for (i in 0..(gradientAmountValue.get() - 1)) {
                RenderUtils.drawRect(0f, 0f, 165f, 63f, Color(0, 0, 0, 120).rgb)
                RenderUtils.drawRect(
                    0f, 0f, 165f, 1f,
                    when (rainbowType) {
                        "CRainbow" -> RenderUtils.getRainbowOpaque(
                            cRainbowSecValue.get(),
                            saturationValue.get(),
                            brightnessValue.get(),
                            i * cRainbowDistValue.get()
                        )

                        "Sky" -> RenderUtils.SkyRainbow(
                            i * skyDistanceValue.get(),
                            saturationValue.get(),
                            brightnessValue.get()
                        )

                        "Mixer" -> ColorMixer.getMixedColor(i * mixerDistValue.get(), mixerSecValue.get()).rgb
                        "Fade" -> ColorUtils.fade(
                            Color(
                                colorRedValue.get(),
                                colorGreenValue.get(),
                                colorBlueValue.get(),
                                255
                            ), i * fadeDistanceValue.get(), 100
                        ).rgb
                        "LiquidSlowly" -> ColorUtils.LiquidSlowly(System.nanoTime(), i * liquidSlowlyDistanceValue.get(), saturationValue.get(), brightnessValue.get())!!.rgb
                        else -> color
                    }
                )
                Fonts.fontTahoma.drawString("SessionInfo", 2F, 4F, Color(0xFFFFFF).rgb)
                Fonts.fontSFUI35.drawString("Playtime", 2F, 15F, Color(0xFFFFFF).rgb)
                Fonts.fontSFUI35.drawString(
                    SessionUtils.getFormatSessionTime(),
                    165 - Fonts.fontSFUI35.getStringWidth(SessionUtils.getFormatSessionTime()) - 3F,
                    15F,
                    Color(0xFFFFFF).rgb
                )
                Fonts.fontSFUI35.drawString("Kills", 2F, 27F, Color(0xFFFFFF).rgb)
                Fonts.fontSFUI35.drawString(
                    "${KillAura.CombatListener.killCounts}",
                    165 - Fonts.fontSFUI35.getStringWidth("${KillAura.CombatListener.killCounts}") - 3F,
                    27F,
                    Color(0xFFFFFF).rgb
                )
                Fonts.fontSFUI35.drawString("Name", 2F, 39F, Color(0xFFFFFF).rgb)
                Fonts.fontSFUI35.drawString(
                    convertedTarget.name,
                    165 - Fonts.fontSFUI35.getStringWidth(convertedTarget.name) - 3F,
                    39F,
                    Color(0xFFFFFF).rgb
                )
                Fonts.fontSFUI35.drawString("Server", 2F, 51F, Color(0xFFFFFF).rgb)
                Fonts.fontSFUI35.drawString(
                    ServerUtils.getRemoteIp(),
                    165 - Fonts.fontSFUI35.getStringWidth(ServerUtils.getRemoteIp()) - 3F,
                    51F,
                    Color(0xFFFFFF).rgb
                )
                return Border(0f, 0f, 165f, 63f)
            }
        }
        if (modeValue.get().equals("3")) {
            val fontRenderer = Fonts.font35
            val y2 = fontRenderer.height * 4 + 11.0
            val x2 = 140.0

            var durationInMillis: Long = System.currentTimeMillis() - LiquidBounce.playTimeStart
            var second = durationInMillis / 1000 % 60
            var minute = durationInMillis / (1000 * 60) % 60
            var hour = durationInMillis / (1000 * 60 * 60) % 24
            var time: String
            time = String.format("%02dh %02dm %02ds", hour, minute, second)

            if (blurValue.get()) {
                BlurUtils.blurAreaRounded(
                    -14f,
                    -35f,
                    x2.toFloat() + 4,
                    y2.toFloat() - 10 + 10,
                    radiusValue.get(),
                    blurStrength.get().toFloat()
                )
            }
            RenderUtils.drawRoundedRect(
                -6f,
                -15f,
                x2.toFloat() + 4,
                y2.toFloat() - 10 + 10,
                radiusValue.get(),
                Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get(), 120).rgb
            )
            if (lineValue.get()) {
                val barLength = 142.toDouble()
                for (i in 0..(gradientAmountValue.get() - 1)) {
                    val barStart = i.toDouble() / gradientAmountValue.get().toDouble() * barLength
                    val barEnd = (i + 1).toDouble() / gradientAmountValue.get().toDouble() * barLength
                    RenderUtils.drawGradientSideways(
                        -2.0 + barStart, -2.5, -2.0 + barEnd, -1.0,
                        when (rainbowType) {
                            "CRainbow" -> RenderUtils.getRainbowOpaque(
                                cRainbowSecValue.get(),
                                saturationValue.get(),
                                brightnessValue.get(),
                                i * cRainbowDistValue.get()
                            )

                            "Sky" -> RenderUtils.SkyRainbow(
                                i * skyDistanceValue.get(),
                                saturationValue.get(),
                                brightnessValue.get()
                            )

                            "Mixer" -> ColorMixer.getMixedColor(i * mixerDistValue.get(), mixerSecValue.get()).rgb
                            "Fade" -> ColorUtils.fade(
                                Color(
                                    colorRedValue.get(),
                                    colorGreenValue.get(),
                                    colorBlueValue.get(),
                                    255
                                ), i * fadeDistanceValue.get(), 100
                            ).rgb
                            "LiquidSlowly" -> ColorUtils.LiquidSlowly(System.nanoTime(), i * liquidSlowlyDistanceValue.get(), saturationValue.get(), brightnessValue.get())!!.rgb
                            else -> color
                        },
                        when (rainbowType) {
                            "CRainbow" -> RenderUtils.getRainbowOpaque(
                                cRainbowSecValue.get(),
                                saturationValue.get(),
                                brightnessValue.get(),
                                i * cRainbowDistValue.get()
                            )

                            "Sky" -> RenderUtils.SkyRainbow(
                                i * skyDistanceValue.get(),
                                saturationValue.get(),
                                brightnessValue.get()
                            )

                            "Mixer" -> ColorMixer.getMixedColor(i * mixerDistValue.get(), mixerSecValue.get()).rgb
                            "Fade" -> ColorUtils.fade(
                                Color(
                                    colorRedValue.get(),
                                    colorGreenValue.get(),
                                    colorBlueValue.get(),
                                    255
                                ), i * fadeDistanceValue.get(), 100
                            ).rgb
                            "LiquidSlowly" -> ColorUtils.LiquidSlowly(System.nanoTime(), i * liquidSlowlyDistanceValue.get(), saturationValue.get(), brightnessValue.get())!!.rgb
                            else -> color
                        }
                    )
                }
            }

            val watchdoglmbans = BanChecker.WATCHDOG_BAN_LAST_MIN.toString()
            val stafflmbans = BanChecker.STAFF_BAN_LAST_MIN.toString()
            Fonts.font35.drawStringWithShadow("Session Information", x2.toFloat() / 4f, -10f, Color.WHITE.rgb)
            Fonts.font35.drawStringWithShadow("Play Time: ", 2f, fontRenderer.height + -6f, Color.WHITE.rgb)
            Fonts.font35.drawStringWithShadow(time, 92f, fontRenderer.height + -6f, Color.WHITE.rgb)
            Fonts.font35.drawStringWithShadow("Player Killed ", 2f, fontRenderer.height * 2 + -4f, Color.WHITE.rgb)
            Fonts.font35.drawStringWithShadow(
                "" + KillAura.CombatListener.killCounts + "",
                135f,
                fontRenderer.height * 2 + -4f,
                Color.WHITE.rgb
            )
            Fonts.font35.drawStringWithShadow("GameWons", 2f, fontRenderer.height * 3 + -2f, Color.WHITE.rgb)
            Fonts.font35.drawStringWithShadow(
                "" + KillAura.CombatListener.win,
                135f,
                fontRenderer.height * 3 + -2f,
                Color.WHITE.rgb
            )
            Fonts.font35.drawStringWithShadow("Staff/Watchdog Bans", 2f, fontRenderer.height * 4 + 0f, Color.WHITE.rgb)
            Fonts.fontSFUI35.drawStringWithShadow(
                stafflmbans + "/" + watchdoglmbans,
                127f,
                fontRenderer.height * 4 + 0f,
                Color.WHITE.rgb
            )
        }
        return Border(0f, 0f, 165f, 63f)
    }
}
