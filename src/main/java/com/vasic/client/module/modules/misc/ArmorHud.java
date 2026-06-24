package com.vasic.client.module.modules.misc;

import com.vasic.client.module.Category;
import com.vasic.client.module.Module;
import org.lwjgl.glfw.GLFW;

public class ArmorHud extends Module {

    private static boolean active = false;

    public ArmorHud() {
        super("ArmorHUD", "Show armor durability on screen", Category.HUD, GLFW.GLFW_KEY_H);
    }

    @Override
    public void onEnable() { active = true; }

    @Override
    public void onDisable() { active = false; }

    public static boolean isActive() { return active; }
}
