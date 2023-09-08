/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.other;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.features.module.modules.movement.InvMove;
import net.ccbluex.liquidbounce.event.MoveEvent;
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode;
import net.ccbluex.liquidbounce.utils.MovementUtils;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.settings.GameSettings;

public class Legit extends SpeedMode {

    public Legit() {
        super("Legit");
    }

    @Override
    public void onMotion() {
        final InvMove invMove = LiquidBounce.moduleManager.getModule(InvMove.class);
        mc.gameSettings.keyBindJump.pressed = ((MovementUtils.isMoving() || GameSettings.isKeyDown(mc.gameSettings.keyBindJump)) && (mc.inGameHasFocus || (invMove.getState() && !(mc.currentScreen instanceof GuiChat || mc.currentScreen instanceof GuiIngameMenu) && (!(mc.currentScreen instanceof GuiContainer)))));
    }

    @Override
    public void onUpdate() {
        
    }

    @Override
    public void onMove(MoveEvent event) {
    }
}
