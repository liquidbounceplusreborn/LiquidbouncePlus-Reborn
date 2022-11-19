/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.novoline2;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Tab {

    private final ModuleCategory enumModuleType;
    private float posX;
    private float posY;
    public boolean dragging, opened;
    public List<Module> modules = new CopyOnWriteArrayList<>();

    public Tab(ModuleCategory enumModuleType, float posX, float posY) {
        this.enumModuleType = enumModuleType;
        this.posX = posX;
        this.posY = posY;
        for (net.ccbluex.liquidbounce.features.module.Module abstractModule : LiquidBounce.moduleManager.getModuleInCategory(enumModuleType)) {
            modules.add(new Module(abstractModule, this));
        }
    }

    public void drawScreen(int mouseX, int mouseY) {

        String l = "";
        if (enumModuleType.name().equalsIgnoreCase("Combat")) {
            l = "";
        } else if (enumModuleType.name().equalsIgnoreCase("Movement")) {
            l = "";
        } else if (enumModuleType.name().equalsIgnoreCase("Player")) {
            l = "";
        } else if (enumModuleType.name().equalsIgnoreCase("Render")) {
            l = "";
        } else if (enumModuleType.name().equalsIgnoreCase("Exploit")) {
            l = "";
        } else if (enumModuleType.name().equalsIgnoreCase("Misc")) {
            l = "";
        } else if (enumModuleType.name().equalsIgnoreCase("World")) {
            l = "";
        }
        RenderUtils.drawRect(posX - 1, posY, posX + 101, posY + 15, new Color(29, 29, 29, 255).getRGB());
        Fonts.fontSFUI35.drawString(l, posX + 88, posY + 5, 0xffffffff);
        if (enumModuleType.name().equalsIgnoreCase("World")){
            Fonts.fontSFUI35.drawString("",posX + 88, posY + 5, 0xffffffff);
        }
        Fonts.fontSFUI35.drawString(enumModuleType.name().charAt(0) + enumModuleType.name().substring(1).toLowerCase(), posX + 4, posY + 4, 0xffffffff, true);
        if (opened) {
            RenderUtils.drawRect(posX - 1, posY + 15, posX + 101, posY + 15 + getTabHeight() + 1, new Color(29, 29, 29, 255).getRGB());
            modules.forEach(module -> module.drawScreen(mouseX, mouseY));
        } else {
            modules.forEach(module -> module.yPerModule = 0);
        }
    }

    public int getTabHeight() {
        int height = 0;
        for (Module module : modules) {
            height += module.yPerModule;
        }
        return height;
    }

    public boolean isHovered(int mouseX, int mouseY) {
        return mouseX >= posX && mouseY >= posY && mouseX <= posX + 101 && mouseY <= posY + 15;
    }

    public void mouseReleased(int mouseX, int mouseY, int state) {
        if (opened) {
            modules.forEach(module -> module.mouseReleased(mouseX, mouseY, state));
        }
    }

    public void keyTyped(char typedChar, int keyCode) {
        modules.forEach(module -> module.keyTyped(typedChar, keyCode));
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (isHovered(mouseX, mouseY) && mouseButton == 1) {
            opened = !opened;
            if (opened) {
                for (Module module : modules) {
                    module.fraction = 0;
                }
            }
        }

        if (opened) {
            modules.forEach(module -> {
                try {
                    module.mouseClicked(mouseX, mouseY, mouseButton);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public void setPosX(float posX) {
        this.posX = posX;
    }

    public void setPosY(float posY) {
        this.posY = posY;
    }

    public float getPosY() {
        return posY;
    }

    public float getPosX() {
        return posX;
    }

    public List<Module> getModules() {
        return modules;
    }
}
