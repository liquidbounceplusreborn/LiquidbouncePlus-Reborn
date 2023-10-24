package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.misc.AntiBot
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.ui.font.GameFontRenderer
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.*
import net.minecraft.client.renderer.GlStateManager.*
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.potion.Potion
import net.minecraft.potion.PotionEffect
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import kotlin.math.roundToInt


@ModuleInfo(
    name = "NameTags",
    spacedName = "Name Tags",
    description = "Custom name tag rendering.",
    category = ModuleCategory.RENDER
)
class NameTags : Module() {
    private val componentsNode = NoteValue("Components")
    private val healthValue = BoolValue("Health", true)
    private val distanceValue = BoolValue("Distance", true)
    private val armorValue = BoolValue("Armor", true)
    private val enchantValue = BoolValue("Enchant", true) { armorValue.get() }
    private val potionValue = BoolValue("Potions", true)
    private val healthBarValue = BoolValue("Bar", true)
    private val pingValue = BoolValue("Ping", true)

    private val settingsNote = NoteValue("Settings")
    private val scaleValue = FloatValue("Scale", 1.0F, 0.5F, 2.0F)
    private val translateY = FloatValue("TranslateY", 0.55F, -2F, 2F)
    private val clearNamesValue = BoolValue("ClearNames", false)
    private val fontValue = FontValue("Font", Fonts.font40)
    private val fontShadowValue = BoolValue("Shadow", true)

    val localValue = BoolValue("LocalPlayer", true)
    val nfpValue = BoolValue("NoFirstPerson", true) { localValue.get() }

    private val outlineValue = BoolValue("Outline", true)

    private val background = BoolValue("Background", false)
    private val bgRed = IntegerValue("Background-R", 0, 0, 255) { background.get() }
    private val bgGreen = IntegerValue("Background-G", 0, 0, 255) { background.get() }
    private val bgBlue = IntegerValue("Background-B", 0, 0, 255) { background.get() }
    private val bgAlpha = IntegerValue("Background-Alpha", 0, 0, 255) { background.get() }

    private val borderValue = BoolValue("Border", true)
    private val borderColorRedValue = IntegerValue("Border-R", 0, 0, 255) { borderValue.get() }
    private val borderColorGreenValue = IntegerValue("Border-G", 0, 0, 255) { borderValue.get() }
    private val borderColorBlueValue = IntegerValue("Border-B", 0, 0, 255) { borderValue.get() }
    private val borderColorAlphaValue = IntegerValue("Border-Alpha", 0, 0, 255) { borderValue.get() }

    private val inventoryBackground = ResourceLocation("textures/gui/container/inventory.png")

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        glPushAttrib(GL_ENABLE_BIT)
        glPushMatrix()
        glDisable(GL_LIGHTING)
        glDisable(GL_DEPTH_TEST)
        glEnable(GL_LINE_SMOOTH)
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        for (entity in mc.theWorld.loadedEntityList) {
            if (!EntityUtils.isSelected(entity, false) && (!localValue.get() || entity != mc.thePlayer || (nfpValue.get() && mc.gameSettings.thirdPersonView == 0)))
                continue

            renderNameTags3D(entity as EntityLivingBase)
        }

        glPopMatrix()
        glPopAttrib()
        resetColor()
        glColor4f(1F, 1F, 1F, 1F)
    }

    private fun getTag(entity: EntityLivingBase): String? {
        val tag = if (clearNamesValue.get())
            ColorUtils.stripColor(entity.displayName.unformattedText) ?: return null
        else
            entity.displayName.unformattedText
        val bot = AntiBot.isBot(entity)
        val ping = if (entity is EntityPlayer) EntityUtils.getPing(entity) else 0

        val distanceText = if (distanceValue.get()) " §a${mc.thePlayer.getDistanceToEntity(entity).roundToInt()} " else ""
        val pingText = if (pingValue.get() && entity is EntityPlayer) " §7[" + (if (ping > 200) "§c" else if (ping > 100) "§e" else "§a") + ping + "ms§7]" else ""
        val healthText = if (healthValue.get()) " §a" + entity.health.toInt() + " " else ""
        val botText = if (bot) " §7[§6§lBot§7] " else ""

        return "$tag$healthText$distanceText$pingText$botText"
    }

    private fun renderNameTags3D(entity: EntityLivingBase) {
        val fontRenderer = fontValue.get()
        val text = getTag(entity) ?: return

        glPushMatrix()

        val timer = mc.timer
        val renderManager = mc.renderManager


        glTranslated( // Translate to player position with render pos and interpolate it
            entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * timer.renderPartialTicks - renderManager.renderPosX,
            entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * timer.renderPartialTicks - renderManager.renderPosY +
                    entity.height + translateY.get().toDouble(),
            entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * timer.renderPartialTicks - renderManager.renderPosZ
        )

        glRotatef(-mc.renderManager.playerViewY, 0F, 1F, 0F)
        glRotatef(mc.renderManager.playerViewX, 1F, 0F, 0F)


        // Scale
        var distance = mc.thePlayer.getDistanceToEntity(entity) / 4F

        distance = distance.coerceAtLeast(1F)

        val scale = (distance / 150F) * scaleValue.get()

        glScalef(-scale, -scale, scale)

        //AWTFontRenderer.assumeNonVolatile = true

        // Draw NameTag
        val width = fontRenderer.getStringWidth(text) * 0.5f
        val widths = fontRenderer.getStringWidth(text) / 3.8f

        val dist = width + 4F - (-width - 2F)

        glDisable(GL_TEXTURE_2D)
        glEnable(GL_BLEND)

        val bgColor = Color(
            bgRed.get(),
            bgGreen.get(),
            bgBlue.get(),
            bgAlpha.get()
        )
        val borderColor = Color(
            borderColorRedValue.get(),
            borderColorGreenValue.get(),
            borderColorBlueValue.get(),
            borderColorAlphaValue.get()
        )

        if (borderValue.get())
            RenderUtils.quickDrawBorderedRect(
                -width - 2F,
                -2F,
                width + 4F,
                fontRenderer.FONT_HEIGHT + 2F + if (healthBarValue.get()) 2F else 0F,
                2F,
                borderColor.rgb,
                bgColor.rgb
            )
        else
            RenderUtils.quickDrawRect(
                -width - 2F,
                -2F,
                width + 4F,
                fontRenderer.FONT_HEIGHT + 2F + if (healthBarValue.get()) 2F else 0F,
                bgColor.rgb
            )

        if (healthBarValue.get())
            if (entity.health < 4096.0) {
                RenderUtils.quickDrawRect(
                    -width - 2F,
                    fontRenderer.FONT_HEIGHT + 3F,
                    -width - 2F + (dist * (entity.health / entity.maxHealth).coerceIn(
                        0F,
                        1F
                    )),
                    fontRenderer.FONT_HEIGHT + 4F,
                    Color(0, 255, 0).rgb
                )
                if (entity.health < 15.0)
                    RenderUtils.quickDrawRect(
                        -width - 2F,
                        fontRenderer.FONT_HEIGHT + 3F,
                        -width - 2F + (dist * (entity.health / entity.maxHealth).coerceIn(
                            0F,
                            1F
                        )),
                        fontRenderer.FONT_HEIGHT + 4F,
                        Color(255, 255, 0).rgb
                    )

                if (entity.health < 10.0)
                    RenderUtils.quickDrawRect(
                        -width - 2F,
                        fontRenderer.FONT_HEIGHT + 3F,
                        -width - 2F + (dist * (entity.health / entity.maxHealth).coerceIn(
                            0F,
                            1F
                        )),
                        fontRenderer.FONT_HEIGHT + 4F,
                        Color(255, 0, 0).rgb
                    )
            }

        glEnable(GL_TEXTURE_2D)
        if (outlineValue.get()) {
            GameFontRenderer.drawOutlineStringWithoutGL(
                text,
                1f + -widths,
                if (fontRenderer == Fonts.minecraftFont) 1F else 1.5F,
                0xFFFFFF,
                fontRenderer = fontRenderer
            )
        } else {
            fontRenderer.drawString(
                text,
                1f + -width,
                if (fontRenderer == Fonts.minecraftFont) 1F else 1.5F,
                0xFFFFFF,
                fontShadowValue.get()
            )
        }

        //AWTFontRenderer.assumeNonVolatile = false

        var foundPotion = false
        if (potionValue.get() && entity is EntityPlayer) {
            val potions =
                (entity.getActivePotionEffects() as Collection<PotionEffect>).map { Potion.potionTypes[it.potionID] }
                    .filter { it.hasStatusIcon() }
            if (potions.isNotEmpty()) {
                foundPotion = true

                color(1.0F, 1.0F, 1.0F, 1.0F)
                disableLighting()
                enableTexture2D()

                val minX = (potions.size * -20) / 2

                glPushMatrix()
                enableRescaleNormal()
                for ((index, potion) in potions.withIndex()) {
                    color(1.0F, 1.0F, 1.0F, 1.0F)
                    mc.textureManager.bindTexture(inventoryBackground)
                    val i1 = potion.statusIconIndex
                    RenderUtils.drawTexturedModalRect(
                        minX + index * 20,
                        -22,
                        0 + i1 % 8 * 18,
                        198 + i1 / 8 * 18,
                        18,
                        18,
                        0F
                    )
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
                mc.renderItem.renderItemAndEffectIntoGUI(
                    entity.getEquipmentInSlot(index),
                    -50 + index * 20,
                    if (potionValue.get() && foundPotion) -42 else -22
                )
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

                val offset = -6
                mc.renderItem.renderItemOverlays(
                    mc.fontRendererObj,
                    entity.getEquipmentInSlot(index),
                    -50 + index * 20,
                    if (potionValue.get() && foundPotion) -40 else -20
                )
                RenderUtils.drawExhiEnchants(
                    entity.getEquipmentInSlot(index),
                    -50f + index * 20f,
                    if (potionValue.get() && foundPotion) -42f + offset else -22f + offset
                )
            }

            glDisable(GL_LIGHTING)
            glDisable(GL_DEPTH_TEST)
            glEnable(GL_LINE_SMOOTH)
            glEnable(GL_BLEND)
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
            glPopMatrix()
            RenderHelper.disableStandardItemLighting()
        }

        resetColor()
        glColor4f(1F, 1F, 1F, 1F)
        glPopMatrix()
    }
}