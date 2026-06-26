package com.vasic.client.module.modules.render;

import com.vasic.client.module.Category;
import com.vasic.client.module.Module;
import org.lwjgl.glfw.GLFW;

public class CustomCrosshair extends Module {

    public static final int GRID_SIZE = 15;
    private static boolean active = false;
    private static int[][] pixels = new int[GRID_SIZE][GRID_SIZE];

    public CustomCrosshair() {
        super("Crosshair", "Custom crosshair overlay", Category.RENDER, GLFW.GLFW_KEY_UNKNOWN);
        loadDefaultCross();
    }

    @Override
    public void onEnable() { active = true; }

    @Override
    public void onDisable() { active = false; }

    public static boolean isActive() { return active; }

    public static int[][] getPixels() { return pixels; }

    public static int getPixel(int row, int col) {
        if (row < 0 || row >= GRID_SIZE || col < 0 || col >= GRID_SIZE) return 0;
        return pixels[row][col];
    }

    public static void setPixel(int row, int col, int color) {
        if (row >= 0 && row < GRID_SIZE && col >= 0 && col < GRID_SIZE) {
            pixels[row][col] = color;
        }
    }

    public static void clearPixel(int row, int col) {
        setPixel(row, col, 0);
    }

    public static void clearAll() {
        pixels = new int[GRID_SIZE][GRID_SIZE];
    }

    public static void setPixels(int[][] newPixels) {
        if (newPixels != null && newPixels.length == GRID_SIZE) {
            pixels = newPixels;
        }
    }

    public static void loadDefaultCross() {
        clearAll();
        int c = 0xFFFFFFFF;
        int mid = GRID_SIZE / 2;
        for (int i = mid - 4; i < mid; i++) pixels[i][mid] = c;
        for (int i = mid + 1; i <= mid + 4; i++) pixels[i][mid] = c;
        for (int j = mid - 4; j < mid; j++) pixels[mid][j] = c;
        for (int j = mid + 1; j <= mid + 4; j++) pixels[mid][j] = c;
        pixels[mid][mid] = c;
    }
}
