package net.ccbluex.liquidbounce.utils

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import me.liuli.elixir.utils.HttpUtils

object ChangelogUtils {
    val changes = mutableListOf<Pair<String, String>>()

    fun update() {
        changes.clear()

        try {
            val s = HttpUtils.get("https://api.github.com/repos/liquidbounceplusreborn/LiquidbouncePlus-Reborn/actions/runs")
            val json: JsonObject = JsonParser().parse(s).asJsonObject
            val builds: JsonArray = json.entrySet().find { it.key == "workflow_runs" }!!.value.asJsonArray
            ClientUtils.getLogger().info("Found ${builds.size()} builds")

            builds.forEach { build ->
                val buildID = build.asJsonObject["head_commit"].asJsonObject["id"].asString.subSequence(0, 6).toString()
                val buildMsg = build.asJsonObject["head_commit"].asJsonObject["message"].asString
                if (!buildMsg.contains("Merge") && buildMsg.length <= 90) // arbitrary
                    changes.add(buildID to buildMsg)
            }
        } catch (_: Exception) {
            ClientUtils.getLogger().error("Failed to fetch changelog")
        }
    }
}