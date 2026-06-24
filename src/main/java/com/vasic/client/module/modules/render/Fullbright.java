package com.vasic.client.module.modules.render;

import com.vasic.client.module.Category;
import com.vasic.client.module.Module;
import org.lwjgl.glfw.GLFW;

public class Fullbright extends Module {

    public Fullbright() {
        super("Fullbright", "See clearly in the dark", Category.RENDER, GLFW.GLFW_KEY_B);
    }

    @Override
    public void onEnable() {
        if (mc.options != null) {
            mc.options.getGamma().setValue(16.0);
        }
    }

    @Override
    public void onDisable() {
        if (mc.options != null) {
            mc.options.getGamma().setValue(1.0);
        }
    }
}
