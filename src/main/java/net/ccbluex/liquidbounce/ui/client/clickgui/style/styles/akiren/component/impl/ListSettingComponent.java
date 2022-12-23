package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.akiren.component.impl;

import net.ccbluex.liquidbounce.features.module.modules.render.ClickGUI;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.akiren.Panel;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.akiren.component.Component;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.akiren.component.ExpandableComponent;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.akiren.component.PropertyComponent;
import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.ccbluex.liquidbounce.value.ListValue;
import net.ccbluex.liquidbounce.value.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.*;


public class ListSettingComponent extends ExpandableComponent implements PropertyComponent {

    private final ListValue listSetting;
    Minecraft mc = Minecraft.getMinecraft();

    public ListSettingComponent(Component parent, ListValue listSetting, int x, int y, int width, int height) {
        super(parent, listSetting.getName(), x, y, width, height);
        this.listSetting = listSetting;
    }

    @Override
    public void drawComponent(ScaledResolution scaledResolution, int mouseX, int mouseY) {
        super.drawComponent(scaledResolution, mouseX, mouseY);
        int x = getX();
        int y = getY();
        int width = getWidth();
        int height = getHeight();
        String selectedText = listSetting.get();
        int dropDownBoxY = y + 4;
        int textColor = 0xFFFFFF;
        RenderUtils.drawRect(x, y, x + width, y + height, new Color(20, 20, 20, 111).getRGB());
        RenderUtils.drawRect(x + 0.5F, dropDownBoxY, x + getWidth() - 0.5F, (int) (dropDownBoxY + 11), new Color(30, 30, 30).getRGB());
        Fonts.fontSFUI35.drawString(getName(), x + 2 + Panel.X_ITEM_OFFSET, dropDownBoxY + 3, new Color(222, 222, 222).getRGB());
            Fonts.fontSFUI35.drawCenteredString(selectedText, x + width - 16, dropDownBoxY + 3, ClickGUI.generateColor().getRGB());
        if (isExpanded()) {
            RenderUtils.drawRect(x + Panel.X_ITEM_OFFSET, y + height, x + width - Panel.X_ITEM_OFFSET, y + getHeightWithExpand(), new Color(25, 25, 25, 160).getRGB());
            handleRender(x, y + getHeight() + 2, width, textColor);
        }
    }

    @Override
    public void onMouseClick(int mouseX, int mouseY, int button) {
        super.onMouseClick(mouseX, mouseY, button);
        if (isExpanded()) {
            handleClick(mouseX, mouseY, getX(), getY() + getHeight() + 2, getWidth());
        }
    }

    private void handleRender(int x, int y, int width, int textColor) {
        for (String e : listSetting.getModes()) {
                Fonts.fontSFUI35.drawCenteredString(e, x + Panel.X_ITEM_OFFSET + width / 2 + 0.5f, y + 2.5F, Color.GRAY.getRGB());
            y += (Panel.ITEM_HEIGHT - 3);
        }
    }

    private void handleClick(int mouseX, int mouseY, int x, int y, int width) {
        for (String e : this.listSetting.getModes()) {
            if (mouseX >= x && mouseY >= y && mouseX <= x + width && mouseY <= y + Panel.ITEM_HEIGHT - 3) {
                listSetting.set(e);
            }

            y += Panel.ITEM_HEIGHT - 3;
        }
    }


    @Override
    public int getHeightWithExpand() {
        return getHeight() + listSetting.getModes().toArray().length * (Panel.ITEM_HEIGHT - 3);
    }

    @Override
    public void onPress(int mouseX, int mouseY, int button) {
    }

    @Override
    public boolean canExpand() {
        return listSetting.getModes().toArray().length > 0;
    }

    @Override
    public Value<String> getSetting() {
        return listSetting;
    }
}