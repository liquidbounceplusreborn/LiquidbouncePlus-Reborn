/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.entity;

import com.mojang.authlib.GameProfile;
import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.features.module.modules.movement.BowJump;
import net.ccbluex.liquidbounce.features.module.modules.movement.LongJump;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.PlayerCapabilities;
import net.minecraft.item.ItemStack;
import net.minecraft.util.FoodStats;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EntityPlayer.class)
public abstract class MixinEntityPlayer extends MixinEntityLivingBase {

    @Shadow
    public abstract ItemStack getHeldItem();

    @Shadow
    public abstract GameProfile getGameProfile();

    @Shadow
    protected abstract boolean canTriggerWalking();

    @Shadow
    protected abstract String getSwimSound();

    @Shadow
    public abstract FoodStats getFoodStats();

    @Shadow
    protected int flyToggleTimer;

    @Shadow
    public PlayerCapabilities capabilities;

    @Shadow
    public abstract int getItemInUseDuration();

    @Shadow
    public abstract ItemStack getItemInUse();

    @Shadow
    public abstract boolean isUsingItem();

    @Shadow
    public abstract boolean isPlayerSleeping();

    @Shadow
    public float eyeHeight = this.getDefaultEyeHeight();

    @Shadow
    public abstract float getDefaultEyeHeight();

    /**
     * @author
     * @reason
     */
    @Overwrite
    public float getEyeHeight() {
        final Minecraft mc = Minecraft.getMinecraft();
        final LongJump longJump = LiquidBounce.moduleManager.getModule(LongJump.class);
        final BowJump bowJump = LiquidBounce.moduleManager.getModule(BowJump.class);
        if(LiquidBounce.moduleManager.get(LongJump.class).getState() && longJump.fakeValue.get()){
            float f2 = 1.62F;
            final double y = longJump.rendery;
            f2 = (float) (1.62F - (mc.thePlayer.lastTickPosY + (((mc.thePlayer.posY - mc.thePlayer.lastTickPosY) * mc.timer.renderPartialTicks)) - y));
            return f2;
        }
        if(LiquidBounce.moduleManager.get(BowJump.class).getState() && bowJump.fakeValue.get()){
            float f2 = 1.62F;
            final double y = bowJump.rendery;
            f2 = (float) (1.62F - (mc.thePlayer.lastTickPosY + (((mc.thePlayer.posY - mc.thePlayer.lastTickPosY) * mc.timer.renderPartialTicks)) - y));
            return f2;
        }
        else {
            float f = this.eyeHeight;
            if (this.isPlayerSleeping()) {
                f = 0.2F;
            }

            if (this.isSneaking()) {
                f -= 0.08F;
            }
            return f;
        }
    }

}