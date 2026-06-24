package com.vasic.client.module.modules.movement;

import com.vasic.client.module.Category;
import com.vasic.client.module.Module;
import org.lwjgl.glfw.GLFW;

public class NoSlowdown extends Module {

    private static boolean active = false;

    public NoSlowdown() {
        super("NoSlowdown", "Remove slowdown when using items", Category.MOVEMENT, GLFW.GLFW_KEY_N);
    }

    @Override
    public void onEnable() {
        active = true;
    }

    @Override
    public void onDisable() {
        active = false;
    }

    public static boolean isActive() {
        return active;
    }
}
