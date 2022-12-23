package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.akiren.component.impl;

import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.modules.render.ClickGUI;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.akiren.ClickGuiScreen;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.akiren.Panel;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.akiren.component.Component;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.akiren.component.ExpandableComponent;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.akiren.component.SorterHelper;
import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.ccbluex.liquidbounce.utils.TimeHelper;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.ccbluex.liquidbounce.value.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Keyboard;

import java.awt.*;


public final class ModuleComponent extends ExpandableComponent {
    Minecraft mc = Minecraft.getMinecraft();
    private final Module module;
    public static TimeHelper timerHelper = new TimeHelper();
    private boolean binding;
    int alpha = 0;
    private final TimeHelper descTimer = new TimeHelper();

    public ModuleComponent(Component parent, Module module, int x, int y, int width, int height) {
        super(parent, module.getName(), x, y, width, height);
        this.module = module;
        int propertyX = Panel.X_ITEM_OFFSET;
        for (Value setting : module.getValues()) {
            if (setting instanceof BoolValue) {
                components.add(new BooleanSettingComponent(this, (BoolValue) setting, propertyX, height, width - (Panel.X_ITEM_OFFSET * 2), Panel.ITEM_HEIGHT + 6));
            } else if (setting instanceof ColorValue) {
                components.add(new ColorPickerComponent(this, (ColorValue) setting, propertyX, height, width - (Panel.X_ITEM_OFFSET * 2), Panel.ITEM_HEIGHT));
            } else if (setting instanceof IntegerValue) {
                components.add(new NumberSettingComponent(this, (IntegerValue) setting, propertyX, height, width - (Panel.X_ITEM_OFFSET * 2), Panel.ITEM_HEIGHT + 5));
            } else if (setting instanceof FloatValue) {
                components.add(new FloatSettingComponet(this, (FloatValue) setting, propertyX, height, width - (Panel.X_ITEM_OFFSET * 2), Panel.ITEM_HEIGHT + 5));
            } else if (setting instanceof ListValue) {
                components.add(new ListSettingComponent(this, (ListValue) setting, propertyX, height, width - (Panel.X_ITEM_OFFSET * 2), Panel.ITEM_HEIGHT + 2));
            }
        }
    }

    public boolean ready = false;
    static String i = " ";

    String getI(String s) {
        if (!timerHelper.hasReached(5)) {
            return i;
        } else {
            timerHelper.reset();
        }
        if (i.length() < s.length()) {
            ready = false;
            return i += s.charAt(i.length());
        }
        ready = true;
        return i;
    }

    @Override
    public void drawComponent(ScaledResolution scaledResolution, int mouseX, int mouseY) {
        components.sort(new SorterHelper());
        float x = getX();
        float y = getY() - 2;
        int width = getWidth();
        int height = getHeight();
        if (isExpanded()) {
            int childY = Panel.ITEM_HEIGHT;
            for (Component child : components) {
                int cHeight = child.getHeight();
                if (child instanceof BooleanSettingComponent) {
                    BooleanSettingComponent booleanSettingComponent = (BooleanSettingComponent) child;

                }
                if (child instanceof NumberSettingComponent) {
                    NumberSettingComponent numberSettingComponent = (NumberSettingComponent) child;

                }

                if (child instanceof ColorPickerComponent) {
                    ColorPickerComponent colorPickerComponent = (ColorPickerComponent) child;

                }
                if (child instanceof ListSettingComponent) {
                    ListSettingComponent listSettingComponent = (ListSettingComponent) child;

                }
                if (child instanceof ExpandableComponent) {
                    ExpandableComponent expandableComponent = (ExpandableComponent) child;
                    if (expandableComponent.isExpanded()) cHeight = expandableComponent.getHeightWithExpand();
                }
                child.setY(childY);
                child.drawComponent(scaledResolution, mouseX, mouseY);
                childY += cHeight;
            }
        }
        if (!ClickGuiScreen.search.getText().isEmpty() && !module.getName().toLowerCase().contains(ClickGuiScreen.search.getText().toLowerCase()))
            return;
        Color color = new Color(ClickGUI.generateColor().getRGB());
        Color color2 = new Color(ClickGUI.generateColor().getRGB());
        boolean hovered = isHovered(mouseX, mouseY);


        if (components.size() > 0.5) {
           Fonts.fontSFUI35.drawStringWithShadow(isExpanded() ? "" : "...", x + width - 8.5f, (float) (y + height / 2F - 3.5), -1);
        }

        components.sort(new SorterHelper());
        if (hovered && module.getDescription() != null) {
            RenderUtils.drawShadow(5, 1, () -> {

                ScaledResolution sr = new ScaledResolution(mc);

                if (!hovered) {
                    i = " ";
                }
                RenderUtils.drawRect(sr.getScaledWidth() / 2 - Fonts.fontSFUI35.getStringWidth(module.getDescription()) / 2 - 10, sr.getScaledHeight() - 25, sr.getScaledWidth() / 2 + Fonts.fontSFUI35.getStringWidth(module.getDescription()) / 2 + 10, sr.getScaledHeight(), new Color(0, 0, 0, 150).getRGB());
                Fonts.fontSFUI35.drawCenteredString(module == null ? "null pointer :(" : getI(module.getDescription()), sr.getScaledWidth() / 2f, sr.getScaledHeight() - 10, -1);


                RenderUtils.drawRect(sr.getScaledWidth() / 2 - Fonts.fontSFUI35.getStringWidth(module.getDescription()) / 2 - 10, sr.getScaledHeight() - 25, sr.getScaledWidth() / 2 + Fonts.fontSFUI35.getStringWidth(module.getDescription()) / 2 + 10, sr.getScaledHeight() - 26, color2.getRGB());
                    Fonts.fontSFUI35.drawCenteredString(module.getName(), sr.getScaledWidth() / 2, sr.getScaledHeight() - 21, -1);
            });

            ScaledResolution sr = new ScaledResolution(mc);

            if (!hovered) {
                i = " ";
            }
            RenderUtils.drawRect(sr.getScaledWidth() / 2 - Fonts.fontSFUI35.getStringWidth(module.getDescription()) / 2 - 10, sr.getScaledHeight() - 25, sr.getScaledWidth() / 2 + Fonts.fontSFUI35.getStringWidth(module.getDescription()) / 2 + 10, sr.getScaledHeight(), new Color(0, 0, 0, 150).getRGB());
            Fonts.fontSFUI35.drawCenteredString(module == null ? "null pointer :(" : getI(module.getDescription()), sr.getScaledWidth() / 2f, sr.getScaledHeight() - 10, -1);


            RenderUtils.drawRect(sr.getScaledWidth() / 2 - Fonts.fontSFUI35.getStringWidth(module.getDescription()) / 2 - 10, sr.getScaledHeight() - 25, sr.getScaledWidth() / 2 + Fonts.fontSFUI35.getStringWidth(module.getDescription()) / 2 + 10, sr.getScaledHeight() - 26, color2.getRGB());
                Fonts.fontSFUI35.drawCenteredString(module.getName(), sr.getScaledWidth() / 2, sr.getScaledHeight() - 21, -1);

            if (module == null) i = "";
            else {
                if (ready && !i.equals(module.getDescription())) i = "";
            }
        } else {
            ready = false;
        }

        if (module.getState()) {
                Fonts.fontSFUI35.drawCenteredString(binding ? "Press a key.. " + Keyboard.getKeyName(module.getKeyBind()) : getName(), x + 53.5f, y + height / 2F - 3, module.getState() ? color.getRGB() : Color.GRAY.getRGB());
        } else {
            Fonts.fontSFUI35.drawCenteredString(binding ? "Press a key.. " + Keyboard.getKeyName(module.getKeyBind()) : getName(), x + 53.5f, y + height / 2F - 3, module.getState() ? new Color(color.getRGB()).getRGB() : Color.GRAY.getRGB());
        }
    }

    @Override
    public boolean canExpand() {
        return !components.isEmpty();
    }

    @Override
    public void onPress(int mouseX, int mouseY, int button) {
        switch (button) {
            case 0:
                module.toggle();
                break;
            case 2:
                binding = !binding;
                break;
        }
    }

    @Override
    public void onKeyPress(int keyCode) {
        if (binding) {
            ClickGuiScreen.escapeKeyInUse = true;
            module.setKeyBind(keyCode == Keyboard.KEY_DELETE ? Keyboard.KEY_NONE : keyCode);
            binding = false;
        }
    }

    @Override
    public int getHeightWithExpand() {
        int height = getHeight();
        if (isExpanded()) {
            for (Component child : components) {
                int cHeight = child.getHeight();
                if (child instanceof BooleanSettingComponent) {
                    BooleanSettingComponent booleanSettingComponent = (BooleanSettingComponent) child;

                }
                if (child instanceof NumberSettingComponent) {
                    NumberSettingComponent numberSettingComponent = (NumberSettingComponent) child;

                }
                if (child instanceof ColorPickerComponent) {
                    ColorPickerComponent colorPickerComponent = (ColorPickerComponent) child;

                }
                if (child instanceof ListSettingComponent) {
                    ListSettingComponent listSettingComponent = (ListSettingComponent) child;

                }
                if (child instanceof ExpandableComponent) {
                    ExpandableComponent expandableComponent = (ExpandableComponent) child;
                    if (expandableComponent.isExpanded()) cHeight = expandableComponent.getHeightWithExpand();
                }
                height += cHeight;
            }
        }
        return height;
    }

}
