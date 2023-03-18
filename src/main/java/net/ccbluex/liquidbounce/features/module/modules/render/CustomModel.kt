package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.ListValue

@ModuleInfo(name = "CustomModel", description = "Customing your model", category = ModuleCategory.RENDER)
class CustomModel : Module() {

    val mode = ListValue("Mode", arrayOf("Freddy", "Rabbit"), "Freddy")
    var onlySelf = BoolValue("OnlySelf", false)
    var friends = BoolValue("Friends", false)

}