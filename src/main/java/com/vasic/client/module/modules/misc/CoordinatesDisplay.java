package com.vasic.client.module.modules.misc;

import com.vasic.client.module.Category;
import com.vasic.client.module.Module;
import org.lwjgl.glfw.GLFW;

public class CoordinatesDisplay extends Module {

    private static boolean active = true;

    public CoordinatesDisplay() {
        super("Coords", "Show XYZ coordinates", Category.HUD, GLFW.GLFW_KEY_UNKNOWN);
        toggle();
    }

    @Override
    public void onEnable() { active = true; }

    @Override
    public void onDisable() { active = false; }

    public static boolean isActive() { return active; }
}
