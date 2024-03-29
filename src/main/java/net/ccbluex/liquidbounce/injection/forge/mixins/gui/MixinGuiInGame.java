/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.gui;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.event.Render2DEvent;
import net.ccbluex.liquidbounce.features.module.modules.client.HUD;
import net.ccbluex.liquidbounce.features.module.modules.render.Camera;
import net.ccbluex.liquidbounce.features.module.modules.render.Crosshair;
import net.ccbluex.liquidbounce.features.module.modules.world.AutoHypixel;
import net.ccbluex.liquidbounce.features.module.modules.world.Scaffold;
import net.ccbluex.liquidbounce.utils.render.ColorUtils;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GuiIngame.class)
public abstract class MixinGuiInGame extends MixinGui {

    @Shadow
    protected abstract void renderHotbarItem(int index, int xPos, int yPos, float partialTicks, EntityPlayer player);

    @Shadow @Final
    protected static ResourceLocation widgetsTexPath;

    @Shadow public GuiPlayerTabOverlay overlayPlayerList;

    @Inject(method = "showCrosshair", at = @At("HEAD"), cancellable = true) 
    private void injectCrosshair(CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        final Crosshair crossHair = LiquidBounce.moduleManager.getModule(Crosshair.class);
        if (crossHair.getState() && crossHair.noVanillaCH.get())
            callbackInfoReturnable.setReturnValue(false);
    }

    @Inject(method = "renderScoreboard", at = @At("HEAD"), cancellable = true)
    private void renderScoreboard(ScoreObjective scoreObjective, ScaledResolution scaledResolution, CallbackInfo callbackInfo) {
        if (scoreObjective != null) AutoHypixel.gameMode = ColorUtils.stripColor(scoreObjective.getDisplayName());

        final Camera camera = LiquidBounce.moduleManager.getModule(Camera.class);
        if ((camera.getState() && camera.getScoreBoard().get() && camera.getAntiBlindValue().get()) || LiquidBounce.moduleManager.getModule(HUD.class).getState())
            callbackInfo.cancel();
    }

    @ModifyConstant(method = "renderScoreboard", constant = @Constant(intValue = 553648127))
    private int fixTextBlending(int original) {
        return -1;
    }

    @Inject(method = "renderBossHealth", at = @At("HEAD"), cancellable = true)
    private void renderBossHealth(CallbackInfo callbackInfo) {
        final Camera camera = LiquidBounce.moduleManager.getModule(Camera.class);
        if (camera.getState() && camera.getBossHealth().get() && camera.getAntiBlindValue().get())
            callbackInfo.cancel();
    }


    @Inject(method = "renderPumpkinOverlay", at = @At("HEAD"), cancellable = true)
    private void renderPumpkinOverlay(final CallbackInfo callbackInfo) {
        final Camera camera = LiquidBounce.moduleManager.getModule(Camera.class);

        if(camera.getState() && camera.getPumpkinEffect().get() && camera.getAntiBlindValue().get())
            callbackInfo.cancel();
    }

    /**
     * @author pii4
     * @reason client side hotbar spoof
     */
    @Overwrite
    protected void renderTooltip(ScaledResolution sr, float partialTicks) {
        final HUD hud = LiquidBounce.moduleManager.getModule(HUD.class);

        if (!(Minecraft.getMinecraft().getRenderViewEntity() instanceof EntityPlayer))
            return;

        final Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer entityPlayer = (EntityPlayer) mc.getRenderViewEntity();

        int slot = entityPlayer.inventory.currentItem;
        Scaffold scaffold = LiquidBounce.moduleManager.getModule(Scaffold.class);
        if (scaffold != null && scaffold.getState() && scaffold.getAutoBlockMode().get().equalsIgnoreCase("spoof"))
            slot = scaffold.getSlot();

        if(hud.getState() && (hud.getBlackHotbarValue().get() || hud.getAnimHotbarValue().get())) {
            boolean blackHB = hud.getBlackHotbarValue().get();
            int middleScreen = sr.getScaledWidth() / 2;
            float posInv = hud.getAnimPos(slot * 20F);

            GlStateManager.resetColor();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            mc.getTextureManager().bindTexture(widgetsTexPath);

            float f = this.zLevel;
            this.zLevel = -90.0F;
            GlStateManager.resetColor();

            if (blackHB) {
                RenderUtils.originalRoundedRect(middleScreen - 91, sr.getScaledHeight() - 2, middleScreen + 91, sr.getScaledHeight() - 22, 3F, Integer.MIN_VALUE);
                RenderUtils.originalRoundedRect(middleScreen - 91 + posInv, sr.getScaledHeight() - 2, middleScreen - 91 + posInv + 22, sr.getScaledHeight() - 22, 3F, Integer.MAX_VALUE);
            } else {
                this.drawTexturedModalRect(middleScreen - 91F, sr.getScaledHeight() - 22, 0, 0, 182, 22);
                this.drawTexturedModalRect(middleScreen - 91F + posInv - 1, sr.getScaledHeight() - 22 - 1, 0, 22, 24, 22);
            }

            this.zLevel = f;
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableRescaleNormal();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            RenderHelper.enableGUIStandardItemLighting();

            for (int j = 0; j < 9; ++j) {
                int k = sr.getScaledWidth() / 2 - 90 + j * 20 + 2;
                int l = sr.getScaledHeight() - 19 - (blackHB ? 1 : 0);
                this.renderHotbarItem(j, k, l, partialTicks, entityPlayer);
            }

            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableRescaleNormal();
            GlStateManager.disableBlend();
            GlStateManager.resetColor();
            LiquidBounce.eventManager.callEvent(new Render2DEvent(partialTicks));
            return;
        }

        // original

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(widgetsTexPath);
        EntityPlayer lvt_3_1_ = (EntityPlayer)Minecraft.getMinecraft().getRenderViewEntity();
        int lvt_4_1_ = sr.getScaledWidth() / 2;
        float lvt_5_1_ = this.zLevel;
        this.zLevel = -90.0F;
        this.drawTexturedModalRect(lvt_4_1_ - 91, sr.getScaledHeight() - 22, 0, 0, 182, 22);
        this.drawTexturedModalRect(lvt_4_1_ - 91 - 1 + slot * 20, sr.getScaledHeight() - 22 - 1, 0, 22, 24, 22);
        this.zLevel = lvt_5_1_;
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        RenderHelper.enableGUIStandardItemLighting();

        for(int lvt_6_1_ = 0; lvt_6_1_ < 9; ++lvt_6_1_) {
            int lvt_7_1_ = sr.getScaledWidth() / 2 - 90 + lvt_6_1_ * 20 + 2;
            int lvt_8_1_ = sr.getScaledHeight() - 16 - 3;
            this.renderHotbarItem(lvt_6_1_, lvt_7_1_, lvt_8_1_, partialTicks, lvt_3_1_);
        }

        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableBlend();

        LiquidBounce.eventManager.callEvent(new Render2DEvent(partialTicks));
//        AWTFontRenderer.Companion.garbageCollectionTick();
    }
}