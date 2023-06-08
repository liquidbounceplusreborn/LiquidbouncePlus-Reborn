package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.zeroday;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.modules.render.ClickGUI;
import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.ccbluex.liquidbounce.ui.font.GameFontRenderer;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.ccbluex.liquidbounce.value.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import net.ccbluex.liquidbounce.utils.Stencil;

import java.awt.*;
import java.util.ArrayList;

public class Window {
    public ModuleCategory moduleCategory;
    public ArrayList<Button> buttons = new ArrayList<>();
    private final GameFontRenderer TitleFont = Fonts.fontTahoma;
    public float x;
    public float y;
    private int pngWidth;
    private int pngHeight;
    private int pngX;
    private int pngY;
    private int wheel;
    public int mouseWheel;
    public float height;
    public Translate translate = new Translate(0F,0F);
    private boolean dragged;
    private int mouseX2,mouseY2;
    public Window(ModuleCategory category, float x, float y){
        this.moduleCategory = category;
        this.x = x;
        this.y = y;
        float moduleY = 0;
        for (Module module : LiquidBounce.INSTANCE.getModuleManager().getModuleInCategory(moduleCategory)){
             buttons.add(new Button(module,x,y+30+moduleY));
             moduleY+=20;
        }
        for (Button button : buttons){
            button.setPanel(this);
        }
    }
    
    public void render(int tick,int mouseX,int mouseY){
        int guiColor = ClickGUI.generateColor().getRGB();
        if(dragged) {
            x = mouseX2 + mouseX;
            y = mouseY2 + mouseY;

            for (Button b4 : buttons) {
                b4.x = x;
            }
        }

        if (isHovered(x,y,x+125,y+25,mouseX,mouseY) && Mouse.isButtonDown(0)){
            dragged = true;
            mouseX2 = (int) (x - mouseX);
            mouseY2 = (int) (y - mouseY);
        } else {
            dragged=false;
        }
        if (moduleCategory == ModuleCategory.COMBAT){
            pngWidth = 16;
            pngHeight = 16;
            pngX = 5;
            pngY = 4;
        }
        if (moduleCategory == ModuleCategory.PLAYER){
            pngWidth = 15;
            pngHeight = 18;
            pngX = 5;
            pngY = 3;
        }
        if (moduleCategory == ModuleCategory.MOVEMENT){
            pngWidth = 16;
            pngHeight = 11;
            pngX = 5;
            pngY = 6;
        }
        if (moduleCategory == ModuleCategory.RENDER){
            pngWidth = 16;
            pngHeight = 18;
            pngX = 5;
            pngY = 3;
        }
        if (moduleCategory == ModuleCategory.WORLD) {
            pngWidth = 15;
            pngHeight = 18;
            pngX = 6;
            pngY = 3;
        }
        float modY = y + 30;
        for (Button b4 : buttons) {
            b4.translate.interpolate(0, modY, 0.1);
            float iY = b4.translate.getY();
            b4.y = iY + translate.getY();
            if (b4.module.showSettings) {
                for (Value value : b4.module.getValues()) {
                    if (value instanceof BoolValue) {
                        modY += 25;
                    }
                    if (value instanceof IntegerValue){
                        modY+=40;
                    }
                    if (value instanceof FloatValue){
                        modY+=40;
                    }
                    if (value instanceof ListValue){
                            if (((ListValue) value).openList){
                                modY+=((ListValue) value).getValues().length * 15;
                            }
                            modY+=25;
                    }
                }
                if (b4.module.getKeyBind()!=0){
                    modY+=30;
                }
            }
            modY += 24;

        }
        height = buttons.size() * 20 > 300 ? 250 : buttons.size() * 20;
        GlStateManager.disableAlpha();
        GlStateManager.enableBlend();
        GL11.glEnable(3042);
        GL11.glColor4f(1, 1, 1, 1);
        Stencil.write(false);
        Stencil.erase(false);
        net.ccbluex.liquidbounce.utils.render.RenderUtils.drawShadow(x, y,125, height+25);
        Stencil.dispose();
        GlStateManager.disableAlpha();
        GlStateManager.enableBlend();
        GL11.glEnable(3042);
        GL11.glColor4f(1, 1, 1, 1);
        Stencil.write(false);
        Stencil.erase(true);
        Stencil.dispose();
        drawGradientRect(x,y,x+125,y+25,false,guiColor,guiColor);
        net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRect(x,y,x+26,y+25,guiColor);
        net.ccbluex.liquidbounce.utils.render.RenderUtils.drawImage(new ResourceLocation("liquidbounce+/clickgui/zeroday/" + moduleCategory.getDisplayName() + ".png"), (int) (x+pngX), (int) (y+pngY),pngWidth,pngHeight);
        TitleFont.drawString(moduleCategory.getDisplayName(),x+31,y+5, -1);
        GlStateManager.pushMatrix();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        RenderUtils.doGlScissor((int) x, (int) y+25, (int) (x+125), (int) height);
        net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRect(x,y+25,x+125,y+25+height,new Color(56,56,56).getRGB());
        for (Button button : buttons){
            button.render(mouseX,mouseY,this);
        }
        GlStateManager.disableAlpha();
        GlStateManager.enableBlend();
        GL11.glEnable(3042);
        GL11.glColor4f(1, 1, 1, 1);
        Stencil.write(false);
        Stencil.erase(false);
        net.ccbluex.liquidbounce.utils.render.RenderUtils.drawShadow(x,y+25,125,0);
        Stencil.dispose();
        GlStateManager.disableAlpha();
        GlStateManager.enableBlend();
        GL11.glEnable(3042);
        GL11.glColor4f(1, 1, 1, 1);
        Stencil.write(false);
        Stencil.erase(true);
        Stencil.dispose();
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GlStateManager.popMatrix();
        float moduleHeight = modY;
        if (isHovered(x,y+25,x+125,y+25+height,mouseX,mouseY)){
            if (wheel > 0) {
                if (mouseWheel<0) {
                    mouseWheel += 10;
                }
            }
            if (wheel < 0) {
                if (Math.abs(mouseWheel)<(moduleHeight-y-height-30)) {
                    mouseWheel -= 10;
                }
            }
        }
        if (Mouse.hasWheel() && Math.abs(mouseWheel)>((moduleHeight-y-height-30))){
            mouseWheel = (int) -((moduleHeight-y-height-30));
        }
        translate.interpolate(0,mouseWheel,0.1);

    }
    public void setWheel(int wheel){
        this.wheel=wheel;
    }
    public boolean isHovered(float x, float y, float x2, float y2, int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x2 && mouseY >= y && mouseY <= y2;
    }

    public void click(int mouseX,int mouseY,int Button){

        buttons.forEach(button -> button.click(mouseX,mouseY,Button));


    }
    public void drawGradientRect(double left, double top, double right, double bottom, boolean sideways, int startColor, int endColor) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glBegin(GL11.GL_QUADS);
        RenderUtils.color(startColor);
        if (sideways) {
            GL11.glVertex2d(left, top);
            GL11.glVertex2d(left, bottom);
            RenderUtils.color(endColor);
            GL11.glVertex2d(right, bottom);
            GL11.glVertex2d(right, top);
        } else {
            GL11.glVertex2d(left, top);
            RenderUtils.color(endColor);
            GL11.glVertex2d(left, bottom);
            GL11.glVertex2d(right, bottom);
            RenderUtils.color(startColor);
            GL11.glVertex2d(right, top);
        }
        GL11.glEnd();
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }
}
