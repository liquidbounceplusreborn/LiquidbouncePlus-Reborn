package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.flux;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.modules.render.ClickGUI;
import net.ccbluex.liquidbounce.file.configs.ProfilesConfig;
import net.ccbluex.liquidbounce.ui.client.hud.designer.GuiHudDesigner;
import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.ccbluex.liquidbounce.value.*;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;


import java.awt.Color;

public class FluxClassic extends GuiScreen {
    /**
     * @author LiquidBounce
     * @version 1.0
     * @see GuiScreen
     * @apiNote org.lwjgl.opengl.GL11
     *
     */
    public float windowsX = 50,
            windowsY = 25,
            dragX = 0,
            dragY = 0,
            animationHeight = 0,
            animationPosition;
    private int mouseWheel = 0;

    public ModuleCategory currentCategory = ModuleCategory.COMBAT;
    public final Translate translate = new Translate(0F, 0F),configTranslate = new Translate(0F, 0F);
    private final ClickGUI clickGuiModule;
    private boolean mouseClicked = false, showConfig = false;
    public String currentConfigName = "Basic";

    /**
     * Create a new Instance
     */
    public FluxClassic() {
        this.clickGuiModule = (ClickGUI) LiquidBounce.moduleManager.getModule(ClickGUI.class);
        this.animationPosition = 75;
        for(Module module : LiquidBounce.moduleManager.getModules())
            module.getAnimationHelper().animationX = module.getState() ? 2.5F : -2.5F;
    }

    /**
     * Called when the screen is unloaded. Used to disable keyboard repeat events
     */
    @Override
    public void onGuiClosed() {
        clickGuiModule.moduleCategory = this.currentCategory;
        clickGuiModule.animationHeight = this.animationHeight;
        clickGuiModule.configName = this.currentConfigName;
    }

    /**
     * Draws the screen and all the components in it. Args : mouseX, mouseY, renderPartialTicks
     *
     * @param mouseX Int
     * @param mouseY Int
     * @param partialTicks Float
     */

    @Override
    public void drawScreen(int mouseX,int mouseY,float partialTicks) {
        this.animationPosition = AnimationUtil.moveUD(this.animationPosition, 0.0F, 0.1F, 0.1F);
        GL11.glRotatef(this.animationPosition, 0.0F, 0.0F, 0.0F);
        GL11.glTranslatef(0.0F, this.animationPosition, 0.0F);
        if (isHovered(windowsX, windowsY, windowsX + 400, windowsY + 15, mouseX, mouseY) && Mouse.isButtonDown(0)) {//移动窗口
            if (dragX == 0 && dragY == 0) {
                dragX = mouseX - windowsX;
                dragY = mouseY - windowsY;
            } else {
                windowsX = mouseX - dragX;
                windowsY = mouseY - dragY;
            }
            this.mouseClicked = true;
        } else if (dragX != 0 || dragY != 0) {
            dragX = 0;
            dragY = 0;
        }

        if(isHovered(windowsX + 390, windowsY + 245, windowsX + 400, windowsY + 250, mouseX,mouseY))
           Fonts.fontSFUI35.drawStringWithColor("o", mouseX, mouseY, new Color(160,160,160).getRGB(), false);

        net.ccbluex.liquidbounce.utils.render.RenderUtils.drawImage(new ResourceLocation( "liquidbounce+/custom_hud_icon.png"), 9, height - 41, 32, 32);
        if (Mouse.isButtonDown(0) && mouseX >= 5 && mouseX <= 50 && mouseY <= height - 5 && mouseY >= height - 50)
            mc.displayGuiScreen(new GuiHudDesigner()); //进入自定义HUD界面

        RenderUtils.drawRoundedRect3(windowsX, windowsY, windowsX + 100, windowsY + 250,2, new Color(255,255,255).getRGB(), 1);
        RenderUtils.drawRoundedRect3(windowsX + 100, windowsY, windowsX + 400, windowsY + 250,2, new Color(241,241,241).getRGB(), 2);
        for(int i = 0; i < ModuleCategory.values().length; i++) {
            ModuleCategory[] categories = ModuleCategory.values();
            int lastHeight = i * 25;
            if(this.isHovered(windowsX, windowsY + 40 + i * 25, windowsX + 100, windowsY + 65 + i * 25, mouseX, mouseY)) {
                if(Mouse.isButtonDown(0)) {
                    if(!mouseClicked) {
                        this.currentCategory = categories[i];
                        mouseWheel = 0;
                    }
                    mouseClicked = true;
                } else mouseClicked = false;
            }
            if (categories[i] == currentCategory) {
                RenderUtils.drawRect(windowsX, windowsY + 40 + animationHeight, windowsX + 100, windowsY + 65 + animationHeight, new Color(65,133,244).getRGB());
                animationHeight += animationHeight < lastHeight ? lastHeight - animationHeight < 30 ? 2.5 : 5.0 : animationHeight > lastHeight ? animationHeight - lastHeight < 30 ? -2.5 : -5.0 : 0;
            }
            Fonts.fontSFUI35.drawString(getCategoryIcon(categories[i]),windowsX + 10, windowsY + i * 25 + 47,categories[i] == currentCategory && (lastHeight > animationHeight ? animationHeight >= lastHeight : animationHeight <= lastHeight) ? -1 : new Color(160,160,160).getRGB());
            Fonts.fontSFUI35.drawString(categories[i].getDisplayName(),windowsX + 30, windowsY + i * 25 + 45, categories[i] == currentCategory && (lastHeight > animationHeight ? animationHeight >= lastHeight : animationHeight <= lastHeight) ? -1 : new Color(160,160,160).getRGB());
        }
        Fonts.fontSFUI35.drawString("", windowsX + 5, windowsY + 10, new Color(70, 92 ,255).getRGB());
        Fonts.fontSFUI35.drawString("Liquidbounce+", windowsX + 10 + Fonts.fontSFUI35.getStringWidth("q"), windowsY + 10, new Color(70, 92, 255).getRGB());
        RenderUtils.drawGradientSideways(windowsX + 100,windowsY, windowsX + 110, windowsY + 250, new Color(0, 0, 0, 70).getRGB(),new Color(241, 241, 241, 30).getRGB());
        RenderUtils.startGlScissor((int)windowsX + 100 + (int)animationPosition, (int)windowsY + 20, 400 - (int) animationPosition, 230 - (int) animationPosition);//Start Scissor Box
        float moduleY = translate.getY();
        float startX = windowsX + 120;
        float startY = windowsY + 20;
        // if(this.currentCategory != ModuleCategory.GLOBAL) {
        for(final Module module : LiquidBounce.moduleManager.getModuleInCategory(this.currentCategory)) {
            if(!module.showSettings)
                module.yPos1 = 30;
            else {
                module.yPos1 = 30;
                if(module.getName().equalsIgnoreCase("TeleportAura"))
                    module.yPos1 += 10;
                if(module.getName().equalsIgnoreCase("InvManager"))
                    module.yPos1 += 15;
                for(int i = 0; i < module.getNumberValues().size(); ++i) {
                    if(i == module.getNumberValues().size() - 1 && i == 0 && module.getValues().size() > 1) {
                        module.yPos1 += 15;
                    }
                    if(i % 2 == 0) {
                        module.yPos1 += 25;
                    }
                }
                float xPos;
                for(int i = 0; i < module.getListValues().size(); ++i) {
                    ListValue optionMode = module.getListValues().get(i);
                    xPos = startX + 8;
                    if(i == 0) {
                        module.yPos1 += 20;
                    } else {
                        module.yPos1 += 15;
                    }
                    if(module.getValues().size() == 1)
                        module.yPos1 += 10;
                    module.yPos1 += 20;
                    for(String mode : optionMode.getValues()) {
                        xPos += 14;
                        xPos += Fonts.fontSFUI35.getStringWidth(mode) + 10;
                        if(xPos > windowsX + 340 && optionMode.indexOf(mode) != optionMode.getValues().length - 1) {
                            xPos = startX + 8;
                            module.yPos1 += 25;
                        }
                    }
                }
                xPos = startX + 8;
                if(module.getBooleanValues().size() > 8 && module.getBooleanValues().size() < 13)
                    module.yPos1 += 30;
                for(int i = 0; i < module.getBooleanValues().size(); ++i) {
                    final BoolValue optionBoolean = module.getBooleanValues().get(i);
                    if(i == 0) {
                        module.yPos1 += 10;
                    }
                    if(module.getValues().size() == 1)
                        module.yPos1 += 10;
                    xPos += Fonts.fontSFUI35.getStringWidth(optionBoolean.getName()) + Fonts.fontSFUI35.getStringWidth("j") + 10;
                    if(xPos > windowsX + 310) {
                        xPos = startX + 8;
                        module.yPos1 += 15;
                    }
                }
            }
            module.getValueTranslate().interpolate(0, module.yPos1, 0.1);
            module.getModuleTranslate().interpolate(0, moduleY, 0.1);
            //Animations
            float modulePosY = module.getModuleTranslate().getY();
            float valuePosY = module.getValueTranslate().getY();
            if(module.showSettings)
                RenderUtils.drawFastRoundedRect(startX - 0.5F, startY + modulePosY - 0.5F, startX + 260 + 0.5F, startY + modulePosY + valuePosY + 0.5F, 2,
                        new Color(49,157,247).getRGB());
            RenderUtils.drawFastRoundedRect(startX, startY + modulePosY, startX + 260, startY + modulePosY + valuePosY, 2,
                    new Color(255, 255, 255).getRGB());
            RenderUtils.circle(startX + 10, startY + 16 + modulePosY, 2, new Color(210,210,210).getRGB());
            RenderUtils.drawFastRoundedRect(startX + 220, startY + modulePosY + 12, startX + 235, startY + modulePosY + 20,3,new Color(229,229,229).getRGB());
            if(module.getState())
                RenderUtils.circle(startX + 227.5F + module.getAnimationHelper().getAnimationX(), startY + 16 + modulePosY, 5, new Color(66,134,245).getRGB());
            else
                RenderUtils.drawFullCircle(startX + 227.5F + module.getAnimationHelper().getAnimationX(), startY + 16 + modulePosY, 5, new Color(255,255,255).getRGB(),new Color(180,180,180).getRGB());
            Fonts.fontSFUI35.drawString(module.getName(), startX + 20, startY + 9 + modulePosY, new Color(170,170,170).getRGB());
            Fonts.fontSFUI35.drawString(!module.showSettings ? "i" : "h", startX + 245, startY + 12 + modulePosY, module.showSettings || this.isHovered(startX + 245, startY + 12 + modulePosY, startX + 253, startY + 18 + modulePosY, mouseX, mouseY) ? new Color(86,147,245).getRGB() : new Color(160,160,160).getRGB());
            if(module.getAnimationHelper().getAnimationX() > -2.5F && !module.getState())
                module.getAnimationHelper().animationX -= 0.25F;
            else if(module.getAnimationHelper().getAnimationX() < 2.5F && module.getState())
                module.getAnimationHelper().animationX += 0.25F;
            if(this.isHovered(startX + 220, startY + modulePosY + 12, startX + 235, startY + modulePosY + 20, mouseX, mouseY) && mouseY < windowsY + 250) {
                if(Mouse.isButtonDown(0)) {
                    if(!mouseClicked)
                        module.toggle();
                    mouseClicked = true;
                } else mouseClicked = false;
            }
            if(this.isHovered(startX, startY + modulePosY, startX + 260, startY + modulePosY + 30, mouseX, mouseY) && !(mouseY < windowsY)) {
                if(Mouse.isButtonDown(0)) {
                    if(!mouseClicked && module.getValues().size() > 0) {
                        for(Module mod : LiquidBounce.moduleManager.getModules()) {
                            if(mod != module) {
                                if(!module.showSettings && mod.showSettings)
                                    mod.showSettings = false;
                            }
                        }
                        module.showSettings = !module.showSettings;
                    }
                    mouseClicked = true;
                } else mouseClicked = false;
            }
            if(module.showSettings) {
                valuePosY = startY + modulePosY;
                boolean changePosX;
                for(int i = 0; i < module.getNumberValues().size(); ++i) {
                    if(!(changePosX = i % 2 != 0)) {
                        moduleY += 25;
                        valuePosY += 30;
                    }
                    Value<?> option = module.getNumberValues().get(i);
                    float posX = startX + (changePosX ? 130 : 0);
                    final double max = Math.max(0.0, (mouseX - (posX + 8)) / 112.0);
                    if(option instanceof IntegerValue) {
                        IntegerValue optionInt = (IntegerValue) option;
                        Fonts.fontSFUI35.drawString(optionInt.getName(), posX + 8, valuePosY, new Color(160,160,160).getRGB());
                        optionInt.getTranslate().interpolate((112F *
                                        (optionInt.get() > optionInt.getMaximum() ? optionInt.getMaximum() : optionInt.get() < optionInt.getMinimum() ? 0 : optionInt.get()
                                                - optionInt.getMinimum()) / (optionInt.getMaximum() - optionInt.getMinimum()) + 8)
                                , 0, 0.1);

                        RenderUtils.drawRect(posX + 8, valuePosY + 16, posX + 120, valuePosY + 17,
                                (new Color(227, 227, 227)).getRGB());
                        RenderUtils.drawRect(posX + 8, valuePosY + 16, (posX + optionInt.getTranslate().getX()), valuePosY + 17,
                                (new Color(66, 134, 245)).getRGB());
                        RenderUtils.circle((posX + optionInt.getTranslate().getX() + 1.5F),  (valuePosY + 16.5F), 2, new Color(66,134,245));
                        Fonts.fontSFUI35.drawString(optionInt.get().toString(), posX + 110, valuePosY, new Color(160,160,160).getRGB());
                        if (this.isHovered(posX + 8, valuePosY + 14F, posX + 120, valuePosY + 19, mouseX, mouseY) && !mouseClicked && Mouse.isButtonDown(0))
                            optionInt.set(Math.toIntExact(Math.round(optionInt.getMinimum() + (optionInt.getMaximum() - optionInt.getMinimum()) * Math.min(max, 1.0))));

                    } else {

                        FloatValue optionDouble = (FloatValue) option;
                        Fonts.fontSFUI35.drawString(optionDouble.getName(), posX + 8, valuePosY, new Color(160,160,160).getRGB());
                        optionDouble.getTranslate().interpolate((float) (112F * (optionDouble.get() > optionDouble.getMaximum() ? optionDouble.getMaximum() : optionDouble.get() < optionDouble.getMinimum() ? 0 : optionDouble.get() - optionDouble.getMinimum()) / (optionDouble.getMaximum() - optionDouble.getMinimum()) + 8), 0, 0.1);
                        RenderUtils.drawRect(posX + 8, valuePosY + 16, posX + 120, valuePosY + 17,
                                (new Color(227, 227, 227)).getRGB());
                        RenderUtils.drawRect(posX + 8, valuePosY + 16, (posX + optionDouble.getTranslate().getX()), valuePosY + 17,
                                (new Color(66, 134, 245)).getRGB());
                        RenderUtils.circle((posX + optionDouble.getTranslate().getX() + 1.5F),  (valuePosY + 16.5F), 2, new Color(66,134,245));
                        Fonts.fontSFUI35.drawString(optionDouble.get().toString(), posX + 110, valuePosY, new Color(160,160,160).getRGB());
                        if (this.isHovered(posX + 8, valuePosY + 14F, posX + 120, valuePosY + 19, mouseX, mouseY) && !mouseClicked && Mouse.isButtonDown(0))
                            optionDouble.set(Math.round((optionDouble.getMinimum() + (optionDouble.getMaximum() - optionDouble.getMinimum()) * Math.min(max, 1.0)) * 100.0) / 100.0);
                    }

                    if(i == module.getNumberValues().size() - 1 && i == 0 && module.getValues().size() > 1) {
                        moduleY += 15;
                        valuePosY += 5;
                    }

                    if(i == module.getNumberValues().size() - 1) {
                        if (module.getName().equalsIgnoreCase("TeleportAura"))
                            valuePosY += 20;
                        if (module.getName().equalsIgnoreCase("ChestStealer"))
                            valuePosY += 15;
                        if (module.getName().equalsIgnoreCase("InvManager"))
                            valuePosY += 20;
                    }

                }
                float xPos;
                for(int i = 0; i < module.getListValues().size(); ++i) {
                    ListValue optionMode = module.getListValues().get(i);
                    xPos = startX + 8;
                    if(i == 0) {
                        valuePosY += 20;
                        moduleY += 20;
                    } else {
                        valuePosY += 15;
                        moduleY += 15;
                    }
                    if(module.getValues().size() == 1)
                        valuePosY += 10;
                    Fonts.fontSFUI35.drawString(optionMode.getName(), xPos, valuePosY, new Color(98,154,247).getRGB());
                    valuePosY += 20;
                    moduleY += 20;
                    for(String mode : optionMode.getValues()) {
                        RenderUtils.drawNoFullCircle(xPos + 5,valuePosY, 4,new Color(98,154,247).getRGB());
                        if(optionMode.isMode(mode))
                            RenderUtils.circle(xPos + 5,valuePosY, 2, new Color(129,173,248).getRGB());
                        if(this.isHovered(xPos - 1, valuePosY - 8, xPos + 10, valuePosY + 9, mouseX, mouseY))
                            if(Mouse.isButtonDown(0)) {
                                if(!mouseClicked)
                                    optionMode.set(mode);
                                mouseClicked = true;
                            } else mouseClicked = false;
                        xPos += 14;
                        Fonts.fontSFUI35.drawString(mode, xPos, valuePosY - 7, new Color(69,134,245).getRGB());
                        xPos += Fonts.fontSFUI35.getStringWidth(mode) + 10;
                        if(xPos > windowsX + 340 && optionMode.indexOf(mode) != optionMode.getValues().length - 1) {
                            valuePosY += 20;
                            xPos = startX + 8;
                            moduleY += 25;
                        }
                    }
                }
                xPos = startX + 8;
                if(module.getBooleanValues().size() > 8 && module.getBooleanValues().size() < 13)
                    moduleY += 30;
                if(module.getName().equalsIgnoreCase("PacketFixer"))
                    valuePosY += 20;
                if(module.getName().equalsIgnoreCase("TargetStrafe"))
                    valuePosY += 15;
                for(int i = 0; i < module.getBooleanValues().size(); ++i) {
                    final BoolValue optionBoolean = module.getBooleanValues().get(i);
                    if(i == 0) {
                        moduleY += 25;
                        valuePosY += 10;
                        if(module.getName().equalsIgnoreCase("CivBreak"))
                            valuePosY += 20;
                        if(module.getName().equalsIgnoreCase("3dTags"))
                            valuePosY += 10;
                        if(module.getListValues().size() == 0 && module.getNumberValues().size() != 0) {
                            valuePosY += 10;
                            moduleY += 10;
                        }

                        if(module.getListValues().size() == 0 && module.getNumberValues().size() == 0 && module.getListValues().size() == 0) {
                            valuePosY += 30;
                            moduleY += 30;
                        }
                    }
                    if(module.getValues().size() == 1)
                        valuePosY += 20;

                    Fonts.fontSFUI35.drawString(optionBoolean.getName(), xPos + Fonts.fontSFUI35.getStringWidth("j") + 4, valuePosY - 2, new Color(98,154,247).getRGB());
                    Fonts.fontSFUI35.drawString("j", xPos, valuePosY, optionBoolean.get() ? new Color(98,154,247).getRGB() : new Color(205,205,205).getRGB());
                    if(this.isHovered(xPos, valuePosY + 1,xPos + 7.5F, valuePosY + 9,mouseX,mouseY))
                        if(Mouse.isButtonDown(0)) {
                            if(!mouseClicked)
                                optionBoolean.set(!optionBoolean.get());
                            mouseClicked = true;
                        } else mouseClicked = false;

                    xPos += Fonts.fontSFUI35.getStringWidth(optionBoolean.getName()) + Fonts.fontSFUI35.getStringWidth("j") + 10;
                    if(xPos > windowsX + 310) {
                        valuePosY += 15;
                        xPos = startX + 8;
                        moduleY += 5;
                    }
                }
                if(module.getName().equalsIgnoreCase("AntiBot"))
                    moduleY += 15;
            }
            moduleY += 50;
        }
        // }
        RenderUtils.stopGlScissor();//Stop Scissor Box
        int wheel = Mouse.getDWheel();//Mouse wheel
        float moduleHeight = moduleY - translate.getY();
        if (Mouse.hasWheel() && mouseX > windowsX + 100 && mouseY > windowsY && mouseX < windowsX + 400 && mouseY < windowsY + 250) {
            if (wheel > 0 && mouseWheel < 0) {
                for (int i = 0; i < 5; i++) {
                    if (!(mouseWheel < 0))
                        break;
                    mouseWheel += 7;
                }
            } else {
                for (int i = 0; i < 5; i++) {
                    if (!(wheel < 0 && moduleHeight > 158 && Math.abs(mouseWheel) < (moduleHeight - 200)))
                        break;
                    mouseWheel -= 7;
                }
            }
        }
        translate.interpolate(0, mouseWheel, 0.15F);

        configTranslate.interpolate(0,showConfig ? LiquidBounce.fileManager.profilesConfigs.size() * 20 : 0, 0.1);
        float posY = configTranslate.getY();
        if(isHovered(width - 80, height - 30 - posY, width - 20, height - 10 - posY,mouseX, mouseY)) {
            if(Mouse.isButtonDown(0)) {
                if(!mouseClicked)
                    showConfig = !showConfig && LiquidBounce.fileManager.profilesConfigs.size() > 0;
                mouseClicked = true;
            } else mouseClicked = false;
        }
        if(isHovered(width - 20, height - 30 - posY, width, height - 10 - posY,mouseX, mouseY)) {
            if(Mouse.isButtonDown(0)) {
                if(!mouseClicked)
                    mc.displayGuiScreen(new ProfilesScreen(this));
                mouseClicked = true;
            } else mouseClicked = false;
        }
        RenderUtils.drawRect(width - 80, height - 30 - posY, width - 5, height - 28,new Color(65,133,242).getRGB());
        RenderUtils.drawRect(width - 80, height - 28 - posY, width - 5, height - 10, new Color(255,255,255).getRGB());//Config Info
        if(showConfig) {
            RenderUtils.drawGradientSidewaysV(width - 80, height - 10 - posY, width - 5, height - posY, new Color(255, 255, 255, 30).getRGB(), new Color(0, 0, 0, 50).getRGB());
            for (int index = 0;index < LiquidBounce.fileManager.profilesConfigs.size(); ++index) {
                ProfilesConfig config = LiquidBounce.fileManager.profilesConfigs.get(index);
                int yPos1 = index * 20;
                if(this.isHovered(width - 80, height - posY + yPos1 - 8, width, height - posY + yPos1 + 10,mouseX,mouseY)) {
                    if(Mouse.isButtonDown(0)) {
                        if(!mouseClicked) {
                            try {
                                config.loadConfig();
                                currentConfigName = config.getFile().getName().split(".profile")[0];
                            } catch (Exception exception) {
                                exception.printStackTrace();
                            }
                        }
                        mouseClicked = true;
                    } else mouseClicked = false;
                }
                Fonts.fontSFUI35.drawStringWithColor("", width - 75, height - 3F - posY + yPos1, new Color(160,160,160).getRGB(), false);
                Fonts.fontSFUI35.drawStringWithColor("" + config.getFile().getName().split(".profile")[0], width - 75F + Fonts.fontSFUI35.getCharWidth('v') + 2, height - 4F - posY + yPos1, new Color(160,160,160).getRGB(), false);
            }
        }
        Fonts.fontSFUI35.drawStringWithColor("",width - 75, height - 22 - posY, new Color(160,160,160).getRGB(), false);
        Fonts.fontSFUI35.drawStringWithColor("", width - 20, height - 22 - posY, new Color(160,160,160).getRGB(), false);
        Fonts.fontSFUI35.drawStringWithColor("" + currentConfigName, width - 75F + Fonts.fontSFUI35.getCharWidth('v') + 2, height - 23F - posY, new Color(160,160,160).getRGB(), false);
        super.drawScreen(mouseX,mouseY,partialTicks);
    }
    //Get icon for each category.
    private String getCategoryIcon(ModuleCategory category) {
        switch(category) {
            case COMBAT:
                return "";
            case MOVEMENT:
                return "";
            case RENDER:
                return "";
            case PLAYER:
                return "";
            case WORLD:
                return "";
            default:
                return "";
        }
    }
    //Check cursor hovered something.
    public boolean isHovered(float x, float y, float x2, float y2, int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x2 && mouseY >= y && mouseY <= y2;
    }
}
