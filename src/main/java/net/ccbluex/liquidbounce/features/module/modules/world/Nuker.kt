/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.injection.implementations.IPlayerControllerMP
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.minecraft.block.Block
import net.minecraft.block.BlockAir
import net.minecraft.entity.projectile.EntitySnowball
import net.minecraft.init.Blocks
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.MathHelper

@ModuleInfo(name = "Nuker", description = "Breaks all blocks around you.", category = ModuleCategory.WORLD)
class Nuker : Module() {

    val silent = BoolValue("Silent",true)
    val radius = IntegerValue("Radius",4,1,7)
    val height = IntegerValue("HeightRadius",4,1,7)

    private var posX = 0
    private var posY = 0
    private var posZ = 0
    private var isRunning = false
    private val timer = Timer()
    private var destroy = false
    private var progress = 0.0f
    private var blockPos: BlockPos? = null
    private var facing: EnumFacing? = null

    override fun onDisable() {
        progress = 0.0f
        destroy = false
        blockPos = null
        facing = null
        isRunning = false
        posZ = 0
        posY = posZ
        posX = posY
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent?) {

        // 箱子判断
        val controller: IPlayerControllerMP = mc.playerController as IPlayerControllerMP
        if (mc.playerController.extendedReach()) {
            controller.setBlockHitDelay(0)
            if (controller.curBlockDamageMP >= 0.3) {
                controller.curBlockDamageMP = 1.0f
            }
        } else if (destroy && mc.thePlayer
                .canHarvestBlock(mc.theWorld.getBlockState(mc.objectMouseOver.blockPos).block)
        ) {
            val block = mc.theWorld.getBlockState(blockPos).block
            progress += (block.getPlayerRelativeBlockHardness(mc.thePlayer, mc.theWorld, blockPos) * 2.0).toFloat()
            if (progress >= 1.0f) {
                mc.theWorld.setBlockState(blockPos, Blocks.air.defaultState, 11)
                PacketUtils.sendPacketNoEvent(
                    C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, blockPos, facing)
                )
                destroy = false
            }
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent?) {
        // 箱子判断
        if (event != null) {
            if (event.packet is C07PacketPlayerDigging && !mc.playerController.extendedReach()) {
                val packet = event.packet as C07PacketPlayerDigging
                if (isRunning) {
                    destroy = true
                    blockPos = packet.position
                    facing = packet.facing
                    progress = 0.0f
                    mc.timer.timerSpeed = 1.0f
                } else if (!isRunning) {
                    destroy = false
                    blockPos = null
                    facing = null
                    mc.timer.timerSpeed = 1.0f
                }
            }
        }
    }

    @EventTarget
    fun onPre(e: MotionEvent) {
        if (e.eventState == EventState.PRE) {
            isRunning = false
            val radius: Int = radius.get()
            val height: Int = height.get()
            for (y in height downTo -height) {
                for (x in -radius until radius) {
                    for (z in -radius until radius) {
                        posX = Math.floor(mc.thePlayer.posX).toInt() + x
                        this.posY = Math.floor(mc.thePlayer.posY).toInt() + y
                        this.posZ = Math.floor(mc.thePlayer.posZ).toInt() + z
                        if (mc.thePlayer.getDistanceSq(
                                mc.thePlayer.posX + x.toDouble(),
                                mc.thePlayer.posY + y.toDouble(), mc.thePlayer.posZ + z.toDouble()
                            ) <= 16.0
                        ) {
                            val block = getBlock(posX, this.posY, this.posZ)
                            var blockChecks = timer.hasTimeElapsed(50L)
                            val selected = getBlock(mc.objectMouseOver.blockPos)
                            blockChecks = (blockChecks && canSeeBlock(posX + 0.5f, this.posY + 0.9f, this.posZ + 0.5f)
                                    && block !is BlockAir)
                            blockChecks = blockChecks && (block.getBlockHardness(mc.theWorld, BlockPos.ORIGIN) != -1.0f
                                    || mc.playerController.isInCreativeMode())
                            if (blockChecks) {
                                isRunning = true
                                if (mc.objectMouseOver != null) {
                                    var strength = 1.0f
                                    var bestToolSlot = -1
                                    for (i in 0..8) {
                                        val itemStack = mc.thePlayer.inventory.getStackInSlot(i)
                                        if (itemStack == null || itemStack.getStrVsBlock(block) <= strength) continue
                                        strength = itemStack.getStrVsBlock(block)
                                        bestToolSlot = i
                                    }
                                    if (bestToolSlot != -1) {
                                        mc.thePlayer.inventory.currentItem = bestToolSlot
                                    }
                                }
                                val angles =
                                    faceBlock((posX + 0.5f).toDouble(), this.posY + 0.9, (this.posZ + 0.5f).toDouble())
                                if (silent.get()) {
                                    e.yaw = angles[0]
                                    e.pitch = angles[1]
                                } else {
                                    mc.thePlayer.rotationYaw = angles[0]
                                    mc.thePlayer.rotationPitch = angles[1]
                                }
                                return
                            }
                        }
                    }
                }
            }
        }
    }

    @EventTarget
    fun onPost(e: MotionEvent?) {
        if (e != null) {
            if (e.eventState == EventState.POST) {
                val block = getBlock(posX, this.posY, this.posZ)
                if (isRunning) {
                    mc.playerController.onPlayerDamageBlock(
                        BlockPos(posX, this.posY, this.posZ),
                        getFacing(BlockPos(posX, this.posY, this.posZ))
                    )
                    mc.thePlayer.swingItem()
                    if (mc.playerController.curBlockDamageMP as Double >= 1.0) timer.reset()
                }
            }
        }
    }

    fun canSeeBlock(x: Float, y: Float, z: Float): Boolean {
        return getFacing(BlockPos(x.toDouble(), y.toDouble(), z.toDouble())) != null
    }

    fun faceBlock(posX: Double, posY: Double, posZ: Double): FloatArray {
        val diffX = posX - mc.thePlayer.posX
        val diffZ = posZ - mc.thePlayer.posZ
        val diffY = posY - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight())
        val dist = MathHelper.sqrt_double(diffX * diffX + diffZ * diffZ).toDouble()
        val yaw = (Math.atan2(diffZ, diffX) * 180.0 / 3.141592653589793).toFloat() - 90.0f
        val pitch = (-(Math.atan2(diffY, dist) * 180.0 / 3.141592653589793)).toFloat()
        var lyaw = mc.thePlayer.rotationYaw
        var lpitch = mc.thePlayer.rotationPitch
        return floatArrayOf(MathHelper.wrapAngleTo180_float(yaw - lyaw).let { lyaw += it; lyaw },
            MathHelper.wrapAngleTo180_float(pitch - lpitch).let {
                lpitch += it; lpitch
            })
    }

    fun getBlock(x: Int, y: Int, z: Int): Block {
        return mc.theWorld.getBlockState(BlockPos(x, y, z)).block
    }

    fun getBlock(pos: BlockPos?): Block {
        return mc.theWorld.getBlockState(pos).block
    }

    fun getFacing(pos: BlockPos): EnumFacing? {
        val orderedValues = arrayOf(
            EnumFacing.UP, EnumFacing.NORTH, EnumFacing.EAST,
            EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.DOWN
        )
        val var3 = orderedValues.size
        for (var4 in 0 until var3) {
            val facing = orderedValues[var4]
            val temp = EntitySnowball(mc.theWorld)
            temp.posX = pos.x.toDouble() + 0.5
            temp.posY = pos.y.toDouble() + 0.5
            temp.posZ = pos.z.toDouble() + 0.5
            temp.posX += facing.directionVec.x.toDouble() * 0.5
            temp.posY += facing.directionVec.y.toDouble() * 0.5
            temp.posZ += facing.directionVec.z.toDouble() * 0.5
            if (mc.thePlayer.canEntityBeSeen(temp)) {
                return facing
            }
        }
        return null
    }

    class Timer {
        private var time: Long

        init {
            time = System.nanoTime() / 1000000L
        }

        fun hasTimeElapsed(time: Long, reset: Boolean): Boolean {
            if (time() >= time) {
                if (reset) reset()
                return true
            }
            return false
        }

        fun hasTimeElapsed(time: Long): Boolean {
            return time() >= time
        }

        fun hasTicksElapsed(ticks: Int): Boolean {
            return time() >= 1000 / ticks - 50
        }

        fun hasTicksElapsed(time: Int, ticks: Int): Boolean {
            return time() >= time / ticks - 50
        }

        fun time(): Long {
            return System.nanoTime() / 1000000L - time
        }

        fun reset() {
            time = System.nanoTime() / 1000000L
        }
    }
}