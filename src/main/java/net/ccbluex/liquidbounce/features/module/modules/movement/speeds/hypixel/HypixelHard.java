package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.hypixel;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.event.MoveEvent;
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed;
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode;
import net.ccbluex.liquidbounce.utils.MovementUtils;
import net.minecraft.potion.Potion;

public class HypixelHard extends SpeedMode {

    public HypixelHard() {
        super("HypixelHard");
    }
    private int groundTick;

    @Override
    public void onUpdate(){
        if (MovementUtils.isMoving()) {
            mc.timer.timerSpeed = 1.2f;
            if (mc.thePlayer.onGround) {
                if (groundTick >= 1) {
                    mc.timer.timerSpeed = 2.0f;
                    MovementUtils.strafe(0.43f);
                    mc.thePlayer.motionY = MovementUtils.getJumpBoostModifier(0.41999998688698, true);
                    if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                        MovementUtils.strafe(0.63f);
                    }
                }
                groundTick++;
            } else{
                groundTick = 0;
            }
            if (mc.thePlayer.hurtTime > 0 || mc.thePlayer.fallDistance > 0.0) {
                MovementUtils.strafe();
            }
            if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                mc.thePlayer.jumpMovementFactor = 0.026f;
            }
        }
    }

    @Override
    public void onMotion() {

    }
    @Override
    public void onMove(MoveEvent event) {

    }
}