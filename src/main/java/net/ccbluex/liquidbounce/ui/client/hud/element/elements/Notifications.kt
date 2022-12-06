
package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.ui.client.hud.designer.GuiHudDesigner
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.Side
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.render.*
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
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
        val styleValue = ListValue("Mode", arrayOf("Classic", "IntelliJIDEA","TenacityOld","New"), "Classic")
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

            return if (styleValue.get().equals("IntelliJIDEA", true)) Border(160F, -59F, -22F, -29F) else if (styleValue.get().equals("New", true)) Border(-exampleNotification.width.toFloat(), -exampleNotification.height.toFloat(), 0F, 0F) else if (styleValue.get().equals("TenacityOld", true)) Border (-exampleNotification.width.toFloat(), -exampleNotification.height.toFloat(), 0F, 0F) else
                Border(-exampleNotification.width.toFloat() - 22, -exampleNotification.height.toFloat(), 0F, 0F)

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
    private val notifyDir = "liquidbounce+/notif/"
    private val imgSuccess = ResourceLocation("${notifyDir}checkmark.png")
    private val imgError = ResourceLocation("${notifyDir}error.png")
    private val imgWarning = ResourceLocation("${notifyDir}warning.png")
    private val imgInfo = ResourceLocation("${notifyDir}info.png")
    val height = 30
    private var firstY = 0f
    var x = 0f
    var textLength = Fonts.minecraftFont.getStringWidth(content)

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
        RenderUtils.drawImage(image, -19,  3, 22, 22)
        GlStateManager.resetColor()
        }
        if(style.equals("IntelliJIDEA")) {

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

            var y = firstY
            val kek = -x - 1 - 20F

            GlStateManager.resetColor()
            Stencil.write(true)

            if (type == NotifyType.ERROR) {
                RenderUtils.drawRoundedRect(-x + 9 + textLength, -y + 1, kek - 1, -28F - y - 1, 0F, Color(115,69,75).rgb)
                RenderUtils.drawRoundedRect(-x + 8 + textLength, -y, kek, -28F - y, 0F, Color(89,61,65).rgb)
                Fonts.minecraftFont.drawStringWithShadow("IDE Error:", -x - 4, -25F - y, Color(249,130,108).rgb)
            }
            if (type == NotifyType.INFO) {
                RenderUtils.drawRoundedRect(-x + 9 + textLength, -y + 1, kek - 1, -28F - y - 1, 0F, Color(70,94,115).rgb)
                RenderUtils.drawRoundedRect(-x + 8 + textLength, -y, kek, -28F - y, 0F, Color(61,72,87).rgb)
                Fonts.minecraftFont.drawStringWithShadow("IDE Information:", -x - 4, -25F - y, Color(119,145,147).rgb)
            }
            if (type == NotifyType.SUCCESS) {
                RenderUtils.drawRoundedRect(-x + 9 + textLength, -y + 1, kek - 1, -28F - y - 1, 0F, Color(67,104,67).rgb)
                RenderUtils.drawRoundedRect(-x + 8 + textLength, -y, kek, -28F - y, 0F, Color(55,78,55).rgb)
                Fonts.minecraftFont.drawStringWithShadow("IDE Success:", -x - 4, -25F - y, Color(10,142,2).rgb)
            }
            if (type == NotifyType.WARNING) {
                RenderUtils.drawRoundedRect(-x + 9 + textLength, -y + 1, kek - 1, -28F - y - 1, 0F, Color(103,103,63).rgb)
                RenderUtils.drawRoundedRect(-x + 8 + textLength, -y, kek, -28F - y, 0F, Color(80,80,57).rgb)
                Fonts.minecraftFont.drawStringWithShadow("IDE Warning:", -x - 4, -25F - y, Color(175,163,0).rgb)
            }
            Stencil.erase(true)

            GlStateManager.resetColor()

            Stencil.dispose()

            GL11.glPushMatrix()
            GlStateManager.disableAlpha()
            GlStateManager.resetColor()
            GL11.glColor4f(1F, 1F, 1F, 1F)
            RenderUtils.drawImage2(when (type) {
                NotifyType.SUCCESS -> imgSuccess
                NotifyType.ERROR -> imgError
                NotifyType.WARNING -> imgWarning
                NotifyType.INFO -> imgInfo
            }, kek + 5, -25F - y, 7, 7)
            GlStateManager.enableAlpha()
            GL11.glPopMatrix()

            Fonts.minecraftFont.drawStringWithShadow(content, -x - 4, -13F - y, -1)
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
            RenderUtils.drawRect(0F, 0F, width.toFloat(), height.toFloat() + 20, Color(63, 63, 63, 210))
            RenderUtils.drawGradientSidewaysV(
                0.0,
                height.toDouble(),
                width.toDouble(),
                height.toDouble() + 2,
                Color(1, 1, 1, 15).rgb,
                Color(1, 1, 1, 0).rgb
            )
            RenderUtils.drawGradientSidewaysV(
                0.0,
                0.0,
                width.toDouble(),
                0.0 - 2,
                Color(1, 1, 1, 15).rgb,
                Color(1, 1, 1, 0).rgb
            )
            RenderUtils.drawRect(
                0.0f,
                0F,
                width * ((nowTime - displayTime) / (animeTime * 2F + time)),
                height.toFloat(),
                fontcolor
            )
            Fonts.fontTahoma.drawString(title, 35f, 15.5f, Color.WHITE.rgb)
            Fonts.font35.drawString(content, 35f, 28f, Color.gray.rgb)
            RenderUtils.drawFilledCircle(width - 7, 6, 2F, Color(255, 255, 255, 220))
            RenderUtils.drawImage(pn, 8, 18, 17, 17)
            GlStateManager.resetColor()


            return false
        }
        if (style.equals("New")) {
            val pn = ResourceLocation(
                when (type.name) {
                    "SUCCESS" -> "liquidbounce+/noti/SUCCESS.png"
                    "ERROR" -> "liquidbounce+/noti/ERROR.png"
                    "WARNING" -> "liquidbounce+/noti/WARNING.png"
                    "INFO" -> "liquidbounce+/noti/INFO.png"
                    else -> "liquidbounce+/error/error1.png"
                }
            )
            var width = 100.coerceAtLeast((Fonts.fontSFUI35.getStringWidth(this.content))+22)
            val realY = -(index+1) * height
            val nowTime = System.currentTimeMillis()
            var transY = nowY.toDouble()

            // Y-Axis Animation
            if (nowY != realY) {
                var pct = (nowTime - animeYTime) / animeTime.toDouble()
                if (pct> 1) {
                    nowY = realY
                    pct = 1.0
                } else {
                    pct = EaseUtils.easeOutExpo(pct)
                }
                transY += (realY - nowY) * pct
            } else {
                animeYTime = nowTime
            }

            // X-Axis Animation
            var pct = (nowTime - animeXTime) / animeTime.toDouble()
            when (fadeState) {
                FadeState.IN -> {
                    if (pct> 1) {
                        fadeState = FadeState.STAY
                        animeXTime = nowTime
                        pct = 1.0
                    }
                    pct = EaseUtils.easeOutExpo(pct)
                }

                FadeState.STAY -> {
                    pct = 1.0
                    if ((nowTime - animeXTime)> time) {
                        fadeState = FadeState.OUT
                        animeXTime = nowTime
                    }
                }

                FadeState.OUT -> {
                    if (pct> 1) {
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
            val transX = width - (width * pct) - width
            GL11.glTranslated(transX, transY, 0.0)

            RenderUtils.drawRect(0f,-1f,width.toFloat(),height.toFloat()-10f,Color(0,0,0,120).rgb)
            RenderUtils.drawShadow(0f,-1f,width.toFloat(),height.toFloat() - 9f)
            RenderUtils.drawImage(pn, 2, 1, 16, 16)
            Fonts.fontSFUI35.drawString(content, 20.0f, Fonts.fontSFUI35.FONT_HEIGHT/2f, Color.WHITE.rgb, false)
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
