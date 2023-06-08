/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.client;

import de.enzaxd.viaforge.ViaForge;
import de.enzaxd.viaforge.util.AttackOrder;
import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.event.*;
import net.ccbluex.liquidbounce.features.module.modules.combat.AutoClicker;
import net.ccbluex.liquidbounce.features.module.modules.combat.BowAimbot;
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura;
import net.ccbluex.liquidbounce.features.module.modules.movement.Fly;
import net.ccbluex.liquidbounce.features.module.modules.player.Patcher;
import net.ccbluex.liquidbounce.features.module.modules.render.Rotations;
import net.ccbluex.liquidbounce.features.module.modules.render.SpinBot;
import net.ccbluex.liquidbounce.features.module.modules.world.*;
import net.ccbluex.liquidbounce.injection.forge.mixins.accessors.MinecraftForgeClientAccessor;
import net.ccbluex.liquidbounce.ui.client.GuiMainMenu;
import net.ccbluex.liquidbounce.utils.CPSCounter;
import net.ccbluex.liquidbounce.utils.RotationUtils;
import net.ccbluex.liquidbounce.utils.render.IconUtils;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.main.GameConfiguration;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.stream.IStream;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Util;
import net.minecraftforge.client.MinecraftForgeClient;
import org.apache.commons.lang3.SystemUtils;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.ByteBuffer;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft {

    @Shadow
    public GuiScreen currentScreen;

    @Shadow
    private Entity renderViewEntity;

    @Shadow
    private boolean fullscreen;

    @Shadow
    public boolean skipRenderWorld;

    @Shadow
    private int leftClickCounter;

    @Shadow
    public MovingObjectPosition objectMouseOver;

    @Shadow
    public WorldClient theWorld;

    @Shadow
    public EntityPlayerSP thePlayer;

    @Shadow
    public EffectRenderer effectRenderer;

    @Shadow public EntityRenderer entityRenderer;

    @Shadow
    public PlayerControllerMP playerController;

    @Shadow
    public int displayWidth;

    @Shadow
    public int displayHeight;

    @Shadow
    public int rightClickDelayTimer;

    @Shadow
    public GameSettings gameSettings;

    @Shadow
    public abstract IResourceManager getResourceManager();

    @Inject(method = "<init>", at = @At("RETURN"))
    public void injectConstructor(GameConfiguration p_i45547_1_, CallbackInfo ci) {
        try {
            ViaForge.getInstance().start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Inject(method = "run", at = @At("HEAD"))
    private void init(CallbackInfo callbackInfo) {
        if(displayWidth < 1067)
            displayWidth = 1067;

        if(displayHeight < 622)
            displayHeight = 622;
    }

    @Inject(method = "startGame", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;checkGLError(Ljava/lang/String;)V", ordinal = 2, shift = At.Shift.AFTER))
    private void startGame(CallbackInfo callbackInfo) {
        LiquidBounce.INSTANCE.startClient();
    }

    @Inject(method = "createDisplay", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/Display;setTitle(Ljava/lang/String;)V", shift = At.Shift.AFTER))
    private void createDisplay(CallbackInfo callbackInfo) {
        Display.setTitle(LiquidBounce.CLIENT_NAME);
    }

    @Inject(method = "loadWorld(Lnet/minecraft/client/multiplayer/WorldClient;Ljava/lang/String;)V", at = @At("HEAD"))
    private void clearLoadedMaps(WorldClient worldClientIn, String loadingMessage, CallbackInfo ci) {
        if (worldClientIn != this.theWorld) {
            this.entityRenderer.getMapItemRenderer().clearLoadedMaps();
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    @Inject(
            method = "loadWorld(Lnet/minecraft/client/multiplayer/WorldClient;Ljava/lang/String;)V",
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;theWorld:Lnet/minecraft/client/multiplayer/WorldClient;", opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER)
    )
    private void clearRenderCache(CallbackInfo ci) {
        //noinspection ResultOfMethodCallIgnored
        MinecraftForgeClient.getRenderPass(); // Ensure class is loaded, strange accessor issue
        MinecraftForgeClientAccessor.getRegionCache().invalidateAll();
        MinecraftForgeClientAccessor.getRegionCache().cleanUp();
    }

    @Redirect(
            method = "runGameLoop",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/stream/IStream;func_152935_j()V")
    )
    private void skipTwitchCode1(IStream instance) {
        // No-op
    }

    @Redirect(
            method = "runGameLoop",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/stream/IStream;func_152922_k()V")
    )
    private void skipTwitchCode2(IStream instance) {
        // No-op
    }

    @Inject(method = "displayGuiScreen", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;currentScreen:Lnet/minecraft/client/gui/GuiScreen;", shift = At.Shift.AFTER))
    private void displayGuiScreen(CallbackInfo callbackInfo) {
        if(currentScreen instanceof net.minecraft.client.gui.GuiMainMenu || (currentScreen != null && currentScreen.getClass().getName().startsWith("net.labymod") && currentScreen.getClass().getSimpleName().equals("ModGuiMainMenu"))) {
            currentScreen = new GuiMainMenu();

            ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
            currentScreen.setWorldAndResolution(Minecraft.getMinecraft(), scaledResolution.getScaledWidth(), scaledResolution.getScaledHeight());
            skipRenderWorld = false;
        }

        LiquidBounce.eventManager.callEvent(new ScreenEvent(currentScreen));
    }

    private long lastFrame = getTime();

    @Inject(method = "runGameLoop", at = @At("HEAD"))
    private void runGameLoop(final CallbackInfo callbackInfo) {
        final long currentTime = getTime();
        final int deltaTime = (int) (currentTime - lastFrame);
        lastFrame = currentTime;

        RenderUtils.deltaTime = deltaTime;
    }

    public long getTime() {
        return (Sys.getTime() * 1000) / Sys.getTimerResolution();
    }

    @Inject(method = "runTick", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;joinPlayerCounter:I", shift = At.Shift.BEFORE))
    private void onTick(final CallbackInfo callbackInfo) {
        LiquidBounce.eventManager.callEvent(new TickEvent());
    }

    @Inject(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;dispatchKeypresses()V", shift = At.Shift.AFTER))
    private void onKey(CallbackInfo callbackInfo) {
        if(Keyboard.getEventKeyState() && currentScreen == null)
            LiquidBounce.eventManager.callEvent(new KeyEvent(Keyboard.getEventKey() == 0 ? Keyboard.getEventCharacter() + 256 : Keyboard.getEventKey()));
    }

    @Inject(method = "sendClickBlockToController", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/MovingObjectPosition;getBlockPos()Lnet/minecraft/util/BlockPos;"))
    private void onClickBlock(CallbackInfo callbackInfo) {
        if(this.leftClickCounter == 0 && theWorld.getBlockState(objectMouseOver.getBlockPos()).getBlock().getMaterial() != Material.air) {
            LiquidBounce.eventManager.callEvent(new ClickBlockEvent(objectMouseOver.getBlockPos(), this.objectMouseOver.sideHit));
        }
    }

    @Inject(method = "setWindowIcon", at = @At("HEAD"), cancellable = true)
    private void setWindowIcon(CallbackInfo callbackInfo) {
        if(Util.getOSType() != Util.EnumOS.OSX) {
            final ByteBuffer[] liquidBounceFavicon = IconUtils.getFavicon();
            if(liquidBounceFavicon != null) {
                Display.setIcon(liquidBounceFavicon);
                callbackInfo.cancel();
            }
        }
    }

    @Inject(method = "shutdown", at = @At("HEAD"))
    private void shutdown(CallbackInfo callbackInfo) {
        LiquidBounce.INSTANCE.stopClient();
    }

    @Inject(method = "clickMouse", at = @At("HEAD"))
    private void clickMouse(CallbackInfo callbackInfo) {
        CPSCounter.registerClick(CPSCounter.MouseButton.LEFT);

        if (Patcher.noHitDelay.get() || LiquidBounce.moduleManager.getModule(AutoClicker.class).getState())
            leftClickCounter = 0;
    }

    @Redirect(
            method = "clickMouse",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;swingItem()V")
    )
    private void fixAttackOrder_VanillaSwing() {
        AttackOrder.sendConditionalSwing(this.objectMouseOver);
    }

    @Inject(method = "getRenderViewEntity", at = @At("HEAD"))
    public void getRenderViewEntity(CallbackInfoReturnable<Entity> cir) {
        if (renderViewEntity instanceof EntityLivingBase && RotationUtils.serverRotation != null && thePlayer != null) {
            final Rotations Rotations = LiquidBounce.moduleManager.getModule(Rotations.class);
            final KillAura killAura = LiquidBounce.moduleManager.getModule(KillAura.class);
            final Scaffold scaffold = LiquidBounce.moduleManager.getModule(Scaffold.class);
            final Disabler disabler = LiquidBounce.moduleManager.getModule(Disabler.class);
            final SpinBot spinBot = LiquidBounce.moduleManager.getModule(SpinBot.class);
            final ChestAura chestAura = LiquidBounce.moduleManager.getModule(ChestAura.class);
            final Fly fly = LiquidBounce.moduleManager.getModule(Fly.class);
            final BowAimbot bowAimbot = LiquidBounce.moduleManager.getModule(BowAimbot.class);
            final Breaker fucker = LiquidBounce.moduleManager.getModule(Breaker.class);
            final Nuker nuker = LiquidBounce.moduleManager.getModule(Nuker.class);
            final EntityLivingBase entityLivingBase = (EntityLivingBase) renderViewEntity;
            final float yaw = RotationUtils.serverRotation.getYaw();
            if (killAura.getTarget() != null && Rotations.getHeadValue().get() && Rotations.getState()) {
                entityLivingBase.rotationYawHead = yaw;
            }
            if (killAura.getTarget() != null && Rotations.getBodyValue().get() == "Normal" && Rotations.getState()) {
                entityLivingBase.renderYawOffset = yaw;
                entityLivingBase.prevRenderYawOffset = yaw;
            }
            if (scaffold.getState() && Rotations.getHeadValue().get() && Rotations.getState()) {
                entityLivingBase.rotationYawHead = yaw;
            }
            if (scaffold.getState() && Rotations.getBodyValue().get() == "Normal" && Rotations.getState()) {
                entityLivingBase.renderYawOffset = yaw;
                entityLivingBase.prevRenderYawOffset = yaw;
            }
            if (disabler.getCanRenderInto3D() && Rotations.getHeadValue().get() && Rotations.getState()) {
                entityLivingBase.rotationYawHead = yaw;
            }
            if (disabler.getCanRenderInto3D() && Rotations.getBodyValue().get() == "Normal" && Rotations.getState()) {
                entityLivingBase.renderYawOffset = yaw;
                entityLivingBase.prevRenderYawOffset = yaw;
            }
            if (spinBot.getState() && Rotations.getHeadValue().get() && Rotations.getState()) {
                entityLivingBase.rotationYawHead = yaw;
            }
            if (spinBot.getState() && Rotations.getBodyValue().get() == "Normal" && Rotations.getState()) {
                entityLivingBase.renderYawOffset = yaw;
                entityLivingBase.prevRenderYawOffset = yaw;
            }
            if (chestAura.getState() && Rotations.getHeadValue().get() && Rotations.getState()) {
                entityLivingBase.rotationYawHead = yaw;
            }
            if (chestAura.getState() && Rotations.getBodyValue().get() == "Normal" && Rotations.getState()) {
                entityLivingBase.renderYawOffset = yaw;
                entityLivingBase.prevRenderYawOffset = yaw;
            }
            if (fly.getState() && Rotations.getHeadValue().get() && Rotations.getState()) {
                entityLivingBase.rotationYawHead = yaw;
            }
            if (fly.getState() && Rotations.getBodyValue().get() == "Normal" && Rotations.getState()) {
                entityLivingBase.renderYawOffset = yaw;
                entityLivingBase.prevRenderYawOffset = yaw;
            }
            if (bowAimbot.getState() && Rotations.getHeadValue().get() && Rotations.getState()) {
                entityLivingBase.rotationYawHead = yaw;
            }
            if (bowAimbot.getState() && Rotations.getBodyValue().get() == "Normal" && Rotations.getState()) {
                entityLivingBase.renderYawOffset = yaw;
                entityLivingBase.prevRenderYawOffset = yaw;
            }
            if (fucker.getState() && Rotations.getHeadValue().get() && Rotations.getState()) {
                entityLivingBase.rotationYawHead = yaw;
            }
            if (fucker.getState() && Rotations.getBodyValue().get() == "Normal" && Rotations.getState()) {
                entityLivingBase.renderYawOffset = yaw;
                entityLivingBase.prevRenderYawOffset = yaw;
            }
            if (nuker.getState() && Rotations.getHeadValue().get() && Rotations.getState()) {
                entityLivingBase.rotationYawHead = yaw;
            }
            if (nuker.getState() && Rotations.getBodyValue().get() == "Normal" && Rotations.getState()) {
                entityLivingBase.renderYawOffset = yaw;
                entityLivingBase.prevRenderYawOffset = yaw;
            }
            if (killAura.getTarget() != null && Rotations.getBodyValue().get() == "Legit" && Rotations.getState()) {
                entityLivingBase.renderYawOffset = yaw;
                entityLivingBase.prevRenderYawOffset = yaw;
                if (Minecraft.getMinecraft().gameSettings.keyBindRight.isKeyDown() && !Minecraft.getMinecraft().gameSettings.keyBindLeft.isKeyDown() && !Minecraft.getMinecraft().gameSettings.keyBindBack.isKeyDown()) {
                    entityLivingBase.renderYawOffset = yaw + 45;
                    entityLivingBase.prevRenderYawOffset = yaw + 45;
                }
                if (Minecraft.getMinecraft().gameSettings.keyBindLeft.isKeyDown() && !Minecraft.getMinecraft().gameSettings.keyBindRight.isKeyDown() && !Minecraft.getMinecraft().gameSettings.keyBindBack.isKeyDown() || Minecraft.getMinecraft().gameSettings.keyBindBack.isKeyDown() && !Minecraft.getMinecraft().gameSettings.keyBindRight.isKeyDown() && !Minecraft.getMinecraft().gameSettings.keyBindLeft.isKeyDown()) {
                    entityLivingBase.renderYawOffset = yaw - 45;
                    entityLivingBase.prevRenderYawOffset = yaw - 45;
                }
            }
            if (scaffold.getState() && Rotations.getBodyValue().get() == "Legit" && Rotations.getState()) {
                entityLivingBase.renderYawOffset = yaw;
                entityLivingBase.prevRenderYawOffset = yaw;
                if (Minecraft.getMinecraft().gameSettings.keyBindRight.isKeyDown() && !Minecraft.getMinecraft().gameSettings.keyBindLeft.isKeyDown() && !Minecraft.getMinecraft().gameSettings.keyBindBack.isKeyDown()) {
                    entityLivingBase.renderYawOffset = yaw + 45;
                    entityLivingBase.prevRenderYawOffset = yaw + 45;
                }
                if (Minecraft.getMinecraft().gameSettings.keyBindLeft.isKeyDown() && !Minecraft.getMinecraft().gameSettings.keyBindRight.isKeyDown() && !Minecraft.getMinecraft().gameSettings.keyBindBack.isKeyDown() || Minecraft.getMinecraft().gameSettings.keyBindBack.isKeyDown() && !Minecraft.getMinecraft().gameSettings.keyBindRight.isKeyDown() && !Minecraft.getMinecraft().gameSettings.keyBindLeft.isKeyDown()) {
                    entityLivingBase.renderYawOffset = yaw - 45;
                    entityLivingBase.prevRenderYawOffset = yaw - 45;
                }
            }
            if (disabler.getCanRenderInto3D() && Rotations.getBodyValue().get() == "Legit" && Rotations.getState()) {
                entityLivingBase.renderYawOffset = yaw;
                entityLivingBase.prevRenderYawOffset = yaw;
                if (Minecraft.getMinecraft().gameSettings.keyBindRight.isKeyDown() && !Minecraft.getMinecraft().gameSettings.keyBindLeft.isKeyDown() && !Minecraft.getMinecraft().gameSettings.keyBindBack.isKeyDown()) {
                    entityLivingBase.renderYawOffset = yaw + 45;
                    entityLivingBase.prevRenderYawOffset = yaw + 45;
                }
                if (Minecraft.getMinecraft().gameSettings.keyBindLeft.isKeyDown() && !Minecraft.getMinecraft().gameSettings.keyBindRight.isKeyDown() && !Minecraft.getMinecraft().gameSettings.keyBindBack.isKeyDown() || Minecraft.getMinecraft().gameSettings.keyBindBack.isKeyDown() && !Minecraft.getMinecraft().gameSettings.keyBindRight.isKeyDown() && !Minecraft.getMinecraft().gameSettings.keyBindLeft.isKeyDown()) {
                    entityLivingBase.renderYawOffset = yaw - 45;
                    entityLivingBase.prevRenderYawOffset = yaw - 45;
                }
            }
            if (spinBot.getState() && Rotations.getBodyValue().get() == "Legit" && Rotations.getState()) {
                entityLivingBase.renderYawOffset = yaw;
                entityLivingBase.prevRenderYawOffset = yaw;
                if (Minecraft.getMinecraft().gameSettings.keyBindRight.isKeyDown() && !Minecraft.getMinecraft().gameSettings.keyBindLeft.isKeyDown() && !Minecraft.getMinecraft().gameSettings.keyBindBack.isKeyDown()) {
                    entityLivingBase.renderYawOffset = yaw + 45;
                    entityLivingBase.prevRenderYawOffset = yaw + 45;
                }

                if (Minecraft.getMinecraft().gameSettings.keyBindLeft.isKeyDown() && !Minecraft.getMinecraft().gameSettings.keyBindRight.isKeyDown() && !Minecraft.getMinecraft().gameSettings.keyBindBack.isKeyDown() || Minecraft.getMinecraft().gameSettings.keyBindBack.isKeyDown() && !Minecraft.getMinecraft().gameSettings.keyBindRight.isKeyDown() && !Minecraft.getMinecraft().gameSettings.keyBindLeft.isKeyDown()) {
                    entityLivingBase.renderYawOffset = yaw - 45;
                    entityLivingBase.prevRenderYawOffset = yaw - 45;
                }
            }
            if (chestAura.getState() && Rotations.getBodyValue().get() == "Legit" && Rotations.getState()) {
                entityLivingBase.renderYawOffset = yaw;
                entityLivingBase.prevRenderYawOffset = yaw;
                if (Minecraft.getMinecraft().gameSettings.keyBindRight.isKeyDown() && !Minecraft.getMinecraft().gameSettings.keyBindLeft.isKeyDown() && !Minecraft.getMinecraft().gameSettings.keyBindBack.isKeyDown()) {
                    entityLivingBase.renderYawOffset = yaw + 45;
                    entityLivingBase.prevRenderYawOffset = yaw + 45;
                }
                if (Minecraft.getMinecraft().gameSettings.keyBindLeft.isKeyDown() && !Minecraft.getMinecraft().gameSettings.keyBindRight.isKeyDown() && !Minecraft.getMinecraft().gameSettings.keyBindBack.isKeyDown() || Minecraft.getMinecraft().gameSettings.keyBindBack.isKeyDown() && !Minecraft.getMinecraft().gameSettings.keyBindRight.isKeyDown() && !Minecraft.getMinecraft().gameSettings.keyBindLeft.isKeyDown()) {
                    entityLivingBase.renderYawOffset = yaw - 45;
                    entityLivingBase.prevRenderYawOffset = yaw - 45;
                }
            }
            if (fly.getState() && Rotations.getBodyValue().get() == "Legit" && Rotations.getState()) {
                entityLivingBase.renderYawOffset = yaw;
                entityLivingBase.prevRenderYawOffset = yaw;
                if (Minecraft.getMinecraft().gameSettings.keyBindRight.isKeyDown() && !Minecraft.getMinecraft().gameSettings.keyBindLeft.isKeyDown() && !Minecraft.getMinecraft().gameSettings.keyBindBack.isKeyDown()) {
                    entityLivingBase.renderYawOffset = yaw + 45;
                    entityLivingBase.prevRenderYawOffset = yaw + 45;
                    if (Minecraft.getMinecraft().gameSettings.keyBindLeft.isKeyDown() && !Minecraft.getMinecraft().gameSettings.keyBindRight.isKeyDown() && !Minecraft.getMinecraft().gameSettings.keyBindBack.isKeyDown() || Minecraft.getMinecraft().gameSettings.keyBindBack.isKeyDown() && !Minecraft.getMinecraft().gameSettings.keyBindRight.isKeyDown() && !Minecraft.getMinecraft().gameSettings.keyBindLeft.isKeyDown()) {
                        entityLivingBase.renderYawOffset = yaw - 45;
                        entityLivingBase.prevRenderYawOffset = yaw - 45;
                    }
                }
                if (bowAimbot.getState() && Rotations.getBodyValue().get() == "Legit" && Rotations.getState()) {
                    entityLivingBase.renderYawOffset = yaw;
                    entityLivingBase.prevRenderYawOffset = yaw;
                    if (Minecraft.getMinecraft().gameSettings.keyBindRight.isKeyDown() && !Minecraft.getMinecraft().gameSettings.keyBindLeft.isKeyDown() && !Minecraft.getMinecraft().gameSettings.keyBindBack.isKeyDown()) {
                        entityLivingBase.renderYawOffset = yaw + 45;
                        entityLivingBase.prevRenderYawOffset = yaw + 45;
                    }
                    if (Minecraft.getMinecraft().gameSettings.keyBindLeft.isKeyDown() && !Minecraft.getMinecraft().gameSettings.keyBindRight.isKeyDown() && !Minecraft.getMinecraft().gameSettings.keyBindBack.isKeyDown() || Minecraft.getMinecraft().gameSettings.keyBindBack.isKeyDown() && !Minecraft.getMinecraft().gameSettings.keyBindRight.isKeyDown() && !Minecraft.getMinecraft().gameSettings.keyBindLeft.isKeyDown()) {
                        entityLivingBase.renderYawOffset = yaw - 45;
                        entityLivingBase.prevRenderYawOffset = yaw - 45;
                    }
                }
                if (fucker.getState() && Rotations.getBodyValue().get() == "Legit" && Rotations.getState()) {
                    entityLivingBase.renderYawOffset = yaw;
                    entityLivingBase.prevRenderYawOffset = yaw;
                    if (Minecraft.getMinecraft().gameSettings.keyBindRight.isKeyDown() && !Minecraft.getMinecraft().gameSettings.keyBindLeft.isKeyDown() && !Minecraft.getMinecraft().gameSettings.keyBindBack.isKeyDown()) {
                        entityLivingBase.renderYawOffset = yaw + 45;
                        entityLivingBase.prevRenderYawOffset = yaw + 45;
                    }
                    if (Minecraft.getMinecraft().gameSettings.keyBindLeft.isKeyDown() && !Minecraft.getMinecraft().gameSettings.keyBindRight.isKeyDown() && !Minecraft.getMinecraft().gameSettings.keyBindBack.isKeyDown() || Minecraft.getMinecraft().gameSettings.keyBindBack.isKeyDown() && !Minecraft.getMinecraft().gameSettings.keyBindRight.isKeyDown() && !Minecraft.getMinecraft().gameSettings.keyBindLeft.isKeyDown()) {
                        entityLivingBase.renderYawOffset = yaw - 45;
                        entityLivingBase.prevRenderYawOffset = yaw - 45;
                    }
                }
                if (nuker.getState() && Rotations.getBodyValue().get() == "Legit" && Rotations.getState()) {
                    entityLivingBase.renderYawOffset = yaw;
                    entityLivingBase.prevRenderYawOffset = yaw;
                    if (Minecraft.getMinecraft().gameSettings.keyBindRight.isKeyDown() && !Minecraft.getMinecraft().gameSettings.keyBindLeft.isKeyDown() && !Minecraft.getMinecraft().gameSettings.keyBindBack.isKeyDown()) {
                        entityLivingBase.renderYawOffset = yaw + 45;
                        entityLivingBase.prevRenderYawOffset = yaw + 45;
                    }
                    if (Minecraft.getMinecraft().gameSettings.keyBindLeft.isKeyDown() && !Minecraft.getMinecraft().gameSettings.keyBindRight.isKeyDown() && !Minecraft.getMinecraft().gameSettings.keyBindBack.isKeyDown() || Minecraft.getMinecraft().gameSettings.keyBindBack.isKeyDown() && !Minecraft.getMinecraft().gameSettings.keyBindRight.isKeyDown() && !Minecraft.getMinecraft().gameSettings.keyBindLeft.isKeyDown()) {
                        entityLivingBase.renderYawOffset = yaw - 45;
                        entityLivingBase.prevRenderYawOffset = yaw - 45;
                    }
                }
            }
        }
    }


    @Redirect(
            method = "clickMouse",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/PlayerControllerMP;attackEntity(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/entity/Entity;)V")
    )
    public void fixAttackOrder_VanillaAttack() {
        AttackOrder.sendFixedAttack(this.thePlayer, this.objectMouseOver.entityHit);
    }

    @Inject(method = "middleClickMouse", at = @At("HEAD"))
    private void middleClickMouse(CallbackInfo ci) {
        CPSCounter.registerClick(CPSCounter.MouseButton.MIDDLE);
    }

    @Inject(method = "rightClickMouse", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;rightClickDelayTimer:I", shift = At.Shift.AFTER))
    private void rightClickMouse(final CallbackInfo callbackInfo) {
        CPSCounter.registerClick(CPSCounter.MouseButton.RIGHT);

        final FastPlace fastPlace = (FastPlace) LiquidBounce.moduleManager.getModule(FastPlace.class);

        if (fastPlace.getState())
            rightClickDelayTimer = fastPlace.getSpeedValue().get();
    }

    @Inject(method = "loadWorld(Lnet/minecraft/client/multiplayer/WorldClient;Ljava/lang/String;)V", at = @At("HEAD"))
    private void loadWorld(WorldClient p_loadWorld_1_, String p_loadWorld_2_, final CallbackInfo callbackInfo) {
        LiquidBounce.eventManager.callEvent(new WorldEvent(p_loadWorld_1_));
    }

    @Inject(method = "toggleFullscreen", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/Display;setFullscreen(Z)V", remap = false))
    private void resolveScreenState(CallbackInfo ci) {
        if (!this.fullscreen && SystemUtils.IS_OS_WINDOWS) {
            Display.setResizable(false);
            Display.setResizable(true);
        }
    }

    @Redirect(method = "dispatchKeypresses", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;getEventCharacter()C", remap = false))
    private char resolveForeignKeyboards() {
        return (char) (Keyboard.getEventCharacter() + 256);
    }

    /**
     * @author CCBlueX
     */
    @Overwrite
    private void sendClickBlockToController(boolean leftClick) {
        if(!leftClick)
            this.leftClickCounter = 0;

        if (this.leftClickCounter <= 0 && (!this.thePlayer.isUsingItem() || LiquidBounce.moduleManager.getModule(MultiActions.class).getState())) {
            if(leftClick && this.objectMouseOver != null && this.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                BlockPos blockPos = this.objectMouseOver.getBlockPos();

                if(this.leftClickCounter == 0)
                    LiquidBounce.eventManager.callEvent(new ClickBlockEvent(blockPos, this.objectMouseOver.sideHit));


                if(this.theWorld.getBlockState(blockPos).getBlock().getMaterial() != Material.air && this.playerController.onPlayerDamageBlock(blockPos, this.objectMouseOver.sideHit)) {
                    this.effectRenderer.addBlockHitEffects(blockPos, this.objectMouseOver.sideHit);
                    this.thePlayer.swingItem();
                }
            } else if (!LiquidBounce.moduleManager.getModule(AbortBreaking.class).getState()) {
                this.playerController.resetBlockRemoving();
            }
        }
    }

    /**
     * @author CCBlueX
     */
    @ModifyConstant(method = "getLimitFramerate", constant = @Constant(intValue = 30))
    public int getLimitFramerate(int constant) {
        return 60;
    }
}