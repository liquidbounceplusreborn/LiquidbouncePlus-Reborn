package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.flux;
import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.file.configs.ProfilesConfig;
import net.ccbluex.liquidbounce.utils.ClientUtils;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class ProfilesScreen extends GuiScreen {
    private final GuiScreen prevScreen;
    private GuiTextField textField;

    public ProfilesScreen(GuiScreen prevScreen) {
        this.prevScreen = prevScreen;
    }

    /**
     * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
     * window resizes, the buttonList is cleared beforehand.
     */
    @Override
    public void initGui() {
        super.initGui();
        textField = new GuiTextField(0, mc.fontRendererObj,width / 2 - 100,height / 2 - 50,200,20);
        textField.setFocused(true);
        GuiButton addButton = new GuiButton(0, width / 2 - 100,height / 2,"Add"),cancelButton = new GuiButton(1, width / 2 - 100, height / 2 + 25, "Cancel");
        this.buttonList.add(addButton);
        this.buttonList.add(cancelButton);
        Keyboard.enableRepeatEvents(true);
    }

    /**
     * Called from the main game loop to update the screen.
     */
    @Override
    public void updateScreen() {
        textField.updateCursorCounter();
    }

    /**
     * Fired when a key is typed (except F11 which toggles full screen). This is the equivalent of
     * KeyListener.keyTyped(KeyEvent e). Args : character (character on the key), keyCode (lwjgl Keyboard key code)
     *
     * @param typedChar char
     * @param keyCode int
     */
    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        this.textField.textboxKeyTyped(typedChar, keyCode);
        super.keyTyped(typedChar, keyCode);
    }

    /**
     * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
     *
     * @param button GuiButton
     */
    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        switch(button.id) {
            case 0:
                if(!textField.getText().isEmpty()) {
                    ProfilesConfig profilesConfig = new ProfilesConfig(new File(LiquidBounce.fileManager.settingsDir, textField.getText() + ".profile"));
                    if (profilesConfig.getFile().exists()) {
                        profilesConfig.deleteConfig();
                        LiquidBounce.fileManager.profilesConfigs.remove(profilesConfig);
                    }
                    try {
                        if(!profilesConfig.hasConfig())
                            profilesConfig.createConfig();
                        profilesConfig.saveConfig();
                        ClientUtils.getLogger().info("[FileManager] Saved config: " + profilesConfig.getFile().getName() + ".");
                    }catch(final Throwable throwable) {
                        ClientUtils.getLogger().error("[FileManager] Failed to save config file: " +
                                profilesConfig.getFile().getName() + ".", throwable);
                    }
                    LiquidBounce.fileManager.profilesConfigs.add(profilesConfig);
                    mc.displayGuiScreen(prevScreen);
                }
                break;
            case 1:
                mc.displayGuiScreen(prevScreen);
                break;
        }
        super.actionPerformed(button);
    }

    /**
     * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
     *
     * @param mouseX int
     * @param mouseY int
     * @param mouseButton int
     */
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        textField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    /**
     * Draws the screen and all the components in it. Args : mouseX, mouseY, renderPartialTicks
     *
     * @param mouseX int
     * @param mouseY int
     * @param partialTicks float
     */
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        RenderUtils.drawRect(-width,-height,width,height,new Color(0,0,0, 200).getRGB());
        this.textField.drawTextBox();
        mc.fontRendererObj.drawStringWithShadow("Name", width * 0.5F - 100, height * 0.5F - 60, -1);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
