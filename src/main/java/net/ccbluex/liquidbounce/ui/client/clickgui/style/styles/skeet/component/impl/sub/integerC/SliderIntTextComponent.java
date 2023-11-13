package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.skeet.component.impl.sub.integerC;

import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.skeet.component.Component;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.skeet.component.PredicateComponent;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.skeet.component.impl.sub.text.TextComponent;

import java.util.function.Consumer;
import java.util.function.Supplier;


public final class SliderIntTextComponent extends Component implements PredicateComponent {
    private static final float SLIDER_THICKNESS = 4.0f;
    private static final int SLIDER_Y_OFFSET = 1;
    private final SliderIntComponent sliderComponent;

    public SliderIntTextComponent(Component parent, String text, final Supplier<Integer> getValue, final Consumer<Integer> setValue, final Supplier<Integer> getMin, final Supplier<Integer> getMax, final Supplier<Integer> getIncrement, final Supplier<Boolean> isVisible, float x, float y) {
        super(parent, x, y, 85f, 4.0f);
        this.sliderComponent = new SliderIntComponent(this, 0.0f, 6.0f, this.getWidth(), 4.0f) {

            @Override
            public float getValue() { return getValue.get(); }

            @Override
            public void setValue(int value) {
                setValue.accept(value);
            }

            @Override
            public float getMin() {
                return getMin.get();
            }

            @Override
            public float getMax() {
                return getMax.get();
            }

            @Override
            public float getIncrement() {
                return getIncrement.get();
            }

            @Override
            public boolean isVisible() {
                return isVisible.get();
            }
        };
        this.addChild(this.sliderComponent);
        this.addChild(new TextComponent(this, text, 1.0f, -1.0f));
    }

    public SliderIntTextComponent(Component parent, String text, Supplier<Integer> getValue, Consumer<Integer> setValue, Supplier<Integer> getMin, Supplier<Integer> getMax, Supplier<Integer> getIncrement, Supplier<Boolean> isVisible) {
        this(parent, text, getValue, setValue, getMin, getMax, getIncrement, isVisible, 0.0f, 0.0f);
    }

    public SliderIntTextComponent(Component parent, String text, Supplier<Integer> getValue, Consumer<Integer> setValue, Supplier<Integer> getMin, Supplier<Integer> getMax, Supplier<Integer> getIncrement) {
        this(parent, text, getValue, setValue, getMin, getMax, getIncrement, () -> true);
    }

    @Override
    public float getHeight() {
        return 6.0f + super.getHeight();
    }

    @Override
    public boolean isVisible() {
        return this.sliderComponent.isVisible();
    }
}

