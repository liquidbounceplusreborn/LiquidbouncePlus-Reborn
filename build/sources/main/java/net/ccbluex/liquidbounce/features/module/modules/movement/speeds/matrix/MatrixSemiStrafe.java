package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.matrix;

import net.ccbluex.liquidbounce.event.MoveEvent;
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode;
import net.ccbluex.liquidbounce.utils.MovementUtils;

public class MatrixSemiStrafe extends SpeedMode {

    public MatrixSemiStrafe() {
        super("MatrixSemiStrafe");
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
        if (MovementUtils.isMoving() && mc.thePlayer.onGround) {
            mc.thePlayer.jump();
            MovementUtils.strafe(0.3f);
        }
        if (mc.thePlayer.fallDistance > 0.1) {
            MovementUtils.strafe(0.22f);
        }
    }

    @Override
    public void onUpdate() {
    }

    @Override
    public void onMove(MoveEvent event) {
    }
}
