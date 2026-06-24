package com.vasic.client.module.modules.render;

import com.vasic.client.module.Category;
import com.vasic.client.module.Module;
import org.lwjgl.glfw.GLFW;

public class Hitbox extends Module {

    public Hitbox() {
        super("Hitbox", "Show entity hitboxes", Category.RENDER, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onEnable() {
        if (mc.getEntityRenderDispatcher() != null) {
            mc.getEntityRenderDispatcher().setRenderHitboxes(true);
        }
    }

    @Override
    public void onDisable() {
        if (mc.getEntityRenderDispatcher() != null) {
            mc.getEntityRenderDispatcher().setRenderHitboxes(false);
        }
    }
}
