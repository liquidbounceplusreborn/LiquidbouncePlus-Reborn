package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.akiren.component;

import net.minecraft.client.gui.ScaledResolution;

public abstract class DraggablePanel extends ExpandableComponent {

    private boolean dragging;

    private int prevX;
    private int prevY;

    public DraggablePanel(Component parent, String name, int x, int y, int width, int height) {
        super(parent, name, x, y, width, height);
        prevX = x;
        prevY = y;
    }

    @Override
    public void drawComponent(ScaledResolution scaledResolution, int mouseX, int mouseY) {
        if (dragging) {
            setX(mouseX - prevX);
            setY(mouseY - prevY);
        }
    }

    @Override
    public void onPress(int mouseX, int mouseY, int button) {
        if (button == 0) {
            dragging = true;
            prevX = mouseX - getX();
            prevY = mouseY - getY();
        }
    }

    @Override
    public void onMouseRelease(int button) {
        super.onMouseRelease(button);
        dragging = false;
    }
}
