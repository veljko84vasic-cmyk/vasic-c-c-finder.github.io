package com.vasic.client.module.modules.misc;

import com.vasic.client.module.Category;
import com.vasic.client.module.Module;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class CPSCounter extends Module {

    private static boolean active = false;
    private static final List<Long> leftClicks = new ArrayList<>();
    private static final List<Long> rightClicks = new ArrayList<>();
    private boolean wasLeftPressed = false;
    private boolean wasRightPressed = false;

    public CPSCounter() {
        super("CPS", "Show clicks per second", Category.HUD, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onEnable() { active = true; }

    @Override
    public void onDisable() { active = false; }

    @Override
    public void onTick() {
        boolean leftPressed = mc.options.attackKey.isPressed();
        boolean rightPressed = mc.options.useKey.isPressed();

        if (leftPressed && !wasLeftPressed) {
            leftClicks.add(System.currentTimeMillis());
        }
        if (rightPressed && !wasRightPressed) {
            rightClicks.add(System.currentTimeMillis());
        }

        wasLeftPressed = leftPressed;
        wasRightPressed = rightPressed;

        long now = System.currentTimeMillis();
        leftClicks.removeIf(t -> now - t > 1000);
        rightClicks.removeIf(t -> now - t > 1000);
    }

    public static boolean isActive() { return active; }
    public static int getLeftCPS() { return leftClicks.size(); }
    public static int getRightCPS() { return rightClicks.size(); }
}
