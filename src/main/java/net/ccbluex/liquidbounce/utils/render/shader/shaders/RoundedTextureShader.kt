/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.utils.render.shader.shaders

import net.ccbluex.liquidbounce.utils.render.shader.Shader
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL20
import java.awt.Color
import java.io.Closeable

/**
 * @author inf, remoted through pie's pc (shader not by me though)
 */
class RoundedTextureShader : Shader("roundedtexture.frag", "other") {
    override fun setupUniforms() {
        setupUniform("textureIn")
        setupUniform("rectSize")
        setupUniform("radius")
        setupUniform("alpha")
    }

    override fun updateUniforms() {
        // ignore
    }

    companion object {
        @JvmField
        val INSTANCE = RoundedTextureShader()

        @Suppress("NOTHING_TO_INLINE")
        inline fun draw(location: ResourceLocation, x: Float, y: Float, width: Float, height: Float, radius: Float, alpha: Float): RoundedTextureShader {
            val instance = INSTANCE

            instance.startShader()

            mc.textureManager.bindTexture(location)
            instance.setUniformi("textureIn", 0)
            instance.setUniformf("rectSize", width, height)
            instance.setUniformf("radius", radius)
            instance.setUniformf("alpha", alpha)

            GlStateManager.enableBlend()
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
            GlStateManager.enableAlpha()
            GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0f)
            drawTextureSpecifiedQuad(x, y, width, height)
            GlStateManager.disableBlend()

            instance.stopShader()

            return instance
        }
    }
}