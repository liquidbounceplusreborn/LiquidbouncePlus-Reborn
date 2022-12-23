package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.akiren;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.modules.render.ClickGUI;
import net.ccbluex.liquidbounce.features.module.modules.render.ColorMixer;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.akiren.component.Component;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.akiren.component.ExpandableComponent;
import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.ccbluex.liquidbounce.utils.render.Animation;
import net.ccbluex.liquidbounce.utils.render.ColorUtils;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;


import java.io.IOException;
import java.util.ArrayList;
import java.awt.*;
import java.util.List;
import java.util.Objects;

public class ClickGuiScreen extends GuiScreen {
    public static boolean escapeKeyInUse;
    public float scale = 2;

    public List<Panel> components = new ArrayList<>();
    public ScreenHelper screenHelper;
    public boolean exit = false;
    public ModuleCategory type;
    private Component selectedPanel;
    private Animation initAnimation;
    private static ResourceLocation ANIME_GIRL;
    public static GuiInputBox search;

    public ClickGuiScreen() {
        int x = 20;
        int y = 80;
        for (ModuleCategory type : ModuleCategory.values()) {
            this.type = type;
            this.components.add(new Panel(type, x, y));
            selectedPanel = new Panel(type, x, y);
            x += width + 125;
        }
        this.screenHelper = new ScreenHelper(0, 0);
    }

    @Override
    public void initGui() {

        ScaledResolution sr = new ScaledResolution(mc);
        initAnimation = new DecelerateAnimation(600, 1);
        this.screenHelper = new ScreenHelper(0, 0);
        search = new GuiInputBox(1337, Fonts.fontSFUI35, sr.getScaledWidth() / 2 + 320, 80, 150, 18);
        super.initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        ScaledResolution sr = new ScaledResolution(mc);

        Color color = Color.WHITE;
        switch (ClickGUI.colorModeValue.get()) {
            case "custom":
                color = new Color(ClickGUI.colorRedValue.get(), ClickGUI.colorGreenValue.get(), ClickGUI.colorBlueValue.get());
                break;
            case "rainbow":
                color = new Color(RenderUtils.getRainbowOpaque(ClickGUI.mixerSecondsValue.get(), ClickGUI.saturationValue.get(), ClickGUI.brightnessValue.get(), 0));
                break;
            case "sky":
                color = RenderUtils.skyRainbow(0, ClickGUI.saturationValue.get(), ClickGUI.brightnessValue.get());
                break;
            case "liquidslowly":
                color = ColorUtils.LiquidSlowly(System.nanoTime(), 0, ClickGUI.saturationValue.get(), ClickGUI.brightnessValue.get());
                break;
            case "fade":
                color = ColorUtils.fade(new Color(ClickGUI.colorRedValue.get(), ClickGUI.colorGreenValue.get(), ClickGUI.colorBlueValue.get()), 0, 100);
                break;
            case "mixer":
                color = ColorMixer.getMixedColor(0, ClickGUI.mixerSecondsValue.get());
                break;

        }

        switch (Objects.requireNonNull(LiquidBounce.moduleManager.getModule(ClickGUI.class)).backgroundValue.get()) {
            case "Default":
                drawDefaultBackground();
                break;
            case "Gradient":
                drawGradientRect(0, 0, width, height,
                        ColorUtils.reAlpha(ClickGUI.generateColor(), Objects.requireNonNull(LiquidBounce.moduleManager.getModule(ClickGUI.class)).gradEndValue.get()).getRGB(),
                        ColorUtils.reAlpha(ClickGUI.generateColor(), Objects.requireNonNull(LiquidBounce.moduleManager.getModule(ClickGUI.class)).gradStartValue.get()).getRGB());
                break;
            default:
                break;
        }
        Fonts.fontSFUI35.drawStringWithShadow("Akrein" + "Client", 2, sr.getScaledHeight() - 10, new Color(255, 255, 255).getRGB());
        Fonts.fontSFUI35.drawStringWithShadow("UID " + "001", sr.getScaledWidth() - Fonts.fontSFUI35.getStringWidth("UID " + "001") - 4, sr.getScaledHeight() - 9, new Color(255, 255, 255).getRGB());
        search.drawTextBox();
        if (search.getText().isEmpty() && !search.isFocused())
            Fonts.fontSFUI35.drawStringWithShadow("Search Feature...", (sr.getScaledWidth() / 2.0F + 362.0F), 86, -1);

        for (Panel panel : components) {
            panel.drawComponent(sr, mouseX, (int) (mouseY));
        }
        updateMouseWheel();
        super.drawScreen(mouseX, mouseY, partialTicks);

    }

    public void updateMouseWheel() {
        int scrollWheel = Mouse.getDWheel();
        for (Component panel : components) {
            if (scrollWheel > 0) {
                panel.setY(panel.getY() + 15);
            }
            if (scrollWheel < 0) {
                panel.setY(panel.getY() - 15);
            }
        }
    }

    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        this.selectedPanel.onKeyPress(keyCode);
        if (!escapeKeyInUse)
            super.keyTyped(typedChar, keyCode);
        search.textboxKeyTyped(typedChar, keyCode);
        if ((typedChar == '\t' || typedChar == '\r') && search.isFocused())
            search.setFocused(!search.isFocused());
        escapeKeyInUse = false;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        search.setFocused(false);
        search.setText("");
        search.mouseClicked(mouseX, mouseY, mouseButton);
        for (Component component : components) {
            int x = component.getX();
            int y = component.getY();
            int cHeight = component.getHeight();
            if (component instanceof ExpandableComponent) {
                ExpandableComponent expandableComponent = (ExpandableComponent) component;
                if (expandableComponent.isExpanded())
                    cHeight = expandableComponent.getHeightWithExpand();
            }
            if (mouseX > x && mouseY > y && mouseX < x + component.getWidth() && mouseY < y + cHeight) {
                selectedPanel = component;
                component.onMouseClick(mouseX, mouseY, mouseButton);
                break;
            }
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        selectedPanel.onMouseRelease(state);
    }

    @Override
    public void onGuiClosed() {
        this.screenHelper = new ScreenHelper(0, 0);
        super.onGuiClosed();
    }
}
