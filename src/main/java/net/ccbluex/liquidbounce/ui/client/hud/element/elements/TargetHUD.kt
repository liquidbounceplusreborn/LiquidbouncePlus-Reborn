package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.ccbluex.liquidbounce.features.module.modules.render.HUD
import net.ccbluex.liquidbounce.ui.client.hud.designer.GuiHudDesigner
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.Colors
import net.ccbluex.liquidbounce.utils.Palette
import net.ccbluex.liquidbounce.utils.PlayerUtils
import net.ccbluex.liquidbounce.utils.TargetHudParticles
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.ccbluex.liquidbounce.utils.extensions.skin
import net.ccbluex.liquidbounce.utils.misc.RandomUtils
import net.ccbluex.liquidbounce.utils.render.*
import net.ccbluex.liquidbounce.utils.render.ColorUtils.rainbow
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.AbstractClientPlayer
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.GuiChat
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.MathHelper
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt

@ElementInfo(name = "TargetHUD")
class TargetHUD : Element() {
    private val decimalFormat = DecimalFormat("##0.0", DecimalFormatSymbols(Locale.ENGLISH))
    private val decimalFormat2 = DecimalFormat("##0", DecimalFormatSymbols(Locale.ENGLISH))
    private val styleValue = ListValue("Style", arrayOf( "Novoline","Novoline2","Exhibition","Novoline3","LiquidBounce","Flux","Lnk","Hanabi","Astolfo","Simplicity","AsuidBounce","Style"), "Novoline3")
    var colorModeValue = ListValue("Mode", arrayOf("Custom", "Health"), "Custom")
    var healthbar = ListValue("Healthbar", arrayOf("easing", "animation"), "animation")
    private val redValue = IntegerValue("Red", 255, 0, 255)
    private val greenValue = IntegerValue("Green", 255, 0, 255)
    private val blueValue = IntegerValue("Blue", 255, 0, 255)
    private val gredValue = IntegerValue("GradientRed", 255, 0, 255)
    private val ggreenValue = IntegerValue("GradientGreen", 255, 0, 255)
    private val gblueValue = IntegerValue("GradientBlue", 255, 0, 255)
    val backgroundalpha = IntegerValue("Alpha", 120, 0, 255)
    val fadeSpeed = FloatValue("FadeSpeed", 2f, 1f, 9f)
    private val par = BoolValue("Particles", true)
    val rainbow = BoolValue("Rainbow", false)
    private val showUrselfWhenChatOpen = BoolValue("DisplayWhenChat", true)
    private var easingHealth: Float = 0F
    private var lastTarget: Entity? = null
    private var fontrender = Fonts.minecraftFont
    val Stringparticles = mutableListOf<TargetHudParticles>()
    val particles = mutableListOf<TargetHudParticles>()
    private val addTimer = MSTimer()
    private var healthBarWidth = 0.0
    private var healthBarWidth2 = 0.0
    private var hudHeight = 0.0
    val counter1 = intArrayOf(50)
    val counter2 = intArrayOf(80)


    /**
     * @author ArrisDream
     */
    override fun drawElement(): Border? {
        var target = (LiquidBounce.moduleManager[KillAura::class.java] as KillAura).target
        if (mc.currentScreen is GuiHudDesigner || mc.currentScreen is GuiChat && showUrselfWhenChatOpen.get()) {
            target = mc.thePlayer
        }
        if (target is EntityPlayer) {
            if (target != lastTarget || easingHealth < 0 || easingHealth > target.maxHealth ||
                abs(easingHealth - target.health) < 0.01
            ) {
                easingHealth = target.health
            }
            val barColor = when (colorModeValue.get()) {
                "Custom" -> Color(redValue.get(), greenValue.get(), blueValue.get())
                "Health" -> if (target != null) BlendUtils.getHealthColor(
                    target.health,
                    target.maxHealth
                ) else Color.green
                else -> Color(redValue.get(), greenValue.get(), blueValue.get(), backgroundalpha.get())
            }
            val bordercolor = Color(redValue.get(), greenValue.get(), blueValue.get())
            if (styleValue.get().equals("LiquidBounce")) {
                val width = (38 + Fonts.font40.getStringWidth(target.name))
                    .coerceAtLeast(118)
                    .toFloat()
                // Draw rect box
                RenderUtils.drawBorderedRect(0F, 0F, width, 36F, 3F, Color.BLACK.rgb, Color.BLACK.rgb)
                // Damage animation
                if (easingHealth > target.health)
                    RenderUtils.drawRect(
                        0F, 34F, (easingHealth / target.maxHealth) * width,
                        36F, Color(252, 185, 65).rgb
                    )
                // Health bar
                RenderUtils.drawRect(
                    0F, 34F, (target.health / target.maxHealth) * width,
                    36F, Color(252, 96, 66).rgb
                )
                // Heal animation
                if (easingHealth < target.health)
                    RenderUtils.drawRect(
                        (easingHealth / target.maxHealth) * width, 34F,
                        (target.health / target.maxHealth) * width, 36F, Color(44, 201, 144).rgb
                    )

                easingHealth += ((target.health - easingHealth) / 2.0F.pow(10.0F - fadeSpeed.get())) * RenderUtils.deltaTime
                Fonts.font40.drawString(target.name, 36, 3, 0xffffff)
                Fonts.font35.drawString(
                    "Distance: ${decimalFormat.format(mc.thePlayer.getDistanceToEntityBox(target))}",
                    36,
                    15,
                    0xffffff
                )
                // Draw info
                val playerInfo = mc.netHandler.getPlayerInfo(target.uniqueID)
                if (playerInfo != null) {
                    Fonts.font35.drawString(
                        "Ping: ${playerInfo.responseTime.coerceAtLeast(0)}",
                        36, 24, 0xffffff
                    )
                    // Draw head
                    drawHead(target.skin, 30, 30)
                }
            }
            if (styleValue.get().equals("Novoline")) {
                val mainColor = barColor
                val percent = target.health.toInt()
                val nameLength = (fontrender.getStringWidth(target.name)).coerceAtLeast(
                    fontrender.getStringWidth(
                        "${
                            decimalFormat.format(percent)
                        }"
                    )
                ).toFloat() + 20F
                val barWidth = (target.health / target.maxHealth).coerceIn(0F, target.maxHealth) * (nameLength - 2F)
                RenderUtils.drawRect(-2F, -2F, 3F + nameLength + 36F, 2F + 36F, Color(50, 50, 50, 150).rgb)
                RenderUtils.drawRect(-1F, -1F, 2F + nameLength + 36F, 1F + 36F, Color(0, 0, 0, 100).rgb)
                drawPlayerHead(target.skin, 0, 0, 36, 36)
                Fonts.minecraftFont.drawStringWithShadow(target.name, 2F + 36F, 2F, -1)
                RenderUtils.drawRect(37F, 14F, 37F + nameLength, 24F, Color(0, 0, 0, 200).rgb)
                easingHealth += ((target.health - easingHealth) / 2.0F.pow(10.0F - fadeSpeed.get())) * RenderUtils.deltaTime
                val animateThingy =
                    (easingHealth.coerceIn(target.health, target.maxHealth) / target.maxHealth) * (nameLength - 2F)
                if (easingHealth > target.health)
                    RenderUtils.drawRect(38F, 15F, 38F + animateThingy, 23F, mainColor.darker().rgb)
                RenderUtils.drawRect(38F, 15F, 38F + barWidth, 23F, mainColor.rgb)
                Fonts.minecraftFont.drawStringWithShadow("${decimalFormat.format(percent)}", 38F, 26F, Color.WHITE.rgb)
                Fonts.fontSFUI35.drawStringWithShadow(
                    "❤",
                    Fonts.minecraftFont.getStringWidth("${decimalFormat.format(percent)}") + 40F,
                    27F,
                    Color(redValue.get(), greenValue.get(), blueValue.get()).rgb
                )
            }
            if (styleValue.get().equals("Novoline2")) {
                val width = (38 + Fonts.fontSFUI40.getStringWidth(target.name)).coerceAtLeast(118).toFloat()
                RenderUtils.drawRect(0f, 0f, width + 14f, 44f, Color(0, 0, 0, backgroundalpha.get()).rgb)
                drawPlayerHead(target.skin, 3, 3, 30, 30)
                Fonts.fontSFUI35.drawString(target.name, 34.5f, 4f, Color.WHITE.rgb)
                Fonts.fontSFUI35.drawString("Health: ${decimalFormat.format(target.health)}", 34.5f, 14f, Color.WHITE.rgb)
                Fonts.fontSFUI35.drawString(
                    "Distance: ${decimalFormat.format(mc.thePlayer.getDistanceToEntity(target))}m",
                    34.5f,
                    24f,
                    Color.WHITE.rgb
                )
                RenderUtils.drawRect(2.5f, 35.5f, width + 11.5f, 37.5f, Color(0, 0, 0, 200).rgb)
                RenderUtils.drawRect(3f, 36f, 3f + (easingHealth / target.maxHealth) * (width + 8f), 37f, barColor)
                RenderUtils.drawRect(2.5f, 39.5f, width + 11.5f, 41.5f, Color(0, 0, 0, 200).rgb)
                RenderUtils.drawRect(
                    3f,
                    40f,
                    3f + (target.totalArmorValue / 20F) * (width + 8f),
                    41f,
                    Color(77, 128, 255).rgb
                )
                easingHealth += ((target.health - easingHealth) / 2.0F.pow(10.0F - fadeSpeed.get())) * RenderUtils.deltaTime
            }
            if (styleValue.get().equals("Novoline3")) {
                val width = (38 + Fonts.minecraftFont.getStringWidth(target.name))
                    .coerceAtLeast(118)
                    .toFloat()
                counter1[0] += 1
                counter2[0] += 1
                counter1[0] = counter1[0].coerceIn(0, 50)
                counter2[0] = counter2[0].coerceIn(0, 80)
                RenderUtils.drawRect(0F, 0F, width, 34.5F, Color(0, 0, 0, backgroundalpha.get()))
                val customColor = Color(redValue.get(), greenValue.get(), blueValue.get(), 255)
                val customColor1 = Color(gredValue.get(), ggreenValue.get(), gblueValue.get(), 255)
                RenderUtils.drawGradientSideways(
                    34.0, 16.0, width.toDouble() - 2,
                    24.0, Color(40, 40, 40, 220).rgb, Color(60, 60, 60, 255).rgb
                )
                RenderUtils.drawGradientSideways(
                    34.0, 16.0, (36.0F + (easingHealth / target.maxHealth) * (width - 36.0F)).toDouble() - 2,
                    24.0, Palette.fade2(customColor, counter1[0], Fonts.fontSFUI35.FONT_HEIGHT).rgb,
                    Palette.fade2(customColor1, counter2[0], Fonts.fontSFUI35.FONT_HEIGHT).rgb
                )
                easingHealth += ((target.health - easingHealth) / 2.0F.pow(10.0F - fadeSpeed.get())) * RenderUtils.deltaTime
                Fonts.minecraftFont.drawString(target.name, 34, 4, Color(255, 255, 255, 255).rgb)
                drawPlayerHead(target.skin, 2, 2, 30, 30)
                Fonts.minecraftFont.drawStringWithShadow(
                    BigDecimal((target.health / target.maxHealth * 100).toDouble()).setScale(
                        1,
                        BigDecimal.ROUND_HALF_UP
                    ).toString() + "%", width / 2F + 5.5F, 17F, Color.white.rgb
                )
            }
            if (styleValue.get().equals("Astolfo")) {
                val colors = Color(redValue.get(), greenValue.get(), blueValue.get(), 255).rgb
                val colors1 = Color(redValue.get(), greenValue.get(), blueValue.get(), 150).rgb
                val colors2 = Color(redValue.get(), greenValue.get(), blueValue.get(), 50).rgb
                val additionalWidth = Fonts.minecraftFont.getStringWidth("${target.name}").coerceAtLeast(125)
                GlStateManager.pushMatrix()
                GlStateManager.translate((15).toFloat(), 55.toFloat(), 0.0f)
                GlStateManager.color(1f, 1f, 1f)
                GuiInventory.drawEntityOnScreen(-18, 47, 30, -180f, 0f, target)
                RenderUtils.MdrawRect(
                    -38.0,
                    -14.0,
                    133.0,
                    52.0,
                    net.ccbluex.liquidbounce.utils.render.Colors.getColor(0, 0, 0, 180)
                )
                mc.fontRendererObj.drawStringWithShadow(target.getName(), 0.0f, -8.0f, Color(255, 255, 255).rgb)
                RenderUtils.MdrawRect(0.0, (8.0f + Math.round(40.0f)).toDouble(), 130.0, 40.0, colors2)
                if (target.getHealth() / 2.0f + target.getAbsorptionAmount() / 2.0f > 1.0) {
                    RenderUtils.MdrawRect(
                        0.0,
                        (8.0f + Math.round(40.0f)).toDouble(),
                        ((target.health / target.maxHealth) * additionalWidth).toDouble() + 5f,
                        40.0,
                        colors1
                    )
                }
                RenderUtils.MdrawRect(
                    0.0,
                    (8.0f + Math.round(40.0f)).toDouble(),
                    ((easingHealth / target.maxHealth) * additionalWidth).toDouble(),
                    40.0,
                    colors
                )
                easingHealth += ((target.health - easingHealth) / 2.0F.pow(10.0F - fadeSpeed.get())) * RenderUtils.deltaTime
                GlStateManager.scale(3f, 3f, 3f)
                mc.fontRendererObj.drawStringWithShadow(
                    "${decimalFormat.format(target.health)}" + " \u2764",
                    0.0f,
                    2.5f,
                    colors
                )
                GlStateManager.popMatrix()
            }
            if (styleValue.get().equals("Exhibition")) {
                val minWidth = 140F.coerceAtLeast(45F + Fonts.fontTahoma.getStringWidth(target.name))
                RenderUtils.drawExhiRect(0F, 0F, minWidth, 45F)
                RenderUtils.drawRect(2.5F, 2.5F, 42.5F, 42.5F, Color(59, 59, 59).rgb)
                RenderUtils.drawRect(3F, 3F, 42F, 42F, Color(19, 19, 19).rgb)
                GL11.glColor4f(1f, 1f, 1f, 1f)
                RenderUtils.drawEntityOnScreen(22, 40, 15, target)
                Fonts.fontTahoma.drawString(target.name, 46, 4, -1)
                val barLength = 75F * (target.health / target.maxHealth).coerceIn(0F, 1F)
                RenderUtils.drawRect(
                    45F,
                    15F,
                    45F + 60F,
                    18F,
                    BlendUtils.getHealthColor(target.health, target.maxHealth).darker().darker().darker().rgb
                )
                RenderUtils.drawRect(
                    45F,
                    15F,
                    45F + barLength,
                    18F,
                    BlendUtils.getHealthColor(target.health, target.maxHealth).rgb
                )
                for (i in 0..9) {
                    RenderUtils.drawBorder(45F + i * 6F, 15F, 45F + (i + 1F) * 6F, 18F, 0.25F, Color.black.rgb)
                }
                GL11.glPushMatrix()
                GL11.glTranslatef(46F, 20F, 0F)
                GL11.glScalef(0.5f, 0.5f, 0.5f)
                Fonts.minecraftFont.drawString(
                    "HP: ${target.health.toInt()} | Dist: ${
                        mc.thePlayer.getDistanceToEntityBox(
                            target
                        ).toInt()
                    }", 0, 0, -1
                )
                GL11.glPopMatrix()
                GlStateManager.resetColor()
                GL11.glPushMatrix()
                GL11.glColor4f(1f, 1f, 1f, 1f)
                RenderHelper.enableGUIStandardItemLighting()
                val renderItem = mc.renderItem
                var x = 45
                var y = 26
                for (index in 3 downTo 0) {
                    val stack = target.inventory.armorInventory[index] ?: continue
                    if (stack.getItem() == null)
                        continue
                    renderItem.renderItemIntoGUI(stack, x, y)
                    renderItem.renderItemOverlays(mc.fontRendererObj, stack, x, y)
                    x += 18
                }
                val mainStack = target.heldItem
                if (mainStack != null && mainStack.getItem() != null) {
                    renderItem.renderItemIntoGUI(mainStack, x, y)
                    renderItem.renderItemOverlays(mc.fontRendererObj, mainStack, x, y)
                }
                RenderHelper.disableStandardItemLighting()
                GlStateManager.enableAlpha()
                GlStateManager.disableBlend()
                GlStateManager.disableLighting()
                GlStateManager.disableCull()
                GL11.glPopMatrix()
            }
            if (styleValue.get().equals("Flux")) {
                val hp = decimalFormat.format(easingHealth)
                val additionalWidth = fontrender.getStringWidth("${target.name}  ${hp} hp").coerceAtLeast(75)
                RenderUtils.drawCircleRect(
                    0f,
                    0f,
                    45f + additionalWidth,
                    34f,
                    5f,
                    Color(0, 0, 0, backgroundalpha.get()).rgb
                )
                drawPlayerHead(target.skin, 5, 3, 29, 28)
                RenderUtils.drawOutlinedRect(5f, 2f, 35f, 32f, 1f, bordercolor.rgb)
                // info text
                fontrender.drawString(target.name, 40, 5, Color.WHITE.rgb)
                "$hp hp".also {
                    fontrender.drawString(
                        it,
                        40 + additionalWidth - fontrender.getStringWidth(it),
                        5,
                        Color.LIGHT_GRAY.rgb
                    )
                }
                // hp bar
                val yPos = 5 + fontrender.FONT_HEIGHT + 2f
                if (easingHealth > target.health) {
                    if (colorModeValue.get().equals("Custom")) {
                        RenderUtils.drawRect(
                            40f,
                            yPos,
                            40 + (easingHealth / target.maxHealth) * additionalWidth,
                            yPos + 3.5f,
                            Color(redValue.get(), greenValue.get(), blueValue.get(), 150)
                        )
                    } else if (colorModeValue.get().equals("Health")) {
                        RenderUtils.drawRect(
                            40f,
                            yPos,
                            40 + (easingHealth / target.maxHealth) * additionalWidth,
                            yPos + 3.5f,
                            BlendUtils.getHealthColor(target.health, target.maxHealth)
                        )
                    }
                }
                RenderUtils.drawRect(
                    40f,
                    yPos,
                    40 + (target.health / target.maxHealth) * additionalWidth,
                    yPos + 3.5f,
                    barColor
                )
                RenderUtils.drawRect(
                    40f,
                    yPos + 9,
                    40 + (target.totalArmorValue / 20F) * additionalWidth,
                    yPos + 12.5f,
                    Color(77, 128, 255).rgb
                )
                easingHealth += ((target.health - easingHealth) / 2.0F.pow(10.0F - fadeSpeed.get())) * RenderUtils.deltaTime
            }
            if (styleValue.get().equals("Simplicity")) {
                GlStateManager.pushMatrix()
                var width = 100.0
                width = PlayerUtils.getIncremental(width, -50.0)
                Fonts.font35.drawStringWithShadow("\u00a7l" + target.getName(), (38).toFloat(), 2.0f, -1)
                if (width < 80.0) {
                    width = 80.0
                }
                if (width > 80.0) {
                    width = 80.0
                }
                RenderUtils.drawGradientSideways(
                    37.5,
                    11.toDouble(),
                    37.5 + (easingHealth / target.maxHealth) * width,
                    (19).toDouble(),
                    rainbow(5000000000L).rgb,
                    rainbow(500L).rgb
                )
                RenderUtils.rectangleBordered(
                    37.0,
                    10.5,
                    38.0 + (easingHealth / target.maxHealth) * width,
                    19.5,
                    0.5,
                    Colors.getColor(0, 0),
                    Colors.getColor(0)
                )
                easingHealth += ((target.health - easingHealth) / 2.0F.pow(10.0F - fadeSpeed.get())) * RenderUtils.deltaTime
                GlStateManager.resetColor()
                GlStateManager.popMatrix()
            }
            if (styleValue.get().equals("AsuidBounce")) {
                GL11.glPushMatrix()
                RenderUtils.drawBorderedRect(0F, 0F, 120F, 38f, 2f, Color(5, 5, 5, 255).rgb, Color(25, 25, 25, 255).rgb)
                RenderUtils.drawRect(1f, 34f, 119f, 37f, Color(50, 50, 50, 255).rgb)
                RenderUtils.drawRect(75F, 25.5f, 115F, 29.5f, Color(35, 35, 35, 255).rgb)
                RenderUtils.drawRect(75F, 15.5f, 115F, 19.5f, Color(35, 35, 35, 255).rgb)
                drawPlayerHead(target.skin, 2, 2, 30, 30)
                RenderUtils.drawOutlinedRect(1f, 1f, 33f, 33f, 0.5f, Color(65, 65, 65, 255).rgb)
                Fonts.fontSFUI35.drawString(target.getName(), 36F, 3f, Color.WHITE.rgb)
                Fonts.fontSFUI35.drawString("Distance", 36F, 14f, Color.WHITE.rgb)
                Fonts.fontSFUI35.drawString(
                    "Armor " + decimalFormat2.format(target.getTotalArmorValue() / 2f),
                    36F,
                    24f,
                    Color.WHITE.rgb
                )
                RenderUtils.drawRect(
                    1f,
                    34f,
                    1f + min((Math.round(easingHealth / target.getMaxHealth() * 10000) / 80f), 118f),
                    37f,
                    barColor
                )
                RenderUtils.drawRect(
                    75F,
                    25.5f,
                    75F + min((Math.round(target.getTotalArmorValue() / 20F * 10000) / 250f), 40f),
                    29.5f,
                    Color(170, 145, 100).rgb
                )
                RenderUtils.drawRect(
                    75F,
                    15.5f,
                    75F + min((Math.round(mc.thePlayer.getDistanceToEntity(target) / 10F * 10000) / 250f), 40f),
                    19.5f,
                    Color(175, 100, 80).rgb
                )
                RenderUtils.drawOutlinedRect(74.5f, 25f, 115.5f, 30f, 0.5f, Color(0, 0, 0, 255).rgb)
                RenderUtils.drawOutlinedRect(74.5f, 15f, 115.5f, 20f, 0.5f, Color(0, 0, 0, 255).rgb)
                easingHealth += ((target.health - easingHealth) / 2.0F.pow(10.0F - fadeSpeed.get())) * RenderUtils.deltaTime
                GL11.glPopMatrix()
            }
            if (styleValue.get().equals("Style")) {
                var index = 0
                while (index < particles.size) {
                    val update = particles[index]
                    if (update.animaitonA >= 255) particles.remove(update)
                    index++
                }
                var Stringindex = 0
                while (Stringindex < Stringparticles.size) {
                    val update = Stringparticles[Stringindex]
                    if (update.StringanimaitonA >= 255) Stringparticles.remove(update)
                    Stringindex++
                }
                if (target != null) {
                    if (target != lastTarget || easingHealth < 0 || easingHealth > target.maxHealth || abs(easingHealth - target.health) < 0.01) easingHealth =
                        target.health
                    val x = if (ColorUtils.stripColor(target.displayName.unformattedText)!!.length >= 13)
                        Fonts.font40.getStringWidth(ColorUtils.stripColor(target.displayName.unformattedText)!!) + 50f else 110f
                    if (target.hurtTime > 0) {
                        if (addTimer.hasTimePassed(500)) {
                            for (i in 0..15) {
                                val Hitparticles = TargetHudParticles()
                                Hitparticles.x = RandomUtils.nextFloat(-60f, 60f)
                                Hitparticles.y = RandomUtils.nextFloat(-50f, 50f)
                                Hitparticles.size = RandomUtils.nextFloat(1.5f, 3.0f)
                                Hitparticles.Red = RandomUtils.nextInt(30, 255)
                                Hitparticles.Green = RandomUtils.nextInt(30, 255)
                                Hitparticles.Blue = RandomUtils.nextInt(30, 255)
                                Hitparticles.A = 255f
                                particles.add(Hitparticles)
                            }
                            addTimer.reset()
                        }
                    }
                    RenderUtils.drawRect(1f, 2f, x + 31f, 48.0f, Color(0, 0, 0, 100).rgb)
                    particles.forEachIndexed { index, module ->
                        if (module.x > 0) module.animaitonX += Math.abs((module.x - (module.x - (Math.abs(module.x - module.animaitonX)))) / 150) * RenderUtils.deltaTime
                        else if (module.x < 0) module.animaitonX -= Math.abs((module.x - (module.x - (Math.abs(module.x - module.animaitonX)))) / 150) * RenderUtils.deltaTime

                        if (Math.abs(module.animaitonX) >= Math.abs(module.x)) module.animaitonX = module.x

                        if (module.y > 0)
                            module.animaitonY += Math.abs((module.y - (module.y - (Math.abs(module.y - module.animaitonY)))) / 150) * RenderUtils.deltaTime
                        else if (module.y < 0) module.animaitonY -= Math.abs((module.y - (module.y - (Math.abs(module.y - module.animaitonY)))) / 150) * RenderUtils.deltaTime
                        if (Math.abs(module.animaitonY) >= Math.abs(module.y)) module.animaitonY = module.y

                        if (module.A > 0) {
                            module.animaitonA += 0.2f * RenderUtils.deltaTime
                            if (module.animaitonA >= module.A) module.animaitonA = module.A
                        }
                        RenderUtils.drawCircleFull(
                            20 + module.animaitonX,
                            20 + module.animaitonY,
                            module.size,
                            0f,
                            Color(module.Red, module.Green, module.Blue, 255 - module.animaitonA.toInt())
                        )
                    }
                    GL11.glPushMatrix()
                    GL11.glTranslated(2.5 + (target.hurtTime * 0.1), 2.5 + (target.hurtTime * 0.1), 0.0)
                    GL11.glScaled(
                        1.0 - (target.hurtTime * 0.01),
                        1.0 - (target.hurtTime * 0.01),
                        1.0 - (target.hurtTime * 0.01)
                    )
                    GL11.glColor4f(1.0f, 1.0f - (target.hurtTime * 0.05f), 1.0f - (target.hurtTime * 0.05f), 1.0f)
                    val playerInfo = mc.netHandler.getPlayerInfo(target.uniqueID)
                    if (playerInfo != null) {
                        val locationSkin = target.skin
                        mc.textureManager.bindTexture(locationSkin)
                        RenderUtils.drawScaledCustomSizeModalRect(2, 2, 8F, 8F, 8, 8, 33, 33, 64F, 64F)
                        GL11.glColor4f(1F, 1F, 1F, 1F)
                    }
                    GL11.glPopMatrix()
                    val DmageHealth = easingHealth > target.health
                    val HealHealth = easingHealth < target.health
                    val str = target.displayName.unformattedText
                    if (str.toByteArray().size == str.length) {
                        GlStateManager.resetColor()
                        Fonts.font40.drawString("Name ${target.displayName.unformattedText}", 40, 10, -1)
                    } else
                        Fonts.font40.drawString(target.displayName.unformattedText, 40, 10, -1)
                    Fonts.fontSFUI35.drawString(
                        "Dist ${
                            DecimalFormat("0.0").format(
                                mc.thePlayer.getDistanceToEntity(
                                    target
                                )
                            )
                        } Hurt ${target.hurtTime}", 40f, 25f, -1
                    )
                    GlStateManager.pushMatrix()
                    GlStateManager.translate(5.0, 0.0, 0.0) // Damage animation
                    if (DmageHealth) {
                        RenderUtils.drawRect(
                            0F,
                            41F,
                            (easingHealth / target.maxHealth) * x,
                            45F,
                            Color(252, 185, 65).rgb
                        )
                    }
                    RenderUtils.drawGradientSideways(
                        0.0,
                        41.0,
                        (target.health / target.maxHealth) * x.toDouble(),
                        45.0,
                        rainbow(8000000000L).rgb,
                        rainbow(200000L).rgb
                    )
                    if (HealHealth) {
                        if (addTimer.hasTimePassed(500)) {
                            for (i in 0..5) {
                                val stringparticles = TargetHudParticles()
                                stringparticles.Stringx = RandomUtils.nextFloat(-20f, 20f)
                                stringparticles.Stringy = RandomUtils.nextFloat(-20f, 20f)
                                stringparticles.StringA = 255f
                                Stringparticles.add(stringparticles)
                            }
                            addTimer.reset()
                        }
                        RenderUtils.drawRect(
                            (easingHealth / target.maxHealth) * x,
                            44F,
                            (target.health / target.maxHealth) * 110,
                            47F,
                            Color(redValue.get(), greenValue.get(), blueValue.get()).rgb
                        )
                    }
                    GlStateManager.popMatrix()
                    easingHealth += ((target.health - easingHealth) / 2.0F.pow(10.0F - fadeSpeed.get())) * RenderUtils.deltaTime
                    val Hpstring = DecimalFormat("0").format((easingHealth / target.maxHealth) * 100)
                    Fonts.fontSFUI35.drawString(
                        "${Hpstring}%",
                        (easingHealth / target.maxHealth) * x + Fonts.minecraftFont.drawStringWithShadow("String",
                            1F,1F , 1) / 2 + 1.5f,
                        40f,
                        if (DmageHealth) Color(255, 111, 111).rgb else Color(255, 255, 255).rgb
                    )
                }
            }

            if (styleValue.get().equals("Hanabi")) {
                val blackcolor = Color(0, 0, 0, 180).rgb
                val blackcolor2 = Color(200, 200, 200).rgb
                val health: Float
                var hpPercentage: Double
                val hurt: Color
                val healthStr: String
                val width = (38 + Fonts.font40.getStringWidth(target.name))
                    .coerceAtLeast(140)
                    .toFloat()
                health = target.getHealth()
                hpPercentage = (health / target.getMaxHealth()).toDouble()
                hurt = Color.getHSBColor(310f / 360f, target.hurtTime.toFloat() / 10f, 1f)
                healthStr = (target.getHealth().toInt().toFloat() / 2.0f).toString()
                hpPercentage = MathHelper.clamp_double(hpPercentage, 0.0, 1.0)
                val hpWidth = 140.0 * hpPercentage
                this.healthBarWidth2 = AnimationUtils.animate(hpWidth, this.healthBarWidth2, 0.20000000298023224)
                this.healthBarWidth = RenderUtils.getAnimationStateSmooth(
                    hpWidth,
                    this.healthBarWidth,
                    (14f / Minecraft.getDebugFPS()).toDouble()
                ).toFloat().toDouble()
                this.hudHeight =
                    RenderUtils.getAnimationStateSmooth(40.0, this.hudHeight, (8f / Minecraft.getDebugFPS()).toDouble())
                if (hudHeight == 0.0) {
                    this.healthBarWidth2 = 140.0
                    this.healthBarWidth = 140.0
                }
                RenderUtils.prepareScissorBox(
                    0f,
                    (40 - hudHeight).toFloat(),
                    (x + 140.0f).toFloat(),
                    (y + 40).toFloat()
                )
                RenderUtils.drawRect(0f, 0f, 140.0f, 40.0f, blackcolor)
                RenderUtils.drawRect(0f, 37.0f, 140f, 40f, Color(0, 0, 0, 48).rgb)
                drawPlayerHead(target.skin, 2, 2, 33, 33)
                if (easingHealth > target.health)
                    RenderUtils.drawRect(
                        0F,
                        37.0f,
                        (easingHealth / target.maxHealth) * width,
                        40.0f,
                        Color(255, 0, 213, 220).rgb
                    )
                // Health bar
                RenderUtils.drawGradientSideways(
                    0.0, 37.0, ((target.health / target.maxHealth) * width).toDouble(),
                    40.0, Color(0, 126, 255).rgb, Color(0, 210, 255).rgb
                )
                easingHealth += ((target.health - easingHealth) / 2.0F.pow(10.0F - fadeSpeed.get())) * RenderUtils.deltaTime
                Fonts.fontSFUI35.drawStringWithShadow("❤", 112F, 28F, hurt.rgb)
                Fonts.fontSFUI35.drawStringWithShadow(healthStr, 120F, 28F, Color.WHITE.rgb)
                Fonts.font35.drawString(
                    "XYZ:" + target.posX.toInt() + " " + target.posY.toInt() + " " + target.posZ.toInt() + " | " + "Hurt:" + (target.hurtTime > 0),
                    38F,
                    15f,
                    blackcolor2
                )
                Fonts.font40.drawString(target.getName(), 38.0f, 4.0f, blackcolor2)
                mc.textureManager.bindTexture((target as AbstractClientPlayer).locationSkin)
                Gui.drawScaledCustomSizeModalRect(3, 3, 8.0f, 8.0f, 8, 8, 32, 32, 64f, 64f)
            }
            if (styleValue.get().equals("Lnk")) {
                val width = (38 + Fonts.minecraftFont.getStringWidth(target.name))
                    .coerceAtLeast(118)
                    .toFloat()
                val fractions = floatArrayOf(0.0f, 0.5f, 1.0f)
                val colors = arrayOf(Color.RED, Color.YELLOW, Color(10, 255, 40))
                val progress = easingHealth / target.maxHealth
                val customColor = if (easingHealth >= target.maxHealth) Color.GREEN else Colors.blendColors(
                    fractions,
                    colors,
                    progress
                ).brighter()
                RenderUtils.drawRect(0F, 0F, width + 5F, 45F, Color(35, 35, 35, 190))
                GL11.glPushMatrix()
                GL11.glColor4f(1f, 1f, 1f, 1f)
                RenderUtils.drawEntityOnScreen(10.0, 33.0, 16f, target)
                GL11.glPopMatrix()
                mc.fontRendererObj.drawStringWithShadow(target.name, 23f, 4f, Color.white.rgb)
                RenderUtils.drawRect(3f, 37f, width + 2.5f, 42f, Color(30, 30, 30, 120))
                RenderUtils.drawRect(
                    3f,
                    37f,
                    easingHealth / target.maxHealth * (width + 2.5f),
                    42f,
                    Color(customColor.red, customColor.green, customColor.blue, 160)
                )
                easingHealth += ((target.health - easingHealth) / 2.0F.pow(10.0F - fadeSpeed.get())) * RenderUtils.deltaTime
                var x = 22
                var y = 14
                for (index in 3 downTo 0) {
                    RenderUtils.drawRect(
                        x.toFloat(),
                        y.toFloat(),
                        x.toFloat() + 18f,
                        y + 15f,
                        Color(30, 30, 30, 120).rgb
                    )
                    if (mc.thePlayer.inventory.armorInventory[index] != null) {
                        GlStateManager.pushMatrix()
                        GlStateManager.scale(0.65, 0.65, 0.65)
                        mc.fontRendererObj.drawStringWithShadow(
                            ((mc.thePlayer.inventory.armorInventory[index].maxDamage - mc.thePlayer.inventory.armorInventory[index].itemDamage)).toString(),
                            (x.toFloat() + 4f) * 1 / 0.65f,
                            47f,
                            Color.white.rgb
                        )
                        GlStateManager.scale(1 / 0.65, 1 / 0.65, 1 / 0.65)
                        GlStateManager.popMatrix()
                        GL11.glPushMatrix()
                        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
                        if (mc.theWorld != null) {
                            GLUtils.enableGUIStandardItemLighting()
                        }
                        GlStateManager.pushMatrix()
                        GlStateManager.disableAlpha()
                        GlStateManager.clear(256)
                        mc.renderItem.renderItemIntoGUI(mc.thePlayer.inventory.armorInventory[index], x + 1, y - 1)
                        mc.renderItem.zLevel = 0.0f
                        GlStateManager.disableBlend()
                        GlStateManager.scale(0.5, 0.5, 0.5)
                        GlStateManager.disableDepth()
                        GlStateManager.disableLighting()
                        GlStateManager.enableDepth()
                        GlStateManager.scale(2.0f, 2.0f, 25.0f)
                        GlStateManager.enableAlpha()
                        GlStateManager.popMatrix()
                        GL11.glPopMatrix()
                    }
                    x += 20
                }
                RenderUtils.drawRect(x.toFloat(), y.toFloat(), x.toFloat() + 18f, y + 15f, Color(30, 30, 30, 120).rgb)
                if (mc.thePlayer.inventory.mainInventory[mc.thePlayer.inventory.currentItem] != null) {
                    if (mc.thePlayer.inventory.mainInventory[mc.thePlayer.inventory.currentItem].isItemStackDamageable) {
                        GlStateManager.pushMatrix()
                        GlStateManager.scale(0.65, 0.65, 0.65)
                        mc.fontRendererObj.drawStringWithShadow(
                            ((mc.thePlayer.inventory.mainInventory[mc.thePlayer.inventory.currentItem].maxDamage - mc.thePlayer.inventory.mainInventory[mc.thePlayer.inventory.currentItem].itemDamage)).toString(),
                            (x.toFloat() + 4f) * 1 / 0.65f,
                            47f,
                            Color.white.rgb
                        )
                        GlStateManager.scale(1 / 0.65, 1 / 0.65, 1 / 0.65)
                        GlStateManager.popMatrix()
                    }
                    GL11.glPushMatrix()
                    GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
                    if (mc.theWorld != null) {
                        RenderHelper.enableGUIStandardItemLighting()
                    }
                    GlStateManager.pushMatrix()
                    GlStateManager.disableAlpha()
                    GlStateManager.clear(256)
                    mc.renderItem.renderItemIntoGUI(
                        mc.thePlayer.inventory.mainInventory[mc.thePlayer.inventory.currentItem],
                        x + 1,
                        y - 1
                    )
                    mc.renderItem.zLevel = 0.0f
                    GlStateManager.disableBlend()
                    GlStateManager.scale(0.5, 0.5, 0.5)
                    GlStateManager.disableDepth()
                    GlStateManager.disableLighting()
                    GlStateManager.enableDepth()
                    GlStateManager.scale(2.0f, 2.0f, 2.0f)
                    GlStateManager.enableAlpha()
                    GlStateManager.popMatrix()
                    GL11.glPopMatrix()
                }
            }
            if (par.get()) {
                if (target != null) {
                    if (target != lastTarget || easingHealth < 0 || easingHealth > target.maxHealth || abs(easingHealth - target.health) < 0.01) easingHealth =
                        target.health
                    if (target.hurtTime > 0) {
                        if (addTimer.hasTimePassed(500)) {
                            for (i in 0..15) {
                                val Hitparticles = TargetHudParticles()
                                Hitparticles.x = RandomUtils.nextFloat(-60f, 60f)
                                Hitparticles.y = RandomUtils.nextFloat(-50f, 50f)
                                Hitparticles.size = RandomUtils.nextFloat(1.5f, 3.0f)
                                Hitparticles.Red = RandomUtils.nextInt(30, 255)
                                Hitparticles.Green = RandomUtils.nextInt(30, 255)
                                Hitparticles.Blue = RandomUtils.nextInt(30, 255)
                                Hitparticles.A = 255f
                                particles.add(Hitparticles)
                            }
                            addTimer.reset()
                        }
                    }
                    particles.forEachIndexed { index, module ->
                        if (module.x > 0) module.animaitonX += Math.abs((module.x - (module.x - (Math.abs(module.x - module.animaitonX)))) / 150) * RenderUtils.deltaTime
                        else if (module.x < 0) module.animaitonX -= Math.abs((module.x - (module.x - (Math.abs(module.x - module.animaitonX)))) / 150) * RenderUtils.deltaTime

                        if (Math.abs(module.animaitonX) >= Math.abs(module.x)) module.animaitonX = module.x

                        if (module.y > 0)
                            module.animaitonY += Math.abs((module.y - (module.y - (Math.abs(module.y - module.animaitonY)))) / 150) * RenderUtils.deltaTime
                        else if (module.y < 0) module.animaitonY -= Math.abs((module.y - (module.y - (Math.abs(module.y - module.animaitonY)))) / 150) * RenderUtils.deltaTime
                        if (Math.abs(module.animaitonY) >= Math.abs(module.y)) module.animaitonY = module.y

                        if (module.A > 0) {
                            module.animaitonA += 0.2f * RenderUtils.deltaTime
                            if (module.animaitonA >= module.A) module.animaitonA = module.A
                        }
                        RenderUtils.drawCircleFull(
                            20 + module.animaitonX,
                            20 + module.animaitonY,
                            module.size,
                            0f,
                            Color(module.Red, module.Green, module.Blue, 255 - module.animaitonA.toInt())
                        )
                    }
                    val HealHealth = easingHealth < target.health
                    if (HealHealth) {
                        if (addTimer.hasTimePassed(500)) {
                            for (i in 0..5) {
                                val stringparticles = TargetHudParticles()
                                stringparticles.Stringx = RandomUtils.nextFloat(-20f, 20f)
                                stringparticles.Stringy = RandomUtils.nextFloat(-20f, 20f)
                                stringparticles.StringA = 255f
                                Stringparticles.add(stringparticles)
                            }
                            addTimer.reset()
                        }
                    }
                }
            }
        }
        lastTarget = target
        return getTBorder()
    }

    private fun getTBorder(): Border = when (styleValue.get()) {
        "Novoline" -> Border(-1F, -2F, 108F, 38F)
        "Exhibition" -> Border(0F, 3F, 140F, 48F)
        "LiquidBounce" -> Border(
            0F,
            0F,
            (36 + mc.thePlayer.name.let(Fonts.font40::getStringWidth)).coerceAtLeast(118).toFloat(),
            36F
        )
        "Flux" -> Border(0F, 0F, 135F, 32F)
        "Style" -> Border(2f, 0f, 140F, 48F)
        "Astolfo" -> Border(-20F, 40F, 148F, 107F)
        "Hanabi" -> Border(0F, 0F, 140F, 40F)
        "Lnk" -> Border(0F, 0F, 124F, 44F)
        "Simplicity" -> Border(37F, 0F, 119F, 20F)
        else -> Border(0F, 0F, 120F, 38F)
    }

    private fun drawHead(skin: ResourceLocation, width: Int, height: Int) {
        GL11.glColor4f(1F, 1F, 1F, 1F)
        mc.textureManager.bindTexture(skin)
        Gui.drawScaledCustomSizeModalRect(
            2, 2, 8F, 8F, 8, 8, width, height,
            64F, 64F
        )
    }

    private fun drawPlayerHead(skin: ResourceLocation, x: Int, y: Int, width: Int, height: Int) {
        GL11.glColor4f(1F, 1F, 1F, 1F)
        mc.textureManager.bindTexture(skin)
        Gui.drawScaledCustomSizeModalRect(
            x, y, 8F, 8F, 8, 8, width, height,
            64F, 64F
        )
    }
}
