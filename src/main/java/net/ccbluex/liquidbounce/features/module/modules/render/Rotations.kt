package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.combat.BowAimbot
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.ccbluex.liquidbounce.features.module.modules.exploit.Disabler
import net.ccbluex.liquidbounce.features.module.modules.movement.Fly
import net.ccbluex.liquidbounce.features.module.modules.movement.Sprint
import net.ccbluex.liquidbounce.features.module.modules.world.ChestAura
import net.ccbluex.liquidbounce.features.module.modules.world.Breaker
import net.ccbluex.liquidbounce.features.module.modules.world.Scaffold
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue

@ModuleInfo(name = "Rotations", description = "Allows you to see server-sided head and body rotations.", category = ModuleCategory.RENDER)
class Rotations : Module() {

    val headValue = BoolValue("Head", true)
    val bodyValue = BoolValue("Body", true)
    val fakeValue = BoolValue("FakeBody", true)
//    var mode = ListValue("Mode", arrayOf("No","FakeBody"),"FakeBody")
    var R = FloatValue("R", 255f, 0f, 255f)
    var G = FloatValue("G", 255f, 0f, 255f)
    var B = FloatValue("B", 255f, 0f, 255f)
    var Alpha = FloatValue("Alpha", 100f, 0f, 255f)

    private fun getState(module: Class<out Module>) = LiquidBounce.moduleManager[module]!!.state

    fun shouldRotate(): Boolean {
        val killAura = LiquidBounce.moduleManager.getModule(KillAura::class.java) as KillAura
        val disabler = LiquidBounce.moduleManager.getModule(Disabler::class.java) as Disabler
        val sprint = LiquidBounce.moduleManager.getModule(Sprint::class.java) as Sprint
        return getState(Scaffold::class.java) ||
                (getState(Sprint::class.java) && sprint.allDirectionsValue.get() && sprint.moveDirPatchValue.get()) ||
                (getState(KillAura::class.java) && killAura.target != null) ||
                (getState(Disabler::class.java) && disabler.canRenderInto3D) ||
                getState(BowAimbot::class.java) || getState(Breaker::class.java) ||
                getState(ChestAura::class.java) || getState(Fly::class.java)
    }
}