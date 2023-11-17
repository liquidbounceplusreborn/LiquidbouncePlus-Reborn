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
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Type

import net.ccbluex.liquidbounce.utils.misc.HttpUtils.get
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.IntegerValue
import kotlin.concurrent.thread

@ModuleInfo(
    name = "BanChecker",
    spacedName = "Ban Checker",
    description = "Checks for ban on Hypixel every minute and alert you if there is any.",
    category = ModuleCategory.WORLD
)
class BanChecker : Module() {
    private val notify = BoolValue("Notify", true)
    private val nofifyWhenNoBan = BoolValue("NotifyWhenNoBan", false)
    private val notifyStaff = BoolValue("NotifyStaff", true)
    private val notifyWatchdog = BoolValue("NotifyWatchdog", true)
    private val onlyOnHypixel = BoolValue("NotifyOnlyOnHypixel", true)
    private val alertTime = IntegerValue("AlertTime", 5, 1, 50, "s")

    var staffLastMin = 0
    var watchdogLastMin = 0
    private var lastStaffTotal = -1
    private var working = false
    private var timer = MSTimer()

    private fun reset() {
        working = false
        timer.time = -1
        staffLastMin = 0
        watchdogLastMin = 0
        lastStaffTotal = -1
    }

    private fun getThread(): Thread {
        return thread(start = false, isDaemon = true, name = "BanCheckerThread") {
            working = true

            try {
                val apiContent = get("https://api.plancke.io/hypixel/v1/punishmentStats")
                val jsonObject = JsonParser().parse(apiContent).getAsJsonObject()
                if (jsonObject["success"].asBoolean && jsonObject.has("record")) {
                    val objectAPI = jsonObject["record"].getAsJsonObject()
                    watchdogLastMin = objectAPI["watchdog_lastMinute"].asInt
                    var staffBanTotal = objectAPI["staff_total"].asInt
                    if (staffBanTotal < lastStaffTotal)
                        staffBanTotal = lastStaffTotal
                    if (lastStaffTotal == -1)
                        lastStaffTotal = staffBanTotal
                    else {
                        staffLastMin = staffBanTotal - lastStaffTotal
                        lastStaffTotal = staffBanTotal
                    }

                    tag = ((if (notifyStaff.get()) staffLastMin else 0)
                            + (if (notifyWatchdog.get()) watchdogLastMin else 0)).toString()

                    if (mc.thePlayer != null && notify.get()) {
                        if (notifyStaff.get() && !(onlyOnHypixel.get() && !isOnHypixel))
                            if (staffLastMin > 0)
                                LiquidBounce.hud.addNotification(
                                    Notification(name,
                                        "Staffs banned $staffLastMin players in the last minute!",
                                        Type.WARNING,
                                        alertTime.get() * 500
                                    )
                                )
                            else if (nofifyWhenNoBan.get())
                                LiquidBounce.hud.addNotification(
                                    Notification(name,
                                        "Staffs didn't ban any player in the last minute.",
                                        Type.SUCCESS,
                                        alertTime.get() * 500
                                    )
                                )

                        if (notifyWatchdog.get() && !(onlyOnHypixel.get() && !isOnHypixel))
                            if (watchdogLastMin > 0)
                                LiquidBounce.hud.addNotification(
                                    Notification(name,
                                        "Watchdog banned $watchdogLastMin players in the last minute!",
                                        Type.WARNING,
                                        alertTime.get() * 500
                                    )
                                )
                            else if (nofifyWhenNoBan.get())
                                LiquidBounce.hud.addNotification(
                                    Notification(name,
                                        "Watchdog didn't ban any player in the last minute.",
                                        Type.SUCCESS,
                                        alertTime.get() * 500
                                    )
                                )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                if (notify.get() && mc.thePlayer != null)
                    chat("BanChecker error")
            }

            working = false
        }

    }

    override fun onEnable() {
        reset()
    }

    @EventTarget
    fun onWorld(event: WorldEvent) {
        reset()
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (!timer.hasTimePassed(60_000L) || working)
            return

        timer.reset()

        getThread().start()
    }

    private val isOnHypixel: Boolean
        get() = !mc.isIntegratedServerRunning && mc.currentServerData.serverIP.contains("hypixel.net")
    override var tag = "Idle..."
}