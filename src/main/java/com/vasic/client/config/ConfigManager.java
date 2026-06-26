package com.vasic.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vasic.client.VasicClient;
import com.vasic.client.hud.HudElement;
import com.vasic.client.module.Module;
import com.vasic.client.module.modules.render.CustomCrosshair;
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

        // Save modules
        JsonObject modules = new JsonObject();
        for (Module module : VasicClient.getInstance().getModuleManager().getModules()) {
            JsonObject obj = new JsonObject();
            obj.addProperty("enabled", module.isEnabled());
            obj.addProperty("keyBind", module.getKeyBind());
            modules.add(module.getName(), obj);
        }
        root.add("modules", modules);

        // Save HUD positions
        JsonObject hud = new JsonObject();
        for (HudElement el : VasicClient.getInstance().getHudRenderer().getElements()) {
            JsonObject obj = new JsonObject();
            obj.addProperty("x", el.getX());
            obj.addProperty("y", el.getY());
            hud.add(el.getId(), obj);
        }
        root.add("hud", hud);

        // Save crosshair settings
        JsonObject crosshair = new JsonObject();
        crosshair.addProperty("size", CustomCrosshair.getSize());
        crosshair.addProperty("gap", CustomCrosshair.getGap());
        crosshair.addProperty("thickness", CustomCrosshair.getThickness());
        crosshair.addProperty("dot", CustomCrosshair.hasDot());
        crosshair.addProperty("red", CustomCrosshair.getRed());
        crosshair.addProperty("green", CustomCrosshair.getGreen());
        crosshair.addProperty("blue", CustomCrosshair.getBlue());
        root.add("crosshair", crosshair);

        // Save username
        String customName = VasicClient.getCustomUsername();
        if (customName != null && !customName.isEmpty()) {
            root.addProperty("username", customName);
        }

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

            // Load modules
            if (root.has("modules")) {
                JsonObject modules = root.getAsJsonObject("modules");
                for (Module module : VasicClient.getInstance().getModuleManager().getModules()) {
                    if (modules.has(module.getName())) {
                        JsonObject obj = modules.getAsJsonObject(module.getName());
                        if (obj.has("enabled") && obj.get("enabled").getAsBoolean()) {
                            module.setEnabled(true);
                        }
                        if (obj.has("keyBind")) {
                            module.setKeyBind(obj.get("keyBind").getAsInt());
                        }
                    }
                }
            }

            // Load HUD positions
            if (root.has("hud")) {
                JsonObject hud = root.getAsJsonObject("hud");
                for (HudElement el : VasicClient.getInstance().getHudRenderer().getElements()) {
                    if (hud.has(el.getId())) {
                        JsonObject obj = hud.getAsJsonObject(el.getId());
                        el.setPosition(obj.get("x").getAsInt(), obj.get("y").getAsInt());
                    }
                }
            }
            // Load username
            if (root.has("username")) {
                VasicClient.setCustomUsername(root.get("username").getAsString());
            }

            // Load crosshair settings
            if (root.has("crosshair")) {
                JsonObject ch = root.getAsJsonObject("crosshair");
                if (ch.has("size")) CustomCrosshair.setSize(ch.get("size").getAsInt());
                if (ch.has("gap")) CustomCrosshair.setGap(ch.get("gap").getAsInt());
                if (ch.has("thickness")) CustomCrosshair.setThickness(ch.get("thickness").getAsInt());
                if (ch.has("dot")) CustomCrosshair.setDot(ch.get("dot").getAsBoolean());
                if (ch.has("red")) CustomCrosshair.setRed(ch.get("red").getAsInt());
                if (ch.has("green")) CustomCrosshair.setGreen(ch.get("green").getAsInt());
                if (ch.has("blue")) CustomCrosshair.setBlue(ch.get("blue").getAsInt());
            }
        } catch (Exception e) {
            System.err.println("[Vasic Client] Failed to load config: " + e.getMessage());
        }
    }
}
