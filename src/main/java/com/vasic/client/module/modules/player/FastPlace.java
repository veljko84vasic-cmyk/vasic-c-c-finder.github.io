package com.vasic.client.module.modules.player;

import com.vasic.client.module.Category;
import com.vasic.client.module.Module;
import org.lwjgl.glfw.GLFW;

public class FastPlace extends Module {

    private static boolean active = false;

    public FastPlace() {
        super("FastPlace", "Remove block placement delay", Category.PLAYER, GLFW.GLFW_KEY_F);
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
