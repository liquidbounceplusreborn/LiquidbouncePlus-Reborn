/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.utils;

import net.ccbluex.liquidbounce.event.EventTarget;
import net.ccbluex.liquidbounce.event.Listenable;
import net.ccbluex.liquidbounce.event.PacketEvent;
import net.ccbluex.liquidbounce.event.TickEvent;

import net.minecraft.network.Packet;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.network.play.server.S32PacketConfirmTransaction;

import net.ccbluex.liquidbounce.utils.timer.MSTimer;

import java.util.ArrayList;

public class PacketUtils extends MinecraftInstance implements Listenable {

    public static int inBound, outBound = 0;
    public static int avgInBound, avgOutBound = 0;

    public static ArrayList<Packet> packets = new ArrayList<>();

    private static MSTimer packetTimer = new MSTimer();
    private static MSTimer wdTimer = new MSTimer();

    private static int transCount = 0;
    private static int wdVL = 0;

    private static boolean isInventoryAction(short action) {
        return action > 0 && action < 100;
    }

    public static boolean isWatchdogActive() {
        return wdVL >= 8;
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        handlePacket(event.getPacket());
    }

    private static void handlePacket(Packet<?> packet) {
        if (packet.getClass().getSimpleName().startsWith("C")) outBound++;
        else if (packet.getClass().getSimpleName().startsWith("S")) inBound++;

        if (packet instanceof S32PacketConfirmTransaction) 
        {
            if (!isInventoryAction(((S32PacketConfirmTransaction) packet).getActionNumber())) 
                transCount++;
        }
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (packetTimer.hasTimePassed(1000L)) {
            avgInBound = inBound; avgOutBound = outBound;
            inBound = outBound = 0;
            packetTimer.reset();
        }
        if (mc.thePlayer == null || mc.theWorld == null) {
            //reset all checks
            wdVL = 0;
            transCount = 0;
            wdTimer.reset();
        } else if (wdTimer.hasTimePassed(100L)) { // watchdog active when the transaction poll rate reaches about 100ms/packet.
            wdVL += (transCount > 0) ? 1 : -1;
            transCount = 0;
            if (wdVL > 10) wdVL = 10;
            if (wdVL < 0) wdVL = 0;
            wdTimer.reset();
        }
    }

    /*
     * This code is from UnlegitMC/FDPClient. Please credit them when using this code in your repository.
     */
    public static void sendPacketNoEvent(Packet<INetHandlerPlayServer> packet) {
        packets.add(packet);
        mc.getNetHandler().addToSendQueue(packet);
    }

    public static boolean handleSendPacket(Packet<?> packet) {
        if (packets.contains(packet)) {
            packets.remove(packet);
            handlePacket(packet); // make sure not to skip silent packets.
            return true;
        }
        return false;
    }

    /**
     * @return wow
     */
    @Override
    public boolean handleEvents() {
        return true;
    }
    
}
