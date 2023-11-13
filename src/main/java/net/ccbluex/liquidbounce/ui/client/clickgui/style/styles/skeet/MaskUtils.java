package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.skeet;

import static org.lwjgl.opengl.GL11.*;

/**
 * A utility to provide full screen masking
 */
public class MaskUtils {
    /**
     * Start defining the screen mask. After calling this use graphics functions to
     * mask out the area
     */
    public static void defineMask() {
        glDepthMask(true);
        glClearDepth(1);
        glClear(GL_DEPTH_BUFFER_BIT);
        glDepthFunc(GL_ALWAYS);
        glEnable(GL_DEPTH_TEST);
        glDepthMask(true);
        glColorMask(false, false, false, false);
    }

    /**
     * Finish defining the screen mask
     */
    public static void finishDefineMask() {
        glDepthMask(false);
        glColorMask(true, true, true, true);
    }

    /**
     * Start drawing only on the masked area
     */
    public static void drawOnMask() {
        glDepthFunc(GL_EQUAL);
    }

    /**
     * Start drawing only off the masked area.
     */
    public static void drawOffMask() {
        glDepthFunc(GL_NOTEQUAL);
    }

    /**
     * Start drawing only minecraft masked area.
     */
    public static void drawMCMask() {
        glDepthFunc(GL_LEQUAL);
    }

    /**
     * Reset the masked area - should be done after you've finished rendering
     */
    public static void resetMask() {
        glDepthMask(true);
        glClearDepth(1);
        glClear(GL_DEPTH_BUFFER_BIT);
        drawMCMask();
        glDepthMask(false);
        glDisable(GL_DEPTH_TEST);
    }
}