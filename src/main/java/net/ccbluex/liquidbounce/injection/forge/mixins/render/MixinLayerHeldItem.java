/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.render;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura;
import net.ccbluex.liquidbounce.features.module.modules.render.Animations;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Objects;
import java.util.UUID;

@Mixin(LayerHeldItem.class)
public class MixinLayerHeldItem {

    @Shadow
    @Final
    private RendererLivingEntity<?> livingEntityRenderer;

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void doRenderLayer(EntityLivingBase entitylivingbaseIn, float p_177141_2_, float p_177141_3_, float partialTicks, float p_177141_5_, float p_177141_6_, float p_177141_7_, float scale) {
        final KillAura killAura = LiquidBounce.moduleManager.getModule(KillAura.class);
        final Animations animations = LiquidBounce.moduleManager.getModule(Animations.class);
        ItemStack itemstack = entitylivingbaseIn.getHeldItem();

        if (itemstack != null) {
            GlStateManager.pushMatrix();

            if (this.livingEntityRenderer.getMainModel().isChild) {
                float f = 0.5F;
                GlStateManager.translate(0.0F, 0.625F, 0.0F);
                GlStateManager.rotate(-20.0F, -1.0F, 0.0F, 0.0F);
                GlStateManager.scale(f, f, f);
            }

            Item item = itemstack.getItem();
            final UUID uuid = entitylivingbaseIn.getUniqueID();
            final EntityPlayer entityplayer = Minecraft.getMinecraft().theWorld.getPlayerEntityByUUID(uuid);

            if (entityplayer != null && entityplayer.isBlocking() || entityplayer != null && killAura.getTarget() != null && animations.getState() && animations.fakeBlock.get() && item instanceof ItemSword && Objects.equals(entityplayer.getGameProfile().getName(), Minecraft.getMinecraft().thePlayer.getGameProfile().getName())) {
                if (entitylivingbaseIn.isSneaking()) {
                    ((ModelBiped) this.livingEntityRenderer.getMainModel()).postRenderArm(0.0325F);
                    GlStateManager.translate(-0.58F, 0.3F, -0.2F);
                    GlStateManager.rotate(-24390.0F, 137290.0F, -2009900.0F, -2054900.0F);
                } else {
                    ((ModelBiped) this.livingEntityRenderer.getMainModel()).postRenderArm(0.0325F);
                    GlStateManager.translate(-0.48F, 0.2F, -0.2F);
                    GlStateManager.rotate(-24390.0F, 137290.0F, -2009900.0F, -2054900.0F);
                }
            } else {
                ((ModelBiped) this.livingEntityRenderer.getMainModel()).postRenderArm(0.0625F);
            }

            GlStateManager.translate(-0.0625F, 0.4375F, 0.0625F);

            if (entitylivingbaseIn instanceof EntityPlayer && ((EntityPlayer) entitylivingbaseIn).fishEntity != null) {
                itemstack = new ItemStack(Items.fishing_rod, 0);
            }

            Minecraft minecraft = Minecraft.getMinecraft();

            if (item instanceof ItemBlock && Block.getBlockFromItem(item).getRenderType() == 2) {
                GlStateManager.translate(0.0F, 0.1875F, -0.3125F);
                GlStateManager.rotate(20.0F, 1.0F, 0.0F, 0.0F);
                GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
                float f1 = 0.375F;
                GlStateManager.scale(-f1, -f1, f1);
            }

            if (entitylivingbaseIn.isSneaking()) {
                GlStateManager.translate(0.0F, 0.203125F, 0.0F);
            }

            minecraft.getItemRenderer().renderItem(entitylivingbaseIn, itemstack, ItemCameraTransforms.TransformType.THIRD_PERSON);
            GlStateManager.popMatrix();
        }
    }
}
