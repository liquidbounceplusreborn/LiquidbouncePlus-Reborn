package net.ccbluex.liquidbounce.injection.forge.mixins.render;

import co.uk.hexeption.utils.OutlineUtils;
import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.features.module.modules.render.*;
import net.ccbluex.liquidbounce.utils.RotationUtils;
import net.ccbluex.liquidbounce.utils.ClientUtils;
import net.ccbluex.liquidbounce.utils.EntityUtils;
import net.ccbluex.liquidbounce.utils.render.ColorUtils;
import net.minecraft.client.model.ModelBase;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;
import net.minecraft.client.renderer.OpenGlHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;
import java.nio.FloatBuffer;
import java.util.Objects;

import static org.lwjgl.opengl.GL11.*;

@Mixin(RendererLivingEntity.class)
@SideOnly(Side.CLIENT)
public abstract class MixinRendererLivingEntity extends MixinRender {

    @Shadow
    protected abstract<T extends EntityLivingBase> float handleRotationFloat(T livingBase, float partialTicks);

    @Shadow
    private static final Logger logger = LogManager.getLogger();

    @Shadow
    protected abstract<T extends EntityLivingBase> float getSwingProgress(T livingBase, float partialTickTime);

    @Shadow
    protected abstract <T extends EntityLivingBase> void renderLivingAt(T entityLivingBaseIn, double x, double y, double z);

    @Shadow
    protected abstract <T extends EntityLivingBase> void preRenderCallback(T entitylivingbaseIn, float partialTickTime);

    @Shadow
    protected abstract <T extends EntityLivingBase> void rotateCorpse(T p_rotateCorpse_1_, float p_rotateCorpse_2_, float p_rotateCorpse_3_, float p_rotateCorpse_4_);

    @Shadow
    protected boolean renderOutlines = false;

    @Shadow
    protected abstract <T extends EntityLivingBase> boolean setScoreTeamColor(T entityLivingBaseIn);

    @Shadow
    protected abstract void unsetBrightness();

    @Shadow
    protected abstract void unsetScoreTeamColor();

    @Shadow
    protected abstract <T extends EntityLivingBase> void renderLayers(T entitylivingbaseIn, float p_177093_2_, float p_177093_3_, float partialTicks, float p_177093_5_, float p_177093_6_, float p_177093_7_, float p_177093_8_);

    @Shadow
    protected abstract <T extends EntityLivingBase> boolean setDoRenderBrightness(T entityLivingBaseIn, float partialTicks);

    private static final DynamicTexture field_177096_e = new DynamicTexture(16, 16);

    @Shadow
    protected abstract float interpolateRotation(float par1, float par2, float par3);

    @Shadow
    protected <T extends EntityLivingBase> float getDeathMaxRotation(T p_getDeathMaxRotation_1_) {
        return 90.0F;
    }

    @Shadow
    protected FloatBuffer brightnessBuffer = GLAllocation.createDirectFloatBuffer(4);

    @Shadow
    public abstract <T extends EntityLivingBase> int getColorMultiplier(T entitylivingbaseIn, float lightBrightness, float partialTickTime);

    @Shadow
    protected ModelBase mainModel;

    @Inject(method = "rotateCorpse", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/EntityLivingBase;deathTime:I", shift = At.Shift.AFTER))
    protected <T extends EntityLivingBase> void rotateCorpse(T p_rotateCorpse_1_, float p_rotateCorpse_2_, float p_rotateCorpse_3_, float p_rotateCorpse_4_, CallbackInfo ci) {
        String s = EnumChatFormatting.getTextWithoutFormattingCodes(p_rotateCorpse_1_.getName());
            if (s != null && (PlayerEdit.rotatePlayer.get() && p_rotateCorpse_1_.equals(Minecraft.getMinecraft().thePlayer) && Objects.requireNonNull(LiquidBounce.moduleManager.get(PlayerEdit.class)).getState()) && (!(p_rotateCorpse_1_ instanceof EntityPlayer) || ((EntityPlayer)p_rotateCorpse_1_).isWearing(EnumPlayerModelParts.CAPE))) {
                GlStateManager.translate(0.0F, p_rotateCorpse_1_.height + 0.1F, 0.0F);
                GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
            }
    }

    @Inject(method = "doRender(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V", at = @At("HEAD"), cancellable = true)
    private <T extends EntityLivingBase> void injectChamsPre(T entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo callbackInfo) {
        final Chams chams = LiquidBounce.moduleManager.getModule(Chams.class);
        final NoRender noRender = LiquidBounce.moduleManager.getModule(NoRender.class);

        assert noRender != null;
        if (noRender.getState() && noRender.shouldStopRender(entity)) {
            callbackInfo.cancel();
            return;
        }

        assert chams != null;
        if (chams.getState() && chams.getTargetsValue().get() && chams.getLegacyMode().get() && ((chams.getLocalPlayerValue().get() && entity == Minecraft.getMinecraft().thePlayer) || EntityUtils.isSelected(entity, false))) {
            GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL);
            GL11.glPolygonOffset(1.0F, -1000000F);
        }
    }

    @Inject(method = "doRender(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V", at = @At("RETURN"))
    private <T extends EntityLivingBase> void injectChamsPost(T entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo callbackInfo) {
        final Chams chams = LiquidBounce.moduleManager.getModule(Chams.class);
        final NoRender noRender = LiquidBounce.moduleManager.getModule(NoRender.class);


        assert chams != null;
        if (chams.getState() && chams.getTargetsValue().get() && chams.getLegacyMode().get() && ((chams.getLocalPlayerValue().get() && entity == Minecraft.getMinecraft().thePlayer) || EntityUtils.isSelected(entity, false))) {
            assert noRender != null;
            if (!(noRender.getState() && noRender.shouldStopRender(entity))) {
                GL11.glPolygonOffset(1.0F, 1000000F);
                GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);
            }
        }
    }

    @Inject(method = "canRenderName", at = @At("HEAD"), cancellable = true)
    private <T extends EntityLivingBase> void canRenderName(T entity, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        final NoRender noRender = LiquidBounce.moduleManager.getModule(NoRender.class);

        if (!ESP.renderNameTags
                || (Objects.requireNonNull(LiquidBounce.moduleManager.getModule(NameTags.class)).getState() && ((Objects.requireNonNull(LiquidBounce.moduleManager.getModule(NameTags.class)).getLocalValue().get() && entity == Minecraft.getMinecraft().thePlayer && (!Objects.requireNonNull(LiquidBounce.moduleManager.getModule(NameTags.class)).getNfpValue().get() || Minecraft.getMinecraft().gameSettings.thirdPersonView != 0)) || EntityUtils.isSelected(entity, false)))
                || ESP2D.shouldCancelNameTag(entity)
                || (Objects.requireNonNull(noRender).getState() && noRender.getNameTagsValue().get()))
            callbackInfoReturnable.setReturnValue(false);
    }

    /**
     * @author Randomguy && wxdbie
     * @reason FakeBode,Baby
     */
    @Overwrite
    public<T extends EntityLivingBase> void doRender(T entity, double x, double y, double z, float entityYaw, float partialTicks)
    {
        GlStateManager.pushMatrix();
        GlStateManager.disableCull();
        this.mainModel.swingProgress = this.getSwingProgress(entity, partialTicks);
        this.mainModel.isRiding = entity.isRiding();
        this.mainModel.isChild = PlayerEdit.baby.get() && (LiquidBounce.moduleManager.getModule(PlayerEdit.class).onlyMe.get() && entity == Minecraft.getMinecraft().thePlayer || LiquidBounce.moduleManager.getModule(PlayerEdit.class).onlyOther.get() && entity != Minecraft.getMinecraft().thePlayer) && LiquidBounce.moduleManager.getModule(PlayerEdit.class).getState() ? true :entity.isChild();

        try
        {
            float f = this.interpolateRotation(entity.prevRenderYawOffset, entity.renderYawOffset, partialTicks);
            float f1 = this.interpolateRotation(entity.prevRotationYawHead, entity.rotationYawHead, partialTicks);
            float f2 = f1 - f;

            if (entity.isRiding() && entity.ridingEntity instanceof EntityLivingBase)
            {
                EntityLivingBase entitylivingbase = (EntityLivingBase)entity.ridingEntity;
                f = this.interpolateRotation(entitylivingbase.prevRenderYawOffset, entitylivingbase.renderYawOffset, partialTicks);
                f2 = f1 - f;
                float f3 = MathHelper.wrapAngleTo180_float(f2);

                if (f3 < -85.0F)
                {
                    f3 = -85.0F;
                }

                if (f3 >= 85.0F)
                {
                    f3 = 85.0F;
                }

                f = f1 - f3;

                if (f3 * f3 > 2500.0F)
                {
                    f += f3 * 0.2F;
                }
            }

            float f7 = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;
            this.renderLivingAt(entity, x, y, z);
            float f8 = this.handleRotationFloat(entity, partialTicks);
            this.rotateCorpse(entity, f8, f, partialTicks);
            GlStateManager.enableRescaleNormal();
            GlStateManager.scale(-1.0F, -1.0F, 1.0F);
            this.preRenderCallback(entity, partialTicks);
            float f4 = 0.0625F;
            GlStateManager.translate(0.0F, -1.5078125F, 0.0F);
            float f5 = entity.prevLimbSwingAmount + (entity.limbSwingAmount - entity.prevLimbSwingAmount) * partialTicks;
            float f6 = entity.limbSwing - entity.limbSwingAmount * (1.0F - partialTicks);

            if (entity.isChild())
            {
                f6 *= 3.0F;
            }

            if (f5 > 1.0F)
            {
                f5 = 1.0F;
            }

            GlStateManager.enableAlpha();
            this.mainModel.setLivingAnimations(entity, f6, f5, partialTicks);
            this.mainModel.setRotationAngles(f6, f5, f8, f2, f7, 0.0625F, entity);

            if (this.renderOutlines)
            {
                boolean flag1 = this.setScoreTeamColor(entity);
                this.renderModel(entity, f6, f5, f8, f2, f7, 0.0625F);

                if (flag1)
                {
                    this.unsetScoreTeamColor();
                }
            }
            else
            {

                boolean flag = this.setDoRenderBrightness(entity, partialTicks);
                this.renderModel(entity, f6, f5, f8, f2, f7, 0.0625F);

                if (flag)
                {
                    this.unsetBrightness();
                }

                GlStateManager.depthMask(true);

                if (!(entity instanceof EntityPlayer) || !((EntityPlayer)entity).isSpectator())
                {
                    this.renderLayers(entity, f6, f5, partialTicks, f8, f2, f7, 0.0625F);
                }
            }


            Rotations rotations = (Rotations)LiquidBounce.moduleManager.getModule(Rotations.class);
            float renderpitch = (Minecraft.getMinecraft().gameSettings.thirdPersonView != 0 && rotations.getState() && rotations.getFakeValue().get() && entity == Minecraft.getMinecraft().thePlayer) ? (entity.prevRotationPitch + (((RotationUtils.serverRotation.getPitch() != 0.0f) ? RotationUtils.serverRotation.getPitch() : entity.rotationPitch) - entity.prevRotationPitch)) : (entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks);
            float renderyaw = (Minecraft.getMinecraft().gameSettings.thirdPersonView != 0 && rotations.getState() && rotations.getFakeValue().get() && entity == Minecraft.getMinecraft().thePlayer) ? (entity.prevRotationYaw + (((RotationUtils.serverRotation.getYaw() != 0.0f) ? RotationUtils.serverRotation.getYaw() : entity.rotationYaw) - entity.prevRotationYaw)) : (entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks);
            assert rotations != null;
            if(rotations.getState() && rotations.getFakeValue().get()&&entity.equals(Minecraft.getMinecraft().thePlayer) && rotations.shouldRotate()) {
                //假身绘制 :/
                glPushMatrix();
                GL11.glPushAttrib(1048575);
                GL11.glDisable(2929);
                GL11.glDisable(3553);
                GL11.glDisable(3553);
                GL11.glEnable(3042);
                GL11.glBlendFunc(770,771);
                GL11.glDisable(2896);
                GL11.glPolygonMode(1032,6914);
                GL11.glColor4f(rotations.getR().get() / 255, rotations.getG().get() / 255, rotations.getB().get(), rotations.getAlpha().get() / 255);
                GL11.glRotatef(renderyaw - f, 0, 0.001f, 0);
                this.mainModel.render(Minecraft.getMinecraft().thePlayer, f6, f5, renderpitch, f2, renderpitch, 0.0625F);
                GL11.glEnable(2896);
                GL11.glDisable(3042);
                GL11.glEnable(3553);
                GL11.glEnable(2929);
                GL11.glColor3d(1,1,1);
                GL11.glPopAttrib();
                GL11.glPopMatrix();
            }

            GlStateManager.disableRescaleNormal();
        }
        catch (Exception exception)
        {
            logger.error((String)"Couldn\'t render entity", (Throwable)exception);
        }

        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.enableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.enableCull();
        GlStateManager.popMatrix();

        if (!this.renderOutlines)
        {
            super.doRender(entity, x, y, z, entityYaw, partialTicks);
        }
    }

    /**
     * @author Randomguy && wxdbie
     * @reason for FakeBode
     */
    @Overwrite
    protected <T extends EntityLivingBase> boolean setBrightness(T entitylivingbaseIn, float partialTicks, boolean combineTextures)
    {
        Camera camera = (Camera)LiquidBounce.moduleManager.getModule(Camera.class);
        float f = entitylivingbaseIn.getBrightness(partialTicks);
        int i = this.getColorMultiplier(entitylivingbaseIn, f, partialTicks);
        boolean flag = (i >> 24 & 255) > 0;
        boolean flag1 = entitylivingbaseIn.hurtTime > 0 || entitylivingbaseIn.deathTime > 0;

        if (!flag && !flag1)
        {
            return false;
        }
        else if (!flag && !combineTextures)
        {
            return false;
        }
        else
        {
            GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
            GlStateManager.enableTexture2D();
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, OpenGlHelper.GL_COMBINE);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_COMBINE_RGB, GL11.GL_MODULATE);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_SOURCE0_RGB, OpenGlHelper.defaultTexUnit);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_SOURCE1_RGB, OpenGlHelper.GL_PRIMARY_COLOR);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_OPERAND0_RGB, GL11.GL_SRC_COLOR);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_OPERAND1_RGB, GL11.GL_SRC_COLOR);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_COMBINE_ALPHA, GL11.GL_REPLACE);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_SOURCE0_ALPHA, OpenGlHelper.defaultTexUnit);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_OPERAND0_ALPHA, GL11.GL_SRC_ALPHA);
            GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
            GlStateManager.enableTexture2D();
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, OpenGlHelper.GL_COMBINE);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_COMBINE_RGB, OpenGlHelper.GL_INTERPOLATE);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_SOURCE0_RGB, OpenGlHelper.GL_CONSTANT);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_SOURCE1_RGB, OpenGlHelper.GL_PREVIOUS);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_SOURCE2_RGB, OpenGlHelper.GL_CONSTANT);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_OPERAND0_RGB, GL11.GL_SRC_COLOR);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_OPERAND1_RGB, GL11.GL_SRC_COLOR);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_OPERAND2_RGB, GL11.GL_SRC_ALPHA);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_COMBINE_ALPHA, GL11.GL_REPLACE);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_SOURCE0_ALPHA, OpenGlHelper.GL_PREVIOUS);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_OPERAND0_ALPHA, GL11.GL_SRC_ALPHA);
            this.brightnessBuffer.position(0);

            if (flag1) {
                assert camera != null;
                if (camera.getState() && camera.getHitColorValue().get()) {
                    int color = new Color(camera.getHitColorRValue().get(), camera.getHitColorGValue().get(), camera.getHitColorBValue().get(), camera.getHitColorAlphaValue().get()).getRGB();
                    float red = (float) (color >> 16 & 0xFF) / 255.0f;
                    float green = (float) (color >> 8 & 0xFF) / 255.0f;
                    float blue = (float) (color & 0xFF) / 255.0f;
                    float alpha = (float) (color >> 24 & 0xFF) / 255.0f;
                    this.brightnessBuffer.put(red);
                    this.brightnessBuffer.put(green);
                    this.brightnessBuffer.put(blue);
                    this.brightnessBuffer.put(alpha);
                } else {
                    this.brightnessBuffer.put(1.0F);
                    this.brightnessBuffer.put(0.0F);
                    this.brightnessBuffer.put(0.0F);
                    this.brightnessBuffer.put(0.3F);
                }
            }
            else
            {
                float f1 = (float)(i >> 24 & 255) / 255.0F;
                float f2 = (float)(i >> 16 & 255) / 255.0F;
                float f3 = (float)(i >> 8 & 255) / 255.0F;
                float f4 = (float)(i & 255) / 255.0F;
                this.brightnessBuffer.put(f2);
                this.brightnessBuffer.put(f3);
                this.brightnessBuffer.put(f4);
                this.brightnessBuffer.put(1.0F - f1);
            }

            this.brightnessBuffer.flip();
            GL11.glTexEnv(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_COLOR, (FloatBuffer)this.brightnessBuffer);
            GlStateManager.setActiveTexture(OpenGlHelper.GL_TEXTURE2);
            GlStateManager.enableTexture2D();
            GlStateManager.bindTexture(field_177096_e.getGlTextureId());
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, OpenGlHelper.GL_COMBINE);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_COMBINE_RGB, GL11.GL_MODULATE);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_SOURCE0_RGB, OpenGlHelper.GL_PREVIOUS);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_SOURCE1_RGB, OpenGlHelper.lightmapTexUnit);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_OPERAND0_RGB, GL11.GL_SRC_COLOR);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_OPERAND1_RGB, GL11.GL_SRC_COLOR);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_COMBINE_ALPHA, GL11.GL_REPLACE);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_SOURCE0_ALPHA, OpenGlHelper.GL_PREVIOUS);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_OPERAND0_ALPHA, GL11.GL_SRC_ALPHA);
            GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
            return true;
        }
    }

    /**
     * @author CCBlueX
     */
    @Overwrite
    protected <T extends EntityLivingBase> void renderModel(T entitylivingbaseIn, float p_77036_2_, float p_77036_3_, float p_77036_4_, float p_77036_5_, float p_77036_6_, float scaleFactor) {
        boolean visible = !entitylivingbaseIn.isInvisible();
        final TrueSight trueSight = (TrueSight) LiquidBounce.moduleManager.getModule(TrueSight.class);
        final Chams chams = LiquidBounce.moduleManager.getModule(Chams.class);
        assert chams != null;
        boolean chamsFlag = (chams.getState() && chams.getTargetsValue().get() && !chams.getLegacyMode().get() && ((chams.getLocalPlayerValue().get() && entitylivingbaseIn == Minecraft.getMinecraft().thePlayer) || EntityUtils.isSelected(entitylivingbaseIn, false)));
        boolean semiVisible = !visible && (!entitylivingbaseIn.isInvisibleToPlayer(Minecraft.getMinecraft().thePlayer) || (Objects.requireNonNull(trueSight).getState() && trueSight.getEntitiesValue().get()));
        if(visible || semiVisible) {
            if(!this.bindEntityTexture(entitylivingbaseIn))
                return;

            if(semiVisible) {
                GlStateManager.pushMatrix();
                GlStateManager.color(1.0F, 1.0F, 1.0F, 0.15F);
                GlStateManager.depthMask(false);
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(770, 771);
                GlStateManager.alphaFunc(516, 0.003921569F);
            }

            final ESP esp = LiquidBounce.moduleManager.getModule(ESP.class);
            assert esp != null;
            if(esp.getState() && EntityUtils.isSelected(entitylivingbaseIn, false)) {
                Minecraft mc = Minecraft.getMinecraft();
                boolean fancyGraphics = mc.gameSettings.fancyGraphics;
                mc.gameSettings.fancyGraphics = false;

                float gamma = mc.gameSettings.gammaSetting;
                mc.gameSettings.gammaSetting = 100000F;

                switch(esp.modeValue.get().toLowerCase()) {
                    case "wireframe":
                        glPushMatrix();
                        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
                        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
                        GL11.glDisable(GL11.GL_TEXTURE_2D);
                        GL11.glDisable(GL_LIGHTING);
                        GL11.glDisable(GL_DEPTH_TEST);
                        GL11.glEnable(GL11.GL_LINE_SMOOTH);
                        GL11.glEnable(GL11.GL_BLEND);
                        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                        RenderUtils.glColor(esp.getColor(entitylivingbaseIn));
                        GL11.glLineWidth(esp.wireframeWidth.get());
                        this.mainModel.render(entitylivingbaseIn, p_77036_2_, p_77036_3_, p_77036_4_, p_77036_5_, p_77036_6_, scaleFactor);
                        GL11.glPopAttrib();
                        GL11.glPopMatrix();
                        break;
                    case "outline":
                        ClientUtils.disableFastRender();
                        GlStateManager.resetColor();

                        final Color color = esp.getColor(entitylivingbaseIn);
                        OutlineUtils.setColor(color);
                        OutlineUtils.renderOne(esp.outlineWidth.get());
                        this.mainModel.render(entitylivingbaseIn, p_77036_2_, p_77036_3_, p_77036_4_, p_77036_5_, p_77036_6_, scaleFactor);
                        OutlineUtils.setColor(color);
                        OutlineUtils.renderTwo();
                        this.mainModel.render(entitylivingbaseIn, p_77036_2_, p_77036_3_, p_77036_4_, p_77036_5_, p_77036_6_, scaleFactor);
                        OutlineUtils.setColor(color);
                        OutlineUtils.renderThree();
                        this.mainModel.render(entitylivingbaseIn, p_77036_2_, p_77036_3_, p_77036_4_, p_77036_5_, p_77036_6_, scaleFactor);
                        OutlineUtils.setColor(color);
                        OutlineUtils.renderFour(color);
                        this.mainModel.render(entitylivingbaseIn, p_77036_2_, p_77036_3_, p_77036_4_, p_77036_5_, p_77036_6_, scaleFactor);
                        OutlineUtils.setColor(color);
                        OutlineUtils.renderFive();
                        OutlineUtils.setColor(Color.WHITE);
                }
                mc.gameSettings.fancyGraphics = fancyGraphics;
                mc.gameSettings.gammaSetting = gamma;
            }

            final int blend = 3042;
            final int depth = 2929;
            final int srcAlpha = 770;
            final int srcAlphaPlus1 = srcAlpha + 1;
            final int polygonOffsetLine = 10754;
            final int texture2D = 3553;
            final int lighting = 2896;

            boolean textured = chams.getTexturedValue().get();

            Color chamsColor = new Color(0x00000000);

            switch (chams.getColorModeValue().get()) {
                case "Custom":
                    chamsColor = new Color(chams.getRedValue().get(), chams.getGreenValue().get(), chams.getBlueValue().get());
                    break;
                case "Rainbow":
                    chamsColor = new Color(RenderUtils.getRainbowOpaque(chams.getMixerSecondsValue().get(), chams.getSaturationValue().get(), chams.getBrightnessValue().get(), 0));
                    break;
                case "Sky":
                    chamsColor = RenderUtils.skyRainbow(0, chams.getSaturationValue().get(), chams.getBrightnessValue().get());
                    break;
                case "LiquidSlowly":
                    chamsColor = ColorUtils.LiquidSlowly(System.nanoTime(), 0, chams.getSaturationValue().get(), chams.getBrightnessValue().get());
                    break;
                case "Mixer":
                    chamsColor = ColorMixer.getMixedColor(0, chams.getMixerSecondsValue().get());
                    break;
                case "Fade":
                    chamsColor = ColorUtils.fade(new Color(chams.getRedValue().get(), chams.getGreenValue().get(), chams.getBlueValue().get(), chams.getAlphaValue().get()), 0, 100);
                    break;
            }

            chamsColor = ColorUtils.reAlpha(chamsColor, chams.getAlphaValue().get());

            if (chamsFlag) {
                Color chamsColor2 = new Color(0x00000000);

                switch (chams.getBehindColorModeValue().get()) {
                    case "Same":
                        chamsColor2 = chamsColor;
                        break;
                    case "Opposite":
                        chamsColor2 = ColorUtils.getOppositeColor(chamsColor);
                        break;
                    case "Red":
                        chamsColor2 = new Color(0xffEF2626);
                        break;
                }

                glPushMatrix();
                GL11.glEnable(polygonOffsetLine);
                GL11.glPolygonOffset(1.0F, 1000000.0F);
                OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);

                if (!textured) {
                    GL11.glEnable(blend);
                    GL11.glDisable(texture2D);
                    GL11.glDisable(lighting);
                    GL11.glBlendFunc(srcAlpha, srcAlphaPlus1);
                    GL11.glColor4f(chamsColor2.getRed() / 255.0F, chamsColor2.getGreen() / 255.0F, chamsColor2.getBlue() / 255.0F, chamsColor2.getAlpha() / 255.0F);
                }

                GL11.glDisable(depth);
                GL11.glDepthMask(false);
            }

            this.mainModel.render(entitylivingbaseIn, p_77036_2_, p_77036_3_, p_77036_4_, p_77036_5_, p_77036_6_, scaleFactor);

            if (chamsFlag) {
                GL11.glEnable(depth);
                GL11.glDepthMask(true);

                if (!textured) {
                    GL11.glColor4f(chamsColor.getRed() / 255.0F, chamsColor.getGreen() / 255.0F, chamsColor.getBlue() / 255.0F, chamsColor.getAlpha() / 255.0F);
                }

                this.mainModel.render(entitylivingbaseIn, p_77036_2_, p_77036_3_, p_77036_4_, p_77036_5_, p_77036_6_, scaleFactor);

                if (!textured) {
                    GL11.glEnable(texture2D);
                    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                    GL11.glDisable(blend);
                    GL11.glEnable(lighting);
                }

                GL11.glPolygonOffset(1.0f, -1000000.0f);
                GL11.glDisable(polygonOffsetLine);
                GL11.glPopMatrix();
            }

            if (semiVisible) {
                GlStateManager.disableBlend();
                GlStateManager.alphaFunc(516, 0.1F);
                GlStateManager.popMatrix();
                GlStateManager.depthMask(true);
            }
        }

    }
}
