package net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.impl

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Target
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.TargetStyle
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.RegexUtils
import net.ccbluex.liquidbounce.utils.render.BlendUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.util.ArrayList

class OldExhibition(inst: Target): TargetStyle("OldExhibition", inst, false) {
    override fun drawTarget(entity: EntityPlayer) {
        updateAnim(entity.health)
        GlStateManager.pushMatrix()

        val width = (38 + Fonts.minecraftFont.getStringWidth(entity.name))
            .coerceAtLeast(118)
            .toFloat()


        // Draws the skeet rectangles.
        RenderUtils.drawRect(0F, -1F, width + 5, 38F, Color(0,0,0, 130).rgb)
//        RenderUtils.skeetRectSmall(0.0, -2.0, 124.0, 38.0, 1.0)

        // Draws name.
        Fonts.minecraftFont.drawString(entity.name, 42.3f.toInt(), 0.3f.toInt(), -1)

        // Gets health.
        val health = entity.health

        // Gets health and absorption
        val healthWithAbsorption = entity.health + entity.absorptionAmount

        // Color stuff for the healthBar.
        val fractions = floatArrayOf(0.0f, 0.5f, 1.0f)
        val colors = arrayOf(Color.RED, Color.YELLOW, Color.GREEN)

        // Max health.
        val progress = health / entity.maxHealth

        // Color.
        val healthColor = if (health >= 0.0f) BlendUtils.blendColors(fractions, colors, progress).brighter() else Color.RED

        // Round.
        var cockWidth = 0.0
        cockWidth = RegexUtils.round(cockWidth, 5.0.toInt())
        if (cockWidth < 50.0) {
            cockWidth = 50.0
        }

        // Healthbar + absorption
        val healthBarPos = cockWidth * progress.toDouble()
        RenderUtils.rectangle(42.5, 10.3, 103.0, 13.5, healthColor.darker().darker().darker().darker().rgb)
        RenderUtils.rectangle(42.5, 10.3, 53.0 + healthBarPos + 0.5, 13.5, healthColor.rgb)
        if (entity.absorptionAmount > 0.0f) {
            RenderUtils.rectangle(97.5 - entity.absorptionAmount.toDouble(), 10.3, 103.5, 13.5, Color(137, 112, 9).rgb)
        }
        // Draws rect around health bar.
        RenderUtils.rectangleBordered(42.0, 9.8, 54.0 + cockWidth, 14.0, 0.5, 0, Color.BLACK.rgb)

        // Draws the lines between the healthbar to make it look like boxes.
        for (dist in 1..9) {
            val cock = cockWidth / 8.5 * dist.toDouble()
            RenderUtils.rectangle(43.5 + cock, 9.8, 43.5 + cock + 0.5, 14.0, Color.BLACK.rgb)
        }

        // Draw targets hp number and distance number.
        GlStateManager.scale(0.5, 0.5, 0.5)
        val distance = mc.thePlayer.getDistanceToEntity(entity).toInt()
        val nice = "HP: " + healthWithAbsorption.toInt() + " | Dist: " + distance
        Fonts.minecraftFont.drawString(nice, 85.3f, 32.3f, -1, true)
        GlStateManager.scale(2.0, 2.0, 2.0)
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
        GlStateManager.enableAlpha()
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        // Draw targets armor and tools and weapons and shows the enchants.
        if (entity != null) drawEquippedShit(28, 20)
        GlStateManager.disableAlpha()
        GlStateManager.disableBlend()
        // Draws targets model.
        GlStateManager.scale(0.31, 0.31, 0.31)
        GlStateManager.translate(73.0f, 102.0f, 40.0f)
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
        RenderUtils.drawModel(entity.rotationYaw, entity.rotationPitch, entity as EntityLivingBase?)
        GlStateManager.popMatrix()
    }

    private fun drawEquippedShit(x: Int, y: Int) {
        var target = (LiquidBounce.moduleManager[KillAura::class.java] as KillAura).target
        if (target == null || target !is EntityPlayer) return
        GL11.glPushMatrix()
        val stuff: MutableList<ItemStack> = ArrayList()
        var cock = -2
        for (geraltOfNigeria in 3 downTo 0) {
            val armor = (target as EntityPlayer).getCurrentArmor(geraltOfNigeria)
            if (armor != null) {
                stuff.add(armor)
            }
        }
        if ((target as EntityPlayer).heldItem != null) {
            stuff.add((target as EntityPlayer).heldItem)
        }
        for (yes in stuff) {
            if (Minecraft.getMinecraft().theWorld != null) {
                RenderHelper.enableGUIStandardItemLighting()
                cock += 16
            }
            GlStateManager.pushMatrix()
            GlStateManager.disableAlpha()
            GlStateManager.clear(256)
            GlStateManager.enableBlend()
            Minecraft.getMinecraft().renderItem.renderItemIntoGUI(yes, cock + x, y)
            RenderUtils.renderEnchantText(yes, cock + x, y + 0.5f)
            GlStateManager.disableBlend()
            GlStateManager.scale(0.5, 0.5, 0.5)
            GlStateManager.disableDepth()
            GlStateManager.disableLighting()
            GlStateManager.enableDepth()
            GlStateManager.scale(2.0f, 2.0f, 2.0f)
            GlStateManager.enableAlpha()
            GlStateManager.popMatrix()
            yes.enchantmentTagList
        }
        GL11.glPopMatrix()
    }

    override fun getBorder(entity: EntityPlayer?): Border? {
        val width = (38 + Fonts.minecraftFont.getStringWidth(entity?.name))
            .coerceAtLeast(118)
            .toFloat()
        return Border(0F, -1F, width + 5, 38F)
    }
}