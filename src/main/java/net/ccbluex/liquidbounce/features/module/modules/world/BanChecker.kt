/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 * 
 * This code belongs to WYSI-Foundation. Please give credits when using this in your repository.
 */
package net.ccbluex.liquidbounce.features.module.modules.world

import com.google.gson.JsonParser
import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.NotifyType
import net.ccbluex.liquidbounce.utils.misc.HttpUtils.get
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.IntegerValue

@ModuleInfo(
    name = "BanChecker",
    spacedName = "Ban Checker",
    description = "Checks for ban on Hypixel every minute and alert you if there is any.",
    category = ModuleCategory.WORLD
)
class BanChecker : Module() {
    val alertValue = BoolValue("Alert", true)
    val serverCheckValue = BoolValue("ServerCheck", true)
    val alertTimeValue = IntegerValue("Alert-Time", 10, 1, 50, " seconds")

    var watchdogLastMin = 0
    var lastStaffTotal = -1
    var staffLastMin = 0
    var thread: Thread? = null

    private fun getBanCheckerThread(): Thread = object : Thread("Hypixel-BanChecker") {
        override fun run() {
            val checkTimer = MSTimer()
            while (true) {
                mc.thePlayer ?: return
                if (checkTimer.hasTimePassed(60000L)) {
                    try {
                        val apiContent = get("https://api.plancke.io/hypixel/v1/punishmentStats")
                        val jsonObject = JsonParser().parse(apiContent).getAsJsonObject()
                        if (jsonObject["success"].asBoolean && jsonObject.has("record")) {
                            val objectAPI = jsonObject["record"].getAsJsonObject()
                            watchdogLastMin = objectAPI["watchdog_lastMinute"].asInt
                            var staffBanTotal = objectAPI["staff_total"].asInt
                            if (staffBanTotal < lastStaffTotal) staffBanTotal = lastStaffTotal
                            if (lastStaffTotal == -1) lastStaffTotal = staffBanTotal else {
                                staffLastMin = staffBanTotal - lastStaffTotal
                                lastStaffTotal = staffBanTotal
                            }
                            tag = staffLastMin.toString() + ""
                            if (LiquidBounce.moduleManager.getModule(BanChecker::class.java)!!.state && alertValue.get() && mc.thePlayer != null && (!serverCheckValue.get() || isOnHypixel)) if (staffLastMin > 0) LiquidBounce.hud.addNotification(
                                Notification(
                                    "BanChecker",
                                    "Staffs banned $staffLastMin players in the last minute!",
                                    if (staffLastMin > 3) NotifyType.ERROR else NotifyType.WARNING,
                                    1500,
                                    alertTimeValue.get() * 500
                                )
                            ) else LiquidBounce.hud.addNotification(
                                Notification(
                                    "BanChecker",
                                    "Staffs didn't ban any player in the last minute.",
                                    NotifyType.SUCCESS,
                                    1500,
                                    alertTimeValue.get() * 500
                                )
                            )

                            // watchdog ban doesnt matter, open an issue if you want to add it.
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        if (LiquidBounce.moduleManager.getModule(BanChecker::class.java)!!.state && alertValue.get() && mc.thePlayer != null && (!serverCheckValue.get() || isOnHypixel)) LiquidBounce.hud.addNotification(
                            Notification(
                                "BanChecker",
                                "An error has occurred.",
                                NotifyType.ERROR,
                                1500,
                                alertTimeValue.get() * 500
                            )
                        )
                    }
                    checkTimer.reset()
                }
            }
        }
    }


    override fun onEnable() {
        thread = getBanCheckerThread() // new one every time
        thread?.start()
    }

    override fun onDisable() {
        thread?.stop()
    }

    val isOnHypixel: Boolean
        get() = !mc.isIntegratedServerRunning && mc.currentServerData.serverIP.contains("hypixel.net")
    override var tag = "Idle..."
}