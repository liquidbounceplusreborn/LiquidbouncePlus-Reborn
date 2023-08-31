package net.ccbluex.liquidbounce.injection.forge.mixins.render;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.features.module.modules.render.Cape;
import net.ccbluex.liquidbounce.features.module.modules.render.PlayerEdit;
import net.ccbluex.liquidbounce.features.module.modules.render.Rotations;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerCape;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
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
        if (Objects.requireNonNull(LiquidBounce.moduleManager.getModule(PlayerEdit.class)).getState() && PlayerEdit.customModel.get()){
            return;
        }
        final Cape cape = Objects.requireNonNull(LiquidBounce.moduleManager.getModule(Cape.class));
        if (!p_doRenderLayer_1_.isInvisible() && p_doRenderLayer_1_ == Minecraft.getMinecraft().thePlayer && p_doRenderLayer_1_.isWearing(EnumPlayerModelParts.CAPE) && p_doRenderLayer_1_.getLocationCape() != null) {
            if (cape.getMovingModeValue().get().equals("Smooth")) {
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                this.playerRenderer.bindTexture(p_doRenderLayer_1_.getLocationCape());
                GlStateManager.pushMatrix();
                GlStateManager.translate(0.0F, 0.0F, 0.125F);
                final double d0 = p_doRenderLayer_1_.prevChasingPosX + (p_doRenderLayer_1_.chasingPosX - p_doRenderLayer_1_.prevChasingPosX) * (double) p_doRenderLayer_4_ - (p_doRenderLayer_1_.prevPosX + (p_doRenderLayer_1_.posX - p_doRenderLayer_1_.prevPosX) * (double) p_doRenderLayer_4_);
                final double d1 = p_doRenderLayer_1_.prevChasingPosY + (p_doRenderLayer_1_.chasingPosY - p_doRenderLayer_1_.prevChasingPosY) * (double) p_doRenderLayer_4_ - (p_doRenderLayer_1_.prevPosY + (p_doRenderLayer_1_.posY - p_doRenderLayer_1_.prevPosY) * (double) p_doRenderLayer_4_);
                final double d2 = p_doRenderLayer_1_.prevChasingPosZ + (p_doRenderLayer_1_.chasingPosZ - p_doRenderLayer_1_.prevChasingPosZ) * (double) p_doRenderLayer_4_ - (p_doRenderLayer_1_.prevPosZ + (p_doRenderLayer_1_.posZ - p_doRenderLayer_1_.prevPosZ) * (double) p_doRenderLayer_4_);
                final float f = p_doRenderLayer_1_.prevRotationYaw + (p_doRenderLayer_1_.rotationYaw - p_doRenderLayer_1_.prevRotationYaw) * p_doRenderLayer_4_;
                final double d3 = MathHelper.sin(f * (float) Math.PI / 180.0F);
                final double d4 = -MathHelper.cos(f * (float) Math.PI / 180.0F);
                float f1 = (float) d1 * 10.0F;
                f1 = MathHelper.clamp_float(f1, -6.0F, 32.0F);
                float f2 = (float) (d0 * d3 + d2 * d4) * 100.0F;
                final float f3 = (float) (d0 * d4 - d2 * d3) * 100.0F;

                if (f2 < 0.0F) {
                    f2 = 0.0F;
                }

                if (f2 > 165.0F) {
                    f2 = 165.0F;
                }

                if (f1 < -5.0F) {
                    f1 = -5.0F;
                }

                final float f4 = p_doRenderLayer_1_.prevCameraYaw + (p_doRenderLayer_1_.cameraYaw - p_doRenderLayer_1_.prevCameraYaw) * p_doRenderLayer_4_;
                f1 = f1 + MathHelper.sin((p_doRenderLayer_1_.prevDistanceWalkedModified + (p_doRenderLayer_1_.distanceWalkedModified - p_doRenderLayer_1_.prevDistanceWalkedModified) * p_doRenderLayer_4_) * 6.0F) * 32.0F * f4;

                if (p_doRenderLayer_1_.isSneaking()) {
                    f1 += 25.0F;
                    GlStateManager.translate(0.0F, 0.142F, -0.0178F);
                }

                GlStateManager.rotate(6.0F + f2 / 2.0F + f1, 1.0F, 0.0F, 0.0F);
                GlStateManager.rotate(f3 / 2.0F, 0.0F, 0.0F, 1.0F);
                GlStateManager.rotate(-f3 / 2.0F, 0.0F, 1.0F, 0.0F);
                GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
                this.playerRenderer.getMainModel().renderCape(0.0625F);
                if (cape.getStyleValue().get().equals("Exhibition")) {
                    this.playerRenderer.bindTexture(new ResourceLocation("liquidbounce+/cape/overlay.png"));
                    float alpha;
                    float red;
                    int rgb;
                    float green;
                    rgb = RenderUtils.getRainbowOpaque(2, 0.55f, 0.9f, 0);
                    alpha = 0.3F;
                    red = (float) (rgb >> 16 & 255) / 255.0F;
                    green = (float) (rgb >> 8 & 255) / 255.0F;
                    float blue = (float) (rgb & 255) / 255.0F;
                    GlStateManager.color(red, green, blue, alpha);
                    this.playerRenderer.getMainModel().renderCape(0.0625F);
                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                }
                GlStateManager.popMatrix();
            }
            if (cape.getMovingModeValue().get().equals("Vanilla")) {
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                this.playerRenderer.bindTexture(p_doRenderLayer_1_.getLocationCape());
                GL11.glPushMatrix();
                GL11.glTranslatef(0.0F, 0.0F, 0.125F);
                double d0 = p_doRenderLayer_1_.prevChasingPosX + (p_doRenderLayer_1_.chasingPosX - p_doRenderLayer_1_.prevChasingPosX) * (double) p_doRenderLayer_4_ - (p_doRenderLayer_1_.prevPosX + (p_doRenderLayer_1_.posX - p_doRenderLayer_1_.prevPosX) * (double) p_doRenderLayer_4_);
                double d1 = p_doRenderLayer_1_.prevChasingPosY + (p_doRenderLayer_1_.chasingPosY - p_doRenderLayer_1_.prevChasingPosY) * (double) p_doRenderLayer_4_ - (p_doRenderLayer_1_.prevPosY + (p_doRenderLayer_1_.posY - p_doRenderLayer_1_.prevPosY) * (double) p_doRenderLayer_4_);
                double d2 = p_doRenderLayer_1_.prevChasingPosZ + (p_doRenderLayer_1_.chasingPosZ - p_doRenderLayer_1_.prevChasingPosZ) * (double) p_doRenderLayer_4_ - (p_doRenderLayer_1_.prevPosZ + (p_doRenderLayer_1_.posZ - p_doRenderLayer_1_.prevPosZ) * (double) p_doRenderLayer_4_);
                float f = p_doRenderLayer_1_.prevRenderYawOffset + (p_doRenderLayer_1_.renderYawOffset - p_doRenderLayer_1_.prevRenderYawOffset) * p_doRenderLayer_4_;
                double d3 = MathHelper.sin(f * 3.1415927F / 180.0F);
                double d4 = -MathHelper.cos(f * 3.1415927F / 180.0F);
                float f1 = (float) d1 * 10.0F;
                f1 = MathHelper.clamp_float(f1, -6.0F, 32.0F);
                float f2 = (float) (d0 * d3 + d2 * d4) * 100.0F;
                float f3 = (float) (d0 * d4 - d2 * d3) * 100.0F;

                if (f2 < 0.0F) {
                    f2 = 0.0F;
                }

                if (f2 > 165.0F) {
                    f2 = 165.0F;
                }

                if (f1 < -5.0F) {
                    f1 = -5.0F;
                }

                float f4 = p_doRenderLayer_1_.prevCameraYaw + (p_doRenderLayer_1_.cameraYaw - p_doRenderLayer_1_.prevCameraYaw) * p_doRenderLayer_4_;
                f1 += MathHelper.sin((p_doRenderLayer_1_.prevDistanceWalkedModified + (p_doRenderLayer_1_.distanceWalkedModified - p_doRenderLayer_1_.prevDistanceWalkedModified) * p_doRenderLayer_4_) * 6.0F) * 32.0F * f4;

                if (p_doRenderLayer_1_.isSneaking()) {
                    f1 += 25.0F;
                    GlStateManager.translate(0.0F, 0.142F, -0.0178F);
                }

                GL11.glRotatef(6.0F + f2 / 2.0F + f1, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(f3 / 2.0F, 0.0F, 0.0F, 1.0F);
                GL11.glRotatef(-f3 / 2.0F, 0.0F, 1.0F, 0.0F);
                GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
                this.playerRenderer.getMainModel().renderCape(0.0625F);
                if (cape.getStyleValue().get().equals("Exhibition")) {
                    this.playerRenderer.bindTexture(new ResourceLocation("liquidbounce+/cape/overlay.png"));
                    float alpha;
                    float red;
                    int rgb;
                    float green;
                    rgb = RenderUtils.getRainbowOpaque(2, 0.55f, 0.9f, 0);
                    alpha = 0.3F;
                    red = (float) (rgb >> 16 & 255) / 255.0F;
                    green = (float) (rgb >> 8 & 255) / 255.0F;
                    float blue = (float) (rgb & 255) / 255.0F;
                    GL11.glColor4f(red, green, blue, alpha);
                    this.playerRenderer.getMainModel().renderCape(0.0625F);
                    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                }
                GL11.glPopMatrix();
            }
        }
    }
    /**
     * @author
     * @reason
     */
    @Overwrite
    public boolean shouldCombineTextures() {
        return false;
    }

}
