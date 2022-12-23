package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.Colors
import net.ccbluex.liquidbounce.utils.InventoryUtils
import net.ccbluex.liquidbounce.utils.render.BlurUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.settings.GameSettings
import net.minecraft.init.Blocks
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack
import net.minecraft.util.BlockPos
import org.lwjgl.opengl.GL11
import java.awt.Color

@ModuleInfo(name = "Eagle", description = "Makes you eagle (aka. FastBridge).", category = ModuleCategory.PLAYER)
class Eagle : Module() {
    private val counterDisplayValue = ListValue("Counter", arrayOf("Off", "Simple", "Advanced", "Sigma", "Novoline", "Exhibition"), "Simple")
    private val blurValue = BoolValue("Blur-Advanced", false) {
        counterDisplayValue.get().equals("advanced", ignoreCase = true) }
    private val blurStrength = FloatValue("Blur-Strength", 1f, 0f, 30f, "x") {
        counterDisplayValue.get().equals("advanced", ignoreCase = true) }
    private var alpha = 0f
    private var progress = 0f
    private var lastMS = 0L

    private val slot = 0
    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        val shouldEagle = mc.theWorld.getBlockState(
                BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.0, mc.thePlayer.posZ)).block === Blocks.air
        mc.gameSettings.keyBindSneak.pressed = shouldEagle
    }

    override fun onDisable() {
        if (mc.thePlayer == null)
            return

        if (!GameSettings.isKeyDown(mc.gameSettings.keyBindSneak))
            mc.gameSettings.keyBindSneak.pressed = false
    }

    @EventTarget
    fun onRender2D(event: Render2DEvent?) {
        progress = (System.currentTimeMillis() - lastMS).toFloat() / 100f
        if (progress >= 1) progress = 1f
        val counterMode = counterDisplayValue.get()
        val scaledResolution = ScaledResolution(mc)
        val info: String = getBlocksAmount().toString() + " blocks"
        val infoWidth = Fonts.fontSFUI40.getStringWidth(info)
        val info3 = "" + getBlocksAmount()
        val infoWidth2 = Fonts.minecraftFont.getStringWidth(getBlocksAmount().toString() + "")
        if (counterMode.equals("advanced", ignoreCase = true)) {
            val canRenderStack =
                slot >= 0 && slot < 9 && mc.thePlayer.inventory.mainInventory[slot] != null && mc.thePlayer.inventory.mainInventory[slot].item != null && mc.thePlayer.inventory.mainInventory[slot].item is ItemBlock
            if (blurValue.get()) BlurUtils.blurArea(
                (scaledResolution.scaledWidth / 2 - infoWidth / 2 - 4).toFloat(),
                (scaledResolution.scaledHeight / 2 - 39).toFloat(),
                (scaledResolution.scaledWidth / 2 + infoWidth / 2 + 4).toFloat(),
                (scaledResolution.scaledHeight / 2 - if (canRenderStack) 5 else 26).toFloat(),
                blurStrength.get()
            )
            RenderUtils.drawRect(
                (scaledResolution.scaledWidth / 2 - infoWidth / 2 - 4).toFloat(),
                (scaledResolution.scaledHeight / 2 - 40).toFloat(),
                (scaledResolution.scaledWidth / 2 + infoWidth / 2 + 4).toFloat(),
                (scaledResolution.scaledHeight / 2 - 39).toFloat(),
                if (getBlocksAmount() > 1) -0x1 else -0xeff0
            )
            RenderUtils.drawRect(
                (scaledResolution.scaledWidth / 2 - infoWidth / 2 - 4).toFloat(),
                (scaledResolution.scaledHeight / 2 - 39).toFloat(),
                (scaledResolution.scaledWidth / 2 + infoWidth / 2 + 4).toFloat(),
                (scaledResolution.scaledHeight / 2 - 26).toFloat(),
                -0x60000000
            )
            if (canRenderStack) {
                RenderUtils.drawRect(
                    (scaledResolution.scaledWidth / 2 - infoWidth / 2 - 4).toFloat(),
                    (scaledResolution.scaledHeight / 2 - 26).toFloat(),
                    (scaledResolution.scaledWidth / 2 + infoWidth / 2 + 4).toFloat(),
                    (scaledResolution.scaledHeight / 2 - 5).toFloat(),
                    -0x60000000
                )
                GlStateManager.pushMatrix()
                GlStateManager.translate(
                    (scaledResolution.scaledWidth / 2 - 8).toFloat(),
                    (scaledResolution.scaledHeight / 2 - 25).toFloat(),
                    (scaledResolution.scaledWidth / 2 - 8).toFloat()
                )
                renderItemStack(mc.thePlayer.inventory.mainInventory[slot], 0, 0)
                GlStateManager.popMatrix()
            }
            GlStateManager.resetColor()
            Fonts.fontSFUI40.drawCenteredString(
                info,
                (scaledResolution.scaledWidth / 2).toFloat(),
                (scaledResolution.scaledHeight / 2 - 36).toFloat(),
                -1
            )
        }
        if (counterMode.equals("sigma", ignoreCase = true)) {
            GlStateManager.translate(0f, -14f - progress * 4f, 0f)
            //GL11.glPushMatrix();
            GL11.glEnable(GL11.GL_BLEND)
            GL11.glDisable(GL11.GL_TEXTURE_2D)
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
            GL11.glEnable(GL11.GL_LINE_SMOOTH)
            GL11.glColor4f(0.15f, 0.15f, 0.15f, progress)
            GL11.glBegin(GL11.GL_TRIANGLE_FAN)
            GL11.glVertex2d(
                (scaledResolution.scaledWidth / 2 - 3).toDouble(),
                (scaledResolution.scaledHeight - 60).toDouble()
            )
            GL11.glVertex2d(
                (scaledResolution.scaledWidth / 2).toDouble(),
                (scaledResolution.scaledHeight - 57).toDouble()
            )
            GL11.glVertex2d(
                (scaledResolution.scaledWidth / 2 + 3).toDouble(),
                (scaledResolution.scaledHeight - 60).toDouble()
            )
            GL11.glEnd()
            GL11.glEnable(GL11.GL_TEXTURE_2D)
            GL11.glDisable(GL11.GL_BLEND)
            GL11.glDisable(GL11.GL_LINE_SMOOTH)
            //GL11.glPopMatrix();
            RenderUtils.drawRoundedRect(
                (scaledResolution.scaledWidth / 2 - infoWidth / 2 - 4).toFloat(),
                (scaledResolution.scaledHeight - 60).toFloat(),
                (scaledResolution.scaledWidth / 2 + infoWidth / 2 + 4).toFloat(),
                (scaledResolution.scaledHeight - 74).toFloat(),
                2f,
                Color(0.15f, 0.15f, 0.15f, progress).rgb
            )
            GlStateManager.resetColor()
            Fonts.fontSFUI35.drawCenteredString(
                info,
                scaledResolution.scaledWidth / 2 + 0.1f,
                (scaledResolution.scaledHeight - 70).toFloat(),
                Color(1f, 1f, 1f, 0.8f * progress).rgb,
                false
            )
            GlStateManager.translate(0f, 14f + progress * 4f, 0f)
        }
        if (counterMode.equals("novoline", ignoreCase = true)) {
            if (slot >= 0 && slot < 9 && mc.thePlayer.inventory.mainInventory[slot] != null && mc.thePlayer.inventory.mainInventory[slot].item != null && mc.thePlayer.inventory.mainInventory[slot].item is ItemBlock) {
                GlStateManager.pushMatrix()
                GlStateManager.translate(
                    (scaledResolution.scaledWidth / 2 - 22).toFloat(),
                    (scaledResolution.scaledHeight / 2 + 16).toFloat(),
                    (scaledResolution.scaledWidth / 2 - 22).toFloat()
                )
                renderItemStack(mc.thePlayer.inventory.mainInventory[slot], 0, 0)
                GlStateManager.popMatrix()
            }
            GlStateManager.resetColor()
            Fonts.minecraftFont.drawString(
                getBlocksAmount().toString() + " blocks",
                (scaledResolution.scaledWidth / 2).toFloat(),
                (scaledResolution.scaledHeight / 2 + 20).toFloat(),
                -1,
                true
            )
        }
        if (counterMode.equals("simple", ignoreCase = true)) {
            val eagle = LiquidBounce.moduleManager[Eagle::class.java]
            val delta = RenderUtils.deltaTime.toFloat()
            if (eagle!!.state) {
                alpha += 2 * delta
                if (alpha >= 250) alpha = 250f
            } else {
                alpha -= 2 * delta
                if (alpha <= 30) alpha = 0f
            }
            if (alpha > 1) {
                GlStateManager.pushMatrix()
                mc.fontRendererObj.drawString(
                    info3,
                    scaledResolution.scaledWidth / 2 + 10 - 1,
                    scaledResolution.scaledHeight / 2,
                    RenderUtils.reAlpha(
                        Color.BLACK.rgb, alpha / 255
                    )
                )
                mc.fontRendererObj.drawString(
                    info3,
                    scaledResolution.scaledWidth / 2 + 10 + 1,
                    scaledResolution.scaledHeight / 2,
                    RenderUtils.reAlpha(
                        Color.BLACK.rgb, alpha / 255
                    )
                )
                mc.fontRendererObj.drawString(
                    info3,
                    scaledResolution.scaledWidth / 2 + 10,
                    scaledResolution.scaledHeight / 2 - 1,
                    RenderUtils.reAlpha(
                        Color.BLACK.rgb, alpha / 255
                    )
                )
                mc.fontRendererObj.drawString(
                    info3,
                    scaledResolution.scaledWidth / 2 + 10,
                    scaledResolution.scaledHeight / 2 + 1,
                    RenderUtils.reAlpha(
                        Color.BLACK.rgb, alpha / 255
                    )
                )
                mc.fontRendererObj.drawString(
                    info3,
                    scaledResolution.scaledWidth / 2 + 10,
                    scaledResolution.scaledHeight / 2,
                    RenderUtils.reAlpha(
                        getBlockColor(getBlocksAmount()
                        ), alpha / 255
                    )
                )
                mc.fontRendererObj.drawString(
                    info3, scaledResolution.scaledWidth / 2 + 10, scaledResolution.scaledHeight / 2, getBlockColor(
                        getBlocksAmount()
                    )
                )
                GlStateManager.popMatrix()
            }
        }
        if (counterMode.equals("exhibition", ignoreCase = true)) {
            var c = Colors.getColor(255, 0, 0, 150)
            if (getBlocksAmount() >= 64 && 128 > getBlocksAmount()) {
                c = Colors.getColor(255, 255, 0, 150)
            } else if (getBlocksAmount() >= 128) {
                c = Colors.getColor(0, 255, 0, 150)
            }
            Fonts.minecraftFont.drawString(
                getBlocksAmount().toString() + "",
                (scaledResolution.scaledWidth / 2 - infoWidth2 / 2 - 1).toFloat(),
                (scaledResolution.scaledHeight / 2 - 36).toFloat(),
                -0x1000000,
                false
            )
            Fonts.minecraftFont.drawString(
                getBlocksAmount().toString() + "",
                (scaledResolution.scaledWidth / 2 - infoWidth2 / 2 + 1).toFloat(),
                (scaledResolution.scaledHeight / 2 - 36).toFloat(),
                -0x1000000,
                false
            )
            Fonts.minecraftFont.drawString(
                getBlocksAmount().toString() + "",
                (scaledResolution.scaledWidth / 2 - infoWidth2 / 2).toFloat(),
                (scaledResolution.scaledHeight / 2 - 35).toFloat(),
                -0x1000000,
                false
            )
            Fonts.minecraftFont.drawString(
                getBlocksAmount().toString() + "",
                (scaledResolution.scaledWidth / 2 - infoWidth2 / 2).toFloat(),
                (scaledResolution.scaledHeight / 2 - 37).toFloat(),
                -0x1000000,
                false
            )
            Fonts.minecraftFont.drawString(
                getBlocksAmount().toString() + "",
                (scaledResolution.scaledWidth / 2 - infoWidth2 / 2).toFloat(),
                (scaledResolution.scaledHeight / 2 - 36).toFloat(),
                c,
                false
            )
        }
    }


    private fun getBlockColor(count: Int): Int {
        val f = count.toFloat()
        val f1 = 64f
        val f2 = Math.max(0.0f, Math.min(f, f1) / f1)
        return Color.HSBtoRGB(f2 / 3.0f, 1.0f, 1.0f) or -0x1000000
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
    private fun getBlocksAmount(): Int {
        var amount = 0
        for (i in 36..44) {
            val itemStack = mc.thePlayer.inventoryContainer.getSlot(i).stack
            if (itemStack != null && itemStack.item is ItemBlock) {
                val block = (itemStack.item as ItemBlock).getBlock()
                if (!InventoryUtils.BLOCK_BLACKLIST.contains(block) && block.isFullCube) amount += itemStack.stackSize
            }
        }
        return amount
    }
}
