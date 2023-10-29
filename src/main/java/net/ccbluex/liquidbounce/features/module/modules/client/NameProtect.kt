/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.TextEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.misc.StringUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils.translateAlternateColorCodes
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.TextValue
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.util.ResourceLocation
import java.io.File
import java.io.FileInputStream
import java.util.*
import javax.imageio.ImageIO
import javax.print.attribute.standard.MediaSize.Other

@ModuleInfo(
    name = "NameProtect",
    spacedName = "Name Protect",
    description = "Changes player names client-side.",
    category = ModuleCategory.CLIENT
)
class NameProtect : Module() {
    private val selfValue = BoolValue("Yourself", true)
    private val fakeNameValue = TextValue("YourFakeName", "&cMe") { selfValue.get() }
    val allPlayersValue = BoolValue("Others", false)
    private val allFakeNameValue = TextValue("OthersFakeName", "Censored") { allPlayersValue.get() }
    private val tagValue = BoolValue("Tag", false)
    val skinProtectValue = BoolValue("SkinProtect", false)
    val customSkinValue = BoolValue("CustomSkin", false) { skinProtectValue.get() }
    var skinImage: ResourceLocation? = null

    override fun onEnable() {
        val skinFile = File(LiquidBounce.fileManager.dir, "cskin.png")
        if (skinFile.isFile()) {
            try {
                val bufferedImage = ImageIO.read(FileInputStream(skinFile)) ?: return
                skinImage = ResourceLocation(LiquidBounce.CLIENT_NAME.lowercase(Locale.getDefault()) + "/cskin.png")
                mc.textureManager.loadTexture(skinImage, DynamicTexture(bufferedImage))
                ClientUtils.getLogger().info("Loaded custom skin for NameProtect.")
            } catch (e: Exception) {
                ClientUtils.getLogger().error("Failed to load custom skin.", e)
            }
        }
    }

    @EventTarget
    fun onText(event: TextEvent) {
        if (mc.thePlayer == null
            || event.text!!.contains("§8[§9§l" + LiquidBounce.CLIENT_NAME + "§8] §3")
            || event.text!!.startsWith("/") ||
            event.text!!.startsWith(LiquidBounce.commandManager.prefix.toString() + "")
            )
            return

        for (friend in LiquidBounce.fileManager.friendsConfig.friends)
            event.text = StringUtils.replace(event.text, friend.playerName, translateAlternateColorCodes(friend.alias) + "§f")
        event.text = StringUtils.replace(
            event.text,
            mc.thePlayer.name,
            if (selfValue.get()) (if (tagValue.get()) StringUtils.injectAirString(mc.thePlayer.name)
                    + " §7(§r"
                    + translateAlternateColorCodes(fakeNameValue.get() + "§r§7)"
            ) else translateAlternateColorCodes(fakeNameValue.get()) + "§r") else mc.thePlayer.name
        )
        if (allPlayersValue.get()) for (playerInfo in mc.netHandler.playerInfoMap) event.text = StringUtils.replace(
            event.text,
            playerInfo.gameProfile.name,
            translateAlternateColorCodes(allFakeNameValue.get()) + "§f"
        )
    }
}
