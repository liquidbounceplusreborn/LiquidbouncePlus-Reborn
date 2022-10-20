package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.flux;

import net.ccbluex.liquidbounce.value.*;
import net.ccbluex.liquidbounce.features.module.*;

public class AnimationHelper
{
    public float animationX;
    public int alpha;

    public int getAlpha() {
        return this.alpha;
    }

    public float getAnimationX() {
        return this.animationX;
    }

    public void resetAlpha() {
        this.alpha = 0;
    }

    public AnimationHelper() {
        this.alpha = 0;
    }

    public void updateAlpha(final int speed) {
        if (this.alpha < 255) {
            this.alpha += speed;
        }
    }

    public AnimationHelper(final BoolValue value) {
        this.animationX = (value.get() ? 5.0f : -5.0f);
    }

    public AnimationHelper(final Module module) {
        this.animationX = (module.getState() ? 5.0f : -5.0f);
    }
}
