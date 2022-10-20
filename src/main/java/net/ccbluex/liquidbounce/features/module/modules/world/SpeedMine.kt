
package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.init.Blocks
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing

@ModuleInfo(name = "SpeedMine", spacedName = "Speed Mine", description = "Mines blocks faster.", category = ModuleCategory.WORLD)
class SpeedMine : Module() {
    private val modeValue = ListValue("Mode", arrayOf("Hypixel", "Packet", "NewPacket", "NewPacket2"),"NewPacket")
    private val breakSpeedValue = FloatValue("BreakSpeed", 1.2F, 1F, 2.0F)
    private var bzs = false
    private var bzx = 0.0F
    var blockPos: BlockPos? = null
    private var facing: EnumFacing? = null
    @EventTarget
    fun onPacket(event: PacketEvent) {
        if(modeValue.get() == "Hypixel") {
            if (event.packet is C07PacketPlayerDigging && !mc.playerController.extendedReach()
                    && mc.playerController != null) {
                val c07PacketPlayerDigging = event.packet
                if (c07PacketPlayerDigging.status == C07PacketPlayerDigging.Action.START_DESTROY_BLOCK) {
                    bzs = true
                    blockPos = c07PacketPlayerDigging.position
                    facing = c07PacketPlayerDigging.facing
                    bzx = 0.0f
                } else if (c07PacketPlayerDigging.status == C07PacketPlayerDigging.Action.ABORT_DESTROY_BLOCK
                        || c07PacketPlayerDigging.status == C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK) {
                    bzs = false
                    blockPos = null
                    facing = null
                }
            }
        }
    }

    @EventTarget
    private fun onUpdate(e: UpdateEvent) {
        when(modeValue.get()) {
            "Packet" -> {
                if(mc.playerController.curBlockDamageMP in 0.1F..0.19F)
                    mc.playerController.curBlockDamageMP += 0.1F
                if(mc.playerController.curBlockDamageMP in 0.4F..0.49F)
                    mc.playerController.curBlockDamageMP += 0.1F
                if(mc.playerController.curBlockDamageMP in 0.8F..0.89F)
                    mc.playerController.curBlockDamageMP += 0.9F
            }
            "NewPacket" -> {
                if(mc.playerController.curBlockDamageMP == 0.1F)
                    mc.playerController.curBlockDamageMP += 0.1F
                if(mc.playerController.curBlockDamageMP == 0.4F)
                    mc.playerController.curBlockDamageMP += 0.1F
                if(mc.playerController.curBlockDamageMP == 0.7F)
                    mc.playerController.curBlockDamageMP += 0.1F
            }
            "NewPacket2" -> {
                if(mc.playerController.curBlockDamageMP == 0.2F)
                    mc.playerController.curBlockDamageMP += 0.1F
                if(mc.playerController.curBlockDamageMP == 0.4F)
                    mc.playerController.curBlockDamageMP += 0.1F
                if(mc.playerController.curBlockDamageMP == 0.6F)
                    mc.playerController.curBlockDamageMP += 0.1F
                if(mc.playerController.curBlockDamageMP == 0.8F)
                    mc.playerController.curBlockDamageMP += 0.2F
            }
            "Hypixel" -> {
                if (mc.playerController.extendedReach()) {
                    mc.playerController.blockHitDelay = 0
                } else if (bzs) {
                    val block = mc.theWorld.getBlockState(blockPos).block
                    bzx += (block.getPlayerRelativeBlockHardness(mc.thePlayer, mc.theWorld, blockPos).toDouble() * breakSpeedValue.get()).toFloat()
                    if (bzx >= 1.0F) {
                        mc.theWorld.setBlockState(blockPos, Blocks.air.defaultState, 11)
                        mc.netHandler.networkManager.sendPacket(C07PacketPlayerDigging(
                                C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, blockPos, facing))
                        bzx = 0.0F
                        bzs = false
                    }
                }
            }
        }
    }
}