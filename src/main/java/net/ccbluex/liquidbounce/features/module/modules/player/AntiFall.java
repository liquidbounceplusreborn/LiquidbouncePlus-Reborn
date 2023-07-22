/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 *
 * This code belongs to WYSI-Foundation. Please give credits when using this in your repository.
 */
package net.ccbluex.liquidbounce.features.module.modules.player;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.event.*;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;
import net.ccbluex.liquidbounce.features.module.modules.movement.Fly;
import net.ccbluex.liquidbounce.features.module.modules.world.Scaffold;
import net.ccbluex.liquidbounce.utils.ClientUtils;
import net.ccbluex.liquidbounce.utils.MovementUtils;
import net.ccbluex.liquidbounce.utils.PacketUtils;
import net.ccbluex.liquidbounce.utils.block.BlockUtils;
import net.ccbluex.liquidbounce.utils.misc.NewFallingPlayer;
import net.ccbluex.liquidbounce.utils.misc.RandomUtils;
import net.ccbluex.liquidbounce.utils.timer.TimerUtils;
import net.ccbluex.liquidbounce.value.*;

import net.minecraft.block.BlockAir;
import net.minecraft.network.Packet;
import net.minecraft.util.BlockPos;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;

import java.util.ArrayList;
import java.util.LinkedList;

import static org.lwjgl.opengl.GL11.*;

@ModuleInfo(name = "AntiFall", spacedName = "Anti Fall", description = "Prevents you from falling into the void.", category = ModuleCategory.PLAYER)
public class AntiFall extends Module {

    public final ListValue voidDetectionAlgorithm = new ListValue("Detect-Method", new String[]{"Collision", "Predict"}, "Collision");
    public final ListValue setBackModeValue = new ListValue("SetBack-Mode", new String[]{"Teleport", "FlyFlag", "IllegalPacket", "IllegalTeleport", "StopMotion", "Position", "Edit", "SpoofBack", "Blink","HypixelTest"}, "Teleport");
    private final BoolValue resetMotionValue = new BoolValue("ResetMotion", false, () -> setBackModeValue.get().toLowerCase().contains("blink"));
    private final FloatValue startFallDistValue = new FloatValue("BlinkStartFallDistance", 2F, 0F, 5F, () -> setBackModeValue.get().toLowerCase().contains("blink"));
    public final IntegerValue maxFallDistSimulateValue = new IntegerValue("Predict-CheckFallDistance", 255, 0, 255, "m", () -> voidDetectionAlgorithm.get().equalsIgnoreCase("predict"));
    public final IntegerValue maxFindRangeValue = new IntegerValue("Predict-MaxFindRange", 60, 0, 255, "m", () -> voidDetectionAlgorithm.get().equalsIgnoreCase("predict"));
    public final IntegerValue illegalDupeValue = new IntegerValue("Illegal-Dupe", 1, 1, 5, "x", () -> setBackModeValue.get().toLowerCase().contains("illegal"));
    public final FloatValue setBackFallDistValue = new FloatValue("Max-FallDistance", 5F, 5F, 20F, "m");
    public final BoolValue resetFallDistanceValue = new BoolValue("Reset-FallDistance", true);
    public final BoolValue renderTraceValue = new BoolValue("Render-Trace", true);
    public final BoolValue scaffoldValue = new BoolValue("AutoScaffold", true);
    public final BoolValue noFlyValue = new BoolValue("NoFly", true);

    private BlockPos detectedLocation = BlockPos.ORIGIN;
    private boolean blink = false;
    private double posX = 0.0;
    private double posY = 0.0;
    private double posZ = 0.0;
    private double motionX = 0.0;
    private double motionY = 0.0;
    private double motionZ = 0.0;
    private boolean canBlink = false;
    private double lastX = 0;
    private double lastY = 0;
    private double lastZ = 0;
    public double[] lastGroundPos = new double[3];
    private double lastFound = 0;
    private boolean shouldRender, shouldStopMotion, shouldEdit = false;

    private final LinkedList<double[]> positions = new LinkedList<>();

    private final ArrayList<C03PacketPlayer> packetCache = new ArrayList<>();

    public static TimerUtils timer = new TimerUtils();

    public static ArrayList<C03PacketPlayer> packets = new ArrayList<>();

    public static boolean isInVoid() {
        for (int i = 0; i <= 128; i++) {
            if (MovementUtils.isOnGround(i)) {
                return false;
            }
        }
        return true;
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (noFlyValue.get() && LiquidBounce.moduleManager.getModule(Fly.class).getState())
            return;

        detectedLocation = null;

        if (setBackModeValue.get().toLowerCase().equalsIgnoreCase("blink")){

            if (!blink) {
                BlockPos collide = new NewFallingPlayer(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, 0.0, 0.0, 0.0, 0F, 0F, 0F, 0F).findCollision(60);
                if (canBlink && (collide == null || (mc.thePlayer.posY - collide.getY())> startFallDistValue.get())) {
                    posX = mc.thePlayer.posX;
                    posY = mc.thePlayer.posY;
                    posZ = mc.thePlayer.posZ;
                    motionX = mc.thePlayer.motionX;
                    motionY = mc.thePlayer.motionY;
                    motionZ = mc.thePlayer.motionZ;

                    packetCache.clear();
                    blink = true;
                }

                if (mc.thePlayer.onGround) {
                    canBlink = true;
                }
            } else {
                if (mc.thePlayer.fallDistance> setBackFallDistValue.get()) {
                    mc.thePlayer.setPositionAndUpdate(posX, posY, posZ);
                    if (resetMotionValue.get()) {
                        mc.thePlayer.motionX = 0.0;
                        mc.thePlayer.motionY = 0.0;
                        mc.thePlayer.motionZ = 0.0;
                        mc.thePlayer.jumpMovementFactor = 0.00f;
                    } else {
                        mc.thePlayer.motionX = motionX;
                        mc.thePlayer.motionY = motionY;
                        mc.thePlayer.motionZ = motionZ;
                        mc.thePlayer.jumpMovementFactor = 0.00f;
                    }

                    if (scaffoldValue.get()) {
                        LiquidBounce.moduleManager.getModule(Scaffold.class).setState(true);
                    }

                    packetCache.clear();
                    blink = false;
                    canBlink = false;
                } else if (mc.thePlayer.onGround) {
                    blink = false;

                    for (final C03PacketPlayer packet : packetCache) {
                        mc.getNetHandler().addToSendQueue(packet);
                    }
                }
            }
        }

        if (voidDetectionAlgorithm.get().equalsIgnoreCase("collision")) {
            if (mc.thePlayer.onGround && !(BlockUtils.getBlock(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.0, mc.thePlayer.posZ)) instanceof BlockAir)) {
                lastX = mc.thePlayer.prevPosX;
                lastY = mc.thePlayer.prevPosY;
                lastZ = mc.thePlayer.prevPosZ;
            }

            shouldRender = renderTraceValue.get() && !MovementUtils.isBlockUnder();

            shouldStopMotion = false;
            shouldEdit = false;
            if (!MovementUtils.isBlockUnder()) {
                if (mc.thePlayer.fallDistance >= setBackFallDistValue.get()) {
                    shouldStopMotion = true;
                    switch (setBackModeValue.get()) {
                        case "IllegalTeleport":
                            mc.thePlayer.setPositionAndUpdate(lastX, lastY, lastZ);
                        case "IllegalPacket":
                            for (int i = 0; i < illegalDupeValue.get(); i++) PacketUtils.sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY - 1E+159, mc.thePlayer.posZ, false));
                            break;
                        case "Teleport":
                            mc.thePlayer.setPositionAndUpdate(lastX, lastY, lastZ);
                            break;
                        case "FlyFlag":
                            mc.thePlayer.motionY = 0F;
                            break;
                        case "StopMotion":
                            float oldFallDist = mc.thePlayer.fallDistance;
                            mc.thePlayer.motionY = 0F;
                            mc.thePlayer.fallDistance = oldFallDist;
                            break;
                        case "Position":
                            PacketUtils.sendPacketNoEvent(new C03PacketPlayer.C06PacketPlayerPosLook(mc.thePlayer.posX, mc.thePlayer.posY + RandomUtils.nextDouble(6D, 10D), mc.thePlayer.posZ, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, false));
                            break;
                        case "Edit":
                        case "SpoofBack":
                            shouldEdit = true;
                            break;
                    }
                    if (resetFallDistanceValue.get() && !setBackModeValue.get().equalsIgnoreCase("StopMotion")) mc.thePlayer.fallDistance = 0;

                    if (scaffoldValue.get() && !LiquidBounce.moduleManager.getModule(Scaffold.class).getState())
                        LiquidBounce.moduleManager.getModule(Scaffold.class).setState(true);

                }
            }
        } else {
            if (mc.thePlayer.onGround && !(BlockUtils.getBlock(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.0, mc.thePlayer.posZ)) instanceof BlockAir)) {
                lastX = mc.thePlayer.prevPosX;
                lastY = mc.thePlayer.prevPosY;
                lastZ = mc.thePlayer.prevPosZ;
            }

            shouldStopMotion = false;
            shouldEdit = false;
            shouldRender = false;

            if (!mc.thePlayer.onGround && !mc.thePlayer.isOnLadder() && !mc.thePlayer.isInWater()) {
                NewFallingPlayer NewFallingPlayer = new NewFallingPlayer(mc.thePlayer);

                try {
                    detectedLocation = NewFallingPlayer.findCollision(maxFindRangeValue.get());
                } catch (Exception e) {
                    // do nothing. i hate errors
                }

                if (detectedLocation != null && Math.abs(mc.thePlayer.posY - detectedLocation.getY()) +
                        mc.thePlayer.fallDistance <= maxFallDistSimulateValue.get()) {
                    lastFound = mc.thePlayer.fallDistance;
                }

                shouldRender = renderTraceValue.get() && detectedLocation == null;

                if (mc.thePlayer.fallDistance - lastFound > setBackFallDistValue.get()) {
                    shouldStopMotion = true;
                    switch (setBackModeValue.get()) {
                        case "IllegalTeleport":
                            mc.thePlayer.setPositionAndUpdate(lastX, lastY, lastZ);
                        case "IllegalPacket":
                            for (int i = 0; i < illegalDupeValue.get(); i++) PacketUtils.sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY - 1E+159, mc.thePlayer.posZ, false));
                            break;
                        case "Teleport":
                            mc.thePlayer.setPositionAndUpdate(lastX, lastY, lastZ);
                            break;
                        case "FlyFlag":
                            mc.thePlayer.motionY = 0F;
                            break;
                        case "StopMotion":
                            float oldFallDist = mc.thePlayer.fallDistance;
                            mc.thePlayer.motionY = 0F;
                            mc.thePlayer.fallDistance = oldFallDist;
                            break;
                        case "Position":
                            PacketUtils.sendPacketNoEvent(new C03PacketPlayer.C06PacketPlayerPosLook(mc.thePlayer.posX, mc.thePlayer.posY + RandomUtils.nextDouble(6D, 10D), mc.thePlayer.posZ, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, false));
                            break;
                        case "Edit":
                        case "SpoofBack":
                            shouldEdit = true;
                            break;
                    }
                    if (resetFallDistanceValue.get() && !setBackModeValue.get().equalsIgnoreCase("StopMotion")) mc.thePlayer.fallDistance = 0;

                    if (scaffoldValue.get() && !LiquidBounce.moduleManager.getModule(Scaffold.class).getState())
                        LiquidBounce.moduleManager.getModule(Scaffold.class).setState(true);
                }
            }
        }

        if (shouldRender) synchronized (positions) {
            positions.add(new double[]{mc.thePlayer.posX, mc.thePlayer.getEntityBoundingBox().minY, mc.thePlayer.posZ});
        }
        else synchronized (positions) {
            positions.clear();
        }
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        if (noFlyValue.get() && LiquidBounce.moduleManager.getModule(Fly.class).getState())
            return;

        if (setBackModeValue.get().equalsIgnoreCase("Blink") && blink && event.getPacket() instanceof C03PacketPlayer) {
            final C03PacketPlayer packet = (C03PacketPlayer) event.getPacket();
            packetCache.add(packet);
            event.cancelEvent();
        }

        if (setBackModeValue.get().equalsIgnoreCase("StopMotion") && event.getPacket() instanceof S08PacketPlayerPosLook)
            mc.thePlayer.fallDistance = 0;

        if (setBackModeValue.get().equalsIgnoreCase("Edit") && shouldEdit && event.getPacket() instanceof C03PacketPlayer) {
            final C03PacketPlayer packetPlayer = (C03PacketPlayer) event.getPacket();
            packetPlayer.y += 100D;
            shouldEdit = false;
        }

        if (setBackModeValue.get().equalsIgnoreCase("SpoofBack") && shouldEdit && event.getPacket() instanceof C03PacketPlayer) {
            final C03PacketPlayer packetPlayer = (C03PacketPlayer) event.getPacket();
            packetPlayer.x = lastX;
            packetPlayer.y = lastY;
            packetPlayer.z = lastZ;
            packetPlayer.setMoving(false);
            shouldEdit = false;
        }
        if (setBackModeValue.get().equalsIgnoreCase("HypixelTest")) {
            if (event.getPacket() instanceof C03PacketPlayer) {
                C03PacketPlayer packet = ((C03PacketPlayer) event.getPacket());
                if (isInVoid()) {
                    event.cancelEvent();
                    packets.add(packet);
                    if (mc.thePlayer.fallDistance >= setBackFallDistValue.get()) {
                        PacketUtils.sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(lastGroundPos[0], lastGroundPos[1] - 1, lastGroundPos[2], true));
                    }
                } else {
                    lastGroundPos[0] = mc.thePlayer.posX;
                    lastGroundPos[1] = mc.thePlayer.posY;
                    lastGroundPos[2] = mc.thePlayer.posZ;

                    if (!packets.isEmpty()) {
                        ClientUtils.displayChatMessage("Release Packets - " + packets.size());
                        for (Packet p : packets)
                            PacketUtils.sendPacketNoEvent(p);
                        packets.clear();
                    }
                    timer.reset();
                }
            }
        }
    }

    @EventTarget
    public void onMove(MoveEvent event) {
        if (noFlyValue.get() && LiquidBounce.moduleManager.getModule(Fly.class).getState())
            return;

        if (setBackModeValue.get().equalsIgnoreCase("StopMotion") && shouldStopMotion) {
            event.zero();
        }
    }

    @EventTarget
    public void onRender3D(Render3DEvent event) {
        if (noFlyValue.get() && LiquidBounce.moduleManager.getModule(Fly.class).getState())
            return;

        if (shouldRender) synchronized (positions) {
            glPushMatrix();

            glDisable(GL_TEXTURE_2D);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            glEnable(GL_LINE_SMOOTH);
            glEnable(GL_BLEND);
            glDisable(GL_DEPTH_TEST);
            mc.entityRenderer.disableLightmap();
            glLineWidth(1F);
            glBegin(GL_LINE_STRIP);
            glColor4f(1F, 1F, 0.1F, 1F);
            final double renderPosX = mc.getRenderManager().viewerPosX;
            final double renderPosY = mc.getRenderManager().viewerPosY;
            final double renderPosZ = mc.getRenderManager().viewerPosZ;

            for (final double[] pos : positions)
                glVertex3d(pos[0] - renderPosX, pos[1] - renderPosY, pos[2] - renderPosZ);

            glColor4d(1, 1, 1, 1);
            glEnd();
            glEnable(GL_DEPTH_TEST);
            glDisable(GL_LINE_SMOOTH);
            glDisable(GL_BLEND);
            glEnable(GL_TEXTURE_2D);
            glPopMatrix();
        }
    }

    @Override
    public void onDisable() {
        reset();
        super.onDisable();
    }

    @Override
    public void onEnable() {
        reset();
        super.onEnable();
    }

    private void reset() {
        canBlink = false;
        blink = false;
        detectedLocation = null;
        lastX = lastY = lastZ = lastFound = 0;
        shouldStopMotion = shouldRender = false;
        synchronized (positions) {
            positions.clear();
        }
    }
}
