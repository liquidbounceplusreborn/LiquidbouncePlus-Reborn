/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.crash;

import net.minecraft.crash.CrashReport;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CrashReport.class)
public class MixinCrashReport {
/*
    @Inject(method = "populateEnvironment", at = @At("TAIL"))
    private void injectCrashEnv(CallbackInfo callbackInfo) {
		  wdl.WDLHooks.onCrashReportPopulateEnvironment((CrashReport) (Object) this);
    }
*/
}