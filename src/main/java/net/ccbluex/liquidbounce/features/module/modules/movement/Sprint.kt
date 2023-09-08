/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.Rotation
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.minecraft.network.play.client.C0BPacketEntityAction
import net.minecraft.potion.Potion

@ModuleInfo(name = "Sprint", description = "Automatically sprints all the time.", category = ModuleCategory.MOVEMENT)
class Sprint : Module() {

    val allDirectionsValue = BoolValue("AllDirections", true)
    val noPacketPatchValue = BoolValue("AllDir-NoPacketsPatch", true, { allDirectionsValue.get() })
    val moveDirPatchValue = BoolValue("AllDir-MoveDirPatch", false, { allDirectionsValue.get() })
    val blindnessValue = BoolValue("Blindness", true)
    val foodValue = BoolValue("Food", true)

    val checkServerSide = BoolValue("CheckServerSide", false)
    val checkServerSideGround = BoolValue("CheckServerSideOnlyGround", false)

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (allDirectionsValue.get() && noPacketPatchValue.get()) {
            if (packet is C0BPacketEntityAction && (packet.getAction() == C0BPacketEntityAction.Action.STOP_SPRINTING || packet.getAction() == C0BPacketEntityAction.Action.START_SPRINTING)) {
                event.cancelEvent()
            }
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        val killAura = LiquidBounce.moduleManager.getModule(KillAura::class.java)!! as KillAura

        if (!MovementUtils.isMoving() || mc.thePlayer.isSneaking() ||
                (blindnessValue.get() && mc.thePlayer.isPotionActive(Potion.blindness)) ||
                (foodValue.get() && !(mc.thePlayer.getFoodStats().getFoodLevel() > 6.0F || mc.thePlayer.capabilities.allowFlying))
                || (checkServerSide.get() && (mc.thePlayer.onGround || !checkServerSideGround.get())
                && !allDirectionsValue.get() && RotationUtils.targetRotation != null &&
                RotationUtils.getRotationDifference(Rotation(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch)) > 30F)) {
            mc.thePlayer.setSprinting(false)
            return
        }

        if (allDirectionsValue.get() || mc.thePlayer.movementInput.moveForward >= 0.8F)
            mc.thePlayer.setSprinting(true)

        if (allDirectionsValue.get() && moveDirPatchValue.get() && killAura.target == null)
            RotationUtils.setTargetRotation(Rotation(MovementUtils.getRawDirection(), mc.thePlayer.rotationPitch))
    }

}
