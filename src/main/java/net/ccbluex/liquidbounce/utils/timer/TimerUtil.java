package net.ccbluex.liquidbounce.utils.timer;

public class TimerUtil {
    private long lastMS;

    private long getCurrentMS() {
        return System.nanoTime() / 1000000L;
    }

    public boolean hasReached(double milliseconds) {
        if ((double)(this.getCurrentMS() - this.lastMS) >= milliseconds) {
            return true;
        }
        return false;
    }

    public final long getElapsedTime() {
        return this.getCurrentMS() - this.lastMS;
    }

    public void reset() {
        this.lastMS = this.getCurrentMS();
    }

    public boolean delay(float milliSec) {
        if ((float)(this.getTime() - this.lastMS) >= milliSec) {
            return true;
        }
        return false;
    }

    public long getTime() {
        return System.nanoTime() / 1000000L;
    }

    public boolean hasTimeElapsed(long time) {
        return System.currentTimeMillis() - lastMS > time;
    }

    public boolean isDelayComplete(long delay) {
        if (System.currentTimeMillis() - this.lastMS > delay)
            return true;
        return false;
    }
}

