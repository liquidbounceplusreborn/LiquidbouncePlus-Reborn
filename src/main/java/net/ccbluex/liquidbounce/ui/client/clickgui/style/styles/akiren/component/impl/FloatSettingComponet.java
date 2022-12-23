package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.akiren.component.impl;

import net.ccbluex.liquidbounce.features.module.modules.render.ClickGUI;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.akiren.AnimationHelper;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.akiren.component.Component;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.akiren.component.PropertyComponent;
import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.ccbluex.liquidbounce.utils.math.MathUtils;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.ccbluex.liquidbounce.value.FloatValue;
import net.ccbluex.liquidbounce.value.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.*;
public class FloatSettingComponet extends Component implements PropertyComponent {

    public FloatValue numberSetting;
    public float currentValueAnimate = 0f;
    private boolean sliding;
    Minecraft mc = Minecraft.getMinecraft();
    public FloatSettingComponet(Component parent, FloatValue numberSetting, int x, int y, int width, int height) {
        super(parent, numberSetting.getName(), x, y, width, height);
        this.numberSetting = numberSetting;
    }

    @Override
    public void drawComponent(ScaledResolution scaledResolution, int mouseX, int mouseY) {
        super.drawComponent(scaledResolution, mouseX, mouseY);

        int x = getX();
        int y = getY();
        int width = getWidth();
        int height = getHeight();
        double min = numberSetting.getMinimum();
        double max = numberSetting.getMaximum();
        boolean hovered = isHovered(mouseX, mouseY);

        if (this.sliding) {
            numberSetting.set((float) MathUtils.round((double) (mouseX - x) * (max - min) / (double) width + min, 0.1));
            if (numberSetting.get() > max) {
                numberSetting.set((float) max);
            } else if (numberSetting.get() < min) {
                numberSetting.set((float) min);
            }
        }

        float amountWidth = (float) (((numberSetting.get()) - min) / (max - min));

        currentValueAnimate = AnimationHelper.animation(currentValueAnimate, amountWidth, 0);
        float optionValue = (float) MathUtils.round(numberSetting.get(), 0.1f);
        RenderUtils.drawRect(x, y, x + width, y + height, new Color(20, 20, 20, 111).getRGB());
        RenderUtils.drawRect(x + 3, y + height - 5, x + (width - 3), y + 13, new Color(40, 39, 39).getRGB());

        RenderUtils.drawRect(x + 3, y + 13.5, x + 5 + currentValueAnimate * (width - 8), y + 15F, ClickGUI.generateColor().getRGB());

        RenderUtils.drawFilledCircle((int) (x + 5 + currentValueAnimate * (width - 8)), (int) (y + 14F), 2.5F, new Color(ClickGUI.generateColor().getRGB()));

        String valueString = "";

        Fonts.fontSFUI35.drawStringWithShadow(numberSetting.getName(), x + 2.0F, y + height / 2.5F - 4F, Color.lightGray.getRGB());
        Fonts.fontSFUI35.drawStringWithShadow(optionValue + " " + valueString, x + width - Fonts.fontSFUI35.getStringWidth(optionValue + " " + valueString) - 5, y + height / 2.5F - 4F, Color.GRAY.getRGB());

    }

    @Override
    public void onMouseClick(int mouseX, int mouseY, int button) {
        if (!sliding && button == 0 && isHovered(mouseX, mouseY)) {
            sliding = true;
        }
    }

    @Override
    public void onMouseRelease(int button) {
        this.sliding = false;
    }

    @Override
    public Value<Float> getSetting() {
        return numberSetting;
    }
}