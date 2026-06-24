package com.vasic.client.module.modules.misc;

import com.vasic.client.module.Category;
import com.vasic.client.module.Module;
import org.lwjgl.glfw.GLFW;

public class KeystrokesModule extends Module {

    private static boolean active = false;

    public KeystrokesModule() {
        super("Keystrokes", "Show WASD and mouse buttons", Category.HUD, GLFW.GLFW_KEY_K);
    }

    @Override
    public void onEnable() { active = true; }

    @Override
    public void onDisable() { active = false; }

    public static boolean isActive() { return active; }
}
