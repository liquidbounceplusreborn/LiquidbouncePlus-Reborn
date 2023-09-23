package net.ccbluex.liquidbounce.utils.render

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.utils.MinecraftInstance.mc
import net.ccbluex.liquidbounce.utils.render.shader.Shader
import net.ccbluex.liquidbounce.utils.render.shader.shaders.BackgroundShader
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.ResourceLocation
import java.io.File
import javax.imageio.ImageIO

abstract class Background(val backgroundFile: File) {

    companion object {

        fun createBackground(backgroundFile: File): Background {
            val background = when (backgroundFile.extension) {
                "png" -> ImageBackground(backgroundFile)
                "frag", "glsl", "shader" -> ShaderBackground(backgroundFile)
                else -> throw IllegalArgumentException("Invalid background file extension")
            }

            background.initBackground()
            return background
        }

    }

    protected abstract fun initBackground()

    abstract fun drawBackground(width: Int, height: Int)

}

class ImageBackground(backgroundFile: File) : Background(backgroundFile) {

    private val resourceLocation = ResourceLocation("${LiquidBounce.CLIENT_NAME.lowercase()}/background.png")

    override fun initBackground() {
        val image = ImageIO.read(backgroundFile.inputStream())
        mc.textureManager.loadTexture(resourceLocation, DynamicTexture(image))
    }

    override fun drawBackground(width: Int, height: Int) {
        mc.textureManager.bindTexture(resourceLocation)
        GlStateManager.color(1f, 1f, 1f, 1f)
        Gui.drawScaledCustomSizeModalRect(0, 0, 0f, 0f, width, height, width, height, width.toFloat(), height.toFloat())
    }


}

class ShaderBackground(backgroundFile: File) : Background(backgroundFile) {

    private lateinit var shader: Shader

    override fun initBackground() {
        shader = BackgroundShader(backgroundFile)
    }

    override fun drawBackground(width: Int, height: Int) {
        shader.startShader()

        val instance = Tessellator.getInstance()
        val worldRenderer = instance.worldRenderer
        worldRenderer.begin(7, DefaultVertexFormats.POSITION)
        worldRenderer.pos(0.0, height.toDouble(), 0.0).endVertex()
        worldRenderer.pos(width.toDouble(), height.toDouble(), 0.0).endVertex()
        worldRenderer.pos(width.toDouble(), 0.0, 0.0).endVertex()
        worldRenderer.pos(0.0, 0.0, 0.0).endVertex()
        instance.draw()

        shader.stopShader()
    }


}
