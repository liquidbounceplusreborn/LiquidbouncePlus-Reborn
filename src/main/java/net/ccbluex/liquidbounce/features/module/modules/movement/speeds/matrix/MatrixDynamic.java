package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.matrix;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.event.MoveEvent;
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura;
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode;
import net.ccbluex.liquidbounce.utils.MovementUtils;

public class MatrixDynamic extends SpeedMode {

    KillAura ka = LiquidBounce.moduleManager.getModule(KillAura.class);

    public MatrixDynamic() {
        super("MatrixDynamic");
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

        if (!ka.getHitable()) {
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
        } else {
            if (MovementUtils.isMoving() && mc.thePlayer.onGround) {
                mc.thePlayer.jump();
                MovementUtils.strafe(0.3f);
            }
            if (mc.thePlayer.fallDistance > 0.1) {
                MovementUtils.strafe(0.22f);
            }
        }
    }

    @Override
    public void onUpdate() {
    }

    @Override
    public void onMove(MoveEvent event) {
    }
}
