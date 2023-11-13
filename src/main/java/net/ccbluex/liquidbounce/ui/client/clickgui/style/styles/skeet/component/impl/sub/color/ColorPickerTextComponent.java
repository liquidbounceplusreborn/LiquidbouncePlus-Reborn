package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.skeet.component.impl.sub.color;


import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.skeet.component.Component;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.skeet.component.ExpandableComponent;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.skeet.component.PredicateComponent;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.skeet.component.impl.sub.text.TextComponent;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ColorPickerTextComponent extends Component implements PredicateComponent, ExpandableComponent {
    private final ColorPickerComponent colorPicker;
    private final TextComponent textComponent;
    public String valID;

    public ColorPickerTextComponent(Component parent, String text, final Supplier<Integer> getColor, final Consumer<Integer> setColor, final Supplier<Boolean> isVisible, float x2, float y2) {
        super(parent, x2, y2, 0.0f, 5.0f);
        this.textComponent = new TextComponent(this, text, 0f, -1f);
        this.colorPicker = new ColorPickerComponent(this, 74f, 0.0f, 11.0f, 5.0f) {
            @Override
            public int getColor() {
                return getColor.get();
            }

            @Override
            public void setColor(int color) {
                setColor.accept(color);
            }

            @Override
            public boolean isVisible() {
                return isVisible.get();
            }
        };

        this.colorPicker.updateValue(this.colorPicker.getColor());
        this.addChild(this.colorPicker);
        this.addChild(this.textComponent);
        this.valID = "NULL";
    }

    public ColorPickerTextComponent(Component parent, String text, Supplier<Integer> getColor, Consumer<Integer> setColor, Supplier<Boolean> isVisible) {
        this(parent, text, getColor, setColor, isVisible, 0.0f, 0.0f);
        this.valID = "NULL";
    }

    public ColorPickerTextComponent(Component parent, String valID, String text, Supplier<Integer> getColor, Consumer<Integer> setColor) {
        this(parent, text, getColor, setColor, () -> true);
        this.valID = valID;
    }

    public ColorPickerTextComponent(Component parent, String text, Supplier<Integer> getColor, Consumer<Integer> setColor) {
        this(parent, text, getColor, setColor, () -> true);
        this.valID = "NULL";
    }

    @Override
    public float getWidth() {
        return 13.0f + this.textComponent.getWidth();
    }

    @Override
    public boolean isVisible() {
        return this.colorPicker.isVisible();
    }

    @Override
    public float getExpandedX() {
        return this.colorPicker.getExpandedX();
    }

    @Override
    public float getExpandedY() {
        return this.colorPicker.getY() + this.colorPicker.getHeight();
    }

    @Override
    public float getExpandedWidth() {
        return this.colorPicker.getExpandedWidth();
    }

    @Override
    public float getExpandedHeight() {
        return this.colorPicker.getExpandedHeight();
    }

    @Override
    public void setExpanded(boolean expanded) {
        this.colorPicker.setExpanded(expanded);
    }

    @Override
    public boolean isExpanded() {
        return this.colorPicker.isExpanded();
    }
}

