package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.skeet.component.impl.sub.button;

import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.skeet.LockedResolution;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.skeet.SkeetClickGUI;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.skeet.SkeetUtils;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.skeet.component.ButtonComponent;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.skeet.component.Component;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;

import java.util.function.Consumer;

public final class ButtonComponentImpl extends ButtonComponent {
    private final String text;
    private final Consumer<Integer> onPress;

    public ButtonComponentImpl(Component parent, String text, Consumer<Integer> onPress, float width, float height) {
        super(parent, 0.0f, 0.0f, width, height);
        this.text = text;
        this.onPress = onPress;
    }

    @Override
    public void drawComponent(LockedResolution lockedResolution, int mouseX, int mouseY) {
        float x = this.getX();
        float y = this.getY();
        float width = this.getWidth();
        float height = this.getHeight();
        boolean hovered = this.isHovered(mouseX, mouseY);
        RenderUtils.drawRect(x, y, x + width, y + height, SkeetClickGUI.getColor(0x111111));
        RenderUtils.drawRect(x + 0.5f, y + 0.5f, x + width - 0.5f, y + height - 0.5f, SkeetClickGUI.getColor(0x262626));
        SkeetUtils.drawGradientRect(x + 1.0f, y + 1.0f, x + width - 1.0f, y + height - 1.0f, false, SkeetClickGUI.getColor(hovered ? SkeetUtils.darker(0x222222, 1.2f) : 0x222222), SkeetClickGUI.getColor(hovered ? SkeetUtils.darker(0x1E1E1E, 1.2f) : 0x1E1E1E));
        if (SkeetClickGUI.shouldRenderText()) {
            SkeetClickGUI.FONT_RENDERER.drawString(this.text, x + width / 2.0f - SkeetClickGUI.FONT_RENDERER.getWidth(this.text) / 2.0f, y + height / 2.0f - 4f, SkeetClickGUI.getColor(0xFFFFFF));
        }
    }

    @Override
    public void onPress(int mouseButton) {
        this.onPress.accept(mouseButton);
    }
}

