package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.skeet.component.impl.sub.text;

import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.skeet.LockedResolution;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.skeet.SkeetClickGUI;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.skeet.component.Component;
import net.ccbluex.liquidbounce.ui.font.GameFontRenderer;
import net.ccbluex.liquidbounce.ui.font.TTFFontRenderer;

public final class TextComponent extends Component {
    private static final TTFFontRenderer FONT_RENDERER = SkeetClickGUI.FONT_RENDERER;
    private final String text;

    public TextComponent(Component parent, String text, float x, float y) {
        super(parent, x, y, FONT_RENDERER.getWidth(text), FONT_RENDERER.getHeight(text));
        this.text = text;
    }

    @Override
    public void drawComponent(LockedResolution resolution, int mouseX, int mouseY) {
        if (SkeetClickGUI.shouldRenderText()) {
            FONT_RENDERER.drawString(this.text, this.getX(), this.getY(), SkeetClickGUI.getColor(0xE6E6E6));
        }
    }
}

