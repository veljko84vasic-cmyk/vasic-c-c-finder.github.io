package com.vasic.client.module.modules.render;

import com.vasic.client.module.Category;
import com.vasic.client.module.Module;
import org.lwjgl.glfw.GLFW;

public class NoFog extends Module {

    private int originalDistance = 12;

    public NoFog() {
        super("NoFog", "Increase render distance to reduce fog effect", Category.RENDER, GLFW.GLFW_KEY_G);
    }

    @Override
    public void onEnable() {
        if (mc.options != null) {
            originalDistance = mc.options.getViewDistance().getValue();
            mc.options.getViewDistance().setValue(32);
        }
    }

    @Override
    public void onDisable() {
        if (mc.options != null) {
            mc.options.getViewDistance().setValue(originalDistance);
        }
    }
}
