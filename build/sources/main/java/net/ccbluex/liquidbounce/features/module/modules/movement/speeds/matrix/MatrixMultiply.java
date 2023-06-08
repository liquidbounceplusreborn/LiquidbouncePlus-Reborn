package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.matrix;

import net.ccbluex.liquidbounce.event.MoveEvent;
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode;
import net.ccbluex.liquidbounce.utils.MovementUtils;

public class MatrixMultiply extends SpeedMode {

    public MatrixMultiply() {
        super("MatrixMultiply");
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
            return;
        }
        if (mc.thePlayer.onGround) {
            mc.timer.timerSpeed = 1.0f;
            mc.thePlayer.jump();
        }
        if (mc.thePlayer.motionY > 0.003) {
            mc.thePlayer.motionX *= 1.0012;
            mc.thePlayer.motionZ *= 1.0012;
            mc.timer.timerSpeed = 1.05f;
        }
    }

    @Override
    public void onUpdate() {
    }

    @Override
    public void onMove(MoveEvent event) {
    }
}
