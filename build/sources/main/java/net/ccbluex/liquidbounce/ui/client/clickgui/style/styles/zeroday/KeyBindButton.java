package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.zeroday;

import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;

import java.awt.*;

public class KeyBindButton {
    int key;
    String keyName;
    public float x;
    public float y;
    public KeyBindButton(int key,String keyName,float x,float y){
        this.key=key;
        this.keyName=keyName;
        this.x=x;
        this.y=y;
    }
    
    public void render(){
        RenderUtils.drawRect(x+5,y-5,x+120,y+15,new Color(46,46,46));
        RenderUtils.drawRect(x+106,y,x+116,y+10,new Color(56,56,56).getRGB());
        Fonts.fontSFUI35.drawStringWithShadow("KeyBind",x+8, (float) (y+2.5),-1);
        Fonts.fontSFUI35.drawStringWithShadow(keyName,x+108, (float) (y+2.5),-1);
    }

}
