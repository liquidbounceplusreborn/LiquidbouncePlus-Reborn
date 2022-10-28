/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.command.commands

import com.google.gson.JsonArray
import com.google.gson.JsonParser
import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.command.Command
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.Config
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.NotifyType
import net.ccbluex.liquidbounce.utils.misc.HttpUtils
import kotlin.concurrent.thread

class ThemeCommand : Command("theme", arrayOf("thememanager", "tm", "themes")) {

    private val loadingLock = Object()
    private var onlineThemes: MutableList<String>? = null

    /**
     * Execute commands with provided [args]
     */
    override fun execute(args: Array<String>) {
        if (args.size <= 1) {
            chatSyntax("theme <load/list>")
            return
        }

        when {
            // Load subcommand
            args[1].equals("load", true) -> {
                if (args.size < 3) {
                    chatSyntax("theme load <name/url>")
                    return
                }

                // Settings url
                val url = if (args[2].startsWith("http"))
                    args[2]
                else
                    "${LiquidBounce.CLIENT_CLOUD}/themes/${args[2].toLowerCase()}"

                chat("Loading theme...")

                thread {
                    try {
                        // Load theme and apply them
                        chat("§9Loading theme...")
                        val theme = HttpUtils.get(url)
                        chat("§9Set theme settings...")
                        LiquidBounce.isStarting = true
                        LiquidBounce.hud.clearElements()
                        LiquidBounce.hud = Config(theme).toHUD()
                        LiquidBounce.isStarting = false
                        chat("§6Theme applied successfully.")
                        LiquidBounce.hud.addNotification(Notification("Theme","Updated HUD Theme.", NotifyType.SUCCESS))
                        playEdit()
                    } catch (exception: Exception) {
                        exception.printStackTrace()
                        chat("Failed to fetch theme.")
                    }
                }
            }

            // List subcommand
            args[1].equals("list", true) -> {
                chat("Loading themes...")

                loadTheme(false) {
                    for (theme in it)
                        chat("> $theme")
                }
            }
        }
    }

    private fun loadTheme(useCached: Boolean, join: Long? = null, callback: (List<String>) -> Unit) {
        var thread = thread {
            // Prevent the theme from being loaded twice
            synchronized(loadingLock) {
                if (useCached && onlineThemes != null) {
                    callback(onlineThemes!!)
                    return@thread
                }

                try {
                    val json = JsonParser().parse(HttpUtils.get(
                            // TODO: Add another way to get all themes
                            "https://api.github.com/repos/WYSI-Foundation/LiquidCloud/contents/LiquidBounce/themes"
                    ))

                    val loadingTheme: MutableList<String> = mutableListOf()

                    if (json is JsonArray) {
                        for (theme in json)
                            loadingTheme.add(theme.asJsonObject["name"].asString)
                    }

                    callback(loadingTheme)

                    this.onlineThemes = loadingTheme
                } catch (e: Exception) {
                    chat("Failed to fetch theme list.")
                }
            }
        }

        if (join != null)
            thread.join(join)
    }

    override fun tabComplete(args: Array<String>): List<String> {
        if (args.isEmpty()) return emptyList()

        return when (args.size) {
            1 -> listOf("list", "load").filter { it.startsWith(args[0], true) }
            2 -> {
                if (args[0].equals("load", true)) {
                    if (onlineThemes == null)
                        this.loadTheme(true, 500) {}

                    if (onlineThemes != null)
                        return onlineThemes!!.filter { it.startsWith(args[1], true) }
                }
                return emptyList()
            }
            else -> emptyList()
        }
    }

}