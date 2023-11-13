package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.skeet.component.impl;

import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.skeet.*;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.skeet.component.Component;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.skeet.component.ScrollableListComponent;
import net.ccbluex.liquidbounce.ui.font.TTFFontRenderer;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ScrollListComponent extends ScrollableListComponent {

    public ArrayList<String> elements;
    public Supplier<ArrayList<String>> allElements;
    public String selected = "";
    public Consumer<String> onSelected;

    public float scrollY = 0;
    public float scrollAni = 0;
    public float minY = -100;
    public float barHeight = 20;

    public ScrollListComponent(Component parent, float x, float y, float width, float height) {
        super(parent, x, y, width, height);
    }

    public ScrollListComponent(Component parent, Supplier<ArrayList<String>> elements, Consumer<String> onSelected, float width, float height) {
        super(parent, 0.0f, 0.0f, width, height);
        this.allElements = elements;
        this.elements = allElements != null ? allElements.get() : new ArrayList<>();
        this.onSelected = onSelected;
    }

    @Override
    public void drawComponent(LockedResolution resolution, int mouseX, int mouseY) {
        RenderUtil.drawRect(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), 0xff232323);
        RenderUtil.drawOutlinedRect(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), .5f, 0xff000000);

        minY = this.getHeight();
        this.scrollAni = AnimationUtils.smoothAnimation(this.scrollAni, scrollY, 50, 0.3f);
        float startY = this.getY() + .5f + this.scrollAni;
        float yShouldbe = 0;

        MaskUtils.defineMask();
        RenderUtil.drawRect(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight() - .5f, 0xff000000);
        MaskUtils.finishDefineMask();
        MaskUtils.drawOnMask();

            for(String element : this.elements) {
                boolean isHovered = RenderUtil.isHovering(mouseX, mouseY, this.getX(), startY, this.getX() + this.getWidth() - .5f, startY + 10f);
                int color = selected.equals(element) ? SkeetClickGUI.getColor() : SkeetClickGUI.getColor(0xDCDCDC);
                TTFFontRenderer fr = isHovered || selected.equals(element) ? SkeetClickGUI.GROUP_BOX_HEADER_RENDERER : SkeetClickGUI.FONT_RENDERER;

                if(isHovered) {
                    RenderUtil.drawRect(this.getX() + .5f, startY, this.getX() + this.getWidth() - .5f, startY + 10f,0xff1a1a1a);
                }

                fr.drawString(element, this.getX() + 4, startY + 1.5f, color);
                startY += 10f;
                yShouldbe += 10f;
            }

            minY -= yShouldbe;
            if(yShouldbe > this.getHeight()) {
                float viewable = this.getHeight();
                float progress = MathUtils.clampValue(-this.scrollAni / -this.minY, 0, 1);
                float ratio = (viewable / yShouldbe) * viewable;
                this.barHeight = Math.max(ratio, 20f);

                float position = progress * (viewable - barHeight);

                if(SkeetClickGUI.alpha > 70) {
                    RenderUtil.drawRect(this.getX() + this.getWidth() - 4f, this.getY() + .5f, this.getX() + this.getWidth() - .5f, this.getY() + this.getHeight() - .5f,0xff2d2d2d);
                    RenderUtil.drawRect(this.getX() + this.getWidth() - 3.5f, this.getY() + 1 + position, this.getX() + this.getWidth() - 1f, this.getY() + position + barHeight - 1,0xff434343);
                }
            }

        MaskUtils.resetMask();
    }

    @Override
    public void onMouseClick(int mouseX, int mouseY, int button) {
            float startY = this.getY() + .5f + this.scrollAni;
            for(String element : this.elements) {
                boolean isHovered = RenderUtil.isHovering(mouseX, mouseY, this.getX(), startY, this.getX() + this.getWidth() - .5f, startY + 10f);
                if(isHovered && button == 0) {
                    this.selected = element;
                    this.onSelected.accept(this.selected);
                    break;
                }
                startY += 10f;
            }
    }

    @Override
    public void onMouseScroll(int mouseX, int mouseY, int value) {
        if(RenderUtil.isHovering(mouseX, mouseY, this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight())) {
            scrollY += value / 6f;
            if (scrollY <= minY)
                scrollY = minY;
            if (scrollY >= 0f)
                scrollY = 0f;
        }
    }
}
