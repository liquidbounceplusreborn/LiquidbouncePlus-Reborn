package net.ccbluex.liquidbounce.injection.forge.mixins.render;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.features.module.modules.render.PlayerEdit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderPlayer.class)
public abstract class MixinRenderPlayer {
    private final ResourceLocation rabbit = new ResourceLocation("liquidbounce+/models/rabbit.png");
    private final ResourceLocation freddy = new ResourceLocation("liquidbounce+/models/freddy.png");
    private final ResourceLocation amogus = new ResourceLocation("liquidbounce+/models/amogus.png");
    @Inject(method = "renderLivingAt", at = @At("HEAD"))
    protected void renderLivingAt(AbstractClientPlayer entityLivingBaseIn, double x, double y, double z, CallbackInfo callbackInfo) {
        if(LiquidBounce.moduleManager.get(PlayerEdit.class).getState() & entityLivingBaseIn.equals(Minecraft.getMinecraft().thePlayer) && PlayerEdit.editPlayerSizeValue.get()) {
            GlStateManager.scale(PlayerEdit.playerSizeValue.get(), PlayerEdit.playerSizeValue.get(), PlayerEdit.playerSizeValue.get());
        }
    }
    @Inject(method = {"getEntityTexture"}, at = {@At("HEAD")}, cancellable = true)
    public void getEntityTexture(AbstractClientPlayer entity, CallbackInfoReturnable<ResourceLocation> ci) {
        if (PlayerEdit.customModel.get() && (LiquidBounce.moduleManager.getModule(PlayerEdit.class).onlyMe.get() && entity == Minecraft.getMinecraft().thePlayer || LiquidBounce.moduleManager.getModule(PlayerEdit.class).onlyOther.get() && entity != Minecraft.getMinecraft().thePlayer) && LiquidBounce.moduleManager.getModule(PlayerEdit.class).getState()) {
            if (LiquidBounce.moduleManager.getModule(PlayerEdit.class).mode.get().contains("Rabbit")) {
                ci.setReturnValue(rabbit);
            }
            if (LiquidBounce.moduleManager.getModule(PlayerEdit.class).mode.get().contains("Freddy")) {
                ci.setReturnValue(freddy);
            }
            if (LiquidBounce.moduleManager.getModule(PlayerEdit.class).mode.get().contains("Amogus")) {
                ci.setReturnValue(amogus);
            }
        }
    }
}
