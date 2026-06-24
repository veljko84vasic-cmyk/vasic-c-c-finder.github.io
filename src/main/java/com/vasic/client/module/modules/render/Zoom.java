package com.vasic.client.module.modules.render;

import com.vasic.client.module.Category;
import com.vasic.client.module.Module;
import org.lwjgl.glfw.GLFW;

public class Zoom extends Module {

    private static boolean active = false;
    private static final double ZOOM_FOV = 30.0;
    private double originalFov = 70.0;
    private double originalSensitivity = 0.5;

    public Zoom() {
        super("Zoom", "Zoom in like a spyglass (press C)", Category.RENDER, GLFW.GLFW_KEY_C);
    }

    @Override
    public void onEnable() {
        active = true;
        if (mc.options != null) {
            originalFov = mc.options.getFov().getValue();
            originalSensitivity = mc.options.getMouseSensitivity().getValue();
            mc.options.getFov().setValue((int) ZOOM_FOV);
            mc.options.getMouseSensitivity().setValue(originalSensitivity * 0.3);
        }
    }

    @Override
    public void onDisable() {
        active = false;
        if (mc.options != null) {
            mc.options.getFov().setValue((int) originalFov);
            mc.options.getMouseSensitivity().setValue(originalSensitivity);
        }
    }

    public static boolean isActive() {
        return active;
    }
}
