package net.ccbluex.liquidbounce.ui.client.hud.element.elements;

import akka.util.Switch;
import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.ccbluex.liquidbounce.utils.render.BlurUtils;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.ccbluex.liquidbounce.utils.render.RoundedUtil;
import net.ccbluex.liquidbounce.Liquidbounce;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.ui.client.hud.element.Border;
import net.ccbluex.liquidbounce.ui.client.hud.element.Element;
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo;
import net.ccbluex.liquidbounce.value.BoolValue;
import net.ccbluex.liquidbounce.value.IntegerValue;
import net.ccbluex.liquidbounce.value.ListValue;
import net.ccbluex.liquidbounce.features.module.modules.render.ColorMixer;
import net.ccbluex.liquidbounce.utils.render.ColorUtils;
import net.minecraft.util.ResourceLocation;
import sun.java2d.pipe.DrawImage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;

@ElementInfo(name = "KeyBinds")
public class KeyBinds extends Element {
    public final BoolValue onlyState = new BoolValue("OnlyModuleState", false);

    public final BoolValue linevalue = new BoolValue("Line", false);
    public final IntegerValue cr = new IntegerValue("red",255,0,255);
    public final IntegerValue cg = new IntegerValue("green",255,0,255);
    public final IntegerValue cb = new IntegerValue("blue",255,0,255);
    public final IntegerValue cr2 = new IntegerValue("red2",255,0,255);
    public final IntegerValue cg2 = new IntegerValue("green2",255,0,255);
    public final IntegerValue cb2 = new IntegerValue("blue2",255,0,255);
    public final IntegerValue bga = new IntegerValue("bgalpha",150,0,255);
    public final BoolValue blurValue1 = new BoolValue("blur", false);
    public final IntegerValue blurstr = new IntegerValue("blurvalue",1,1,60);
    public final IntegerValue radiusvalue = new IntegerValue("radius",1,0,20);

    @Override
    public Border drawElement() {
        int y2 =0;


        //draw Background
        RoundedUtil.drawRound(0, 0, 90, 19 + getmoduley(), radiusvalue.get(), new Color(0, 0, 0, bga.get()));
        //blur
        if (blurValue1.get()) {
            BlurUtils.blurAreaRounded(0, 0, 90, 19 + getmoduley(), radiusvalue.get(), blurstr.get());
        }

        //line
        if(linevalue.get()) {
            RenderUtils.drawGradientSideways(2,14,88,15,new Color(cr.get(),cg.get(),cb.get()).getRGB(),new Color(cr2.get(),cg2.get(),cb2.get()).getRGB());
        }
        //draw Title

        Fonts.font35.drawString("KeyBinds", 28, 5.5f, -1, true);

        //draw Module Bind
        for (Module module : Liquidbounce.moduleManager.getModules()) {
            if (module.getKeyBind() == 0) continue;
            if(onlyState.get()) {
                if (!module.getState()) continue;
            }

            Fonts.font35.drawString(module.getName(), 3, y2 + 21f, -1, true);

            Fonts.font35.drawString("[Toggle]", 89 - Fonts.font35.getStringWidth("[Toggle]"), y2 + 21f, module.getState() ? new Color(255, 255, 255).getRGB() : new Color(100,100,100).getRGB(), true);
            y2 += 12;
        }

        return new Border(0,0,84,17+ getmoduley());
    }

    public int getmoduley(){
        int y=0;
        for (Module module: Liquidbounce.moduleManager.getModules()) {
            if (module.getKeyBind() == 0) continue;
            if(onlyState.get()) {
                if (!module.getState()) continue;
            }
            y+=12;
        }

        return y;
    }

}
