package net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.impl

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Target
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.TargetStyle
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.Colors
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.client.gui.GuiChat
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import org.lwjgl.opengl.GL11
import java.awt.Color


class Tifality(inst: Target): TargetStyle("Tifality", inst, true) {
    override fun drawTarget(entity: EntityPlayer) {
        val killAura = LiquidBounce.moduleManager.getModule(KillAura::class.java)
        val scaledRes = ScaledResolution(mc)
        val width = scaledRes.scaledWidth.toFloat()
        val height = scaledRes.scaledHeight.toFloat()
        if (entity != null) {
            GlStateManager.pushMatrix()
            RenderUtils.targetHudRect(
                0.0,
                -2.0,
                if (Fonts.fontTahoma.getStringWidth(entity.name) > 70.0f) 124.0f +Fonts.fontTahoma.getStringWidth(entity.name).toDouble() - 70.0f else 124.0,
                38.0,
                1.0
            )
            RenderUtils.targetHudRect1(0.0, -2.0, 124.0, 38.0, 1.0)
            Fonts.fontTahoma.drawString(entity.name, 42.0f, 0.5f, -1, true)
            val health = entity.health
            val totalHealth = entity.health + entity.absorptionAmount
            val fractions = floatArrayOf(0.0f, 0.5f, 1.0f)
            val colors: Array<Color> = arrayOf<Color>(Color.RED, Color.YELLOW, Color.GREEN)
            val progress = health / entity.maxHealth
            val customColor: Color =
                if (health >= 0.0f) Colors.blendColors(fractions, colors, progress).brighter() else Color.RED
            var width1 = 0.0
            width1 = Colors.getIncremental(width1, 5.0)
            if (width1 < 50.0) width1 = 50.0
            val healthLocation = width1 * progress
            RenderUtils.rectangle(42.5, 10.3, 53.0 + healthLocation + 0.5, 13.5, customColor.getRGB())
            if (entity.absorptionAmount > 0.0f) RenderUtils.rectangle(
                97.5 - entity.absorptionAmount,
                10.3,
                103.5,
                13.5,
                Color(137, 112, 9).getRGB()
            )
            RenderUtils.drawRectBordered(
                42.0,
                9.800000190734863,
                54.0 + width1,
                14.0,
                0.5,
                Colors.getColor(0, 0),
                Colors.getColor(0)
            )
            for (dist in 1..9) {
                val dThing = width1 / 8.5 * dist
                RenderUtils.rectangle(43.5 + dThing, 9.8, 43.5 + dThing + 0.5, 14.0, Colors.getColor(0))
            }
            val var18 = mc.thePlayer.getDistanceToEntity(entity as Entity).toInt()
            val str = "HP: " + totalHealth.toInt() + " | Dist: " + var18
            Fonts.fontTahomaSmall.drawString(str, 42.6f, 15.0f, -1)
            GlStateManager.scale(0.5, 0.5, 0.5)
            GlStateManager.scale(2.0, 2.0, 2.0)
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
            GlStateManager.enableAlpha()
            GlStateManager.enableBlend()
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
            if (entity is EntityPlayer) drawArmor(entity,28, 19)
            GlStateManager.scale(0.31, 0.31, 0.31)
            GlStateManager.translate(73.0f, 102.0f, 40.0f)
            model(entity.rotationYaw, entity.rotationPitch, entity)
            GlStateManager.popMatrix()
        }
    }

    override fun getBorder(entity: EntityPlayer?): Border? {
        entity ?: return Border(0f, 0f, 124F, 0F)

        val font = Fonts.fontTahoma
        val minWidth = 124F.coerceAtLeast(31F + font.getStringWidth(entity.name))

        return Border(0f, 40f, minWidth, -5F)
    }

    private fun model(yaw: Float, pitch: Float, entityLivingBase: EntityLivingBase) {
        GlStateManager.resetColor()
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
        GlStateManager.enableColorMaterial()
        GlStateManager.pushMatrix()
        GlStateManager.translate(0.0f, 0.0f, 50.0f)
        GlStateManager.scale(-50.0f, 50.0f, 50.0f)
        GlStateManager.rotate(180.0f, 0.0f, 0.0f, 1.0f)
        val renderYawOffset = entityLivingBase.renderYawOffset
        val rotationYaw = entityLivingBase.rotationYaw
        val rotationPitch = entityLivingBase.rotationPitch
        val prevRotationYawHead = entityLivingBase.prevRotationYawHead
        val rotationYawHead = entityLivingBase.rotationYawHead
        GlStateManager.rotate(135.0f, 0.0f, 1.0f, 0.0f)
        RenderHelper.enableStandardItemLighting()
        GlStateManager.rotate(-135.0f, 0.0f, 1.0f, 0.0f)
        GlStateManager.rotate((-Math.atan((pitch / 40.0f).toDouble()) * 20.0).toFloat(), 1.0f, 0.0f, 0.0f)
        entityLivingBase.renderYawOffset = yaw - yaw / yaw * 0.4f
        entityLivingBase.rotationYaw = yaw - yaw / yaw * 0.2f
        entityLivingBase.rotationPitch = pitch
        entityLivingBase.rotationYawHead = entityLivingBase.rotationYaw
        entityLivingBase.prevRotationYawHead = entityLivingBase.rotationYaw
        GlStateManager.translate(0.0f, 0.0f, 0.0f)
        val renderManager = mc.renderManager
        renderManager.setPlayerViewY(180.0f)
        renderManager.isRenderShadow = false
        renderManager.renderEntityWithPosYaw(entityLivingBase as Entity, 0.0, 0.0, 0.0, 0.0f, 1.0f)
        renderManager.isRenderShadow = true
        entityLivingBase.renderYawOffset = renderYawOffset
        entityLivingBase.rotationYaw = rotationYaw
        entityLivingBase.rotationPitch = rotationPitch
        entityLivingBase.prevRotationYawHead = prevRotationYawHead
        entityLivingBase.rotationYawHead = rotationYawHead
        GlStateManager.popMatrix()
        RenderHelper.disableStandardItemLighting()
        GlStateManager.disableRescaleNormal()
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit)
        GlStateManager.disableTexture2D()
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit)
        GlStateManager.resetColor()
    }

    private fun drawArmor(entity: EntityPlayer?, x: Int, y: Int) {
        GL11.glPushMatrix()
        val stuff: MutableList<ItemStack> = ArrayList()
        var split = -3
        for (index in 3 downTo 0) {
            val armer = entity!!.inventory.armorInventory[index]
            if (armer != null) stuff.add(armer)
        }
        if (entity!!.currentEquippedItem != null) stuff.add(entity!!.currentEquippedItem)
        for (everything in stuff) {
            if (mc.theWorld != null) {
                RenderHelper.enableGUIStandardItemLighting()
                split += 16
            }
            GlStateManager.pushMatrix()
            GlStateManager.disableAlpha()
            GlStateManager.clear(256)
            GlStateManager.enableBlend()
            mc.renderItem.zLevel = -150.0f
            mc.renderItem.renderItemIntoGUI(everything, split + x, y)
            mc.renderItem.renderItemOverlays(mc.fontRendererObj, everything, split + x, y)
            RenderUtils.drawExhiEnchants(everything, (split + x).toFloat(), y.toFloat())
            mc.renderItem.zLevel = 0.0f
            GlStateManager.disableBlend()
            GlStateManager.scale(0.5, 0.5, 0.5)
            GlStateManager.disableDepth()
            GlStateManager.disableLighting()
            GlStateManager.enableDepth()
            GlStateManager.scale(2.0f, 2.0f, 2.0f)
            GlStateManager.enableAlpha()
            GlStateManager.popMatrix()
            everything.enchantmentTagList
        }
        GL11.glPopMatrix()
    }
}
