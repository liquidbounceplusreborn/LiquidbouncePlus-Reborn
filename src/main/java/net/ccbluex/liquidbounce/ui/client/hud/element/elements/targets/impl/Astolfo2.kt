/*
 * LiquidBounce++ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/PlusPlusMC/LiquidBouncePlusPlus/
 */
package net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.impl

import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Target
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.TargetStyle
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawEntityOnScreen
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import org.lwjgl.opengl.GL11
import kotlin.math.roundToInt

class Astolfo2(inst: Target): TargetStyle("Astolfo2", inst, true) {

    override fun drawTarget(entity: EntityPlayer) {
        val font = Fonts.minecraftFont

        updateAnim(entity.health)

        RenderUtils.drawRect(0F, 0F, 130F, 52F, targetInstance.bgColor.rgb)

        font.drawStringWithShadow(entity.name, 37F, 3F, getColor(-1).rgb)

        GL11.glPushMatrix()
        GL11.glScalef(2F,2F,2F)
        font.drawString("${getHealth2(entity).roundToInt()} ❤", 19,9, targetInstance.barColor.rgb)
        GL11.glPopMatrix()

        GlStateManager.resetColor()
        drawEntityOnScreen(19,40,20, entity)

        //HP
        RenderUtils.drawRect(2F, 41F, (easingHealth / entity.maxHealth).coerceIn(0F, entity.maxHealth) * (128F), 49F, targetInstance.barColor.rgb)
    }

    override fun handleBlur(entity: EntityPlayer) {

        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        RenderUtils.quickDrawRect(0F, 0F, 130f, 45F)
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }

    override fun handleShadowCut(entity: EntityPlayer) = handleBlur(entity)

    override fun handleShadow(entity: EntityPlayer) {
        val font = Fonts.minecraftFont
        val healthString = "${decimalFormat2.format(entity.health)} ❤"
        val length = 60.coerceAtLeast(font.getStringWidth(entity.name)).coerceAtLeast(font.getStringWidth(healthString)).toFloat() + 10F

        RenderUtils.newDrawRect(0F, 0F, 130F + length, 45F, shadowOpaque.rgb)
    }

    override fun getBorder(entity: EntityPlayer?): Border? {
        entity ?: return Border(0F, 0F, 102F, 36F)
        return Border(0F, 0F, 130F, 45F)
    }

    private fun getHealth2(entity: EntityLivingBase?):Float{
        return if(entity==null || entity.isDead){ 0f }else{ entity.health }
    }

}
