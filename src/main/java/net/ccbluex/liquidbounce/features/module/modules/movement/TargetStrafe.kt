/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 * 
 * This code belongs to WYSI-Foundation. Please give credits when using this in your repository.
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.ccbluex.liquidbounce.features.module.modules.render.ColorMixer
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.MathHelper
import org.lwjgl.opengl.GL11
import java.awt.Color

@ModuleInfo(name = "TargetStrafe", description = "Strafe around your target. (Require Fly or Speed to be enabled)", category = ModuleCategory.MOVEMENT)
class TargetStrafe : Module() {
    private val radiusMode = ListValue("StrafeMode", arrayOf("TrueRadius", "Simple","Behind"), "Behind")
    val radius = FloatValue("Radius", 2.0f, 0.1f, 4.0f)
    private val render = BoolValue("Render", true)
    private val alwaysRender = BoolValue("Always-Render", true, { render.get() })
    private val modeValue = ListValue("KeyMode", arrayOf("Jump", "None"), "None")
    private val colorType = ListValue("Color", arrayOf("Custom", "Dynamic", "Rainbow", "Rainbow2", "Sky", "Fade", "Mixer"), "Custom")
    private val redValue = IntegerValue("Red", 255, 0, 255)
    private val greenValue = IntegerValue("Green", 255, 0, 255)
    private val blueValue = IntegerValue("Blue", 255, 0, 255)
    private val safewalk = BoolValue("SafeWalk", true)
    val thirdPerson = BoolValue("ThirdPerson", true)
    val always = BoolValue("Always",false)
    val onground = BoolValue("Ground",false)
    val air = BoolValue("Air",false)
    private val accuracyValue = IntegerValue("Accuracy", 0, 0, 59)
    private val thicknessValue = FloatValue("Thickness", 1F, 0.1F, 5F)
    private val mixerSecondsValue = IntegerValue("Mixer-Seconds", 2, 1, 10)
    private val outLine = BoolValue("Outline", true)
    private val saturationValue = FloatValue("Saturation", 0.7F, 0F, 1F)
    private val brightnessValue = FloatValue("Brightness", 1F, 0F, 1F)

    private  var killAura = LiquidBounce.moduleManager.getModule(KillAura::class.java) as KillAura
    private  var speed=LiquidBounce.moduleManager.getModule(Speed::class.java) as Speed
    private  var fly =LiquidBounce.moduleManager.getModule(Fly::class.java) as Fly

    var direction: Int = 1
    var lastView: Int = 0
    var hasChangedThirdPerson: Boolean = true

    val cansize: Float
        get() = when {
            radiusMode.get().toLowerCase() == "simple" ->
                45f / mc.thePlayer!!.getDistance(killAura.target!!.posX, mc.thePlayer!!.posY, killAura.target!!.posZ).toFloat()
            else -> 45f
        }
    val Enemydistance: Double
        get() = mc.thePlayer!!.getDistance(killAura.target!!.posX, mc.thePlayer!!.posY, killAura.target!!.posZ)

    val algorithm: Float
        get() = Math.max(Enemydistance - radius.get(), Enemydistance - (Enemydistance - radius.get() / (radius.get() * 2))).toFloat()


    override fun onEnable() {
        hasChangedThirdPerson = true
        lastView = mc.gameSettings.thirdPersonView
    }

    @EventTarget
    fun onMotion(event: MotionEvent) {
        if (thirdPerson.get()) { // smart change back lol
            if (canStrafe) {
                if (hasChangedThirdPerson) lastView = mc.gameSettings.thirdPersonView
                mc.gameSettings.thirdPersonView = 1
                hasChangedThirdPerson = false
            } else if (!hasChangedThirdPerson) {
                mc.gameSettings.thirdPersonView = lastView
                hasChangedThirdPerson = true
            }
        }

        if (event.eventState == EventState.PRE) {
            if (mc.thePlayer.isCollidedHorizontally)
                this.direction = -this.direction

            if (mc.gameSettings.keyBindLeft.pressed)
                this.direction = 1

            if (mc.gameSettings.keyBindRight.pressed)
                this.direction = -1
        }
    }

    @EventTarget
    fun onMove(event: MoveEvent) {
        if (canStrafe) {
            strafe(event, MovementUtils.getSpeed(event.x, event.z))

            if (safewalk.get() && checkVoid()) {
                event.isSafeWalk = true
            }
        }
    }


    fun strafe(event: MoveEvent, moveSpeed: Double) {
        if (killAura.target == null) return
        val target = killAura.target

        val rotYaw = RotationUtils.getRotationsEntity(killAura.target).yaw

        when (radiusMode.get()){
            "TrueRadius" -> {
                if (mc.thePlayer.getDistanceToEntity(target) <= radius.get())
                    setSpeed(event, moveSpeed, rotYaw, direction, 0.0)
                else
                    setSpeed(event, moveSpeed, rotYaw, direction, 1.0)
            }
            "Simple" -> {
                if (mc.thePlayer.getDistanceToEntity(target) <= radius.get())
                    setSpeed(event, moveSpeed, rotYaw, direction, 0.0)
                else
                    setSpeed(event, moveSpeed, rotYaw, direction, 1.0)
            }
            "Behind" -> {
                val xPos: Double = target!!.posX + -Math.sin(Math.toRadians(target.rotationYaw.toDouble())) * -2
                val zPos: Double = target.posZ + Math.cos(Math.toRadians(target.rotationYaw.toDouble())) * -2
                event.setX(moveSpeed * -MathHelper.sin(Math.toRadians(RotationUtils.getRotations1(xPos, target.posY, zPos)[0].toDouble())
                    .toFloat()))
                event.setZ(moveSpeed * MathHelper.cos(Math.toRadians(RotationUtils.getRotations1(xPos, target.posY, zPos)[0].toDouble())
                    .toFloat()))
            }
        }
    }

    fun setSpeed(
        moveEvent: MoveEvent, moveSpeed: Double, pseudoYaw: Float, pseudoStrafe: Int,
        pseudoForward: Double) {
        var yaw = pseudoYaw
        var forward = pseudoForward
        var strafe = pseudoStrafe
        var strafe2 = 0f

        if (forward != 0.0) {
            if (strafe > 0.0) {
                if (radiusMode.get().toLowerCase() == "trueradius")
                    yaw += (if (forward > 0.0) -cansize else cansize)
                strafe2 += (if (forward > 0.0) -45 / algorithm else 45 / algorithm)
            } else if (strafe < 0.0) {
                if (radiusMode.get().toLowerCase() == "trueradius")
                    yaw += (if (forward > 0.0) cansize else -cansize)
                strafe2 += (if (forward > 0.0) 45 / algorithm else -45 / algorithm)
            }
            strafe = 0
            if (forward > 0.0)
                forward = 1.0
            else if (forward < 0.0)
                forward = -1.0

        }
        if (strafe > 0.0)
            strafe = 1
        else if (strafe < 0.0)
            strafe = -1


        val mx = Math.cos(Math.toRadians(yaw + 90.0 + strafe2))
        val mz = Math.sin(Math.toRadians(yaw + 90.0 + strafe2))
        moveEvent.x = forward * moveSpeed * mx + strafe * moveSpeed * mz
        moveEvent.z = forward * moveSpeed * mz - strafe * moveSpeed * mx
    }


    val keyMode: Boolean
        get() = when (modeValue.get().toLowerCase()) {
            "jump" -> mc.gameSettings.keyBindJump.isKeyDown
            "none" -> mc.thePlayer.movementInput.moveStrafe != 0f || mc.thePlayer.movementInput.moveForward != 0f
            else -> false
        }

    val canStrafe: Boolean
        get() = (state && (speed.state || fly.state || always.get()) && (onground.get() && mc.thePlayer.onGround || !mc.thePlayer.onGround && air.get()) && killAura.state && killAura.target != null && !mc.thePlayer.isSneaking && keyMode)

    private fun checkVoid(): Boolean {
        for (x in -1..0) {
            for (z in -1..0) {
                if (isVoid(x, z)) {
                    return true
                }
            }
        }
        return false
    }

    private fun isVoid(X: Int, Z: Int): Boolean {
        if (mc.thePlayer.posY < 0.0) {
            return true
        }
        var off = 0
        while (off < mc.thePlayer.posY.toInt() + 2) {
            val bb: AxisAlignedBB = mc.thePlayer.entityBoundingBox.offset(X.toDouble(), (-off).toDouble(), Z.toDouble())
            if (mc.theWorld!!.getCollidingBoundingBoxes(mc.thePlayer as Entity, bb).isEmpty()) {
                off += 2
                continue
            }
            return false
            off += 2
        }
        return true
    }
    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        val target = killAura.target
        if ((canStrafe || alwaysRender.get()) && render.get()) {
            target?:return
            GL11.glPushMatrix()
            GL11.glTranslated(
                target.lastTickPosX + (target.posX - target.lastTickPosX) * mc.timer.renderPartialTicks - mc.getRenderManager().renderPosX,
                target.lastTickPosY + (target.posY - target.lastTickPosY) * mc.timer.renderPartialTicks - mc.getRenderManager().renderPosY,
                target.lastTickPosZ + (target.posZ - target.lastTickPosZ) * mc.timer.renderPartialTicks - mc.getRenderManager().renderPosZ
            )
            GL11.glEnable(GL11.GL_BLEND)
            GL11.glEnable(GL11.GL_LINE_SMOOTH)
            GL11.glDisable(GL11.GL_TEXTURE_2D)
            GL11.glDisable(GL11.GL_DEPTH_TEST)
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
            GL11.glRotatef(90F, 1F, 0F, 0F)

            if (outLine.get()) {
                GL11.glLineWidth(thicknessValue.get() + 1.25F)
                GL11.glColor3f(0F, 0F, 0F)
                GL11.glBegin(GL11.GL_LINE_LOOP)

                for (i in 0..360 step 60 - accuracyValue.get()) { // You can change circle accuracy  (60 - accuracy)
                    GL11.glVertex2f(Math.cos(i * Math.PI / 180.0).toFloat() * radius.get(), (Math.sin(i * Math.PI / 180.0).toFloat() * radius.get()))
                }

                GL11.glEnd()
            }

            val rainbow2 = ColorUtils.LiquidSlowly(System.nanoTime(), 0, saturationValue.get(), brightnessValue.get())
            val sky = RenderUtils.skyRainbow(0, saturationValue.get(), brightnessValue.get())
            val mixer = ColorMixer.getMixedColor(0, mixerSecondsValue.get())
            val fade = ColorUtils.fade(Color(redValue.get(), greenValue.get(), blueValue.get()), 0, 100)

            GL11.glLineWidth(thicknessValue.get())
            GL11.glBegin(GL11.GL_LINE_LOOP)

            for (i in 0..360 step 60 - accuracyValue.get()) { // You can change circle accuracy  (60 - accuracy)
                when (colorType.get()) {
                    "Custom" -> GL11.glColor3f(redValue.get() / 255.0f, greenValue.get() / 255.0f, blueValue.get() / 255.0f)
                    "Dynamic" -> if (canStrafe) GL11.glColor4f(redValue.get() / 255.0f, greenValue.get() / 255.0f, blueValue.get() / 255f,255f) else GL11.glColor4f(1f, 1f, 1f, 1f)
                    "Rainbow" -> {
                        val rainbow = Color(RenderUtils.getNormalRainbow(i, saturationValue.get(), brightnessValue.get()))
                        GL11.glColor3f(rainbow.red / 255.0f, rainbow.green / 255.0f, rainbow.blue / 255.0f)
                    }
                    "Rainbow2" -> GL11.glColor3f(rainbow2!!.red / 255.0f, rainbow2!!.green / 255.0f, rainbow2!!.blue / 255.0f)
                    "Sky" -> GL11.glColor3f(sky.red / 255.0f, sky.green / 255.0f, sky.blue / 255.0f)
                    "Mixer" -> GL11.glColor3f(mixer.red / 255.0f, mixer.green / 255.0f, mixer.blue / 255.0f)
                    else -> GL11.glColor3f(fade.red / 255.0f, fade.green / 255.0f, fade.blue / 255.0f)
                }
                GL11.glVertex2f(Math.cos(i * Math.PI / 180.0).toFloat() * radius.get(), (Math.sin(i * Math.PI / 180.0).toFloat() * radius.get()))
            }

            GL11.glEnd()

            GL11.glDisable(GL11.GL_BLEND)
            GL11.glEnable(GL11.GL_TEXTURE_2D)
            GL11.glEnable(GL11.GL_DEPTH_TEST)
            GL11.glDisable(GL11.GL_LINE_SMOOTH)

            GL11.glPopMatrix()

            GlStateManager.resetColor()
            GL11.glColor4f(1F, 1F, 1F, 1F)
        }
    }
}
