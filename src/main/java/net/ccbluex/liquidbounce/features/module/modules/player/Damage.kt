package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.util.BlockPos

@ModuleInfo(name = "Damage", description = "Deals damage to yourself.", category = ModuleCategory.PLAYER, onlyEnable = true)
class Damage : Module() {

    private val modeValue = ListValue("Mode", arrayOf("NCP","c04","c03-c04","c03-c06","instant-c06","Verus"), "NCP")
    private val jumpYPosArr = arrayOf(0.41999998688698, 0.7531999805212, 1.00133597911214, 1.16610926093821, 1.24918707874468, 1.24918707874468, 1.1707870772188, 1.0155550727022, 0.78502770378924, 0.4807108763317, 0.10408037809304, 0.0)
    val x = mc.thePlayer!!.posX
    val y = mc.thePlayer!!.posY
    val z = mc.thePlayer!!.posZ
    override fun onEnable() {
        when (modeValue.get().lowercase()) {
            "ncp" -> {
                        val x = mc.thePlayer.posX
                        val y = mc.thePlayer.posY
                        val z = mc.thePlayer.posZ
                        repeat(4) {
                            jumpYPosArr.forEach {
                                PacketUtils.sendPacketNoEvent(C04PacketPlayerPosition(x, y + it, z, false))
                            }
                            PacketUtils.sendPacketNoEvent(C04PacketPlayerPosition(x, y, z, false))
                        }
                        PacketUtils.sendPacketNoEvent(C04PacketPlayerPosition(x, y, z, true))
                    }
                    "c04" -> {
                        PacketUtils.sendPacketNoEvent(C04PacketPlayerPosition(x, y + 3.35, z, false))
                        PacketUtils.sendPacketNoEvent(C04PacketPlayerPosition(x, y, z, false))
                        PacketUtils.sendPacketNoEvent(C04PacketPlayerPosition(x, y, z, true))
                    }
                    "c03-c04"->{
                        PacketUtils.sendPacketNoEvent(C03PacketPlayer(false))
                        PacketUtils.sendPacketNoEvent(C04PacketPlayerPosition(x, y + 3.36, z, false))
                        PacketUtils.sendPacketNoEvent(C04PacketPlayerPosition(x, y, z, false))
                        PacketUtils.sendPacketNoEvent(C04PacketPlayerPosition(x, y, z, true))
                }
            "c03-c06" -> {
                if (mc.thePlayer!!.onGround) {
                    PacketUtils.sendPacketNoEvent(C03PacketPlayer(false))
                    PacketUtils.sendPacketNoEvent(C03PacketPlayer.C06PacketPlayerPosLook(x, y + 3.35, z, mc.thePlayer!!.rotationYaw, mc.thePlayer!!.rotationPitch, false))
                    PacketUtils.sendPacketNoEvent(C03PacketPlayer.C06PacketPlayerPosLook(x, y, z, mc.thePlayer!!.rotationYaw, mc.thePlayer!!.rotationPitch, false))
                    PacketUtils.sendPacketNoEvent(C03PacketPlayer.C06PacketPlayerPosLook(x, y, z, mc.thePlayer!!.rotationYaw, mc.thePlayer!!.rotationPitch, true))
                }
            }
            "instant-c06" -> {
                if (mc.thePlayer!!.onGround) {
                    PacketUtils.sendPacketNoEvent(
                        C03PacketPlayer.C06PacketPlayerPosLook(
                            mc.thePlayer!!.posX,
                            mc.thePlayer.posY,
                            mc.thePlayer!!.posZ,
                            mc.thePlayer!!.rotationYaw,
                            mc.thePlayer!!.rotationPitch,
                            false
                        )
                    )
                    PacketUtils.sendPacketNoEvent(
                        C03PacketPlayer.C06PacketPlayerPosLook(
                            x,
                            y + 3.35,
                            z,
                            mc.thePlayer!!.rotationYaw,
                            mc.thePlayer!!.rotationPitch,
                            false
                        )
                    )
                    PacketUtils.sendPacketNoEvent(
                        C03PacketPlayer.C06PacketPlayerPosLook(
                            x,
                            y,
                            z,
                            mc.thePlayer!!.rotationYaw,
                            mc.thePlayer!!.rotationPitch,
                            false
                        )
                    )
                    PacketUtils.sendPacketNoEvent(
                        C03PacketPlayer.C06PacketPlayerPosLook(
                            x,
                            y,
                            z,
                            mc.thePlayer!!.rotationYaw,
                            mc.thePlayer!!.rotationPitch,
                            true
                        )
                    )
                }
            }
            "verus"->{
                PacketUtils.sendPacketNoEvent(C08PacketPlayerBlockPlacement(BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.5, mc.thePlayer.posZ), 1, ItemStack(Blocks.stone.getItem(mc.theWorld, BlockPos(-1, -1, -1))), 0f, 0.94f, 0f))
                PacketUtils.sendPacketNoEvent(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 3.05, mc.thePlayer.posZ, false))
                PacketUtils.sendPacketNoEvent(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, false))
                PacketUtils.sendPacketNoEvent(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY+0.41999998688697815, mc.thePlayer.posZ, true))

            }
        } 
    }
}
