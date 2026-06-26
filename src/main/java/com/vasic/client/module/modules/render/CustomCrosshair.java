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
    private static int red = 255, green = 255, blue = 255;

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
    public static int getRed() { return red; }
    public static int getGreen() { return green; }
    public static int getBlue() { return blue; }

    public static void setSize(int v) { size = Math.max(1, Math.min(20, v)); }
    public static void setGap(int v) { gap = Math.max(0, Math.min(15, v)); }
    public static void setThickness(int v) { thickness = Math.max(1, Math.min(5, v)); }
    public static void setDot(boolean v) { dot = v; }

    public static void setRed(int v) {
        red = Math.max(0, Math.min(255, v));
        updateColor();
    }

    public static void setGreen(int v) {
        green = Math.max(0, Math.min(255, v));
        updateColor();
    }

    public static void setBlue(int v) {
        blue = Math.max(0, Math.min(255, v));
        updateColor();
    }

    private static void updateColor() {
        color = 0xFF000000 | (red << 16) | (green << 8) | blue;
    }
}
