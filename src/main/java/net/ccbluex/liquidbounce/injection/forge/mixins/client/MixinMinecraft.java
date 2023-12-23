/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.client;

import cc.paimonmc.viamcp.ViaMCP;
import cc.paimonmc.viamcp.utils.AttackOrder;
import com.google.common.collect.Queues;
import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.event.*;
import net.ccbluex.liquidbounce.features.module.modules.combat.AutoClicker;
import net.ccbluex.liquidbounce.features.module.modules.combat.TimerRange;
import net.ccbluex.liquidbounce.features.module.modules.exploit.AbortBreaking;
import net.ccbluex.liquidbounce.features.module.modules.exploit.MultiActions;
import net.ccbluex.liquidbounce.features.module.modules.render.FreeLook;
import net.ccbluex.liquidbounce.features.module.modules.world.FastPlace;
import net.ccbluex.liquidbounce.injection.forge.mixins.accessors.MinecraftForgeClientAccessor;
import net.ccbluex.liquidbounce.ui.client.GuiMainMenu;
import net.ccbluex.liquidbounce.utils.CPSCounter;
import net.ccbluex.liquidbounce.utils.render.IconUtils;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MusicTicker;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.achievement.GuiAchievement;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.main.GameConfiguration;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.stream.IStream;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.client.C16PacketClientStatus;
import net.minecraft.profiler.IPlayerUsage;
import net.minecraft.profiler.PlayerUsageSnooper;
import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.*;
import net.minecraft.world.EnumDifficulty;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.apache.commons.lang3.SystemUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import static net.minecraft.client.Minecraft.getSystemTime;
import static org.objectweb.asm.Opcodes.PUTFIELD;

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
    private Profiler mcProfiler;

    @Shadow
    private boolean isGamePaused;

    @Shadow
    public final Timer timer = new Timer(20.0F);

    @Shadow
    private void rightClickMouse() {

    }

    @Shadow
    private void clickMouse() {

    }

    @Shadow
    private void middleClickMouse() {

    }

    @Shadow
    public boolean inGameHasFocus;

    @Shadow
    public abstract IResourceManager getResourceManager();

    @Inject(method = "<init>", at = @At("RETURN"))
    public void injectConstructor(GameConfiguration p_i45547_1_, CallbackInfo ci) {
            ViaMCP.staticInit();
    }


    @Shadow
    private PlayerUsageSnooper usageSnooper = new PlayerUsageSnooper("client", (IPlayerUsage) this, MinecraftServer.getCurrentTimeMillis());

    @Shadow
    private final Queue<FutureTask<?>> scheduledTasks = Queues.newArrayDeque();

    @Shadow
    public abstract void shutdown();

    @Shadow
    public GuiAchievement guiAchievement;

    @Shadow
    int fpsCounter;
    @Shadow
    long prevFrameTime = -1L;

    @Shadow
    private Framebuffer framebufferMc;

    @Shadow
    long startNanoTime = System.nanoTime();

    @Shadow
    public abstract void checkGLError(String message);

    @Shadow
    long debugUpdateTime = getSystemTime();

    @Shadow
    private IStream stream;
    @Shadow
    public final FrameTimer frameTimer = new FrameTimer();

    @Shadow
    public String debug = "";

    @Shadow
    private IntegratedServer theIntegratedServer;

    @Shadow
    public abstract boolean isFramerateLimitBelowMax();

    @Shadow
    private static int debugFPS;

    @Shadow
    public abstract void updateDisplay();

    @Shadow
    public abstract boolean isSingleplayer();

    @Shadow
    private static final Logger logger = LogManager.getLogger();

    @Shadow
    private void displayDebugInfo(long elapsedTicksTime) {

    }

    @Shadow
    private long debugCrashKeyPressTime = -1L;

    @Shadow
    public GuiIngame ingameGUI;

    @Shadow
    public TextureManager renderEngine;

    @Shadow
    public abstract void refreshResources();

    @Shadow
    private int joinPlayerCounter;

    @Shadow
    public abstract void dispatchKeypresses();

    @Shadow
    public RenderGlobal renderGlobal;

    @Shadow
    private RenderManager renderManager;

    @Shadow
    private NetworkManager myNetworkManager;

    @Shadow
    long systemTime = getSystemTime();

    @Shadow
    public abstract Entity getRenderViewEntity();

    @Shadow
    private SoundHandler mcSoundHandler;
    @Shadow
    private MusicTicker mcMusicTicker;
    @Shadow
    public abstract NetHandlerPlayClient getNetHandler();
    @Shadow
    public abstract void setIngameFocus();

    @Shadow
    private void updateDebugProfilerName(int p_updateDebugProfilerName_1_) {

    }
    @Shadow
    public abstract void displayInGameMenu();

    @Shadow
    public void displayGuiScreen(GuiScreen p_displayGuiScreen_1_) {

    }
    @Shadow
    public abstract int getLimitFramerate();

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

    @Inject(method = "displayGuiScreen", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;currentScreen:Lnet/minecraft/client/gui/GuiScreen;", shift = At.Shift.AFTER))
    private void displayGuiScreen1(CallbackInfo callbackInfo) {
        if(currentScreen instanceof net.minecraft.client.gui.GuiMainMenu || (currentScreen != null && currentScreen.getClass().getName().startsWith("net.labymod") && currentScreen.getClass().getSimpleName().equals("ModGuiMainMenu"))) {
            currentScreen = new GuiMainMenu();

            ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
            currentScreen.setWorldAndResolution(Minecraft.getMinecraft(), scaledResolution.getScaledWidth(), scaledResolution.getScaledHeight());
            skipRenderWorld = false;
        }

        LiquidBounce.eventManager.callEvent(new ScreenEvent(currentScreen));
    }

    private long lastFrame = getTime();

    public long getTime() {
        return (Sys.getTime() * 1000) / Sys.getTimerResolution();
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

    /**
     * @author
     * @reason
     */
    @Overwrite
    private void runGameLoop() throws IOException {
        final long currentTime = getTime();
        final int deltaTime = (int) (currentTime - lastFrame);
        lastFrame = currentTime;

        RenderUtils.deltaTime = deltaTime;

        long i = System.nanoTime();
        this.mcProfiler.startSection("root");
        if (Display.isCreated() && Display.isCloseRequested()) {
            this.shutdown();
        }

        if (this.isGamePaused && this.theWorld != null) {
            float f = this.timer.renderPartialTicks;
            this.timer.updateTimer();
            this.timer.renderPartialTicks = f;
        } else {
            this.timer.updateTimer();
        }

        this.mcProfiler.startSection("scheduledExecutables");
        synchronized(this.scheduledTasks) {
            while(!this.scheduledTasks.isEmpty()) {
                Util.runTask((FutureTask)this.scheduledTasks.poll(), logger);
            }
        }

        this.mcProfiler.endSection();
        long l = System.nanoTime();
        this.mcProfiler.startSection("tick");

        for (int j = 0; j < this.timer.elapsedTicks; ++j)
        {
            if (TimerRange.handleTick()) continue;
            this.runTick();
        }

        this.mcProfiler.endStartSection("preRenderErrors");
        long i1 = System.nanoTime() - l;
        this.checkGLError("Pre render");
        this.mcProfiler.endStartSection("sound");
        this.mcSoundHandler.setListener(this.thePlayer, this.timer.renderPartialTicks);
        this.mcProfiler.endSection();
        this.mcProfiler.startSection("render");
        GlStateManager.pushMatrix();
        GlStateManager.clear(16640);
        this.framebufferMc.bindFramebuffer(true);
        this.mcProfiler.startSection("display");
        GlStateManager.enableTexture2D();
        if (this.thePlayer != null && this.thePlayer.isEntityInsideOpaqueBlock()) {
            this.gameSettings.thirdPersonView = 0;
        }

        this.mcProfiler.endSection();
        if (!this.skipRenderWorld && !TimerRange.freezeAnimation())
        {
            FMLCommonHandler.instance().onRenderTickStart(this.timer.renderPartialTicks);
            this.mcProfiler.endStartSection("gameRenderer");
            this.entityRenderer.updateCameraAndRender(this.timer.renderPartialTicks, i);
            this.mcProfiler.endSection();
            FMLCommonHandler.instance().onRenderTickEnd(this.timer.renderPartialTicks);
        }

        this.mcProfiler.endSection();
        if (this.gameSettings.showDebugInfo && this.gameSettings.showDebugProfilerChart && !this.gameSettings.hideGUI) {
            if (!this.mcProfiler.profilingEnabled) {
                this.mcProfiler.clearProfiling();
            }

            this.mcProfiler.profilingEnabled = true;
            this.displayDebugInfo(i1);
        } else {
            this.mcProfiler.profilingEnabled = false;
            this.prevFrameTime = System.nanoTime();
        }

        this.guiAchievement.updateAchievementWindow();
        this.framebufferMc.unbindFramebuffer();
        GlStateManager.popMatrix();
        GlStateManager.pushMatrix();
        this.framebufferMc.framebufferRender(this.displayWidth, this.displayHeight);
        GlStateManager.popMatrix();
        GlStateManager.pushMatrix();
        this.entityRenderer.renderStreamIndicator(this.timer.renderPartialTicks);
        GlStateManager.popMatrix();
        this.mcProfiler.startSection("root");
        this.updateDisplay();
        Thread.yield();
        this.mcProfiler.startSection("stream");
        this.mcProfiler.startSection("update");
        //this.stream.func_152935_j();
        this.mcProfiler.endStartSection("submit");
        //this.stream.func_152922_k();
        this.mcProfiler.endSection();
        this.mcProfiler.endSection();
        this.checkGLError("Post render");
        ++this.fpsCounter;
        this.isGamePaused = this.isSingleplayer() && this.currentScreen != null && this.currentScreen.doesGuiPauseGame() && !this.theIntegratedServer.getPublic();
        long k = System.nanoTime();
        this.frameTimer.addFrame(k - this.startNanoTime);
        this.startNanoTime = k;

        while(getSystemTime() >= this.debugUpdateTime + 1000L) {
            debugFPS = this.fpsCounter;
            this.debug = String.format("%d fps (%d chunk update%s) T: %s%s%s%s%s", debugFPS, RenderChunk.renderChunksUpdated, RenderChunk.renderChunksUpdated != 1 ? "s" : "", (float)this.gameSettings.limitFramerate == GameSettings.Options.FRAMERATE_LIMIT.getValueMax() ? "inf" : this.gameSettings.limitFramerate, this.gameSettings.enableVsync ? " vsync" : "", this.gameSettings.fancyGraphics ? "" : " fast", this.gameSettings.clouds == 0 ? "" : (this.gameSettings.clouds == 1 ? " fast-clouds" : " fancy-clouds"), OpenGlHelper.useVbo() ? " vbo" : "");
            RenderChunk.renderChunksUpdated = 0;
            this.debugUpdateTime += 1000L;
            this.fpsCounter = 0;
            this.usageSnooper.addMemoryStatsToSnooper();
            if (!this.usageSnooper.isSnooperRunning()) {
                this.usageSnooper.startSnooper();
            }
        }

        if (this.isFramerateLimitBelowMax()) {
            this.mcProfiler.startSection("fpslimit_wait");
            Display.sync(this.getLimitFramerate());
            this.mcProfiler.endSection();
        }

        this.mcProfiler.endSection();
    }

    @Inject(method = "shutdown", at = @At("HEAD"))
    private void shutdown1(CallbackInfo callbackInfo) {
        LiquidBounce.INSTANCE.stopClient();
    }

    @Inject(method = "clickMouse", at = @At("HEAD"))
    private void clickMouse1(CallbackInfo callbackInfo) {
        CPSCounter.registerClick(CPSCounter.MouseButton.LEFT);

        if (LiquidBounce.moduleManager.getModule(AutoClicker.class).getState())
            leftClickCounter = 0;
    }

    @Redirect(
            method = "clickMouse",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;swingItem()V")
    )
    private void fixAttackOrder_VanillaSwing() {
        AttackOrder.sendConditionalSwing(this.objectMouseOver);
    }


    @Redirect(
            method = "clickMouse",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/PlayerControllerMP;attackEntity(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/entity/Entity;)V")
    )
    public void fixAttackOrder_VanillaAttack(PlayerControllerMP controller, EntityPlayer player, Entity e) {
        AttackOrder.sendFixedAttack(this.thePlayer, this.objectMouseOver.entityHit);
    }

    @Inject(method = "middleClickMouse", at = @At("HEAD"))
    private void middleClickMouse1(CallbackInfo ci) {
        CPSCounter.registerClick(CPSCounter.MouseButton.MIDDLE);
    }

    @Inject(method = "rightClickMouse", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;rightClickDelayTimer:I", shift = At.Shift.AFTER))
    private void rightClickMouse1(final CallbackInfo callbackInfo) {
        CPSCounter.registerClick(CPSCounter.MouseButton.RIGHT);

        final FastPlace fastPlace = LiquidBounce.moduleManager.getModule(FastPlace.class);

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

    @Redirect(method = "runTick", at = @At(value = "FIELD", target = "Lnet/minecraft/client/settings/GameSettings;thirdPersonView:I", opcode = PUTFIELD))
    public void setThirdPersonView(GameSettings gameSettings, int value) {
        if (FreeLook.perspectiveToggled) {
            FreeLook.resetPerspective();
        } else {
            gameSettings.thirdPersonView = value;
        }
    }

    /**
     * @author CCBlueX
     */
    @ModifyConstant(method = "getLimitFramerate", constant = @Constant(intValue = 30))
    public int getLimitFramerate1(int constant) {
        return 60;
    }

    /**
     * @author
     */
    @Overwrite
    public void runTick() throws IOException {
        LiquidBounce.eventManager.callEvent(new TickEvent());
        if (this.rightClickDelayTimer > 0) {
            --this.rightClickDelayTimer;
        }

        FMLCommonHandler.instance().onPreClientTick();
        this.mcProfiler.startSection("gui");
        if (!this.isGamePaused) {
            this.ingameGUI.updateTick();
        }

        this.mcProfiler.endSection();
        this.entityRenderer.getMouseOver(1.0F);
        this.mcProfiler.startSection("gameMode");
        if (!this.isGamePaused && this.theWorld != null) {
            this.playerController.updateController();
        }

        this.mcProfiler.endStartSection("textures");
        if (!this.isGamePaused) {
            this.renderEngine.tick();
        }

        if (this.currentScreen == null && this.thePlayer != null) {
            if (this.thePlayer.getHealth() <= 0.0F) {
                this.displayGuiScreen((GuiScreen)null);
            } else if (this.thePlayer.isPlayerSleeping() && this.theWorld != null) {
                this.displayGuiScreen(new GuiSleepMP());
            }
        } else if (this.currentScreen != null && this.currentScreen instanceof GuiSleepMP && !this.thePlayer.isPlayerSleeping()) {
            this.displayGuiScreen((GuiScreen)null);
        }

        if (this.currentScreen != null) {
            this.leftClickCounter = 10000;
        }

        CrashReport crashreport2;
        CrashReportCategory crashreportcategory2;
        if (this.currentScreen != null) {
            try {
                this.currentScreen.handleInput();
            } catch (Throwable var7) {
                crashreport2 = CrashReport.makeCrashReport(var7, "Updating screen events");
                crashreportcategory2 = crashreport2.makeCategory("Affected screen");
                crashreportcategory2.addCrashSectionCallable("Screen name", new Callable<String>() {
                    public String call() throws Exception {
                        return Minecraft.getMinecraft().currentScreen.getClass().getCanonicalName();
                    }
                });
                throw new ReportedException(crashreport2);
            }

            if (this.currentScreen != null) {
                try {
                    this.currentScreen.updateScreen();
                } catch (Throwable var6) {
                    crashreport2 = CrashReport.makeCrashReport(var6, "Ticking screen");
                    crashreportcategory2 = crashreport2.makeCategory("Affected screen");
                    crashreportcategory2.addCrashSectionCallable("Screen name", new Callable<String>() {
                        public String call() throws Exception {
                            return Minecraft.getMinecraft().currentScreen.getClass().getCanonicalName();
                        }
                    });
                    throw new ReportedException(crashreport2);
                }
            }
        }

        if (this.currentScreen == null || this.currentScreen.allowUserInput) {
            this.mcProfiler.endStartSection("mouse");

            label516:
            while(true) {
                int k;
                do {
                    if (!Mouse.next()) {
                        if (this.leftClickCounter > 0) {
                            --this.leftClickCounter;
                        }

                        this.mcProfiler.endStartSection("keyboard");

                        for(; Keyboard.next(); FMLCommonHandler.instance().fireKeyInput()) {
                            k = Keyboard.getEventKey() == 0 ? Keyboard.getEventCharacter() + 256 : Keyboard.getEventKey();
                            KeyBinding.setKeyBindState(k, Keyboard.getEventKeyState());
                            if (Keyboard.getEventKeyState()) {
                                KeyBinding.onTick(k);
                            }

                            if (this.debugCrashKeyPressTime > 0L) {
                                if (getSystemTime() - this.debugCrashKeyPressTime >= 6000L) {
                                    throw new ReportedException(new CrashReport("Manually triggered debug crash", new Throwable()));
                                }

                                if (!Keyboard.isKeyDown(46) || !Keyboard.isKeyDown(61)) {
                                    this.debugCrashKeyPressTime = -1L;
                                }
                            } else if (Keyboard.isKeyDown(46) && Keyboard.isKeyDown(61)) {
                                this.debugCrashKeyPressTime = getSystemTime();
                            }

                            this.dispatchKeypresses();

                            if(Keyboard.getEventKeyState() && currentScreen == null)
                                LiquidBounce.eventManager.callEvent(new KeyEvent(Keyboard.getEventKey() == 0 ? Keyboard.getEventCharacter() + 256 : Keyboard.getEventKey()));

                            if (Keyboard.getEventKeyState()) {
                                if (k == 62 && this.entityRenderer != null) {
                                    this.entityRenderer.switchUseShader();
                                }

                                if (this.currentScreen != null) {
                                    this.currentScreen.handleKeyboardInput();
                                } else {
                                    if (k == 1) {
                                        this.displayInGameMenu();
                                    }

                                    if (k == 32 && Keyboard.isKeyDown(61) && this.ingameGUI != null) {
                                        this.ingameGUI.getChatGUI().clearChatMessages();
                                    }

                                    if (k == 31 && Keyboard.isKeyDown(61)) {
                                        this.refreshResources();
                                    }

                                    if (k == 17 && Keyboard.isKeyDown(61)) {
                                    }

                                    if (k == 18 && Keyboard.isKeyDown(61)) {
                                    }

                                    if (k == 47 && Keyboard.isKeyDown(61)) {
                                    }

                                    if (k == 38 && Keyboard.isKeyDown(61)) {
                                    }

                                    if (k == 22 && Keyboard.isKeyDown(61)) {
                                    }

                                    if (k == 20 && Keyboard.isKeyDown(61)) {
                                        this.refreshResources();
                                    }

                                    if (k == 33 && Keyboard.isKeyDown(61)) {
                                        this.gameSettings.setOptionValue(GameSettings.Options.RENDER_DISTANCE, GuiScreen.isShiftKeyDown() ? -1 : 1);
                                    }

                                    if (k == 30 && Keyboard.isKeyDown(61)) {
                                        this.renderGlobal.loadRenderers();
                                    }

                                    if (k == 35 && Keyboard.isKeyDown(61)) {
                                        this.gameSettings.advancedItemTooltips = !this.gameSettings.advancedItemTooltips;
                                        this.gameSettings.saveOptions();
                                    }

                                    if (k == 48 && Keyboard.isKeyDown(61)) {
                                        this.renderManager.setDebugBoundingBox(!this.renderManager.isDebugBoundingBox());
                                    }

                                    if (k == 25 && Keyboard.isKeyDown(61)) {
                                        this.gameSettings.pauseOnLostFocus = !this.gameSettings.pauseOnLostFocus;
                                        this.gameSettings.saveOptions();
                                    }

                                    if (k == 59) {
                                        this.gameSettings.hideGUI = !this.gameSettings.hideGUI;
                                    }

                                    if (k == 61) {
                                        this.gameSettings.showDebugInfo = !this.gameSettings.showDebugInfo;
                                        this.gameSettings.showDebugProfilerChart = GuiScreen.isShiftKeyDown();
                                        this.gameSettings.showLagometer = GuiScreen.isAltKeyDown();
                                    }

                                    if (this.gameSettings.keyBindTogglePerspective.isPressed()) {
                                        ++this.gameSettings.thirdPersonView;
                                        if (this.gameSettings.thirdPersonView > 2) {
                                            this.gameSettings.thirdPersonView = 0;
                                        }

                                        if (this.gameSettings.thirdPersonView == 0) {
                                            this.entityRenderer.loadEntityShader(this.getRenderViewEntity());
                                        } else if (this.gameSettings.thirdPersonView == 1) {
                                            this.entityRenderer.loadEntityShader((Entity)null);
                                        }

                                        this.renderGlobal.setDisplayListEntitiesDirty();
                                    }

                                    if (this.gameSettings.keyBindSmoothCamera.isPressed()) {
                                        this.gameSettings.smoothCamera = !this.gameSettings.smoothCamera;
                                    }
                                }

                                if (this.gameSettings.showDebugInfo && this.gameSettings.showDebugProfilerChart) {
                                    if (k == 11) {
                                        this.updateDebugProfilerName(0);
                                    }

                                    for(int j1 = 0; j1 < 9; ++j1) {
                                        if (k == 2 + j1) {
                                            this.updateDebugProfilerName(j1 + 1);
                                        }
                                    }
                                }
                            }
                        }

                        for(k = 0; k < 9; ++k) {
                            if (this.gameSettings.keyBindsHotbar[k].isPressed()) {
                                if (this.thePlayer.isSpectator()) {
                                    this.ingameGUI.getSpectatorGui().func_175260_a(k);
                                } else {
                                    this.thePlayer.inventory.currentItem = k;
                                }
                            }
                        }

                        boolean flag = this.gameSettings.chatVisibility != EntityPlayer.EnumChatVisibility.HIDDEN;

                        while(this.gameSettings.keyBindInventory.isPressed()) {
                            if (this.playerController.isRidingHorse()) {
                                this.thePlayer.sendHorseInventory();
                            } else {
                                this.getNetHandler().addToSendQueue(new C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT));
                                this.displayGuiScreen(new GuiInventory(this.thePlayer));
                            }
                        }

                        while(this.gameSettings.keyBindDrop.isPressed()) {
                            if (!this.thePlayer.isSpectator()) {
                                this.thePlayer.dropOneItem(GuiScreen.isCtrlKeyDown());
                            }
                        }

                        while(this.gameSettings.keyBindChat.isPressed() && flag) {
                            this.displayGuiScreen(new GuiChat());
                        }

                        if (this.currentScreen == null && this.gameSettings.keyBindCommand.isPressed() && flag) {
                            this.displayGuiScreen(new GuiChat("/"));
                        }

                        ClickUpdateEvent.INSTANCE.reInit();
                        LiquidBounce.eventManager.callEvent(ClickUpdateEvent.INSTANCE);

                        if (!ClickUpdateEvent.INSTANCE.isCancelled()) {
                            if (this.thePlayer.isUsingItem())
                            {
                                if (!this.gameSettings.keyBindUseItem.isKeyDown())
                                {
                                    this.playerController.onStoppedUsingItem(this.thePlayer);
                                }

                                while (this.gameSettings.keyBindAttack.isPressed())
                                {
                                    ;
                                }

                                while (this.gameSettings.keyBindUseItem.isPressed())
                                {
                                    ;
                                }

                                while (this.gameSettings.keyBindPickBlock.isPressed())
                                {
                                    ;
                                }
                            }
                            else
                            {
                                while (this.gameSettings.keyBindAttack.isPressed())
                                {
                                    this.clickMouse();
                                }

                                while (this.gameSettings.keyBindUseItem.isPressed())
                                {
                                    this.rightClickMouse();
                                }

                                while (this.gameSettings.keyBindPickBlock.isPressed())
                                {
                                    this.middleClickMouse();
                                }
                            }

                            if (this.gameSettings.keyBindUseItem.isKeyDown() && this.rightClickDelayTimer == 0 && !this.thePlayer.isUsingItem())
                            {
                                this.rightClickMouse();
                            }
                        }

                        if (this.gameSettings.keyBindUseItem.isKeyDown() && this.rightClickDelayTimer == 0 && !this.thePlayer.isUsingItem()) {
                            this.rightClickMouse();
                        }

                        this.sendClickBlockToController(this.currentScreen == null && this.gameSettings.keyBindAttack.isKeyDown() && this.inGameHasFocus);
                        break label516;
                    }
                } while(ForgeHooksClient.postMouseEvent());

                k = Mouse.getEventButton();
                KeyBinding.setKeyBindState(k - 100, Mouse.getEventButtonState());
                if (Mouse.getEventButtonState()) {
                    if (this.thePlayer.isSpectator() && k == 2) {
                        this.ingameGUI.getSpectatorGui().func_175261_b();
                    } else {
                        KeyBinding.onTick(k - 100);
                    }
                }

                long i1 = getSystemTime() - this.systemTime;
                if (i1 <= 200L) {
                    int j = Mouse.getEventDWheel();
                    if (j != 0) {
                        if (this.thePlayer.isSpectator()) {
                            j = j < 0 ? -1 : 1;
                            if (this.ingameGUI.getSpectatorGui().func_175262_a()) {
                                this.ingameGUI.getSpectatorGui().func_175259_b(-j);
                            } else {
                                float f = MathHelper.clamp_float(this.thePlayer.capabilities.getFlySpeed() + (float)j * 0.005F, 0.0F, 0.2F);
                                this.thePlayer.capabilities.setFlySpeed(f);
                            }
                        } else {
                            this.thePlayer.inventory.changeCurrentItem(j);
                        }
                    }

                    if (this.currentScreen == null) {
                        if (!this.inGameHasFocus && Mouse.getEventButtonState()) {
                            this.setIngameFocus();
                        }
                    } else if (this.currentScreen != null) {
                        this.currentScreen.handleMouseInput();
                    }
                }

                FMLCommonHandler.instance().fireMouseInput();
            }
        }

        if (this.theWorld != null) {
            if (this.thePlayer != null) {
                ++this.joinPlayerCounter;
                if (this.joinPlayerCounter == 30) {
                    this.joinPlayerCounter = 0;
                    this.theWorld.joinEntityInSurroundings(this.thePlayer);
                }
            }

            this.mcProfiler.endStartSection("gameRenderer");
            if (!this.isGamePaused) {
                this.entityRenderer.updateRenderer();
            }

            this.mcProfiler.endStartSection("levelRenderer");
            if (!this.isGamePaused) {
                this.renderGlobal.updateClouds();
            }

            this.mcProfiler.endStartSection("level");
            if (!this.isGamePaused) {
                if (this.theWorld.getLastLightningBolt() > 0) {
                    this.theWorld.setLastLightningBolt(this.theWorld.getLastLightningBolt() - 1);
                }

                this.theWorld.updateEntities();
            }
        } else if (this.entityRenderer.isShaderActive()) {
            this.entityRenderer.stopUseShader();
        }

        if (!this.isGamePaused) {
            this.mcMusicTicker.update();
            this.mcSoundHandler.update();
        }

        if (this.theWorld != null) {
            if (!this.isGamePaused) {
                this.theWorld.setAllowedSpawnTypes(this.theWorld.getDifficulty() != EnumDifficulty.PEACEFUL, true);

                try {
                    this.theWorld.tick();
                } catch (Throwable var8) {
                    crashreport2 = CrashReport.makeCrashReport(var8, "Exception in world tick");
                    if (this.theWorld == null) {
                        crashreportcategory2 = crashreport2.makeCategory("Affected level");
                        crashreportcategory2.addCrashSection("Problem", "Level is null!");
                    } else {
                        this.theWorld.addWorldInfoToCrashReport(crashreport2);
                    }

                    throw new ReportedException(crashreport2);
                }
            }

            this.mcProfiler.endStartSection("animateTick");
            if (!this.isGamePaused && this.theWorld != null) {
                this.theWorld.doVoidFogParticles(MathHelper.floor_double(this.thePlayer.posX), MathHelper.floor_double(this.thePlayer.posY), MathHelper.floor_double(this.thePlayer.posZ));
            }

            this.mcProfiler.endStartSection("particles");
            if (!this.isGamePaused) {
                this.effectRenderer.updateEffects();
            }
        } else if (this.myNetworkManager != null) {
            this.mcProfiler.endStartSection("pendingConnection");
            this.myNetworkManager.processReceivedPackets();
        }

        FMLCommonHandler.instance().onPostClientTick();
        this.mcProfiler.endSection();
        this.systemTime = getSystemTime();
    }
}