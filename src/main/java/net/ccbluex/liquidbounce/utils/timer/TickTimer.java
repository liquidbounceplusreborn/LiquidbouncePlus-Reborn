/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.utils.timer;

public final class TickTimer {

    public int tick;

    public void update() {
        tick++;
    }

    public void reset() {
        tick = 0;
    }

    public boolean hasTimePassed(final int ticks) {
        return tick >= ticks;
    }
}
