package com.vasic.armorhud;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class ArmorHudClient implements ClientModInitializer {

    public static HudConfig config;
    public static KeyBinding openConfigKey;

    @Override
    public void onInitializeClient() {
        config = HudConfig.load();

        openConfigKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.armorhud.open_config",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            "category.armorhud"
        ));

        ArmorHudRenderer.register(config);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openConfigKey.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new ArmorHudConfigScreen(config));
                }
            }
        });

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> config.save());
    }
}
