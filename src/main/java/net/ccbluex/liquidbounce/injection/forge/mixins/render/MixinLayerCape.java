package net.ccbluex.liquidbounce.injection.forge.mixins.render;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.features.module.modules.render.PlayerEdit;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerCape;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.util.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Objects;

@Mixin(LayerCape.class)
public class MixinLayerCape {

    @Final
    @Shadow
    private RenderPlayer playerRenderer;

    /**
     * @author Randomguy && wxdbie
     * @reason for PlayerEdit
     */
    @Overwrite
    public void doRenderLayer(AbstractClientPlayer p_doRenderLayer_1_, float p_doRenderLayer_2_, float p_doRenderLayer_3_, float p_doRenderLayer_4_, float p_doRenderLayer_5_, float p_doRenderLayer_6_, float p_doRenderLayer_7_, float p_doRenderLayer_8_) {
        if (PlayerEdit.baby.get() && Objects.requireNonNull(LiquidBounce.moduleManager.getModule(PlayerEdit.class)).getState()) {
            return;
        }
        if (p_doRenderLayer_1_.hasPlayerInfo() && !p_doRenderLayer_1_.isInvisible() && p_doRenderLayer_1_.isWearing(EnumPlayerModelParts.CAPE) && p_doRenderLayer_1_.getLocationCape() != null) {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.playerRenderer.bindTexture(p_doRenderLayer_1_.getLocationCape());
            GlStateManager.pushMatrix();
            GlStateManager.translate(0.0F, 0.0F, 0.125F);
            double lvt_9_1_ = p_doRenderLayer_1_.prevChasingPosX + (p_doRenderLayer_1_.chasingPosX - p_doRenderLayer_1_.prevChasingPosX) * (double)p_doRenderLayer_4_ - (p_doRenderLayer_1_.prevPosX + (p_doRenderLayer_1_.posX - p_doRenderLayer_1_.prevPosX) * (double)p_doRenderLayer_4_);
            double lvt_11_1_ = p_doRenderLayer_1_.prevChasingPosY + (p_doRenderLayer_1_.chasingPosY - p_doRenderLayer_1_.prevChasingPosY) * (double)p_doRenderLayer_4_ - (p_doRenderLayer_1_.prevPosY + (p_doRenderLayer_1_.posY - p_doRenderLayer_1_.prevPosY) * (double)p_doRenderLayer_4_);
            double lvt_13_1_ = p_doRenderLayer_1_.prevChasingPosZ + (p_doRenderLayer_1_.chasingPosZ - p_doRenderLayer_1_.prevChasingPosZ) * (double)p_doRenderLayer_4_ - (p_doRenderLayer_1_.prevPosZ + (p_doRenderLayer_1_.posZ - p_doRenderLayer_1_.prevPosZ) * (double)p_doRenderLayer_4_);
            float lvt_15_1_ = p_doRenderLayer_1_.prevRenderYawOffset + (p_doRenderLayer_1_.renderYawOffset - p_doRenderLayer_1_.prevRenderYawOffset) * p_doRenderLayer_4_;
            double lvt_16_1_ = (double) MathHelper.sin(lvt_15_1_ * 3.1415927F / 180.0F);
            double lvt_18_1_ = (double)(-MathHelper.cos(lvt_15_1_ * 3.1415927F / 180.0F));
            float lvt_20_1_ = (float)lvt_11_1_ * 10.0F;
            lvt_20_1_ = MathHelper.clamp_float(lvt_20_1_, -6.0F, 32.0F);
            float lvt_21_1_ = (float)(lvt_9_1_ * lvt_16_1_ + lvt_13_1_ * lvt_18_1_) * 100.0F;
            float lvt_22_1_ = (float)(lvt_9_1_ * lvt_18_1_ - lvt_13_1_ * lvt_16_1_) * 100.0F;
            if (lvt_21_1_ < 0.0F) {
                lvt_21_1_ = 0.0F;
            }

            float lvt_23_1_ = p_doRenderLayer_1_.prevCameraYaw + (p_doRenderLayer_1_.cameraYaw - p_doRenderLayer_1_.prevCameraYaw) * p_doRenderLayer_4_;
            lvt_20_1_ += MathHelper.sin((p_doRenderLayer_1_.prevDistanceWalkedModified + (p_doRenderLayer_1_.distanceWalkedModified - p_doRenderLayer_1_.prevDistanceWalkedModified) * p_doRenderLayer_4_) * 6.0F) * 32.0F * lvt_23_1_;
            if (p_doRenderLayer_1_.isSneaking()) {
                lvt_20_1_ += 25.0F;
            }

            GlStateManager.rotate(6.0F + lvt_21_1_ / 2.0F + lvt_20_1_, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(lvt_22_1_ / 2.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(-lvt_22_1_ / 2.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
            this.playerRenderer.getMainModel().renderCape(0.0625F);
            GlStateManager.popMatrix();
        }
    }

}
