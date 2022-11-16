package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.zeroday;

public final class Translate {
    private float x;
    private float y;
    public boolean first = false;

    public Translate(float x, float y) {
        this.x = x;
        this.y = y;
    }
    public void translate(float targetX, float targetY, double speed) {
        x = AnimationUtil.animate(x, targetX, speed);
        y = AnimationUtil.animate(y, targetY, speed);
    }

    public final void interpolate(float targetX, float targetY, double smoothing) {
        if(first) {
            this.x = AnimationUtil.animate(targetX, this.x, smoothing);
            this.y = AnimationUtil.animate(targetY, this.y, smoothing);
        } else {
            this.x = targetX;
            this.y = targetY;
            first = true;
        }
    }
    public final void interpolate(float targetX, double smoothing) {
        if(first) {
            this.x = AnimationUtil.animate(targetX, this.x, smoothing);
        } else {
            this.x = targetX;
            first = true;
        }
    }
    public final void interpolate2(float targetX, float targetY, double smoothing) {
        this.x = targetX;
        this.y = AnimationUtil.animate(targetY, this.y, smoothing);
    }

    public float getX() {
        return this.x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return this.y;
    }

    public void setY(float y) {
        this.y = y;
    }
}

