/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.aac;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.event.*;
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed;
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode;
import net.ccbluex.liquidbounce.utils.MovementUtils;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.network.play.server.S12PacketEntityVelocity;

public class AACHop3310 extends SpeedMode {

    public AACHop3310() {
        super("AACHop3.3.10");
    }

    @Override
    public void onMotion() {

    }


    @Override
    public void onUpdate() {


    }

    @Override
    public void onMove(final MoveEvent event) {

        final Speed speed = LiquidBounce.moduleManager.getModule(Speed.class);
        if(speed == null) return;

        EntityPlayerSP player = mc.thePlayer;
        mc.gameSettings.keyBindJump.pressed = false;
        MovementUtils.strafe((float) (MovementUtils.getBaseMoveSpeed() * 1.0164f));


        if (mc.thePlayer.onGround && mc.thePlayer.isCollidedVertically && GameSettings.isKeyDown(mc.gameSettings.keyBindSneak)) {
            event.setY(player.motionY = MovementUtils.getJumpBoostModifier(0.41999998688697815D));
        }

        if (mc.thePlayer.onGround && mc.thePlayer.isCollidedVertically && !GameSettings.isKeyDown(mc.gameSettings.keyBindSneak)) {
            event.setY(player.motionY = MovementUtils.getJumpBoostModifier(0.41999998688697815D));
        }

    }

    @Override
    public void onDisable() {
    }

    public void onPacket(PacketEvent event) {

        if (event.getPacket() instanceof S12PacketEntityVelocity) {
            if (mc.thePlayer.onGround && mc.thePlayer.isSneaking() && MovementUtils.isMoving())
                return;

            event.cancelEvent();
        }
    }
}
