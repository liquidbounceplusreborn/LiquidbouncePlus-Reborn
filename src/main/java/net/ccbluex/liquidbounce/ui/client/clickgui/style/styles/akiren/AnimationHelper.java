package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.akiren;
import net.ccbluex.liquidbounce.utils.MinecraftInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.util.MathHelper;

public class AnimationHelper extends MinecraftInstance {
    public static double delta;

    public static float animation(float animation, float target, float speedTarget) {
        float dif = (target - animation) / Math.max((float) Minecraft.getDebugFPS(), 5) * 15;

        if (dif > 0) {
            dif = Math.max(speedTarget, dif);
            dif = Math.min(target - animation, dif);
        } else if (dif < 0) {
            dif = Math.min(-speedTarget, dif);
            dif = Math.max(target - animation, dif);
        }
        return animation + dif;
    }
    public static double animation(double animation, double target, double speedTarget) {
        double dif = (target - animation) / Math.max(Minecraft.getDebugFPS(), 5) * speedTarget;
        if (dif > 0.0D) {
            dif = Math.max(speedTarget, dif);
            dif = Math.min(target - animation, dif);
        } else if (dif < 0.0D) {
            dif = Math.min(-speedTarget, dif);
            dif = Math.max(target - animation, dif);
        }
        return animation + dif;
    }
    public static float calculateCompensation(float target, float current, float delta, double speed) {
        float diff = current - target;
        if (delta < 1.0f) {
            delta = 1.0f;
        }
        if (delta > 1000.0f) {
            delta = 16.0f;
        }
        double dif = Math.max(speed * (double)delta / 16.66666603088379, 0.5);
        if ((double)diff > speed) {
            if ((current = (float)((double)current - dif)) < target) {
                current = target;
            }
        } else if ((double)diff < -speed) {
            if ((current = (float)((double)current + dif)) > target) {
                current = target;
            }
        } else {
            current = target;
        }
        return current;
    }
    public static float Move(float from, float to, float minstep, float maxstep, float factor) {

        float f = (to - from) * MathHelper.clamp_float(factor,0,1);

        if (f < 0)
            f = MathHelper.clamp_float(f, -maxstep, -minstep);
        else
            f = MathHelper.clamp_float(f, minstep, maxstep);

        if(Math.abs(f) > Math.abs(to - from))
            return to;

        return from + f;
    }
    public static double Interpolate(double current, double old, double scale) {
        return old + (current - old) * scale;
    }
    public static float getAnimationState(float animation, float finalState, float speed) {
        final float add = (float) (delta * (speed / 1000f));
        if (animation < finalState) {
            if (animation + add < finalState) {
                animation += add;
            } else {
                animation = finalState;
            }
        } else if (animation - add > finalState) {
            animation -= add;
        } else {
            animation = finalState;
        }
        return animation;
    }
}
