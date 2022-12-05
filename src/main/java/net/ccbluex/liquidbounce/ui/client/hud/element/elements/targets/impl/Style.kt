package net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.impl

import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Target
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.TargetStyle
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.TargetHudParticles
import net.ccbluex.liquidbounce.utils.extensions.skin
import net.ccbluex.liquidbounce.utils.misc.RandomUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.player.EntityPlayer
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.text.DecimalFormat
import kotlin.math.abs
import kotlin.math.pow

class Style(inst: Target): TargetStyle("Style", inst, true) {
    override fun drawTarget(entity: EntityPlayer) {
        updateAnim(entity.health)
        var index = 0
        while (index < targetInstance.particles.size) {
            val update = targetInstance.particles[index]
            if (update.animaitonA >= 255) targetInstance.particles.remove(update)
            index++
        }
        var Stringindex = 0
        while (Stringindex < targetInstance.Stringparticles.size) {
            val update = targetInstance.Stringparticles[Stringindex]
            if (update.StringanimaitonA >= 255) targetInstance.Stringparticles.remove(update)
            Stringindex++
        }
        if (entity != null) {
            if (entity != targetInstance.lastTarget || easingHealth < 0 || easingHealth > entity.maxHealth || abs(easingHealth - entity.health) < 0.01) easingHealth =
                entity.health
            val x = if (ColorUtils.stripColor(entity.displayName.unformattedText)!!.length >= 13)
                Fonts.font40.getStringWidth(ColorUtils.stripColor(entity.displayName.unformattedText)!!) + 50f else 110f
            if (entity.hurtTime > 0) {
                if (targetInstance.addTimer.hasTimePassed(500)) {
                    for (i in 0..15) {
                        val Hitparticles = TargetHudParticles()
                        Hitparticles.x = RandomUtils.nextFloat(-60f, 60f)
                        Hitparticles.y = RandomUtils.nextFloat(-50f, 50f)
                        Hitparticles.size = RandomUtils.nextFloat(1.5f, 3.0f)
                        Hitparticles.Red = RandomUtils.nextInt(30, 255)
                        Hitparticles.Green = RandomUtils.nextInt(30, 255)
                        Hitparticles.Blue = RandomUtils.nextInt(30, 255)
                        Hitparticles.A = 255f
                        targetInstance.particles.add(Hitparticles)
                    }
                    targetInstance.addTimer.reset()
                }
            }
            RenderUtils.drawRect(1f, 2f, x + 31f, 48.0f, Color(0, 0, 0, 100).rgb)
            targetInstance.particles.forEachIndexed { index, module ->
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
            GL11.glTranslated(2.5 + (entity.hurtTime * 0.1), 2.5 + (entity.hurtTime * 0.1), 0.0)
            GL11.glScaled(
                1.0 - (entity.hurtTime * 0.01),
                1.0 - (entity.hurtTime * 0.01),
                1.0 - (entity.hurtTime * 0.01)
            )
            GL11.glColor4f(1.0f, 1.0f - (entity.hurtTime * 0.05f), 1.0f - (entity.hurtTime * 0.05f), 1.0f)
            val playerInfo = mc.netHandler.getPlayerInfo(entity.uniqueID)
            if (playerInfo != null) {
                val locationSkin = entity.skin
                mc.textureManager.bindTexture(locationSkin)
                RenderUtils.drawScaledCustomSizeModalRect(2, 2, 8F, 8F, 8, 8, 33, 33, 64F, 64F)
                GL11.glColor4f(1F, 1F, 1F, 1F)
            }
            GL11.glPopMatrix()
            val DmageHealth = easingHealth > entity.health
            val HealHealth = easingHealth < entity.health
            val str = entity.displayName.unformattedText
            if (str.toByteArray().size == str.length) {
                GlStateManager.resetColor()
                Fonts.font40.drawString("Name ${entity.displayName.unformattedText}", 40, 10, -1)
            } else
                Fonts.font40.drawString(entity.displayName.unformattedText, 40, 10, -1)
            Fonts.fontSFUI35.drawString(
                "Dist ${
                    DecimalFormat("0.0").format(
                        mc.thePlayer.getDistanceToEntity(
                            entity
                        )
                    )
                } Hurt ${entity.hurtTime}", 40f, 25f, -1
            )
            GlStateManager.pushMatrix()
            GlStateManager.translate(5.0, 0.0, 0.0) // Damage animation
            if (DmageHealth) {
                RenderUtils.drawRect(
                    0F,
                    41F,
                    (easingHealth / entity.maxHealth) * x,
                    45F,
                    Color(252, 185, 65).rgb
                )
            }
            RenderUtils.drawGradientSideways(
                0.0,
                41.0,
                (entity.health / entity.maxHealth) * x.toDouble(),
                45.0,
                ColorUtils.rainbow(8000000000L).rgb,
                ColorUtils.rainbow(200000L).rgb
            )
            if (HealHealth) {
                if (targetInstance.addTimer.hasTimePassed(500)) {
                    for (i in 0..5) {
                        val stringparticles = TargetHudParticles()
                        stringparticles.Stringx = RandomUtils.nextFloat(-20f, 20f)
                        stringparticles.Stringy = RandomUtils.nextFloat(-20f, 20f)
                        stringparticles.StringA = 255f
                        targetInstance.Stringparticles.add(stringparticles)
                    }
                    targetInstance.addTimer.reset()
                }
                RenderUtils.drawRect(
                    (easingHealth / entity.maxHealth) * x,
                    44F,
                    (entity.health / entity.maxHealth) * 110,
                    47F,
                    Color(targetInstance.redValue.get(), targetInstance.greenValue.get(), targetInstance.blueValue.get()).rgb
                )
            }
            GlStateManager.popMatrix()
            easingHealth += ((entity.health - easingHealth) / 2.0F.pow(10.0F - targetInstance.fadeSpeed.get())) * RenderUtils.deltaTime
            val Hpstring = DecimalFormat("0").format((easingHealth / entity.maxHealth) * 100)
            Fonts.fontSFUI35.drawString(
                "${Hpstring}%",
                (easingHealth / entity.maxHealth) * x + Fonts.minecraftFont.drawStringWithShadow("",
                    1F,1F , 1) / 2 + 1.5f,
                40f,
                if (DmageHealth) Color(255, 111, 111).rgb else Color(255, 255, 255).rgb
            )
        }
    }

    override fun handleBlur(entity: EntityPlayer) {
        val width = (38 + Fonts.fontSFUI40.getStringWidth(entity.name))
            .coerceAtLeast(118)
            .toFloat()

        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        RenderUtils.quickDrawRect(2f, 2f, 140F, 48F)
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()

    }

    override fun handleShadowCut(entity: EntityPlayer) = handleBlur(entity)

    override fun handleShadow(entity: EntityPlayer) {
        val width = (38 + Fonts.fontSFUI40.getStringWidth(entity.name))
            .coerceAtLeast(118)
            .toFloat()

        RenderUtils.newDrawRect(2f, 2f, 140F, 48F, shadowOpaque.rgb)
    }

    override fun getBorder(entity: EntityPlayer?): Border? {
        return Border(2f, 2f, 140F, 48F)
    }
}