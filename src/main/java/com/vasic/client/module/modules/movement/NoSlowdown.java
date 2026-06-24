package com.vasic.client.module.modules.movement;

import com.vasic.client.module.Category;
import com.vasic.client.module.Module;
import org.lwjgl.glfw.GLFW;

public class NoSlowdown extends Module {

    public NoSlowdown() {
        super("NoSlowdown", "Keep sprinting while using items", Category.MOVEMENT, GLFW.GLFW_KEY_N);
    }

    @Override
    public void onTick() {
        if (mc.player != null && mc.player.isUsingItem() && mc.player.input.movementForward > 0) {
            mc.player.setSprinting(true);
        }
    }
}
