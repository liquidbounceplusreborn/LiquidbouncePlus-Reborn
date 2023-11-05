/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.player;

import net.ccbluex.liquidbounce.event.EventTarget;
import net.ccbluex.liquidbounce.event.UpdateEvent;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;
import net.ccbluex.liquidbounce.utils.timer.TimerUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;

@ModuleInfo(name = "VirtueAutoArmor", spacedName = "Virtue Auto Armor", description = "Automatically equips the best armor in your inventory.", category = ModuleCategory.PLAYER)
public class AutoArmor2 extends Module {
    private int[] bestArmor = new int[4];
    private final TimerUtils timer = new TimerUtils();

    @EventTarget
    public void onUpdate(final UpdateEvent event) {
        if (timer.hasReached(250)) {
            for (int i = 0; i < this.bestArmor.length; i++) {
                this.bestArmor[i] = -1;
            }
            for (int i = 0; i < 36; i++) {
                final ItemStack itemstack = this.mc.thePlayer.inventory.getStackInSlot(i);
                if (itemstack != null && itemstack.getItem() instanceof ItemArmor) {
                    final ItemArmor armor = (ItemArmor)itemstack.getItem();
                    if (armor.damageReduceAmount > this.bestArmor[3 - armor.armorType]) {
                        this.bestArmor[3 - armor.armorType] = i;
                    }
                }
            }
            for (int i = 0; i < 4; i++) {
                final ItemStack itemstack = this.mc.thePlayer.inventory.armorItemInSlot(i);
                ItemArmor currentArmor = null;
                if (itemstack != null && itemstack.getItem() instanceof ItemArmor) {
                    currentArmor = (ItemArmor)itemstack.getItem();
                }
                final ItemArmor bestArmor;
                try {
                    bestArmor = (ItemArmor)this.mc.thePlayer.inventory.getStackInSlot(this.bestArmor[i]).getItem();
                } catch (Exception e) {
                    continue;
                }
                if (bestArmor == null || (currentArmor != null && bestArmor.damageReduceAmount <= currentArmor.damageReduceAmount)) {
                    continue;
                }
                if (this.mc.thePlayer.inventory.getFirstEmptyStack() == -1 && currentArmor == null) {
                    continue;
                }
                this.mc.playerController.windowClick(0, 8 - i, 0, 1, this.mc.thePlayer);
                this.mc.playerController.windowClick(0, this.bestArmor[i] < 9 ? 36 + this.bestArmor[i] : this.bestArmor[i], 0, 1, Minecraft.getMinecraft().thePlayer);
            }
            this.timer.reset();
        }
    }
}