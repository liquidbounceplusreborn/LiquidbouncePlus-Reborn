/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 *
 * This code belongs to WYSI-Foundation. Please give credits when using this in your repository.
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.ui.font.GameFontRenderer.Companion.getColorIndex
import net.ccbluex.liquidbounce.ui.font.TTFFontRenderer
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.utils.item.ItemUtils
import net.ccbluex.liquidbounce.utils.render.BlendUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils.LiquidSlowly
import net.ccbluex.liquidbounce.utils.render.ColorUtils.fade
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.*
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GLAllocation
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityItem
import net.minecraft.item.ItemStack
import net.minecraft.potion.Potion
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.MathHelper
import org.lwjgl.opengl.Display
import org.lwjgl.opengl.GL11
import org.lwjgl.util.glu.GLU
import java.awt.Color
import java.nio.FloatBuffer
import java.nio.IntBuffer
import java.text.DecimalFormat
import java.util.*
import javax.vecmath.Vector3d
import javax.vecmath.Vector4d
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

@ModuleInfo(name = "ESP2D", description = "autumn skid.", category = ModuleCategory.RENDER)
class ESP2D : Module() {
    private val outline = BoolValue("Outline", true)
    private val boxMode = ListValue("Mode", arrayOf("Box", "Corners"), "Box")
    private val healthBar = BoolValue("Health-bar", true)
    private val hpBarMode = ListValue("HBar-Mode", arrayOf("Dot", "Line"), "Dot") { healthBar.get() }
    private val absorption = BoolValue("Render-Absorption", true) { healthBar.get() && hpBarMode.get().equals("line", ignoreCase = true) }
    private val armorBar = BoolValue("Armor-bar", true)
    private val armorBarMode = ListValue("ABar-Mode", arrayOf("Total", "Items"), "Total") { armorBar.get() }
    private val healthNumber = BoolValue("HealthNumber", true) { healthBar.get() }
    private val hpMode = ListValue("HP-Mode", arrayOf("Health", "Percent"), "Health") { healthBar.get() && healthNumber.get() }
    private val armorNumber = BoolValue("ItemArmorNumber", true) { armorBar.get() }
    private val armorItems = BoolValue("ArmorItems", true)
    private val armorDur = BoolValue("ArmorDurability", true) { armorItems.get() }
    private val hoverValue = BoolValue("Details-HoverOnly", false)
    private val tagsValue = BoolValue("Tags", true)
    private val tagsBGValue = BoolValue("Tags-Background", false) { tagsValue.get() }
    private val itemTagsValue = BoolValue("Item-Tags", true)
    private val outlineFont = BoolValue("OutlineFont", false)
    private val clearNameValue = BoolValue("Use-Clear-Name", false)
    private val localPlayer = BoolValue("Local-Player", true)
    private val droppedItems = BoolValue("Dropped-Items", false)
    private val colorModeValue = ListValue("Color", arrayOf("Custom", "Rainbow", "Sky", "LiquidSlowly", "Fade", "Mixer"), "Custom")
    private val colorRedValue = IntegerValue("Red", 255, 0, 255)
    private val colorGreenValue = IntegerValue("Green", 255, 0, 255)
    private val colorBlueValue = IntegerValue("Blue", 255, 0, 255)
    private val saturationValue = FloatValue("Saturation", 1f, 0f, 1f)
    private val brightnessValue = FloatValue("Brightness", 1f, 0f, 1f)
    private val mixerSecondsValue = IntegerValue("Seconds", 2, 1, 10)
    private val fontScaleValue = FloatValue("Font-Scale", 0.5f, 0f, 1f, "x")
    private val fontValue = FontValue("Font", Fonts.font40)
    private val colorTeam = BoolValue("Team", false)
    private val viewport: IntBuffer = GLAllocation.createDirectIntBuffer(16)
    private val modelview: FloatBuffer = GLAllocation.createDirectFloatBuffer(16)
    private val projection: FloatBuffer = GLAllocation.createDirectFloatBuffer(16)
    private val vector: FloatBuffer = GLAllocation.createDirectFloatBuffer(4)
    private val backgroundColor: Int = Color(0, 0, 0, 120).rgb
    private val black: Int = Color.BLACK.rgb
    private val dFormat = DecimalFormat("0.0")

    fun getColor(entity: Entity?): Color? {
        if (entity is EntityLivingBase) {
            val entityLivingBase = entity
            if (EntityUtils.isFriend(entityLivingBase)) return Color.BLUE
            if (colorTeam.get()) {
                val chars = entityLivingBase.displayName.formattedText.toCharArray()
                var color = Int.MAX_VALUE
                for (i in chars.indices) {
                    if (chars[i] != '§' || i + 1 >= chars.size) continue
                    val index = getColorIndex(chars[i + 1])
                    if (index < 0 || index > 15) continue
                    color = ColorUtils.hexColors[index]
                    break
                }
                return Color(color)
            }
        }
        return when (colorModeValue.get()) {
            "Custom" -> Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get())
            "Rainbow" -> Color(
                RenderUtils.getRainbowOpaque(
                    mixerSecondsValue.get(),
                    saturationValue.get(),
                    brightnessValue.get(),
                    0
                )
            )

            "Sky" -> RenderUtils.skyRainbow(
                0,
                saturationValue.get(),
                brightnessValue.get()
            )

            "LiquidSlowly" -> LiquidSlowly(
                System.nanoTime(),
                0,
                saturationValue.get(),
                brightnessValue.get()
            )

            "Mixer" -> ColorMixer.getMixedColor(0, mixerSecondsValue.get())
            else -> fade(
                Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get()),
                0,
                100
            )
        }
    }

    override fun onDisable() {
        collectedEntities.clear()
    }

    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        GL11.glPushMatrix()
        collectEntities()
        val partialTicks = event.partialTicks
        val scaledResolution = ScaledResolution(mc)
        val scaleFactor = scaledResolution.scaleFactor
        val scaling: Double = scaleFactor.toDouble() / scaleFactor.toDouble().pow(2.0)
        GL11.glScaled(scaling, scaling, scaling)
        val black = black
        val background = backgroundColor
        val renderMng = mc.renderManager
        val entityRenderer = mc.entityRenderer
        val outline = outline.get()
        val health = healthBar.get()
        val armor = armorBar.get()
        var i = 0
        val collectedEntitiesSize = collectedEntities.size
        while (i < collectedEntitiesSize) {
            val entity = collectedEntities[i]
            val color = getColor(entity)!!.rgb
            if (RenderUtils.isInViewFrustrum(entity)) {
                val x = RenderUtils.interpolate(entity.posX, entity.lastTickPosX, partialTicks.toDouble())
                val y = RenderUtils.interpolate(entity.posY, entity.lastTickPosY, partialTicks.toDouble())
                val z = RenderUtils.interpolate(entity.posZ, entity.lastTickPosZ, partialTicks.toDouble())
                val width = entity.width.toDouble() / 1.5
                val height = entity.height.toDouble() + if (entity.isSneaking) -0.3 else 0.2
                val aabb = AxisAlignedBB(x - width, y, z - width, x + width, y + height, z + width)
                val vectors: List<*> = Arrays.asList(
                    Vector3d(aabb.minX, aabb.minY, aabb.minZ),
                    Vector3d(aabb.minX, aabb.maxY, aabb.minZ),
                    Vector3d(aabb.maxX, aabb.minY, aabb.minZ),
                    Vector3d(aabb.maxX, aabb.maxY, aabb.minZ),
                    Vector3d(aabb.minX, aabb.minY, aabb.maxZ),
                    Vector3d(aabb.minX, aabb.maxY, aabb.maxZ),
                    Vector3d(aabb.maxX, aabb.minY, aabb.maxZ),
                    Vector3d(aabb.maxX, aabb.maxY, aabb.maxZ)
                )
                entityRenderer.setupCameraTransform(partialTicks, 0)
                var position: Vector4d? = null
                val var38 = vectors.iterator()
                while (var38.hasNext()) {
                    var vector = var38.next() as Vector3d?
                    vector = project2D(
                        scaleFactor,
                        vector!!.x - renderMng.viewerPosX,
                        vector.y - renderMng.viewerPosY,
                        vector.z - renderMng.viewerPosZ
                    )
                    if (vector != null && vector.z >= 0.0 && vector.z < 1.0) {
                        if (position == null) {
                            position = Vector4d(vector.x, vector.y, vector.z, 0.0)
                        }
                        position.x = min(vector.x, position.x)
                        position.y = min(vector.y, position.y)
                        position.z = max(vector.x, position.z)
                        position.w = max(vector.y, position.w)
                    }
                }
                if (position != null) {
                    entityRenderer.setupOverlayRendering()
                    val posX = position.x
                    val posY = position.y
                    val endPosX = position.z
                    val endPosY = position.w
                    if (outline) {
                        if (boxMode.get() === "Box") {
                            RenderUtils.newDrawRect(posX - 1.0, posY, posX + 0.5, endPosY + 0.5, black)
                            RenderUtils.newDrawRect(posX - 1.0, posY - 0.5, endPosX + 0.5, posY + 0.5 + 0.5, black)
                            RenderUtils.newDrawRect(endPosX - 0.5 - 0.5, posY, endPosX + 0.5, endPosY + 0.5, black)
                            RenderUtils.newDrawRect(
                                posX - 1.0,
                                endPosY - 0.5 - 0.5,
                                endPosX + 0.5,
                                endPosY + 0.5,
                                black
                            )
                            RenderUtils.newDrawRect(posX - 0.5, posY, posX + 0.5 - 0.5, endPosY, color)
                            RenderUtils.newDrawRect(posX, endPosY - 0.5, endPosX, endPosY, color)
                            RenderUtils.newDrawRect(posX - 0.5, posY, endPosX, posY + 0.5, color)
                            RenderUtils.newDrawRect(endPosX - 0.5, posY, endPosX, endPosY, color)
                        } else {
                            RenderUtils.newDrawRect(
                                posX + 0.5,
                                posY,
                                posX - 1.0,
                                posY + (endPosY - posY) / 4.0 + 0.5,
                                black
                            )
                            RenderUtils.newDrawRect(
                                posX - 1.0,
                                endPosY,
                                posX + 0.5,
                                endPosY - (endPosY - posY) / 4.0 - 0.5,
                                black
                            )
                            RenderUtils.newDrawRect(
                                posX - 1.0,
                                posY - 0.5,
                                posX + (endPosX - posX) / 3.0 + 0.5,
                                posY + 1.0,
                                black
                            )
                            RenderUtils.newDrawRect(
                                endPosX - (endPosX - posX) / 3.0 - 0.5,
                                posY - 0.5,
                                endPosX,
                                posY + 1.0,
                                black
                            )
                            RenderUtils.newDrawRect(
                                endPosX - 1.0,
                                posY,
                                endPosX + 0.5,
                                posY + (endPosY - posY) / 4.0 + 0.5,
                                black
                            )
                            RenderUtils.newDrawRect(
                                endPosX - 1.0,
                                endPosY,
                                endPosX + 0.5,
                                endPosY - (endPosY - posY) / 4.0 - 0.5,
                                black
                            )
                            RenderUtils.newDrawRect(
                                posX - 1.0,
                                endPosY - 1.0,
                                posX + (endPosX - posX) / 3.0 + 0.5,
                                endPosY + 0.5,
                                black
                            )
                            RenderUtils.newDrawRect(
                                endPosX - (endPosX - posX) / 3.0 - 0.5,
                                endPosY - 1.0,
                                endPosX + 0.5,
                                endPosY + 0.5,
                                black
                            )
                            RenderUtils.newDrawRect(posX, posY, posX - 0.5, posY + (endPosY - posY) / 4.0, color)
                            RenderUtils.newDrawRect(posX, endPosY, posX - 0.5, endPosY - (endPosY - posY) / 4.0, color)
                            RenderUtils.newDrawRect(posX - 0.5, posY, posX + (endPosX - posX) / 3.0, posY + 0.5, color)
                            RenderUtils.newDrawRect(endPosX - (endPosX - posX) / 3.0, posY, endPosX, posY + 0.5, color)
                            RenderUtils.newDrawRect(endPosX - 0.5, posY, endPosX, posY + (endPosY - posY) / 4.0, color)
                            RenderUtils.newDrawRect(
                                endPosX - 0.5,
                                endPosY,
                                endPosX,
                                endPosY - (endPosY - posY) / 4.0,
                                color
                            )
                            RenderUtils.newDrawRect(posX, endPosY - 0.5, posX + (endPosX - posX) / 3.0, endPosY, color)
                            RenderUtils.newDrawRect(
                                endPosX - (endPosX - posX) / 3.0,
                                endPosY - 0.5,
                                endPosX - 0.5,
                                endPosY,
                                color
                            )
                        }
                    }
                    val living = entity is EntityLivingBase
                    var entityLivingBase: EntityLivingBase
                    var armorValue: Float
                    var itemDurability: Float
                    var durabilityWidth: Double
                    var textWidth: Double
                    var tagY: Float
                    if (living) {
                        entityLivingBase = entity as EntityLivingBase
                        if (health) {
                            armorValue = entityLivingBase.health
                            itemDurability = entityLivingBase.maxHealth
                            if (armorValue > itemDurability) armorValue = itemDurability
                            durabilityWidth = (armorValue / itemDurability).toDouble()
                            textWidth = (endPosY - posY) * durabilityWidth
                            val healthDisplay = dFormat.format(entityLivingBase.health.toDouble()) + " §c❤"
                            val healthPercent =
                                (entityLivingBase.health / itemDurability * 100f).toInt().toString() + "%"
                            if (healthNumber.get() && (!hoverValue.get() || entity === mc.thePlayer || isHovering(
                                    posX,
                                    endPosX,
                                    posY,
                                    endPosY,
                                    scaledResolution
                                ))
                            ) drawScaledString(
                                if (hpMode.get().equals("health", ignoreCase = true)) healthDisplay else healthPercent,
                                posX - 4.0 - fontValue.get().getStringWidth(
                                    if (hpMode.get()
                                            .equals("health", ignoreCase = true)
                                    ) healthDisplay else healthPercent
                                ) * fontScaleValue.get(),
                                endPosY - textWidth - fontValue.get().FONT_HEIGHT / 2f * fontScaleValue.get(),
                                fontScaleValue.get().toDouble(),
                                -1
                            )
                            RenderUtils.newDrawRect(posX - 3.5, posY - 0.5, posX - 1.5, endPosY + 0.5, background)
                            if (armorValue > 0.0f) {
                                val healthColor = BlendUtils.getHealthColor(armorValue, itemDurability).rgb
                                val deltaY = endPosY - posY
                                if (hpBarMode.get()
                                        .equals("dot", ignoreCase = true) && deltaY >= 60
                                ) { // revert back to normal bar if the height is too low
                                    for (k in 0..9) {
                                        val reratio = MathHelper.clamp_double(
                                            armorValue - k * (itemDurability / 10.0),
                                            0.0,
                                            itemDurability / 10.0
                                        ) / (itemDurability / 10.0)
                                        val hei = (deltaY / 10.0 - 0.5) * reratio
                                        RenderUtils.newDrawRect(
                                            posX - 3.0,
                                            endPosY - (deltaY + 0.5) / 10.0 * k,
                                            posX - 2.0,
                                            endPosY - (deltaY + 0.5) / 10.0 * k - hei,
                                            healthColor
                                        )
                                    }
                                } else {
                                    RenderUtils.newDrawRect(
                                        posX - 3.0,
                                        endPosY,
                                        posX - 2.0,
                                        endPosY - textWidth,
                                        healthColor
                                    )
                                    tagY = entityLivingBase.absorptionAmount
                                    if (absorption.get() && tagY > 0.0f) RenderUtils.newDrawRect(
                                        posX - 3.0,
                                        endPosY,
                                        posX - 2.0,
                                        endPosY - (endPosY - posY) / 6.0 * tagY.toDouble() / 2.0,
                                        Color(
                                            Potion.absorption.liquidColor
                                        ).rgb
                                    )
                                }
                            }
                        }
                    }
                    if (armor) {
                        if (living) {
                            entityLivingBase = entity as EntityLivingBase
                            if (armorBarMode.get().equals("items", ignoreCase = true)) {
                                val constHeight = (endPosY - posY) / 4.0
                                for (m in 4 downTo 1) {
                                    val armorStack = entityLivingBase.getEquipmentInSlot(m)
                                    val theHeight = constHeight + 0.25
                                    if (armorStack != null && armorStack.item != null) {
                                        RenderUtils.newDrawRect(
                                            endPosX + 1.5,
                                            endPosY + 0.5 - theHeight * m,
                                            endPosX + 3.5,
                                            endPosY + 0.5 - theHeight * (m - 1),
                                            background
                                        )
                                        RenderUtils.newDrawRect(
                                            endPosX + 2.0,
                                            endPosY + 0.5 - theHeight * (m - 1) - 0.25,
                                            endPosX + 3.0,
                                            endPosY + 0.5 - theHeight * (m - 1) - 0.25 - (constHeight - 0.25) * MathHelper.clamp_double(
                                                ItemUtils.getItemDurability(armorStack)
                                                    .toDouble() / armorStack.maxDamage.toDouble(),
                                                0.0,
                                                1.0
                                            ), Color(0, 255, 255).rgb
                                        )
                                    }
                                }
                            } else {
                                armorValue = entityLivingBase.totalArmorValue.toFloat()
                                val armorWidth = (endPosY - posY) * armorValue.toDouble() / 20.0
                                RenderUtils.newDrawRect(
                                    endPosX + 1.5,
                                    posY - 0.5,
                                    endPosX + 3.5,
                                    endPosY + 0.5,
                                    background
                                )
                                if (armorValue > 0.0f) RenderUtils.newDrawRect(
                                    endPosX + 2.0,
                                    endPosY,
                                    endPosX + 3.0,
                                    endPosY - armorWidth,
                                    Color(0, 255, 255).rgb
                                )
                            }
                        } else if (entity is EntityItem) {
                            val itemStack = entity.entityItem
                            if (itemStack.isItemStackDamageable) {
                                val maxDamage = itemStack.maxDamage
                                itemDurability = (maxDamage - itemStack.itemDamage).toFloat()
                                durabilityWidth = (endPosY - posY) * itemDurability.toDouble() / maxDamage.toDouble()
                                if (armorNumber.get() && (!hoverValue.get() || isHovering(
                                        posX,
                                        endPosX,
                                        posY,
                                        endPosY,
                                        scaledResolution
                                    ))
                                ) drawScaledString(
                                    itemDurability.toInt().toString() + "",
                                    endPosX + 4.0,
                                    endPosY - durabilityWidth - fontValue.get().FONT_HEIGHT / 2f * fontScaleValue.get(),
                                    fontScaleValue.get().toDouble(),
                                    -1
                                )
                                RenderUtils.newDrawRect(
                                    endPosX + 1.5,
                                    posY - 0.5,
                                    endPosX + 3.5,
                                    endPosY + 0.5,
                                    background
                                )
                                RenderUtils.newDrawRect(
                                    endPosX + 2.0,
                                    endPosY,
                                    endPosX + 3.0,
                                    endPosY - durabilityWidth,
                                    Color(0, 255, 255).rgb
                                )
                            }
                        }
                    }
                    if (living && armorItems.get() && (!hoverValue.get() || entity === mc.thePlayer || isHovering(
                            posX,
                            endPosX,
                            posY,
                            endPosY,
                            scaledResolution
                        ))
                    ) {
                        entityLivingBase = entity as EntityLivingBase
                        val yDist = (endPosY - posY) / 4.0
                        for (j in 4 downTo 1) {
                            val armorStack = entityLivingBase.getEquipmentInSlot(j)
                            if (armorStack != null && armorStack.item != null) {
                                renderItemStack(
                                    armorStack,
                                    endPosX + if (armor) 4.0 else 2.0,
                                    posY + yDist * (4 - j) + yDist / 2.0 - 5.0
                                )
                                if (armorDur.get()) drawScaledCenteredString(
                                    ItemUtils.getItemDurability(armorStack).toString() + "",
                                    endPosX + (if (armor) 4.0 else 2.0) + 4.5,
                                    posY + yDist * (4 - j) + yDist / 2.0 + 4.0,
                                    fontScaleValue.get().toDouble(),
                                    -1
                                )
                            }
                        }
                    }
                    if (living && tagsValue.get()) {
                        entityLivingBase = entity as EntityLivingBase
                        val entName =
                            if (clearNameValue.get()) entityLivingBase.name else entityLivingBase.displayName.formattedText
                        if (tagsBGValue.get()) RenderUtils.newDrawRect(
                            posX + (endPosX - posX) / 2f - (fontValue.get().getStringWidth(
                                entName
                            ) / 2f + 2f) * fontScaleValue.get(),
                            posY - 1f - (fontValue.get().FONT_HEIGHT + 2f) * fontScaleValue.get(),
                            posX + (endPosX - posX) / 2f + (fontValue.get().getStringWidth(entName) / 2f + 2f) * fontScaleValue.get(),
                            posY - 1f + 2f * fontScaleValue.get(),
                            -0x60000000
                        )
                        drawScaledCenteredString(
                            entName,
                            posX + (endPosX - posX) / 2f,
                            posY - 1f - fontValue.get().FONT_HEIGHT * fontScaleValue.get(),
                            fontScaleValue.get().toDouble(),
                            -1
                        )
                    }
                    if (itemTagsValue.get()) {
                        if (living) {
                            entityLivingBase = entity as EntityLivingBase
                            if (entityLivingBase.heldItem != null && entityLivingBase.heldItem.item != null) {
                                val itemName = entityLivingBase.heldItem.displayName
                                if (tagsBGValue.get()) RenderUtils.newDrawRect(
                                    posX + (endPosX - posX) / 2f - (fontValue.get().getStringWidth(
                                        itemName
                                    ) / 2f + 2f) * fontScaleValue.get(),
                                    endPosY + 1f - 2f * fontScaleValue.get(),
                                    posX + (endPosX - posX) / 2f + (fontValue.get().getStringWidth(itemName) / 2f + 2f) * fontScaleValue.get(),
                                    endPosY + 1f + (fontValue.get().FONT_HEIGHT + 2f) * fontScaleValue.get(),
                                    -0x60000000
                                )
                                drawScaledCenteredString(
                                    itemName,
                                    posX + (endPosX - posX) / 2f,
                                    endPosY + 1f,
                                    fontScaleValue.get().toDouble(),
                                    -1
                                )
                            }
                        } else if (entity is EntityItem) {
                            val entName = entity.entityItem.displayName
                            if (tagsBGValue.get()) RenderUtils.newDrawRect(
                                posX + (endPosX - posX) / 2f - (fontValue.get().getStringWidth(
                                    entName
                                ) / 2f + 2f) * fontScaleValue.get(),
                                endPosY + 1f - 2f * fontScaleValue.get(),
                                posX + (endPosX - posX) / 2f + (fontValue.get().getStringWidth(entName) / 2f + 2f) * fontScaleValue.get(),
                                endPosY + 1f + (fontValue.get().FONT_HEIGHT + 2f) * fontScaleValue.get(),
                                -0x60000000
                            )
                            drawScaledCenteredString(
                                entName,
                                posX + (endPosX - posX) / 2f,
                                endPosY + 1f,
                                fontScaleValue.get().toDouble(),
                                -1
                            )
                        }
                    }
                }
            }
            ++i
        }
        GL11.glPopMatrix()
        GlStateManager.enableBlend()
        GlStateManager.resetColor()
        entityRenderer.setupOverlayRendering()
    }

    private fun isHovering(minX: Double, maxX: Double, minY: Double, maxY: Double, sc: ScaledResolution): Boolean {
        return sc.scaledWidth / 2 >= minX && sc.scaledWidth / 2 < maxX && sc.scaledHeight / 2 >= minY && sc.scaledHeight / 2 < maxY
    }

    private fun drawScaledString(text: String, x: Double, y: Double, scale: Double, color: Int) {
        GlStateManager.pushMatrix()
        GlStateManager.translate(x, y, x)
        GlStateManager.scale(scale, scale, scale)
        if (outlineFont.get()) {
            TTFFontRenderer.drawOutlineStringWithoutGL(text, 0f, 0f, color, fontValue.get())
        } else {
            fontValue.get().drawStringWithShadow(text, 0f, 0f, color)
        }
        GlStateManager.popMatrix()
    }

    private fun drawScaledCenteredString(text: String, x: Double, y: Double, scale: Double, color: Int) {
        drawScaledString(text, x - fontValue.get().getStringWidth(text) / 2f * scale, y, scale, color)
    }

    private fun renderItemStack(stack: ItemStack, x: Double, y: Double) {
        GlStateManager.pushMatrix()
        GlStateManager.translate(x, y, x)
        GlStateManager.scale(0.5, 0.5, 0.5)
        GlStateManager.enableRescaleNormal()
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        RenderHelper.enableGUIStandardItemLighting()
        mc.renderItem.renderItemAndEffectIntoGUI(stack, 0, 0)
        mc.renderItem.renderItemOverlays(fontValue.get(), stack, 0, 0)
        RenderHelper.disableStandardItemLighting()
        GlStateManager.disableRescaleNormal()
        GlStateManager.disableBlend()
        GlStateManager.popMatrix()
    }

    private fun collectEntities() {
        collectedEntities.clear()
        val playerEntities: List<*> = mc.theWorld.loadedEntityList
        var i = 0
        val playerEntitiesSize = playerEntities.size
        while (i < playerEntitiesSize) {
            val entity = playerEntities[i] as Entity
            if (EntityUtils.isSelected(
                    entity,
                    false
                ) || localPlayer.get() && entity is EntityPlayerSP && mc.gameSettings.thirdPersonView != 0 || droppedItems.get() && entity is EntityItem
            ) {
                collectedEntities += entity
            }
            ++i
        }
    }

    private fun project2D(scaleFactor: Int, x: Double, y: Double, z: Double): Vector3d? {
        GL11.glGetFloat(2982, modelview)
        GL11.glGetFloat(2983, projection)
        GL11.glGetInteger(2978, viewport)
        return if (GLU.gluProject(
                x.toFloat(),
                y.toFloat(),
                z.toFloat(),
                modelview,
                projection,
                viewport,
                vector
            )
        ) Vector3d(
            (vector[0] / scaleFactor.toFloat()).toDouble(),
            ((Display.getHeight().toFloat() - vector[1]) / scaleFactor.toFloat()).toDouble(),
            vector[2].toDouble()
        ) else null
    }

    companion object {
        var collectedEntities: MutableList<Entity> = ArrayList<Entity>()
        @JvmStatic
        fun shouldCancelNameTag(entity: EntityLivingBase?): Boolean {
            return LiquidBounce.moduleManager.getModule(ESP2D::class.java) != null && LiquidBounce.moduleManager.getModule(
                ESP2D::class.java
            )!!.state && LiquidBounce.moduleManager.getModule(ESP2D::class.java)!!.tagsValue.get() && collectedEntities.contains(
                entity as Entity
            )
        }
    }
}