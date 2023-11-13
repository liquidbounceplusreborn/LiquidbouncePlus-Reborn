package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.skeet;


public class TimeHelper {
    public void setLastMs(long lastMs) {
        this.lastMs = lastMs;
    }

    public long lastMs;

    public TimeHelper() {
        this.lastMs = 0L;
    }

    public void reset() {
        this.lastMs = System.currentTimeMillis();
    }

    public boolean delay(long nextDelay) {
        return System.currentTimeMillis() - lastMs >= nextDelay;
    }

    public boolean delay(float nextDelay, boolean reset) {
        if (System.currentTimeMillis() - lastMs >= nextDelay) {
            if (reset) {
                this.reset();
            }
            return true;
        }
        return false;
    }

    public boolean isDelayComplete(double valueState) {
        return System.currentTimeMillis() - lastMs >= valueState;
    }

    public long getElapsedTime() {
        return System.currentTimeMillis() - this.lastMs;
    }

    public long getLastMs() {
        return lastMs;
    }
}

