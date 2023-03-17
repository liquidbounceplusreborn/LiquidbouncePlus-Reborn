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
        final Speed speed = LiquidBounce.moduleManager.getModule(Speed.class);
        if (MovementUtils.isMoving()) {
            mc.timer.timerSpeed = speed.wdHAirTimerValue.get();
            if (mc.thePlayer.onGround) {
                if (groundTick >= speed.wdGroundStay.get()) {
                    mc.timer.timerSpeed = speed.wdGroundTimer.get();
                    MovementUtils.strafe(0.42f);
                    mc.thePlayer.motionY = MovementUtils.getJumpBoostModifier(0.41999998688698, true);
                    if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                        MovementUtils.strafe(0.57f);
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
                mc.thePlayer.jumpMovementFactor = speed.customSpeed2Boost.get();
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
