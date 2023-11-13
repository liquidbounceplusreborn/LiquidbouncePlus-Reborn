package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.skeet.component;

public abstract class ButtonComponent extends Component {
    public ButtonComponent(Component parent, float x, float y, float width, float height) {
        super(parent, x, y, width, height);
    }

    @Override
    public void onMouseClick(int mouseX, int mouseY, int button) {
        if (this.isHovered(mouseX, mouseY)) {
            this.onPress(button);
        }
        super.onMouseClick(mouseX, mouseY, button);
    }

    public abstract void onPress(int button);
}

