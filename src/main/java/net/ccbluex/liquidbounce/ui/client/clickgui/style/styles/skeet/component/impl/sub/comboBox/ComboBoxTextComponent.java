package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.skeet.component.impl.sub.comboBox;

import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.skeet.component.Component;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.skeet.component.PredicateComponent;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.skeet.component.impl.sub.text.TextComponent;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class ComboBoxTextComponent extends Component implements PredicateComponent {
    private static final int COMBO_BOX_HEIGHT = 10;
    private static final int COMBO_BOX_Y_OFFSET = 1;
    private final ComboBoxComponent comboBoxComponent;

    public ComboBoxTextComponent(Component parent, String name, final Supplier<String>getValue, final Supplier<String[]> getValues, final Consumer<String> setValue, float x, float y) {
        super(parent, x, y, 85f, 16.0f);
        this.comboBoxComponent = new ComboBoxComponent(this, 0.0f, 6.0f, this.getWidth(), 10.0f) {

            @Override
            public boolean isVisible() {
                return true;
            }

            @Override
            public String getValue() {
                return getValue.get();
            }

            @Override
            public void setValue(String val) {
                setValue.accept(val);
            }

            @Override
            public String[] getValues() {
                return getValues.get();
            }
        };
        this.addChild(this.comboBoxComponent);
        this.addChild(new TextComponent(this, name, 1.0f, -1.0f));
    }

    public ComboBoxTextComponent(Component parent, String name, Supplier<String>getValue, Supplier<String[]> getValues, Consumer<String> setValue) {
        this(parent, name, getValue, getValues, setValue, 0.0f, 0.0f);
    }

    @Override
    public boolean isVisible() {
        return this.comboBoxComponent.isVisible();
    }

    public ComboBoxComponent getComboBoxComponent() {
        return this.comboBoxComponent;
    }
}

