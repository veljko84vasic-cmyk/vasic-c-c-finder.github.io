package com.vasic.client.module.modules.movement;

import com.vasic.client.module.Category;
import com.vasic.client.module.Module;
import org.lwjgl.glfw.GLFW;

public class Sprint extends Module {

    public Sprint() {
        super("Sprint", "Always sprint when moving forward", Category.MOVEMENT, GLFW.GLFW_KEY_V);
    }

    @Override
    public void onTick() {
        if (mc.player != null && mc.player.input.movementForward > 0 && !mc.player.isSneaking()) {
            mc.player.setSprinting(true);
        }
    }
}
