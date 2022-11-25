/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.player;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;

import net.ccbluex.liquidbounce.value.*;

import java.util.HashMap;

@ModuleInfo(name = "Patcher", description = "improving your experience without bloatware, aka. Essential.", category = ModuleCategory.PLAYER, canEnable = false)
public class Patcher extends Module {

    public static final BoolValue noHitDelay = new BoolValue("NoHitDelay", false);
    public static final BoolValue jumpPatch = new BoolValue("JumpFix", true);
    public static final BoolValue silentNPESP = new BoolValue("SilentNPE-SpawnPlayer", true);
}