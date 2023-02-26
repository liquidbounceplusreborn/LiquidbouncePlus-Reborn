
package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.ui.client.hud.designer.GuiHudDesigner
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.Side
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notifications.Companion.blue2Value
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notifications.Companion.blueValue
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notifications.Companion.green2Value
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notifications.Companion.greenValue
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notifications.Companion.red2Value
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notifications.Companion.redValue
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.render.*
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.math.BigDecimal
import kotlin.math.max

/**
 * CustomHUD Notification element
 */
@ElementInfo(name = "Notifications", single = true)
class Notifications(
    x: Double = 0.0, y: Double = 30.0, scale: Float = 1F,
    side: Side = Side(Side.Horizontal.RIGHT, Side.Vertical.DOWN)): Element(x, y, scale, side) {

    /**
     * Example notification for CustomHUD designer
     */
    private val exampleNotification = Notification("Notification", "This is an example notification.", NotifyType.INFO)
    companion object {
        val styleValue = ListValue("Mode", arrayOf("Classic", "IntelliJIDEA","TenacityOld","Novoline"), "Classic")
        val redValue = IntegerValue("Red", 255, 0, 255)
        val greenValue = IntegerValue("Green", 255, 0, 255)
        val blueValue = IntegerValue("Blue", 255, 0, 255)
        val red2Value = IntegerValue("Red2", 255, 0, 255)
        val green2Value = IntegerValue("Green2", 255, 0, 255)
        val blue2Value = IntegerValue("Blue2", 255, 0, 255)
    
    }

    /**
     * Draw element
     */
    override fun drawElement(): Border? {
        val notifications = mutableListOf<Notification>()
        for ((index, notify) in LiquidBounce.hud.notifications.withIndex()) {
            GL11.glPushMatrix()

            if (notify.drawNotification(index, Companion, renderX.toFloat(), renderY.toFloat())) {
                notifications.add(notify)
            }

            GL11.glPopMatrix()
        }
        for (notify in notifications) {
            LiquidBounce.hud.notifications.remove(notify)
        }

        if (mc.currentScreen is GuiHudDesigner) {
            if (!LiquidBounce.hud.notifications.contains(exampleNotification))
                LiquidBounce.hud.addNotification(exampleNotification)

            exampleNotification.fadeState = FadeState.STAY
            exampleNotification.displayTime = System.currentTimeMillis()
//            exampleNotification.x = exampleNotification.textLength + 8F

            return Border(-exampleNotification.width.toFloat(), -exampleNotification.height.toFloat(), 0F, 0F)

        }

        return null
    }

}

class Notification(
    val title: String,
    val content: String,
    val type: NotifyType,
    val time: Int = 1500,
    val animeTime: Int = 500,
) {
    val width = 100.coerceAtLeast(
        Fonts.fontSFUI35.getStringWidth(this.title)
            .coerceAtLeast(Fonts.fontSFUI35.getStringWidth(this.content)) + 12
    )
    private val notifyDir = "liquidbounce+/notif/intellj/"
    val height = 30
    private var firstY = 0f
    private var firstYz = 0
    var x = 0f
    var textLength = Fonts.minecraftFont.getStringWidth(content) + 10

    var fadeState = FadeState.IN
    var nowY = -height
    var displayTime = System.currentTimeMillis()
    var animeXTime = System.currentTimeMillis()
    var animeYTime = System.currentTimeMillis()

    /**
     * Draw notification
     */
    fun drawNotification(index: Int, parent: Notifications.Companion, originalX: Float, originalY: Float): Boolean {
        val nowTime = System.currentTimeMillis()
        val style = parent.styleValue.get()
        val realY = -(index + 1) * height
        var pct = (nowTime - animeXTime) / animeTime.toDouble()
        if (style.equals("Classic")) {
            val image = ResourceLocation("liquidbounce+/ui/" + type.name + ".png")
            //Y-Axis Animation
            if (nowY != realY) {
                var pct = (nowTime - animeYTime) / animeTime.toDouble()
                if (pct > 1) {
                    nowY = realY
                    pct = 1.0
                } else {
                    pct = EaseUtils.easeOutExpo(pct)
                }
                GL11.glTranslated(0.0, (realY - nowY) * pct, 0.0)
            } else {
                animeYTime = nowTime
            }
            GL11.glTranslated(0.0, nowY.toDouble(), 0.0)

            //X-Axis Animation
            when (fadeState) {
                FadeState.IN -> {
                    if (pct > 1) {
                        fadeState = FadeState.STAY
                        animeXTime = nowTime
                        pct = 1.0
                    }
                    pct = EaseUtils.easeOutExpo(pct)
                }

                FadeState.STAY -> {
                    pct = 1.0
                    if ((nowTime - animeXTime) > time) {
                        fadeState = FadeState.OUT
                        animeXTime = nowTime
                    }
                }

                FadeState.OUT -> {
                    if (pct > 1) {
                        fadeState = FadeState.END
                        animeXTime = nowTime
                        pct = 1.0
                    }
                    pct = 1 - EaseUtils.easeInExpo(pct)
                }

                FadeState.END -> {
                    return true
                }
            }
            GL11.glTranslated(width - (width * pct), 0.0, 0.0)
            GL11.glTranslatef(-width.toFloat(), 0F, 0F)
            RenderUtils.drawShadow(-22F, 0F, width.toFloat() + 22, height.toFloat())
            RenderUtils.drawRect(-22F, 0F, width.toFloat(), height.toFloat(), type.renderColor)
            RenderUtils.drawRect(-22F, 0F, width.toFloat(), height.toFloat(), Color(0, 0, 0, 100))
            RenderUtils.drawRect(
                -22F,
                height - 2F,
                max(width - width * ((nowTime - displayTime) / (animeTime * 2F + time)), -22F),
                height.toFloat(),
                type.renderColor
            )
            Fonts.fontTahoma.drawString(title, 6F, 4F, -1)
            Fonts.fontSFUI35.drawString(content, 6F, 17F, -1)
            RenderUtils.drawImage(image, -19, 3, 22, 22)
            GlStateManager.resetColor()
        }
        if (style.equals("IntelliJIDEA")) {


            if (nowY != realY) {
                if (pct > 1) {
                    nowY = realY
                    pct = 1.0
                } else {
                    pct = EaseUtils.easeOutExpo(pct)
                }
                GL11.glTranslated(0.0, (realY - nowY) * pct, 0.0)
            } else {
                animeYTime = nowTime
            }
            GL11.glTranslated(0.0, nowY.toDouble(), 0.0)
            when (fadeState) {
                FadeState.IN -> {
                    if (pct > 1) {
                        fadeState = FadeState.STAY
                        animeXTime = nowTime
                        pct = 1.0
                    }
                    pct = EaseUtils.easeOutExpo(pct)
                }

                FadeState.STAY -> {
                    pct = 1.0
                    if ((nowTime - animeXTime) > time) {
                        fadeState = FadeState.OUT
                        animeXTime = nowTime
                    }
                }

                FadeState.OUT -> {
                    if (pct > 1) {
                        fadeState = FadeState.END
                        animeXTime = nowTime
                        pct = 1.0
                    }
                    pct = 1 - EaseUtils.easeInExpo(pct)
                }

                FadeState.END -> {
                    return true
                }
            }

            var y = firstYz
            val kek = -x - 1 - 20F

            GlStateManager.resetColor()
            Stencil.write(true)

            if (type == NotifyType.ERROR) {
                RenderUtils.drawRect(
                    -textLength - 23f + 5,
                    -y.toFloat(),
                    kek + 21f,
                    height.toFloat(),
                    Color(115, 69, 75).rgb
                )
                RenderUtils.drawRect(
                    -textLength.toFloat() - 22f + 5,
                    -y.toFloat() + 1,
                    kek + 20,
                    height.toFloat() - 1,
                    Color(89, 61, 65).rgb
                )
                Fonts.minecraftFont.drawStringWithShadow(
                    "IDE Error:",
                    -textLength.toFloat() - 1,
                    -y.toFloat() + 2,
                    Color(249, 130, 108).rgb
                )
            }
            if (type == NotifyType.INFO) {
                RenderUtils.drawRect(
                    -textLength - 23f + 5,
                    -y.toFloat(),
                    textLength.toFloat() - 152,
                    height.toFloat(),
                    Color(70, 94, 115).rgb
                )
                RenderUtils.drawRect(
                    -textLength.toFloat() - 22f + 5,
                    -y + 1f,
                    textLength.toFloat() - 153,
                    height.toFloat() - 1,
                    Color(61, 72, 87).rgb
                )
                Fonts.minecraftFont.drawStringWithShadow(
                    "IDE Information:",
                    -textLength.toFloat() - 1,
                    -y.toFloat() + 2,
                    Color(119, 145, 147).rgb
                )
            }
            if (type == NotifyType.SUCCESS) {
                RenderUtils.drawRect(
                    -textLength - 23f + 5,
                    -y.toFloat(),
                    kek + 21f,
                    height.toFloat(),
                    Color(67, 104, 67).rgb
                )
                RenderUtils.drawRect(
                    -textLength.toFloat() - 22f + 5,
                    -y + 1f,
                    kek + 20,
                    height.toFloat() - 1,
                    Color(55, 78, 55).rgb
                )
                Fonts.minecraftFont.drawStringWithShadow(
                    "IDE Success:",
                    -textLength.toFloat() - 1,
                    -y.toFloat() + 2,
                    Color(10, 142, 2).rgb
                )
            }
            if (type == NotifyType.WARNING) {
                RenderUtils.drawRect(
                    -textLength - 23f + 5,
                    -y.toFloat(),
                    kek + 21f,
                    height.toFloat(),
                    Color(103, 103, 63).rgb
                )
                RenderUtils.drawRect(
                    -textLength.toFloat() - 22f + 5,
                    -y + 1f,
                    kek + 20,
                    height.toFloat() - 1,
                    Color(80, 80, 57).rgb
                )
                Fonts.minecraftFont.drawStringWithShadow(
                    "IDE Warning:",
                    -textLength.toFloat() - 1,
                    -y.toFloat() + 2,
                    Color(175, 163, 0).rgb
                )
            }
            Stencil.erase(true)

            GlStateManager.resetColor()

            Stencil.dispose()

            GL11.glPushMatrix()
            GlStateManager.disableAlpha()
            GlStateManager.resetColor()
            GL11.glColor4f(1F, 1F, 1F, 1F)
            val pn = ResourceLocation(
                when (type.name) {
                    "SUCCESS" -> "liquidbounce+/noti/intellj/checkmark.png"
                    "ERROR" -> "liquidbounce+/noti/intellj/error.png"
                    "WARNING" -> "liquidbounce+/noti/intellj/warning.png"
                    "INFO" -> "liquidbounce+/noti/intellj/info.png"
                    else -> "liquidbounce+/error/intellj/error1.png"
                }
            )
            RenderUtils.drawImage(pn, -textLength - 11, -y + 2, 7, 7)
            GlStateManager.enableAlpha()
            GL11.glPopMatrix()

            Fonts.minecraftFont.drawStringWithShadow(content, -textLength.toFloat() - 1, -y.toFloat() + 15, -1)
        }
        if (style.equals("TenacityOld")) {

            val realY = -(index + 1) * (height + 10)
            val nowTime = System.currentTimeMillis()

            val pn = ResourceLocation(
                when (type.name) {
                    "SUCCESS" -> "liquidbounce+/noti/SUCCESS.png"
                    "ERROR" -> "liquidbounce+/noti/ERROR.png"
                    "WARNING" -> "liquidbounce+/noti/WARNING.png"
                    "INFO" -> "liquidbounce+/noti/INFO.png"
                    else -> "liquidbounce+/error/error1.png"
                }
            )
            //Y-Axis Animation
            if (nowY != realY) {
                var pct = (nowTime - animeYTime) / animeTime.toDouble()
                if (pct > 1) {
                    nowY = realY
                    pct = 1.0
                } else {
                    pct = EaseUtils.easeOutExpo(pct)
                }
                GL11.glTranslated(0.0, (realY - nowY) * pct, 0.0)
            } else {
                animeYTime = nowTime
            }
            GL11.glTranslated(0.0, nowY.toDouble(), 0.0)

            //X-Axis Animation
            var pct = (nowTime - animeXTime) / animeTime.toDouble()
            when (fadeState) {
                FadeState.IN -> {
                    if (pct > 1) {
                        fadeState = FadeState.STAY
                        animeXTime = nowTime
                        pct = 1.0
                    }
                    pct = EaseUtils.easeOutExpo(pct)
                }

                FadeState.STAY -> {
                    pct = 1.0
                    if ((nowTime - animeXTime) > time) {
                        fadeState = FadeState.OUT
                        animeXTime = nowTime
                    }
                }

                FadeState.OUT -> {
                    if (pct > 1) {
                        fadeState = FadeState.END
                        animeXTime = nowTime
                        pct = 1.0
                    }
                    pct = 1 - EaseUtils.easeInExpo(pct)
                }

                FadeState.END -> {
                    return true
                }
            }
            GL11.glScaled(pct, pct, pct)
            GL11.glTranslatef(-width.toFloat(), 0F, 0F)

            var fontcolor = 0
            if (type.toString() == "SUCCESS") {
                fontcolor = Color(40, 250, 40, 75).rgb
            }
            if (type.toString() == "ERROR") {
                fontcolor = Color(250, 40, 40, 75).rgb
            }
            if (type.toString() == "WARNING") {
                fontcolor = Color(219, 167, 20, 75).rgb
            }
            if (type.toString() == "INFO") {
                fontcolor = Color(106, 106, 245, 75).rgb
            }
            RenderUtils.drawRect(-10F, 0F, width.toFloat(), height.toFloat(), Color(63, 63, 63, 210))
            RenderUtils.drawGradientSidewaysV(
                -10.0,
                height.toDouble(),
                width.toDouble(),
                height.toDouble() + 2,
                Color(1, 1, 1, 15).rgb,
                Color(1, 1, 1, 0).rgb
            )
            RenderUtils.drawGradientSidewaysV(
                -10.0,
                0.0,
                width.toDouble(),
                0.0 - 2,
                Color(1, 1, 1, 15).rgb,
                Color(1, 1, 1, 0).rgb
            )
            RenderUtils.drawRect(
                -10.0f,
                0F,
                width * ((nowTime - displayTime) / (animeTime * 2F + time)),
                height.toFloat(),
                fontcolor
            )
            Fonts.fontTahoma.drawString(title, 10f, 5.5f, Color.WHITE.rgb)
            Fonts.font35.drawString(content, 10f, 18f, Color.gray.rgb)
            RenderUtils.drawFilledCircle(width - 7, 6, 2F, Color(255, 255, 255, 220))
            RenderUtils.drawImage(pn, -8, 8, 17, 17)
            GlStateManager.resetColor()


            return false
        }
        if (style.equals("Novoline")) {
            val image = ResourceLocation("liquidbounce+/ui/" + type.name + ".png")
            val width = 100.coerceAtLeast((Fonts.font35.getStringWidth(this.content)) + 70)

            //Y-Axis Animation
            if (nowY != realY) {
                var pct = (nowTime - animeYTime) / animeTime.toDouble()
                if (pct > 1) {
                    nowY = realY
                    pct = 1.0
                } else {
                    pct = EaseUtils.easeOutBack(pct)
                }
                GL11.glTranslated(0.0, (realY - nowY) * pct, 0.0)
            } else {
                animeYTime = nowTime
            }
            GL11.glTranslated(0.0, nowY.toDouble(), 0.0)

            //X-Axis Animation
            var pct = (nowTime - animeXTime) / animeTime.toDouble()
            when (fadeState) {
                FadeState.IN -> {
                    if (pct > 1) {
                        fadeState = FadeState.STAY
                        animeXTime = nowTime
                        pct = 1.0
                    }
                    pct = EaseUtils.easeOutBack(pct)
                }

                FadeState.STAY -> {
                    pct = 1.0
                    if ((nowTime - animeXTime) > time) {
                        fadeState = FadeState.OUT
                        animeXTime = nowTime
                    }
                }

                FadeState.OUT -> {
                    if (pct > 1) {
                        fadeState = FadeState.END
                        animeXTime = nowTime
                        pct = 1.0
                    }
                    pct = 1 - EaseUtils.easeInBack(pct)
                }

                FadeState.END -> {
                    return true
                }
            }
            GL11.glScaled(pct, pct, pct)
            GL11.glTranslatef(-width.toFloat() / 2, -height.toFloat() / 2, 0F)
            RenderUtils.drawShadow(1F, 0F, width.toFloat() - 1, height.toFloat())
            RenderUtils.drawRect(1F, 0F, width.toFloat(), height.toFloat(), Color(0, 0, 0, 50))
            fun drawCircle(x: Float, y: Float, radius: Float, start: Int, end: Int) {
                GlStateManager.enableBlend()
                GlStateManager.disableTexture2D()
                GlStateManager.tryBlendFuncSeparate(
                    GL11.GL_SRC_ALPHA,
                    GL11.GL_ONE_MINUS_SRC_ALPHA,
                    GL11.GL_ONE,
                    GL11.GL_ZERO
                )
                GL11.glEnable(GL11.GL_LINE_SMOOTH)
                GL11.glLineWidth(2f)
                GL11.glBegin(GL11.GL_LINE_STRIP)
                var i = end.toFloat()
                while (i >= start) {
                    var c = RenderUtils.getGradientOffset(
                        Color(redValue.get(), greenValue.get(), blueValue.get()),
                        Color(red2Value.get(), green2Value.get(), blue2Value.get(), 1),
                        (Math.abs(System.currentTimeMillis() / 360.0 + (i * 34 / 360) * 56 / 100) / 10)
                    ).rgb
                    val f2 = (c shr 24 and 255).toFloat() / 255.0f
                    val f22 = (c shr 16 and 255).toFloat() / 255.0f
                    val f3 = (c shr 8 and 255).toFloat() / 255.0f
                    val f4 = (c and 255).toFloat() / 255.0f
                    GlStateManager.color(f22, f3, f4, f2)
                    GL11.glVertex2f(
                        (x + Math.cos(i * Math.PI / 180) * (radius * 1.001f)).toFloat(),
                        (y + Math.sin(i * Math.PI / 180) * (radius * 1.001f)).toFloat()
                    )
                    i -= 360f / 90.0f
                }
                GL11.glEnd()
                GL11.glDisable(GL11.GL_LINE_SMOOTH)
                GlStateManager.enableTexture2D()
                GlStateManager.disableBlend()
            }

            RenderUtils.drawFilledCircle(16f, 15f, 12.85f, Color(255, 255, 255, 255))
            RenderUtils.drawGradientSideways(
                1.0,
                height.toFloat() + 0.0,
                width * ((nowTime - displayTime) / (animeTime * 2F + time)) + 0.0,
                height.toFloat() + 2.0,
                Color(redValue.get(), greenValue.get(), blueValue.get()).rgb,
                Color(red2Value.get(), green2Value.get(), blue2Value.get()).rgb
            )
            drawCircle(16f, 15f, 13f, 0, 360)
            if (type == NotifyType.INFO) {
                Fonts.NOTIFICATIONS.drawString("B", 11F, 8F, 0)
            } else if (type == NotifyType.WARNING) {
                Fonts.NOTIFICATIONS.drawString("A", 14F, 8F, 0)
            } else if (type == NotifyType.ERROR) {
                Fonts.NOTIFICATIONS2.drawString("L", 9F, 10F, 0)
            } else {
                Fonts.NOTIFICATIONS2.drawString("M", 8F, 10F, 0)
            }
            Fonts.fontSFUI40.drawString(title, 34F, 4F, -1)
            Fonts.fontSFUI35.drawString(
                content + "  (" + BigDecimal(((time - time * ((nowTime - displayTime) / (animeTime * 2F + time))) / 1000).toDouble()).setScale(
                    1,
                    BigDecimal.ROUND_HALF_UP
                ).toString() + "s)", 34F, 17F, -1
            )


            GlStateManager.resetColor()
            return false
        }
        return false
    }
}


enum class NotifyType(var renderColor: Color) {
    SUCCESS(Color(0x60E066)),
    ERROR(Color(0xFF2F3A)),
    WARNING(Color(0xF5FD00)),
    INFO( Color(106, 106, 220));
}


enum class FadeState { IN, STAY, OUT, END }