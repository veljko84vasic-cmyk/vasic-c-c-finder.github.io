package com.vasic.client.module.modules.misc;

import com.vasic.client.module.Category;
import com.vasic.client.module.Module;
import org.lwjgl.glfw.GLFW;

public class Timer extends Module {

    private static boolean active = false;

    public Timer() {
        super("Timer", "Show a session timer on the HUD", Category.MISC, GLFW.GLFW_KEY_U);
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
