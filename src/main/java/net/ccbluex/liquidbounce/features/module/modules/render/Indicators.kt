package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ResourceLocation
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

@ModuleInfo(name = "Indicators", description = "Indicators2", category = ModuleCategory.RENDER)
class Indicators : Module() {

    private val fireBall = BoolValue("FireBall", true)
    private val arrow = BoolValue("Arrow", true)
    private val pearl = BoolValue("Pearl", true)
    private val scaleValue = FloatValue("Scale", 0.7f, 0.65f, 1.25f)
    private val radiusValue = FloatValue("Radius", 50f, 15f, 150f)

    var distance = 0f
    lateinit var displayName : String

    fun drawArrow(x: Double, y: Double, angle: Double, size: Double, degrees: Double) {
        val arrowSize = size * 2
        val arrowX = x - arrowSize * cos(angle)
        val arrowY = y - arrowSize * sin(angle)
        val arrowAngle1 = angle + Math.toRadians(degrees)
        val arrowAngle2 = angle - Math.toRadians(degrees)
        RenderUtils.drawLine(
            x,
            y,
            arrowX + arrowSize * cos(arrowAngle1),
            arrowY + arrowSize * sin(arrowAngle1),
            size.toFloat(),
        )
        RenderUtils.drawLine(
            x,
            y,
            arrowX + arrowSize * cos(arrowAngle2),
            arrowY + arrowSize * sin(arrowAngle2),
            size.toFloat(),
        )
    }

    fun getRotations(eX: Double, eZ: Double, x: Double, z: Double): Double {
        val xDiff = eX - x
        val zDiff = eZ - z
        val yaw = -(atan2(xDiff, zDiff) * 57.29577951308232)
        return yaw
    }

    @EventTarget
    fun onRender2DEvent(event: Render2DEvent) {
        val t = ScaledResolution(mc)
        for (entity in mc.theWorld.loadedEntityList) {
            val name = entity.name
            if (name == "Fireball" || name == "Arrow" || name == "entity.ThrownEnderpearl.name") {
                distance = floor(mc.thePlayer.getDistanceToEntity(entity))
                displayName = if (name == "entity.ThrownEnderpearl.name") "Pearl" else name

                val scale = scaleValue.get()
                val entX = entity.posX
                val entZ = entity.posZ
                val px = mc.thePlayer.posX
                val pz = mc.thePlayer.posZ
                val pYaw = mc.thePlayer.rotationYaw
                val radius = radiusValue.get()
                val yaw = Math.toRadians(getRotations(entX, entZ, px, pz) - pYaw)
                val arrowX = t.scaledWidth / 2 + radius * sin(yaw)
                val arrowY = t.scaledHeight / 2 - radius * cos(yaw)
                val textX = t.scaledWidth / 2 + (radius - 13) * sin(yaw)
                val textY = t.scaledHeight / 2 - (radius - 13) * cos(yaw)
                val imgX = (t.scaledWidth / 2) + (radius - 18) * sin(yaw)
                val imgY = (t.scaledHeight / 2) - (radius - 18) * cos(yaw)
                val arrowAngle = atan2(arrowY - t.scaledHeight / 2, arrowX - t.scaledWidth / 2)
                drawArrow(arrowX, arrowY, arrowAngle, 3.0, 100.0)
                GlStateManager.color(255f, 255f, 255f, 255f)
                if (displayName == "Fireball" && fireBall.get()) {
                    GlStateManager.scale(scale, scale, scale)
                    RenderUtils.drawImage(
                        ResourceLocation("textures/items/fireball.png"),
                        imgX / scale - 5,
                        imgY / scale - 5,
                        32.toDouble(),
                        32.toDouble()
                    )
                    GlStateManager.scale(1 / scale, 1 / scale, 1 / scale)
                }
                if (displayName == "Pearl" && pearl.get()) {
                    GlStateManager.scale(scale, scale, scale)
                    RenderUtils.drawImage(
                        ResourceLocation("textures/items/ender_pearl.png"),
                        imgX / scale - 6,
                        imgY / scale - 5,
                        32.toDouble(),
                        32.toDouble()
                    )
                    GlStateManager.scale(1 / scale, 1 / scale, 1 / scale)
                }
                if (displayName == "Arrow" && arrow.get()) {
                    GlStateManager.scale(scale, scale, scale)
                    RenderUtils.drawImage(
                        ResourceLocation("textures/items/arrow.png"),
                        imgX / scale - 5,
                        imgY / scale - 5,
                        32.toDouble(),
                        32.toDouble()
                    )
                    GlStateManager.scale(1 / scale, 1 / scale, 1 / scale)
                }
                GlStateManager.scale(scale, scale, scale)
                Fonts.minecraftFont.drawStringWithShadow(
                    distance.toString() + "m",
                    (textX / scale - (Fonts.minecraftFont.getStringWidth(distance.toString() + "m") / 2)).toFloat(),
                    (textY / scale - 4).toFloat(),
                    -1
                )
                GlStateManager.scale(1 / scale, 1 / scale, 1 / scale)
            }
        }
    }
}