/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.ccbluex.liquidbounce.features.module.modules.combat.TeleportAura
import net.ccbluex.liquidbounce.features.module.modules.render.ColorMixer
import net.ccbluex.liquidbounce.ui.client.hud.designer.GuiHudDesigner
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.TargetStyle
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.impl.*
import net.ccbluex.liquidbounce.utils.render.*
import net.ccbluex.liquidbounce.utils.render.animations.Direction
import net.ccbluex.liquidbounce.utils.render.animations.impl.EaseBackIn
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.*
import net.minecraft.client.gui.GuiChat
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import org.lwjgl.opengl.GL11
import java.awt.Color

/**
 * A target hud
 */
@ElementInfo(name = "Target", disableScale = true, retrieveDamage = true)
class Target : Element() {

    val styleList = mutableListOf<TargetStyle>()

    val styleValue: ListValue

    // Global variables
    val blurValue = BoolValue("Blur", false)
    val blurStrength = FloatValue("Blur-Strength", 1F, 0.01F, 40F, { blurValue.get() })

    val shadowValue = BoolValue("Shadow", false)
    val shadowStrength = FloatValue("Shadow-Strength", 1F, 0.01F, 40F, { shadowValue.get() })
    val shadowColorMode = ListValue("Shadow-Color", arrayOf("Background", "Custom", "Bar"), "Background", { shadowValue.get() })

    val shadowColorRedValue = IntegerValue("Shadow-Red", 0, 0, 255, { shadowValue.get() && shadowColorMode.get().equals("custom", true) })
    val shadowColorGreenValue = IntegerValue("Shadow-Green", 111, 0, 255, { shadowValue.get() && shadowColorMode.get().equals("custom", true) })
    val shadowColorBlueValue = IntegerValue("Shadow-Blue", 255, 0, 255, { shadowValue.get() && shadowColorMode.get().equals("custom", true) })

    val fadeValue = BoolValue("FadeAnim", false)
    val fadeSpeed = FloatValue("Fade-Speed", 1F, 0F, 5F, { fadeValue.get() })
    val animation = EaseBackIn(350 * this.fadeSpeed.get().toInt(),1.0,2f)
    val noAnimValue = BoolValue("No-Animation", false)
    val globalAnimSpeed = FloatValue("Global-AnimSpeed", 3F, 1F, 6.30F, { !noAnimValue.get() })

    val showWithChatOpen = BoolValue("Show-ChatOpen", true)
    val resetBar = BoolValue("ResetBarWhenHiding", false)

    val colorModeValue = ListValue("Color", arrayOf("Custom", "Rainbow", "Sky", "Slowly", "Fade", "Mixer", "Health"), "Custom")
    val redValue = IntegerValue("Red", 252, 0, 255)
    val greenValue = IntegerValue("Green", 96, 0, 255)
    val blueValue = IntegerValue("Blue", 66, 0, 255)
    val saturationValue = FloatValue("Saturation", 1F, 0F, 1F)
    val brightnessValue = FloatValue("Brightness", 1F, 0F, 1F)
    val waveSecondValue = IntegerValue("Seconds", 2, 1, 10)
    val bgRedValue = IntegerValue("Background-Red", 0, 0, 255)
    val bgGreenValue = IntegerValue("Background-Green", 0, 0, 255)
    val bgBlueValue = IntegerValue("Background-Blue", 0, 0, 255)
    val bgAlphaValue = IntegerValue("Background-Alpha", 160, 0, 255)

    val bordercolor = Color(redValue.get(), greenValue.get(), blueValue.get())

    var target = (LiquidBounce.moduleManager[KillAura::class.java] as KillAura).target

    var healthBarWidth2 = 0.0
    var hudHeight = 0.0
    var healthBarWidth = 0.0

    val counter1 = intArrayOf(50)
    val counter2 = intArrayOf(80)
    var lastTarget: Entity? = null
    val addTimer = MSTimer()

    override val values: List<Value<*>>
        get() {
            val valueList = mutableListOf<Value<*>>()
            styleList.forEach { valueList.addAll(it.values) }
            return super.values.toMutableList() + valueList
        }
    //
    init {
        styleValue = ListValue("Style", addStyles(
            Astolfo(this),
            Astolfo2(this),
            AsuidBounce(this),
            Chill(this),
            Distance(this),
            Exhibition(this),
            OldExhibition(this),
            Flux(this),
            Hanabi(this),
            IDK(this),
            IDK2(this),
            LiquidBounce(this),
            Lnk(this),
            Moon(this),
            MoonTwo(this),
            Moon4(this),
            Novoline(this),
            NovolineTwo(this),
            NovolineThree(this),
            NovolineFour(this),
            NovolineFive(this),
            NovolineOld(this),
            Remix(this),
            Rice(this),
            Slowly(this),
            Simplicity(this),
            Tifality(this),
        ).toTypedArray(), "LiquidBounce")
    }
    val killAura: KillAura = LiquidBounce.moduleManager.getModule(KillAura::class.java) as KillAura
    val tpaura: TeleportAura = LiquidBounce.moduleManager.getModule(TeleportAura::class.java) as TeleportAura
    var mainTarget: EntityPlayer? = null
    var animProgress = 0F

    var barColor = Color(-1)
    var bgColor = Color(-1)

    override fun drawElement(): Border? {
        val mainStyle = getCurrentStyle(styleValue.get()) ?: return null

        val kaTarget = (LiquidBounce.moduleManager[KillAura::class.java] as KillAura).target
        val taTarget = (LiquidBounce.moduleManager[TeleportAura::class.java] as TeleportAura).lastTarget

        val actualTarget = if (kaTarget != null && kaTarget is EntityPlayer) kaTarget 
                            else if (taTarget != null &&  taTarget is EntityPlayer) taTarget
                            else if ((mc.currentScreen is GuiChat && showWithChatOpen.get()) || mc.currentScreen is GuiHudDesigner) mc.thePlayer 
                            else null
        if (!killAura.state || !tpaura.state) {
            animation.setDirection(Direction.BACKWARDS)
        }
        if (actualTarget != null){
            animation.setDirection(Direction.FORWARDS)
        }
        if (actualTarget == null){
            animation.setDirection(Direction.BACKWARDS)
        }
        val preBarColor = when (colorModeValue.get()) {
            "Rainbow" -> Color(RenderUtils.getRainbowOpaque(waveSecondValue.get(), saturationValue.get(), brightnessValue.get(), 0))
            "Custom" -> Color(redValue.get(), greenValue.get(), blueValue.get())
            "Sky" -> RenderUtils.skyRainbow(0, saturationValue.get(), brightnessValue.get())
            "Fade" -> ColorUtils.fade(Color(redValue.get(), greenValue.get(), blueValue.get()), 0, 100)
            "Health" -> if (actualTarget != null) BlendUtils.getHealthColor(actualTarget.health, actualTarget.maxHealth) else Color.green
            "Mixer" -> ColorMixer.getMixedColor(0, waveSecondValue.get())
            else -> ColorUtils.LiquidSlowly(System.nanoTime(), 0, saturationValue.get(), brightnessValue.get())!!
        }

        val preBgColor = Color(bgRedValue.get(), bgGreenValue.get(), bgBlueValue.get(), bgAlphaValue.get())

        if (fadeValue.get())
            animProgress += (0.0075F * fadeSpeed.get() * RenderUtils.deltaTime * if (actualTarget != null) -1F else 1F)
        else animProgress = 0F

        animProgress = animProgress.coerceIn(0F, 1F)

        barColor = ColorUtils.reAlpha(preBarColor, preBarColor.alpha / 255F * (1F - animProgress))
        bgColor = ColorUtils.reAlpha(preBgColor, preBgColor.alpha / 255F * (1F - animProgress))

        if (actualTarget != null || !fadeValue.get())
            mainTarget = actualTarget
        else if (animProgress >= 1F)
            mainTarget = null

        val returnBorder = mainStyle.getBorder(mainTarget) ?: return null
        val borderWidth = returnBorder.x2 - returnBorder.x
        val borderHeight = returnBorder.y2 - returnBorder.y

        if (mainTarget == null) {
            if (resetBar.get()) 
                mainStyle.easingHealth = 0F
            if (mainStyle is Rice)
                mainStyle.particleList.clear()
            return returnBorder
        }
        val convertTarget = mainTarget!! as EntityPlayer
        
        val calcScaleX = animProgress * (4F / (borderWidth / 2F))
        val calcScaleY = animProgress * (4F / (borderHeight / 2F))
        val calcTranslateX = borderWidth / 2F * calcScaleX
        val calcTranslateY = borderHeight / 2F * calcScaleY

        val idkWidth = returnBorder.x2
        val idkHeight = returnBorder.y2

            if (shadowValue.get() && mainStyle.shaderSupport) {
            val floatX = renderX.toFloat()
            val floatY = renderY.toFloat()

            GL11.glTranslated(-renderX, -renderY, 0.0)
            GL11.glPushMatrix()

            ShadowUtils.shadow(shadowStrength.get(), {
                GL11.glPushMatrix()
                GL11.glTranslated(renderX, renderY, 0.0)
                if (fadeValue.get()) {
                    RenderUtils.scaleStart(idkWidth/2, idkHeight/2,animation.output.toFloat())
                }
                mainStyle.handleShadow(convertTarget)
                GL11.glPopMatrix()
            }, {
                GL11.glPushMatrix()
                GL11.glTranslated(renderX, renderY, 0.0)
                if (fadeValue.get()) {
                    RenderUtils.scaleStart(idkWidth/2, idkHeight/2,animation.output.toFloat())
                }
                mainStyle.handleShadowCut(convertTarget)
                GL11.glPopMatrix()
            })

            GL11.glPopMatrix()
            GL11.glTranslated(renderX, renderY, 0.0)
        }

        if (blurValue.get() && mainStyle.shaderSupport) {
            val floatX = renderX.toFloat()
            val floatY = renderY.toFloat()

            GL11.glTranslated(-renderX, -renderY, 0.0)
            GL11.glPushMatrix()
            BlurUtils.blur(floatX + returnBorder.x, floatY + returnBorder.y, floatX + returnBorder.x2, floatY + returnBorder.y2, blurStrength.get() * (1F - animProgress), false) {
                GL11.glPushMatrix()
                GL11.glTranslated(renderX, renderY, 0.0)
                if (fadeValue.get()) {
                    RenderUtils.scaleStart(idkWidth/2, idkHeight/2,animation.output.toFloat())
                }
                mainStyle.handleBlur(convertTarget)
                GL11.glPopMatrix()
            }
            GL11.glPopMatrix()
            GL11.glTranslated(renderX, renderY, 0.0)
        }

        if (fadeValue.get()) {
            RenderUtils.scaleStart(idkWidth/2, idkHeight/2,animation.output.toFloat())
        }
        
        if (mainStyle is Chill)
            mainStyle.updateData(renderX.toFloat() + calcTranslateX, renderY.toFloat() + calcTranslateY, calcScaleX, calcScaleY)
        mainStyle.drawTarget(convertTarget)

        GlStateManager.resetColor()
        return returnBorder
    }

    override fun handleDamage(ent: EntityPlayer) {
        if (mainTarget != null && ent == mainTarget)
            getCurrentStyle(styleValue.get())?.handleDamage(ent)
    }

    fun getFadeProgress() = animProgress

    @SafeVarargs
    fun addStyles(vararg styles: TargetStyle): List<String> {
        val nameList = mutableListOf<String>()
        styles.forEach { 
            styleList.add(it) 
            nameList.add(it.name)
        }
        return nameList
    }

    fun getCurrentStyle(styleName: String): TargetStyle? = styleList.find { it.name.equals(styleName, true) }
    
}
