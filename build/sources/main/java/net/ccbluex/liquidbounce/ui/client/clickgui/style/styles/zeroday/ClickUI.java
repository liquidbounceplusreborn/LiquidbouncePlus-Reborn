package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.zeroday;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.ccbluex.liquidbounce.ui.font.GameFontRenderer;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;
import java.io.*;
import java.util.ArrayList;

public class ClickUI extends GuiScreen {

    ArrayList<Window> windows = new ArrayList<>();
    private final GameFontRenderer TitleFont = Fonts.fontTahoma;
    public ClickUI(){
        float x = 60f;
        float y = 40f;
        for (int i = 0;i<5;i++){
             ModuleCategory moduleCategory = ModuleCategory.values()[i];
             windows.add(new Window(moduleCategory,x,y));
            x+=135;
        }
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(LiquidBounce.INSTANCE.getFileManager().ClickguiDir + "\\clickgui.txt")));
            String line;
            while ((line = bufferedReader.readLine())!=null) {
                for (Window window : windows) {
                    if (line.split(":")[0].equals(window.moduleCategory.getDisplayName())) {
                        window.x = Float.parseFloat(line.split(":")[1]);
                        window.y = Float.parseFloat(line.split(":")[2]);
                        window.mouseWheel = (int) Float.parseFloat(line.split(":")[3]);
                        for (Button b4 : window.buttons) {
                            b4.x = window.x;
                            b4.y = window.y;
                        }
                    }
                }
            }
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        int tick = 1;
        int wheel = Mouse.getDWheel();
        for (Window window : windows){
            window.setWheel(wheel);
            window.render(tick,mouseX,mouseY);
            tick+=2;
        }

    }
    
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        for (Window window : windows){
            window.click(mouseX,mouseY,mouseButton);
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);

    }
    
    @Override
    public void onGuiClosed() {
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(LiquidBounce.INSTANCE.getFileManager().ClickguiDir + "\\clickgui.txt")));
            for (Window window : windows){
                bufferedWriter.write(window.moduleCategory.getDisplayName() + ":"+window.x+":"+window.y+":"+window.mouseWheel);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


        super.onGuiClosed();
    }
}
