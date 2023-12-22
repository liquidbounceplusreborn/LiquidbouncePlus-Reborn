/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.utils.block

import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.Vec3

class PlaceInfo(val blockPos: BlockPos, var enumFacing: EnumFacing,
                var vec3: Vec3 = Vec3(blockPos.x + 0.5, blockPos.y + 0.5, blockPos.z + 0.5)) {

    companion object {

        /**
         * Allows you to find a specific place info for your [blockPos]
         */
        @JvmStatic
        fun get(blockPos: BlockPos) =
            when {
                //1
                BlockUtils.canBeClicked(blockPos.add(0, -1, 0)) ->
                    PlaceInfo(blockPos.add(0, -1, 0), EnumFacing.UP)
                BlockUtils.canBeClicked(blockPos.add(0, 0, 1)) ->
                    PlaceInfo(blockPos.add(0, 0, 1), EnumFacing.NORTH)
                BlockUtils.canBeClicked(blockPos.add(-1, 0, 0)) ->
                    PlaceInfo(blockPos.add(-1, 0, 0), EnumFacing.EAST)
                BlockUtils.canBeClicked(blockPos.add(0, 0, -1)) ->
                    PlaceInfo(blockPos.add(0, 0, -1), EnumFacing.SOUTH)
                BlockUtils.canBeClicked(blockPos.add(1, 0, 0)) ->
                    PlaceInfo(blockPos.add(1, 0, 0), EnumFacing.WEST)

                BlockUtils.canBeClicked(blockPos.add(0, -2, 0)) ->
                    PlaceInfo(blockPos.add(0, -2, 0), EnumFacing.UP)
                BlockUtils.canBeClicked(blockPos.add(0, 0, 2)) ->
                    PlaceInfo(blockPos.add(0, 0, 2), EnumFacing.NORTH)
                BlockUtils.canBeClicked(blockPos.add(-2, 0, 0)) ->
                    PlaceInfo(blockPos.add(-2, 0, 0), EnumFacing.EAST)
                BlockUtils.canBeClicked(blockPos.add(0, 0, -2)) ->
                    PlaceInfo(blockPos.add(0, 0, -2), EnumFacing.SOUTH)
                BlockUtils.canBeClicked(blockPos.add(2, 0, 0)) ->
                    PlaceInfo(blockPos.add(2, 0, 0), EnumFacing.WEST)

                //3
                BlockUtils.canBeClicked(blockPos.add(0, -3, 0)) ->
                    PlaceInfo(blockPos.add(0, -3, 0), EnumFacing.UP)
                BlockUtils.canBeClicked(blockPos.add(0, 0, 3)) ->
                    PlaceInfo(blockPos.add(0, 0, 3), EnumFacing.NORTH)
                BlockUtils.canBeClicked(blockPos.add(-3, 0, 0)) ->
                    PlaceInfo(blockPos.add(-3, 0, 0), EnumFacing.EAST)
                BlockUtils.canBeClicked(blockPos.add(0, 0, -3)) ->
                    PlaceInfo(blockPos.add(0, 0, -3), EnumFacing.SOUTH)
                BlockUtils.canBeClicked(blockPos.add(3, 0, 0)) ->
                    PlaceInfo(blockPos.add(3, 0, 0), EnumFacing.WEST)
                else -> null
            }

        @JvmStatic
        fun getPlaceInfo(pos: BlockPos): PlaceInfo? {
            if (BlockUtils.canBeClicked(pos.add(0, -1, 0))) {
                return PlaceInfo(pos.add(0, -1, 0), EnumFacing.UP)
            }
            if (BlockUtils.canBeClicked(pos.add(-1, 0, 0))) {
                return PlaceInfo(pos.add(-1, 0, 0), EnumFacing.EAST)
            }
            if (BlockUtils.canBeClicked(pos.add(1, 0, 0))) {
                return PlaceInfo(pos.add(1, 0, 0), EnumFacing.WEST)
            }
            if (BlockUtils.canBeClicked(pos.add(0, 0, 1))) {
                return PlaceInfo(pos.add(0, 0, 1), EnumFacing.NORTH)
            }
            if (BlockUtils.canBeClicked(pos.add(0, 0, -1))) {
                return PlaceInfo(pos.add(0, 0, -1), EnumFacing.SOUTH)
            }
            val pos2 = pos.add(-1, 0, 0)
            if (BlockUtils.canBeClicked(pos2.add(0, -1, 0))) {
                return PlaceInfo(pos2.add(0, -1, 0), EnumFacing.UP)
            }
            if (BlockUtils.canBeClicked(pos2.add(-1, 0, 0))) {
                return PlaceInfo(pos2.add(-1, 0, 0), EnumFacing.EAST)
            }
            if (BlockUtils.canBeClicked(pos2.add(1, 0, 0))) {
                return PlaceInfo(pos2.add(1, 0, 0), EnumFacing.WEST)
            }
            if (BlockUtils.canBeClicked(pos2.add(0, 0, 1))) {
                return PlaceInfo(pos2.add(0, 0, 1), EnumFacing.NORTH)
            }
            if (BlockUtils.canBeClicked(pos2.add(0, 0, -1))) {
                return PlaceInfo(pos2.add(0, 0, -1), EnumFacing.SOUTH)
            }
            val pos3 = pos.add(1, 0, 0)
            if (BlockUtils.canBeClicked(pos3.add(0, -1, 0))) {
                return PlaceInfo(pos3.add(0, -1, 0), EnumFacing.UP)
            }
            if (BlockUtils.canBeClicked(pos3.add(-1, 0, 0))) {
                return PlaceInfo(pos3.add(-1, 0, 0), EnumFacing.EAST)
            }
            if (BlockUtils.canBeClicked(pos3.add(1, 0, 0))) {
                return PlaceInfo(pos3.add(1, 0, 0), EnumFacing.WEST)
            }
            if (BlockUtils.canBeClicked(pos3.add(0, 0, 1))) {
                return PlaceInfo(pos3.add(0, 0, 1), EnumFacing.NORTH)
            }
            if (BlockUtils.canBeClicked(pos3.add(0, 0, -1))) {
                return PlaceInfo(pos3.add(0, 0, -1), EnumFacing.SOUTH)
            }
            val pos4 = pos.add(0, 0, 1)
            if (BlockUtils.canBeClicked(pos4.add(0, -1, 0))) {
                return PlaceInfo(pos4.add(0, -1, 0), EnumFacing.UP)
            }
            if (BlockUtils.canBeClicked(pos4.add(-1, 0, 0))) {
                return PlaceInfo(pos4.add(-1, 0, 0), EnumFacing.EAST)
            }
            if (BlockUtils.canBeClicked(pos4.add(1, 0, 0))) {
                return PlaceInfo(pos4.add(1, 0, 0), EnumFacing.WEST)
            }
            if (BlockUtils.canBeClicked(pos4.add(0, 0, 1))) {
                return PlaceInfo(pos4.add(0, 0, 1), EnumFacing.NORTH)
            }
            if (BlockUtils.canBeClicked(pos4.add(0, 0, -1))) {
                return PlaceInfo(pos4.add(0, 0, -1), EnumFacing.SOUTH)
            }
            val pos5 = pos.add(0, 0, -1)
            if (BlockUtils.canBeClicked(pos5.add(0, -1, 0))) {
                return PlaceInfo(pos5.add(0, -1, 0), EnumFacing.UP)
            }
            if (BlockUtils.canBeClicked(pos5.add(-1, 0, 0))) {
                return PlaceInfo(pos5.add(-1, 0, 0), EnumFacing.EAST)
            }
            if (BlockUtils.canBeClicked(pos5.add(1, 0, 0))) {
                return PlaceInfo(pos5.add(1, 0, 0), EnumFacing.WEST)
            }
            if (BlockUtils.canBeClicked(pos5.add(0, 0, 1))) {
                return PlaceInfo(pos5.add(0, 0, 1), EnumFacing.NORTH)
            }
            if (BlockUtils.canBeClicked(pos5.add(0, 0, -1))) {
                return PlaceInfo(pos5.add(0, 0, -1), EnumFacing.SOUTH)
            }
            pos.add(-2, 0, 0)
            if (BlockUtils.canBeClicked(pos2.add(0, -1, 0))) {
                return PlaceInfo(pos2.add(0, -1, 0), EnumFacing.UP)
            }
            if (BlockUtils.canBeClicked(pos2.add(-1, 0, 0))) {
                return PlaceInfo(pos2.add(-1, 0, 0), EnumFacing.EAST)
            }
            if (BlockUtils.canBeClicked(pos2.add(1, 0, 0))) {
                return PlaceInfo(pos2.add(1, 0, 0), EnumFacing.WEST)
            }
            if (BlockUtils.canBeClicked(pos2.add(0, 0, 1))) {
                return PlaceInfo(pos2.add(0, 0, 1), EnumFacing.NORTH)
            }
            if (BlockUtils.canBeClicked(pos2.add(0, 0, -1))) {
                return PlaceInfo(pos2.add(0, 0, -1), EnumFacing.SOUTH)
            }
            pos.add(2, 0, 0)
            if (BlockUtils.canBeClicked(pos3.add(0, -1, 0))) {
                return PlaceInfo(pos3.add(0, -1, 0), EnumFacing.UP)
            }
            if (BlockUtils.canBeClicked(pos3.add(-1, 0, 0))) {
                return PlaceInfo(pos3.add(-1, 0, 0), EnumFacing.EAST)
            }
            if (BlockUtils.canBeClicked(pos3.add(1, 0, 0))) {
                return PlaceInfo(pos3.add(1, 0, 0), EnumFacing.WEST)
            }
            if (BlockUtils.canBeClicked(pos3.add(0, 0, 1))) {
                return PlaceInfo(pos3.add(0, 0, 1), EnumFacing.NORTH)
            }
            if (BlockUtils.canBeClicked(pos3.add(0, 0, -1))) {
                return PlaceInfo(pos3.add(0, 0, -1), EnumFacing.SOUTH)
            }
            pos.add(0, 0, 2)
            if (BlockUtils.canBeClicked(pos4.add(0, -1, 0))) {
                return PlaceInfo(pos4.add(0, -1, 0), EnumFacing.UP)
            }
            if (BlockUtils.canBeClicked(pos4.add(-1, 0, 0))) {
                return PlaceInfo(pos4.add(-1, 0, 0), EnumFacing.EAST)
            }
            if (BlockUtils.canBeClicked(pos4.add(1, 0, 0))) {
                return PlaceInfo(pos4.add(1, 0, 0), EnumFacing.WEST)
            }
            if (BlockUtils.canBeClicked(pos4.add(0, 0, 1))) {
                return PlaceInfo(pos4.add(0, 0, 1), EnumFacing.NORTH)
            }
            if (BlockUtils.canBeClicked(pos4.add(0, 0, -1))) {
                return PlaceInfo(pos4.add(0, 0, -1), EnumFacing.SOUTH)
            }
            pos.add(0, 0, -2)
            if (BlockUtils.canBeClicked(pos5.add(0, -1, 0))) {
                return PlaceInfo(pos5.add(0, -1, 0), EnumFacing.UP)
            }
            if (BlockUtils.canBeClicked(pos5.add(-1, 0, 0))) {
                return PlaceInfo(pos5.add(-1, 0, 0), EnumFacing.EAST)
            }
            if (BlockUtils.canBeClicked(pos5.add(1, 0, 0))) {
                return PlaceInfo(pos5.add(1, 0, 0), EnumFacing.WEST)
            }
            if (BlockUtils.canBeClicked(pos5.add(0, 0, 1))) {
                return PlaceInfo(pos5.add(0, 0, 1), EnumFacing.NORTH)
            }
            if (BlockUtils.canBeClicked(pos5.add(0, 0, -1))) {
                return PlaceInfo(pos5.add(0, 0, -1), EnumFacing.SOUTH)
            }
            val pos6 = pos.add(0, -1, 0)
            if (BlockUtils.canBeClicked(pos6.add(0, -1, 0))) {
                return PlaceInfo(pos6.add(0, -1, 0), EnumFacing.UP)
            }
            if (BlockUtils.canBeClicked(pos6.add(-1, 0, 0))) {
                return PlaceInfo(pos6.add(-1, 0, 0), EnumFacing.EAST)
            }
            if (BlockUtils.canBeClicked(pos6.add(1, 0, 0))) {
                return PlaceInfo(pos6.add(1, 0, 0), EnumFacing.WEST)
            }
            if (BlockUtils.canBeClicked(pos6.add(0, 0, 1))) {
                return PlaceInfo(pos6.add(0, 0, 1), EnumFacing.NORTH)
            }
            if (BlockUtils.canBeClicked(pos6.add(0, 0, -1))) {
                return PlaceInfo(pos6.add(0, 0, -1), EnumFacing.SOUTH)
            }
            val pos7 = pos6.add(1, 0, 0)
            if (BlockUtils.canBeClicked(pos7.add(0, -1, 0))) {
                return PlaceInfo(pos7.add(0, -1, 0), EnumFacing.UP)
            }
            if (BlockUtils.canBeClicked(pos7.add(-1, 0, 0))) {
                return PlaceInfo(pos7.add(-1, 0, 0), EnumFacing.EAST)
            }
            if (BlockUtils.canBeClicked(pos7.add(1, 0, 0))) {
                return PlaceInfo(pos7.add(1, 0, 0), EnumFacing.WEST)
            }
            if (BlockUtils.canBeClicked(pos7.add(0, 0, 1))) {
                return PlaceInfo(pos7.add(0, 0, 1), EnumFacing.NORTH)
            }
            if (BlockUtils.canBeClicked(pos7.add(0, 0, -1))) {
                return PlaceInfo(pos7.add(0, 0, -1), EnumFacing.SOUTH)
            }
            val pos8 = pos6.add(-1, 0, 0)
            if (BlockUtils.canBeClicked(pos8.add(0, -1, 0))) {
                return PlaceInfo(pos8.add(0, -1, 0), EnumFacing.UP)
            }
            if (BlockUtils.canBeClicked(pos8.add(-1, 0, 0))) {
                return PlaceInfo(pos8.add(-1, 0, 0), EnumFacing.EAST)
            }
            if (BlockUtils.canBeClicked(pos8.add(1, 0, 0))) {
                return PlaceInfo(pos8.add(1, 0, 0), EnumFacing.WEST)
            }
            if (BlockUtils.canBeClicked(pos8.add(0, 0, 1))) {
                return PlaceInfo(pos8.add(0, 0, 1), EnumFacing.NORTH)
            }
            if (BlockUtils.canBeClicked(pos8.add(0, 0, -1))) {
                return PlaceInfo(pos8.add(0, 0, -1), EnumFacing.SOUTH)
            }
            val pos9 = pos6.add(0, 0, 1)
            if (BlockUtils.canBeClicked(pos9.add(0, -1, 0))) {
                return PlaceInfo(pos9.add(0, -1, 0), EnumFacing.UP)
            }
            if (BlockUtils.canBeClicked(pos9.add(-1, 0, 0))) {
                return PlaceInfo(pos9.add(-1, 0, 0), EnumFacing.EAST)
            }
            if (BlockUtils.canBeClicked(pos9.add(1, 0, 0))) {
                return PlaceInfo(pos9.add(1, 0, 0), EnumFacing.WEST)
            }
            if (BlockUtils.canBeClicked(pos9.add(0, 0, 1))) {
                return PlaceInfo(pos9.add(0, 0, 1), EnumFacing.NORTH)
            }
            if (BlockUtils.canBeClicked(pos9.add(0, 0, -1))) {
                return PlaceInfo(pos9.add(0, 0, -1), EnumFacing.SOUTH)
            }
            val pos10 = pos6.add(0, 0, -1)
            if (BlockUtils.canBeClicked(pos10.add(0, -1, 0))) {
                return PlaceInfo(pos10.add(0, -1, 0), EnumFacing.UP)
            }
            if (BlockUtils.canBeClicked(pos10.add(-1, 0, 0))) {
                return PlaceInfo(pos10.add(-1, 0, 0), EnumFacing.EAST)
            }
            if (BlockUtils.canBeClicked(pos10.add(1, 0, 0))) {
                return PlaceInfo(pos10.add(1, 0, 0), EnumFacing.WEST)
            }
            if (BlockUtils.canBeClicked(pos10.add(0, 0, 1))) {
                return PlaceInfo(pos10.add(0, 0, 1), EnumFacing.NORTH)
            }
            return if (BlockUtils.canBeClicked(pos10.add(0, 0, -1))) {
                PlaceInfo(pos10.add(0, 0, -1), EnumFacing.SOUTH)
            } else null
        }
    }
}