/*
* this one is from evobounce, temp added until main dev is not lazy to fix slow health animation issue
*/

package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.ccbluex.liquidbounce.features.module.modules.render.ColorMixer
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.BlendUtils
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.ListValue
import net.ccbluex.liquidbounce.value.FontValue
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.GuiChat
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.ResourceLocation
import net.minecraft.util.MathHelper
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import kotlin.math.abs
import kotlin.math.pow

/**
 * A target hud
 */
@ElementInfo(name = "EvoTargets")
class EvoTargets : Element() {

    private val decimalFormat = DecimalFormat("##0.00", DecimalFormatSymbols(Locale.ENGLISH))
    private val decimalFormat2 = DecimalFormat("##0.0", DecimalFormatSymbols(Locale.ENGLISH))
    private val decimalFormat3 = DecimalFormat("0.#", DecimalFormatSymbols(Locale.ENGLISH))
    private val styleValue = ListValue("Style", arrayOf("LiquidBounce", "Flux", "Novoline", "NovolineOld", "AstolfoOld", "AstolfoNew", "RiseOld", "RiseNew", "Exhibition", "CatSense", "ChocoPie"), "LiquidBounce")
    private val autoResetWhenIdleValue = BoolValue("AutoResetWhenIdle", true)
    private val useCustomBackgroundColorValue = BoolValue("CatSense-Exhi-CustomBackground", false)
    private val exhiAnimValue = BoolValue("Exhi-ChocoPie-Animation", false)
    private val displayTargetNameValue = BoolValue("RiseNew-DisplayName", false)
    private val fontValue = FontValue("RiseNew-Font", Fonts.fontSFUI40)
    private val fadeSpeed = FloatValue("FadeSpeed", 2F, 1F, 9F)
    private val showUrselfWhenChatOpen = BoolValue("DisplayWhenChat", true)
    private val backgroundColorRedValue = IntegerValue("Background-Red", 0, 0, 255)
    private val backgroundColorGreenValue = IntegerValue("Background-Green", 0, 0, 255)
    private val backgroundColorBlueValue = IntegerValue("Background-Blue", 0, 0, 255)
    private val backgroundColorAlphaValue = IntegerValue("Background-Alpha", 160, 0, 255)
    private val borderColorRedValue = IntegerValue("Liquid-Border-Red", 0, 0, 255)
    private val borderColorGreenValue = IntegerValue("Liquid-Border-Green", 0, 0, 255)
    private val borderColorBlueValue = IntegerValue("Liquid-Border-Blue", 0, 0, 255)
    private val borderColorAlphaValue = IntegerValue("Liquid-Border-Alpha", 0, 0, 255)
    private val colorModeValue = ListValue("Color", arrayOf("Custom", "Sky", "LiquidSlowly", "Fade", "Mixer", "Health"), "Custom")
    private val redValue = IntegerValue("Red", 252, 0, 255)
    private val greenValue = IntegerValue("Green", 96, 0, 255)
    private val blueValue = IntegerValue("Blue", 66, 0, 255)
    private val saturationValue = FloatValue("Saturation", 1F, 0F, 1F)
    private val brightnessValue = FloatValue("Brightness", 1F, 0F, 1F)
    private val mixerSecondsValue = IntegerValue("Mixer-Seconds", 2, 1, 10)

    private var easingHealth: Float = 0F
    private var lastTarget: Entity? = null

    override fun drawElement(): Border {
        val target = if (mc.currentScreen is GuiChat && showUrselfWhenChatOpen.get()) mc.thePlayer!! else (LiquidBounce.moduleManager[KillAura::class.java] as KillAura).target
        val barColor = when (colorModeValue.get()) {
            "Custom" -> Color(redValue.get(), greenValue.get(), blueValue.get())
            "Sky" -> RenderUtils.skyRainbow(0, saturationValue.get(), brightnessValue.get())
            "Fade" -> ColorUtils.fade(Color(redValue.get(), greenValue.get(), blueValue.get()), 0, 100)
            "Health" -> if (target != null && target is EntityPlayer) BlendUtils.getHealthColor(target.health, target.maxHealth) else Color.green
            "Mixer" -> ColorMixer.getMixedColor(0, mixerSecondsValue.get())
            else -> ColorUtils.LiquidSlowly(System.nanoTime(), 0, saturationValue.get(), brightnessValue.get()) ?: return when (styleValue.get()) {
                "LiquidBounce" -> Border(0F, 0F, 120F, 36F)
                "Flux" -> Border(0F, 0F, 90F, 30F)
                "Novoline" -> Border(-1F, -1F, 120F, 37F)
                "NovolineOld" -> Border(0F, 0F, 80F, 40F)
                "AstolfoOld" -> Border(0F, 0F, 140F, 60F)
                "AstolfoNew", "CatSense", "Exhibition" -> Border(0F, 0F, 160F, 60F)
                "RiseOld" -> Border(0F, 0F, 154F, 60F)
                "RiseNew" -> Border(0F, 0F, 100F, 10F)
                else -> Border(0F, 0F, 124F, 39F)
            }
        }
        val bgColor = Color(backgroundColorRedValue.get(), backgroundColorGreenValue.get(), backgroundColorBlueValue.get(), backgroundColorAlphaValue.get())
        val borderColor = Color(borderColorRedValue.get(), borderColorGreenValue.get(), borderColorBlueValue.get(), borderColorAlphaValue.get())

        if (target is EntityPlayer) {
            when (styleValue.get()) {
                "LiquidBounce" -> {
                    if (target != lastTarget || easingHealth < 0 || easingHealth > target.maxHealth ||
                            abs(easingHealth - target.health) < 0.01) {
                        easingHealth = target.health
                    }

                    val width = (38 + Fonts.font40.getStringWidth(target.name))
                            .coerceAtLeast(118)
                            .toFloat()

                    // Draw rect box
                    RenderUtils.drawBorderedRect(0F, 0F, width, 36F, 3F, borderColor.rgb, bgColor.rgb)

                    // Damage animation
                    if (easingHealth > target.health)
                        RenderUtils.drawRect(0F, 34F, (easingHealth / target.maxHealth) * width,
                                36F, Color(252, 185, 65).rgb)

                    // Health bar
                    RenderUtils.drawRect(0F, 34F, (target.health / target.maxHealth) * width,
                            36F, barColor.rgb)

                    // Heal animation
                    if (easingHealth < target.health)
                        RenderUtils.drawRect((easingHealth / target.maxHealth) * width, 34F,
                                (target.health / target.maxHealth) * width, 36F, Color(44, 201, 144).rgb)

                    easingHealth += ((target.health - easingHealth) / 2.0F.pow(10.0F - fadeSpeed.get())) * RenderUtils.deltaTime

                    Fonts.font40.drawString(target.name, 36, 3, 0xffffff)
                    Fonts.font35.drawString("Distance: ${decimalFormat.format(mc.thePlayer.getDistanceToEntityBox(target))}", 36, 15, 0xffffff)

                    // Draw info
                    val playerInfo = mc.netHandler.getPlayerInfo(target.uniqueID)
                    if (playerInfo != null) {
                        Fonts.font35.drawString("Ping: ${playerInfo.responseTime.coerceAtLeast(0)}",
                                36, 24, 0xffffff)

                        // Draw head
                        val locationSkin = playerInfo.locationSkin
                        drawHead(locationSkin, 30, 30)
                    }
                }

                "Flux" -> {
                    val width = (20F + Fonts.font35.getStringWidth(target.name)).coerceAtLeast(20F + Fonts.font35.getStringWidth("Health: ${decimalFormat2.format(target.health)}")).toFloat()

                    GL11.glTranslatef(0F, -34F, 0F) //recalibrate position
                    RenderUtils.drawRect(-2F, 34F, 2F + width, if (target.getTotalArmorValue() != 0) 66F else 60F, Color(35, 35, 40, 230).rgb) // Draw background
                    Fonts.font35.drawString(target.name, 20, 36, 0xFFFFFF) // Draw target name
                    Fonts.font35.drawString("Health: ${decimalFormat2.format(target.health)}", 20, 46, 0xFFFFFF) // Draw target health
                    drawHead2(mc.netHandler.getPlayerInfo(target.uniqueID).locationSkin, 0, 36) // Draw target head

                    RenderUtils.drawRect(0F, 56F, width, 58F, Color(25, 25, 35, 255).rgb) // Draw health bar background

                    easingHealth += ((target.health - easingHealth) / Math.pow(2.0, 10.0 - 3.0)).toFloat() * RenderUtils.deltaTime.toFloat()

                    if (easingHealth < 0 || easingHealth > target.maxHealth) {
                        easingHealth = target.health.toFloat()
                    }
                    if (easingHealth > target.health) {
                        RenderUtils.drawRect(0F, 56F, (easingHealth / target.maxHealth) * width, 58F, Color(231, 182, 0, 255).rgb)
                    } // Damage animation
                    if (easingHealth < target.health) {
                        RenderUtils.drawRect((easingHealth / target.maxHealth) * width, 56F, (easingHealth / target.maxHealth) * width, 58F, Color(231, 182, 0, 255).rgb)
                    } // Heal animation

                    RenderUtils.drawRect(0F, 56F, (target.health / target.maxHealth) * width, 58F, Color(0, 224, 84, 255).rgb) // Draw health bar

                    if (target.getTotalArmorValue() != 0) {
                        RenderUtils.drawRect(0F, 62F, width, 64F, Color(25, 25, 35, 255).rgb) // Draw armor bar background
                        RenderUtils.drawRect(0F, 62F, (target.getTotalArmorValue() / 20F) * width, 64F, Color(77, 128, 255, 255).rgb) // Draw armor bar
                    }
                    GL11.glTranslatef(0F, 34F, 0F)
                }

                "Novoline" -> {
                    val font = Fonts.minecraftFont
                    val fontHeight = font.FONT_HEIGHT
                    val mainColor = barColor
                    val nameLength = (font.getStringWidth(target.name)).coerceAtLeast(font.getStringWidth("${decimalFormat.format(target.health)} ❤")).toFloat() + 30F
                    val barWidth = (target.health / target.maxHealth) * nameLength

                    RenderUtils.drawBorderedRect(-1F, -1F, 2F + nameLength + 36F, 1F + 36F, 2F, Color(24, 24, 24, 255).rgb, Color(31, 31, 31, 255).rgb)
                    drawHead3(mc.netHandler.getPlayerInfo(target.uniqueID).locationSkin, 0, 0, 36, 36)
                    font.drawStringWithShadow(target.name, 1F + 36F, 2F, -1)
                    font.drawStringWithShadow("${decimalFormat.format(target.health)}", 1F + 36F, 36F - fontHeight.toFloat(), -1)
                    font.drawStringWithShadow("❤", 1F + 36F + font.getStringWidth("${decimalFormat.format(target.health)} "), 36F - fontHeight, mainColor.rgb)
                    RenderUtils.drawRect(1F + 36F, 12F, 1F + 36F + nameLength, 24F, Color(24, 24, 24, 255).rgb)

                    easingHealth += ((target.health - easingHealth) / 2.0F.pow(10.0F - fadeSpeed.get())) * RenderUtils.deltaTime

                    val animateThingy = (easingHealth.coerceIn(target.health, target.maxHealth) / target.maxHealth) * nameLength //fixed the weird bar thingy

                    if (easingHealth > target.health)
                        RenderUtils.drawRect(1F + 36F, 12F, 1F + 36F + animateThingy, 24F, mainColor.darker().rgb)

                    RenderUtils.drawRect(1F + 36F, 12F, 1F + 36F + barWidth, 24F, mainColor.rgb)
                }

                "NovolineOld" -> {
                    easingHealth += ((target.health - easingHealth) / 2.0F.pow(10.0F - fadeSpeed.get())) * RenderUtils.deltaTime

                    val nameLength = (35F + Fonts.minecraftFont.getStringWidth(target.name).toFloat()).coerceAtLeast(35F + getArmorLength(target))
                    val barWidth = (easingHealth / target.maxHealth) * nameLength

                    RenderUtils.drawRect(0F, 0F, nameLength, 40F, bgColor.rgb)

                    GlStateManager.resetColor()
                    GL11.glColor4f(1F, 1F, 1F, 1F)
                    RenderUtils.drawEntityOnScreen(15, 35, 15, target)

                    Fonts.minecraftFont.drawStringWithShadow(target.name, 30F, 5F, -1);
                    drawArmor(30, 15, target)

                    RenderUtils.drawRect(0F, 39F, barWidth, 40F, barColor.rgb)
                }

                "AstolfoOld" -> {
                    val font = Fonts.minecraftFont
                    val color = barColor

                    easingHealth += ((target.health - easingHealth) / 2.0F.pow(10.0F - fadeSpeed.get())) * RenderUtils.deltaTime

                    val hpPct = easingHealth / target.maxHealth

                    RenderUtils.drawRect(0F, 0F, 140F, 60F, bgColor.rgb)

                    // health rect
                    RenderUtils.drawRect(3F, 55F, 137F, 58F, ColorUtils.reAlpha(color, 100).rgb)
                    RenderUtils.drawRect(3F, 55F, 3 + (hpPct * 134F), 58F, color.rgb)

                    GlStateManager.resetColor()
                    GL11.glColor4f(1F, 1F, 1F, 1F)
                    RenderUtils.drawEntityOnScreen(18, 46, 20, target)

                    font.drawStringWithShadow(target.name, 37F, 6F, -1)
                    GL11.glPushMatrix()
                    GL11.glScalef(2F,2F,2F)
                    font.drawString("${decimalFormat3.format(target.health)} ❤", 19, 9, color.rgb)
                    GL11.glPopMatrix()
                }

                "AstolfoNew" -> {
                    easingHealth += ((target.health - easingHealth) / 2.0F.pow(10.0F - fadeSpeed.get())) * RenderUtils.deltaTime

                    RenderUtils.drawRect(0F, 0F, 160F, 60F, bgColor.rgb)

                    GlStateManager.resetColor()
                    GL11.glColor4f(1F, 1F, 1F, 1F)
                    RenderUtils.drawEntityOnScreen(16.0, 55.0, 25F, target)

                    Fonts.minecraftFont.drawString(target.name, 32F, 5F, -1, true)
                    GL11.glPushMatrix()
                    GL11.glTranslatef(32F, 20F, 32F)
                    GL11.glScalef(2F, 2F, 2F)
                    Fonts.minecraftFont.drawString("${decimalFormat3.format(target.health)} ❤", 0, 0, barColor.rgb);
                    GL11.glPopMatrix()

                    RenderUtils.drawRect(32F, 48F, 32F + 122F, 55F, barColor.darker().rgb)
                    RenderUtils.drawRect(32F, 48F, 32F + (easingHealth / target.maxHealth).toFloat() * 122F, 55F, barColor.rgb)
                }

                "RiseOld" -> {
                    easingHealth += ((target.health - easingHealth) / 2.0F.pow(10.0F - fadeSpeed.get())) * RenderUtils.deltaTime

                    RenderUtils.drawRect(0F, 0F, 154F, 60F, bgColor.rgb)
                    Fonts.font40.drawString(target.name, 5F, 5F, barColor.rgb, false)

                    GlStateManager.resetColor()
                    RenderUtils.drawEntityOnScreen(14.0, 55.0, 20.3F, target)
                    RenderUtils.drawRect(33F, 50F, (easingHealth / target.maxHealth).toFloat() * 140F, 54F, barColor.rgb)
                    Fonts.font40.drawString("${decimalFormat2.format(mc.thePlayer.getDistanceToEntityBox(target))}  |  ${if (target.onGround) "OnGround" else "OffGround"}  |  Hurt ${target.hurtTime}", 33F, 18F, -1, false)

                    val playerInfo = mc.netHandler.getPlayerInfo(target.uniqueID)
                    Fonts.font40.drawString("Ping ${if (playerInfo != null) playerInfo.responseTime.coerceAtLeast(0) else 0} ms", 33F, 30F, -1, false)
                }

                "RiseNew" -> {
                    easingHealth += ((target.health - easingHealth) / 2.0F.pow(10.0F - fadeSpeed.get())) * RenderUtils.deltaTime
                    val font = fontValue.get()

                    RenderUtils.drawRect(0F, 0F, (easingHealth / target.maxHealth).toFloat() * 100F, 10F, barColor.rgb)
                    if (displayTargetNameValue.get()) font.drawString(target.name, -2F - font.getStringWidth(target.name).toFloat(), 1F, -1, true)
                    font.drawString("${decimalFormat2.format(easingHealth)}", 2F + (easingHealth / target.maxHealth).toFloat() * 100F, 1F, -1, true)
                }

                "CatSense" -> {
                    easingHealth += ((target.health - easingHealth) / 2.0F.pow(10.0F - fadeSpeed.get())) * RenderUtils.deltaTime

                    if (useCustomBackgroundColorValue.get())
                        RenderUtils.drawRect(0F, 0F, 160F, 60F, bgColor.rgb)
                    else
                        drawExhiRect(0F, 0F, 160F, 60F)

                    GlStateManager.resetColor()
                    GL11.glColor4f(1F, 1F, 1F, 1F)

                    for (i in 0..4) {
                        val percent = easingHealth / target.maxHealth
                        val huh = (MathHelper.clamp_float(percent, i.toFloat() / 5F, (i.toFloat() + 1F) / 5F) - (i.toFloat() / 5F)) / 0.2F
                        val w = huh * 29F

                        RenderUtils.drawRect(5F + (i.toFloat() * 30F), 54F, 5F + (i.toFloat() * 30F) + w, 55F, barColor.rgb)
                    }

                    GlStateManager.resetColor()
                    GL11.glColor4f(1F, 1F, 1F, 1F)

                    Fonts.minecraftFont.drawStringWithShadow("${decimalFormat2.format(mc.thePlayer.getDistanceToEntityBox(target))} m", 6F, 30F, -1)
                    Fonts.minecraftFont.drawStringWithShadow(target.name, 6F, 40F, -1)

                    GL11.glPushMatrix()
                    GL11.glTranslatef(155F, 20F, 0F)
                    GL11.glScalef(2F, 2F, 0F)
                    Fonts.minecraftFont.drawStringWithShadow("${decimalFormat3.format(target.health)} ❤", -Fonts.minecraftFont.getStringWidth("${decimalFormat3.format(target.health)} ❤").toFloat(), 0F, -1)
                    GL11.glPopMatrix()
                }

                "Exhibition" -> {
                    easingHealth += ((target.health - easingHealth) / 2.0F.pow(10.0F - fadeSpeed.get())) * RenderUtils.deltaTime

                    if (useCustomBackgroundColorValue.get())
                        RenderUtils.drawRect(0F, 0F, 160F, 60F, bgColor.rgb)
                    else
                        drawExhiRect(0F, 0F, 160F, 60F)

                    GlStateManager.resetColor()
                    GL11.glColor4f(1F, 1F, 1F, 1F)
                    RenderUtils.drawEntityOnScreen(20, 50, 20, target)

                    Fonts.font40.drawStringWithShadow(target.name, 40F, 10F, -1)
                    Fonts.fontSmall.drawStringWithShadow("HP: ${target.health.toInt()} Dist: ${mc.thePlayer.getDistanceToEntityBox(target).toInt()}", 40F, 30F, -1)

                    GlStateManager.resetColor()
                    GL11.glColor4f(1F, 1F, 1F, 1F)
                    drawArmor(40, 35, target)

                    for (i in 0..9) {
                        val percent = (if (exhiAnimValue.get()) easingHealth else target.health) / target.maxHealth
                        val huh = (MathHelper.clamp_float(percent.toFloat(), i.toFloat() / 10F, (i.toFloat() + 1F) / 10F) - (i.toFloat() / 10F)) / 0.1F
                        val w = huh * 6F

                        RenderUtils.drawRect(40F + (i.toFloat() * 7F), 22F, 40F + (i.toFloat() * 7F) + 6F, 25F, barColor.darker().darker().rgb)
                        RenderUtils.drawRect(40F + (i.toFloat() * 7F), 22F, 40F + (i.toFloat() * 7F) + w, 25F, barColor.rgb)
                    }
                }

                else -> { //ChocoPie
                    easingHealth += ((target.health - easingHealth) / 2.0F.pow(10.0F - fadeSpeed.get())) * RenderUtils.deltaTime

                    GL11.glTranslatef(-13F, -42F, 0F)

                    RenderUtils.drawRect(15F, 44F, 139F, 83F, Color(23,23,25,233).rgb)
                    RenderUtils.drawRect(28F, 76.8F, 105.2F + 28F, 78.9F, Color(12,12,12,225).rgb)
                    RenderUtils.drawRect(28F, 76.8f, ((if (exhiAnimValue.get()) easingHealth else target.health) / target.maxHealth) * 105.2F + 28F, 78.9F, barColor.rgb)
                    Fonts.fontSFUI40.drawStringWithShadow(target.name, 45F, 49F, -1)
                    Fonts.minecraftFont.drawStringWithShadow("§c❤", 18F, 73F, -1)

                    val healthDist = mc.thePlayer.health - target.health
                    if (healthDist > 0F)
                        Fonts.fontSFUI40.drawStringWithShadow("Winning", 45F, 60F, 0x6CC312)
                    if (healthDist < 0F)
                        Fonts.fontSFUI40.drawStringWithShadow("Losing", 45F, 60F, 0xFF3300)
                    if (healthDist == 0F)
                        Fonts.fontSFUI40.drawStringWithShadow("Draw", 45F, 60F, 0xFFFF00)

                    GL11.glTranslatef(13F, 42F, 0F)
                    drawHead3(mc.netHandler.getPlayerInfo(target.uniqueID).locationSkin, 3, 3, 27, 27)
                }
            }
        } else if (target == null && autoResetWhenIdleValue.get()) {
            easingHealth = 0F
        }

        lastTarget = target
        return when (styleValue.get()) {
            "LiquidBounce" -> Border(0F, 0F, 120F, 36F)
            "Flux" -> Border(0F, 0F, 90F, 30F)
            "Novoline" -> Border(-1F, -1F, 120F, 37F)
            "NovolineOld" -> Border(0F, 0F, 80F, 40F)
            "AstolfoOld" -> Border(0F, 0F, 140F, 60F)
            "AstolfoNew", "CatSense", "Exhibition" -> Border(0F, 0F, 160F, 60F)
            "RiseOld" -> Border(0F, 0F, 154F, 60F)
            "RiseNew" -> Border(0F, 0F, 100F, 10F)
            else -> Border(0F, 0F, 126F, 39F)
        }
    }

    private fun drawExhiRect(x: Float, y: Float, x2: Float, y2: Float) {
        RenderUtils.drawRect(x - 1, y - 1, x2 + 1, y2 + 1, Color(59, 59, 59).rgb)
        RenderUtils.drawBorderedRect(x + 2F, y + 2F, x2 - 2F, y2 - 2F, 0.5F, Color(18, 18, 18).rgb, Color(28, 28, 28).rgb)
    }

    private fun drawHead(skin: ResourceLocation, width: Int, height: Int) {
        GL11.glColor4f(1F, 1F, 1F, 1F)
        mc.textureManager.bindTexture(skin)
        Gui.drawScaledCustomSizeModalRect(2, 2, 8F, 8F, 8, 8, width, height,
                64F, 64F)
    }

    private fun drawHead2(skin: ResourceLocation, x: Int, y: Int) {
        GL11.glColor4f(1F, 1F, 1F, 1F)
        mc.textureManager.bindTexture(skin)
        Gui.drawScaledCustomSizeModalRect(x, y, 8F, 8F, 8, 8, 16, 16,
                64F, 64F)
    }

    private fun drawHead3(skin: ResourceLocation, x: Int, y: Int, width: Int, height: Int) {
        GL11.glColor4f(1F, 1F, 1F, 1F)
        mc.textureManager.bindTexture(skin)
        Gui.drawScaledCustomSizeModalRect(x, y, 8F, 8F, 8, 8, width, height,
                64F, 64F)
    }

    private fun getArmorLength(ent: EntityPlayer): Float {
        var x : Float = 0F
        for (i in 3 downTo 0) {
            val stack = ent.inventory.armorInventory[i] ?: continue
            x += 18F
        }
        if (ent.getHeldItem() != null && ent.getHeldItem().getItem() != null)
            x += 18F

        return x
    }

    private fun drawArmor(x: Int, y: Int, ent: EntityPlayer) {
        GL11.glPushMatrix()
        RenderHelper.enableGUIStandardItemLighting()

        val renderItem = mc.renderItem

        var drawX : Int = x
        var drawY : Int = y

        for (index in 3 downTo 0) {
            val stack = ent.inventory.armorInventory[index] ?: continue

            renderItem.renderItemIntoGUI(stack, drawX, drawY)
            renderItem.renderItemOverlays(mc.fontRendererObj, stack, drawX, drawY)

            drawX += 18
        }

        if (ent.getHeldItem() != null && ent.getHeldItem().getItem() != null) {
            renderItem.renderItemIntoGUI(ent.getHeldItem(), drawX, drawY)
            renderItem.renderItemOverlays(mc.fontRendererObj, ent.getHeldItem(), drawX, drawY)
        }

        RenderHelper.disableStandardItemLighting()
        GlStateManager.enableAlpha()
        GlStateManager.disableBlend()
        GlStateManager.disableLighting()
        GlStateManager.disableCull()
        GL11.glPopMatrix()
    }

}
