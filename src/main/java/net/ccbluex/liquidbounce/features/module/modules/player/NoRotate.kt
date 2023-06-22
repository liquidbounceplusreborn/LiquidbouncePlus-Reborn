/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.minecraft.network.play.client.C03PacketPlayer.C05PacketPlayerLook
import net.minecraft.network.play.server.S08PacketPlayerPosLook

@ModuleInfo(name = "NoRotate", spacedName = "No Rotate", description = "Prevents the server from rotating your head.", category = ModuleCategory.WORLD)
class NoRotate : Module() {

    val noLoadingValue = BoolValue("NoLoading", true)
    val overwriteTeleportValue = BoolValue("SilentConfirm", true)
    val rotateValue = BoolValue("SilentConfirmSetRotation", true, { overwriteTeleportValue.get() })
}