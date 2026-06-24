package com.vasic.client.module.modules.render;

import com.vasic.client.module.Category;
import com.vasic.client.module.Module;
import org.lwjgl.glfw.GLFW;

public class NoFog extends Module {

    private static boolean active = false;

    public NoFog() {
        super("NoFog", "Remove fog rendering for better visibility", Category.RENDER, GLFW.GLFW_KEY_G);
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
