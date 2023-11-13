package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.skeet.component.impl.sub.comboBox;

import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.skeet.LockedResolution;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.skeet.RenderUtil;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.skeet.SkeetClickGUI;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.skeet.SkeetUtils;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.skeet.component.ButtonComponent;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.skeet.component.Component;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.skeet.component.ExpandableComponent;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.skeet.component.PredicateComponent;
import net.ccbluex.liquidbounce.ui.font.GameFontRenderer;
import net.ccbluex.liquidbounce.ui.font.TTFFontRenderer;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import org.lwjgl.opengl.GL11;

public abstract class ComboBoxComponent extends ButtonComponent implements PredicateComponent, ExpandableComponent {
    private boolean expanded;

    public ComboBoxComponent(Component parent, float x, float y, float width, float height) {
        super(parent, x, y, width, height);
    }

    private String getDisplayString() {
        return this.getValue();
    }

    @Override
    public void drawComponent(LockedResolution lockedResolution, int mouseX, int mouseY) {
        float x = this.getX();
        float y = this.getY();
        float width = this.getWidth();
        float height = this.getHeight();
        RenderUtils.drawRect(x, y, x + width, y + height, SkeetClickGUI.getColor(855309));
        boolean hovered = this.isHovered(mouseX, mouseY);
        SkeetUtils.drawGradientRect(x + 0.5f, y + 0.5f, x + width - 0.5f, y + height - 0.5f, false, SkeetClickGUI.getColor(hovered ? SkeetUtils.darker(0x1E1E1E, 1.4f) : 0x1E1E1E), SkeetClickGUI.getColor(hovered ? SkeetUtils.darker(0x232323, 1.4f) : 0x232323));
        GL11.glColor4f((float) 0.6f, (float) 0.6f, (float) 0.6f, (float) ((float) SkeetClickGUI.getAlpha() / 255.0f));
        SkeetUtils.drawAndRotateArrow(x + width - 5.0f, y + height / 2.0f - 0.5f, 3.0f, this.isExpanded());

        if (SkeetClickGUI.shouldRenderText()) {
            SkeetClickGUI.FONT_RENDERER.drawString(this.getDisplayString(), x + 2.0f, y + height / 3.0f - 2f, SkeetClickGUI.getColor(0x969696));
        }

        GL11.glTranslatef((float) 0.0f, (float) 0.0f, (float) 2.0f);
        if (this.expanded) {
            String[] values = this.getValues();
            float dropDownHeight = (float)values.length * height;
            RenderUtils.drawRect(x, y + height, x + width, y + height + dropDownHeight + 0.5f, SkeetClickGUI.getColor(855309));
            float valueBoxHeight = height;

            for (String value : values) {
                boolean valueBoxHovered = (float) mouseX >= x && (float) mouseY >= y + valueBoxHeight && (float) mouseX <= x + width && (float) mouseY < y + valueBoxHeight + height;
                RenderUtil.drawRect(x + 0.5f, y + valueBoxHeight, x + width - 0.5f, y + valueBoxHeight + height, SkeetClickGUI.getColor(valueBoxHovered ? SkeetUtils.darker(0x232323, 0.7f) : 0x232323));
                boolean selected = value.equals(this.getValue());
                int color = selected ? SkeetClickGUI.getColor() : SkeetClickGUI.getColor(0xDCDCDC);
                TTFFontRenderer fr = selected || valueBoxHovered ? SkeetClickGUI.GROUP_BOX_HEADER_RENDERER : SkeetClickGUI.FONT_RENDERER;
                fr.drawString(value, x + 2.0f, y + valueBoxHeight + 1.5f, color);
                valueBoxHeight += height;
            }
        }
        GL11.glTranslatef((float) 0.0f, (float) 0.0f, (float) -2.0f);
    }

    @Override
    public void onMouseClick(int mouseX, int mouseY, int button) {
        if (this.isHovered(mouseX, mouseY)) {
            this.onPress(button);
        }

        if (this.expanded && button == 0) {
            float x = this.getX();
            float y = this.getY();
            float height = this.getHeight();
            float width = this.getWidth();
            float valueBoxHeight = height;
            for(String value : this.getValues()) {

                if ((float) mouseX >= x && (float) mouseY >= y + valueBoxHeight && (float) mouseX <= x + width && (float) mouseY < y + valueBoxHeight + height) {
                    this.setValue(value);
                    this.expandOrClose();
                    break;
                }

                valueBoxHeight += height;
            }
        }
    }

    private void expandOrClose() {
        this.setExpanded(!this.isExpanded());
    }

    @Override
    public void onPress(int mouseButton) {
        if (mouseButton == 0) {
            this.expandOrClose();
        }
    }

    @Override
    public float getExpandedX() {
        return this.getX();
    }

    @Override
    public float getExpandedY() {
        return this.getY();
    }

    public abstract String getValue();

    public abstract String[] getValues();

    public abstract void setValue(String value);

    @Override
    public boolean isExpanded() {
        return this.expanded;
    }

    @Override
    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    @Override
    public float getExpandedWidth() {
        return this.getWidth();
    }

    @Override
    public float getExpandedHeight() {
        return (float) this.getValues().length * this.getHeight();
    }
}

