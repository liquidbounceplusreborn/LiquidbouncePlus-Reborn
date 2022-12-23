/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.impl

import net.ccbluex.liquidbounce.features.module.modules.render.ColorMixer
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Target
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.TargetStyle
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.ListValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.entity.player.EntityPlayer
import org.lwjgl.opengl.GL11
import java.awt.Color

import kotlin.math.abs
import kotlin.math.pow

class NovolineFour(inst: Target): TargetStyle("NovolineFour", inst, true) {
    val gradientDistanceValue = IntegerValue("GradientDistance", 50, 1, 200, { targetInstance.styleValue.get().equals("liquidbounce", true) })
    val gradientRoundedBarValue = BoolValue("GradientRoundedBar", true, { targetInstance.styleValue.get().equals("liquidbounce", true) })
    val hurtTimeAnim = BoolValue("HurtTimeAnim", true, { targetInstance.styleValue.get().equals("liquidbounce", true) })
    val borderColorMode = ListValue("Border-Color", arrayOf("Custom", "MatchBar", "None"), "None", { targetInstance.styleValue.get().equals("liquidbounce", true) })
    val borderWidthValue = FloatValue("Border-Width", 3F, 0.5F, 5F, { targetInstance.styleValue.get().equals("liquidbounce", true) })
    val borderRedValue = IntegerValue("Border-Red", 0, 0, 255, { targetInstance.styleValue.get().equals("liquidbounce", true) && borderColorMode.get().equals("custom", true) })
    val borderGreenValue = IntegerValue("Border-Green", 0, 0, 255, { targetInstance.styleValue.get().equals("liquidbounce", true) && borderColorMode.get().equals("custom", true) })
    val borderBlueValue = IntegerValue("Border-Blue", 0, 0, 255, { targetInstance.styleValue.get().equals("liquidbounce", true) && borderColorMode.get().equals("custom", true) })
    val borderAlphaValue = IntegerValue("Border-Alpha", 0, 0, 255, { targetInstance.styleValue.get().equals("liquidbounce", true) && borderColorMode.get().equals("custom", true) })
    private fun getColorAtIndex(i: Int): Int {
        return getColor(when (targetInstance.colorModeValue.get()) {
            "Rainbow" -> RenderUtils.getRainbowOpaque(targetInstance.waveSecondValue.get(), targetInstance.saturationValue.get(), targetInstance.brightnessValue.get(), i * gradientDistanceValue.get())
            "Sky" -> RenderUtils.SkyRainbow(i * gradientDistanceValue.get(), targetInstance.saturationValue.get(), targetInstance.brightnessValue.get())
            "Slowly" -> ColorUtils.LiquidSlowly(System.nanoTime(), i * gradientDistanceValue.get(), targetInstance.saturationValue.get(), targetInstance.brightnessValue.get())!!.rgb
            "Mixer" -> ColorMixer.getMixedColor(i * gradientDistanceValue.get(), targetInstance.waveSecondValue.get()).rgb
            "Fade" -> ColorUtils.fade(Color(targetInstance.redValue.get(), targetInstance.greenValue.get(), targetInstance.blueValue.get()), i * gradientDistanceValue.get(), 100).rgb
            else -> -1
        }).rgb
    }
    private var lastTarget: EntityPlayer? = null

    override fun drawTarget(entity: EntityPlayer) {
        if (entity != lastTarget || easingHealth < 0 || easingHealth > entity.maxHealth ||
            abs(easingHealth - entity.health) < 0.01) {
            easingHealth = entity.health

        }

        val width = (38 + Fonts.fontSFUI40.getStringWidth(entity.name))
                .coerceAtLeast(118)
                .toFloat()

        val borderColor = getColor(Color(borderRedValue.get(), borderGreenValue.get(), borderBlueValue.get(), borderAlphaValue.get()))

        // Draw rect box
        if (borderColorMode.get().equals("none", true))
            RenderUtils.drawRect(-2F, -2F, width +3, 44F, targetInstance.bgColor.rgb)
        else
            RenderUtils.drawBorderedRect(-2F, -2F, width +3, 40F, borderWidthValue.get(), if (borderColorMode.get().equals("matchbar", true)) targetInstance.barColor.rgb else borderColor.rgb, targetInstance.bgColor.rgb)


        // armor items
        GL11.glPushMatrix()
        GL11.glColor4f(1f, 1f, 1f, 1f - targetInstance.getFadeProgress())
        RenderHelper.enableGUIStandardItemLighting()

        val renderItem = mc.renderItem

        var x = 52
        var y = 14

        for (index in 3 downTo 0) {
            val stack = entity.inventory.armorInventory[index] ?: continue

            if (stack.getItem() == null)
                continue

            renderItem.renderItemAndEffectIntoGUI(stack, x, y)
            x += 17


            var x2 = 36
            var y2 = 14
            val mainStack = entity.heldItem
            if (mainStack != null && mainStack.item != null) {
                renderItem.renderItemIntoGUI(mainStack, x2, y2)
                renderItem.renderItemOverlays(mc.fontRendererObj, mainStack, x2, y2)
                RenderUtils.drawExhiEnchants(mainStack, x2.toFloat(), y2.toFloat())
            }
        }

        RenderHelper.disableStandardItemLighting()
        GlStateManager.enableAlpha()
        GlStateManager.disableBlend()
        GlStateManager.disableLighting()
        GlStateManager.disableCull()
        GL11.glPopMatrix()

        // Damage animation
        if (easingHealth > entity.health)
            RenderUtils.drawRect(2F, 35F, (easingHealth / entity.maxHealth) * width,
                    39F, targetInstance.barColor.rgb)

        // Health bar
        val barWidth = (0F - 5F - 32F) * (easingHealth / entity.maxHealth.toFloat()).coerceIn(0F, 1F)
        RenderUtils.drawRect(2F, 35F, (entity.health / entity.maxHealth) * width, 39F, targetInstance.barColor.rgb)
        if (gradientRoundedBarValue.get()) {
            if (barWidth > 0F)
                RenderUtils.fastRoundedRect(5F, 36F, 5F + barWidth, 48F, 3F)
        } else
            RenderUtils.quickDrawRect(5F, 42F, 5F + barWidth, 48F)

        GL11.glDisable(GL11.GL_BLEND)

        // Heal animation
        if (easingHealth < entity.health)
            RenderUtils.drawRect((easingHealth / entity.maxHealth) * width -2, 36F,
                    (entity.health / entity.maxHealth) * width, 39F, targetInstance.barColor.rgb)

        updateAnim(entity.health)


        Fonts.fontSFUI40.drawString(entity.name, 36, 3, getColor(-1).rgb)


        // Draw info
        val playerInfo = mc.netHandler.getPlayerInfo(entity.uniqueID)
        if (playerInfo != null) {


            // Draw head
            val locationSkin = playerInfo.locationSkin
            if (hurtTimeAnim.get()) {
                val scaleHT = (entity.hurtTime.toFloat() / entity.maxHurtTime.coerceAtLeast(1).toFloat()).coerceIn(0F, 1F)
                drawHead(locationSkin, 
                    2F + 15F * (scaleHT * 0.2F), 
                    2F + 15F * (scaleHT * 0.2F), 
                    1F - scaleHT * 0.2F, 
                    30, 30, 
                    1F, 0.4F + (1F - scaleHT) * 0.6F, 0.4F + (1F - scaleHT) * 0.6F)
            } else
                drawHead(skin = locationSkin, width = 30, height = 30, alpha = 1F - targetInstance.getFadeProgress())

        }

        lastTarget = entity

    }

    override fun handleBlur(entity: EntityPlayer) {
        val width = (38 + Fonts.fontSFUI40.getStringWidth(entity.name))
                        .coerceAtLeast(118)
                        .toFloat()

        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        RenderUtils.quickDrawRect(-2F, -2F, width+3, 43F)
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }

    override fun handleShadowCut(entity: EntityPlayer) = handleBlur(entity)
    
    override fun handleShadow(entity: EntityPlayer) {
        val width = (38 + Fonts.fontSFUI40.getStringWidth(entity.name))
                        .coerceAtLeast(118)
                        .toFloat()

        RenderUtils.newDrawRect(-2F, -2F, width+3, 43F, shadowOpaque.rgb)
    }

    override fun getBorder(entity: EntityPlayer?): Border? {
        entity ?: return Border(0F, 0F, 118F, 43F)
        val width = (38 + Fonts.fontSFUI40.getStringWidth(entity.name))
                        .coerceAtLeast(118)
                        .toFloat()
        return Border(-2F, -2F, width+3, 43F)
    }

}