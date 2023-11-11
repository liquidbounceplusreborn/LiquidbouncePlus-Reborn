package net.ccbluex.liquidbounce.features.module.modules.render;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.event.EventTarget;
import net.ccbluex.liquidbounce.event.Render3DEvent;
import net.ccbluex.liquidbounce.event.UpdateEvent;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura;
import net.ccbluex.liquidbounce.utils.render.AttackParticle;
import net.ccbluex.liquidbounce.utils.render.ColorUtils;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.ccbluex.liquidbounce.utils.timer.TimerUtils;
import net.ccbluex.liquidbounce.value.FloatValue;
import net.ccbluex.liquidbounce.value.IntegerValue;
import net.ccbluex.liquidbounce.value.ListValue;
import net.minecraft.util.Vec3;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;

@ModuleInfo(name = "AttackEffect", spacedName = "Attack Effect", description = "Gey", category = ModuleCategory.RENDER)
public class AttackEffect extends Module {

    private final IntegerValue amount = new IntegerValue("Amount", 8, 0, 30);
    private final ListValue colorModeValue = new ListValue("Color", new String[] {"Custom", "Health", "Rainbow", "Sky", "LiquidSlowly", "Fade", "Mixer"}, "Custom");
    private final IntegerValue colorRedValue = new IntegerValue("Red", 255, 0, 255);
    private final IntegerValue colorGreenValue = new IntegerValue("Green", 255, 0, 255);
    private final IntegerValue colorBlueValue = new IntegerValue("Blue", 255, 0, 255);
    private final FloatValue saturationValue = new FloatValue("Saturation", 1F, 0F, 1F);
    private final FloatValue brightnessValue = new FloatValue("Brightness", 1F, 0F, 1F);
    private final IntegerValue mixerSecondsValue = new IntegerValue("Seconds", 2, 1, 10);
    private final List<AttackParticle> particles = new LinkedList<>();
    private final TimerUtils timer = new TimerUtils();
    @EventTarget
    private void onUpdate(UpdateEvent event) {

       KillAura killAura = LiquidBounce.INSTANCE.getModuleManager().getModule(KillAura.class);
        if (killAura.getState()) {
            if (killAura.getTarget().hurtTime != 0) {
                for (int i = 1; i < amount.get(); ++i) {
                    particles.add(new AttackParticle(new Vec3(killAura.getTarget().posX + (Math.random() - 0.5) * 0.5, killAura.getTarget().posY + Math.random() + 0.5, killAura.getTarget().posZ + (Math.random() - 0.5) * 0.5)));
                }
            }
        }
    }
    @EventTarget
    private void onRender3D(Render3DEvent event){
            if (particles.isEmpty()) {
                return;
            }
            int i = 0;
            while ((double)i <= (double)timer.getTime() / 1.0E11) {
                particles.forEach(AttackParticle::updateWithoutPhysics);
                ++i;
            }
            particles.removeIf(particle -> mc.thePlayer.getDistanceSq(particle.position.xCoord, particle.position.yCoord, particle.position.zCoord) > 300.0);
            timer.reset();
            RenderUtils.renderParticles(particles,getColor());
    }
    public final Color getColor() {
        switch (colorModeValue.get()) {
            case "Custom":
                return new Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get());
            case "Rainbow":
                return new Color(RenderUtils.getRainbowOpaque(mixerSecondsValue.get(), saturationValue.get(), brightnessValue.get(), 0));
            case "Sky":
                return RenderUtils.skyRainbow(0, saturationValue.get(), brightnessValue.get());
            case "LiquidSlowly":
                return ColorUtils.LiquidSlowly(System.nanoTime(), 0, saturationValue.get(), brightnessValue.get());
            case "Mixer":
                return ColorMixer.getMixedColor(0, mixerSecondsValue.get());
            case "Fade":
                return ColorUtils.fade(new Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get()), 0, 100);
            default:
                return Color.white;
        }
    }
}
