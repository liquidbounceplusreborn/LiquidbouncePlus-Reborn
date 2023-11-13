package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.skeet;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

public class MathUtils {
	public static float clampValue(final float value, final float floor, final float cap) {
		if (value < floor) {
			return floor;
		}
		return Math.min(value, cap);
	}

}
