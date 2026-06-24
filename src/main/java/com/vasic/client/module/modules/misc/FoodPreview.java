package com.vasic.client.module.modules.misc;

import com.vasic.client.module.Category;
import com.vasic.client.module.Module;
import org.lwjgl.glfw.GLFW;

public class FoodPreview extends Module {

    private static boolean active = false;

    public FoodPreview() {
        super("FoodPreview", "Show food restore amount on hunger bar", Category.HUD, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onEnable() { active = true; }

    @Override
    public void onDisable() { active = false; }

    public static boolean isActive() { return active; }
}
