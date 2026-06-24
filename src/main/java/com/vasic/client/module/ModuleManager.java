package com.vasic.client.module;

import com.vasic.client.module.modules.misc.*;
import com.vasic.client.module.modules.movement.*;
import com.vasic.client.module.modules.player.*;
import com.vasic.client.module.modules.render.*;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class ModuleManager {

    private final List<Module> modules = new ArrayList<>();

    public void init() {
        // Render
        modules.add(new Fullbright());
        modules.add(new Zoom());
        modules.add(new NoFog());
        modules.add(new CustomCrosshair());
        modules.add(new Hitbox());
        modules.add(new FPSBoost());

        // HUD
        modules.add(new FPSDisplay());
        modules.add(new CPSCounter());
        modules.add(new PingDisplay());
        modules.add(new KeystrokesModule());
        modules.add(new CoordinatesDisplay());
        modules.add(new ArmorHud());
        modules.add(new SaturationDisplay());
        modules.add(new FoodPreview());
        modules.add(new ShieldStatus());
        modules.add(new Timer());

        // Movement
        modules.add(new Sprint());
        modules.add(new ToggleSneak());
        modules.add(new NoSlowdown());

        // Player
        modules.add(new AutoTool());
        modules.add(new FastPlace());
        modules.add(new NoFall());
    }

    public void onTick() {
        for (Module module : modules) {
            if (module.isEnabled()) {
                module.onTick();
            }
        }
    }

    public void onKey(int key) {
        if (key == GLFW.GLFW_KEY_UNKNOWN) return;
        for (Module module : modules) {
            if (module.getKeyBind() == key) {
                module.toggle();
            }
        }
    }

    public List<Module> getModules() {
        return modules;
    }

    public List<Module> getModulesByCategory(Category category) {
        List<Module> result = new ArrayList<>();
        for (Module module : modules) {
            if (module.getCategory() == category) {
                result.add(module);
            }
        }
        return result;
    }

    public Module getModule(String name) {
        for (Module module : modules) {
            if (module.getName().equalsIgnoreCase(name)) {
                return module;
            }
        }
        return null;
    }

    public List<Module> getEnabledModules() {
        List<Module> result = new ArrayList<>();
        for (Module module : modules) {
            if (module.isEnabled()) {
                result.add(module);
            }
        }
        return result;
    }
}
