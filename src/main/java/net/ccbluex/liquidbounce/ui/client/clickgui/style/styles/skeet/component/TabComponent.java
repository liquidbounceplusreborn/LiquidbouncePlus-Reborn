
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.skeet.component;

import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.skeet.*;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public abstract class TabComponent
extends Component {
    public final String name;
    public float highest = 0;
    public float scrollY = 0;
    public float scrollAni = 0;
    public float minY = -100;
    public float barHeight = 20;

    public TabComponent(Component parent, String name, float x, float y, float width, float height) {
        super(parent, x, y, width, height);
        this.setupChildren();
        this.name = name;
    }

    public abstract void setupChildren();

    @Override
    public void drawComponent(LockedResolution resolution, int mouseX, int mouseY) {
        SkeetClickGUI.FONT_RENDERER.drawStringWithShadow(this.name, this.getX() + 6.0f, this.getY() + 3f, SkeetClickGUI.getColor(0xFFFFFF));
        float x = 8.0f;
        int i = 0;

        if(RenderUtil.isHoveringBound(mouseX, mouseY, this.getX() + 2, this.getY() + 11, this.getWidth() - 4, this.getHeight() - 13)) {
            minY = getHeight() - 24;
        }

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GuiRenderUtils.doGlScissor((int) this.getX() + 2, (int) this.getY() + 11, (int) this.getWidth() - 4, (int) this.getHeight() - 13, 2f);
        while (i < this.children.size()) {
            Component child = this.children.get(i);
            child.setX(x);
            if (i < 3) {
                child.setY(14.0f + this.scrollAni);
            }

            child.drawComponent(resolution, mouseX, mouseY);

            x += child.getWidth() + 8;
            if (x + 8f + child.getWidth() > this.getWidth()) {
                x = 8.0f;
            }
            if (i > 2) {
                int above = i - 3;
                float totalY = 14;

                do {
                    Component componentAbove = this.getChildren().get(above);
                    totalY = totalY + (componentAbove.getHeight() + 8.0f);
                } while ((above -= 3) >= 0);

                if (totalY > this.highest) {
                    this.highest = totalY;
                }

                child.setY(totalY + this.scrollAni);
            }

            ++i;
        }
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        if(RenderUtil.isHoveringBound(mouseX, mouseY, this.getX() + 2, this.getY() + 11, this.getWidth() - 4, this.getHeight() - 13)) {
            minY -= this.highest;
        }

        if(this.highest > this.getHeight() - 13) {
            if(!RenderUtil.isHoveringBound(mouseX, mouseY, this.getX() + 2, this.getY() + 11, this.getWidth() - 4, this.getHeight() - 13)) {
                Mouse.getDWheel(); //刷新滚轮
            }
            this.scrollAni = AnimationUtils.smoothAnimation(this.scrollAni, scrollY, 50, 0.3f);

            float viewable = this.getHeight() - 13;
            float progress = MathUtils.clampValue(-this.scrollAni / -this.minY, 0, 1);
            float ratio = (viewable / this.highest) * viewable;
            this.barHeight = Math.max(ratio, 20f);

            float position = progress * (viewable - barHeight);
            if(SkeetClickGUI.alpha > 70) {
                RenderUtil.drawRect(this.getX() + this.getWidth() - 6, this.getY() + 11, this.getX() + this.getWidth() - 3, this.getY() + this.getHeight() - 2f, 0xff323232);
                RenderUtil.drawRect(this.getX() + this.getWidth() - 6, this.getY() + 11 + position, this.getX() + this.getWidth() - 3, this.getY() + 11 + position + this.barHeight, 0xff474747);
                RenderUtil.drawOutlinedRect(this.getX() + this.getWidth() - 6, this.getY() + 11, this.getX() + this.getWidth() - 3, this.getY() + this.getHeight() - 2f, .5f, 0xff000000);
            }
        }

    }

    @Override
    public void onMouseClick(int mouseX, int mouseY, int button) {
        if(RenderUtil.isHoveringBound(mouseX, mouseY, this.getX() + 2, this.getY() + 11, this.getWidth() - 4, this.getHeight() - 13)) {
            for (Component child : this.children) {
                child.onMouseClick(mouseX, mouseY, button);
            }
        }
    }

    @Override
    public void onMouseScroll(int mouseX, int mouseY, int value) {
        if(RenderUtil.isHoveringBound(mouseX, mouseY, this.getX() + 2, this.getY() + 11, this.getWidth() - 4, this.getHeight() - 13)) {
            scrollY += value / 6f;
            if (scrollY <= minY)
                scrollY = minY;
            if (scrollY >= 0f)
                scrollY = 0f;
        }

        for (Component child : this.children) {
            child.onMouseScroll(mouseX, mouseY, value);
        }
    }
}

