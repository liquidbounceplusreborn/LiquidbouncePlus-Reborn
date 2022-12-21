/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.render;

import com.google.common.base.Predicates;
import net.ccbluex.liquidbounce.utils.Interpolator;
import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.event.Render3DEvent;
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura;
import net.ccbluex.liquidbounce.features.module.modules.player.Reach;
import net.ccbluex.liquidbounce.features.module.modules.render.Camera;
import net.ccbluex.liquidbounce.features.module.modules.render.TargetMark;
import net.ccbluex.liquidbounce.features.module.modules.render.Tracers;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.List;
import java.util.Set;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer {

    protected MixinEntityRenderer(int[] lightmapColors, DynamicTexture lightmapTexture, float torchFlickerX, float bossColorModifier, float bossColorModifierPrev, Minecraft mc, float thirdPersonDistanceTemp, float thirdPersonDistance) {
        this.lightmapColors = lightmapColors;
        this.lightmapTexture = lightmapTexture;
        this.torchFlickerX = torchFlickerX;
        this.bossColorModifier = bossColorModifier;
        this.bossColorModifierPrev = bossColorModifierPrev;
        this.mc = mc;
        this.thirdPersonDistanceTemp = thirdPersonDistanceTemp;
        this.thirdPersonDistance = thirdPersonDistance;
    }

    @Shadow
    private FloatBuffer fogColorBuffer = GLAllocation.createDirectFloatBuffer(16);
    @Shadow
    private float farPlaneDistance;

    @Shadow
    private float fogColorRed;
    @Shadow
    private float fogColorGreen;
    @Shadow
    private float fogColorBlue;

    @Shadow
    private float fogColor2;
    @Shadow
    private float fogColor1;

    @Shadow
    public abstract void loadShader(ResourceLocation resourceLocationIn);

    @Shadow
    private final int[] lightmapColors;

    @Shadow
    private final DynamicTexture lightmapTexture;

    @Shadow
    public abstract void setupCameraTransform(float partialTicks, int pass);

    @Shadow
    private Entity pointedEntity;

    @Shadow
    private float torchFlickerX;

    @Shadow
    private float bossColorModifier;

    @Shadow
    private float bossColorModifierPrev;

    @Shadow
    private boolean lightmapUpdateNeeded;

    @Shadow
    private Minecraft mc;

    float d3 = 0.0f;

    @Shadow
    private float thirdPersonDistanceTemp;

    @Shadow
    private float thirdPersonDistance;

    @Shadow
    private boolean cloudFog;

    @Inject(method = "renderStreamIndicator", at = @At("HEAD"), cancellable = true)
    private void cancelStreamIndicator(CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(
            method = "renderWorldPass",
            slice = @Slice(from = @At(value = "FIELD", target = "Lnet/minecraft/util/EnumWorldBlockLayer;TRANSLUCENT:Lnet/minecraft/util/EnumWorldBlockLayer;")),
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/RenderGlobal;renderBlockLayer(Lnet/minecraft/util/EnumWorldBlockLayer;DILnet/minecraft/entity/Entity;)I",
                    ordinal = 0
            )
    )
    private void enablePolygonOffset(CallbackInfo ci) {
        GlStateManager.enablePolygonOffset();
        GlStateManager.doPolygonOffset(-0.325F, -0.325F);
    }

    @Inject(
            method = "renderWorldPass",
            slice = @Slice(from = @At(value = "FIELD", target = "Lnet/minecraft/util/EnumWorldBlockLayer;TRANSLUCENT:Lnet/minecraft/util/EnumWorldBlockLayer;")),
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/RenderGlobal;renderBlockLayer(Lnet/minecraft/util/EnumWorldBlockLayer;DILnet/minecraft/entity/Entity;)I",
                    ordinal = 0,
                    shift = At.Shift.AFTER
            )
    )
    private void disablePolygonOffset(CallbackInfo ci) {
        GlStateManager.disablePolygonOffset();
    }

    @Inject(method = "renderWorldPass", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/EntityRenderer;renderHand:Z", shift = At.Shift.BEFORE))
    private void renderWorldPass(int pass, float partialTicks, long finishTimeNano, CallbackInfo callbackInfo) {
        LiquidBounce.eventManager.callEvent(new Render3DEvent(partialTicks));
    }

    @Inject(method = "hurtCameraEffect", at = @At("HEAD"), cancellable = true)
    private void injectHurtCameraEffect(CallbackInfo callbackInfo) {
        Camera camera = LiquidBounce.moduleManager.getModule(Camera.class);
        if (camera.getState() && camera.getNoHurtCam().get())
            callbackInfo.cancel();
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    private void updateLightmap(float f2) {
        Camera camera = LiquidBounce.moduleManager.getModule(Camera.class);
        if (this.lightmapUpdateNeeded) {
            this.mc.mcProfiler.startSection("lightTex");
            World world = this.mc.theWorld;
            if (world != null) {
                float f3 = world.getSunBrightness(1.0f);
                float f4 = f3 * 0.95f + 0.05f;
                for (int i2 = 0; i2 < 256; ++i2) {
                    float f5;
                    float f6;
                    float f7 = world.provider.getLightBrightnessTable()[i2 / 16] * f4;
                    float f8 = world.provider.getLightBrightnessTable()[i2 % 16] * (this.torchFlickerX * 0.1f + 1.5f);
                    if (world.getLastLightningBolt() > 0) {
                        f7 = world.provider.getLightBrightnessTable()[i2 / 16];
                    }
                    float f9 = f7 * (f3 * 0.65f + 0.35f);
                    float f10 = f7 * (f3 * 0.65f + 0.35f);
                    float f11 = f8 * ((f8 * 0.6f + 0.4f) * 0.6f + 0.4f);
                    float f12 = f8 * (f8 * f8 * 0.6f + 0.4f);
                    float f13 = f9 + f8;
                    float f14 = f10 + f11;
                    float f15 = f7 + f12;
                    f13 = f13 * 0.96f + 0.03f;
                    f14 = f14 * 0.96f + 0.03f;
                    f15 = f15 * 0.96f + 0.03f;
                    if (this.bossColorModifier > 0.0f) {
                        float f16 = this.bossColorModifierPrev + (this.bossColorModifier - this.bossColorModifierPrev) * f2;
                        f13 = f13 * (1.0f - f16) + f13 * 0.7f * f16;
                        f14 = f14 * (1.0f - f16) + f14 * 0.6f * f16;
                        f15 = f15 * (1.0f - f16) + f15 * 0.6f * f16;
                    }
                    if (world.provider.getDimensionId() == 1) {
                        f13 = 0.22f + f8 * 0.75f;
                        f14 = 0.28f + f11 * 0.75f;
                        f15 = 0.25f + f12 * 0.75f;
                    }
                    if (this.mc.thePlayer.isPotionActive(Potion.nightVision)) {
                        f6 = this.getNightVisionBrightness((EntityLivingBase)this.mc.thePlayer, f2);
                        f5 = 1.0f / f13;
                        if (f5 > 1.0f / f14) {
                            f5 = 1.0f / f14;
                        }
                        if (f5 > 1.0f / f15) {
                            f5 = 1.0f / f15;
                        }
                        f13 = f13 * (1.0f - f6) + f13 * f5 * f6;
                        f14 = f14 * (1.0f - f6) + f14 * f5 * f6;
                        f15 = f15 * (1.0f - f6) + f15 * f5 * f6;
                    }
                    if (f13 > 1.0f) {
                        f13 = 1.0f;
                    }
                    if (f14 > 1.0f) {
                        f14 = 1.0f;
                    }
                    if (f15 > 1.0f) {
                        f15 = 1.0f;
                    }
                    f6 = this.mc.gameSettings.gammaSetting;
                    f5 = 1.0f - f13;
                    float f17 = 1.0f - f14;
                    float f18 = 1.0f - f15;
                    f5 = 1.0f - f5 * f5 * f5 * f5;
                    f17 = 1.0f - f17 * f17 * f17 * f17;
                    f18 = 1.0f - f18 * f18 * f18 * f18;
                    f13 = f13 * (1.0f - f6) + f5 * f6;
                    f14 = f14 * (1.0f - f6) + f17 * f6;
                    f15 = f15 * (1.0f - f6) + f18 * f6;
                    f13 = f13 * 0.96f + 0.03f;
                    f14 = f14 * 0.96f + 0.03f;
                    f15 = f15 * 0.96f + 0.03f;
                    if (f13 > 1.0f) {
                        f13 = 1.0f;
                    }
                    if (f14 > 1.0f) {
                        f14 = 1.0f;
                    }
                    if (f15 > 1.0f) {
                        f15 = 1.0f;
                    }
                    if (f13 < 0.0f) {
                        f13 = 0.0f;
                    }
                    if (f14 < 0.0f) {
                        f14 = 0.0f;
                    }
                    if (f15 < 0.0f) {
                        f15 = 0.0f;
                    }
                    int n2 = (int)(f13 * 255.0f);
                    int n3 = (int)(f14 * 255.0f);
                    int n4 = (int)(f15 * 255.0f);
                    this.lightmapColors[i2] = camera.getState() && camera.getWorldColorValue().get() ? new Color(camera.getWorldColorRValue().get(),camera.getWorldColorGValue().get(),camera.getWorldColorBValue().get()).getRGB() : 0xFF000000 | n2 << 16 | n3 << 8 | n4;
                }
                this.lightmapTexture.updateDynamicTexture();
                this.lightmapUpdateNeeded = false;
                this.mc.mcProfiler.endSection();
            }
        }
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    private void orientCamera(float partialTicks) {
        Camera camera = LiquidBounce.moduleManager.getModule(Camera.class);
        Entity entity = this.mc.getRenderViewEntity();
        float f = entity.getEyeHeight();
        double d0 = entity.prevPosX + (entity.posX - entity.prevPosX) * (double)partialTicks;
        double d1 = entity.prevPosY + (entity.posY - entity.prevPosY) * (double)partialTicks + (double)f;
        double d2 = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * (double)partialTicks;
        if (entity instanceof EntityLivingBase && ((EntityLivingBase)entity).isPlayerSleeping()) {
            f = (float)((double)f + 1.0);
            GlStateManager.translate(0.0f, 0.3f, 0.0f);
            if (!this.mc.gameSettings.debugCamEnable) {
                BlockPos blockpos = new BlockPos(entity);
                IBlockState iblockstate = this.mc.theWorld.getBlockState(blockpos);
                ForgeHooksClient.orientBedCamera(this.mc.theWorld, blockpos, iblockstate, entity);
                GlStateManager.rotate(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks + 180.0F, 0.0F, -1.0F, 0.0F);
                GlStateManager.rotate(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks, -1.0F, 0.0F, 0.0F);
            }
        } else if (this.mc.gameSettings.thirdPersonView > 0) {
            float f2 = this.d3 = camera.getState() && camera.getCameraPositionValue().get() ? (float) Interpolator.LINEAR.interpolate((double)this.d3, (double)camera.getCameraPositionFovValue().get(), 5.0 / (double)Minecraft.getDebugFPS()) : (float)Interpolator.LINEAR.interpolate((double)this.d3, 4.0, 5.0 / (double)Minecraft.getDebugFPS());
            if (Double.isNaN(this.d3)) {
                this.d3 = 0.01f;
            }
            this.d3 = (float)MathHelper.clamp_float((float) this.d3, 0.01F, (float) (camera.getState() && camera.getCameraPositionValue().get() ? (double)camera.getCameraPositionFovValue().get() : 4.0));
            if (this.mc.gameSettings.debugCamEnable) {
                GlStateManager.translate(0.0f, 0.0f, -this.d3);
            } else {
                float f22;
                float f1;
                if (camera.getState() && camera.getCameraPositionValue().get()) {
                    f1 = entity.rotationYaw + camera.getCameraPositionYawValue().get();
                    f22 = entity.rotationPitch + camera.getCameraPositionPitchValue().get();
                } else {
                    f1 = entity.rotationYaw;
                    f22 = entity.rotationPitch;
                }
                if (this.mc.gameSettings.thirdPersonView == 2) {
                    f22 += 180.0f;
                }
                double d4 = (double)(-MathHelper.sin(f1 * ((float)Math.PI / 180)) * MathHelper.cos(f22 * ((float)Math.PI / 180))) * (double)this.d3;
                double d5 = (double)(MathHelper.cos(f1 * ((float)Math.PI / 180)) * MathHelper.cos(f22 * ((float)Math.PI / 180))) * (double)this.d3;
                double d6 = (double)(-MathHelper.sin(f22 * ((float)Math.PI / 180))) * (double)this.d3;
                for (int i = 0; i < 8; ++i) {
                    double d7;
                    MovingObjectPosition movingobjectposition = this.mc.theWorld.rayTraceBlocks(new Vec3(d0 + (double)d4, d1 + (double)d5, d2 + (double)d6), new Vec3(d0 - d4 + (double)d4 + (double)d6, d1 - d6 + (double)d5, d2 - d5 + (double)d6));
                    float f3 = (i & 1) * 2 - 1;
                    float f4 = (i >> 1 & 1) * 2 - 1;
                    float f5 = (i >> 2 & 1) * 2 - 1;
                    if (camera.getState() || (movingobjectposition = this.mc.theWorld.rayTraceBlocks(new Vec3(d0 + (double)(f3 *= 0.1f), d1 + (double)(f4 *= 0.1f), d2 + (double)(f5 *= 0.1f)), new Vec3(d0 - d4 + (double)f3 + (double)f5, d1 - d6 + (double)f4, d2 - d5 + (double)f5))) == null || !((d7 = movingobjectposition.hitVec.distanceTo(new Vec3(d0, d1, d2))) < (double)this.d3)) continue;
                    this.d3 = (float)d7;
                }
                if (this.mc.gameSettings.thirdPersonView == 2) {
                    GlStateManager.rotate(180.0f, 0.0f, 1.0f, 0.0f);
                }
                GlStateManager.rotate(entity.rotationPitch - f22, 1.0f, 0.0f, 0.0f);
                GlStateManager.rotate(entity.rotationYaw - f1, 0.0f, 1.0f, 0.0f);
                GlStateManager.translate(0.0f, 0.0f, -this.d3);
                GlStateManager.rotate(f1 - entity.rotationYaw, 0.0f, 1.0f, 0.0f);
                GlStateManager.rotate(f22 - entity.rotationPitch, 1.0f, 0.0f, 0.0f);
            }
        } else {
            GlStateManager.translate(0.0f, 0.0f, 0.05f);
            this.d3 = 0.01f;
        }
        if (!this.mc.gameSettings.debugCamEnable) {
            float yaw = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks + 180.0F;
            float pitch = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;
            float f8 = 0.0F;
            if (entity instanceof EntityAnimal) {
                EntityAnimal entityanimal = (EntityAnimal)entity;
                yaw = entityanimal.prevRotationYawHead + (entityanimal.rotationYawHead - entityanimal.prevRotationYawHead) * partialTicks + 180.0F;
            }

            Block block = ActiveRenderInfo.getBlockAtEntityViewpoint(this.mc.theWorld, entity, partialTicks);
            EntityViewRenderEvent.CameraSetup event = new EntityViewRenderEvent.CameraSetup(new EntityRenderer(Minecraft.getMinecraft(), new IResourceManager() {
                @Override
                public Set<String> getResourceDomains() {
                    return null;
                }

                @Override
                public IResource getResource(ResourceLocation resourceLocation) throws IOException {
                    return null;
                }

                @Override
                public List<IResource> getAllResources(ResourceLocation resourceLocation) throws IOException {
                    return null;
                }
            }), entity, block, (double)partialTicks, yaw, pitch, f8);
            MinecraftForge.EVENT_BUS.post(event);
            GlStateManager.rotate(event.roll, 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(event.pitch, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(event.yaw, 0.0F, 1.0F, 0.0F);
        } else if (!this.mc.gameSettings.debugCamEnable) {
            GlStateManager.rotate(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks, 1.0f, 0.0f, 0.0f);
            if (entity instanceof EntityAnimal) {
                EntityAnimal entityanimal = (EntityAnimal)entity;
                GlStateManager.rotate(entityanimal.prevRotationYawHead + (entityanimal.rotationYawHead - entityanimal.prevRotationYawHead) * partialTicks + 180.0f, 0.0f, 1.0f, 0.0f);
            } else {
                GlStateManager.rotate(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks + 180.0f, 0.0f, 1.0f, 0.0f);
            }
        }
        GlStateManager.translate(0.0f, -f, 0.0f);
        d0 = entity.prevPosX + (entity.posX - entity.prevPosX) * (double)partialTicks;
        d1 = entity.prevPosY + (entity.posY - entity.prevPosY) * (double)partialTicks + (double)f;
        d2 = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * (double)partialTicks;
        this.cloudFog = this.mc.renderGlobal.hasCloudFog(d0, d1, d2, partialTicks);
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    private void updateFogColor(float p_updateFogColor_1_) {
        Camera camera = LiquidBounce.moduleManager.getModule(Camera.class);
        World world = this.mc.theWorld;
        Entity entity = this.mc.getRenderViewEntity();
        float f = camera.getState() && camera.getFogColorValue().get() ? camera.getFogDistance().get() * 10.0f : 0.25f + 0.75f * (float)this.mc.gameSettings.renderDistanceChunks / 32.0f;
        f = 1.0F - (float)Math.pow((double)f, 0.25);
        Vec3 vec3 = world.getSkyColor(this.mc.getRenderViewEntity(), p_updateFogColor_1_);
        float f1 = (float)vec3.xCoord;
        float f2 = (float)vec3.yCoord;
        float f3 = (float)vec3.zCoord;
        Vec3 vec31 = world.getFogColor(p_updateFogColor_1_);
        this.fogColorRed = (float)vec31.xCoord;
        this.fogColorGreen = (float)vec31.yCoord;
        this.fogColorBlue = (float)vec31.zCoord;
        float f12;
        if (this.mc.gameSettings.renderDistanceChunks >= 4) {
            double d0 = -1.0;
            Vec3 vec32 = MathHelper.sin(world.getCelestialAngleRadians(p_updateFogColor_1_)) > 0.0F ? new Vec3(d0, 0.0, 0.0) : new Vec3(1.0, 0.0, 0.0);
            f12 = (float)entity.getLook(p_updateFogColor_1_).dotProduct(vec32);
            if (f12 < 0.0F) {
                f12 = 0.0F;
            }

            if (f12 > 0.0F) {
                float[] afloat = world.provider.calcSunriseSunsetColors(world.getCelestialAngle(p_updateFogColor_1_), p_updateFogColor_1_);
                if (afloat != null) {
                    f12 *= afloat[3];
                    this.fogColorRed = this.fogColorRed * (1.0F - f12) + afloat[0] * f12;
                    this.fogColorGreen = this.fogColorGreen * (1.0F - f12) + afloat[1] * f12;
                    this.fogColorBlue = this.fogColorBlue * (1.0F - f12) + afloat[2] * f12;
                }
            }
        }

        this.fogColorRed += (f1 - this.fogColorRed) * f;
        this.fogColorGreen += (f2 - this.fogColorGreen) * f;
        this.fogColorBlue += (f3 - this.fogColorBlue) * f;
        float f8 = world.getRainStrength(p_updateFogColor_1_);
        float f9;
        float f11;
        if (f8 > 0.0F) {
            f9 = 1.0F - f8 * 0.5F;
            f11 = 1.0F - f8 * 0.4F;
            this.fogColorRed *= f9;
            this.fogColorGreen *= f9;
            this.fogColorBlue *= f11;
        }

        f9 = world.getThunderStrength(p_updateFogColor_1_);
        if (f9 > 0.0F) {
            f11 = 1.0F - f9 * 0.5F;
            this.fogColorRed *= f11;
            this.fogColorGreen *= f11;
            this.fogColorBlue *= f11;
        }

        Block block = ActiveRenderInfo.getBlockAtEntityViewpoint(this.mc.theWorld, entity, p_updateFogColor_1_);
        if (this.cloudFog) {
            Vec3 vec33 = world.getCloudColour(p_updateFogColor_1_);
            this.fogColorRed = (float)vec33.xCoord;
            this.fogColorGreen = (float)vec33.yCoord;
            this.fogColorBlue = (float)vec33.zCoord;
        } else if (block.getMaterial() == Material.water) {
            f12 = (float) EnchantmentHelper.getRespiration(entity) * 0.2F;
            if (entity instanceof EntityLivingBase && ((EntityLivingBase)entity).isPotionActive(Potion.waterBreathing)) {
                f12 = f12 * 0.3F + 0.6F;
            }

            this.fogColorRed = 0.02F + f12;
            this.fogColorGreen = 0.02F + f12;
            this.fogColorBlue = 0.2F + f12;
        } else if (block.getMaterial() == Material.lava) {
            this.fogColorRed = 0.6F;
            this.fogColorGreen = 0.1F;
            this.fogColorBlue = 0.0F;
        }

        f12 = this.fogColor2 + (this.fogColor1 - this.fogColor2) * p_updateFogColor_1_;
        this.fogColorRed *= f12;
        this.fogColorGreen *= f12;
        this.fogColorBlue *= f12;
        double d1 = (entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double)p_updateFogColor_1_) * world.provider.getVoidFogYFactor();
        if (entity instanceof EntityLivingBase && ((EntityLivingBase)entity).isPotionActive(Potion.blindness)) {
            int i = ((EntityLivingBase)entity).getActivePotionEffect(Potion.blindness).getDuration();
            if (i < 20) {
                d1 *= (double)(1.0F - (float)i / 20.0F);
            } else {
                d1 = 0.0;
            }
        }

        if (d1 < 1.0) {
            if (d1 < 0.0) {
                d1 = 0.0;
            }

            d1 *= d1;
            this.fogColorRed = (float)((double)this.fogColorRed * d1);
            this.fogColorGreen = (float)((double)this.fogColorGreen * d1);
            this.fogColorBlue = (float)((double)this.fogColorBlue * d1);
        }

        float f15;
        if (this.bossColorModifier > 0.0F) {
            f15 = this.bossColorModifierPrev + (this.bossColorModifier - this.bossColorModifierPrev) * p_updateFogColor_1_;
            this.fogColorRed = this.fogColorRed * (1.0F - f15) + this.fogColorRed * 0.7F * f15;
            this.fogColorGreen = this.fogColorGreen * (1.0F - f15) + this.fogColorGreen * 0.6F * f15;
            this.fogColorBlue = this.fogColorBlue * (1.0F - f15) + this.fogColorBlue * 0.6F * f15;
        }

        float f6;
        if (entity instanceof EntityLivingBase && ((EntityLivingBase)entity).isPotionActive(Potion.nightVision)) {
            f15 = this.getNightVisionBrightness((EntityLivingBase)entity, p_updateFogColor_1_);
            f6 = 1.0F / this.fogColorRed;
            if (f6 > 1.0F / this.fogColorGreen) {
                f6 = 1.0F / this.fogColorGreen;
            }

            if (f6 > 1.0F / this.fogColorBlue) {
                f6 = 1.0F / this.fogColorBlue;
            }

            this.fogColorRed = this.fogColorRed * (1.0F - f15) + this.fogColorRed * f6 * f15;
            this.fogColorGreen = this.fogColorGreen * (1.0F - f15) + this.fogColorGreen * f6 * f15;
            this.fogColorBlue = this.fogColorBlue * (1.0F - f15) + this.fogColorBlue * f6 * f15;
        }

        if (this.mc.gameSettings.anaglyph) {
            f15 = (this.fogColorRed * 30.0F + this.fogColorGreen * 59.0F + this.fogColorBlue * 11.0F) / 100.0F;
            f6 = (this.fogColorRed * 30.0F + this.fogColorGreen * 70.0F) / 100.0F;
            float f7 = (this.fogColorRed * 30.0F + this.fogColorBlue * 70.0F) / 100.0F;
            this.fogColorRed = f15;
            this.fogColorGreen = f6;
            this.fogColorBlue = f7;
        }

        EntityViewRenderEvent.FogColors event = new EntityViewRenderEvent.FogColors(new EntityRenderer(Minecraft.getMinecraft(), new IResourceManager() {
            @Override
            public Set<String> getResourceDomains() {
                return null;
            }

            @Override
            public IResource getResource(ResourceLocation resourceLocation) throws IOException {
                return null;
            }

            @Override
            public List<IResource> getAllResources(ResourceLocation resourceLocation) throws IOException {
                return null;
            }
        }), entity, block, (double)p_updateFogColor_1_, this.fogColorRed, this.fogColorGreen, this.fogColorBlue);
        MinecraftForge.EVENT_BUS.post(event);
        this.fogColorRed = event.red;
        this.fogColorGreen = event.green;
        this.fogColorBlue = event.blue;
        GlStateManager.clearColor(this.fogColorRed, this.fogColorGreen, this.fogColorBlue, 0.0F);
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    private void setupFog(int p_setupFog_1_, float p_setupFog_2_) {
        Camera camera = LiquidBounce.moduleManager.getModule(Camera.class);
        Entity entity = this.mc.getRenderViewEntity();
        boolean flag = false;
        if (entity instanceof EntityPlayer) {
            flag = ((EntityPlayer)entity).capabilities.isCreativeMode;
        }

        GL11.glFog(2918, this.setFogColorBuffer(this.fogColorRed, this.fogColorGreen, this.fogColorBlue, 1.0F));
        GL11.glNormal3f(0.0F, -1.0F, 0.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Block block = ActiveRenderInfo.getBlockAtEntityViewpoint(this.mc.theWorld, entity, p_setupFog_2_);
        float hook = ForgeHooksClient.getFogDensity(new EntityRenderer(Minecraft.getMinecraft(), new IResourceManager() {
            @Override
            public Set<String> getResourceDomains() {
                return null;
            }

            @Override
            public IResource getResource(ResourceLocation resourceLocation) throws IOException {
                return null;
            }

            @Override
            public List<IResource> getAllResources(ResourceLocation resourceLocation) throws IOException {
                return null;
            }
        }), entity, block, p_setupFog_2_, 0.1F);
        if (hook >= 0.0F) {
            GlStateManager.setFogDensity(hook);
        } else {
            float f;
            if (entity instanceof EntityLivingBase && ((EntityLivingBase)entity).isPotionActive(Potion.blindness)) {
                f = 5.0F;
                int i = ((EntityLivingBase)entity).getActivePotionEffect(Potion.blindness).getDuration();
                if (i < 20) {
                    f = 5.0F + (this.farPlaneDistance - 5.0F) * (1.0F - (float)i / 20.0F);
                }

                GlStateManager.setFog(9729);
                if (p_setupFog_1_ == -1) {
                    GlStateManager.setFogStart(0.0F);
                    GlStateManager.setFogEnd(f * 0.8F);
                } else {
                    GlStateManager.setFogStart(f * 0.25F);
                    GlStateManager.setFogEnd(f);
                }

                if (GLContext.getCapabilities().GL_NV_fog_distance) {
                    GL11.glFogi(34138, 34139);
                }
            } else if (this.cloudFog) {
                GlStateManager.setFog(2048);
                GlStateManager.setFogDensity(0.1F);
            } else if (block.getMaterial() == Material.water) {
                GlStateManager.setFog(2048);
                if (entity instanceof EntityLivingBase && ((EntityLivingBase)entity).isPotionActive(Potion.waterBreathing)) {
                    GlStateManager.setFogDensity(0.01F);
                } else {
                    GlStateManager.setFogDensity(0.1F - (float)EnchantmentHelper.getRespiration(entity) * 0.03F);
                }
            } else if (block.getMaterial() == Material.lava) {
                GlStateManager.setFog(2048);
                GlStateManager.setFogDensity(2.0F);
            } else {
                float value = camera.getState() && camera.getFogColorValue().get() ? camera.getFogDistance().getValue() * 50.0f : 0.0f;
                f = this.farPlaneDistance - value;
                GlStateManager.setFog(9729);
                if (p_setupFog_1_ == -1) {
                    GlStateManager.setFogStart(0.0F);
                    GlStateManager.setFogEnd(f);
                } else {
                    GlStateManager.setFogStart(f * 0.75F);
                    GlStateManager.setFogEnd(f);
                }

                if (GLContext.getCapabilities().GL_NV_fog_distance) {
                    GL11.glFogi(34138, 34139);
                }

                if (this.mc.theWorld.provider.doesXZShowFog((int)entity.posX, (int)entity.posZ)) {
                    GlStateManager.setFogStart(f * 0.05F);
                    GlStateManager.setFogEnd(Math.min(f, 192.0F) * 0.5F);
                }

                ForgeHooksClient.onFogRender(new EntityRenderer(Minecraft.getMinecraft(), new IResourceManager() {
                    @Override
                    public Set<String> getResourceDomains() {
                        return null;
                    }

                    @Override
                    public IResource getResource(ResourceLocation resourceLocation) throws IOException {
                        return null;
                    }

                    @Override
                    public List<IResource> getAllResources(ResourceLocation resourceLocation) throws IOException {
                        return null;
                    }
                }), entity, block, p_setupFog_2_, p_setupFog_1_, f);
            }
        }

        if (camera.getState() && camera.getFogColorValue().get()) {
            this.fogColorRed = camera.getFogColorRValue().get() / 255.0f;
            this.fogColorGreen = camera.getFogColorGValue().get() / 255.0f;
            this.fogColorBlue = camera.getFogColorBValue().get() / 255.0f;
        }else{
            this.fogColorRed = this.fogColorRed;
            this.fogColorGreen = this.fogColorGreen;
            this.fogColorBlue = this.fogColorBlue;
        }
        GlStateManager.enableColorMaterial();
        GlStateManager.enableFog();
        GlStateManager.colorMaterial(1028, 4608);
    }

    private float getNightVisionBrightness(EntityLivingBase p_getNightVisionBrightness_1_, float p_getNightVisionBrightness_2_) {
        int i = p_getNightVisionBrightness_1_.getActivePotionEffect(Potion.nightVision).getDuration();
        return i > 200 ? 1.0F : 0.7F + MathHelper.sin(((float)i - p_getNightVisionBrightness_2_) * 3.1415927F * 0.2F) * 0.3F;
    }
    private FloatBuffer setFogColorBuffer(float p_setFogColorBuffer_1_, float p_setFogColorBuffer_2_, float p_setFogColorBuffer_3_, float p_setFogColorBuffer_4_) {
        this.fogColorBuffer.clear();
        this.fogColorBuffer.put(p_setFogColorBuffer_1_).put(p_setFogColorBuffer_2_).put(p_setFogColorBuffer_3_).put(p_setFogColorBuffer_4_);
        this.fogColorBuffer.flip();
        return this.fogColorBuffer;
    }

    @Inject(method = "orientCamera", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Vec3;distanceTo(Lnet/minecraft/util/Vec3;)D"), cancellable = true)
    private void cameraClip(float partialTicks, CallbackInfo callbackInfo) {
        final Camera camera = LiquidBounce.moduleManager.getModule(Camera.class);
        if (camera.getState() && camera.getCameraClipValue().get()) {
            callbackInfo.cancel();

            Entity entity = this.mc.getRenderViewEntity();
            float f = entity.getEyeHeight();

            if(entity instanceof EntityLivingBase && ((EntityLivingBase) entity).isPlayerSleeping()) {
                f = (float) ((double) f + 1D);
                GlStateManager.translate(0F, 0.3F, 0.0F);

                if(!this.mc.gameSettings.debugCamEnable) {
                    BlockPos blockpos = new BlockPos(entity);
                    IBlockState iblockstate = this.mc.theWorld.getBlockState(blockpos);
                    net.minecraftforge.client.ForgeHooksClient.orientBedCamera(this.mc.theWorld, blockpos, iblockstate, entity);

                    GlStateManager.rotate(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks + 180.0F, 0.0F, -1.0F, 0.0F);
                    GlStateManager.rotate(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks, -1.0F, 0.0F, 0.0F);
                }
            }else if(this.mc.gameSettings.thirdPersonView > 0) {
                double d3 = (double) (this.thirdPersonDistanceTemp + (this.thirdPersonDistance - this.thirdPersonDistanceTemp) * partialTicks);

                if(this.mc.gameSettings.debugCamEnable) {
                    GlStateManager.translate(0.0F, 0.0F, (float) (-d3));
                }else{
                    float f1 = entity.rotationYaw;
                    float f2 = entity.rotationPitch;

                    if(this.mc.gameSettings.thirdPersonView == 2)
                        f2 += 180.0F;

                    if(this.mc.gameSettings.thirdPersonView == 2)
                        GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);

                    GlStateManager.rotate(entity.rotationPitch - f2, 1.0F, 0.0F, 0.0F);
                    GlStateManager.rotate(entity.rotationYaw - f1, 0.0F, 1.0F, 0.0F);
                    GlStateManager.translate(0.0F, 0.0F, (float) (-d3));
                    GlStateManager.rotate(f1 - entity.rotationYaw, 0.0F, 1.0F, 0.0F);
                    GlStateManager.rotate(f2 - entity.rotationPitch, 1.0F, 0.0F, 0.0F);
                }
            }else
                GlStateManager.translate(0.0F, 0.0F, -0.1F);

            if(!this.mc.gameSettings.debugCamEnable) {
                float yaw = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks + 180.0F;
                float pitch = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;
                float roll = 0.0F;
                if(entity instanceof EntityAnimal) {
                    EntityAnimal entityanimal = (EntityAnimal) entity;
                    yaw = entityanimal.prevRotationYawHead + (entityanimal.rotationYawHead - entityanimal.prevRotationYawHead) * partialTicks + 180.0F;
                }

                Block block = ActiveRenderInfo.getBlockAtEntityViewpoint(this.mc.theWorld, entity, partialTicks);
                net.minecraftforge.client.event.EntityViewRenderEvent.CameraSetup event = new net.minecraftforge.client.event.EntityViewRenderEvent.CameraSetup((EntityRenderer) (Object) this, entity, block, partialTicks, yaw, pitch, roll);
                net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event);
                GlStateManager.rotate(event.roll, 0.0F, 0.0F, 1.0F);
                GlStateManager.rotate(event.pitch, 1.0F, 0.0F, 0.0F);
                GlStateManager.rotate(event.yaw, 0.0F, 1.0F, 0.0F);
            }

            GlStateManager.translate(0.0F, -f, 0.0F);
            double d0 = entity.prevPosX + (entity.posX - entity.prevPosX) * (double) partialTicks;
            double d1 = entity.prevPosY + (entity.posY - entity.prevPosY) * (double) partialTicks + (double) f;
            double d2 = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * (double) partialTicks;
            this.cloudFog = this.mc.renderGlobal.hasCloudFog(d0, d1, d2, partialTicks);
        }
    }

    @Inject(method = "setupCameraTransform", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/EntityRenderer;setupViewBobbing(F)V", shift = At.Shift.BEFORE))
    private void setupCameraViewBobbingBefore(final CallbackInfo callbackInfo) {
        final TargetMark targetMark = LiquidBounce.moduleManager.getModule(TargetMark.class);
        final KillAura aura = LiquidBounce.moduleManager.getModule(KillAura.class);

        if ((targetMark != null && aura != null && targetMark.modeValue.get().equalsIgnoreCase("tracers") && !aura.getTargetModeValue().get().equalsIgnoreCase("multi") && aura.getTarget() != null) || LiquidBounce.moduleManager.getModule(Tracers.class).getState()) GL11.glPushMatrix();
    }

    @Inject(method = "setupCameraTransform", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/EntityRenderer;setupViewBobbing(F)V", shift = At.Shift.AFTER))
    private void setupCameraViewBobbingAfter(final CallbackInfo callbackInfo) {
        final TargetMark targetMark = LiquidBounce.moduleManager.getModule(TargetMark.class);
        final KillAura aura = LiquidBounce.moduleManager.getModule(KillAura.class);

        if ((targetMark != null && aura != null && targetMark.modeValue.get().equalsIgnoreCase("tracers") && !aura.getTargetModeValue().get().equalsIgnoreCase("multi") && aura.getTarget() != null) || LiquidBounce.moduleManager.getModule(Tracers.class).getState()) GL11.glPopMatrix();
    }

    /**
     * @author CCBlueX
     */
    @Inject(method = "getMouseOver", at = @At("HEAD"), cancellable = true)
    private void getMouseOver(float p_getMouseOver_1_, CallbackInfo ci) {
        Entity entity = this.mc.getRenderViewEntity();
        if (entity != null && this.mc.theWorld != null) {
            this.mc.mcProfiler.startSection("pick");
            this.mc.pointedEntity = null;

            final Reach reach = (Reach) LiquidBounce.moduleManager.getModule(Reach.class);

            double d0 = reach.getState() ? reach.getMaxRange() : (double) this.mc.playerController.getBlockReachDistance();
            this.mc.objectMouseOver = entity.rayTrace(reach.getState() ? reach.getBuildReachValue().get() : d0, p_getMouseOver_1_);
            double d1 = d0;
            Vec3 vec3 = entity.getPositionEyes(p_getMouseOver_1_);
            boolean flag = false;
            if (this.mc.playerController.extendedReach()) {
                d0 = 6.0D;
                d1 = 6.0D;
            } else if (d0 > 3.0D) {
                flag = true;
            }

            if (this.mc.objectMouseOver != null) {
                d1 = this.mc.objectMouseOver.hitVec.distanceTo(vec3);
            }

            if (reach.getState()) {
                d1 = reach.getCombatReachValue().get();

                final MovingObjectPosition movingObjectPosition = entity.rayTrace(d1, p_getMouseOver_1_);

                if (movingObjectPosition != null) d1 = movingObjectPosition.hitVec.distanceTo(vec3);
            }

            Vec3 vec31 = entity.getLook(p_getMouseOver_1_);
            Vec3 vec32 = vec3.addVector(vec31.xCoord * d0, vec31.yCoord * d0, vec31.zCoord * d0);
            this.pointedEntity = null;
            Vec3 vec33 = null;
            float f = 1.0F;
            List<Entity> list = this.mc.theWorld.getEntitiesInAABBexcluding(entity, entity.getEntityBoundingBox().addCoord(vec31.xCoord * d0, vec31.yCoord * d0, vec31.zCoord * d0).expand((double) f, (double) f, (double) f), Predicates.and(EntitySelectors.NOT_SPECTATING, p_apply_1_ -> p_apply_1_.canBeCollidedWith()));
            double d2 = d1;

            for (int j = 0; j < list.size(); ++j) {
                Entity entity1 = (Entity) list.get(j);
                float f1 = entity1.getCollisionBorderSize();
                AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().expand((double) f1, (double) f1, (double) f1);
                MovingObjectPosition movingobjectposition = axisalignedbb.calculateIntercept(vec3, vec32);
                if (axisalignedbb.isVecInside(vec3)) {
                    if (d2 >= 0.0D) {
                        this.pointedEntity = entity1;
                        vec33 = movingobjectposition == null ? vec3 : movingobjectposition.hitVec;
                        d2 = 0.0D;
                    }
                } else if (movingobjectposition != null) {
                    double d3 = vec3.distanceTo(movingobjectposition.hitVec);
                    if (d3 < d2 || d2 == 0.0D) {
                        if (entity1 == entity.ridingEntity && !entity.canRiderInteract()) {
                            if (d2 == 0.0D) {
                                this.pointedEntity = entity1;
                                vec33 = movingobjectposition.hitVec;
                            }
                        } else {
                            this.pointedEntity = entity1;
                            vec33 = movingobjectposition.hitVec;
                            d2 = d3;
                        }
                    }
                }
            }

            if (this.pointedEntity != null && flag && vec3.distanceTo(vec33) > (reach.getState() ? reach.getCombatReachValue().get() : 3.0D)) {
                this.pointedEntity = null;
                this.mc.objectMouseOver = new MovingObjectPosition(MovingObjectPosition.MovingObjectType.MISS, vec33, (EnumFacing) null, new BlockPos(vec33));
            }

            if (this.pointedEntity != null && (d2 < d1 || this.mc.objectMouseOver == null)) {
                this.mc.objectMouseOver = new MovingObjectPosition(this.pointedEntity, vec33);
                if (this.pointedEntity instanceof EntityLivingBase || this.pointedEntity instanceof EntityItemFrame) {
                    this.mc.pointedEntity = this.pointedEntity;
                }
            }

            this.mc.mcProfiler.endSection();
        }

        ci.cancel();
    }
}