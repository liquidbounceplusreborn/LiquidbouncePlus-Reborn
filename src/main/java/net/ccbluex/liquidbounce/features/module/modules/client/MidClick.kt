/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.file.configs.FriendsConfig
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import org.lwjgl.input.Mouse

@ModuleInfo(name = "MidClick", spacedName = "Mid Click", description = "Allows you to add a player as a friend by middle clicking them.", category = ModuleCategory.CLIENT)
class MidClick : Module() {
    private var wasDown = false

    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        if (mc.currentScreen != null) return

        if (wasDown && Mouse.isButtonDown(2)) {
            val entity = mc.objectMouseOver.entityHit
            if (entity != null && entity is EntityPlayer) {
                val playerName = ColorUtils.stripColor(entity.name)
                val friendsConfig = LiquidBounce.fileManager.friendsConfig

                if (friendsConfig.isFriend(playerName)) {
                    friendsConfig.removeFriend(playerName)
                    LiquidBounce.fileManager.saveConfig(friendsConfig)
                    ClientUtils.displayChatMessage("§a§l$playerName§c was removed from your friends.")
                } else {
                    friendsConfig.addFriend(playerName)
                    LiquidBounce.fileManager.saveConfig(friendsConfig)
                    ClientUtils.displayChatMessage("§a§l$playerName§c was added to your friends.")
                }
            } else
                ClientUtils.displayChatMessage("§c§lError: §aYou need to select a player.")
        }
    }
}