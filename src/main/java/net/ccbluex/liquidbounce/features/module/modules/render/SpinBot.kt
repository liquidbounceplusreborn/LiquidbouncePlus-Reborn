/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.features.module.*
import net.ccbluex.liquidbounce.value.*
import net.ccbluex.liquidbounce.event.*

@ModuleInfo(name = "SpinBot", spacedName = "Spin Bot", description = "Client-sided spin bot like CS:GO hacks.", category = ModuleCategory.RENDER)
class SpinBot : Module() {
    private val yawMode = ListValue("Yaw", arrayOf("Static", "Offset", "Random", "Jitter", "Spin", "None"), "Offset")
    val pitchMode = ListValue("Pitch", arrayOf("Static", "Offset", "Random", "Jitter", "None"), "Offset")
    private val static_offsetYaw = FloatValue("Static/Offset-Yaw", 0F, -180F, 180F, "°")
    private val static_offsetPitch = FloatValue("Static/Offset-Pitch", 0F, -90F, 90F, "°")
    private val yawJitterTimer = IntegerValue("YawJitterTimer", 1, 1, 40, " tick(s)")
    private val pitchJitterTimer = IntegerValue("PitchJitterTimer", 1, 1, 40, " tick(s)")
    private val yawSpinSpeed = FloatValue("YawSpinSpeed", 5F, -90F, 90F, "°")

    var pitch = 0F
    private var lastSpin = 0F
    private var yawTimer = 0
    private var pitchTimer = 0

    override fun onDisable() {
        pitch = -4.9531336E7f
        lastSpin = 0.0f
        yawTimer = 0
        pitchTimer = 0
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        mc.thePlayer ?: return
        
        if (!yawMode.get().equals("none", true)) {
            var yaw = 0F
            when (yawMode.get().toLowerCase()) {
                "static" -> yaw = static_offsetYaw.get()
                "offset" -> yaw = mc.thePlayer.rotationYaw + static_offsetYaw.get()
                "random" -> yaw = Math.floor(Math.random() * 360.0 - 180.0).toFloat()
                "jitter" -> {
                    if (yawTimer++ % (yawJitterTimer.get() * 2) >= yawJitterTimer.get())
                        yaw = mc.thePlayer.rotationYaw
                    else
                        yaw = mc.thePlayer.rotationYaw - 180F
                }
                "spin" -> {
                    lastSpin += yawSpinSpeed.get()
                    yaw = lastSpin
                }
            }
            mc.thePlayer.renderYawOffset = yaw
            mc.thePlayer.rotationYawHead = yaw
            lastSpin = yaw
        }
        when (pitchMode.get().toLowerCase()) {
            "static" -> pitch = static_offsetPitch.get()
            "offset" -> pitch = mc.thePlayer.rotationPitch + static_offsetPitch.get()
            "random" -> pitch = Math.floor(Math.random() * 180.0 - 90.0).toFloat()
            "jitter" -> {
                if (pitchTimer++ % (pitchJitterTimer.get() * 2) >= pitchJitterTimer.get())
                    pitch = 90F
                else
                    pitch = -90F
            }
        }
    }

    override val tag: String?
        get() = "${yawMode.get()}, ${pitchMode.get()}"
}
