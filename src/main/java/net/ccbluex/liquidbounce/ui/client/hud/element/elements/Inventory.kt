/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack
import org.lwjgl.opengl.GL11
import java.awt.Color

/**
 * CustomHUD Armor element
 *
 * Shows a horizontal display of current armor
 */
@ElementInfo(name = "Inventory")
class Inventory(x: Double = 10.0, y: Double = 10.0, scale: Float = 1F) : Element(x, y, scale) {
    private var inventoryRows = 0
    private val lowerInv: IInventory? = null
    private val Mode = ListValue("Background-Mode", arrayOf("Bordered", "Rounded"), "Bordered")
    private val width = IntegerValue("BorderWidth", 1, 0, 10)
    private val redValue = IntegerValue("Red", 0, 0, 255)
    private val greenValue = IntegerValue("Green", 0, 0, 255)
    private val blueValue = IntegerValue("Blue", 0, 0, 255)
    private val alpha = IntegerValue("Alpha", 120, 0, 255)
    private val bordredValue = IntegerValue("BorderRed", 255, 0, 255)
    private val bordgreenValue = IntegerValue("BorderGreen", 255, 0, 255)
    private val bordblueValue = IntegerValue("BorderBlue", 255, 0, 255)
    private val bordalpha = IntegerValue("BorderAlpha", 255, 0, 255)
    private val bordRad = FloatValue("BorderRadius", 3F, 0F, 10F)

    /**
     * Draw element
     */
    override fun drawElement(): Border {
        if (Mode.get() == "Rounded") {
            RenderUtils.drawRoundedRect(0F, this.inventoryRows * 18F + 17F, 176F, 96F, bordRad.get(), Color(redValue.get(), greenValue.get(), blueValue.get(), alpha.get()).rgb)
        }
        if (Mode.get() == "Bordered") {
            RenderUtils.drawBorderedRect(0F, this.inventoryRows * 18F + 17F, 176F, 96F, width.get().toFloat(), Color(bordredValue.get(), bordgreenValue.get(), bordblueValue.get(), bordalpha.get()).rgb, Color(redValue.get(), greenValue.get(), blueValue.get(), alpha.get()).rgb)
        }
        if (lowerInv != null) {
            this.inventoryRows = lowerInv.getSizeInventory()
        }
        renderInventory1(mc.thePlayer)
        renderInventory2(mc.thePlayer)
        renderInventory3(mc.thePlayer)
        return Border(0F, this.inventoryRows * 18F + 17F, 176F, 96F)
    }

    private fun renderInventory1(player: EntityPlayer) {
        var armourStack: ItemStack?
        var renderStack = player.inventory.mainInventory
        var xOffset = 8
        renderStack = player.inventory.mainInventory
        for (index in 9..17) {
            armourStack = renderStack[index]
            if (armourStack != null) this.renderItemStack(armourStack, xOffset, 30)
            xOffset += 18
        }
    }

    private fun renderInventory2(player: EntityPlayer) {
        var armourStack: ItemStack?
        var renderStack = player.inventory.mainInventory
        var xOffset = 8
        renderStack = player.inventory.mainInventory
        for (index in 18..26) {
            armourStack = renderStack[index]
            if (armourStack != null) this.renderItemStack(armourStack, xOffset, 48)
            xOffset += 18
        }
    }

    private fun renderInventory3(player: EntityPlayer) {
        var armourStack: ItemStack?
        var renderStack = player.inventory.mainInventory
        var xOffset = 8
        renderStack = player.inventory.mainInventory
        for (index in 27..35) {
            armourStack = renderStack[index]
            if (armourStack != null) this.renderItemStack(armourStack, xOffset, 66)
            xOffset += 18
        }
    }

    private fun renderItemStack(stack: ItemStack, x: Int, y: Int) {
        GlStateManager.pushMatrix()
        GlStateManager.enableRescaleNormal()
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        RenderHelper.enableGUIStandardItemLighting()
        mc.renderItem.renderItemAndEffectIntoGUI(stack, x, y)
        mc.renderItem.renderItemOverlays(mc.fontRendererObj, stack, x, y)
        RenderHelper.disableStandardItemLighting()
        GlStateManager.disableRescaleNormal()
        GlStateManager.disableBlend()
        GlStateManager.popMatrix()
    }
}