/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.utils.render.shader.shaders;

import net.ccbluex.liquidbounce.utils.render.shader.FramebufferShader;
import org.lwjgl.opengl.GL20;


class GlowShader : FramebufferShader("glow.frag") {
    companion object {
        val GLOW_SHADER = GlowShader()
    }

    override fun setupUniforms() {
        setupUniform("texture")
        setupUniform("texelSize")
        setupUniform("color")
        setupUniform("fade")
        setupUniform("radius")
        setupUniform("targetAlpha")
    }

    override fun updateUniforms() {
        GL20.glUniform1i(getUniform("texture"), 0)
        GL20.glUniform2f(
            getUniform("texelSize"),
            1f / mc.displayWidth * renderScale,
            1f / mc.displayHeight * renderScale
        )
        GL20.glUniform3f(getUniform("color"), red, green, blue)
        GL20.glUniform1f(getUniform("fade"), fade.toFloat())
        GL20.glUniform1i(getUniform("radius"), radius)
        GL20.glUniform1f(getUniform("targetAlpha"), targetAlpha)
    }
}
