package net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.impl


import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.module.modules.render.HUD
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Target
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.TargetStyle
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.extensions.skin
import net.ccbluex.liquidbounce.utils.misc.RandomUtils
import net.ccbluex.liquidbounce.utils.render.*
import net.ccbluex.liquidbounce.utils.render.ColorUtils.LiquidSlowly
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.entity.player.EntityPlayer
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.pow


class NovolineFive(inst: Target): TargetStyle("NovolineFive", inst, true){
    private val riseParticle = BoolValue("Rise-Particle", true, { targetInstance.styleValue.get().equals("NovolineFive", true) })
    private val riseParticleFade = BoolValue("Rise-Particle-Fade", true, { targetInstance.styleValue.get().equals("NovolineFive", true) })
    private val riseParticleSpeed = FloatValue("Rise-ParticleSpeed", 0.01F, 0.01F, 0.2F, { targetInstance.styleValue.get().equals("NovolineFive", true) })
    private val particleList = mutableListOf<Particle>()
    private var gotDamaged: Boolean = false
    val hurtTimeAnim = BoolValue("HurtTimeAnim", true, { targetInstance.styleValue.get().equals("NovolineFive", true) })
    val gradientDistanceValue = IntegerValue("GradientDistance", 50, 1, 200, { targetInstance.styleValue.get().equals("NovolineFive", true) })
    override fun drawTarget(entity: EntityPlayer) {
        updateAnim(entity.health)
        val ColorMixer = (LiquidBounce.moduleManager.getModule(net.ccbluex.liquidbounce.features.module.modules.render.ColorMixer::class.java) as net.ccbluex.liquidbounce.features.module.modules.render.ColorMixer)!!


        val width = (38 + Fonts.fontSFUI40.getStringWidth(entity.name))
            .coerceAtLeast(118)
            .toFloat()

        RenderUtils.drawRoundedRect(-3F, -4F, width + 27F, 47F,1F,targetInstance.bgColor.rgb)

        Fonts.fontSFUI40.drawStringWithShadow(entity.name, 40f, 3f, Color(255,255,255,255).rgb)
        val playerInfo = mc.netHandler.getPlayerInfo(entity.uniqueID)
        if (playerInfo != null) {


            // Draw head
            val locationSkin = entity.skin
            if (hurtTimeAnim.get()) {
                val scaleHT = (entity.hurtTime.toFloat() / entity.maxHurtTime.coerceAtLeast(1).toFloat()).coerceIn(0F, 1F)
                drawHead(locationSkin,
                    1F + 15F * (scaleHT * 0.2F),
                    1F + 15F * (scaleHT * 0.2F),
                    1F - scaleHT * 0.2F,
                    33, 33,
                    1F, 0.4F + (1F - scaleHT) * 0.6F, 0.4F + (1F - scaleHT) * 0.6F)
            } else {
                drawHead(skin = locationSkin, width = 33, height = 33, alpha = 1F - targetInstance.getFadeProgress())
            }
            if (riseParticle.get()) {
                if (entity.hurtTime > entity.maxHurtTime / 2) {
                    if (!gotDamaged) {
                        for (j in 0..8)
                            particleList.add(Particle(BlendUtils.blendColors(floatArrayOf(0F, 1F), arrayOf<Color>(targetInstance.barColor, targetInstance.barColor), if (RandomUtils.nextBoolean()) RandomUtils.nextFloat(0.4F, 1.0F) else 0F), RandomUtils.nextFloat(-30F, 30F), RandomUtils.nextFloat(-30F, 30F), RandomUtils.nextFloat(0.5F, 2.5F)))

                        gotDamaged = true
                    }
                } else if (gotDamaged) {
                    gotDamaged = false
                }

                val deleteQueue = mutableListOf<Particle>()

                particleList.forEach { particle ->
                    if (particle.alpha > 0F)
                        particle.render(5F + 15F, 5 + 15F, riseParticleFade.get(), riseParticleSpeed.get())
                    else
                        deleteQueue.add(particle)
                }

                for (p in deleteQueue)
                    particleList.remove(p)
            }else if (mc.netHandler.getPlayerInfo(entity.uniqueID) == null) {
                easingHealth = 0F
                gotDamaged = false
                particleList.clear()
            }

        }

        if (!targetInstance.colorModeValue.get().equals("Rainbow")){
            if (!targetInstance.colorModeValue.get().equals("Mixer")) {
                RenderUtils.drawRect(
                    1F,
                    39f,
                    easingHealth / entity.maxHealth * (width + 2.5f) + 7F,
                    43f,
                    targetInstance.barColor.rgb
                )
            } else {
                RenderUtils.drawGradientSideways(
                    easingHealth / entity.maxHealth * (width + 2.5f) + 7.0,
                    43.0,
                    1.0,
                    39.0,
                    Color(ColorMixer.col1RedValue.get(), ColorMixer.col1GreenValue.get(), ColorMixer.col1BlueValue.get()).rgb, Color(ColorMixer.col2RedValue.get(),
                        ColorMixer.col2GreenValue.get(), ColorMixer.col2BlueValue.get()).rgb)
            }
        }else{
            for (i in 0..(gradientDistanceValue.get() -1)) {
                RenderUtils.drawGradientSideways(
                    easingHealth / entity.maxHealth * (width + 2.5f) + 7.0,
                    43.0,
                    1.0,
                    39.0,
                    LiquidSlowly(System.nanoTime(), i * gradientDistanceValue.get(), targetInstance.saturationValue.get(), targetInstance.brightnessValue.get())!!.rgb, LiquidSlowly(System.nanoTime(), -i * gradientDistanceValue.get(), targetInstance.saturationValue.get(), targetInstance.brightnessValue.get())!!.rgb)
            }
        }


        easingHealth += ((entity.health - easingHealth) / 2.0F.pow(10.0F - targetInstance.fadeSpeed.get())) * RenderUtils.deltaTime
        var x = 22
        var y = 14
        for (index in 3 downTo 0) {
            RenderUtils.drawRect(
                x.toFloat() + 15F,
                y.toFloat(),
                x.toFloat() + 33f,
                y + 18f,
                Color(30, 30, 30, 120).rgb
            )
            if (entity.inventory.armorInventory[index] != null) {
                GlStateManager.pushMatrix()
                GlStateManager.scale(0.65, 0.65, 0.65)
                Fonts.fontSFUI40.drawStringWithShadow(
                    ((entity.inventory.armorInventory[index].maxDamage - entity.inventory.armorInventory[index].itemDamage)).toString(),
                    (x.toFloat() + 19f) * 1 / 0.65f,
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
                mc.renderItem.renderItemIntoGUI(entity.inventory.armorInventory[index], x + 16, y + 1)
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
        RenderUtils.drawRect(x.toFloat() + 15, y.toFloat(), x.toFloat() + 33f, y + 18f, Color(30, 30, 30, 120).rgb)
        if (entity.inventory.mainInventory[entity.inventory.currentItem] != null) {
            if (entity.inventory.mainInventory[entity.inventory.currentItem].isItemStackDamageable) {
                GlStateManager.pushMatrix()
                GlStateManager.scale(0.65, 0.65, 0.65)
                Fonts.fontSFUI40.drawStringWithShadow(
                    ((entity.inventory.mainInventory[entity.inventory.currentItem].maxDamage - entity.inventory.mainInventory[entity.inventory.currentItem].itemDamage)).toString(),
                    (x.toFloat() + 18f) * 1 / 0.65f,
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
            mc.renderItem.renderItemIntoGUI(entity.inventory.mainInventory[entity.inventory.currentItem], x + 16, y + 1)
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
        val healthName = decimalFormat2.format(easingHealth)
        Fonts.fontSFUI35.drawString(healthName, easingHealth / entity.maxHealth * (width + 2.5f) + 5, 37F, Color(255,255,255,255).rgb)

    }
    private class Particle(var color: Color, var distX: Float, var distY: Float, var radius: Float) {
        var alpha = 0.75f
        var progress = 0.0
        fun render(x: Float, y: Float, fade: Boolean, speed: Float) {
            if (progress >= 1.0) {
                progress = 1.0
                if (fade) alpha -= 0.1F
                if (alpha < 0F) alpha = 0F
            } else
                progress += speed.toDouble()

            if (alpha <= 0F) return

            var reColored = Color(color.red / 255.0F, color.green / 255.0F, color.blue / 255.0F, alpha)
            var easeOut = EaseUtils.easeOutQuart(progress).toFloat()

            RenderUtils.drawFilledCircle(x + distX * easeOut, y + distY * easeOut, radius, reColored)
        }
    }
    override fun handleBlur(entity: EntityPlayer) {
        val width = (38 + Fonts.fontSFUI40.getStringWidth(entity.name))
            .coerceAtLeast(118)
            .toFloat()
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        RenderUtils.fastRoundedRect(-3F, -4F, width + 26F, 47F, 1F)
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }
    override fun handleShadowCut(entity: EntityPlayer) = handleBlur(entity)

    override fun handleShadow(entity: EntityPlayer) {
        val width = (38 + Fonts.fontSFUI40.getStringWidth(entity.name))
            .coerceAtLeast(118)
            .toFloat()
        RenderUtils.originalRoundedRect(-3F, -4F, width + 26F, 47F, 1F, shadowOpaque.rgb)
    }

    override fun getBorder(entity: EntityPlayer?): Border? {
        return Border(0F, 0F, 124F, 44F)
    }
}



