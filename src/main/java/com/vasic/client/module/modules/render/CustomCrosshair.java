package com.vasic.client.module.modules.render;

import com.vasic.client.module.Category;
import com.vasic.client.module.Module;
import org.lwjgl.glfw.GLFW;

public class CustomCrosshair extends Module {

    private static boolean active = false;
    private static int color = 0xFFFFFFFF;
    private static int size = 6;
    private static int gap = 3;
    private static int thickness = 1;
    private static boolean dot = true;

    public CustomCrosshair() {
        super("Crosshair", "Custom crosshair overlay", Category.RENDER, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onEnable() { active = true; }

    @Override
    public void onDisable() { active = false; }

    public static boolean isActive() { return active; }
    public static int getColor() { return color; }
    public static int getSize() { return size; }
    public static int getGap() { return gap; }
    public static int getThickness() { return thickness; }
    public static boolean hasDot() { return dot; }
}
