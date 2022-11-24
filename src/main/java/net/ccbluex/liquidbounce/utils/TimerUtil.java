package net.ccbluex.liquidbounce.utils;

public class TimerUtil {
    private long lastMS;

    private long getCurrentMS() {
        return System.nanoTime() / 1000000L;
    }

    public boolean hasReached(double milliseconds) {
        if ((double) (this.getCurrentMS() - this.lastMS) >= milliseconds) {
            return true;
        }
        return false;
    }
    public boolean hasTimeElapsed(long time) {
        return System.currentTimeMillis() - lastMS > time;
    }
    public void reset() {
        this.lastMS = this.getCurrentMS();
    }

    public boolean delay(float milliSec) {
        if ((float) (this.getTime() - this.lastMS) >= milliSec) {
            return true;
        }
        return false;
    }

    public void setTime(final long time) {
        this.lastMS = time;
    }

    public long getTime() {
        return System.nanoTime() / 1000000L;
    }
}
