package com.vasic.armorhud;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.Path;

public class HudConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("armorhud.json");

    public float x = 5;
    public float y = 5;
    public float scale = 1.0f;
    public boolean showPercentage = false;
    public boolean visible = true;
    public boolean horizontal = false;
    public boolean showOffhand = true;
    public boolean colorCoded = true;

    public static HudConfig load() {
        File file = CONFIG_PATH.toFile();
        if (file.exists()) {
            try (Reader reader = new FileReader(file)) {
                HudConfig loaded = GSON.fromJson(reader, HudConfig.class);
                if (loaded != null) return loaded;
            } catch (Exception e) {
                ArmorHudMod.LOGGER.error("Failed to load armorhud config", e);
            }
        }
        return new HudConfig();
    }

    public void save() {
        try (Writer writer = new FileWriter(CONFIG_PATH.toFile())) {
            GSON.toJson(this, writer);
        } catch (Exception e) {
            ArmorHudMod.LOGGER.error("Failed to save armorhud config", e);
        }
    }
}
