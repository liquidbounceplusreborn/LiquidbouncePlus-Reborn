package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.world.AntiBot
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.ui.font.GameFontRenderer
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.utils.GLUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.*
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager.*
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Items
import net.minecraft.item.*
import net.minecraft.potion.Potion
import net.minecraft.potion.PotionEffect
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import kotlin.math.roundToInt


@ModuleInfo(name = "NameTags", spacedName = "Name Tags", description = "Changes the scale of the nametags so you can always read them.", category = ModuleCategory.RENDER)
class NameTags : Module() {

    val typeValue = ListValue("Mode", arrayOf("3DTag", "2DTag"), "3DTag")

    //2DTags
    private val armorValue = BoolValue("Armor", true)
    private val healthValue = BoolValue("Health", true)
    private val distanceValue = BoolValue("Distance", true)
    private val outlineValue = BoolValue("Outline", true)
    private val background = BoolValue("Background", true, { !typeValue.get().equals("3dtag", true) })
    private val scaleValue = FloatValue("Scale", 1.0F, 0.5F, 2.0F)
    //3DTags
    private val healthBarValue = BoolValue("Bar", true, { !typeValue.get().equals("2dtag", true) })
    private val pingValue = BoolValue("Ping", true, { !typeValue.get().equals("2dtag", true) })
    private val translateY = FloatValue("TranslateY", 0.55F, -2F, 2F, { !typeValue.get().equals("2dtag", true) })
    private val enchantValue = BoolValue("Enchant", false) { armorValue.get() && !typeValue.get().equals("2dtag", true)}
    private val potionValue = BoolValue("Potions", true, { !typeValue.get().equals("2dtag", true) })
    private val clearNamesValue = BoolValue("ClearNames", false, { !typeValue.get().equals("2dtag", true) })
    private val fontValue = FontValue("Font", Fonts.font40, { !typeValue.get().equals("2dtag", true) })
    private val fontShadowValue = BoolValue("Shadow", true, { !typeValue.get().equals("2dtag", true) })
    private val borderValue = BoolValue("Border", true, { !typeValue.get().equals("2dtag", true) })
    val localValue = BoolValue("LocalPlayer", true, { !typeValue.get().equals("2dtag", true) })
    val nfpValue = BoolValue("NoFirstPerson", true, { localValue.get() && !typeValue.get().equals("2dtag", true)})
    private val backgroundColorRedValue = IntegerValue("Background-R", 0, 0, 255, { !typeValue.get().equals("2dtag", true) })
    private val backgroundColorGreenValue = IntegerValue("Background-G", 0, 0, 255, { !typeValue.get().equals("2dtag", true) })
    private val backgroundColorBlueValue = IntegerValue("Background-B", 0, 0, 255, { !typeValue.get().equals("2dtag", true) })
    private val backgroundColorAlphaValue = IntegerValue("Background-Alpha", 0, 0, 255, { !typeValue.get().equals("2dtag", true) })
    private val borderColorRedValue = IntegerValue("Border-R", 0, 0, 255, { !typeValue.get().equals("2dtag", true) })
    private val borderColorGreenValue = IntegerValue("Border-G", 0, 0, 255, { !typeValue.get().equals("2dtag", true) })
    private val borderColorBlueValue = IntegerValue("Border-B", 0, 0, 255, { !typeValue.get().equals("2dtag", true) })
    private val borderColorAlphaValue = IntegerValue("Border-Alpha", 0, 0, 255, { !typeValue.get().equals("2dtag", true) })

    private val inventoryBackground = ResourceLocation("textures/gui/container/inventory.png")
    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        when (typeValue.get().toLowerCase()) {
            "2dtag" -> {
                for (o in mc.theWorld.playerEntities) {
                    val e = o as EntityLivingBase
                    if (e.isEntityAlive && e !== mc.theWorld.playerEntities) {
                        if (!EntityUtils.isSelected(e, false))
                            continue
                        val renderManager = mc.renderManager
                        val pX: Double = e.lastTickPosX + (e.posX - e.lastTickPosX) * mc.timer.renderPartialTicks - renderManager.renderPosX
                        val pY: Double = e.lastTickPosY + (e.posY - e.lastTickPosY) * mc.timer.renderPartialTicks - renderManager.renderPosY
                        val pZ: Double = e.lastTickPosZ + (e.posZ - e.lastTickPosZ) * mc.timer.renderPartialTicks - renderManager.renderPosZ
                        renderNameTag(e, e.name, pX, pY, pZ)
                    }
                }
            }
            "3dtag" -> {
                glPushAttrib(GL_ENABLE_BIT)
                glPushMatrix()

                // Disable lightning and depth test
                glDisable(GL_LIGHTING)
                glDisable(GL_DEPTH_TEST)

                glEnable(GL_LINE_SMOOTH)

                // Enable blend
                glEnable(GL_BLEND)
                glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

                for (entity in mc.theWorld.loadedEntityList) {
                    if (!EntityUtils.isSelected(entity, false) && (!localValue.get() || entity != mc.thePlayer || (nfpValue.get() && mc.gameSettings.thirdPersonView == 0)))
                        continue

                    renderNameTags(entity as EntityLivingBase,
                            if (clearNamesValue.get())
                                ColorUtils.stripColor(entity.getDisplayName().unformattedText) ?: continue
                            else
                                entity.getDisplayName().unformattedText
                    )
                }

                glPopMatrix()
                glPopAttrib()

                // Reset color
                resetColor()
                glColor4f(1F, 1F, 1F, 1F)
            }
        }
    }

    private fun renderNameTag(entity: EntityLivingBase, tag: String, pX: Double, pY: Double, pZ: Double) {
        when (typeValue.get().toLowerCase()) {
            "2dtag" -> {
        var tag = tag
        var pY = pY
        val sr = ScaledResolution(Minecraft.getMinecraft())
        val fr: FontRenderer = mc.fontRendererObj
        var size = mc.thePlayer.getDistanceToEntity(entity) / 2.5f
        if (size < 4.0f) {
            size = 4.0f
        }
        pY += if (entity.isSneaking) 0.45 else 0.6
        var scale = (size * scaleValue.get())
        scale /= 200f
        tag = entity.displayName.formattedText

        var bot = ""
        bot = if (AntiBot.isBot(entity)) {
            "\u00a77[Bot] "
        } else {
            ""
        }

        val healthText = if (healthValue.get()) " §a" + entity.health.toInt() + "" else ""
        val distanceText = if (distanceValue.get()) "§a[§f" + mc.thePlayer.getDistanceToEntity(entity).toInt() + "§a] " else ""
        val HEALTH: Int = entity.health.toInt()
        val COLOR1: String
        COLOR1 = if (HEALTH > 20.0) {
            "\u00a79"
        } else if (HEALTH >= 11.0) {
            "\u00a7a"
        } else if (HEALTH >= 4.0) {
            "\u00a7e"
        } else {
            "\u00a74"
        }
        var hp = " [$COLOR1$HEALTH §c❤§f]"
        glPushMatrix()
        glTranslatef(pX.toFloat(), pY.toFloat() + 1.4f, pZ.toFloat())
        glNormal3f(0.0f, 1.0f, 0.0f)
        val renderManager = mc.renderManager
        glRotatef(-renderManager.playerViewY, 0.0f, 1.0f, 0.0f)
        glRotatef(renderManager.playerViewX, 1.0f, 0.0f, 0.0f)
        glScalef(-scale, -scale, scale)
        GLUtils.setGLCap(2896, false)
        GLUtils.setGLCap(2929, false)
        val width = sr.scaledHeight / 2
        val Height = sr.scaledHeight / 2
        GLUtils.setGLCap(3042, true)
        glBlendFunc(770, 771)
        val lol = entity.displayName.formattedText
        val USERNAME = distanceText + bot + lol + healthText
        val STRING_WIDTH = fr.getStringWidth(USERNAME) / 2
        val STRING_WIDTHz = fr.getStringWidth(USERNAME) / 4
        if(background.get()){
        Gui.drawRect((-STRING_WIDTH - 1).toInt(), -14, (STRING_WIDTH + 1).toInt(), -4, Integer.MIN_VALUE)
    }
        if (this.healthValue.get()) {

        }
        if (outlineValue.get()) {
            GameFontRenderer.drawOutlineStringWithoutGL(USERNAME, ((-STRING_WIDTHz).toFloat()), ((fr.FONT_HEIGHT - 15.5).toFloat()), 16777215, fontRenderer = fr)
        }else{
            mc.fontRendererObj.drawString(USERNAME, (-STRING_WIDTH).toFloat(), (fr.FONT_HEIGHT - 22).toFloat(), 16777215,fontShadowValue.get())
        }
        glColor3f(1f, 1f, 1f)
        glScaled(0.5, 0.5, 0.5)
        glScaled(1.0, 1.0, 1.0)
        var COLOR = Color(200, 75, 75).rgb
        if (entity.health > 20) {
            COLOR = -65292
        }
        glPushMatrix()
        glScaled(1.5, 1.5, 1.5)
        if (this.armorValue.get()) {
            var xOffset = 0
            for (armourStack in (entity as EntityPlayer).inventory.armorInventory) {
                if (armourStack != null) xOffset -= 10
            }
            val renderStack: Any
            if (entity.heldItem != null) {
                xOffset -= 8
                renderStack = entity.heldItem.copy()
                if (renderStack.hasEffect()
                    && (renderStack.item is ItemTool
                            || renderStack.item is ItemArmor)) renderStack.stackSize = 1
                renderItemStack(renderStack, xOffset, -34)
                xOffset += 20
            }
            for (armourStack in entity.inventory.armorInventory) if (armourStack != null) {
                val renderStack1 = armourStack.copy()
                if (renderStack1.hasEffect() && (renderStack1.item is ItemTool
                            || renderStack1.item is ItemArmor)) renderStack1.stackSize = 1
                renderItemStack(renderStack1, xOffset, -33)
                xOffset += 20
            }
        }
        glPopMatrix()
        GLUtils.revertAllCaps()
        glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
        glPopMatrix()
            }
        }
    }

    private fun renderNameTags(entity: EntityLivingBase, tag: String) {
        when (typeValue.get().toLowerCase()) {
            "3dtag" -> {
        val fontRenderer = fontValue.get()

        // Modify tag
        val bot = AntiBot.isBot(entity)
        val ping = if (entity is EntityPlayer) EntityUtils.getPing(entity) else 0

        val distanceText = if (distanceValue.get()) " §a${mc.thePlayer.getDistanceToEntity(entity).roundToInt()} " else ""
        val pingText = if (pingValue.get() && entity is EntityPlayer) " §7[" + (if (ping > 200) "§c" else if (ping > 100) "§e" else "§a") + ping + "ms§7]" else ""
        val healthText = if (healthValue.get()) " §a" + entity.health.toInt() + " " else ""
        val botText = if (bot) " §7[§6§lBot§7] " else ""

        val text = "$tag$healthText$distanceText$pingText$botText"

        // Push
        glPushMatrix()

        // Translate to player position
        val timer = mc.timer
        val renderManager = mc.renderManager


        glTranslated( // Translate to player position with render pos and interpolate it
                entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * timer.renderPartialTicks - renderManager.renderPosX,
                entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * timer.renderPartialTicks - renderManager.renderPosY + entity.eyeHeight.toDouble() + translateY.get().toDouble(),
                entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * timer.renderPartialTicks - renderManager.renderPosZ
        )

        glRotatef(-mc.renderManager.playerViewY, 0F, 1F, 0F)
        glRotatef(mc.renderManager.playerViewX, 1F, 0F, 0F)


        // Scale
        var distance = mc.thePlayer.getDistanceToEntity(entity) / 4F

        if (distance < 1F) {
            distance = 1F
        }

        val scale = (distance / 150F) * scaleValue.get()

        glScalef(-scale, -scale, scale)

        //AWTFontRenderer.assumeNonVolatile = true

        // Draw NameTag
        val width = fontRenderer.getStringWidth(text) * 0.5f
        val widths = fontRenderer.getStringWidth(text) / 3.8f

        val dist = width + 4F - (-width - 2F)

        glDisable(GL_TEXTURE_2D)
        glEnable(GL_BLEND)

        val bgColor = Color(backgroundColorRedValue.get(), backgroundColorGreenValue.get(), backgroundColorBlueValue.get(), backgroundColorAlphaValue.get())
        val borderColor = Color(borderColorRedValue.get(), borderColorGreenValue.get(), borderColorBlueValue.get(), borderColorAlphaValue.get())

        if (borderValue.get())
            RenderUtils.quickDrawBorderedRect(-width - 2F, -2F, width + 4F, fontRenderer.FONT_HEIGHT + 2F + if (healthBarValue.get()) 2F else 0F, 2F, borderColor.rgb,  bgColor.rgb)
        else
            RenderUtils.quickDrawRect(-width - 2F, -2F, width + 4F, fontRenderer.FONT_HEIGHT + 2F + if (healthBarValue.get()) 2F else 0F, bgColor.rgb)

        if(healthBarValue.get())
            if (entity.health < 4096.0) {
                RenderUtils.quickDrawRect(-width - 2F, fontRenderer.FONT_HEIGHT + 3F, -width - 2F + (dist * (entity.health.toFloat() / entity.maxHealth.toFloat()).coerceIn(0F, 1F)), fontRenderer.FONT_HEIGHT + 4F, Color(0, 255, 0).rgb)
                if(entity.health < 15.0)
                    RenderUtils.quickDrawRect(-width - 2F, fontRenderer.FONT_HEIGHT + 3F, -width - 2F + (dist * (entity.health.toFloat() / entity.maxHealth.toFloat()).coerceIn(0F, 1F)), fontRenderer.FONT_HEIGHT + 4F, Color(255, 255, 0).rgb)

                if(entity.health < 10.0)
                    RenderUtils.quickDrawRect(-width - 2F, fontRenderer.FONT_HEIGHT + 3F, -width - 2F + (dist * (entity.health.toFloat() / entity.maxHealth.toFloat()).coerceIn(0F, 1F)), fontRenderer.FONT_HEIGHT + 4F, Color(255, 0, 0).rgb)
            }

        glEnable(GL_TEXTURE_2D)
        if (outlineValue.get()) {
            GameFontRenderer.drawOutlineStringWithoutGL(text, 1f + -widths, if (fontRenderer == Fonts.minecraftFont) 1F else 1.5F, 0xFFFFFF,fontRenderer = fontRenderer )
                    }else{
            fontRenderer.drawString(text, 1f + -width, if (fontRenderer == Fonts.minecraftFont) 1F else 1.5F, 0xFFFFFF, fontShadowValue.get())
        }

        //AWTFontRenderer.assumeNonVolatile = false

        var foundPotion = false
        if (potionValue.get() && entity is EntityPlayer) {
            val potions = (entity.getActivePotionEffects() as Collection<PotionEffect>).map { Potion.potionTypes[it.getPotionID()] }.filter { it.hasStatusIcon() }
            if (!potions.isEmpty()) {
                foundPotion = true

                color(1.0F, 1.0F, 1.0F, 1.0F)
                disableLighting()
                enableTexture2D()

                val minX = (potions.size * -20) / 2

                var index = 0

                glPushMatrix()
                enableRescaleNormal()
                for (potion in potions) {
                    color(1.0F, 1.0F, 1.0F, 1.0F)
                    mc.getTextureManager().bindTexture(inventoryBackground)
                    val i1 = potion.getStatusIconIndex()
                    RenderUtils.drawTexturedModalRect(minX + index * 20, -22, 0 + i1 % 8 * 18, 198 + i1 / 8 * 18, 18, 18, 0F)
                    index++
                }
                disableRescaleNormal()
                glPopMatrix()

                enableAlpha()
                disableBlend()
                enableTexture2D()
            }
        }

        if (armorValue.get() && entity is EntityPlayer) {
            for (index in 0..4) {
                if (entity.getEquipmentInSlot(index) == null)
                    continue

                mc.renderItem.zLevel = -147F
                mc.renderItem.renderItemAndEffectIntoGUI(entity.getEquipmentInSlot(index), -50 + index * 20, if (potionValue.get() && foundPotion) -42 else -22)
            }

            enableAlpha()
            disableBlend()
            enableTexture2D()
        }

        if (enchantValue.get() && entity is EntityPlayer) {
            glPushMatrix()
            for (index in 0..4) {
                if (entity.getEquipmentInSlot(index) == null)
                    continue

                mc.renderItem.renderItemOverlays(mc.fontRendererObj, entity.getEquipmentInSlot(index), -50 + index * 20, if (potionValue.get() && foundPotion) -42 else -22)
                RenderUtils.drawExhiEnchants(entity.getEquipmentInSlot(index), -50f + index * 20f, if (potionValue.get() && foundPotion) -42f else -22f)
            }


            // Disable lightning and depth test
            glDisable(GL_LIGHTING)
            glDisable(GL_DEPTH_TEST)

            glEnable(GL_LINE_SMOOTH)

            // Enable blend
            glEnable(GL_BLEND)
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

            glPopMatrix()
            RenderHelper.disableStandardItemLighting();
        }

        // Reset color
        resetColor()
        glColor4f(1F, 1F, 1F, 1F)

        // Pop
        glPopMatrix()
    }
        }
    }

    fun renderItemStack(stack: ItemStack, x: Int, y: Int) {
        when (typeValue.get().toLowerCase()) {
            "2dtag" -> {
                glPushMatrix()
                glDepthMask(true)
                clear(256)
                RenderHelper.enableStandardItemLighting()
                Minecraft.getMinecraft().getRenderItem().zLevel = -150.0f
                whatTheFuckOpenGLThisFixesItemGlint()
                Minecraft.getMinecraft().getRenderItem().renderItemAndEffectIntoGUI(stack, x, y)
                Minecraft.getMinecraft().getRenderItem().renderItemOverlays(Minecraft.getMinecraft().fontRendererObj, stack, x, y)
                Minecraft.getMinecraft().getRenderItem().zLevel = 0.0f
                RenderHelper.disableStandardItemLighting()
                renderEnchantText(stack, x, y)
                disableCull()
                enableAlpha()
                disableBlend()
                disableLighting()
                scale(0.5, 0.5, 0.5)
                disableDepth()
                enableDepth()
                scale(2.0f, 2.0f, 2.0f)
                glPopMatrix()
            }
        }
    }

    private fun whatTheFuckOpenGLThisFixesItemGlint() {
        when (typeValue.get().toLowerCase()) {
            "2dtag" -> {
                disableLighting()
                disableDepth()
                disableBlend()
                enableLighting()
                enableDepth()
                disableLighting()
                disableDepth()
                disableTexture2D()
                disableAlpha()
                disableBlend()
                enableBlend()
                enableAlpha()
                enableTexture2D()
                enableLighting()
                enableDepth()
            }
        }
    }

    private fun renderEnchantText(stack: ItemStack, x: Int, y: Int) {
        when (typeValue.get().toLowerCase()) {
            "2dtag" -> {
        var unbreakingLevel2: Int
        var enchantmentY = y - 8
        if (stack.enchantmentTagList != null && stack.enchantmentTagList.tagCount() >= 6) {
            mc.fontRendererObj.drawStringWithShadow("god", x * 2.toFloat(), enchantmentY.toFloat(), 16711680)
            return
        }
        if (stack.item is ItemArmor) {
            val protectionLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.protection.effectId, stack)
            val projectileProtectionLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.projectileProtection.effectId, stack)
            val blastProtectionLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.blastProtection.effectId, stack)
            val fireProtectionLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.fireProtection.effectId, stack)
            val thornsLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.thorns.effectId, stack)
            val unbreakingLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack)
            if (protectionLevel > 0) {
                mc.fontRendererObj.drawStringWithShadow("pr$protectionLevel", x * 1.toFloat(), enchantmentY.toFloat(), 52479)
                enchantmentY += 8
            }
            if (projectileProtectionLevel > 0) {
                mc.fontRendererObj.drawStringWithShadow("pp$projectileProtectionLevel", x * 1.toFloat(), enchantmentY.toFloat(), 52479)
                enchantmentY += 8
            }
            if (blastProtectionLevel > 0) {
                mc.fontRendererObj.drawStringWithShadow("bp$blastProtectionLevel", x * 1.toFloat(), enchantmentY.toFloat(), 52479)
                enchantmentY += 8
            }
            if (fireProtectionLevel > 0) {
                mc.fontRendererObj.drawStringWithShadow("fp$fireProtectionLevel", x * 1.toFloat(), enchantmentY.toFloat(), 52479)
                enchantmentY += 8
            }
            if (thornsLevel > 0) {
                mc.fontRendererObj.drawStringWithShadow("t$thornsLevel", x * 1.toFloat(), enchantmentY.toFloat(), 52479)
                enchantmentY += 8
            }
            if (unbreakingLevel > 0) {
                mc.fontRendererObj.drawStringWithShadow("u$unbreakingLevel", x * 1.toFloat(), enchantmentY.toFloat(), 52479)
                enchantmentY += 8
            }
        }
        if (stack.item is ItemBow) {
            val powerLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, stack)
            val punchLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.punch.effectId, stack)
            val flameLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, stack)
            unbreakingLevel2 = EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack)
            if (powerLevel > 0) {
                mc.fontRendererObj.drawStringWithShadow("po$powerLevel", x * 1.toFloat(), enchantmentY.toFloat(), 65535)
                enchantmentY += 8
            }
            if (punchLevel > 0) {
                mc.fontRendererObj.drawStringWithShadow("pu$punchLevel", x * 1.toFloat(), enchantmentY.toFloat(), 65535)
                enchantmentY += 8
            }
            if (flameLevel > 0) {
                mc.fontRendererObj.drawStringWithShadow("f$flameLevel", x * 1.toFloat(), enchantmentY.toFloat(), 65535)
                enchantmentY += 8
            }
            if (unbreakingLevel2 > 0) {
                mc.fontRendererObj.drawStringWithShadow("u$unbreakingLevel2", x * 1.toFloat(), enchantmentY.toFloat(), 65535)
                enchantmentY += 8
            }
        }
        if (stack.item is ItemSword) {
            val sharpnessLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, stack)
            val knockbackLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.knockback.effectId, stack)
            val fireAspectLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.fireAspect.effectId, stack)
            unbreakingLevel2 = EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack)
            if (sharpnessLevel > 0) {
                mc.fontRendererObj.drawStringWithShadow("sh$sharpnessLevel", x * 1.toFloat(), enchantmentY.toFloat(), 65535)
                enchantmentY += 8
            }
            if (knockbackLevel > 0) {
                mc.fontRendererObj.drawStringWithShadow("kn$knockbackLevel", x * 1.toFloat(), enchantmentY.toFloat(), 65535)
                enchantmentY += 8
            }
            if (fireAspectLevel > 0) {
                mc.fontRendererObj.drawStringWithShadow("f$fireAspectLevel", x * 1.toFloat(), enchantmentY.toFloat(), 65535)
                enchantmentY += 8
            }
            if (unbreakingLevel2 > 0) {
                mc.fontRendererObj.drawStringWithShadow("ub$unbreakingLevel2", x * 1.toFloat(), enchantmentY.toFloat(), 65535)
            }
        }
        if (stack.item is ItemTool) {
            val unbreakingLevel22 = EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack)
            val efficiencyLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId, stack)
            val fortuneLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.fortune.effectId, stack)
            val silkTouch = EnchantmentHelper.getEnchantmentLevel(Enchantment.silkTouch.effectId, stack)
            if (efficiencyLevel > 0) {
                mc.fontRendererObj.drawStringWithShadow("eff$efficiencyLevel", x * 1.toFloat(), enchantmentY.toFloat(), 65535)
                enchantmentY += 8
            }
            if (fortuneLevel > 0) {
                mc.fontRendererObj.drawStringWithShadow("fo$fortuneLevel", x * 1.toFloat(), enchantmentY.toFloat(), 65535)
                enchantmentY += 8
            }
            if (silkTouch > 0) {
                mc.fontRendererObj.drawStringWithShadow("st$silkTouch", x * 1.toFloat(), enchantmentY.toFloat(), 65535)
                enchantmentY += 8
            }
            if (unbreakingLevel22 > 0) {
                mc.fontRendererObj.drawStringWithShadow("ub$unbreakingLevel22", x * 1.toFloat(), enchantmentY.toFloat(), 65535)
            }
        }
        if (stack.item === Items.golden_apple && stack.hasEffect()) {
            mc.fontRendererObj.drawStringWithShadow("god", x * 2.toFloat(), enchantmentY.toFloat(), 52479)
        }
    }
        }
    }
}

