package com.vasic.client.module.modules.movement;

import com.vasic.client.module.Category;
import com.vasic.client.module.Module;
import org.lwjgl.glfw.GLFW;

public class ToggleSneak extends Module {

    public ToggleSneak() {
        super("ToggleSneak", "Hold sneak without holding the key", Category.MOVEMENT, GLFW.GLFW_KEY_Z);
    }

    @Override
    public void onTick() {
        if (mc.player != null && mc.currentScreen == null) {
            mc.options.sneakKey.setPressed(true);
        }
    }

    @Override
    public void onDisable() {
        if (mc.options != null) {
            mc.options.sneakKey.setPressed(false);
        }
    }
}
