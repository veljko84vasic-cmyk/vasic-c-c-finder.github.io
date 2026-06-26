package com.vasic.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
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
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Path configFile;
    private final Map<String, int[]> savedHudPositions = new HashMap<>();

    public ConfigManager() {
        configFile = FabricLoader.getInstance().getConfigDir().resolve("vasic-client.json");
    }

    public Map<String, int[]> getSavedHudPositions() {
        return savedHudPositions;
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

        // Save crosshair pixel grid + outline
        JsonObject crosshair = new JsonObject();
        int[][] pixels = CustomCrosshair.getPixels();
        JsonArray grid = new JsonArray();
        for (int[] row : pixels) {
            JsonArray rowArr = new JsonArray();
            for (int pixel : row) {
                rowArr.add(pixel);
            }
            grid.add(rowArr);
        }
        crosshair.add("grid", grid);
        crosshair.addProperty("outline", CustomCrosshair.hasOutline());
        root.add("crosshair", crosshair);

        // Save username
        String customName = VasicClient.getCustomUsername();
        if (customName != null && !customName.isEmpty()) {
            root.addProperty("username", customName);
        }

        try {
            Files.writeString(configFile, GSON.toJson(root));
        } catch (IOException e) {
            System.err.println("[NebulaX] Failed to save config: " + e.getMessage());
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

            // Store HUD positions to apply later when elements are created
            if (root.has("hud")) {
                JsonObject hud = root.getAsJsonObject("hud");
                for (String key : hud.keySet()) {
                    JsonObject obj = hud.getAsJsonObject(key);
                    savedHudPositions.put(key, new int[]{obj.get("x").getAsInt(), obj.get("y").getAsInt()});
                }
            }

            // Load username
            if (root.has("username")) {
                VasicClient.setCustomUsername(root.get("username").getAsString());
            }

            // Load crosshair pixel grid + outline
            if (root.has("crosshair")) {
                JsonObject ch = root.getAsJsonObject("crosshair");
                if (ch.has("grid")) {
                    JsonArray grid = ch.getAsJsonArray("grid");
                    int gs = CustomCrosshair.GRID_SIZE;
                    int[][] px = new int[gs][gs];
                    for (int r = 0; r < Math.min(gs, grid.size()); r++) {
                        JsonArray row = grid.get(r).getAsJsonArray();
                        for (int c = 0; c < Math.min(gs, row.size()); c++) {
                            px[r][c] = row.get(c).getAsInt();
                        }
                    }
                    CustomCrosshair.setPixels(px);
                }
                if (ch.has("outline")) {
                    CustomCrosshair.setOutline(ch.get("outline").getAsBoolean());
                }
            }
        } catch (Exception e) {
            System.err.println("[NebulaX] Failed to load config: " + e.getMessage());
        }
    }
}
