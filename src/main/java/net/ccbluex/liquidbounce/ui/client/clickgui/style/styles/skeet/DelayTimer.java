package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.skeet;

public class DelayTimer {
	private long prevTime;

	public DelayTimer() {
		this.reset();
	}

	public DelayTimer(long def) {
		this.prevTime = System.currentTimeMillis() - def;
	}

	public boolean hasPassed(double milli) {
		return System.currentTimeMillis() - this.prevTime >= milli;
	}

	public void reset() {
		this.prevTime = System.currentTimeMillis();
	}

	public long getPassed(){
		return System.currentTimeMillis() - this.prevTime;
	}

    public void reset(long def) {
        this.prevTime = System.currentTimeMillis() - def;
    }

	public boolean isDelayComplete(double d) {
		return this.hasPassed(d);
	}

   /* public void setTime(long mili) {
        this.prevTime -= mili;
    }*/
}
