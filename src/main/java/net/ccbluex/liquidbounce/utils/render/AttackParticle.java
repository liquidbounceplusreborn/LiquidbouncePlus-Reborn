package net.ccbluex.liquidbounce.utils.render;

import net.ccbluex.liquidbounce.utils.timer.TimerUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockLiquid;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

public class AttackParticle {
    private final TimerUtils removeTimer = new TimerUtils();
    public final Vec3 position;
    private Vec3 delta;

    public AttackParticle(Vec3 position) {
        this.position = position;
        this.delta = new Vec3((Math.random() * 2.5 - 1.25) * 0.04 + 0, (Math.random() * 0.5 - 0.2) * 0.04 + 0, (Math.random() * 2.5 - 1.25) * 0.04 + 0);
        this.removeTimer.reset();
    }

    public void update() {
        Block block3;
        Block block2;
        Block block1 = getBlock(position.xCoord, position.yCoord, position.zCoord + delta.zCoord);
        if (!(block1 instanceof BlockAir || block1 instanceof BlockBush || block1 instanceof BlockLiquid)) {
            delta.zCoord *= -0.8;
        }
        if (!((block2 = getBlock(position.xCoord, position.yCoord + delta.yCoord, position.zCoord)) instanceof BlockAir || block2 instanceof BlockBush || block2 instanceof BlockLiquid)) {
            delta.xCoord *= (double)0.99f;
            delta.zCoord *= (double)0.99f;
            delta.yCoord *= -0.5;
        }
        if (!((block3 = getBlock(position.xCoord + delta.xCoord, position.yCoord, position.zCoord)) instanceof BlockAir || block3 instanceof BlockBush || block3 instanceof BlockLiquid)) {
            delta.xCoord *= -0.8;
        }
        updateWithoutPhysics();
    }

    public void updateWithoutPhysics() {
        position.xCoord += delta.xCoord;
        position.yCoord += delta.yCoord;
        position.zCoord += delta.zCoord;
        delta.xCoord *= (double)0.998f;
        delta.yCoord -= 3.1E-5;
        delta.zCoord *= (double)0.998f;
    }

    public static Block getBlock(double offsetX, double offsetY, double offsetZ) {
        return Minecraft.getMinecraft().theWorld.getBlockState(new BlockPos(offsetX, offsetY, offsetZ)).getBlock();
    }
}