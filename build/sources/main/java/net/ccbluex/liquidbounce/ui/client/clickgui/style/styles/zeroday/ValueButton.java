package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.zeroday;

import net.ccbluex.liquidbounce.features.module.modules.render.ClickGUI;
import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.ccbluex.liquidbounce.ui.font.GameFontRenderer;
import net.ccbluex.liquidbounce.utils.render.ColorUtils;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.ccbluex.liquidbounce.value.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.common.patcher.ClassPatch;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import net.ccbluex.liquidbounce.utils.Colors;

import java.awt.*;

public class ValueButton {
    public final Value value;
    public String name;
    public boolean custom;
    public boolean change;
    public int x;
    public GameFontRenderer ValueFont = Fonts.fontSFUI35;
    public float y;
    private float animationY;
    public Translate translate = new Translate(0F,0F);
    public float listY;
    public ValueButton(Value value, int x, float y) {
        this.value = value;
        this.x = x;
        this.y = y;
    }
    
    public void render(int mouseX, int mouseY, Window parent) {
        int guiColor = ClickGUI.generateColor().getRGB();
        animationY = y;
        int left = guiColor;
        int right = guiColor;
        if (value instanceof BoolValue){
            RenderUtils.drawRect(x+5,animationY-5,x+120,animationY+15,new Color(46,46,46));
            ValueFont.drawString(value.getName(), (float) (x+8), (float) (animationY+2.5),-1);
            if (((BoolValue) value).get()){
                parent.drawGradientRect(x+106,animationY,x+116,animationY+10,true, guiColor,guiColor);
            }else{
                parent.drawGradientRect(x+106,animationY,x+116,animationY+10,true, new Color(56,56,56).getRGB(),new Color(56,56,56).getRGB());
            }
        }
        GlStateManager.resetColor();
        if (value instanceof IntegerValue){
            RenderUtils.drawRect(x+5,animationY-5,x+120,animationY+30,new Color(46,46,46));
            ValueFont.drawString(value.getName(), (float) (x+11), animationY+2,-1);
            float posX = x+14;
            final double max = Math.max(0.0, (mouseX - (posX)) / 90);
            IntegerValue optionInt = (IntegerValue) value;
            optionInt.getTranslate().interpolate((90F * (optionInt.get() > optionInt.getMaximum() ? optionInt.getMaximum() : optionInt.get() < optionInt.getMinimum() ? 0 : optionInt.get() - optionInt.getMinimum()) / (optionInt.getMaximum() - optionInt.getMinimum())), 0, 0.1);
            RenderUtils.drawRect(posX, animationY+15, posX+90, animationY+19,(new Color(56,56,56)).getRGB());

            parent.drawGradientRect(posX, animationY+15, posX+(optionInt.getTranslate().getX()-2),animationY+19,true,left,right);
            parent.drawGradientRect(posX+(optionInt.getTranslate().getX()-1), animationY+12, posX+(optionInt.getTranslate().getX()+3),animationY+22,true,guiColor,guiColor);
            ValueFont.drawString(String.valueOf(value.get()), posX+(optionInt.getTranslate().getX()+5), animationY+15,-1);
            if (this.isHovered(posX,animationY+15, posX+90,animationY+19, mouseX, mouseY) && Mouse.isButtonDown(0))
                optionInt.set(Math.toIntExact(Math.round(optionInt.getMinimum() + (optionInt.getMaximum() - optionInt.getMinimum()) * Math.min(max, 1.0))));
        }
        if(value instanceof FloatValue){
            RenderUtils.drawRect(x+5,animationY-5,x+120,animationY+30,new Color(46,46,46));
            ValueFont.drawString(value.getName(), (float) (x+11), animationY+2,-1);
            float posX = x+14;
            final double max = Math.max(0.0, (mouseX - (posX)) / 90);
            FloatValue optionInt = (FloatValue) value;
            optionInt.getTranslate().interpolate((90F * (optionInt.get() > optionInt.getMaximum() ? optionInt.getMaximum() : optionInt.get() < optionInt.getMinimum() ? 0 : optionInt.get() - optionInt.getMinimum()) / (optionInt.getMaximum() - optionInt.getMinimum())), 0, 0.1);
            RenderUtils.drawRect(posX, animationY+15, posX+90, animationY+19,(new Color(56,56,56)).getRGB());

            parent.drawGradientRect(posX, animationY+15, posX+(optionInt.getTranslate().getX()-2),animationY+19,true,left,right);
            parent.drawGradientRect(posX+(optionInt.getTranslate().getX()-1), animationY+12, posX+(optionInt.getTranslate().getX()+3),animationY+22,true,guiColor,guiColor);
            ValueFont.drawString(String.valueOf(value.get()), posX+(optionInt.getTranslate().getX()+5), animationY+15,-1);
            if (this.isHovered(posX,animationY+15, posX+90,animationY+19, mouseX, mouseY) && Mouse.isButtonDown(0))
                optionInt.set(Math.round((optionInt.getMinimum() + (optionInt.getMaximum() - optionInt.getMinimum()) * Math.min(max, 1.0)) * 100.0) / 100.0);
        }
        if (value instanceof ListValue){
            RenderUtils.drawRect(x+5,animationY-5,x+120,animationY+15 + (((ListValue) value).openList ? ((ListValue) value).getValues().length * 15 :0),new Color(46,46,46));
            Fonts.fontSFUI35.drawString(value.getName(), (float) (x+10), (float) (animationY+2.5),-1);
            Fonts.fontSFUI35.drawString(((ListValue) value).get(),(float) (x+120-Fonts.fontSFUI35.getStringWidth(((ListValue) value).get())-20), (float) (animationY+2.5),-1);
            drawAndRotateArrow((x+106)-0.2f,animationY  + 2 + 1f,8.0f,((ListValue) value).openList);
            if (((ListValue) value).openList){
                listY = animationY+16;
                for (String value1 : ((ListValue) value).getValues()){
                    Fonts.fontSFUI35.drawString("n",x+11,listY,guiColor);
                    Fonts.fontSFUI35.drawString(value1,(float) (x+28), listY+1,-1);
                    if (value1.equals(((ListValue) value).getValue())){
                        RenderUtils.drawRoundedRect(x+13.2f,listY-0.2F, 8,8,13f,guiColor,1f,guiColor);
                    }
                    listY+=15;
                }
            }
        }
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
    public void click(int mouseX,int mouseY,int Button){
         if (Button == 0){
             if (isHovered(x+106,animationY,x+116,animationY+10,mouseX,mouseY)){
                 if (value instanceof BoolValue){
                     value.set(!((BoolValue) value).get());
                 }
             }
             if (value instanceof ListValue){
                 if (((ListValue) value).openList){
                     listY = animationY+16;
                     for (String value1 : ((ListValue) value).getValues()){
                         if (isHovered(x+11,listY-1,x+24,listY+9,mouseX,mouseY)){
                             value.set(value1);
                         }
                         listY+=15;
                     }
                 }
             }
         }
         if (Button == 1){
             if (isHovered(x+5,animationY-5,x+120,animationY+15,mouseX,mouseY)){
                 if (value instanceof ListValue){
                     ((ListValue) value).openList=!((ListValue) value).openList;
                 }
             }
         }
    }
    public boolean isHovered(float x, float y, float x2, float y2, int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x2 && mouseY >= y && mouseY <= y2;
    }
}
