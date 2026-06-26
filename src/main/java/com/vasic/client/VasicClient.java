package com.vasic.client;

import com.vasic.client.config.ConfigManager;
import com.vasic.client.gui.ClickGui;
import com.vasic.client.hud.HudRenderer;
import com.vasic.client.module.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class VasicClient implements ClientModInitializer {

    public static final String NAME = "Vasic Client";
    public static final String VERSION = "1.0.0";

    private static VasicClient instance;
    private static String customUsername = "";
    private ModuleManager moduleManager;
    private ConfigManager configManager;
    private HudRenderer hudRenderer;
    private KeyBinding openGuiKey;

    @Override
    public void onInitializeClient() {
        instance = this;

        moduleManager = new ModuleManager();
        configManager = new ConfigManager();
        hudRenderer = new HudRenderer();

        moduleManager.init();
        configManager.load();

        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "Open Vasic GUI",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "Vasic Client"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
        HudRenderCallback.EVENT.register((drawContext, renderTickCounter) -> hudRenderer.render(drawContext));
    }

    private void onTick(MinecraftClient client) {
        if (openGuiKey.wasPressed()) {
            client.setScreen(new ClickGui());
        }
        moduleManager.onTick();
    }

    public static VasicClient getInstance() {
        return instance;
    }

    public ModuleManager getModuleManager() {
        return moduleManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public HudRenderer getHudRenderer() {
        return hudRenderer;
    }

    public static String getCustomUsername() {
        return customUsername;
    }

    public static void setCustomUsername(String name) {
        customUsername = name;
    }
}
