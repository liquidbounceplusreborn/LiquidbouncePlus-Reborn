package net.ccbluex.liquidbounce.features.module.modules.world;

import net.ccbluex.liquidbounce.event.EventTarget;
import net.ccbluex.liquidbounce.event.MotionEvent;
import net.ccbluex.liquidbounce.event.UpdateEvent;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;

@ModuleInfo(name = "AntiObbyTrap", description = "AntiObbyTrap", category = ModuleCategory.PLAYER)
public class AntiObbyTrap extends Module {
    @EventTarget
    public void onUpdate(UpdateEvent event) {
        BlockPos sand = new BlockPos(new Vec3(mc.thePlayer.posX, mc.thePlayer.posY + 3.0D, mc.thePlayer.posZ));
        Block sandblock = mc.theWorld.getBlockState(sand).getBlock();
        BlockPos forge = new BlockPos(new Vec3(mc.thePlayer.posX, mc.thePlayer.posY + 2.0D, mc.thePlayer.posZ));
        Block forgeblock = mc.theWorld.getBlockState(forge).getBlock();
        BlockPos obsidianpos = new BlockPos(new Vec3(mc.thePlayer.posX, mc.thePlayer.posY + 1.0D, mc.thePlayer.posZ));
        Block obsidianblock = mc.theWorld.getBlockState(obsidianpos).getBlock();

        if (obsidianblock == Block.getBlockById(49)) {
            updateTool(new BlockPos(new Vec3(mc.thePlayer.posX, mc.thePlayer.posY - 1, mc.thePlayer.posZ)));
        }
        if (forgeblock == Block.getBlockById(61)) {
            updateTool(new BlockPos(new Vec3(mc.thePlayer.posX, mc.thePlayer.posY - 1, mc.thePlayer.posZ)));
        }
        if (sandblock == Block.getBlockById(12) || sandblock == Block.getBlockById(13)) {
            updateTool(new BlockPos(new Vec3(mc.thePlayer.posX, mc.thePlayer.posY - 1, mc.thePlayer.posZ)));
        }
    }

    public static void updateTool(BlockPos pos) {
        Block block = mc.theWorld.getBlockState(pos).getBlock();
        float strength = 1.0F;
        int bestItemIndex = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack itemStack = mc.thePlayer.inventory.mainInventory[i];
            if (itemStack == null) {
                continue;
            }
            if ((itemStack.getStrVsBlock(block) > strength)) {
                strength = itemStack.getStrVsBlock(block);
                bestItemIndex = i;
            }
        }
        if (bestItemIndex != -1) {
            mc.thePlayer.inventory.currentItem = bestItemIndex;
        }
    }
}
