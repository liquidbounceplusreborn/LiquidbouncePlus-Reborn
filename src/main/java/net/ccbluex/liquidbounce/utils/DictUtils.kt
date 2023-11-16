package net.ccbluex.liquidbounce.utils

import net.ccbluex.liquidbounce.LiquidBounce
import java.io.File
import kotlin.random.Random

object DictUtils {
//    private val dict = LiquidBounce::class.java.getResource("/assets/minecraft/liquidbounce+/dict.txt").readText().lines()

    private var dict: MutableList<String>? = null

    fun init() {
        val dictFile = File(LiquidBounce.fileManager.dir, "dict.txt")
        if (!dictFile.exists()) {
            dictFile.writeText(
                LiquidBounce::class.java.getResource("/assets/minecraft/liquidbounce+/dict.txt").readText()
            )
            ClientUtils.getLogger().info("[DictUtils] Extracted dictionary")
        }

        dict = mutableListOf()
        dict!!.addAll(dictFile.readText().lines().filter { !it.contains(Regex("\\s")) })
        ClientUtils.getLogger().info("[DictUtils] Loaded ${dict!!.size} words from dictionary")

    }

    private fun getInternal(format: String): String {
        var name = format
        name = name
            .replace(Regex("%w")) { dict!!.random() }
            .replace(Regex("%W")) { dict!!.random().capitalize() }
            .replace(Regex("%d")) { Random.nextInt(10).toString() }
            .replace(Regex("%c")) { "abcdefghijklmnopqrstuvwxyz".random().toString() }
            .replace(Regex("%C")) { "ABCDEFGHIJKLMNOPQRSTUVWXYZ".random().toString() }

        return name
    }

    fun get(format: String): String {
        var s = ""
        while (true) {
            s = getInternal(format)
            if (s.length <= 16)
                break;
        }
        return s
    }
}