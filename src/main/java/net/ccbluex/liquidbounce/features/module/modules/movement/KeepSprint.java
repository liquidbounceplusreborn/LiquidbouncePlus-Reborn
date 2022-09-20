/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement;

import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@ModuleInfo(name = "KeepSprint", description = "Keep sprint", category = ModuleCategory.MOVEMENT)
public class KeepSprint extends Module {

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
            if (mc.gameSettings.keyBindForward.isKeyDown())
            if (!mc.thePlayer.isSprinting()) mc.thePlayer.setSprinting(true);
    }
}