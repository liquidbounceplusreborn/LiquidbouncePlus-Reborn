package net.ccbluex.liquidbounce.injection.forge.mixins.render;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.features.module.modules.render.PlayerEdit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.layers.LayerArmorBase;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({LayerArmorBase.class})
public class MixinLayerArmorBase {

    @Inject(method = {"doRenderLayer"}, at = {@At("HEAD")}, cancellable = true)
    public void doRenderLayer(final EntityLivingBase entitylivingbaseIn, final float limbSwing, final float limbSwingAmount, final float partialTicks, final float ageInTicks, final float netHeadYaw, final float headPitch, final float scale, final CallbackInfo ci) {
        if (PlayerEdit.customModel.get() && (LiquidBounce.moduleManager.getModule(PlayerEdit.class).onlyMe.get() && entitylivingbaseIn == Minecraft.getMinecraft().thePlayer || LiquidBounce.moduleManager.getModule(PlayerEdit.class).onlyOther.get() && entitylivingbaseIn != Minecraft.getMinecraft().thePlayer) && LiquidBounce.moduleManager.getModule(PlayerEdit.class).getState()) {
            ci.cancel();
        }
    }

}
