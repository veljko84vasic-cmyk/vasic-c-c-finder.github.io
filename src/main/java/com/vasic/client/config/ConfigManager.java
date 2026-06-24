package com.vasic.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vasic.client.VasicClient;
import com.vasic.client.module.Module;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Path configFile;

    public ConfigManager() {
        configFile = FabricLoader.getInstance().getConfigDir().resolve("vasic-client.json");
    }

    public void save() {
        JsonObject root = new JsonObject();
        JsonObject modules = new JsonObject();

        for (Module module : VasicClient.getInstance().getModuleManager().getModules()) {
            JsonObject moduleObj = new JsonObject();
            moduleObj.addProperty("enabled", module.isEnabled());
            moduleObj.addProperty("keyBind", module.getKeyBind());
            modules.add(module.getName(), moduleObj);
        }

        root.add("modules", modules);

        try {
            Files.writeString(configFile, GSON.toJson(root));
        } catch (IOException e) {
            System.err.println("[Vasic Client] Failed to save config: " + e.getMessage());
        }
    }

    public void load() {
        if (!Files.exists(configFile)) return;

        try {
            String json = Files.readString(configFile);
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();

            if (root.has("modules")) {
                JsonObject modules = root.getAsJsonObject("modules");
                for (Module module : VasicClient.getInstance().getModuleManager().getModules()) {
                    if (modules.has(module.getName())) {
                        JsonObject moduleObj = modules.getAsJsonObject(module.getName());
                        if (moduleObj.has("enabled") && moduleObj.get("enabled").getAsBoolean()) {
                            module.setEnabled(true);
                        }
                        if (moduleObj.has("keyBind")) {
                            module.setKeyBind(moduleObj.get("keyBind").getAsInt());
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[Vasic Client] Failed to load config: " + e.getMessage());
        }
    }
}
