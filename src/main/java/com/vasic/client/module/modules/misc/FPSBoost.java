package com.vasic.client.module.modules.misc;

import com.vasic.client.module.Category;
import com.vasic.client.module.Module;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.option.ParticlesMode;
import org.lwjgl.glfw.GLFW;

public class FPSBoost extends Module {

    private static boolean active = false;

    public FPSBoost() {
        super("FPSBoost", "Kill particles and clouds for more FPS", Category.RENDER, GLFW.GLFW_KEY_P);
    }

    @Override
    public void onEnable() {
        active = true;
        if (mc.options != null) {
            mc.options.getParticles().setValue(ParticlesMode.MINIMAL);
            mc.options.getCloudRenderMode().setValue(CloudRenderMode.OFF);
            mc.options.getViewDistance().setValue(Math.min(mc.options.getViewDistance().getValue(), 8));
        }
    }

    @Override
    public void onDisable() {
        active = false;
        if (mc.options != null) {
            mc.options.getParticles().setValue(ParticlesMode.ALL);
            mc.options.getCloudRenderMode().setValue(CloudRenderMode.FANCY);
        }
    }

    public static boolean isActive() { return active; }
}
