package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.zeroday;


import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.modules.render.ClickGUI;
import net.ccbluex.liquidbounce.ui.client.clickgui.elements.ModuleElement;
import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.ccbluex.liquidbounce.ui.font.GameFontRenderer;
import net.ccbluex.liquidbounce.utils.render.ColorUtils;
import net.ccbluex.liquidbounce.value.*;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Button {
    Module module;
    public GameFontRenderer ListFont = Fonts.fontSFUI35;
    public float x;
    public float y;
    public ArrayList<ValueButton> buttons = new ArrayList<>();
    public ArrayList<KeyBindButton> keyBindButtons = new ArrayList<>();
    private Window parent;
    private float animationY;
    public Translate translate = new Translate(0F,0F);
    public Button(Module module,float x,float y){
        this.module = module;
        this.x=x;
        this.y=y;
        int y2 = (int) (y + 5);
        for (Value v : this.module.getValues()) {
            this.buttons.add(new ValueButton(v, (int) (x), y2));
            y2 += 20;
        }
        if (module.getKeyBind()!=0) {
            keyBindButtons.add(new KeyBindButton(module.getKeyBind(), Keyboard.getKeyName(module.getKeyBind()), x, y2));
        }
    }
    
    public void render(int mouseX,int mouseY,Window window){
        int guiColor = ClickGUI.generateColor().getRGB();
        animationY = y;
        if (module.getState()){
            this.parent.drawGradientRect(x,animationY-7,x+125,animationY+17,true, guiColor, guiColor);
        }
        GlStateManager.resetColor();

        ListFont.drawString(module.getName(),x+5,animationY+1,-1);
        if (!module.getValues().isEmpty()) drawAndRotateArrow(x+110-0.2f,animationY  + 3 + 1f,8.0f,module.showSettings);
        if (module.showSettings){
            float modY = animationY+25;
            for (ValueButton b4 : buttons) {
                b4.y = modY;
                b4.x = (int) x;
                if (b4.value instanceof BoolValue){
                    modY+=25;
                }
                if (b4.value instanceof IntegerValue){
                    modY+=40;
                }
                if (b4.value instanceof FloatValue){
                    modY+=40;
                }
                if (b4.value instanceof ListValue){
                    if (((ListValue) b4.value).openList){
                        modY+=((ListValue) b4.value).getValues().length * 15;
                    }
                    modY+=25;
                }
            }
            for (ValueButton b4 : buttons) {
                b4.render(mouseX,mouseY,window);
            }
            if (module.getKeyBind()!=0){
                for (KeyBindButton keyBindButton : keyBindButtons){
                    keyBindButton.y = modY;
                    keyBindButton.x = x;
                }
                for (KeyBindButton keyBindButton : keyBindButtons){
                    keyBindButton.render();
                }
            }
        }
    }
    
    public void click(int mouseX,int mouseY,int Button){
        if (isHovered(parent.x,parent.y+25,parent.x+125,parent.y+25+parent.height,mouseX,mouseY)) {
            if (Button == 0) {
                if (isHovered(x, animationY - 5, x + 125, animationY + 15, mouseX, mouseY)) {
                    module.setState(!module.getState());
                }
            }
            if (Button == 1) {
                if (isHovered(x, animationY - 5, x + 125, animationY + 15, mouseX, mouseY) && !module.getValues().isEmpty()) {
                    module.showSettings = !module.showSettings;
                }
            }
            buttons.forEach(valueButton -> valueButton.click(mouseX, mouseY, Button));
        }
    }
    public boolean isHovered(float x, float y, float x2, float y2, int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x2 && mouseY >= y && mouseY <= y2;
    }
    
    public static void drawAndRotateArrow(float x, float y, float size, boolean rotate) {
        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, 0f);
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(2848);
        GL11.glHint(3154, 4354);
        GL11.glLineWidth(1.0f);
        GL11.glDisable(3553);
        GL11.glBegin(4);

        if (rotate) {
            GL11.glVertex2f(size, (size / 2.0f));
            GL11.glVertex2f((size / 2.0f), 0.0f);
            GL11.glVertex2f(0.0f, (size / 2.0f));
        } else {
            GL11.glVertex2f(0.0f, 0.0f);
            GL11.glVertex2f((size / 2.0f), (size / 2.0f));
            GL11.glVertex2f(size, 0.0f);
        }

        GL11.glEnd();
        GL11.glDisable(2848);
        GL11.glEnable(3553);
        GL11.glDisable(3042);
        GL11.glPopMatrix();
    }
    public void setPanel(Window window){
        this.parent = window;
       /* for (int i = 0; i < this.parent.buttons.size(); ++i) {
            if (this.parent.buttons.get(i) != this) continue;
            index = i;
            break;
        }*/
    }


}
