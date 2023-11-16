package net.ccbluex.liquidbounce.utils.render

import net.ccbluex.liquidbounce.utils.render.shader.shaders.RoundedRectShader
import java.awt.Color

object ShaderUtils {
    fun drawRoundedRect(x: Float, y: Float, x2: Float, y2: Float, radius: Float, color: Color) {
        RoundedRectShader.draw(x, y, x2, y2, radius, color)
    }

    fun drawRoundedRect(x: Float, y: Float, x2: Float, y2: Float, radius: Float, color: Int) {
        RoundedRectShader.draw(x, y, x2, y2, radius, Color(color))
    }

    fun drawFilledCircle(x: Float, y: Float, radius: Float, color: Color) {
        RoundedRectShader.draw(x - radius, y - radius, x + radius, y + radius, radius, color)
    }
}