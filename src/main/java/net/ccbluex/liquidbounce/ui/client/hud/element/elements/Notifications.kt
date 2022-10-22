/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 * 
 * This code belongs to WYSI-Foundation. Please give credits when using this in your repository.
 */
package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.ui.client.hud.designer.GuiHudDesigner
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.Side
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.render.*
import net.ccbluex.liquidbounce.value.*
import net.minecraft.client.gui.FontRenderer
import net.minecraft.util.ResourceLocation
import java.awt.Color

import org.lwjgl.opengl.GL11
import kotlin.math.max

@ElementInfo(name = "Notifications")
class Notifications(
    x: Double = 0.0,
    y: Double = 0.0,
    scale: Float = 1F,
    side: Side = Side(Side.Horizontal.RIGHT, Side.Vertical.DOWN)
) : Element(x, y, scale, side) {

    private val backGroundAlphaValue = IntegerValue("BackGroundAlpha", 170, 0, 255)
    private val fontValue = FontValue("Font", Fonts.font35)

    /**
     * Example notification for CustomHUD designer
     */
    private val exampleNotification = Notification("This is an example notification.", NotifyType.INFO)

    /**
     * Draw element
     */
    override fun drawElement(): Border? {
        // bypass java.util.ConcurrentModificationException
        LiquidBounce.hud.notifications.map { it }.forEachIndexed { index, notify ->
            GL11.glPushMatrix()

            if (notify.drawNotification(index, fontValue.get(), backGroundAlphaValue.get(), 0f, this.renderX.toFloat(), this.renderY.toFloat())) {
                LiquidBounce.hud.notifications.remove(notify)
            }

            GL11.glPopMatrix()
        }

        if (mc.currentScreen is GuiHudDesigner) {
            if (!LiquidBounce.hud.notifications.contains(exampleNotification)) {
                LiquidBounce.hud.addNotification(exampleNotification)
            }

            exampleNotification.fadeState = FadeState.STAY
            exampleNotification.displayTime = System.currentTimeMillis()
//            exampleNotification.x = exampleNotification.textLength + 8F

            return Border(-exampleNotification.width.toFloat(), -exampleNotification.height.toFloat(), 0F, 0F)
        }

        return null
    }
}

class Notification(
    val message: String,
    val type: NotifyType,
    val time: Int = 1500,
    val animeTime: Int = 500
) {
    var width = 100
    val height = 15

    var fadeState = FadeState.IN
    var nowY = -height
    var displayTime = System.currentTimeMillis()
    var animeXTime = System.currentTimeMillis()
    var animeYTime = System.currentTimeMillis()

    /**
     * Draw notification
     */
    fun drawNotification(index: Int, font: FontRenderer, alpha: Int, x: Float, y: Float, scale: Float): Boolean {
        this.width = 100.coerceAtLeast((font.getStringWidth(this.message))+10 + 15)
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

        // draw notify
        RenderUtils.drawRect(0F, -1F, width.toFloat() + 12, height.toFloat(), Color(35, 35, 40, 250))
        GL11.glEnable(0x809D)
        RenderUtils.drawImage(type.resourcepack,2,1,12,12)
        GL11.glDisable(0x809D);
        RenderUtils.drawRect(0F, height.toFloat(), max(width - width * ((nowTime - displayTime) / (animeTime * 2F + time)), 0F), height.toFloat() + 1f, Color(255,255,255))
        font.drawString(message, 16F, 4F, Color.WHITE.rgb, false)
        return false
    }
}

enum class NotifyType(var resourcepack: ResourceLocation) {
    SUCCESS(ResourceLocation("liquidbounce+/notification/checkmark.png")),
    ERROR(ResourceLocation("liquidbounce+/notification/error.png")),
    WARNING(ResourceLocation("liquidbounce+/notification/warning.png")),
    INFO(ResourceLocation("liquidbounce+/notification/info.png"));
}

enum class FadeState { IN, STAY, OUT, END }