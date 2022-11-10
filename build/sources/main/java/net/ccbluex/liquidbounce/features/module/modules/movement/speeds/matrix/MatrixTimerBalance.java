package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.matrix;

import net.ccbluex.liquidbounce.event.MoveEvent;
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode;
import net.ccbluex.liquidbounce.utils.MovementUtils;

public class MatrixTimerBalance extends SpeedMode {

    public MatrixTimerBalance() {
        super("MatrixTimerBalance");
    }

    @Override
    public void onEnable() {
        mc.thePlayer.jumpMovementFactor = 0.02f;
        mc.timer.timerSpeed = 1.0f;
    }

    @Override
    public void onDisable() {
        mc.thePlayer.jumpMovementFactor = 0.02f;
        mc.timer.timerSpeed = 1.0f;
    }

    @Override
    public void onMotion() {
        if (!MovementUtils.isMoving()) {
            mc.timer.timerSpeed = 1.0f;
            return;
        }
        if (mc.thePlayer.onGround) {
            mc.thePlayer.jump();
            return;
        }
        if (mc.thePlayer.fallDistance <= 0.1) {
            mc.timer.timerSpeed = 1.9f;
            return;
        }
        if (mc.thePlayer.fallDistance < 1.3) {
            mc.timer.timerSpeed = 0.6f;
            return;
        }
        mc.timer.timerSpeed = 1.0f;
    }

    @Override
    public void onUpdate() {
    }

    @Override
    public void onMove(MoveEvent event) {
    }
}
