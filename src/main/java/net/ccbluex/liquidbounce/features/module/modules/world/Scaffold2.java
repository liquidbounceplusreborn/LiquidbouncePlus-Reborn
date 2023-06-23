package net.ccbluex.liquidbounce.features.module.modules.world;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.event.*;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed;
import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.ccbluex.liquidbounce.utils.*;
import net.ccbluex.liquidbounce.utils.render.BlurUtils;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.ccbluex.liquidbounce.utils.timer.TimerUtils;
import net.ccbluex.liquidbounce.value.BoolValue;
import net.ccbluex.liquidbounce.value.FloatValue;
import net.ccbluex.liquidbounce.value.IntegerValue;
import net.ccbluex.liquidbounce.value.ListValue;
import net.minecraft.block.*;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.stats.StatList;
import net.minecraft.util.*;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import java.awt.*;

@ModuleInfo(name = "Scaffold2", description = "Automatically places blocks beneath your feet.", category = ModuleCategory.WORLD, keyBind = Keyboard.KEY_I)
public class Scaffold2 extends Module {


    public final ListValue rotationModeValue = new ListValue("RotationMode", new String[]{"Novoline","Backward"}, "Novoline");

    private final ListValue autoBlockMode = new ListValue("AutoBlock", new String[]{"Spoof", "Switch", "Off"}, "Spoof");

    public final ListValue sprintModeValue = new ListValue("SprintMode", new String[]{
            "Vanilla", "NoPacket", "None"
    }, "Vanilla");
    private final BoolValue swingValue = new BoolValue("Swing", false);

    private final BoolValue safeWalkValue = new BoolValue("SafeWalk", true);

    private final BoolValue eagleValue = new BoolValue("Eagle", false);

    public final FloatValue speedModifierValue = new FloatValue("SpeedModifier", 1F, 0, 2F, "x");
    public final FloatValue xzMultiplier = new FloatValue("XZ-Multiplier", 1F, 0F, 4F, "x");

    private final ListValue placeConditionValue = new ListValue("Place-Condition", new String[]{"Air", "FallDown", "NegativeMotion", "Always"}, "Always");
    private final BoolValue towerEnabled = new BoolValue("Tower", false);
    private final ListValue towerModeValue = new ListValue("TowerMode", new String[]{
            "Jump", "Motion","ConstantMotion", "MotionTP"
    }, "Motion", () -> towerEnabled.get());

    private final FloatValue constantMotionValue = new FloatValue("ConstantMotion", 0.42F, 0.1F, 1F, () -> towerEnabled.get() && towerModeValue.get().equalsIgnoreCase("ConstantMotion"));
    private final FloatValue constantMotionJumpGroundValue = new FloatValue("ConstantMotionJumpGround", 0.79F, 0.76F, 1F, () -> towerEnabled.get() && towerModeValue.get().equalsIgnoreCase("ConstantMotion"));


    private final FloatValue jumpMotionValue = new FloatValue("JumpMotion", 0.42F, 0.3681289F, 0.79F, () -> towerEnabled.get() && towerModeValue.get().equalsIgnoreCase("Jump"));

    private final BoolValue noMoveOnlyValue = new BoolValue("NoMove", true, towerEnabled::get);

    public final ListValue counterDisplayValue = new ListValue("Counter", new String[]{"Off", "Simple", "Advanced", "Sigma", "Novoline","Exhibition"}, "Simple");

    private final BoolValue blurValue = new BoolValue("Blur-Advanced", false, () -> counterDisplayValue.get().equalsIgnoreCase("advanced"));
    private final FloatValue blurStrength = new FloatValue("Blur-Strength", 1F, 0F, 30F, "x", () -> counterDisplayValue.get().equalsIgnoreCase("advanced"));


    private BlockData blockData;

    private double jumpGround = 0;
    private float alpha;

    private int slot;

    private final TimerUtils timer = new TimerUtils();

    public void onEnable() {
        slot = mc.thePlayer.inventory.currentItem;
    }


    public void onDisable() {
        mc.timer.timerSpeed = 1;
        if (eagleValue.get()) {
            mc.gameSettings.keyBindSneak.pressed = Keyboard.isKeyDown(mc.gameSettings.keyBindSneak.getKeyCode());
        }
        if (slot != mc.thePlayer.inventory.currentItem && autoBlockMode.get().equalsIgnoreCase("spoof"))
            mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
    }

    private boolean shouldPlace() {
        boolean placeWhenAir = placeConditionValue.get().equalsIgnoreCase("air");
        boolean placeWhenFall = placeConditionValue.get().equalsIgnoreCase("falldown");
        boolean placeWhenNegativeMotion = placeConditionValue.get().equalsIgnoreCase("negativemotion");
        boolean alwaysPlace = placeConditionValue.get().equalsIgnoreCase("always");
        return alwaysPlace || (placeWhenAir && !mc.thePlayer.onGround) || (placeWhenFall && mc.thePlayer.fallDistance > 0) || (placeWhenNegativeMotion && mc.thePlayer.motionY < 0);
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {

        mc.thePlayer.motionX *= xzMultiplier.get();
        mc.thePlayer.motionZ *= xzMultiplier.get();

            int blockSlot = -1;
        ItemStack itemStack = mc.thePlayer.getHeldItem();

            if (mc.thePlayer.getHeldItem() == null || !(mc.thePlayer.getHeldItem().getItem() instanceof ItemBlock)) {
                if (autoBlockMode.get().equalsIgnoreCase("Off"))
                    return;

                blockSlot = InventoryUtils.findAutoBlockBlock();

                if (blockSlot == -1)
                    return;

                if (autoBlockMode.get().equalsIgnoreCase("Spoof")) {
                    mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(blockSlot - 36));
                    itemStack = mc.thePlayer.inventoryContainer.getSlot(blockSlot).getStack();
                } else {
                    mc.thePlayer.inventory.currentItem = blockSlot - 36;
                    mc.playerController.updateController();
                }
            }

            // blacklist check
            if (itemStack != null && itemStack.getItem() != null && itemStack.getItem() instanceof ItemBlock) {
                Block block = ((ItemBlock) itemStack.getItem()).getBlock();
                if (InventoryUtils.BLOCK_BLACKLIST.contains(block) || !block.isFullCube() || itemStack.stackSize <= 0)
                    return;
            }
            this.blockData = ((this
                    .getBlockData(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.0, mc.thePlayer.posZ)) == null)
                    ? this.getBlockData(
                    new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.0, mc.thePlayer.posZ).down(1))
                    : this.getBlockData(
                    new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.0, mc.thePlayer.posZ)));


        switch (sprintModeValue.get()) {
            case "Vanilla":
            case "NoPacket": {
                mc.thePlayer.setSprinting(true);
                break;
            }
            case "None": {
                mc.thePlayer.setSprinting(false);
                break;
            }
        }
        if (shouldPlace()) {

            if (towerMoving()) {
                move(event);
            }

            if (blockData != null) {
                if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.getCurrentEquippedItem(),
                        blockData.position, blockData.facing,
                        getVec3(blockData.position, blockData.facing))) {
                    if (mc.thePlayer.inventory.getStackInSlot(mc.thePlayer.inventory.currentItem).getItem() != null
                            && mc.thePlayer.inventory.getStackInSlot(mc.thePlayer.inventory.currentItem)
                            .getItem() instanceof ItemBlock
                            && !mc.isSingleplayer()) {
                        mc.thePlayer.inventory.getStackInSlot(mc.thePlayer.inventory.currentItem).getItem();
                    }
                    if (swingValue.getValue()) {
                        mc.thePlayer.swingItem();
                    } else {
                        mc.getNetHandler().addToSendQueue(new C0APacketAnimation());
                    }
                    if (mc.thePlayer.onGround) {
                        final float modifier = speedModifierValue.get();

                        mc.thePlayer.motionX *= modifier;
                        mc.thePlayer.motionZ *= modifier;
                    }
                    if (eagleValue.get())
                        mc.gameSettings.keyBindSneak.pressed = PlayerUtils.isAirUnder(mc.thePlayer);
                }
                switch (rotationModeValue.get()) {
                    case "Novoline": {
                        if (blockData != null) {
                            EntityPig entity = new EntityPig(mc.theWorld);
                            entity.posX = blockData.position.getX() + 0.5;
                            entity.posY = blockData.position.getY() + 0.5;
                            entity.posZ = blockData.position.getZ() + 0.5;
                            Rotation rots = RotationUtils.getAngles(entity);
                            RotationUtils.setTargetRotation(rots);
                        }
                        break;
                    }
                    case "Backward": {
                        for (int i = 0; i < 2; i++) {
                            final Vec3 eyesPos = new Vec3(mc.thePlayer.posX, mc.thePlayer.getEntityBoundingBox().minY + mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ);
                            final double diffX = i == 0 ? 0 : blockData.hitVec.xCoord - eyesPos.xCoord;
                            final double diffY = blockData.hitVec.yCoord - eyesPos.yCoord;
                            final double diffZ = i == 1 ? 0 : blockData.hitVec.zCoord - eyesPos.zCoord;

                            final double diffXZ = MathHelper.sqrt_double(diffX * diffX + diffZ * diffZ);

                            Rotation rots = new Rotation(
                                    mc.thePlayer.rotationYaw + 180,
                                    MathHelper.wrapAngleTo180_float((float) -Math.toDegrees(Math.atan2(diffY, diffXZ)))
                            );
                            RotationUtils.setTargetRotation(rots);
                        }
                        break;
                    }
                }
            }
        }
    }

    private boolean towerMoving() {
        return towerEnabled.get() && Keyboard.isKeyDown(Keyboard.KEY_SPACE);
    }

    private void move(UpdateEvent event) {

        mc.thePlayer.cameraYaw = 0;
        mc.thePlayer.cameraPitch = 0;
        if (noMoveOnlyValue.get()) {
            mc.thePlayer.motionX = mc.thePlayer.motionZ = 0;
        }
        switch (towerModeValue.get().toLowerCase()) {
            case "jump":
                if (mc.thePlayer.onGround) {
                    fakeJump();
                    mc.thePlayer.motionY = jumpMotionValue.get();
                    timer.reset();
                }
                break;
            case "motion":
                if (mc.thePlayer.onGround) {
                    fakeJump();
                    mc.thePlayer.motionY = 0.42D;
                } else if (mc.thePlayer.motionY < 0.1D) mc.thePlayer.motionY = -0.3D;
                break;
            case "motiontp":
                if (mc.thePlayer.onGround) {
                    fakeJump();
                    mc.thePlayer.motionY = 0.42D;
                } else if (mc.thePlayer.motionY < 0.23D)
                    mc.thePlayer.setPosition(mc.thePlayer.posX, (int) mc.thePlayer.posY, mc.thePlayer.posZ);
                break;
            case "constantmotion":
                if (mc.thePlayer.onGround) {
                    fakeJump();
                    jumpGround = mc.thePlayer.posY;
                    mc.thePlayer.motionY = constantMotionValue.get();
                }

                if (mc.thePlayer.posY > jumpGround + constantMotionJumpGroundValue.get()) {
                    fakeJump();
                    mc.thePlayer.setPosition(mc.thePlayer.posX, (int) mc.thePlayer.posY, mc.thePlayer.posZ);
                    mc.thePlayer.motionY = constantMotionValue.get();
                    jumpGround = mc.thePlayer.posY;
                }
                break;
        }
    }

    public static double randomNumber(final double max, final double min) {
        return Math.random() * (max - min) + min;
    }

    public static Vec3 getVec3(final BlockPos pos, final EnumFacing facing) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;
        if (facing == EnumFacing.UP || facing == EnumFacing.DOWN) {
            x += randomNumber(0.3, -0.3);
            z += randomNumber(0.3, -0.3);
        } else {
            y += randomNumber(0.3, -0.3);
        }
        if (facing == EnumFacing.WEST || facing == EnumFacing.EAST) {
            z += randomNumber(0.3, -0.3);
        }
        if (facing == EnumFacing.SOUTH || facing == EnumFacing.NORTH) {
            x += randomNumber(0.3, -0.3);
        }
        return new Vec3(x, y, z);
    }

    private BlockData getBlockData(BlockPos pos) {
        if (isPosValid(pos.add(0, -1, 0))) {
            return new BlockData(pos.add(0, -1, 0), EnumFacing.UP);
        }
        if (isPosValid(pos.add(-1, 0, 0))) {
            return new BlockData(pos.add(-1, 0, 0), EnumFacing.EAST);
        }
        if (isPosValid(pos.add(1, 0, 0))) {
            return new BlockData(pos.add(1, 0, 0), EnumFacing.WEST);
        }
        if (isPosValid(pos.add(0, 0, 1))) {
            return new BlockData(pos.add(0, 0, 1), EnumFacing.NORTH);
        }
        if (isPosValid(pos.add(0, 0, -1))) {
            return new BlockData(pos.add(0, 0, -1), EnumFacing.SOUTH);
        }
        final BlockPos pos2 = pos.add(-1, 0, 0);
        if (isPosValid(pos2.add(0, -1, 0))) {
            return new BlockData(pos2.add(0, -1, 0), EnumFacing.UP);
        }
        if (isPosValid(pos2.add(-1, 0, 0))) {
            return new BlockData(pos2.add(-1, 0, 0), EnumFacing.EAST);
        }
        if (isPosValid(pos2.add(1, 0, 0))) {
            return new BlockData(pos2.add(1, 0, 0), EnumFacing.WEST);
        }
        if (isPosValid(pos2.add(0, 0, 1))) {
            return new BlockData(pos2.add(0, 0, 1), EnumFacing.NORTH);
        }
        if (isPosValid(pos2.add(0, 0, -1))) {
            return new BlockData(pos2.add(0, 0, -1), EnumFacing.SOUTH);
        }
        final BlockPos pos3 = pos.add(1, 0, 0);
        if (isPosValid(pos3.add(0, -1, 0))) {
            return new BlockData(pos3.add(0, -1, 0), EnumFacing.UP);
        }
        if (isPosValid(pos3.add(-1, 0, 0))) {
            return new BlockData(pos3.add(-1, 0, 0), EnumFacing.EAST);
        }
        if (isPosValid(pos3.add(1, 0, 0))) {
            return new BlockData(pos3.add(1, 0, 0), EnumFacing.WEST);
        }
        if (isPosValid(pos3.add(0, 0, 1))) {
            return new BlockData(pos3.add(0, 0, 1), EnumFacing.NORTH);
        }
        if (isPosValid(pos3.add(0, 0, -1))) {
            return new BlockData(pos3.add(0, 0, -1), EnumFacing.SOUTH);
        }
        final BlockPos pos4 = pos.add(0, 0, 1);
        if (isPosValid(pos4.add(0, -1, 0))) {
            return new BlockData(pos4.add(0, -1, 0), EnumFacing.UP);
        }
        if (isPosValid(pos4.add(-1, 0, 0))) {
            return new BlockData(pos4.add(-1, 0, 0), EnumFacing.EAST);
        }
        if (isPosValid(pos4.add(1, 0, 0))) {
            return new BlockData(pos4.add(1, 0, 0), EnumFacing.WEST);
        }
        if (isPosValid(pos4.add(0, 0, 1))) {
            return new BlockData(pos4.add(0, 0, 1), EnumFacing.NORTH);
        }
        if (isPosValid(pos4.add(0, 0, -1))) {
            return new BlockData(pos4.add(0, 0, -1), EnumFacing.SOUTH);
        }
        final BlockPos pos5 = pos.add(0, 0, -1);
        if (isPosValid(pos5.add(0, -1, 0))) {
            return new BlockData(pos5.add(0, -1, 0), EnumFacing.UP);
        }
        if (isPosValid(pos5.add(-1, 0, 0))) {
            return new BlockData(pos5.add(-1, 0, 0), EnumFacing.EAST);
        }
        if (isPosValid(pos5.add(1, 0, 0))) {
            return new BlockData(pos5.add(1, 0, 0), EnumFacing.WEST);
        }
        if (isPosValid(pos5.add(0, 0, 1))) {
            return new BlockData(pos5.add(0, 0, 1), EnumFacing.NORTH);
        }
        if (isPosValid(pos5.add(0, 0, -1))) {
            return new BlockData(pos5.add(0, 0, -1), EnumFacing.SOUTH);
        }
        pos.add(-2, 0, 0);
        if (isPosValid(pos2.add(0, -1, 0))) {
            return new BlockData(pos2.add(0, -1, 0), EnumFacing.UP);
        }
        if (isPosValid(pos2.add(-1, 0, 0))) {
            return new BlockData(pos2.add(-1, 0, 0), EnumFacing.EAST);
        }
        if (isPosValid(pos2.add(1, 0, 0))) {
            return new BlockData(pos2.add(1, 0, 0), EnumFacing.WEST);
        }
        if (isPosValid(pos2.add(0, 0, 1))) {
            return new BlockData(pos2.add(0, 0, 1), EnumFacing.NORTH);
        }
        if (isPosValid(pos2.add(0, 0, -1))) {
            return new BlockData(pos2.add(0, 0, -1), EnumFacing.SOUTH);
        }
        pos.add(2, 0, 0);
        if (isPosValid(pos3.add(0, -1, 0))) {
            return new BlockData(pos3.add(0, -1, 0), EnumFacing.UP);
        }
        if (isPosValid(pos3.add(-1, 0, 0))) {
            return new BlockData(pos3.add(-1, 0, 0), EnumFacing.EAST);
        }
        if (isPosValid(pos3.add(1, 0, 0))) {
            return new BlockData(pos3.add(1, 0, 0), EnumFacing.WEST);
        }
        if (isPosValid(pos3.add(0, 0, 1))) {
            return new BlockData(pos3.add(0, 0, 1), EnumFacing.NORTH);
        }
        if (isPosValid(pos3.add(0, 0, -1))) {
            return new BlockData(pos3.add(0, 0, -1), EnumFacing.SOUTH);
        }
        pos.add(0, 0, 2);
        if (isPosValid(pos4.add(0, -1, 0))) {
            return new BlockData(pos4.add(0, -1, 0), EnumFacing.UP);
        }
        if (isPosValid(pos4.add(-1, 0, 0))) {
            return new BlockData(pos4.add(-1, 0, 0), EnumFacing.EAST);
        }
        if (isPosValid(pos4.add(1, 0, 0))) {
            return new BlockData(pos4.add(1, 0, 0), EnumFacing.WEST);
        }
        if (isPosValid(pos4.add(0, 0, 1))) {
            return new BlockData(pos4.add(0, 0, 1), EnumFacing.NORTH);
        }
        if (isPosValid(pos4.add(0, 0, -1))) {
            return new BlockData(pos4.add(0, 0, -1), EnumFacing.SOUTH);
        }
        pos.add(0, 0, -2);
        if (isPosValid(pos5.add(0, -1, 0))) {
            return new BlockData(pos5.add(0, -1, 0), EnumFacing.UP);
        }
        if (isPosValid(pos5.add(-1, 0, 0))) {
            return new BlockData(pos5.add(-1, 0, 0), EnumFacing.EAST);
        }
        if (isPosValid(pos5.add(1, 0, 0))) {
            return new BlockData(pos5.add(1, 0, 0), EnumFacing.WEST);
        }
        if (isPosValid(pos5.add(0, 0, 1))) {
            return new BlockData(pos5.add(0, 0, 1), EnumFacing.NORTH);
        }
        if (isPosValid(pos5.add(0, 0, -1))) {
            return new BlockData(pos5.add(0, 0, -1), EnumFacing.SOUTH);
        }
        final BlockPos pos6 = pos.add(0, -1, 0);
        if (isPosValid(pos6.add(0, -1, 0))) {
            return new BlockData(pos6.add(0, -1, 0), EnumFacing.UP);
        }
        if (isPosValid(pos6.add(-1, 0, 0))) {
            return new BlockData(pos6.add(-1, 0, 0), EnumFacing.EAST);
        }
        if (isPosValid(pos6.add(1, 0, 0))) {
            return new BlockData(pos6.add(1, 0, 0), EnumFacing.WEST);
        }
        if (isPosValid(pos6.add(0, 0, 1))) {
            return new BlockData(pos6.add(0, 0, 1), EnumFacing.NORTH);
        }
        if (isPosValid(pos6.add(0, 0, -1))) {
            return new BlockData(pos6.add(0, 0, -1), EnumFacing.SOUTH);
        }
        final BlockPos pos7 = pos6.add(1, 0, 0);
        if (isPosValid(pos7.add(0, -1, 0))) {
            return new BlockData(pos7.add(0, -1, 0), EnumFacing.UP);
        }
        if (isPosValid(pos7.add(-1, 0, 0))) {
            return new BlockData(pos7.add(-1, 0, 0), EnumFacing.EAST);
        }
        if (isPosValid(pos7.add(1, 0, 0))) {
            return new BlockData(pos7.add(1, 0, 0), EnumFacing.WEST);
        }
        if (isPosValid(pos7.add(0, 0, 1))) {
            return new BlockData(pos7.add(0, 0, 1), EnumFacing.NORTH);
        }
        if (isPosValid(pos7.add(0, 0, -1))) {
            return new BlockData(pos7.add(0, 0, -1), EnumFacing.SOUTH);
        }
        final BlockPos pos8 = pos6.add(-1, 0, 0);
        if (isPosValid(pos8.add(0, -1, 0))) {
            return new BlockData(pos8.add(0, -1, 0), EnumFacing.UP);
        }
        if (isPosValid(pos8.add(-1, 0, 0))) {
            return new BlockData(pos8.add(-1, 0, 0), EnumFacing.EAST);
        }
        if (isPosValid(pos8.add(1, 0, 0))) {
            return new BlockData(pos8.add(1, 0, 0), EnumFacing.WEST);
        }
        if (isPosValid(pos8.add(0, 0, 1))) {
            return new BlockData(pos8.add(0, 0, 1), EnumFacing.NORTH);
        }
        if (isPosValid(pos8.add(0, 0, -1))) {
            return new BlockData(pos8.add(0, 0, -1), EnumFacing.SOUTH);
        }
        final BlockPos pos9 = pos6.add(0, 0, 1);
        if (isPosValid(pos9.add(0, -1, 0))) {
            return new BlockData(pos9.add(0, -1, 0), EnumFacing.UP);
        }
        if (isPosValid(pos9.add(-1, 0, 0))) {
            return new BlockData(pos9.add(-1, 0, 0), EnumFacing.EAST);
        }
        if (isPosValid(pos9.add(1, 0, 0))) {
            return new BlockData(pos9.add(1, 0, 0), EnumFacing.WEST);
        }
        if (isPosValid(pos9.add(0, 0, 1))) {
            return new BlockData(pos9.add(0, 0, 1), EnumFacing.NORTH);
        }
        if (isPosValid(pos9.add(0, 0, -1))) {
            return new BlockData(pos9.add(0, 0, -1), EnumFacing.SOUTH);
        }
        final BlockPos pos10 = pos6.add(0, 0, -1);
        if (isPosValid(pos10.add(0, -1, 0))) {
            return new BlockData(pos10.add(0, -1, 0), EnumFacing.UP);
        }
        if (isPosValid(pos10.add(-1, 0, 0))) {
            return new BlockData(pos10.add(-1, 0, 0), EnumFacing.EAST);
        }
        if (isPosValid(pos10.add(1, 0, 0))) {
            return new BlockData(pos10.add(1, 0, 0), EnumFacing.WEST);
        }
        if (isPosValid(pos10.add(0, 0, 1))) {
            return new BlockData(pos10.add(0, 0, 1), EnumFacing.NORTH);
        }
        if (isPosValid(pos10.add(0, 0, -1))) {
            return new BlockData(pos10.add(0, 0, -1), EnumFacing.SOUTH);
        }
        return null;
    }

    private boolean isPosValid(BlockPos pos) {
        Block block = mc.theWorld.getBlockState(pos).getBlock();
        return (block.getMaterial().isSolid() || !block.isTranslucent() || block.isVisuallyOpaque()
                || block instanceof BlockLadder || block instanceof BlockCarpet || block instanceof BlockSnow
                || block instanceof BlockSkull) && !block.getMaterial().isLiquid()
                && !(block instanceof BlockContainer);
    }

    private static class BlockData {

        private final BlockPos position;
        private final EnumFacing facing;
        private final Vec3 hitVec;

        public BlockData(BlockPos position, EnumFacing facing) {
            this.position = position;
            this.facing = facing;
            hitVec = getHitVec();
        }

        private Vec3 getHitVec() {
            Vec3i directionVec = facing.getDirectionVec();
            double x = (double)directionVec.getX() * 0.5;
            double z = (double)directionVec.getZ() * 0.5;
            if (facing.getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE) {
                x = -x;
                z = -z;
            }
            Vec3 hitVec = new Vec3(position).addVector(x + z, (double)directionVec.getY() * 0.5, x + z);
            Vec3 src = mc.thePlayer.getPositionEyes(1.0f);
            MovingObjectPosition obj = mc.theWorld.rayTraceBlocks(src, hitVec, false, false, true);
            if (obj == null || obj.hitVec == null || obj.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) {
                return null;
            }
            if (facing != EnumFacing.DOWN && facing != EnumFacing.UP) {
                obj.hitVec = obj.hitVec.addVector(0.0, -0.2, 0.0);
            }
            return obj.hitVec;
        }
    }

    @EventTarget
    public void onPacket(final PacketEvent event) {
        if (mc.thePlayer == null)
            return;

        final Packet<?> packet = event.getPacket();

        // Sprint
        if (sprintModeValue.get().equalsIgnoreCase("silent")) {
            if (packet instanceof C0BPacketEntityAction &&
                    (((C0BPacketEntityAction) packet).getAction() == C0BPacketEntityAction.Action.STOP_SPRINTING || ((C0BPacketEntityAction) packet).getAction() == C0BPacketEntityAction.Action.START_SPRINTING))
                event.cancelEvent();
        }

        // AutoBlock
        if (packet instanceof C09PacketHeldItemChange) {
            final C09PacketHeldItemChange packetHeldItemChange = (C09PacketHeldItemChange) packet;

            slot = packetHeldItemChange.getSlotId();
        }
    }
    private void fakeJump() {
        mc.thePlayer.isAirBorne = true;
        mc.thePlayer.triggerAchievement(StatList.jumpStat);
    }

    private int getBlocksAmount() {
        int amount = 0;

        for (int i = 36; i < 45; i++) {
            final ItemStack itemStack = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
            if (itemStack != null && itemStack.getItem() instanceof ItemBlock) {
                Block block = ((ItemBlock)itemStack.getItem()).getBlock();
                if (!InventoryUtils.BLOCK_BLACKLIST.contains(block) && block.isFullCube()) amount += itemStack.stackSize;
            }
        }

        return amount;
    }
    private int getBlockColor(int count) {
        float f = count;
        float f1 = 64;
        float f2 = Math.max(0.0F, Math.min(f, f1) / f1);
        return Color.HSBtoRGB(f2 / 3.0F, 1.0F, 1.0F) | 0xFF000000;
    }

    private void renderItemStack(ItemStack stack, int x, int y) {
        GlStateManager.pushMatrix();
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        RenderHelper.enableGUIStandardItemLighting();
        mc.getRenderItem().renderItemAndEffectIntoGUI(stack, x, y);
        mc.getRenderItem().renderItemOverlays(mc.fontRendererObj, stack, x, y);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    @EventTarget
    public void onRender2D(final Render2DEvent event) {
        long lastMS = 0L;
        float progress = (float) (System.currentTimeMillis() - lastMS) / 100F;


        if (progress >= 1) progress = 1;

        String counterMode = counterDisplayValue.get();
        final ScaledResolution scaledResolution = new ScaledResolution(mc);
        final String info = getBlocksAmount() + " blocks";
        int infoWidth = Fonts.fontSFUI40.getStringWidth(info);
        String info3 = "" + getBlocksAmount();
        int infoWidth2 = Fonts.minecraftFont.getStringWidth(getBlocksAmount() + "");

        if (counterMode.equalsIgnoreCase("advanced")) {
            boolean canRenderStack = (slot >= 0 && slot < 9 && mc.thePlayer.inventory.mainInventory[slot] != null && mc.thePlayer.inventory.mainInventory[slot].getItem() != null && mc.thePlayer.inventory.mainInventory[slot].getItem() instanceof ItemBlock);
            if (blurValue.get())
                BlurUtils.blurArea(scaledResolution.getScaledWidth() / 2 - (infoWidth / 2) - 4, scaledResolution.getScaledHeight() / 2 - 39, scaledResolution.getScaledWidth() / 2 + (infoWidth / 2) + 4, scaledResolution.getScaledHeight() / 2 - (canRenderStack ? 5 : 26), blurStrength.get());

            RenderUtils.drawRect(scaledResolution.getScaledWidth() / 2 - (infoWidth / 2) - 4, scaledResolution.getScaledHeight() / 2 - 40, scaledResolution.getScaledWidth() / 2 + (infoWidth / 2) + 4, scaledResolution.getScaledHeight() / 2 - 39, (getBlocksAmount() > 1 ? 0xFFFFFFFF : 0xFFFF1010));
            RenderUtils.drawRect(scaledResolution.getScaledWidth() / 2 - (infoWidth / 2) - 4, scaledResolution.getScaledHeight() / 2 - 39, scaledResolution.getScaledWidth() / 2 + (infoWidth / 2) + 4, scaledResolution.getScaledHeight() / 2 - 26, 0xA0000000);

            if (canRenderStack) {
                RenderUtils.drawRect(scaledResolution.getScaledWidth() / 2 - (infoWidth / 2) - 4, scaledResolution.getScaledHeight() / 2 - 26, scaledResolution.getScaledWidth() / 2 + (infoWidth / 2) + 4, scaledResolution.getScaledHeight() / 2 - 5, 0xA0000000);
                GlStateManager.pushMatrix();
                GlStateManager.translate(scaledResolution.getScaledWidth() / 2 - 8, scaledResolution.getScaledHeight() / 2 - 25, scaledResolution.getScaledWidth() / 2 - 8);
                renderItemStack(mc.thePlayer.inventory.mainInventory[slot], 0, 0);
                GlStateManager.popMatrix();
            }
            GlStateManager.resetColor();

            Fonts.fontSFUI40.drawCenteredString(info, scaledResolution.getScaledWidth() / 2, scaledResolution.getScaledHeight() / 2 - 36, -1);
        }

        if (counterMode.equalsIgnoreCase("sigma")) {
            GlStateManager.translate(0, -14F - (progress * 4F), 0);
            //GL11.glPushMatrix();
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glEnable(GL11.GL_LINE_SMOOTH);
            GL11.glColor4f(0.15F, 0.15F, 0.15F, progress);
            GL11.glBegin(GL11.GL_TRIANGLE_FAN);
            GL11.glVertex2d(scaledResolution.getScaledWidth() / 2 - 3, scaledResolution.getScaledHeight() - 60);
            GL11.glVertex2d(scaledResolution.getScaledWidth() / 2, scaledResolution.getScaledHeight() - 57);
            GL11.glVertex2d(scaledResolution.getScaledWidth() / 2 + 3, scaledResolution.getScaledHeight() - 60);
            GL11.glEnd();
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glDisable(GL11.GL_LINE_SMOOTH);
            //GL11.glPopMatrix();
            RenderUtils.drawRoundedRect(scaledResolution.getScaledWidth() / 2 - (infoWidth / 2) - 4, scaledResolution.getScaledHeight() - 60, scaledResolution.getScaledWidth() / 2 + (infoWidth / 2) + 4, scaledResolution.getScaledHeight() - 74, 2F, new Color(0.15F, 0.15F, 0.15F, progress).getRGB());
            GlStateManager.resetColor();
            Fonts.fontSFUI35.drawCenteredString(info, scaledResolution.getScaledWidth() / 2 + 0.1F, scaledResolution.getScaledHeight() - 70, new Color(1F, 1F, 1F, 0.8F * progress).getRGB(), false);
            GlStateManager.translate(0, 14F + (progress * 4F), 0);
        }

        if (counterMode.equalsIgnoreCase("novoline")) {
            if (slot >= 0 && slot < 9 && mc.thePlayer.inventory.mainInventory[slot] != null && mc.thePlayer.inventory.mainInventory[slot].getItem() != null && mc.thePlayer.inventory.mainInventory[slot].getItem() instanceof ItemBlock) {
                //RenderUtils.drawRect(scaledResolution.getScaledWidth() / 2 - (infoWidth / 2) - 4, scaledResolution.getScaledHeight() / 2 - 26, scaledResolution.getScaledWidth() / 2 + (infoWidth / 2) + 4, scaledResolution.getScaledHeight() / 2 - 5, 0xA0000000);
                GlStateManager.pushMatrix();
                GlStateManager.translate(scaledResolution.getScaledWidth() / 2 - 22, scaledResolution.getScaledHeight() / 2 + 16, scaledResolution.getScaledWidth() / 2 - 22);
                renderItemStack(mc.thePlayer.inventory.mainInventory[slot], 0, 0);
                GlStateManager.popMatrix();
            }
            GlStateManager.resetColor();

            Fonts.minecraftFont.drawString(getBlocksAmount() + " blocks", scaledResolution.getScaledWidth() / 2, scaledResolution.getScaledHeight() / 2 + 20, -1, true);
        }
        if (counterMode.equalsIgnoreCase("simple")) {
            float delta = RenderUtils.deltaTime;
            if (getState()) {
                alpha += 2 * delta;
                if (alpha >= 250) alpha = 250;
            } else {
                alpha -= 2 * delta;
                if (alpha <= 30) alpha = 0;
            }
            if (alpha > 1) {
                GlStateManager.pushMatrix();
                mc.fontRendererObj.drawString(info3, (scaledResolution.getScaledWidth() / 2 + 10) - 1, scaledResolution.getScaledHeight() / 2, RenderUtils.reAlpha(Color.BLACK.getRGB(), alpha / 255));
                mc.fontRendererObj.drawString(info3, (scaledResolution.getScaledWidth() / 2 + 10) + 1, scaledResolution.getScaledHeight() / 2, RenderUtils.reAlpha(Color.BLACK.getRGB(), alpha / 255));
                mc.fontRendererObj.drawString(info3, scaledResolution.getScaledWidth() / 2 + 10, (scaledResolution.getScaledHeight() / 2) - 1, RenderUtils.reAlpha(Color.BLACK.getRGB(), alpha / 255));
                mc.fontRendererObj.drawString(info3, scaledResolution.getScaledWidth() / 2 + 10, (scaledResolution.getScaledHeight() / 2) + 1, RenderUtils.reAlpha(Color.BLACK.getRGB(), alpha / 255));
                mc.fontRendererObj.drawString(info3, scaledResolution.getScaledWidth() / 2 + 10, scaledResolution.getScaledHeight() / 2, RenderUtils.reAlpha(getBlockColor(getBlocksAmount()), alpha / 255));
                mc.fontRendererObj.drawString(info3, scaledResolution.getScaledWidth() / 2 + 10, scaledResolution.getScaledHeight() / 2, getBlockColor(getBlocksAmount()));
                GlStateManager.popMatrix();
            }
        }
        if (counterMode.equalsIgnoreCase("exhibition")) {
            int c = Colors.getColor(255, 0, 0, 150);
            if (getBlocksAmount() >= 64 && 128 > getBlocksAmount()) {
                c = Colors.getColor(255, 255, 0, 150);
            } else if (getBlocksAmount() >= 128) {
                c = Colors.getColor(0, 255, 0, 150);
            }
            Fonts.minecraftFont.drawString(getBlocksAmount() + "", scaledResolution.getScaledWidth() / 2 - (infoWidth2 / 2) - 1, scaledResolution.getScaledHeight() / 2 - 36, 0xff000000, false);
            Fonts.minecraftFont.drawString(getBlocksAmount() + "", scaledResolution.getScaledWidth() / 2 - (infoWidth2 / 2) + 1, scaledResolution.getScaledHeight() / 2 - 36, 0xff000000, false);
            Fonts.minecraftFont.drawString(getBlocksAmount() + "", scaledResolution.getScaledWidth() / 2 - (infoWidth2 / 2), scaledResolution.getScaledHeight() / 2 - 35, 0xff000000, false);
            Fonts.minecraftFont.drawString(getBlocksAmount() + "", scaledResolution.getScaledWidth() / 2 - (infoWidth2 / 2), scaledResolution.getScaledHeight() / 2 - 37, 0xff000000, false);
            Fonts.minecraftFont.drawString(getBlocksAmount() + "", scaledResolution.getScaledWidth() / 2 - (infoWidth2 / 2), scaledResolution.getScaledHeight() / 2 - 36, c, false);
        }
    }
    @EventTarget
    public void onMove(final MoveEvent event) {
        if (safeWalkValue.get() && mc.thePlayer.onGround)
            event.setSafeWalk(true);
    }

    @Override
    public String getTag() {
        return rotationModeValue.get();
    }
}
