package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.akiren.component.impl;

import net.ccbluex.liquidbounce.features.module.modules.render.ClickGUI;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.akiren.AnimationHelper;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.akiren.component.Component;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.akiren.component.PropertyComponent;
import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.ccbluex.liquidbounce.value.BoolValue;
import net.ccbluex.liquidbounce.value.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import java.awt.*;



public class BooleanSettingComponent extends Component implements PropertyComponent {
    public float textHoverAnimate = 0f;
    public float leftRectAnimation = 0;
    public float rightRectAnimation = 0;
    public BoolValue booleanSetting;
    Minecraft mc = Minecraft.getMinecraft();

    public BooleanSettingComponent(Component parent, BoolValue booleanSetting, int x, int y, int width, int height) {
        super(parent, booleanSetting.getName(), x, y, width, height);
        this.booleanSetting = booleanSetting;
    }

    @Override
    public void drawComponent(ScaledResolution scaledResolution, int mouseX, int mouseY) {
            int x = getX();
            int y = getY();
            int width = getWidth();
            int height = getHeight();
            int middleHeight = getHeight() / 2;
            boolean hovered = isHovered(mouseX, mouseY);
            RenderUtils.drawRect(x, y, x + width, y + height, new Color(20, 20, 20, 111).getRGB());
            Fonts.fontSFUI35.drawStringWithShadow(getName(), x + 3, y + middleHeight - 2, Color.GRAY.getRGB());
            textHoverAnimate = AnimationHelper.animation(textHoverAnimate, hovered ? 2.3f : 2, 0);
            leftRectAnimation = AnimationHelper.animation(leftRectAnimation, booleanSetting.get() ? 10 : 17, 0);
            rightRectAnimation = AnimationHelper.animation(rightRectAnimation, (booleanSetting.getHide() ? 3 : 10), 0);
            RenderUtils.drawSmoothRect(x + width - 18, y + 7, x + width - 2, y + height - 5, new Color(21, 21, 21).getRGB());
            RenderUtils.drawRect(x + width - leftRectAnimation, y + 7.5f, x + width - rightRectAnimation, y + height - 6, booleanSetting.get() ? ClickGUI.generateColor().getRGB() : new Color(50, 50, 50).getRGB());
    }

    @Override
    public void onMouseClick(int mouseX, int mouseY, int button) {
        if (button == 0 && isHovered(mouseX, mouseY)) {
            booleanSetting.set(!booleanSetting.get());
        }
    }

    @Override
    public Value<Boolean> getSetting() {
        return booleanSetting;
    }
}
