package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.akiren;

import net.ccbluex.liquidbounce.utils.misc.Direction;
import net.ccbluex.liquidbounce.utils.render.Animation;

;

public class DecelerateAnimation extends Animation {

    public DecelerateAnimation(int ms, double endPoint) {
        super(ms, endPoint);
    }

    public DecelerateAnimation(int ms, double endPoint, Direction direction) {
        super(ms, endPoint, direction);
    }

    protected double getEquation(double x) {
        double x1 = x / duration;
        return 1 - ((x1 - 1) * (x1 - 1));
    }
}
